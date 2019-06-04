package com.opaleye.ganseq.settings;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 * Title : VariantController
 * FXML Controller class for VariantList.fxml
 * @author Young-gon Kim
 * 2018.10.
 */
public class WholeTraceController implements Initializable {
	@FXML private TextArea traceTextArea;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void setText(String text) {
		traceTextArea.setText(text);
	}


}
