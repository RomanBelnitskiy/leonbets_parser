package org.example.parser;

import org.example.model.League;
import org.example.dto.*;
import org.example.parser.task.LeagueTask;
import org.example.printer.ResultPrinter;
import org.example.service.DataService;
import org.example.service.DataServiceImpl;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Parser {

    private final ForkJoinPool forkJoinPool;
    private final DataService dataService;
    private final List<ResultPrinter> printers;

    public Parser(List<ResultPrinter> printers) {
        forkJoinPool = new ForkJoinPool(2);
        dataService = new DataServiceImpl();
        this.printers = printers;
    }

    public void run() {
        List<SportDto> sportDtoList = dataService.fetchSports();

        List<LeagueDto> footballLeagueDtos = getLeaguesBySportType(sportDtoList, SportType.FOOTBALL);
        processLeagues(footballLeagueDtos, SportType.FOOTBALL);
        List<LeagueDto> iceHockeyLeagueDtos = getLeaguesBySportType(sportDtoList, SportType.ICE_HOCKEY);
        processLeagues(iceHockeyLeagueDtos, SportType.ICE_HOCKEY);
        List<LeagueDto> tennisLeagueDtos = getLeaguesBySportType(sportDtoList, SportType.TENNIS);
        processLeagues(tennisLeagueDtos, SportType.TENNIS);
        List<LeagueDto> basketballLeagueDtos = getLeaguesBySportType(sportDtoList, SportType.BASKETBALL);
        processLeagues(basketballLeagueDtos, SportType.BASKETBALL);
    }

    private List<LeagueDto> getLeaguesBySportType(List<SportDto> sportDtos, SportType sportType) {
        return sportDtos.stream()
                .filter(sportDto -> sportType.getValue().equals(sportDto.getName()))
                .flatMap(sportDto -> sportDto.getRegions().stream())
                .flatMap(regionDto -> regionDto.getLeagues().stream()
                        .peek(leagueDto -> leagueDto.setRegionName(regionDto.getName())))
                .filter(LeagueDto::getTop)
                .sorted(Comparator.comparingInt(LeagueDto::getTopOrder))
                .collect(Collectors.toList());
    }

    private void processLeagues(List<LeagueDto> leagueDtos, SportType sportType) {
        if (!leagueDtos.isEmpty()) {
            List<League> leaguesResult = forkJoinPool.invoke(LeagueTask.builder()
                    .leagueDtos(leagueDtos)
                    .sportType(sportType)
                    .dataService(dataService)
                    .forkJoinPool(forkJoinPool)
                    .build());
            printers.forEach(resultPrinter -> resultPrinter.printLeagues(leaguesResult));
        }
    }
}
