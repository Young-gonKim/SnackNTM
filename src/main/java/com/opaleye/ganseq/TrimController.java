package com.opaleye.ganseq;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Title : TrimController
 * FXML Controller class for Trim.fxml
 * @author Young-gon Kim
 * 2018.7.
 */
public class TrimController implements Initializable {
	@FXML private ScrollPane tracePane;
	@FXML private Button confirmBtn;

	private GanseqTrace targetTrace;

	private int direction = 1;
	private int startTrimPosition = 0;
	private int endTrimPosition = 0;

	private RootController rootController = null;
	private Stage primaryStage;
	private ImageView imageView = null;

	/**
	 *Handler for confirm button
	 */
	public void handleConfirm() {
		try {
			targetTrace.makeTrimmedTrace(startTrimPosition, endTrimPosition);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			popUp("An error occured in trimming\n"+ex.getMessage());
			return;
		}
		if(direction == GanseqTrace.FORWARD) {
			rootController.confirmFwdTrace(targetTrace);
		}
		else {
			targetTrace.makeComplement();
			rootController.confirmRevTrace(targetTrace);
		}
		primaryStage.close();
	}

	/**
	 * Handler for Reset button
	 */
	public void handleReset() {
		startTrimPosition = targetTrace.getFrontTrimPosition();
		endTrimPosition = targetTrace.getTailTrimPosition();
		
		Image image = targetTrace.getTrimmingImage(startTrimPosition, endTrimPosition);
		imageView.setImage(image);
		tracePane.setContent(imageView);
	}


	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * Initialize
	 */
	public void init() {
		startTrimPosition = targetTrace.getFrontTrimPosition();
		endTrimPosition = targetTrace.getTailTrimPosition();

		Image ret =targetTrace.getTrimmingImage(startTrimPosition, endTrimPosition);
		imageView = new ImageView(ret);
		tracePane.setContent(imageView);

		imageView.setOnMouseClicked(t-> {
			int tempStart = startTrimPosition, tempEnd = endTrimPosition;
			if(t.getButton() == MouseButton.PRIMARY) {
				tempStart = (int)t.getX();
			}
			else if (t.getButton()==MouseButton.SECONDARY) {
				tempEnd = (int)t.getX();
			}
			if(tempStart >= tempEnd) {
				popUp("Overlapping trimming area");
				return;
			}
			startTrimPosition = tempStart;
			endTrimPosition = tempEnd;
			imageView.setImage(targetTrace.getTrimmingImage(startTrimPosition, endTrimPosition));
			tracePane.setContent(imageView);
		}
				);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}



	/**
	 * Loads ABI trace
	 * @param trace
	 * @param direction
	 */

	//to replace
	public void setTargetTrace(GanseqTrace trace, int direction) {
		this.targetTrace = trace;
		this.direction = direction;
	}


	/** 
	 * Shows the message in a popup
	 * @param message : message to be shown
	 */
	private void popUp (String message) {
		Stage dialog = new Stage(StageStyle.DECORATED);
		dialog.initOwner(primaryStage);
		dialog.setTitle("Notice");
		Parent parent;
		try {
			parent = FXMLLoader.load(getClass().getResource("popup.fxml"));
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
