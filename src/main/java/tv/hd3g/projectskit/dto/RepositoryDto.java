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
package tv.hd3g.projectskit.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RepositoryDto {

	private String name;
	private String description;
	private String homepage;
	private long id;
	private String lastTag;
	private long lastUpdate;
	private int openIssues;
	private int openBugIssues;
	private int branch;
	private String defaultBranch;
	private int openPR;
	private String pomArtifact;
	private String pomVersion;
	private String pomParentArtifact;
	private String pomParentVersion;
	private Map<String, String> dependenciesWithVersion;
	private String centralRepoPresence;

	@JsonIgnore
	public String getKey() {
		return RepositoriesDto.getKey(name);
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setHomepage(final String homepage) {
		this.homepage = homepage;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public void setLastTag(final String lastTag) {
		this.lastTag = lastTag;
	}

	public void setLastUpdate(final long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setOpenIssues(final int openIssues) {
		this.openIssues = openIssues;
	}

	public void setOpenBugIssues(final int openBugIssues) {
		this.openBugIssues = openBugIssues;
	}

	public void setBranch(final int branch) {
		this.branch = branch;
	}

	public void setDefaultBranch(final String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public void setOpenPR(final int openPR) {
		this.openPR = openPR;
	}

	public void setPomArtifact(final String pomArtifact) {
		this.pomArtifact = pomArtifact;
	}

	public void setPomVersion(final String pomVersion) {
		this.pomVersion = pomVersion;
	}

	public void setPomParentArtifact(final String pomParentArtifact) {
		this.pomParentArtifact = pomParentArtifact;
	}

	public void setPomParentVersion(final String pomParentVersion) {
		this.pomParentVersion = pomParentVersion;
	}

	public void setDependenciesWithVersion(final Map<String, String> dependenciesWithVersion) {
		this.dependenciesWithVersion = dependenciesWithVersion;
	}

	public void setCentralRepoPresence(final String centralRepoPresence) {
		this.centralRepoPresence = centralRepoPresence;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getHomepage() {
		return homepage;
	}

	public long getId() {
		return id;
	}

	public String getLastTag() {
		return lastTag;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public int getOpenIssues() {
		return openIssues;
	}

	public int getOpenBugIssues() {
		return openBugIssues;
	}

	public int getBranch() {
		return branch;
	}

	public int getOpenPR() {
		return openPR;
	}

	public String getPomArtifact() {
		return pomArtifact;
	}

	public String getPomVersion() {
		return pomVersion;
	}

	public String getPomParentArtifact() {
		return pomParentArtifact;
	}

	public String getPomParentVersion() {
		return pomParentVersion;
	}

	public Map<String, String> getDependenciesWithVersion() {
		return dependenciesWithVersion;
	}

	public String getCentralRepoPresence() {
		return centralRepoPresence;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	@JsonIgnore
	public String getPomArtifactId() {
		final var artifact = getPomArtifact();
		final var lastDot = artifact.lastIndexOf(".");
		return artifact.substring(lastDot + 1);
	}

	@JsonIgnore
	public String getPomGroupId() {
		final var artifact = getPomArtifact();
		final var lastDot = artifact.lastIndexOf(".");
		return artifact.substring(0, lastDot);
	}

}
