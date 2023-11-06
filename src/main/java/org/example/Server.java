package org.example;
import org.example.ClientConnection;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = 12348;
    public static List<ClientConnection> activeConnections = new CopyOnWriteArrayList();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            try {
                System.out.println("Server is running on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected.");
                    ClientConnection clientConnection = new ClientConnection(clientSocket);
                    activeConnections.add(clientConnection);
                    Thread clientThread = new Thread(clientConnection);
                    clientThread.start();
                }
            } catch (Throwable var6) {
                try {
                    serverSocket.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
                throw var6;
            }
        } catch (IOException var7) {
            var7.printStackTrace();
        }
    }

    public static void broadcastMessage(String message) {
        Iterator var1 = activeConnections.iterator();
        while (var1.hasNext()) {
            ClientConnection clientConnection = (ClientConnection) var1.next();
            clientConnection.sendMessage(message);
        }
    }

    public void sendMessage(String message) {
        System.out.println(message);
    }

    public void handleClientCommand(ClientConnection client, String command) {
        if (command.equals("-exit")) {
            activeConnections.remove(client);
            client.disconnect();
        }

        if (command.startsWith("-file")) {
            String[] parts = command.split(" ");
            if (parts.length == 2) {
                String filePath = parts[1];
                this.saveFile(filePath, client.getInputStream());
            }
        }
    }

    private void saveFile(String filePath, InputStream inputStream) {
        try {
            OutputStream outputStream = new FileOutputStream("server_files/" + filePath);

            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Throwable var7) {
                try {
                    outputStream.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
                throw var7;
            }
            outputStream.close();
        } catch (IOException var8) {
            var8.printStackTrace();
        }
    }
}