package org.example;

import org.example.parser.Parser;
import org.example.printer.ResultPrinter;
import org.example.printer.impl.ConsoleResultPrinter;
import org.example.printer.impl.ExcelResultPrinter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<ResultPrinter> printers = List.of(
                new ConsoleResultPrinter(),
                new ExcelResultPrinter("top_leagues.xlsx"));

        Parser parser = new Parser(printers);
        parser.run();
    }
}