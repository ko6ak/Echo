package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Collectors;

public class EchoServerTest {

    @BeforeAll
    static void setUp() {
        Thread thread = new Thread(() -> {
            try {
                new EchoServer().start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @Test
    void echo() throws IOException {
        PrintStream stdout = System.out;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteStream));


        new EchoClient(4).start();

        System.setOut(stdout);

        String expected = """
                echo 0\r
                echo 1\r
                echo 2\r
                echo 3""";

        String actual = byteStream.toString();

        String str = actual.lines().filter(s -> s.startsWith("echo")).collect(Collectors.joining("\r\n"));

        Assertions.assertEquals(expected, str);

        expected = """
                Echoing: echo 0\r
                Echoing: echo 1\r
                Echoing: echo 2\r
                Echoing: echo 3""";

        str = actual.lines().filter(s -> s.startsWith("Echoing")).collect(Collectors.joining("\r\n"));

        Assertions.assertEquals(expected, str);
    }
}
