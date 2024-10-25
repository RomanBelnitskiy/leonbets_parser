package org.example.parser.task;

import lombok.Builder;
import org.example.dto.EventDto;
import org.example.dto.LeagueDto;
import org.example.dto.MatchesDto;
import org.example.dto.SportType;
import org.example.model.Event;
import org.example.model.League;
import org.example.service.DataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Builder
public class LeagueTask extends RecursiveTask<List<League>> {
    private final List<LeagueDto> leagueDtos;
    private final SportType sportType;
    private final DataService dataService;
    private final ForkJoinPool forkJoinPool;

    public LeagueTask(List<LeagueDto> leagueDtos,
                      SportType sportType,
                      DataService dataService,
                      ForkJoinPool forkJoinPool
    ) {
        this.leagueDtos = leagueDtos;
        this.sportType = sportType;
        this.dataService = dataService;
        this.forkJoinPool = forkJoinPool;
    }

    @Override
    protected List<League> compute() {
        if (leagueDtos.size() > 1) {
            return invokeAll(divideIntoSubtasks())
                    .stream()
                    .map(ForkJoinTask::join)
                    .flatMap(Collection::stream)
                    .toList();
        } else {
            return List.of(processLeague(leagueDtos.get(0), sportType));
        }
    }

    private Collection<LeagueTask> divideIntoSubtasks() {
        List<LeagueTask> subtasks = new ArrayList<>();
        int mid = leagueDtos.size() / 2;
        subtasks.add(LeagueTask.builder()
                .leagueDtos(leagueDtos.subList(0, mid))
                .sportType(sportType)
                .dataService(dataService)
                .forkJoinPool(forkJoinPool)
                .build());
        subtasks.add(LeagueTask.builder()
                .leagueDtos(leagueDtos.subList(mid, leagueDtos.size()))
                .sportType(sportType)
                .dataService(dataService)
                .forkJoinPool(forkJoinPool)
                .build());
        return subtasks;
    }

    private League processLeague(LeagueDto leagueDto, SportType sportType) {
        String caption = String.format("%s, %s - %s", sportType.getValue(), leagueDto.getRegionName(), leagueDto.getName());
        League league = League.builder()
                .caption(caption)
                .order(leagueDto.getTopOrder())
                .sport(sportType.getValue())
                .build();
        MatchesDto matchesDto = dataService.fetchLeagueMatches(leagueDto.getId());
        if (matchesDto == null) {
            return league;
        }

        List<EventDto> eventDtos = findTwoFirstEvents(matchesDto);
        List<Event> eventList = forkJoinPool.invoke(EventTask.builder()
                .eventDtos(eventDtos)
                .dataService(dataService)
                .forkJoinPool(forkJoinPool)
                .build());
        league.setEvents(eventList);
        return league;
    }

    private List<EventDto> findTwoFirstEvents(MatchesDto matchesDto) {
        return matchesDto.getEvents().stream()
                .limit(2)
                .toList();
    }
}
