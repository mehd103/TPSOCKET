package logs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class LogServer {

    private static final int PORT = 3244;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("LogServer en attente de connexions sur le port " + PORT);

            while (true) {
                // Accepter les connexions des clients
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            System.err.println("Erreur du serveur de log : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String logMessage;
            while ((logMessage = in.readLine()) != null) {
                System.out.println("Message de journalisation reçu : " + logMessage);
                // Répondre au client pour confirmer la réception
                out.println("Log reçu avec succès.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
