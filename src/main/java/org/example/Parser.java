package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
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

public class Parser {
    private static final String SPORTS_URL = "https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2";
    private static final String EVENTS_URL = "https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id=%S&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup";
    private static final String EVENT_DETAILS_URL = "https://leonbets.com/api-2/betline/event/all?ctag=en-US&eventId=%s&flags=reg,urlv2,mm2,rrc,nodup,smg,outv2";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public Parser() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void run() {
        List<Sport> sportList = fetchSports();

        List<League> footballLeagues = getLeaguesBySportType(sportList, SportType.FOOTBALL);
        List<League> iceHockeyLeagues = getLeaguesBySportType(sportList, SportType.ICE_HOCKEY);
        List<League> tennisLeagues = getLeaguesBySportType(sportList, SportType.TENNIS);
        List<League> basketballLeagues = getLeaguesBySportType(sportList, SportType.BASKETBALL);

        processLeagues(footballLeagues, SportType.FOOTBALL);
        processLeagues(iceHockeyLeagues, SportType.ICE_HOCKEY);
        processLeagues(tennisLeagues, SportType.TENNIS);
        processLeagues(basketballLeagues, SportType.BASKETBALL);
    }

    private List<Sport> fetchSports() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SPORTS_URL))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            CollectionType spotCollectionType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Sport.class);
            return mapper.readValue(response.body(), spotCollectionType);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return new ArrayList<>(0);
    }

    private void processLeagues(List<League> leagues, SportType sportType) {
        for (League league : leagues) {
            System.out.printf("%s, %s\n", sportType.getValue(), league.getName());

            Matches matches = fetchLeagueMatches(league.getId());
            if (matches == null) {
                continue;
            }

            List<Event> events = findTwoFirstEvents(matches);

            processEvents(events);
        }
    }

    private Matches fetchLeagueMatches(Long leagueId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(EVENTS_URL, leagueId)))
                .build();

        try {
            HttpResponse<String> footballResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(footballResponse.body(), Matches.class);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    private List<Event> findTwoFirstEvents(Matches matches) {
        return matches.getEvents().stream()
                .limit(2)
                .toList();
    }

    private void processEvents(List<Event> events) {
        for (Event event : events) {
            System.out.printf("\t%s, %s, %s\n", event.getName(), convertDateToUTC(event.getKickoff()), event.getId());

            Event eventDetails = fetchEventDetails(event.getId());
            if (eventDetails == null) {
                continue;
            }

            printEventMarketRunners(eventDetails);
        }
    }

    private Event fetchEventDetails(Long eventId) {
        HttpRequest eventDetailsRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format(EVENT_DETAILS_URL, eventId)))
                .build();

        try {
            HttpResponse<String> eventDetailsResponse = client.send(eventDetailsRequest, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(eventDetailsResponse.body(), Event.class);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    private void printEventMarketRunners(Event event) {
        for (Market market : event.getMarkets()) {
            System.out.println("\t\t" + market.getName());
            for (Runner runner : market.getRunners()) {
                System.out.printf("\t\t\t%s, %s, %s\n", runner.getName(), runner.getPrice(), runner.getId());
            }
        }
    }

    private static List<League> getLeaguesBySportType(List<Sport> sports, SportType sportType) {
        return sports.stream()
                .filter(sport -> sportType.getValue().equals(sport.getName()))
                .flatMap(sport -> sport.getRegions().stream())
                .flatMap(region -> region.getLeagues().stream())
                .filter(League::getTop)
                .sorted(Comparator.comparingInt(League::getTopOrder))
                .toList();
    }

    private static String convertDateToUTC(Date date) {
        Instant instant = date.toInstant();
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));
        return utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
    }
}
