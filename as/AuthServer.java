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
                Socket clientSocket = serverSocket.accept(); 
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
            String protocol = "TCP"; 

            String request;
            while ((request = in.readLine()) != null) { 
                System.out.println("Requête reçue: " + request);
                String response = handleRequest(request, clientHost, clientPort, protocol);
                out.println(response);
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

            // Journalisation de la requête
            JsonLogger.log(clientHost, clientPort, protocol, operationType, login, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public static void main(String[] args) {
        AuthServer server = new AuthServer("authentif");
        server.start();
    }
}
