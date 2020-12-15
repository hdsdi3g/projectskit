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
package tv.hd3g.projectskit;

import java.io.File;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "projectskit")
@Validated
public class Config {

	@NotEmpty
	private String username;
	@NotEmpty
	private String token;
	@NotNull
	private File cacheDir;
	@NotEmpty
	private List<String> repositories;
	@NotNull
	private File dbFile;

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(final File cacheDir) {
		this.cacheDir = cacheDir;
	}

	public List<String> getRepositories() {
		return repositories;
	}

	public void setRepositories(final List<String> repositories) {
		this.repositories = repositories;
	}

	public void setDbFile(final File dbFile) {
		this.dbFile = dbFile;
	}

	public File getDbFile() {
		return dbFile;
	}
}
