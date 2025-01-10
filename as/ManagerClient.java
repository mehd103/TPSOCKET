package as;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ManagerClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 28414;

    public void start() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connexion au serveur AS sur " + SERVER_HOST + ":" + SERVER_PORT);

            while (true) {
                System.out.println("\n+-------------------------------+");
                System.out.println("| 1 - Vérifier une paire (CHK)  |");
                System.out.println("| 2 - Ajouter une paire (ADD)   |");
                System.out.println("| 3 - Modifier une paire (MOD)  |");
                System.out.println("| 4 - Supprimer une paire (DEL) |");
                System.out.println("| 0 - Quitter                   |");
                System.out.println("+-------------------------------+");

                int choix = scanner.nextInt();
                scanner.nextLine(); 

                String login, password, newPassword;
                String request;

                switch (choix) {
                    case 1: // CHK
                        System.out.println("Tapez le login :");
                        login = scanner.nextLine();
                        System.out.println("Tapez le mot de passe :");
                        password = scanner.nextLine();
                        request = "CHK " + login + " " + password;
                        break;

                    case 2: // ADD
                        System.out.println("Tapez le login :");
                        login = scanner.nextLine();
                        System.out.println("Tapez le mot de passe :");
                        password = scanner.nextLine();
                        request = "ADD " + login + " " + password;
                        break;

                        case 3: // MOD
                        System.out.println("Tapez le login :");
                        login = scanner.nextLine();
                        System.out.println("Tapez le nouveau mot de passe :");
                        newPassword = scanner.nextLine();
                        request = "MOD " + login + " " + newPassword;
                        break;

                    case 4: // DEL
                        System.out.println("Tapez le login :");
                        login = scanner.nextLine();
                        System.out.println("Tapez le mot de passe :");
                        password = scanner.nextLine();
                        request = "DEL " + login + " " + password;
                        break;

                    case 0: // Quit
                        System.out.println("Déconnexion...");
                        return;

                    default:
                        System.out.println("Option invalide. Veuillez réessayer.");
                        continue;
                }

                
                out.println(request);

                
                String response = in.readLine();
                if (response != null) {
                    System.out.println("Réponse du serveur : " + response);
                } else {
                    System.out.println("Erreur : pas de réponse du serveur.");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion au serveur : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ManagerClient client = new ManagerClient();
        client.start();
    }
}
