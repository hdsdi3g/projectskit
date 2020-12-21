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
package tv.hd3g.projectskit.controller;

import java.io.IOException;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import tv.hd3g.projectskit.Config;
import tv.hd3g.projectskit.dto.RepositoriesDto;
import tv.hd3g.projectskit.service.CrossProjectsAnalysisService;
import tv.hd3g.projectskit.service.GHProjectService;

@Controller
public class Homepage {

	@Autowired
	private RepositoriesDto reposDto;
	@Autowired
	private GHProjectService ghProjectService;
	@Autowired
	private CrossProjectsAnalysisService crossProjectsAnalysisService;
	@Autowired
	private Config config;
	@Autowired
	@Qualifier("yaml")
	private ObjectMapper mapper;

	@GetMapping("/")
	public String index(final Model model) {
		model.addAttribute("repositories", reposDto.getRepositories().values());
		model.addAttribute("projectWarns", crossProjectsAnalysisService.getDepsWithWarn());
		return "index";
	}

	@GetMapping("/redo/{id}")
	public ModelAndView redo(@PathVariable("id") @NotEmpty final long id) throws IOException {
		final var previousRepo = reposDto.getRepositories().values().stream()
		        .filter(r -> r.getId() == id)
		        .findFirst()
		        .orElseThrow(() -> new IllegalArgumentException("Can't found repo id: " + id));
		final var updated = ghProjectService.scanRepository(previousRepo.getName());
		reposDto.getRepositories().put(updated.getKey(), updated);

		mapper.writeValue(config.getDbFile(), reposDto);
		return new ModelAndView("redirect:/");
	}

}
