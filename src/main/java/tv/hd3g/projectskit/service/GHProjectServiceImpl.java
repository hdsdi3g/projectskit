/*
 * This file is part of projectskit.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.projectskit.service;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.kohsuke.github.GHIssueState.OPEN;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.hd3g.commons.IORuntimeException;
import tv.hd3g.projectskit.dto.RepositoryDto;

@Service
public class GHProjectServiceImpl implements GHProjectService {
	private static final Logger log = LogManager.getLogger();

	private static final MavenXpp3Reader pomReader = new MavenXpp3Reader();

	@Autowired
	private GitHub github;
	@Autowired
	private OkHttpClient okHttpClient;

	@Value("${projectskit.mavenCentralRepo:https://repo1.maven.org/maven2}")
	private String mavenCentralRepo;

	@Override
	public Map<String, GHRepository> getUserRepositories(final String username) {
		try {
			return github.getUser(username)
			        .listRepositories()
			        .toSet()
			        .stream()
			        .collect(toUnmodifiableMap(GHRepository::getFullName, r -> r));
		} catch (final IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public RepositoryDto scanRepository(final String repoName) {
		try {
			log.info("Start scan repo: {}", repoName);
			final var repoDto = new RepositoryDto();

			final var ghRepo = github.getRepository(repoName);
			Objects.requireNonNull(ghRepo, "Can't get repository " + repoName + " from GitHub");

			repoDto.setName(ghRepo.getFullName());
			repoDto.setDescription(ghRepo.getDescription());
			repoDto.setHomepage(ghRepo.getHomepage());
			repoDto.setId(ghRepo.getId());
			setLastTag(repoDto, ghRepo);
			repoDto.setBranch(ghRepo.getBranches().size());
			repoDto.setDefaultBranch(ghRepo.getDefaultBranch());
			repoDto.setLastUpdate(System.currentTimeMillis());
			final var allOpenIssues = ghRepo.getIssues(OPEN);
			repoDto.setOpenIssues(allOpenIssues.size());
			repoDto.setOpenBugIssues((int) allOpenIssues.stream()
			        .filter(i -> i.getLabels().stream()
			                .map(GHLabel::getName)
			                .anyMatch(l -> l.equalsIgnoreCase("bug")))
			        .count());
			repoDto.setOpenPR(ghRepo.getPullRequests(OPEN).size());

			final var oModel = getModel(ghRepo);
			final var oModelParent = oModel.map(Model::getParent);

			final var pomArtifact = oModel.map(m -> toGroupArtifactName(m, oModelParent)).orElse(null);
			repoDto.setPomArtifact(pomArtifact);
			final var pomVersion = oModel.flatMap(m -> Optional.ofNullable(m.getVersion()))
			        .or(() -> oModelParent.flatMap(p -> Optional.ofNullable(p.getVersion())))
			        .orElse("0.0.0");
			repoDto.setPomVersion(pomVersion);

			oModelParent.ifPresent(pomParent -> repoDto.setPomParentArtifact(toGroupArtifactName(pomParent)));
			oModelParent.ifPresent(pomParent -> repoDto.setPomParentVersion(pomParent.getVersion()));

			final var allDependencies = oModel.map(Model::getDependencies)
			        .flatMap(Optional::ofNullable)
			        .orElse(List.of());
			final var selectedDependencies = allDependencies.stream()
			        .filter(d -> "true".equalsIgnoreCase(d.getOptional()) == false)
			        .filter(d -> d.getVersion() != null && d.getVersion().isEmpty() == false)
			        .collect(toUnmodifiableList());
			repoDto.setDependenciesWithVersion(selectedDependencies.stream()
			        .map(GHProjectServiceImpl::toGroupArtifactName)
			        .distinct()
			        .collect(toUnmodifiableMap(gRArName -> gRArName,
			                gRArName -> selectedDependencies.stream()
			                        .filter(d -> gRArName.equals(toGroupArtifactName(d)))
			                        .findFirst()
			                        .get()
			                        .getVersion())));

			if (pomArtifact != null && oModel.isPresent()) {
				final var packaging = Optional.ofNullable(oModel.get().getPackaging()).orElse("jar");
				final var url = Stream.of(mavenCentralRepo,
				        pomArtifact.replace(".", "/"),
				        pomVersion,
				        oModel.get().getArtifactId() + "-" + pomVersion + "." + packaging)
				        .collect(joining("/"));
				log.info("Get Maven central artifact from {}", url);
				final Response response;
				response = okHttpClient.newCall(new Request.Builder().url(url).head().build())
				        .execute();
				if (response.isSuccessful()) {
					repoDto.setCentralRepoPresence(mavenCentralRepo + "/"
					                               + pomArtifact.replace(".", "/") + "/"
					                               + pomVersion);
				}
				response.close();
			}

			return repoDto;
		} catch (final IOException e) {
			throw new IORuntimeException(e);
		}
	}

	private void setLastTag(final RepositoryDto repoDto, final GHRepository ghRepo) throws IOException {
		try {
			repoDto.setLastTag(Stream.of(ghRepo.getRefs("tags"))
			        .map(GHRef::getRef)
			        .map(t -> t.replace("refs/tags/", ""))
			        .sorted(GHProjectServiceImpl::compareBestRecentVersion)
			        .findFirst()
			        .orElse(null));
		} catch (final GHFileNotFoundException e) {
			repoDto.setLastTag(null);
		}
	}

	private static int compareBestRecentVersion(final String l, final String r) {
		final var vL = new DefaultArtifactVersion(l);
		final var vR = new DefaultArtifactVersion(r);
		return vR.compareTo(vL);
	}

	private static final String toGroupArtifactName(final Model m, final Optional<Parent> oP) {
		return Optional.ofNullable(m.getGroupId()).or(() -> oP.map(Parent::getGroupId)).orElse("")
		       + "."
		       + Optional.ofNullable(m.getArtifactId()).or(() -> oP.map(Parent::getArtifactId)).orElse("");
	}

	private static final String toGroupArtifactName(final Parent p) {
		return p.getGroupId() + "." + p.getArtifactId();
	}

	private static final String toGroupArtifactName(final Dependency d) {
		return d.getGroupId() + "." + d.getArtifactId();
	}

	private Optional<Model> getModel(final GHRepository ghRepo) throws IOException {
		try {
			return Optional.ofNullable(pomReader.read(ghRepo.getFileContent("/pom.xml").read()));
		} catch (final GHFileNotFoundException e) {
			log.error("Can't found pom.xml from repo {}", ghRepo.getName());
			return Optional.empty();
		} catch (final XmlPullParserException e) {
			log.error("Can't read pom.xml from repo {}", ghRepo.getName(), e);
			return Optional.empty();
		}
	}

}
