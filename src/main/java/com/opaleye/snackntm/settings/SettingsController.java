/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opaleye.snackntm.settings;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.opaleye.snackntm.GanseqTrace;
import com.opaleye.snackntm.RootController;

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
			//rootController.handleReset();
			primaryStage.close();

		}
		catch (NumberFormatException ex) {
			popUp(ex.getMessage());
		}

	}

	public void handleConfirm() {
		setValues();
	}

	public void handleDefault() {
		tf_gapOpenPenalty.setText(String.format("%d",  RootController.defaultGOP));

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
