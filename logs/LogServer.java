package logs;

import java.io.*;
import java.net.*;

public class LogServer {

    private static final int PORT = 3244;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("LogServer en attente de connexions sur le port " + PORT);

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
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String logMessage;
            while ((logMessage = in.readLine()) != null) { // Lire les messages ligne par ligne
                System.out.println("Log reçu : " + logMessage); // Afficher le log reçu
                saveLogToFile(logMessage); // Sauvegarder le log dans un fichier
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

    private void saveLogToFile(String logMessage) {
        try (FileWriter fileWriter = new FileWriter("logs.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(logMessage);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du log : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LogServer logServer = new LogServer();
        logServer.start();
    }
}
