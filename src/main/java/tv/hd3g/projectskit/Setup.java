/*
 * This file is part of MailKit.
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
package tv.hd3g.projectskit;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import tv.hd3g.commons.IORuntimeException;
import tv.hd3g.projectskit.dto.RepositoriesDto;
import tv.hd3g.projectskit.dto.RepositoryDto;
import tv.hd3g.projectskit.service.GHProjectService;

@Configuration
public class Setup {
	private static final Logger log = LogManager.getLogger();

	@Autowired
	private Config config;

	@Bean
	public MessageSource messageSource() {
		return new ResourceBundleMessageSource();
	}

	@Bean
	public OkHttpClient getOkHttpClient(final Cache cache) {
		return new OkHttpClient.Builder().cache(cache).build();
	}

	@Bean
	public GitHub getGitHub(final OkHttpClient client) throws IOException {
		return GitHubBuilder.fromEnvironment()
		        .withConnector(new OkHttpConnector(client))
		        .withPassword(config.getUsername(), config.getToken())
		        .build();
	}

	@Bean
	public Cache getHttpClientCache() {
		return new Cache(config.getCacheDir(), 10L * 1024 * 1024);
	}

	@Bean("yaml")
	public ObjectMapper getYamlObjectMapper() {
		return new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
	}

	@Bean
	public RepositoriesDto getRepositoriesDto(final GHProjectService ghProjectService,
	                                          @Qualifier("yaml") final ObjectMapper mapper) throws IOException {
		final RepositoriesDto reposDto;
		if (config.getDbFile().exists()) {
			reposDto = mapper.readValue(config.getDbFile(), RepositoriesDto.class);

			config.getRepositories().stream()
			        .filter(r -> reposDto.getRepositories().containsKey(RepositoriesDto.getKey(r)) == false)
			        .forEach(r -> updateRepoDto(r, mapper, ghProjectService, reposDto));
		} else {
			reposDto = new RepositoriesDto();

			final var repos = new LinkedHashMap<String, RepositoryDto>();
			reposDto.setRepositories(repos);

			config.getRepositories()
			        .forEach(r -> updateRepoDto(r, mapper, ghProjectService, reposDto));
		}

		log.info("Load last state is done ({} repositories)", reposDto.getRepositories().size());
		return reposDto;
	}

	private void updateRepoDto(final String repoName,
	                           final ObjectMapper mapper,
	                           final GHProjectService ghProjectService,
	                           final RepositoriesDto reposDto) {
		final var repo = ghProjectService.scanRepository(repoName);
		reposDto.getRepositories().put(repo.getKey(), repo);
		try {
			/**
			 * Write all for each scan: if troubles, don't loose firsts scans...
			 */
			mapper.writeValue(config.getDbFile(), reposDto);
		} catch (final IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
