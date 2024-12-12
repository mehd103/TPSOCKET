import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatServer {

    public static void main(String[] args) {
        int port = 12345;

        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Serveur en attente de connexion sur le port " + port);

            Socket clientSocket = server.accept();
            System.out.println("Client connecté : " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Client : " + message);
                if ("quit".equalsIgnoreCase(message)) {
                    System.out.println("Client a quitté la session.");
                    break;
                }
                out.println("Serveur : " + message.toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        

    }


}