package org.example;
import java.io.*;
import java.net.Socket;

public class ClientConnection implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String inputLine;
            while (true) {
                if (clientSocket.isClosed()) {
                    break;
                }
                if ((inputLine = this.in.readLine()) != null) {
                    if (!inputLine.equals("-exit")) {
                        if (inputLine.startsWith("-file")) {
                            this.handleFileCommand(inputLine);
                        } else {
                            this.handleMessage(inputLine);
                        }
                    } else {
                        this.handleExitCommand();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
    }
    public void sendMessage(String message) {
        this.out.println(message);
    }

    public void disconnect() {
        try {
            this.in.close();
            this.out.close();
            this.clientSocket.close();
            Server.activeConnections.remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleExitCommand() {
        Server.broadcastMessage("[SERVER] " + this.clientName + " disconnected.");
        this.disconnect();
    }

    private void handleFileCommand(String command) {
        String filePath = command.substring(6).trim();
        this.saveFile(filePath);
    }

    private void handleMessage(String message) {
        String formattedMessage = "[" + this.clientName + "] " + message;
        Server.broadcastMessage(formattedMessage);
    }

    private void saveFile(String filePath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("server_files/" + filePath);

            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead;
                    if ((bytesRead = this.clientSocket.getInputStream().read(buffer)) == -1) {
                        Server.broadcastMessage("[SERVER] " + this.clientName + " sent a file: " + filePath);
                        break;
                    }
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (Throwable var6) {
                try {
                    fileOutputStream.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
                throw var6;
            }
            fileOutputStream.close();
        } catch (IOException var7) {
            var7.printStackTrace();
        }
    }

    public InputStream getInputStream() {
        try {
            return this.clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getClientName() {
        return this.clientName;
    }
}