package modele;

import java.sql.* ;
import java.util.* ;
	
public class ChargerGrille{
	
	 private Connection connexion ;
	 
	 public ChargerGrille(){
	 try { connexion = connecterBD() ; }
	 catch (SQLException e) { e.printStackTrace(); }
	 }
	 
	 public static Connection connecterBD() throws SQLException{
		 
		 String hostName = "localhost";
		 String dbName = "prga";
	     String userName = "root";
	     String password = "";
		 
		 Connection connect ;
		 String  connectionURL  = "jdbc:mysql://" + hostName + "/" + dbName;
		 connect = DriverManager.getConnection( connectionURL ,userName,password);
		 
		 return connect ;
	 }
	 // Retourne la liste des grilles disponibles dans la B.D.
	 // Chaque grille est décrite par la concaténation des valeurs
	 // respectives des colonnes nom_grille, hauteur et largeur.
	 // L’élément de liste ainsi obtenu est indexé par le numéro de
	 // la grille (colonne num_grille).
	 // Ainsi "Français débutants (7x6)" devrait être associé à la clé 10
	 public Map<Integer, String> grillesDisponibles(){ 
		 Map<Integer, String> map= new HashMap<Integer, String>();
		 try {
			Statement statement = connexion.createStatement();
			String sql = "Select num_grille,nom_grille,largeur,hauteur from TP5_GRILLE";
			ResultSet rs = statement.executeQuery(sql);
		       while (rs.next()) {
		           int num_grille= rs.getInt(1);
		           String nom_grille = rs.getString(2);
		           int largeur = rs.getInt(3);
		           int hauteur = rs.getInt(4);
		           map.put(num_grille, nom_grille+" ("+hauteur+","+largeur+")");
		       }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return map;
		  }


	 public MotsCroisesTP6 extraireGrille(int numGrille) throws SQLException{

			 Statement statement;
			 statement = connexion.createStatement();
			 String sql = "Select largeur,hauteur from TP5_GRILLE where num_grille="+numGrille;
			 ResultSet rs = statement.executeQuery(sql);
			 MotsCroisesTP6 mocr=null;
			 while (rs.next()) {
			 mocr= new MotsCroisesTP6(rs.getInt("hauteur"),rs.getInt("largeur"));
			 }
			 String sql2="Select * from tp5_mot where num_grille="+numGrille;
			 rs = statement.executeQuery(sql2);
			 while (rs.next()) {
				 String definition=rs.getString("definition");
				 if (rs.getInt("horizontal")==1) {
					 mocr.setDefinition(rs.getInt("ligne"), rs.getInt("colonne"), true, definition);
				 }else {
					 mocr.setDefinition(rs.getInt("ligne"), rs.getInt("colonne"), false, definition);
				 }
				 String solution=rs.getString("solution");
				 int longueurMot=solution.length();
				 for (int i=0; i<longueurMot; i++) {
					 if (rs.getInt("horizontal")==1) {
						 mocr.setSolution(rs.getInt("ligne"), rs.getInt("colonne")+i, solution.charAt(i));
					 }else {
						 mocr.setSolution(rs.getInt("ligne")+i, rs.getInt("colonne"), solution.charAt(i));
					 }
				 }
			 }
		
		 return mocr; 
	 }
	

public static void main(String[]args) throws SQLException {
	ChargerGrille cg=new ChargerGrille();
	System.out.println(cg.extraireGrille(1).toString());
	MotsCroisesTP6 mc=cg.extraireGrille(1);
	System.out.println(mc.toString());
   }

}
