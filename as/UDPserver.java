package as;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPServer {
    private static final int SERVER_PORT = 28414;
    private ListeAuth listeAuth;

    public UDPServer() {
        // Initialiser la base d'authentification (par exemple avec une BD H2)
        listeAuth = new ListeAuth("authentif");
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("Serveur UDP en écoute sur le port " + SERVER_PORT);

            byte[] buffer = new byte[1024];
            while (true) {
                // Recevoir une requête
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Requête reçue : " + request);

                // Traiter la requête
                String response = handleRequest(request);

                // Envoyer la réponse
                byte[] responseBytes = response.getBytes();
                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                socket.send(responsePacket);
                System.out.println("Réponse envoyée : " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String handleRequest(String request) {
        try {
            // Parser la requête
            String[] parts = request.split(" ");
            if (parts.length != 3 || !"CHK".equals(parts[0])) {
                return "ERROR invalid_request";
            }

            String login = parts[1];
            String password = parts[2];

            // Vérifier les informations
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

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.start();
    }
}
