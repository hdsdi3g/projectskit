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

import java.util.List;
import java.util.Objects;

public class RepositoryDepsDto {

	private final RepositoryDto reference;
	private final List<RepositoryDepsWarnDto> dependenciesWarns;
	private final RepositoryDepsWarnDto parentDependencyWarn;

	public RepositoryDepsDto(final RepositoryDto reference,
	                         final List<RepositoryDepsWarnDto> dependenciesWarns,
	                         final RepositoryDepsWarnDto parentDependencyWarn) {
		this.reference = Objects.requireNonNull(reference);
		this.dependenciesWarns = Objects.requireNonNull(dependenciesWarns);
		this.parentDependencyWarn = parentDependencyWarn;
	}

	public RepositoryDepsDto(final RepositoryDto reference,
	                         final List<RepositoryDepsWarnDto> dependenciesWarns) {
		this(reference, dependenciesWarns, null);
	}

	public boolean isEmpty() {
		return parentDependencyWarn == null
		       && dependenciesWarns.isEmpty();
	}

	public RepositoryDto getReference() {
		return reference;
	}

	public List<RepositoryDepsWarnDto> getDependenciesWarns() {
		return dependenciesWarns;
	}

	public RepositoryDepsWarnDto getParentDependencyWarn() {
		return parentDependencyWarn;
	}

}
