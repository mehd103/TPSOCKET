package as;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 28414;

    public void start() {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.println("\n+-------------------------------+");
                System.out.println("| 1 - Vérifier une paire (CHK)  |");
                System.out.println("| 0 - Quitter                   |");
                System.out.println("+-------------------------------+");

                int choix = scanner.nextInt();
                scanner.nextLine(); // Ignorer la ligne vide

                if (choix == 0) {
                    System.out.println("Déconnexion...");
                    break;
                }

                if (choix == 1) {
                    System.out.println("Tapez le login :");
                    String login = scanner.nextLine();
                    System.out.println("Tapez le mot de passe :");
                    String password = scanner.nextLine();

                    // Construire la requête
                    String request = "CHK " + login + " " + password;

                    // Envoyer la requête au serveur
                    byte[] requestBytes = request.getBytes();
                    DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddress, SERVER_PORT);
                    socket.send(requestPacket);

                    // Recevoir la réponse du serveur
                    byte[] responseBuffer = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                    socket.receive(responsePacket);

                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    System.out.println("Réponse du serveur : " + response);
                } else {
                    System.out.println("Choix invalide, veuillez réessayer.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        client.start();
    }
}
