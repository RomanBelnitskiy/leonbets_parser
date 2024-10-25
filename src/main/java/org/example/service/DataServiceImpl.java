package org.example.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.example.dto.EventDto;
import org.example.dto.MatchesDto;
import org.example.dto.SportDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DataServiceImpl implements DataService {
    private static final String SPORTS_URL = "https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2";
    private static final String EVENTS_URL = "https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id=%S&hideClosed=true&flags=reg,urlv2,mm2,rrc,nodup";
    private static final String EVENT_DETAILS_URL = "https://leonbets.com/api-2/betline/event/all?ctag=en-US&eventId=%s&flags=reg,urlv2,mm2,rrc,nodup,smg,outv2";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public DataServiceImpl() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public List<SportDto> fetchSports() {
        HttpRequest request = buildRequest(SPORTS_URL);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            CollectionType spotCollectionType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, SportDto.class);
            return mapper.readValue(response.body(), spotCollectionType);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return List.of();
    }

    @Override
    public MatchesDto fetchLeagueMatches(Long leagueId) {
        HttpRequest request = buildRequest(String.format(EVENTS_URL, leagueId));

        try {
            HttpResponse<String> footballResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(footballResponse.body(), MatchesDto.class);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    @Override
    public EventDto fetchEventDetails(Long eventId) {
        HttpRequest eventDetailsRequest = buildRequest(String.format(EVENT_DETAILS_URL, eventId));

        try {
            HttpResponse<String> eventDetailsResponse = client.send(eventDetailsRequest, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(eventDetailsResponse.body(), EventDto.class);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    private static HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
    }
}
