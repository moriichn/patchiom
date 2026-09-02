package dev.mori.patchiom;

import dev.mori.patchiom.cli.Cli;
import dev.mori.patchiom.file.DirectoryScanner;
import dev.mori.patchiom.file.FilePatcher;
import dev.mori.patchiom.log.OutputAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Main {

	public static void main(String[] args) {
		try {
			if (args.length > 2) {
				System.err.println("Usage: java -jar patcher.jar [input] [output]");
				System.exit(1);
			}

			if (args.length > 0) {
				runWithArgs(args);
			} else {
				runInteractive();
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void printBanner(OutputAdapter log) {
		log.success("PATCHiom by @morii.chn");
		log.progress("This software was made for educational and research purposes only.");
		log.progress("I do not encourage the unlawful use, modification, or distribution of third-party software.");
		log.progress("But in fact, I would download a car!");
		log.empty();
	}

	private static void runWithArgs(String[] args) throws Exception {
		Path input = Path.of(args[0]);
		OutputAdapter.Raw log = new OutputAdapter.Raw();
		printBanner(log);

		if (!Files.exists(input)) {
			throw new IllegalArgumentException("Input path does not exist: " + input);
		}

		if (Files.isDirectory(input)) {
			log.info("Scanning directory at '" + input + "'..");

			input = DirectoryScanner.findCandidate(input, System.out::println);

			if (input == null) {
				throw new IllegalArgumentException("No matching JAR found.");
			}

			log.success("Found matching JAR: " + input);
		}

		informedPatch(input, args.length > 1 ? Path.of(args[1]) : null, log);
	}

	private static void runInteractive() throws Exception {
		OutputAdapter log = new OutputAdapter.Formatted();
		printBanner(log);
		Cli cli = new Cli(log);

		Path input = cli.askForInput();

		if (input == null) {
			return;
		}

		if (Files.isDirectory(input)) {
			log.info("Scanning directory..");
			input = DirectoryScanner.findCandidate(input, log::progress);

			if (input == null) {
				log.error("No candidate found.");
				return;
			}

			log.success("Found candidate '" + input.getFileName().toString() + "'");
		} else {
			log.info("Selected " + input.getFileName().toString());
		}

		if (cli.askReplaceOriginal()) {
			informedPatch(input, null, log);
		} else {
			Path output = cli.askOutputPath();

			if (output == null) {
				log.error("No output path specified.");
				return;
			}

			informedPatch(input, output, log);
		}
	}

	private static void informedPatch(Path input, Path output, OutputAdapter log) throws Exception {
		boolean overwrite = output == null;
		Path dest = !overwrite ? output : tempFile(input);
		Path parent = dest.getParent();

		if (parent != null) {
			Files.createDirectories(parent);
		}

		log.info("Patching '" + input.getFileName().toString() + "'..");
		FilePatcher.patchJar(input, dest, log);
		log.success("Patched jar saved as '" + dest.getFileName().toString() + "'");

		if(overwrite) {
			Files.move(dest, input, StandardCopyOption.REPLACE_EXISTING);
			log.success("Copied to " + input.getFileName().toString());
		}
	}

	private static Path tempFile(Path input) throws Exception {
		return Files.createTempFile(input.getFileName().toString() + System.currentTimeMillis(), null);
	}
}
