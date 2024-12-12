package as;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CheckerClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 28414;

    public void start() {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connexion au serveur AS sur " + SERVER_HOST + ":" + SERVER_PORT);

            while (true) {
                System.out.println("\nEntrez votre login (ou 'exit' pour quitter) : ");
                String login = scanner.nextLine();
                if (login.equalsIgnoreCase("exit")) {
                    System.out.println("Déconnexion...");
                    break;
                }

                System.out.println("Entrez votre mot de passe : ");
                String password = scanner.nextLine();

                // Formatage de la requête
                String request = "CHK " + login + " " + password;
                out.println(request);

                // Lecture de la réponse du serveur
                String response = in.readLine();
                if (response != null) {
                    System.out.println("Réponse du serveur : " + response);
                } else {
                    System.out.println("Aucune réponse reçue du serveur.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CheckerClient client = new CheckerClient();
        client.start();
    }
}
