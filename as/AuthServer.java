package as;

import java.io.*;
import java.net.*;

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
            String request;
            while ((request = in.readLine()) != null) { // Handle multiple requests
                System.out.println("Requête reçue: " + request);
                String response = handleRequest(request);
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

    private String handleRequest(String request) {
        if (request.startsWith("CHK ")) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String login = parts[1];
                String password = parts[2];
                if (listeAuth.tester(login, password)) {
                    return "GOOD";
                } else {
                    return "BAD";
                }
            }
        } else if (request.startsWith("ADD ")) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String login = parts[1];
                String password = parts[2];
                if (listeAuth.ajouter(login, password)) {
                    return "DONE";
                }
            }
        } else if (request.startsWith("MOD ")) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String login = parts[1];
                String newPassword = parts[2];
                if (listeAuth.mettreAJour(login, newPassword)) {
                    return "DONE";
                }
            }
        } else if (request.startsWith("DEL ")) {
            String[] parts = request.split(" ");
            if (parts.length == 3) {
                String login = parts[1];
                String password = parts[2];
                if (listeAuth.supprimer(login, password)) {
                    return "DONE";
                }
            }
        }
        return "ERROR invalid_request";
    }

    public static void main(String[] args) {
        AuthServer server = new AuthServer("authentif");
        server.start();
    }
}
