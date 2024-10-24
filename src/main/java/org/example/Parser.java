package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.example.dto.EventDto;
import org.example.dto.LeagueDto;
import org.example.dto.MarketDto;
import org.example.model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class Parser {
    private static final String SPORTS_URL = "https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2";
    private static final String EVENTS_URL = "https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id=%S&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup";
    private static final String EVENT_DETAILS_URL = "https://leonbets.com/api-2/betline/event/all?ctag=en-US&eventId=%s&flags=reg,urlv2,mm2,rrc,nodup,smg,outv2";

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final ForkJoinPool forkJoinPool;

    public Parser() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        forkJoinPool = new ForkJoinPool(2);
    }

    public void run() {
        List<Sport> sportList = fetchSports();

        List<League> footballLeagues = getLeaguesBySportType(sportList, SportType.FOOTBALL);
        List<League> iceHockeyLeagues = getLeaguesBySportType(sportList, SportType.ICE_HOCKEY);
        List<League> tennisLeagues = getLeaguesBySportType(sportList, SportType.TENNIS);
        List<League> basketballLeagues = getLeaguesBySportType(sportList, SportType.BASKETBALL);

        if (!footballLeagues.isEmpty()) {
            List<LeagueDto> footballLeaguesResult = forkJoinPool.invoke(new LeagueTask(footballLeagues, SportType.FOOTBALL));
            printLeagues(footballLeaguesResult);
        }

        if (!iceHockeyLeagues.isEmpty()) {
            List<LeagueDto> iceHokeyLeaguesResult = forkJoinPool.invoke(new LeagueTask(iceHockeyLeagues, SportType.ICE_HOCKEY));
            printLeagues(iceHokeyLeaguesResult);
        }

        if (!tennisLeagues.isEmpty()) {
            List<LeagueDto> tennisLeaguesResult = forkJoinPool.invoke(new LeagueTask(tennisLeagues, SportType.TENNIS));
            printLeagues(tennisLeaguesResult);
        }

        if (!basketballLeagues.isEmpty()) {
            List<LeagueDto> basketballLeaguesResult = forkJoinPool.invoke(new LeagueTask(basketballLeagues, SportType.BASKETBALL));
            printLeagues(basketballLeaguesResult);
        }
    }

    private List<Sport> fetchSports() {
        HttpRequest request = buildRequest(SPORTS_URL);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            CollectionType spotCollectionType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Sport.class);
            return mapper.readValue(response.body(), spotCollectionType);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return List.of();
    }

    private void printLeagues(List<LeagueDto> leagues) {
        for (LeagueDto league : leagues) {
            System.out.println(league);
        }
    }

    private List<League> getLeaguesBySportType(List<Sport> sports, SportType sportType) {
        return sports.stream()
                .filter(sport -> sportType.getValue().equals(sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                .flatMap(region -> region.getLeagues().stream()
                        .peek(league -> league.setRegionName(region.getName())))
                .filter(League::getTop)
                .sorted(Comparator.comparingInt(League::getTopOrder))
                .collect(Collectors.toList());
    }

    private static String convertDateToUTC(Date date) {
        Instant instant = date.toInstant();
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));
        return utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
    }

    private static HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
    }

    private class LeagueTask extends RecursiveTask<List<LeagueDto>> {
        private final List<League> leagues;
        private final SportType sportType;

        public LeagueTask(List<League> leagues, SportType sportType) {
            this.leagues = leagues;
            this.sportType = sportType;
        }

        @Override
        protected List<LeagueDto> compute() {
            if (leagues.size() > 1) {
                return invokeAll(divideIntoSubtasks())
                        .stream()
                        .map(ForkJoinTask::join)
                        .flatMap(Collection::stream)
                        .toList();
            } else {
                return List.of(processLeague(leagues.get(0), sportType));
            }
        }

        private Collection<LeagueTask> divideIntoSubtasks() {
            List<LeagueTask> subtasks = new ArrayList<>();
            int mid = leagues.size() / 2;
            subtasks.add(new LeagueTask(leagues.subList(0, mid), sportType));
            subtasks.add(new LeagueTask(leagues.subList(mid, leagues.size()), sportType));
            return subtasks;
        }

        private LeagueDto processLeague(League league, SportType sportType) {
            String caption = String.format("%s, %s - %s", sportType.getValue(), league.getRegionName(), league.getName());
            LeagueDto leagueDto = LeagueDto.builder()
                    .caption(caption)
                    .order(league.getTopOrder())
                    .sport(sportType.getValue())
                    .build();
            Matches matches = fetchLeagueMatches(league.getId());
            if (matches == null) {
                return leagueDto;
            }

            List<Event> events = findTwoFirstEvents(matches);
            List<EventDto> eventDtoList = forkJoinPool.invoke(new EventTask(events));
            leagueDto.setEvents(eventDtoList);
            return leagueDto;
        }

        private List<Event> findTwoFirstEvents(Matches matches) {
            return matches.getEvents().stream()
                    .limit(2)
                    .toList();
        }

        private Matches fetchLeagueMatches(Long leagueId) {
            HttpRequest request = buildRequest(String.format(EVENTS_URL, leagueId));

            try {
                HttpResponse<String> footballResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                return mapper.readValue(footballResponse.body(), Matches.class);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }

            return null;
        }
    }

    private class EventTask extends RecursiveTask<List<EventDto>> {
        private final List<Event> events;

        public EventTask(List<Event> events) {
            this.events = events;
        }

        @Override
        protected List<EventDto> compute() {
            if (events.size() > 1) {
                return invokeAll(divideIntoSubtasks())
                        .stream()
                        .map(ForkJoinTask::join)
                        .flatMap(Collection::stream)
                        .toList();
            } else {
                return List.of(processEvent(events.get(0)));
            }
        }

        private Collection<EventTask> divideIntoSubtasks() {
            List<EventTask> subTasks = new ArrayList<>();
            int mid = events.size() / 2;
            subTasks.add(new EventTask(events.subList(0, mid)));
            subTasks.add(new EventTask(events.subList(mid, events.size())));
            return subTasks;
        }

        private EventDto processEvent(Event event) {
            String caption = String
                    .format("%s, %s, %s", event.getName(), convertDateToUTC(event.getKickoff()), event.getId());

            Event eventDetails = fetchEventDetails(event.getId());
            if (eventDetails == null) {
                return EventDto.builder()
                        .caption(caption)
                        .build();
            }

            List<MarketDto> marketDtoList = new ArrayList<>();
            for (Market market : eventDetails.getMarkets()) {
                List<String> runnersResult = new ArrayList<>();
                for (Runner runner : market.getRunners()) {
                    runnersResult.add(
                            String.format("%s, %s, %s", runner.getName(), runner.getPrice(), runner.getId())
                    );
                }

                MarketDto newMarketDto = MarketDto.builder()
                        .name(market.getName())
                        .build();
                if (marketDtoList.contains(newMarketDto)) {
                    MarketDto marketDto = marketDtoList.get(marketDtoList.indexOf(newMarketDto));
                    marketDto.getRunners().addAll(runnersResult);
                } else {
                    newMarketDto.setRunners(runnersResult);
                    marketDtoList.add(newMarketDto);
                }
            }

            return EventDto.builder()
                    .caption(caption)
                    .markets(marketDtoList)
                    .build();
        }

        private Event fetchEventDetails(Long eventId) {
            HttpRequest eventDetailsRequest = buildRequest(String.format(EVENT_DETAILS_URL, eventId));

            try {
                HttpResponse<String> eventDetailsResponse = client.send(eventDetailsRequest, HttpResponse.BodyHandlers.ofString());
                return mapper.readValue(eventDetailsResponse.body(), Event.class);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }

            return null;
        }
    }
}
