package org.example.parser.task;

import lombok.Builder;
import org.example.dto.EventDto;
import org.example.dto.MarketDto;
import org.example.dto.RunnerDto;
import org.example.model.Event;
import org.example.model.Market;
import org.example.service.DataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static org.example.utils.DateUtil.convertDateToUTC;

@Builder
public class EventTask extends RecursiveTask<List<Event>> {
    private final List<EventDto> eventDtos;
    private final DataService dataService;
    private final ForkJoinPool forkJoinPool;

    public EventTask(List<EventDto> eventDtos, DataService dataService, ForkJoinPool forkJoinPool) {
        this.eventDtos = eventDtos;
        this.dataService = dataService;
        this.forkJoinPool = forkJoinPool;
    }

    @Override
    protected List<Event> compute() {
        if (eventDtos.size() > 1) {
            return invokeAll(divideIntoSubtasks())
                    .stream()
                    .map(ForkJoinTask::join)
                    .flatMap(Collection::stream)
                    .toList();
        } else {
            return List.of(processEvent(eventDtos.get(0)));
        }
    }

    private Collection<EventTask> divideIntoSubtasks() {
        List<EventTask> subTasks = new ArrayList<>();
        int mid = eventDtos.size() / 2;
        subTasks.add(EventTask.builder()
                .eventDtos(eventDtos.subList(0, mid))
                .dataService(dataService)
                .forkJoinPool(forkJoinPool)
                .build());
        subTasks.add(EventTask.builder()
                .eventDtos(eventDtos.subList(mid, eventDtos.size()))
                .dataService(dataService)
                .forkJoinPool(forkJoinPool)
                .build());
        return subTasks;
    }

    private Event processEvent(EventDto eventDto) {
        String caption = String
                .format("%s, %s, %s", eventDto.getName(), convertDateToUTC(eventDto.getKickoff()), eventDto.getId());

        EventDto eventDtoDetails = dataService.fetchEventDetails(eventDto.getId());
        if (eventDtoDetails == null) {
            return Event.builder()
                    .caption(caption)
                    .build();
        }

        List<Market> marketList = new ArrayList<>();
        for (MarketDto marketDto : eventDtoDetails.getMarkets()) {
            List<String> runnersResult = new ArrayList<>();
            for (RunnerDto runnerDto : marketDto.getRunners()) {
                runnersResult.add(
                        String.format("%s, %s, %s", runnerDto.getName(), runnerDto.getPrice(), runnerDto.getId())
                );
            }

            Market newMarket = Market.builder()
                    .name(marketDto.getName())
                    .build();
            if (marketList.contains(newMarket)) {
                Market market = marketList.get(marketList.indexOf(newMarket));
                market.getRunners().addAll(runnersResult);
            } else {
                newMarket.setRunners(runnersResult);
                marketList.add(newMarket);
            }
        }

        return Event.builder()
                .caption(caption)
                .markets(marketList)
                .build();
    }
}
