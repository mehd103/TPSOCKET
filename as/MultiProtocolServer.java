package as;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// AS version 1.0

public class MultiProtocolServer {
    //adaptation de 2 protocols
    //tcp
    private static final int TCP_PORT = 28414;
    //udp
    private static final int UDP_PORT = 28414;
    private ListeAuth listeAuth;

    public MultiProtocolServer() {
        // Initialiser la base d'authentification (par exemple avec une BD H2)
        listeAuth = new ListeAuth("authentif");
    }


    public void start() {
        // Lancer les sockets d'ecoute TCP et UDP
        new Thread(this::startTCPServer).start();
        new Thread(this::startUDPServer).start();
    }

    private void startTCPServer() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Serveur TCP en écoute sur le port " + TCP_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleTCPClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //protocole tcp
    private void handleTCPClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String request;
            while ((request = in.readLine()) != null) { // Read until the client closes the connection
                System.out.println("[TCP] Requête reçue : " + request);
    
                // Traiter la requête
                String response = handleRequest(request);
    
                // Envoyer la réponse
                out.write((response + "\n").getBytes());
                out.flush(); // IMPORTANT : vider le tampon pour s'assurer que le client reçoit
                System.out.println("[TCP] Réponse envoyée : " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //protocol udp socket en datagramme
    private void startUDPServer() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("Serveur UDP en écoute sur le port " + UDP_PORT);
            byte[] buffer = new byte[1024];

            while (true) {
                // Recevoir une requête
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("[UDP] Requête reçue : " + request);

                // Traiter la requête
                String response = handleRequest(request);

                // Envoyer la réponse
                byte[] responseBytes = response.getBytes();
                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                socket.send(responsePacket);
                System.out.println("[UDP] Réponse envoyée : " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Requetes chk checker
    private String handleRequest(String request) {
        try {
            
            String[] parts = request.split(" ");
            if (parts.length != 3 || !"CHK".equals(parts[0])) {
                return "ERROR invalid_request";
            }

            String login = parts[1];
            String password = parts[2];

            

            
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
        MultiProtocolServer server = new MultiProtocolServer();
        server.start();
    }
}
