package org.example.service;

import org.example.dto.EventDto;
import org.example.dto.MatchesDto;
import org.example.dto.SportDto;

import java.util.List;

public interface DataService {

    List<SportDto> fetchSports();

    MatchesDto fetchLeagueMatches(Long leagueId);

    EventDto fetchEventDetails(Long eventId);
}
