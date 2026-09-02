package dev.mori.patchiom.cli;

import dev.mori.patchiom.log.OutputAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class Cli {

    private final OutputAdapter output;
    private final Scanner scanner;

    public Cli(OutputAdapter output) {
		this.output = output;
		this.scanner = new Scanner(System.in);
    }

    public Path askForInput() {
        System.out.print(Colors.YELLOW + "Input file or directory: " + Colors.RESET);

        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            output.error("No path specified.");
            return null;
        }

        Path path;

        try {
            path = Path.of(input);
        } catch (Exception e) {
            output.error("Invalid path.");
            return null;
        }

        if (!Files.exists(path)) {
            output.error("Path does not exist: " + path);
            return null;
        }

        return path;
    }

    public boolean askReplaceOriginal() {
        System.out.print(Colors.YELLOW + "Replace input file? [y/n]: " + Colors.RESET);
        String input = scanner.nextLine().trim().toLowerCase();
        return Stream.of("y", "yes", "1").anyMatch(input::equalsIgnoreCase);
    }

    public Path askOutputPath() {
        System.out.print(Colors.YELLOW + "Output file: " + Colors.RESET);
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }

        try {
            return Path.of(input);
        } catch (Exception e) {
            output.error("Invalid output path.");
            return null;
        }
    }

}
