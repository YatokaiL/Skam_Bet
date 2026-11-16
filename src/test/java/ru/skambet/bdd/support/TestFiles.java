package ru.skambet.bdd.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Feature 19: files for upload. They are in src/test/resources/documents,
 * a too big file is made in a temp folder so that megabytes don't go to git.
 */
public final class TestFiles {

    public static final String BIG_FILE = "файл размером 6 МБ";

    private TestFiles() {
    }

    public static Path document(String name) {
        if (BIG_FILE.equals(name)) {
            return bigJpeg();
        }
        URL url = TestFiles.class.getResource("/documents/" + name);
        if (url == null) {
            throw new IllegalArgumentException("No test document " + name);
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] content(String name) {
        try {
            return Files.readAllBytes(document(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // a real jpeg header and 6 MB of zeros - bigger than the limit of 5 MB
    private static Path bigJpeg() {
        try {
            Path file = Files.createTempDirectory("skambet").resolve("big_passport.jpg");
            byte[] content = new byte[6 * 1024 * 1024];
            content[0] = (byte) 0xFF;
            content[1] = (byte) 0xD8;
            content[2] = (byte) 0xFF;
            Files.write(file, content);
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
