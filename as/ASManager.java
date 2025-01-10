package as;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//v 1.C as manager 28415
public class ASManager {
    private static final int PORT = 28415; // Port d'écoute du serveur
    private ListeAuth listeAuth;

    public ASManager() {
        listeAuth = new ListeAuth("authentif");
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur AS en écoute sur le port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepter une connexion client
                new Thread(() -> handleClient(clientSocket)).start(); // Traiter chaque client dans un nouveau thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("[AS] Requête reçue : " + request);

                // Traitement de la requête du client
                String response = handleRequest(request);

                // Envoi de la réponse au client
                out.write((response + "\n").getBytes());
                out.flush();
                System.out.println("[AS] Réponse envoyée : " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleRequest(String request) {
        try {
            String[] parts = request.split(" ");
            if (parts.length != 3) {
                return "ERROR invalid_request";
            }

            String action = parts[0];
            String login = parts[1];
            String password = parts[2];

            switch (action) {
                case "CHK":
                    if (listeAuth.tester(login, password)) {
                        return "GOOD";
                    } else {
                        return "BAD";
                    }

                case "ADD":
                    // Ajouter la paire login/mot de passe
                    if (listeAuth.creer(login, password)) {
                        return "DONE";
                    } else {
                        return "ERROR creer";
                    }

                case "MOD":
                    // Modifier le mot de passe pour un login existant
                    if (listeAuth.mettreAJour(login, password)) {
                        return "DONE";
                    } else {
                        return "ERROR modif";
                    }

                case "DEL":
                    // Supprimer la paire login/mot de passe
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
        ASManager server = new ASManager();
        server.start();
    }
}
