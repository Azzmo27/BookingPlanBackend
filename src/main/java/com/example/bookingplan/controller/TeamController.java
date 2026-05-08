package com.example.bookingplan.controller;

import com.example.bookingplan.dto.TeamDTO;
import com.example.bookingplan.model.Team;
import com.example.bookingplan.repository.TeamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public List<TeamDTO> getTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private TeamDTO toDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        return dto;
    }
}
