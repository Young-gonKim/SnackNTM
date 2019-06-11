package com.opaleye.ganseq;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EditBaseController implements Initializable  {

		@FXML private Label refLabel;
		@FXML private ComboBox fwdComboBox, revComboBox;


		private Stage primaryStage;
		private RootController rootController;

		
		private String[] ATGC = {"-", "A", "T", "G", "C"}; 


		@Override
		public void initialize(URL location, ResourceBundle resources) {
			fwdComboBox.getItems().addAll(ATGC);
			revComboBox.getItems().addAll(ATGC);
			
				
		}
		

		public void setRootController(RootController rootController) {
			this.rootController = rootController;
		}

		public void setPrimaryStage(Stage primaryStage) {
			this.primaryStage = primaryStage;
		}
		
		public void setInitBases(String refString, String fwdString, String revString) {
			refLabel.setText(refString);
			fwdComboBox.setValue(fwdString);
			revComboBox.setValue(revString);
		}

		
		            
		public void handleConfirm() {
			
			//System.out.println(String.format("fwd : " + fwdComboBox.getValue()));
			//System.out.println(String.format("rev : " + revComboBox.getValue()));
			rootController.updateBase(((String)fwdComboBox.getValue()).charAt(0), ((String)revComboBox.getValue()).charAt(0));
			primaryStage.close();
		}

		/**
		 * Loads ABI trace
		 * @param trace
		 * @param direction
		 */

		private void popUp (String message) {
			Stage dialog = new Stage(StageStyle.DECORATED);
			dialog.initOwner(primaryStage);
			dialog.setTitle("Notice");
			Parent parent;
			try {
				parent = FXMLLoader.load(getClass().getResource("../popup.fxml"));
				Label messageLabel = (Label)parent.lookup("#messageLabel");
				messageLabel.setText(message);
				messageLabel.setWrapText(true);
				Button okButton = (Button) parent.lookup("#okButton");
				okButton.setOnAction(event->dialog.close());
				Scene scene = new Scene(parent);

				dialog.setScene(scene);
				dialog.setResizable(false);
				dialog.show();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}

		}

	}
