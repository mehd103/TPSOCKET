package logs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Classe Singleton qui permet de logger des requêtes vers un serveur de log sur le port 3244 de la machine locale
 * 
 * @author torguet
 *
 */
public class JsonLogger {
	
	// Attributs à compléter
	public static final String LOG_SERVER_HOST = "localhost";
	public static final  int LOG_SERVER_PORT = 3244;

	
	/**
	 * Constructeur à compléter
	 */
	private JsonLogger() {
	}
	
	/**
	 * Transforme une requête en Json
	 * 
	 * @param host machine client
	 * @param port port sur la machine client
	 * @param proto protocole de transport utilisé
	 * @param type type de la requête
	 * @param login login utilisé
	 * @param result résultat de l'opération
	 * @return un objet Json correspondant à la requête
	 */
	private JsonObject reqToJson(String host, int port, String proto, String type, String login, String result) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("host", host)
		   	   .add("port", port)
		   	   .add("proto", proto)
			   .add("type", type)
			   .add("login", login)
			   .add("result", result)
			   .add("date", new Date().toString());

		return builder.build();
	}
	
	/**
	 *  singleton
	 */
	private static JsonLogger logger = null;
	
	/**
	 * récupération du logger qui est créé si nécessaire
	 * 
	 * @return le logger
	 */
	private static JsonLogger getLogger() {
		if (logger == null) {
			logger = new JsonLogger();
		}
		return logger;
	}
	
	/**
	 * méthode pour logger
	 * 
	 * @param host machine client
	 * @param port port sur la machine client
	 * @param proto protocole de transport utilisé
	 * @param type type de la requête
	 * @param login login utilisé
	 * @param result résultat de l'opération
	 */
	public static void log(String host, int port, String proto, String type, String login, String result) {
    JsonLogger logger = getLogger();
    JsonObject log = logger.reqToJson(host, port, proto, type, login, result);

    System.out.println("Tentative de connexion au LogServer " + LOG_SERVER_HOST + ":" + LOG_SERVER_PORT);
    System.out.println("Message de log : " + log.toString());

    try (Socket logSocket = new Socket(LOG_SERVER_HOST, LOG_SERVER_PORT);
         PrintWriter out = new PrintWriter(new OutputStreamWriter(logSocket.getOutputStream()), true)) {

        out.println(log.toString());
        System.out.println("Message envoyé au LogServer : " + log);

    } catch (IOException e) {
        System.err.println("Erreur lors de l'envoi du log au serveur : " + e.getMessage());
    }
}


	public static void main(String [] args){
		JsonLogger jsonLogger = new JsonLogger();
		jsonLogger.reqToJson("localhost", 3244, "TCP", "CHK" , "toto", "DONE");
	}
}
