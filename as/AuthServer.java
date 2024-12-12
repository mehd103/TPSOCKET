package as;


import java.io.*;
import java.net.*;

import logs.JsonLogger;

public class AuthServer {

    private static final int PORT = 28414;
    private ListeAuth listeAuth;

    public AuthServer(String nomBD) {
        this.listeAuth = new ListeAuth(nomBD);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("AuthServer en attente de connexions sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Handle each client in a new thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientHost = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort();

            String request;
            while ((request = in.readLine()) != null) { // Handle multiple requests
                System.out.println("Requête reçue: " + request);
                String response = handleRequest(request, clientHost, clientPort, "TCP");
                out.println(response); // Send the response
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String handleRequest(String request, String clientHost, int clientPort, String protocol) {
        String operationType = "";
        String login = "";
        String result = "ERROR";
        String response = "ERROR invalid_request";

        try {
            if (request.startsWith("CHK ")) {
                operationType = "CHK";
                String[] parts = request.split(" ");
                if (parts.length == 3) {
                    login = parts[1];
                    String password = parts[2];
                    if (listeAuth.tester(login, password)) {
                        result = "GOOD";
                        response = "GOOD";
                    } else {
                        result = "BAD";
                        response = "BAD";
                    }
                }
            } else if (request.startsWith("ADD ")) {
                operationType = "ADD";
                String[] parts = request.split(" ");
                if (parts.length == 3) {
                    login = parts[1];
                    String password = parts[2];
                    if (listeAuth.creer(login, password)) {
                        result = "DONE";
                        response = "DONE";
                    }
                }
            } else if (request.startsWith("MOD ")) {
                operationType = "MOD";
                String[] parts = request.split(" ");
                if (parts.length == 3) {
                    login = parts[1];
                    String newPassword = parts[2];
                    if (listeAuth.mettreAJour(login, newPassword)) {
                        result = "DONE";
                        response = "DONE";
                    }
                }
            } else if (request.startsWith("DEL ")) {
                operationType = "DEL";
                String[] parts = request.split(" ");
                if (parts.length == 3) {
                    login = parts[1];
                    String password = parts[2];
                    if (listeAuth.supprimer(login, password)) {
                        result = "DONE";
                        response = "DONE";
                    }
                }
            }
        } finally {
            sendLogToLogServer(clientHost, clientPort, protocol, operationType, login, result);
        }
        return response;
    }

    private void sendLogToLogServer(String clientHost, int clientPort, String protocol, String operationType, String login, String result) {
        final String LOG_SERVER_HOST = "localhost"; // Adjust if the LogServer is on another machine
        final int LOG_SERVER_PORT = 3244;

        try (Socket logSocket = new Socket(LOG_SERVER_HOST, LOG_SERVER_PORT);
             PrintWriter out = new PrintWriter(logSocket.getOutputStream(), true)) {

            String logMessage = JsonLogger.getLogger()
                .reqToJson(clientHost, clientPort, protocol, operationType, login, result)
                .toString();
            
            out.println(logMessage); // Send the log message to the LogServer
            System.out.println("Log message sent to LogServer: " + logMessage);

        } catch (IOException e) {
            System.err.println("Failed to send log to LogServer: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AuthServer server = new AuthServer("authentif");
        server.start();
    }
}
