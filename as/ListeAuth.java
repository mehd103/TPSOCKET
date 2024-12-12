package as;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Gère une Liste d'Authentification avec persistance dans une BD H2
 * @author torguet
 *
 */
public class ListeAuth {
	/**
	 *  Connection à la BD via JDBC
	 */
	private Connection conn;
	
	/**
	 * Requête SQL pour tester
	 */
	private static final String requeteCheck = "select login from AUTH where login = ? AND passwd = ?";
	/**
	 * Requête SQL pour création
	 */
	private static final String requeteInsert = "insert into AUTH values (?, ?)";
	/**
	 * Requête SQL pour modification
	 */
	private static final String requeteUpdate = "update AUTH set passwd = ? where login = ?";
	/**
	 * Requête SQL pour suppression
	 */
	private static final String requeteDelete = "delete from AUTH where login = ? AND passwd = ?";


	//lol
	private static final String requeteSelectAll = "SELECT * FROM AUTH";
	PreparedStatement requeteSelectAllSt;



	
	/**
	 * Prepared Statement pour tester
	 */
	private PreparedStatement requeteCheckSt = null;


	/**
	 * Prepared Statement pour création
	 */
	private PreparedStatement requeteInsertSt = null;
	/**
	 * Prepared Statement pour modification
	 */
	private PreparedStatement requeteUpdateSt = null;
	/**
	 * Prepared Statement pour suppression
	 */
	private PreparedStatement requeteDeleteSt = null;
	
	/**
	 * constructeur
	 * @param nomBD le nom de la bd
	 */
	public ListeAuth(String nomBD) {
		try {
			// récupération du driver
		    Class.forName("org.h2.Driver");
		    // création d'une connexion
		    conn = DriverManager.getConnection("jdbc:h2:"+nomBD+";IGNORECASE=TRUE", "sa", "");
	        // On regarde si la table existe deja
	        try {
	        	// construction du prepared statement
	        	requeteCheckSt = conn.prepareStatement(requeteCheck);
	        } catch(Exception e) {
	        	// sinon on la cree
	        	Statement s = conn.createStatement();
	        	s.execute("create table AUTH  ( " +
	        			" login VARCHAR( 256 ) NOT NULL PRIMARY KEY, " +
	        			" passwd VARCHAR( 256 ) )");
	        	// on ajoute des entrees de test
	        	s.executeUpdate("insert into AUTH values ('Toto', 'Toto')");
	        	s.executeUpdate("insert into AUTH values ('Titi', 'Titi')");
	        	s.executeUpdate("insert into AUTH values ('Tata', 'Tata')");
	        	s.executeUpdate("insert into AUTH values ('Tutu', 'Tutu')");
	        	
	        	// on retente la construction qui devrait desormais marcher
	        	requeteCheckSt = conn.prepareStatement(requeteCheck);
	        }
	        // construction des autres prepared statement
		    requeteInsertSt = conn.prepareStatement(requeteInsert);
		    requeteUpdateSt = conn.prepareStatement(requeteUpdate);
		    requeteDeleteSt = conn.prepareStatement(requeteDelete);
		} catch(Exception e) {
			// il y a eu une erreur
			e.printStackTrace();
		}
	}

	public synchronized  void display (){
		try {
			requeteSelectAllSt = conn.prepareStatement(requeteSelectAll);
			ResultSet rs = requeteSelectAllSt.executeQuery();
		
			while (rs.next()) {
				String login = rs.getString("login");
				String passwd = rs.getString("passwd");
				System.out.println("Login: " + login + ", Password: " + passwd);
			}
		
			rs.close();
			requeteSelectAllSt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * création d'un couple (login, mot de passe)
	 * @param login : le login
	 * @param passwd : le mot de passe
	 * @return true si ça c'est bien passé
	 */
	public synchronized boolean creer(String login, String passwd) {
		try {

			requeteInsertSt.setString(1, login);
	        requeteInsertSt.setString(2, passwd);
	        if (requeteInsertSt.executeUpdate()==1)
				return true;
        	else
        		return false;
		} catch (SQLException e) {
			//e.printStackTrace();
			return false;
		}
	}

	/**
	 *  mise à jour d'un couple (login, mot de passe)
	 * @param login : le login
	 * @param passwd : le mot de passe
	 * @return true si ça c'est bien passé
	 */
	public synchronized boolean mettreAJour(String login, String passwd) {	
		try {
			requeteUpdateSt.setString(1, passwd);
			requeteUpdateSt.setString(2, login);
        	if (requeteUpdateSt.executeUpdate()==1)
				return true;
        	else
        		return false;
		} catch (SQLException e) {
			//e.printStackTrace();
			return false;
		}	
	}
	
	/**
	 *  suppression d'un couple (login, mot de passe)
	 * @param login : le login
	 * @param passwd : le mot de passe
	 * @return true si ça c'est bien passé
	 */

	public synchronized boolean supprimer(String login, String passwd) {
		try {
			requeteDeleteSt.setString(1, login);
			requeteDeleteSt.setString(2, passwd);
        	if (requeteDeleteSt.executeUpdate()==1)
				return true;
        	else
        		return false;   
		} catch (SQLException e) {
			//e.printStackTrace();
			return false;
		}	
	}

	/**
	 *  test d'un couple (login, mot de passe)
	 * @param login : le login
	 * @param passwd : le mot de passe
	 * @return true si ça c'est bien passé
	 */
	public synchronized boolean tester(String login, String passwd) {
		try {
			requeteCheckSt.setString(1, login);
			requeteCheckSt.setString(2, passwd);
			ResultSet rs = requeteCheckSt.executeQuery();
        	if (rs.next())
				return true;
        	else
        		return false;   
		} catch (SQLException e) {
			//e.printStackTrace();
			return false;
		}	
	}

	/**
	 * fermeture de la connexion JDBC
	 * @throws Exception s'il y a eu un problème
	 */
	public void fermer() throws Exception {		
		try {
			conn.close();
		} catch(Exception ex) {
			// il y a eu une erreur
			ex.printStackTrace();
		}
	}
	
	/**
	 * Programme de test
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ListeAuth la = new ListeAuth("authentif");
		Scanner sc = new Scanner(System.in);
		
		while (true) {
			int choix;
			String login;
			String passwd;
		
			System.out.println("+---------------------------------+");
			System.out.println("| 1 - creer une paire             |");
			System.out.println("| 2 - tester une paire            |");
			System.out.println("| 3 - mettre à jour une paire     |");
			System.out.println("| 4 - supprimer une paire         |");
			System.out.println("| 0 - arreter  5 : Display                    |");
			System.out.println("+---------------------------------+");
		
			choix = sc.nextInt();
			sc.nextLine(); // saute le retour à la ligne
			
			switch (choix) {
			case 0:
				la.fermer();
				sc.close();
				System.exit(0);
			case 1:
				System.out.println("Tapez le login");
				login = sc.next();
				sc.nextLine(); // saute le retour à la ligne
				System.out.println("Tapez le mot de passe");
				passwd = sc.next();
				sc.nextLine(); // saute le retour à la ligne
				if (!la.creer(login, passwd))
					System.out.println("La paire existe deja!");
				else
					System.out.println("Creation effectuee.");
				break;
			case 2:
				System.out.println("Tapez le login");
				login = sc.next();
				sc.nextLine(); // saute le retour à la ligne
				System.out.println("Tapez le mot de passe");
				passwd = sc.next();
				if (la.tester(login, passwd))
					System.out.println("Validé");
				else
					System.out.println("Erreur d'authentification");
				break;
			case 3:
				System.out.println("Tapez le login");
				login = sc.next();
				sc.nextLine(); // saute le retour à la ligne
				System.out.println("Tapez le mot de passe");
				passwd = sc.next();
				if(!la.mettreAJour(login, passwd))
					System.out.println("La paire n'existe pas!");
				else
					System.out.println("MAJ effectue.");
				break;
			case 4:
				System.out.println("Tapez le login");
				login = sc.next();
				sc.nextLine(); // saute le retour à la ligne
				System.out.println("Tapez le mot de passe");
				passwd = sc.next();
				if (!la.supprimer(login, passwd))
					System.out.println("La paire n'existe pas!");
				else
					System.out.println("Retrait effectue.");
				break;
			case 5: 
				la.display();
			}
		}
	}
}
