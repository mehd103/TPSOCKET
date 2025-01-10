package as;

import java.io.*;
import java.net.*;
import logs.JsonLogger;

//Version 3.0 journalisation
public class AuthServer {

    private static final int PORT = 28414;
    private ListeAuth listeAuth;

    public AuthServer(String nomBD) {
        // Instanciation de la liste d'authentification
        this.listeAuth = new ListeAuth(nomBD);
    }

    public void start() {
        // Création du socket TCP pour les connexions des clients
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("AuthServer en attente de connexions sur le port " + PORT);

            // Lancement du serveur TCP dans un thread
            new Thread(this::startUDPServer).start();

            // Gestion des connexions TCP
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientTCP(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fonction pour démarrer le serveur UDP sur le même port
    private void startUDPServer() {
        try (DatagramSocket udpSocket = new DatagramSocket(PORT)) {
            System.out.println("AuthServer UDP en attente de requêtes sur le port " + PORT);

            // Écoute en continu pour les requêtes UDP
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(requestPacket); // Attente d'une requête UDP

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                String clientHost = requestPacket.getAddress().getHostAddress();
                int clientPort = requestPacket.getPort();
                String protocol = "UDP";

                System.out.println("[UDP] Requête reçue : " + request);

                // Traitement de la requête UDP (CHK)
                String response = handleRequest(request, clientHost, clientPort, protocol);

                // Envoi de la réponse via UDP
                byte[] responseBytes = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, requestPacket.getAddress(), requestPacket.getPort());
                udpSocket.send(responsePacket);
                System.out.println("[UDP] Réponse envoyée : " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Traitement des clients TCP
    private void handleClientTCP(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientHost = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort();
            String protocol = "TCP";

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("[TCP] Requête reçue : " + request);
                String response = handleRequest(request, clientHost, clientPort, protocol);
                out.println(response);
                System.out.println("[TCP] Réponse envoyée : " + response);
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

    // Traitement des requêtes (CHK, ADD, MOD, DEL)
    private String handleRequest(String request, String clientHost, int clientPort, String protocol) {
        String operationType = "";
        String login = "";
        String result = "ERROR";
        String response = "ERROR invalid_request";

        try {
            // Traitement de la requête de type CHK (vérification du login et du mot de passe)
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
            }
            // Traitement des requêtes ADD, MOD, DEL
            else if (request.startsWith("ADD ")) {
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

            // Journalisation avec JsonLogger
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
