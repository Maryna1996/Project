package org.example;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.net.Socket;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ServerTest {
    private Server server;

    @Before
    public void setUp() {
        server = new Server();
    }

    @After
    public void tearDown() {
        server = null;
    }

    @Test
    public void testSendMessage() {
        ByteArrayOutputStream consoleOutputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(consoleOutputStream));

        Thread clientThread = new Thread(() -> {
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("Hello");
                String response = in.readLine();
                System.out.println("Received response from the server: " + response);

                out.println("-exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clientThread.start();
        server.main(new String[]{});
        clientThread.interrupt();

        System.setOut(originalOut);

        String consoleOutput = consoleOutputStream.toString();
        String[] outputLines = consoleOutput.split(System.lineSeparator());

        assertTrue(outputLines[0].contains("Server is running on port 12345"));
        assertEquals("New client connected.", outputLines[1]);
        assertEquals("Received response from the server: Hello", outputLines[2]);
    }

    @Test
    public void testBroadcastMessage() {
        ByteArrayOutputStream consoleOutputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(consoleOutputStream));

        Thread client1Thread = new Thread(() -> {
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println("Hello from Client 1");
                String response = in.readLine();
                System.out.println("Received response from the server: " + response);

                out.println("-exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread client2Thread = new Thread(() -> {
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                out.println("Hello from Client 2");
                String response = in.readLine();
                System.out.println("Received response from the server: " + response);

                out.println("-exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        client1Thread.start();
        client2Thread.start();
        server.main(new String[]{});
        client1Thread.interrupt();
        client2Thread.interrupt();

        System.setOut(originalOut);

        String consoleOutput = consoleOutputStream.toString();
        String[] outputLines = consoleOutput.split(System.lineSeparator());

        assertTrue(outputLines[0].contains("Server is running on port 12345"));
        assertEquals("New client connected.", outputLines[1]);
        assertEquals("Received response from the server: Hello from Client 1", outputLines[2]);
        assertEquals("Received response from the server: Hello from Client 2", outputLines[3]);
    }
}