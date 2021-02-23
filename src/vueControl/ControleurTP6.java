package vueControl;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import modele.ChargerGrille;
import modele.MotsCroisesTP6;

public class ControleurTP6 {
	
	private MotsCroisesTP6 mocr;
	
	@FXML
	private GridPane monGridPane;
	
	@FXML // pour rendre la méthode visible depuis SceneBuilder
	private void initialize() throws SQLException {
		int rand = 1 + (int)(Math.random() * ((10 - 1) + 1));
		mocr=newGrid(rand);
		for (Node n : monGridPane.getChildren())
		{ n.setOnMouseClicked(e -> this.clicCase(e));; }

		for (Node n : monGridPane.getChildren()){
			if (n instanceof TextField){
				TextField tf = (TextField) n ;
				int lig = ((int) n.getProperties().get("gridpane-row")) + 1 ;
				int col = ((int) n.getProperties().get("gridpane-column")) + 1 ;

				tf.textProperty().bindBidirectional(mocr.propositionProperty(lig, col)); //la valeur modifié ne s'affiche pas dans le textefield pourquoi?
				if(!mocr.estCaseNoire(lig, col)) {
					String texte=mocr.getDefinition(lig, col, true)+" / "+mocr.getDefinition(lig, col, false);
					tf.setTooltip(new Tooltip(texte));
				}else {
					tf.setTooltip(new Tooltip(null));
				}

			}
		}
	}

	public static MotsCroisesTP6 creerMotsCroises2x3(){
		MotsCroisesTP6 mc = new MotsCroisesTP6(2,3) ;
		for (int i=1; i<=mc.getHauteur(); i++) {
			for (int j=1; j<=mc.getLargeur(); j++){
				mc.setCaseNoire(i, j, false);
			}
			mc.setCaseNoire(2, 2, true);
			mc.setDefinition(1, 1, true, "Note");
			mc.setSolution(1, 1, 'S');
			mc.setSolution(1, 2, 'O');
			mc.setSolution(1, 3, 'L');
			mc.setDefinition(1, 1, false, "Autre note");
			mc.setSolution(2, 1, 'I');
			mc.setDefinition(1, 3, false, "Et encore une note");
			mc.setSolution(2, 3, 'A');
		}
		return mc ;
	}
	
	@FXML
	public void clicCase(MouseEvent e) {
		if (e.getButton() == MouseButton.MIDDLE){// C'est un clic "central"		
			Node n = (Node) e.getSource() ;
			if (n instanceof TextField){
			int lig = ((Integer) n.getProperties().get("gridpane-row"))+1;// n° ligne de la case (cf. boucle du 1.2)
			int col = ((Integer) n.getProperties().get("gridpane-column"))+1;// n° colonne de la case (cf. boucle du 1.2)
			mocr.reveler(lig, col);// demande de révélation de la solution sur (lig,col)
				((TextField) n).textProperty().set(mocr.getProposition(lig, col)+""); 
			System.out.println("test reactivité bouton ligne:"+ lig+ " col: "+ col);
			System.out.println(mocr.getProposition(lig, col));
			System.out.println(mocr.propositionProperty(lig, col));
			}
		}
	}
	
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

	
	public static void main(String[] args) throws SQLException
	{
		ChargerGrille cg=new ChargerGrille();
		System.out.println(cg.extraireGrille(1).toString());
		MotsCroisesTP6 mc=cg.extraireGrille(1);
		System.out.println(mc.toString());
	}

}
