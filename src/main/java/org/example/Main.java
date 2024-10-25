package org.example;

import org.example.parser.Parser;
import org.example.printer.impl.ConsoleResultPrinter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        new Parser(List.of(new ConsoleResultPrinter())).run();
    }
}