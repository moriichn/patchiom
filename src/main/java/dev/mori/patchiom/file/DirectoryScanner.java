package dev.mori.patchiom.file;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class DirectoryScanner {

    private static final String MOD_ID = "axiom";

    public static Path findCandidate(Path directory, Consumer<String> progress) throws IOException {

        try (Stream<Path> paths = Files.walk(directory)) {
            Iterator<Path> iterator = paths.filter(Files::isRegularFile).filter(DirectoryScanner::isJar).iterator();

            while (iterator.hasNext()) {
                Path jar = iterator.next();

                progress.accept("Checking '" + jar.getFileName().toString() + "'");

                if (checkCandidate(jar)) {
                    return jar;
                }
            }
        }

        return null;
    }

    private static boolean isJar(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".jar");
    }

    private static boolean checkCandidate(Path jar) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            JarEntry entry = jarFile.getJarEntry("fabric.mod.json");

            if (entry == null || entry.isDirectory()) {
                return false;
            }

            try (InputStream input = jarFile.getInputStream(entry); InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                return json.has("id") && MOD_ID.equals(json.get("id").getAsString());
            }

        } catch (Exception e) {
            return false;
        }
    }
}
