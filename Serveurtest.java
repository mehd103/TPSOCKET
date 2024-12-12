import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;




class Serveurtest {
    public static void main(String[] args) {
        
            ServerSocket sockEcoute;

            try {
                // Étape 1
                 sockEcoute = new ServerSocket(13214);
           
                // Etape 2
           
                // Declaration du socket de service
                Socket sockService;
           
                // on gère les clients SEQUENTIELLEMENT
                while(true) {
                     try {
                            sockService = sockEcoute.accept();
                            // Exemple pour le mode texte
                            // avec un simple reception/envoi du serveur
           
                            try {
                               // positionner le flux entrant de la Socket
                               BufferedReader insocket = new BufferedReader (new InputStreamReader (
                                            sockService.getInputStream()));
           
                               // positionner le flux de sortie de la Socket
                               PrintStream outsocket = new PrintStream(sockService.getOutputStream());
           
                               // Réception message de la socket
                               String messagerecu = insocket.readLine();
           
                               // Envoi message dans la socket
                               String messagenvoi = "toto";
                               outsocket.println(messagenvoi);
                            }
                               catch (IOException io) {
                                // Exception levée en cas d'erreur d'E/S
                               // ex : le client a fermé la connexion 
                            }
           
                            // Clôture de communication avec un client
                            sockService.close();
           
                     }
                     catch(IOException ioe) {
                         System.out.println("Erreur de accept : " + ioe.getMessage());
                         break;
                     }
                }
           }
           catch(IOException ioe) {
                System.out.println("Erreur de création du server socket: " 
                                 + ioe.getMessage());
           return;
           }
        }
}
