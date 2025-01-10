package as;

import java.io.*;
import java.net.*;


//version 2.0
public class ASServerUnifie {
    private static final int PORT = 28415; // Port d'écoute commun pour TCP et UDP
    private ListeAuth listeAuth;

    public ASServerUnifie() {
        listeAuth = new ListeAuth("authentif");
    }

    // Méthode pour démarrer le serveur et accepter les connexions TCP et UDP
    public void start() {
        try {
            // Initialisation du serveur UDP
            DatagramSocket udpSocket = new DatagramSocket(PORT);
            System.out.println("Serveur AS en écoute sur le port " + PORT);

            // Accepter les connexions TCP dans un thread
            new Thread(this::startTCPServer).start();

            // Attente des requêtes UDP (qui sont uniquement des requêtes CHK)
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(requestPacket); // Attente d'une requête UDP

                // Récupérer la requête en tant que chaîne
                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("[UDP] Requête reçue : " + request);

                // Vérification de la requête CHK
                if (request.startsWith("CHK")) {
                    String response = handleCHKRequest(request); // Traitement de la requête CHK
                    // Envoi de la réponse via UDP
                    byte[] responseBytes = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, requestPacket.getAddress(), requestPacket.getPort());
                    udpSocket.send(responsePacket);
                    System.out.println("[UDP] Réponse envoyée : " + response);
                } else {
                    // Si la requête n'est pas une CHK, on renvoie une erreur
                    String errorResponse = "ERROR invalid_request";
                    byte[] errorBytes = errorResponse.getBytes();
                    DatagramPacket errorPacket = new DatagramPacket(errorBytes, errorBytes.length, requestPacket.getAddress(), requestPacket.getPort());
                    udpSocket.send(errorPacket);
                    System.out.println("[UDP] Requête invalide, réponse envoyée : " + errorResponse);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour accepter les connexions TCP
    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur AS TCP en écoute sur le port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepter une connexion TCP
                new Thread(() -> handleTCPClient(clientSocket)).start(); // Traiter chaque client dans un nouveau thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour traiter les clients TCP
    private void handleTCPClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("[TCP] Requête reçue : " + request);
                String response = handleTCPRequest(request); // Traiter la requête TCP (ADD, MOD, DEL)
                out.println(response); // Envoi de la réponse au client
                System.out.println("[TCP] Réponse envoyée : " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour traiter les requêtes CHK en UDP et TCP
    private String handleCHKRequest(String request) {
        try {
            String[] parts = request.split(" ");
            if (parts.length != 3) {
                return "ERROR invalid_request";
            }

            String login = parts[1];
            String password = parts[2];

            // Vérification du login et du mot de passe dans la liste d'authentification
            if (listeAuth.tester(login, password)) {
                return "GOOD";
            } else {
                return "BAD";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR internal_error";
        }
    }

    // Méthode pour traiter les requêtes TCP (ADD, MOD, DEL)
    private String handleTCPRequest(String request) {
        try {
            String[] parts = request.split(" ");
            if (parts.length != 3) {
                return "ERROR invalid_request";
            }

            String action = parts[0];
            String login = parts[1];
            String password = parts[2];

            switch (action) {
                case "ADD":
                    if (listeAuth.creer(login, password)) {
                        return "DONE";
                    } else {
                        return "ERROR creer";
                    }

                case "MOD":
                    if (listeAuth.mettreAJour(login, password)) {
                        return "DONE";
                    } else {
                        return "ERROR modif";
                    }

                case "DEL":
                    if (listeAuth.supprimer(login, password)) {
                        return "DONE";
                    } else {
                        return "ERROR suppr";
                    }

                default:
                    return "ERROR invalid_action";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR internal_error";
        }
    }

    public static void main(String[] args) {
        ASServerUnifie server = new ASServerUnifie();
        server.start();
    }
}
