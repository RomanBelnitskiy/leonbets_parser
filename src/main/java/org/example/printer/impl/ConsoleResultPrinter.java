package org.example.printer.impl;

import org.example.model.League;
import org.example.printer.ResultPrinter;

import java.util.List;

public class ConsoleResultPrinter implements ResultPrinter {
    @Override
    public void printLeagues(List<League> leagues) {
        for (League league : leagues) {
            System.out.println(league);
        }
    }
}
