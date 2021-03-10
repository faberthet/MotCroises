package vueControl;

import java.sql.SQLException;

//import javafx.animation.ScaleTransition;
//import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
//import javafx.util.Duration;
import modele.ChargerGrille;
import modele.MotsCroisesTP6;

public class ControleurTP6 {

	private MotsCroisesTP6 mocr;
	@FXML
	private GridPane monGridPane;
	@FXML
	private Button nouvelleGrille;
	
	private StringProperty caseCourante; //id de la case courante - attribué à chaque case quand on charge une nouvelle grille
    //sous la forme "ligne,colonne" 

	private boolean directionCourante; //true pour "vers la droite, false pour "vers le bas"
	
	private boolean correspond; //true si l'utilisateur veut afficher les propositions correctes

	@FXML 
	private void initialize() throws SQLException {	
		int rand = 1 + (int)(Math.random() * ((10 - 1) + 1)); // chiffre aléatoire enre 1 et 10
		mocr=newGrid(rand); // génération d'une grille aléatoire
		initializeAux();
	}
	
	private void initializeAux() { //fonction auxiliaire utile à chaque chargement d'une nouvelle grille
		caseCourante= new SimpleStringProperty(monGridPane.getChildren().get(0).getId());
		correspond=false;
		for (Node n : monGridPane.getChildren()){
			n.setOnMouseClicked(e -> this.clicCase(e)); //clique souris (gauche et milieu)
			n.setOnKeyPressed(e -> this.pressClavier(e)); //touche directionelle
		}
		nouvelleGrille.setOnMouseClicked(e -> { 
			try {
				this.clicBouton(e); //clique sur le bouton "nouvelle grille"
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}); 
		for (Node n : monGridPane.getChildren()){
			if (n instanceof TextField){
				TextField tf = (TextField) n ;
				int lig = ((int) n.getProperties().get("gridpane-row")) + 1 ;
				int col = ((int) n.getProperties().get("gridpane-column")) + 1 ;
				tf.textProperty().bindBidirectional(mocr.propositionProperty(lig, col));
				
				//changement de case courante après qu'une lettre soit tapée au clavier
				tf.textProperty().addListener((obsValue, oldValue, newValue)-> updateCaseCourante(lig,col,oldValue,newValue.toUpperCase()));//tf.textProperty() mocr.propositionProperty(lig, col)
				if(!mocr.estCaseNoire(lig, col)) {
					String texte=mocr.getDefinition(lig, col, true)+" / "+mocr.getDefinition(lig, col, false);
					tf.setTooltip(new Tooltip(texte));
				}else {
					tf.setTooltip(new Tooltip(null));
				}
			}
		}
		caseCourante= new SimpleStringProperty(monGridPane.getChildren().get(0).getId());//caseCourante= premiere case
		caseCourante.addListener((obsValue, oldValue, newValue) -> updateDirectionCourante());
		directionCourante=true; //par defaut direction courante vers la droite
	}
	
	@FXML
	public void clicCase(MouseEvent e) {	
		Node n = (Node) e.getSource() ;
		int lig = ((Integer) n.getProperties().get("gridpane-row"))+1;// n° ligne de la case (cf. boucle du 1.2)
		int col = ((Integer) n.getProperties().get("gridpane-column"))+1;// n° colonne de la case (cf. boucle du 1.2)
		if (e.getButton() == MouseButton.MIDDLE){// C'est un clic "central"		
			mocr.reveler(lig, col);// révélation de la solution sur (lig,col)
		}
		caseCourante.set(n.getId());
	}
	
	/*
	 * @param numGrille le numero de la grille dans la base de donnée
	 * @return une nouvelle grille de mot croisé de la base de donnnée
	 */
	private MotsCroisesTP6 newGrid(int numGrille) throws SQLException {
		TextField tf= (TextField) monGridPane.getChildren().get(0);
		monGridPane.getChildren().clear();
		ChargerGrille cg= new ChargerGrille();
		MotsCroisesTP6 mc= cg.extraireGrille(numGrille);
		for (int lig=0; lig<mc.getHauteur();lig++) {
			for (int col=0; col<mc.getLargeur();col++) {
				TextField ntf=new TextField();
				ntf.setPrefHeight(tf.getPrefHeight());
				ntf.setPrefWidth(tf.getPrefWidth());
				ntf.setId((lig+1)+","+(col+1));
				ntf.setAlignment(Pos.CENTER);
				for (Object cle : tf.getProperties().keySet()){
				ntf.getProperties().put(cle, tf.getProperties().get(cle)) ;
				} 
				if(!mc.estCaseNoire(lig+1, col+1)) {
				monGridPane.add(ntf, col, lig);
				}
			}
		}
		return mc;
	}
	
	private void clicBouton(MouseEvent e) throws SQLException{ 
		int rand = 1 + (int)(Math.random() * ((10 - 1) + 1)); // chiffre aléatoire enre 1 et 10
		mocr=newGrid(rand); // génération d'une grille aléatoire
		initializeAux(); //initialisation de la nouvelle grille
	}
	
	//fonction qui s'occupe des divers evenement lié a la modification de la case courante
	private void updateCaseCourante(int lig, int col,String oldVal, String newVal) {
		updateBackground(lig,col); 
		Node n= monGridPane.lookup("#"+lig+","+col);
		TextField tf= (TextField) n;
		limiteLettre(tf, newVal, oldVal);
		
		//deplacement du focus quand on tape ou efface une lettre
		boolean gauche=mocr.directionPossible(lig, col,"gauche");
		boolean haut=mocr.directionPossible(lig, col,"haut");
		boolean bas=mocr.directionPossible(lig, col,"bas");
		boolean droite=mocr.directionPossible(lig, col,"droite");
		if(!tf.textProperty().get().equals("")) { //si on efface le contenu d'une case on ne doit pas avancer d'une case
			if (directionCourante==true&&droite) { 
				n=monGridPane.lookup("#"+lig+","+(col+1));
			}else if(directionCourante==false&&bas) {
				n=monGridPane.lookup("#"+(lig+1)+","+col);
			}else if(directionCourante==true&&bas) {
				n=monGridPane.lookup("#"+(lig+1)+","+col);
			}else if(directionCourante==false&&droite) {
				n=monGridPane.lookup("#"+lig+","+(col+1));
				//}else if(!droite&&!bas) { //ajout possible: si apres avoir taper une lettre on ne peut aller
				//n=monGridPane.getChildren().get(0);//ni en bas ni a droite on revient a la premiere case de la grille
			}
		}else { //retour en arriere quand on efface une case
			if (directionCourante==true&&gauche||(!haut&&gauche)) {
				n=monGridPane.lookup("#"+lig+","+(col-1));
			}else if((directionCourante==false&&haut)||(!gauche&&haut)) {
				n=monGridPane.lookup("#"+(lig-1)+","+col);
			}
		}				
				n.setFocusTraversable(true);
				n.requestFocus();
	}
	
	//evolution du fond vert dans la case courante 
	//(dans le cas ou l'utilisateur a appuye sur entree pour afficher les propositions correctes)
	private void updateBackground(int lig, int col) {
		TextField tf=(TextField) monGridPane.lookup("#"+lig+","+col);
		if (correspond==true) {
			if(!tf.textProperty().get().equals(mocr.getSolution(lig, col)+"")) {
				tf.setStyle(null);
			}else {tf.setStyle("-fx-background-color: green ;");}
		}
	}
	
	//limitation a une seule lettre par case
	//par defaut le textfield contient un espace. 
	//cette fonction gere les cas ou on ecris avant, apres et a la place de l'espace ou de la lettre
	private void limiteLettre(TextField tf, String newVal, String oldVal) {
		if(newVal.length()==2&&(newVal.charAt(0)+"").equals("")){
			tf.textProperty().set((newVal.charAt(1)+""));
		}else if(newVal.length()==2&&(newVal.charAt(1)+"").equals(" ")){
			tf.textProperty().set((newVal.charAt(0)+""));
		}else if(newVal.length()==2&&!((newVal.charAt(0)+"").equals(""))&&!((newVal.charAt(1)+"").equals(" "))) {
			String s=newVal.replaceFirst(oldVal, "");
			tf.textProperty().set(s);
		}else if(newVal.length()!=0){
			tf.textProperty().set(((newVal.charAt(newVal.length()-1))+""));
		}
	}

	//changement automatique de direction courante quand on arrive en bout de ligne ou de colonne
	private void updateDirectionCourante() {
		String [] tab=caseCourante.get().split(",");
		int lig=Integer.parseInt(tab[0]);
		int col=Integer.parseInt(tab[1]);
		boolean droite=mocr.directionPossible(lig, col,"droite");
		boolean bas=mocr.directionPossible(lig, col,"bas");
		if(directionCourante==true) { //si direction courante vers la droite
			if (!droite&&bas) { //et qu'on ne peux plus aller à droite mais on peut aller vers le bas
				directionCourante=false; //on change de direction courante vers le bas
			}
		}else {
			if (droite&&!bas) { //et reciproquement...
				directionCourante=true;
			}
		}
	}
	
	private void pressClavier(KeyEvent e) {
		Node n = (Node) e.getSource();
		int lig = ((int) n.getProperties().get("gridpane-row")) + 1 ;
		int col = ((int) n.getProperties().get("gridpane-column")) + 1 ;

		boolean haut=mocr.directionPossible(lig, col,"haut");
		boolean droite=mocr.directionPossible(lig, col,"droite");
		boolean bas=mocr.directionPossible(lig, col,"bas");
		boolean gauche=mocr.directionPossible(lig, col,"gauche");

		if(e.getCode() == KeyCode.RIGHT && droite) { //si touche flèche droite et direction vers la droite possible
			directionCourante=true;
			n=monGridPane.lookup("#"+lig+","+(col+1));
			n.setFocusTraversable(true);
			n.requestFocus();
		}else if(e.getCode() == KeyCode.DOWN && bas) {// touche flèche bas
			directionCourante=false;
			n=monGridPane.lookup("#"+(lig+1)+","+col);
			n.setFocusTraversable(true);
			n.requestFocus();
		}else if(e.getCode() == KeyCode.UP && haut) {// touche flèche haut
			n=monGridPane.lookup("#"+(lig-1)+","+col);
			n.setFocusTraversable(true);
			n.requestFocus();
		}else if(e.getCode() == KeyCode.LEFT && gauche) {// touche flèche gauche
			n=monGridPane.lookup("#"+lig+","+(col-1));
			n.setFocusTraversable(true);
			n.requestFocus();
		}if(e.getCode() == KeyCode.ENTER) {	
			afficherPropositionsCorrectes();
		}
		caseCourante.set(n.getId()); //mise à jour de la case courante
	}
	
	//applique un fond vert dans les cases de toutes les lettre correctes de la grille
	private void afficherPropositionsCorrectes() {
		if(correspond==false) {
			for (Node no : monGridPane.getChildren()){
				TextField tf = (TextField) no ;
				int ligne = ((int) tf.getProperties().get("gridpane-row")) + 1 ;
				int colonne = ((int) tf.getProperties().get("gridpane-column")) + 1 ;
				char sol= mocr.getSolution(ligne, colonne);
				if(tf.textProperty().get().equals(sol+"")) {
					tf.setStyle("-fx-background-color: green ;");
				}
			}
			correspond=true;
		}else {
			for (Node no : monGridPane.getChildren()){
				TextField tf = (TextField) no ;
				int ligne = ((int) tf.getProperties().get("gridpane-row")) + 1 ;
				int colonne = ((int) tf.getProperties().get("gridpane-column")) + 1 ;
				char sol= mocr.getSolution(ligne, colonne);
				if(tf.textProperty().get().equals(sol+"")) {
					tf.setStyle(null);
				}
			}
			correspond=false;		
		}	
	}

	public static void main(String[] args) throws SQLException
	{
		ChargerGrille cg=new ChargerGrille();
		System.out.println(cg.extraireGrille(1).toString());
		MotsCroisesTP6 mc=cg.extraireGrille(1);
		System.out.println(mc.toString());
	}

}
