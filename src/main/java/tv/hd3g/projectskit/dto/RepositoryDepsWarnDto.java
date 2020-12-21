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

import java.util.Objects;

public class RepositoryDepsWarnDto {

	private final RepositoryDto dependency;
	private final String currentUsedVersion;

	public RepositoryDepsWarnDto(final RepositoryDto dependency, final String currentUsedVersion) {
		this.dependency = Objects.requireNonNull(dependency);
		this.currentUsedVersion = Objects.requireNonNull(currentUsedVersion);
	}

	public RepositoryDto getDependency() {
		return dependency;
	}

	public String getCurrentUsedVersion() {
		return currentUsedVersion;
	}

	@Override
	public String toString() {
		if (dependency.getCentralRepoPresence() == null) {
			return dependency.getPomArtifact() + ":" + dependency.getPomVersion() + " <= " + currentUsedVersion;
		} else {
			return dependency.getPomArtifact() + ":" + dependency.getPomVersion() + " (published) <= "
			       + currentUsedVersion;
		}
	}

}
