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

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tv.hd3g.projectskit.dto.RepositoriesDto;
import tv.hd3g.projectskit.dto.RepositoryDepsDto;
import tv.hd3g.projectskit.dto.RepositoryDepsWarnDto;
import tv.hd3g.projectskit.dto.RepositoryDto;

@Service
public class CrossProjectsAnalysisServiceImpl implements CrossProjectsAnalysisService {

	@Autowired
	private RepositoriesDto reposDto;

	@Override
	public List<RepositoryDepsDto> getDepsWithWarn() {
		final var allRepos = reposDto.getRepositories().values().stream().collect(toUnmodifiableList());
		final var reposByArtifact = allRepos.stream()
		        .filter(rDto -> rDto.getPomArtifact() != null)
		        .collect(toUnmodifiableMap(RepositoryDto::getPomArtifact, r -> r));

		return allRepos.stream()
		        .map(r -> {
			        final var dependenciesWithVersion = Optional.ofNullable(r.getDependenciesWithVersion())
			                .map(Map::entrySet)
			                .orElse(Set.of());
			        final var dependenciesWarns = dependenciesWithVersion.stream()
			                .map(depEntry -> {
				                final var usedArtifact = depEntry.getKey();
				                final var usedVersion = depEntry.getValue();
				                return compareDep(usedArtifact, usedVersion, reposByArtifact);
			                })
			                .filter(Objects::nonNull)
			                .collect(Collectors.toUnmodifiableList());

			        if (r.getPomParentArtifact() != null && r.getPomParentVersion() != null) {
				        final var parentDependencyWarn = compareDep(r.getPomParentArtifact(),
				                r.getPomParentVersion(), reposByArtifact);
				        return new RepositoryDepsDto(r, dependenciesWarns, parentDependencyWarn);
			        }
			        return new RepositoryDepsDto(r, dependenciesWarns);
		        })
		        .filter(not(RepositoryDepsDto::isEmpty))
		        .collect(Collectors.toUnmodifiableList());
	}

	private RepositoryDepsWarnDto compareDep(final String usedArtifact,
	                                         final String usedVersion,
	                                         final Map<String, RepositoryDto> reposByArtifact) {
		final var watchedProjectRepo = reposByArtifact.get(usedArtifact);
		if (watchedProjectRepo == null) {
			return null;
		}
		final var bestVersion = watchedProjectRepo.getPomVersion();
		if (usedVersion.equals(bestVersion)) {
			return null;
		}
		return new RepositoryDepsWarnDto(watchedProjectRepo, usedVersion);
	}

}
