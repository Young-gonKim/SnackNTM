package com.opaleye.ganseq.settings;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.opaleye.ganseq.GanseqTrace;
import com.opaleye.ganseq.RootController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsController implements Initializable  {
	@FXML private TextField tf_gapOpenPenalty;

	@FXML private Label gapOpenDesc;

	private RootController rootController = null;
	private Stage primaryStage;



	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void setRootController(RootController rootController) {
		this.rootController = rootController;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public void initValues(int gapOpenPenalty) {
		gapOpenDesc.setWrapText(true);
		tf_gapOpenPenalty.setText(String.format("%d",  gapOpenPenalty));
	}


	private void setValues() {
		int gapOpenPenalty;

		try {
			gapOpenPenalty = Integer.parseInt(tf_gapOpenPenalty.getText());
			if(gapOpenPenalty <= 0)
				throw new NumberFormatException("Gap open penalty should be positive integer");

			rootController.setProperties(gapOpenPenalty);
			rootController.handleRemoveFwd();
			rootController.handleRemoveRev();
			primaryStage.close();

		}
		catch (NumberFormatException ex) {
			popUp(ex.getMessage());
		}

	}

	public void handleConfirm() {
		if(rootController.getFwdLoaded() || rootController.getRevLoaded()) {
			Alert alert = new Alert(AlertType.CONFIRMATION, "This will reset fwd/rev trace files, continue? " , ButtonType.YES, ButtonType.NO);
			alert.initOwner(primaryStage);
			alert.showAndWait();

			if (alert.getResult() == ButtonType.YES) {
				setValues();
			}
		}
		else {
			setValues();
		}
	}

	public void handleDefault() {
		tf_gapOpenPenalty.setText(String.format("%d",  RootController.defaultGOP));

	}


	public void handleFwdWholeTrace() {
		GanseqTrace trimmedFwdTrace = rootController.getTrimmedFwdTrace();

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("wholeTrace.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			WholeTraceController controller = fxmlLoader.getController();
			stage.initOwner(primaryStage);

			stage.setScene(new Scene(root1));
			stage.show();

			int baseCount = 0;
			String temp = "";
			for(int i=0;i<trimmedFwdTrace.getTraceLength();i++) {
				if(baseCount<trimmedFwdTrace.getSequenceLength())
					if(i==trimmedFwdTrace.getBaseCalls()[baseCount]) {
						temp+=(String.format("Base call here %d : %c\n", (baseCount+1), trimmedFwdTrace.getSequence().charAt(baseCount++)));
					}
				temp+=(String.format("%-6d : %-8d %-8d %-8d %-8d \n", i, trimmedFwdTrace.getTraceA()[i], trimmedFwdTrace.getTraceT()[i], 
						trimmedFwdTrace.getTraceG()[i], trimmedFwdTrace.getTraceC()[i])); 
			}

			controller.setText(temp);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
	}


	public void handleGenerateTrainingData() {
		/*
		GanseqTrace trimmedFwdTrace = rootController.getTrimmedFwdTrace();
		try {
			baseDir.mkdir();
			baseTrainDir.mkdir();
			featuresDirTrain.mkdir();
			labelsDirTrain.mkdir();
			baseTestDir.mkdir();
			featuresDirTest.mkdir();
			labelsDirTest.mkdir();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}


		try {
			trainCsvCounter = Integer.parseInt(tf_trainStartFileNo.getText());
		}
		catch(Exception ex) {
			popUp(ex.getMessage());
			ex.printStackTrace();
			return;
		}


		for(int i=0;i<trimmedFwdTrace.getSequenceLength();i++) {
			if(trimmedFwdTrace.getQCalls()[i]<10) continue;
			int pos = trimmedFwdTrace.getBaseCalls()[i];
			if(pos<10 || pos>=trimmedFwdTrace.getTraceLength()-10) continue;
			char base = trimmedFwdTrace.getOriginalSequence().charAt(i);
			System.out.println(String.format("%dth base call : %c", (i+1), base));

			int[] positiveTrace = null;
			int[] negativeTrace1 = null;
			int[] negativeTrace2 = null;
			int[] negativeTrace3 = null;

			switch(base) {
			case 'A' :  
				positiveTrace = trimmedFwdTrace.getTraceA();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'T' : 
				positiveTrace = trimmedFwdTrace.getTraceT();
				negativeTrace1 = trimmedFwdTrace.getTraceA();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'G' : 
				positiveTrace = trimmedFwdTrace.getTraceG();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceA();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'C' : 
				positiveTrace = trimmedFwdTrace.getTraceC();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceA();
				break;
			default: continue;
			}

			String posString = ""; 
			String negString1 = "";
			String negString2 = "";
			String negString3 = "";
			for(int j=pos-10;j<=pos+10;j++) {
				posString += String.format("%d\n",  positiveTrace[j]);
				negString1 += String.format("%d\n",  negativeTrace1[j]);
				negString2 += String.format("%d\n",  negativeTrace2[j]);
				negString3 += String.format("%d\n",  negativeTrace3[j]);
				//System.out.println(positiveTrace[j]);
			}
			//System.out.println(posString);
			File outPathFeatures;
			File outPathLabels;
			try {
				outPathFeatures = new File(featuresDirTrain, trainCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTrain, trainCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, posString);
				FileUtils.writeStringToFile(outPathLabels, "1");
				trainCsvCounter++;

				outPathFeatures = new File(featuresDirTrain, trainCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTrain, trainCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString1);
				FileUtils.writeStringToFile(outPathLabels, "0");
				trainCsvCounter++;

				outPathFeatures = new File(featuresDirTrain, trainCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTrain, trainCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString2);
				FileUtils.writeStringToFile(outPathLabels, "0");
				trainCsvCounter++;

				outPathFeatures = new File(featuresDirTrain, trainCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTrain, trainCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString3);
				FileUtils.writeStringToFile(outPathLabels, "0");
				trainCsvCounter++;
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		tf_trainStartFileNo.setText(String.format("%d", trainCsvCounter));
		*/
	}

	public void handleRevWholeTrace() {
		GanseqTrace trimmedRevTrace = rootController.getTrimmedRevTrace();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("wholeTrace.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			WholeTraceController controller = fxmlLoader.getController();
			stage.initOwner(primaryStage);

			stage.setScene(new Scene(root1));
			stage.show();

			int baseCount = 0;
			String temp = "";
			for(int i=0;i<trimmedRevTrace.getTraceLength();i++) {
				if(baseCount<trimmedRevTrace.getSequenceLength())
					if(i==trimmedRevTrace.getBaseCalls()[baseCount]) {
						temp+=(String.format("Base call here %d : %c", (baseCount+1), trimmedRevTrace.getSequence().charAt(baseCount++)));
					}
				temp+=(String.format("%-6d : %-8d %-8d %-8d %-8d ", i, trimmedRevTrace.getTraceA()[i], trimmedRevTrace.getTraceT()[i], 
						trimmedRevTrace.getTraceG()[i], trimmedRevTrace.getTraceC()[i])); 
			}

			controller.setText(temp);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
		
	}

	public void handleGenerateTestData() {
		/*
		GanseqTrace trimmedFwdTrace = rootController.getTrimmedFwdTrace();
		try {

			baseDir.mkdir();
			baseTrainDir.mkdir();
			featuresDirTrain.mkdir();
			labelsDirTrain.mkdir();
			baseTestDir.mkdir();
			featuresDirTest.mkdir();
			labelsDirTest.mkdir();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		try {
			testCsvCounter = Integer.parseInt(tf_testStartFileNo.getText());
		}
		catch(Exception ex) {
			popUp(ex.getMessage());
			ex.printStackTrace();
			return;
		}

		for(int i=0;i<trimmedFwdTrace.getSequenceLength();i++) {
			if(trimmedFwdTrace.getQCalls()[i]<10) continue;
			int pos = trimmedFwdTrace.getBaseCalls()[i];
			if(pos<10 || pos>=trimmedFwdTrace.getTraceLength()-10) continue;
			char base = trimmedFwdTrace.getOriginalSequence().charAt(i);
			System.out.println(String.format("%dth base call : %c", (i+1), base));

			int[] positiveTrace = null;
			int[] negativeTrace1 = null;
			int[] negativeTrace2 = null;
			int[] negativeTrace3 = null;

			switch(base) {
			case 'A' :  
				positiveTrace = trimmedFwdTrace.getTraceA();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'T' : 
				positiveTrace = trimmedFwdTrace.getTraceT();
				negativeTrace1 = trimmedFwdTrace.getTraceA();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'G' : 
				positiveTrace = trimmedFwdTrace.getTraceG();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceA();
				negativeTrace3 = trimmedFwdTrace.getTraceC();
				break;
			case 'C' : 
				positiveTrace = trimmedFwdTrace.getTraceC();
				negativeTrace1 = trimmedFwdTrace.getTraceT();
				negativeTrace2 = trimmedFwdTrace.getTraceG();
				negativeTrace3 = trimmedFwdTrace.getTraceA();
				break;
			default: continue;
			}

			String posString = ""; 
			String negString1 = "";
			String negString2 = "";
			String negString3 = "";
			for(int j=pos-10;j<=pos+10;j++) {
				posString += String.format("%d\n",  positiveTrace[j]);
				negString1 += String.format("%d\n",  negativeTrace1[j]);
				negString2 += String.format("%d\n",  negativeTrace2[j]);
				negString3 += String.format("%d\n",  negativeTrace3[j]);
				//System.out.println(positiveTrace[j]);
			}
			//System.out.println(posString);
			File outPathFeatures;
			File outPathLabels;
			try {
				outPathFeatures = new File(featuresDirTest, testCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTest, testCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, posString);
				FileUtils.writeStringToFile(outPathLabels, "1");
				testCsvCounter++;

				outPathFeatures = new File(featuresDirTest, testCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTest, testCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString1);
				FileUtils.writeStringToFile(outPathLabels, "0");
				testCsvCounter++;

				outPathFeatures = new File(featuresDirTest, testCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTest, testCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString2);
				FileUtils.writeStringToFile(outPathLabels, "0");
				testCsvCounter++;

				outPathFeatures = new File(featuresDirTest, testCsvCounter + ".csv");
				outPathLabels = new File(labelsDirTest, testCsvCounter + ".csv");
				FileUtils.writeStringToFile(outPathFeatures, negString3);
				FileUtils.writeStringToFile(outPathLabels, "0");
				testCsvCounter++;
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		tf_testStartFileNo.setText(String.format("%d", testCsvCounter));
		*/
	}
	public void handleFwdTraceValueBtn() {
		GanseqTrace trimmedFwdTrace = rootController.getTrimmedFwdTrace();

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("traceValue.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			TraceValueController controller = fxmlLoader.getController();
			stage.initOwner(primaryStage);
			stage.setScene(new Scene(root1));
			stage.show();


			String temp = "";
			double[] avg, avg100;
			avg = new double[4];
			avg100 = new double[4];
			int maxPeak[] = new int[4];
			int sum[] = new int[4];
			int count[] = new int[4];
			int sumQscore = 0;


			for(int i=0;i<trimmedFwdTrace.getSequenceLength();i++) {
				char base = trimmedFwdTrace.getSequence().charAt(i);
				int position = trimmedFwdTrace.getBaseCalls()[i];
				int tempValue = 0;
				switch(base) {
				case 'A' :  
					tempValue = trimmedFwdTrace.getTraceA()[position];
					sum[0] += tempValue;
					if(tempValue > maxPeak[0]) maxPeak[0] = tempValue;
					count[0]++;
					break;
				case 'T' : 
					tempValue = trimmedFwdTrace.getTraceT()[position];
					sum[1] += tempValue;
					if(tempValue > maxPeak[1]) maxPeak[1] = tempValue;
					count[1]++;
					break;
				case 'G' : 
					tempValue = trimmedFwdTrace.getTraceG()[position];
					sum[2] += tempValue;
					if(tempValue > maxPeak[2]) maxPeak[2] = tempValue;
					count[2]++;
					break;
				case 'C' : 
					tempValue = trimmedFwdTrace.getTraceC()[position];
					sum[3] += tempValue;
					if(tempValue > maxPeak[3]) maxPeak[3] = tempValue;
					count[3]++;
					break;
				}

				if(i==99) {
					for(int j=0;j<4;j++) {
						avg100[j]= (double)sum[j]/count[j];
					}
				}

				temp+= String.format("%-6d : %-8d %-8d %-8d %-8d %-8d\n", (i+1), trimmedFwdTrace.getTraceA()[position], trimmedFwdTrace.getTraceT()[position], 
						trimmedFwdTrace.getTraceG()[position], trimmedFwdTrace.getTraceC()[position], trimmedFwdTrace.getQCalls()[i]);
				sumQscore += trimmedFwdTrace.getQCalls()[i];
			}
			for(int j=0;j<4;j++) {
				avg[j]= (double)sum[j]/count[j];
			}

			String ret = String.format("Average Q-score : %.1f\n", sumQscore / (double)trimmedFwdTrace.getSequenceLength()); 
			ret += "        A       T       G       C     Qscore\n";
			ret += String.format("max    : %-8d %-8d %-8d %-8d\n", maxPeak[0], maxPeak[1], maxPeak[2], maxPeak[3]);
			ret += String.format("avg100 : %-8.1f %-8.1f %-8.1f %-8.1f\n", avg100[0], avg100[1], avg100[2], avg100[3]);
			ret += String.format("avg    : %-8.1f %-8.1f %-8.1f %-8.1f\n", avg[0], avg[1], avg[2], avg[3]);
			ret += temp;
			controller.setText(ret);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
	}

	public void handleRevTraceValueBtn() {
		GanseqTrace trimmedRevTrace = rootController.getTrimmedRevTrace();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("traceValue.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			TraceValueController controller = fxmlLoader.getController();
			stage.initOwner(primaryStage);
			stage.setScene(new Scene(root1));
			stage.show();
			String temp = "";
			double[] avg, avg100;
			avg = new double[4];
			avg100 = new double[4];
			int maxPeak[] = new int[4];
			int sum[] = new int[4];
			int count[] = new int[4];
			int sumQscore = 0;


			for(int i=0;i<trimmedRevTrace.getSequenceLength();i++) {
				char base = trimmedRevTrace.getSequence().charAt(i);
				int position = trimmedRevTrace.getBaseCalls()[i];
				int tempValue = 0;
				switch(base) {
				case 'A' :  
					tempValue = trimmedRevTrace.getTraceA()[position];
					sum[0] += tempValue;
					if(tempValue > maxPeak[0]) maxPeak[0] = tempValue;
					count[0]++;
					break;
				case 'T' : 
					tempValue = trimmedRevTrace.getTraceT()[position];
					sum[1] += tempValue;
					if(tempValue > maxPeak[1]) maxPeak[1] = tempValue;
					count[1]++;
					break;
				case 'G' : 
					tempValue = trimmedRevTrace.getTraceG()[position];
					sum[2] += tempValue;
					if(tempValue > maxPeak[2]) maxPeak[2] = tempValue;
					count[2]++;
					break;
				case 'C' : 
					tempValue = trimmedRevTrace.getTraceC()[position];
					sum[3] += tempValue;
					if(tempValue > maxPeak[3]) maxPeak[3] = tempValue;
					count[3]++;
					break;
				}

				if(i==99) {
					for(int j=0;j<4;j++) {
						avg100[j]= (double)sum[j]/count[j];
					}
				}

				temp+= String.format("%-6d : %-8d %-8d %-8d %-8d %-8d\n", (i+1), trimmedRevTrace.getTraceA()[position], trimmedRevTrace.getTraceT()[position], 
						trimmedRevTrace.getTraceG()[position], trimmedRevTrace.getTraceC()[position], trimmedRevTrace.getQCalls()[i]);
				sumQscore += trimmedRevTrace.getQCalls()[i];
			}
			for(int j=0;j<4;j++) {
				avg[j]= (double)sum[j]/count[j];
			}

			String ret = String.format("Average Q-score : %.1f\n", sumQscore / (double)trimmedRevTrace.getSequenceLength()); 
			ret += "        A       T       G       C     Qscore\n";
			ret += String.format("max    : %-8d %-8d %-8d %-8d\n", maxPeak[0], maxPeak[1], maxPeak[2], maxPeak[3]);
			ret += String.format("avg100 : %-8.1f %-8.1f %-8.1f %-8.1f\n", avg100[0], avg100[1], avg100[2], avg100[3]);
			ret += String.format("avg    : %-8.1f %-8.1f %-8.1f %-8.1f\n", avg[0], avg[1], avg[2], avg[3]);
			ret += temp;
			controller.setText(ret);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
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
