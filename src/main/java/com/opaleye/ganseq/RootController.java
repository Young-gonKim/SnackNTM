package com.opaleye.ganseq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import com.opaleye.ganseq.mmalignment.AlignedPair;
import com.opaleye.ganseq.mmalignment.MMAlignment;
import com.opaleye.ganseq.reference.ReferenceSeq;
import com.opaleye.ganseq.settings.SettingsController;
import com.opaleye.ganseq.tools.TooltipDelay;
import com.opaleye.ganseq.variants.Indel;
import com.opaleye.ganseq.variants.Variant;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Title : RootController
 * FXML Controller class for MainStage.fxml
 * Main class of the Ganseq application
 * @author Young-gon Kim
 *2018.5
 */
public class RootController implements Initializable {
	private static String settingsFileName = "settings/settings.properties";
	public static final int defaultGOP = 30;
	public static final String version = "1.0";
	public static int firstNumber = 1; 
	private static int fontSize = 13;

	private static final String s16 = "16s rRNA";
	private static final String rpo = "rpo";
	private static final String tuf = "tuf";
	public static String chimaeraSeq = "";

	private Vector<NTMSpecies> speciesList = null;

	@FXML private ScrollPane  fwdPane, revPane, alignmentPane, newAlignmentPane;
	@FXML private Label fwdTraceFileLabel, revTraceFileLabel;
	@FXML private Button removeRef, removeFwd, removeRev, removeVariant;
	@FXML private Button fwdHeteroBtn, revHeteroBtn;
	@FXML private CheckBox cb_hetIndelMode;
	@FXML private TextField tf_firstNumber;
	@FXML private ComboBox cb_targetRegion;

	@FXML private Button btn_settings;
	//@FXML private ImageView fwdRuler, revRuler;
	@FXML private TableView<Variant> variantTable;
	private String lastVisitedDir="C:\\Users\\user\\Desktop\\ygkim\\1906 NTM";
	private Stage primaryStage;
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * Settings parameters
	 */
	public static double secondPeakCutoff;
	public static int gapOpenPenalty;
	public static int filterQualityCutoff;
	public static String filteringOption;

	//public static boolean AIFiltering;


	public static boolean alignmentPerformed = false;
	public static Vector<AlignedPoint> alignedPoints = null;

	private int startRange = 0, endRange = 0;		//range : fwd, rev 양쪽다 align 된 range
	private File fwdTraceFile, revTraceFile;
	private static ReferenceSeq refFile;

	private GanseqTrace trimmedFwdTrace, trimmedRevTrace;
	private HeteroTrace fwdHeteroTrace, revHeteroTrace;
	private boolean refLoaded = false, fwdLoaded = false, revLoaded = false;

	private GridPane gridPane = null;
	private Label[][] labels = null;
	private static int fwdTraceStart = 0, fwdTraceEnd = 0;
	private static int revTraceStart = 0, revTraceEnd = 0;

	//trimming 진행중일때는 open trace, open ref버튼 다시 못누르게 하기 위함. 나중에 없어질 로직?
	private boolean trimmingOngoing;	


	private void checkVersion() {
		String homepage = "https://blog.naver.com/opaleye83", email = "opaleye83@naver.com", copyright = "Copyrightⓒ2019 by Young-gon Kim";
		String comment = "Ganseq NTM Ver " + version;
		comment += "\n\n" + homepage;
		comment += "\n" + email;
		comment += "\n\n" + copyright;

		textPopUp(comment);


		/* settings Property 읽기. 
		 * 
		 */
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(settingsFileName));
			fontSize = Integer.parseInt(props.getProperty("fontsize"));
			chimaeraSeq = props.getProperty("chimaera");
		}
		catch(Exception e) {
			e.printStackTrace();
			textPopUp("Cannot access to the configuration file. (settings/settings.properties) Please reinstall the program.");
			System.exit(0);
		}

	}

	/**
	 * Initializes required settings
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		checkVersion();
		readDefaultProperties();
		File tempFile = new File(lastVisitedDir);
		if(!tempFile.exists())
			lastVisitedDir=".";
		fwdHeteroBtn.setVisible(false);
		revHeteroBtn.setVisible(false);
		/*
		try {
			LSTMOutput.init();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp("Trained LSTM model file does not exist");
		}
		 */
	}


	private void readDefaultProperties() {
		cb_targetRegion.getItems().addAll(s16, rpo, tuf);
		cb_targetRegion.setValue(s16);

		secondPeakCutoff = 0.20;
		gapOpenPenalty = defaultGOP;
		filterQualityCutoff = 20;
		filteringOption = SettingsController.ruleBasedFiltering;

		String tooltipText ="Try this option when unexpected homo indels are detected due to misleading alignment\n" 
				+ "ex) homo indel + hetero indel is detected instead of one hetero indel";


		Tooltip tooltip = new Tooltip(tooltipText);
		tooltip.setAutoHide(false);
		TooltipDelay.activateTooltipInstantly(tooltip);
		TooltipDelay.holdTooltip(tooltip);
		cb_hetIndelMode.setTooltip(tooltip);

		cb_hetIndelMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov,
					Boolean old_val, Boolean new_val) {
				//System.out.println(cb_hetIndelMode.isSelected());
				if(cb_hetIndelMode.isSelected()) 
					gapOpenPenalty = 200;
				else
					gapOpenPenalty = defaultGOP;
			}
		});
	}

	public void setProperties(double secondPeakCutoff, int gapOpenPenalty, int filterQualityCutoff, String filteringOption) {
		RootController.secondPeakCutoff = secondPeakCutoff;
		RootController.gapOpenPenalty = gapOpenPenalty;
		RootController.filterQualityCutoff = filterQualityCutoff;
		RootController.filteringOption = filteringOption;

	}

	public void handleSettings() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			SettingsController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setRootController(this);
			controller.initValues(secondPeakCutoff, gapOpenPenalty, filterQualityCutoff, filteringOption);
			stage.setScene(new Scene(root1));
			stage.setTitle("Settings");
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}


	private void resetParameters() {
		alignmentPerformed = false;
		alignedPoints = null;
		gridPane = null;
		labels = null;
		alignmentPane.setContent(new Label(""));
		fwdTraceStart = 0; fwdTraceEnd = 0;
		revTraceStart = 0; revTraceEnd = 0;
		startRange = 0; 
		endRange = 0;		
		fwdHeteroTrace = null; 
		revHeteroTrace = null;
		fwdHeteroBtn.setVisible(false);
		revHeteroBtn.setVisible(false);
		variantTable.getItems().clear();
	}


	private void makeSpeciesList() throws Exception {
		speciesList = new Vector<NTMSpecies>();
		String targetRegion = (String)cb_targetRegion.getValue();


		File file = null;
		if(targetRegion.equals(s16))
			file = new File("reference/ref_16s.fasta");
		else if(targetRegion.equals(rpo))
			file = new File("reference/ref_rpob.fasta");
		else if(targetRegion.equals(tuf))
			file = new File("reference/ref_tuf.fasta");

		StringBuffer buffer = new StringBuffer();
		String wholeString = "";

		try (FileReader fileReader = new FileReader(file)){
			int ch;
			while ((ch = fileReader.read()) != -1) {
				char readChar = (char) ch;
				buffer.append(readChar);
			}
			wholeString = buffer.toString();

			String[] tokens = wholeString.split(">");

			/*
			for(String token:tokens) {
				System.out.println(token + "\n\n\n");
			}
			 */

			//  맨앞에 공백 한칸 들어감 --> 1부터 시작 
			for(int i=1;i<tokens.length;i++) {
				NTMSpecies tempSpecies = new NTMSpecies(tokens[i]);
				speciesList.add(tempSpecies);
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Error in reading reference file");
		}
	}





	/** 
	 * Open forward trace file and opens trim.fxml with that file
	 */
	public void handleOpenFwdTrace() {
		if(trimmingOngoing) return;

		File tempFile2 = new File(lastVisitedDir);
		if(!tempFile2.exists())
			lastVisitedDir=".";

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("AB1 Files", "*.ab1"), 
				new ExtensionFilter("All Files", "*.*"));
		fileChooser.setInitialDirectory(new File(lastVisitedDir));

		fwdTraceFile = fileChooser.showOpenDialog(primaryStage);
		if(fwdTraceFile == null) return;
		lastVisitedDir=fwdTraceFile.getParent();

		try {
			GanseqTrace tempTrace = new GanseqTrace(fwdTraceFile);
			if(tempTrace.getSequenceLength()<30) {
				popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
				return;
			}

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Trim.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			TrimController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);

			controller.setTargetTrace(tempTrace, GanseqTrace.FORWARD);

			controller.setRootController(this);
			controller.init();
			stage.setScene(new Scene(root1));
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.setTitle("Trim sequences");
			stage.show();
			stage.setOnCloseRequest( event -> {trimmingOngoing = false;} );
			trimmingOngoing = true;	// trimming 하는동안 (confirm trace 될때까지) open fwd tracefile 또 클릭하면 무시
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp("Error in loading forward trace file\n" + ex.getMessage());
			return;
		}
	}

	/**
	 * Loads the image of trimmed forward trace file
	 * @param trace : trimmed forward trace file
	 */
	public void confirmFwdTrace(GanseqTrace trimmedTrace) {
		trimmingOngoing = false;		

		trimmedFwdTrace = trimmedTrace;

		try {

			BufferedImage awtImage = trimmedFwdTrace.getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			//fwdRuler.setImage(trimmedFwdTrace.getRulerImage());

			String fileName = fwdTraceFile.getAbsolutePath();
			fwdTraceFileLabel.setText(fileName);
			fwdLoaded = true;
		}
		catch(Exception ex) {
			popUp("Error in loading forward trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {

		}
		resetParameters();
	}

	/**
	 * Remove forward trace file
	 */
	public void handleRemoveFwd() {
		resetParameters();
		fwdTraceFileLabel.setText("");
		fwdPane.setContent(new Label(""));
		fwdTraceFile = null;
		trimmedFwdTrace = null;
		fwdLoaded = false;
	}

	/** 
	 * Open reverse trace file and opens trim.fxml with that file
	 */
	public void handleOpenRevTrace() {
		if(trimmingOngoing) return;

		File tempFile2 = new File(lastVisitedDir);
		if(!tempFile2.exists())
			lastVisitedDir=".";

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("AB1 Files", "*.ab1"), 
				new ExtensionFilter("All Files", "*.*"));

		//fileChooser.setInitialDirectory(new File("f:/GoogleDrive/ganseq"));
		fileChooser.setInitialDirectory(new File(lastVisitedDir));
		revTraceFile = fileChooser.showOpenDialog(primaryStage);
		if(revTraceFile == null) return;
		lastVisitedDir=revTraceFile.getParent();

		try {
			GanseqTrace tempTrace = new GanseqTrace(revTraceFile);
			if(tempTrace.getSequenceLength()<30) {
				popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
				return;
			}

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Trim.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			TrimController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setTargetTrace(tempTrace, GanseqTrace.REVERSE);
			controller.setRootController(this);
			controller.init();
			stage.setScene(new Scene(root1));
			stage.setTitle("Trim sequences");
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.show();
			stage.setOnCloseRequest( event -> {trimmingOngoing = false;} );
			trimmingOngoing = true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp("Error in loading reverse trace file\n" + ex.getMessage());
			return;
		}
	}

	/**
	 * Loads the image of trimmed reverse trace file
	 * @param trace : trimmed reverse trace file
	 */
	public void confirmRevTrace(GanseqTrace trimmedTrace) {
		trimmingOngoing = false;
		trimmedRevTrace = trimmedTrace;

		try {
			BufferedImage awtImage = trimmedRevTrace.getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			revPane.setContent(imageView);

			//revRuler.setImage(trimmedRevTrace.getRulerImage());
			String fileName = revTraceFile.getAbsolutePath();
			revTraceFileLabel.setText(fileName);
			revLoaded = true;

		}
		catch(Exception ex) {
			popUp("Error in loading reverse trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		resetParameters();
	}

	/**
	 * Remove reverse trace file
	 */
	public void handleRemoveRev() {
		resetParameters();
		revTraceFileLabel.setText("");
		revPane.setContent(new Label(""));

		revTraceFile = null;
		trimmedRevTrace = null;
		revLoaded = false;
	}

	/**
	 * Activates Hetero Indel View for forward trace 
	 */
	public void handleFwdHetero() {
		try {
			if(trimmedFwdTrace == null) {
				popUp("forward trace file is not loaded!");
				return;
			}
			if(fwdHeteroTrace == null) {
				popUp("No Hetero Indel Detected");
				return;
			}
			if(alignmentPerformed == false) {
				popUp("Alignment is not performed yet!");
				return;
			}

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Hetero.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			HeteroController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setHeteroTrace(fwdHeteroTrace);
			stage.setScene(new Scene(root1)); 
			stage.setTitle("Hetero Indel View");
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.show();
			controller.showResult();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Activates Hetero Indel View for reverse trace 
	 */
	public void handleRevHetero() {
		try {
			if(trimmedRevTrace == null) {
				popUp("reverse trace file is not loaded!");
				return;
			}
			if(revHeteroTrace == null) {
				popUp("No Hetero Indel Detected");
				return;
			}
			if(alignmentPerformed == false) {
				popUp("Alignment is not performed yet!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Hetero.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			HeteroController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setHeteroTrace(revHeteroTrace);
			stage.setScene(new Scene(root1));
			stage.setTitle("Hetero Indel View");
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.show();
			controller.showResult();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Shows the message with a popup
	 * @param message : message to be showen
	 */
	public void popUp (String message) {
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


	/**
	 * Shows the message with a popup
	 * @param message : message to be showen
	 */
	public void textPopUp (String message) {
		Stage dialog = new Stage(StageStyle.DECORATED);
		dialog.initOwner(primaryStage);
		dialog.setTitle("Notice");
		Parent parent;
		try {
			parent = FXMLLoader.load(getClass().getResource("login.fxml"));
			TextArea ta_message = (TextArea)parent.lookup("#ta_message");


			ta_message.setText(message);
			Button okButton = (Button) parent.lookup("#okButton");
			okButton.setOnAction(event->dialog.close());
			Scene scene = new Scene(parent);

			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.showAndWait();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	private void doAlignment(int selectedSpecies) {
		resetParameters();
		Formatter.init();
		refFile = speciesList.get(selectedSpecies).getRefSeq();
		//When only fwd trace is given as input

		MMAlignment mma = new MMAlignment();
		AlignedPair fwdAp = null;
		AlignedPair revAp = null;

		if(fwdLoaded == true) {
			try {
				fwdAp = mma.localAlignment(refFile.getRefString(), trimmedFwdTrace.getSequence());
			}

			catch (Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}

		if(revLoaded == true) {
			try {
				revAp = mma.localAlignment(refFile.getRefString(), trimmedRevTrace.getSequence());
			}

			catch (Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}




		if(fwdLoaded == true && revLoaded == false) {
			try {
				alignedPoints = Formatter.format2(fwdAp, refFile, trimmedFwdTrace, 1);
			}

			catch (Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}

		//When only rev trace is given as input
		else if(fwdLoaded == false && revLoaded == true) {
			try {
				alignedPoints = Formatter.format2(revAp, refFile, trimmedRevTrace, -1);
			}
			catch (Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}

		//When both of fwd trace and rev trace are given
		else  if(fwdLoaded == true && revLoaded == true) {

			try {
				alignedPoints = Formatter.format3(fwdAp, revAp, refFile, trimmedFwdTrace, trimmedRevTrace);
			}
			catch (NoContigException ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
			catch (Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}
		setRange();
	}

	/**
	 * Performs alignment, Detects variants, Shows results
	 */
	public void handleRun() {
		if(fwdLoaded == false && revLoaded == false) {  
			popUp("At least one of forward trace file and reverse trace file \n should be loaded before running.");
			return;
		}

		try {
			makeSpeciesList();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
		}

		for(int i=0;i<speciesList.size();i++) {
			NTMSpecies thisSpecies = speciesList.get(i);
			doAlignment(i);

			int i_score = 0;
			double d_score = 0;
			for(int j=0;j<alignedPoints.size();j++) {
				AlignedPoint ap = alignedPoints.get(j);
				if(ap.getDiscrepency()!='*') i_score++;
			}
			if(alignedPoints.size() != 0)
				d_score = (double)i_score / alignedPoints.size();
			d_score*=100;
			//System.out.println("score : " + d_score);
			thisSpecies.setScore(d_score);
		}

		Collections.sort(speciesList);


		for(int i=0;i<Integer.min(5, speciesList.size());i++) {
			NTMSpecies thisSpecies = speciesList.get(i);
			System.out.println(thisSpecies.getAccession() + ", " + thisSpecies.getSpeciesName());
			System.out.println("score : " + thisSpecies.getScore());
		}



		/*








		doAlignment();



		printAlignedResult();
		alignmentPerformed = true;

		if(fwdLoaded) {
			// 새로운 좌표로 update (fwdPane, revPane)
			java.awt.image.BufferedImage awtImage = trimmedFwdTrace.getShadedImage(0,0,0);
			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			// 시작점에 화면 align
			adjustFwdPane(Formatter.fwdTraceAlignStartPoint);
		}
		if(revLoaded) {
			java.awt.image.BufferedImage awtImage2 = trimmedRevTrace.getShadedImage(0,0,0);
			javafx.scene.image.Image fxImage2 = SwingFXUtils.toFXImage(awtImage2, null);
			ImageView imageView2 = new ImageView(fxImage2);
			revPane.setContent(imageView2);
			adjustRevPane(Formatter.revTraceAlignStartPoint);
		}

		Vector<Variant> heteroIndelList = detectHeteroIndel();
		VariantCallerFilter vcf = new VariantCallerFilter(fwdLoaded, revLoaded, startRange, endRange, heteroIndelList, trimmedFwdTrace, trimmedRevTrace );
		TreeSet<Variant> variantList = vcf.getVariantList();


		if(variantList.size()==0) popUp("No variant detected!");
		else {
			variantTable.setEditable(true);
			TableColumn tcVariant = variantTable.getColumns().get(0);
			TableColumn tcZygosity = variantTable.getColumns().get(1);
			TableColumn tcFrequency = variantTable.getColumns().get(2);
			TableColumn tcFrom = variantTable.getColumns().get(3);
			TableColumn tcEquivalentExpressions = variantTable.getColumns().get(4);

			tcVariant.setCellValueFactory(new PropertyValueFactory("variantProperty"));
			tcZygosity.setCellValueFactory(new PropertyValueFactory("zygosityProperty"));
			tcFrequency.setCellValueFactory(new PropertyValueFactory("frequencyProperty"));
			tcFrom.setCellValueFactory(new PropertyValueFactory("fromProperty"));
			tcEquivalentExpressions.setCellValueFactory(new PropertyValueFactory("equivalentExpressionsProperty"));

			tcVariant.setCellFactory(TextFieldTableCell.<Variant>forTableColumn());

			variantTable.setItems(FXCollections.observableArrayList(variantList));



			variantTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if(newValue.intValue()<0) return; 

					Variant variant = variantTable.getItems().get(newValue.intValue());

					if(variant instanceof Indel && ((Indel) variant).getZygosity().equals("homo"))
						focus2((Indel)variant);
					else 
						focus(variant.getAlignmentIndex()-1, variant.getFwdTraceIndex(), variant.getRevTraceIndex(), variant.getFwdTraceChar(), variant.getRevTraceChar());

				}
			});

			Scene scene = primaryStage.getScene();
			scene.setOnKeyPressed(event-> {
				if(event.getCode()==KeyCode.DELETE) handleRemoveVariant();
			});

		}

		 */
	}

	/**
	 * Sets the start and end range of the alignment
	 */
	private void setRange() {
		boolean fwdFound = false, revFound = false;
		for(int i=0;i<alignedPoints.size();i++) {
			AlignedPoint ap = alignedPoints.get(i);
			if(!fwdFound && ap.getFwdChar() != Formatter.gapChar) { 
				fwdTraceStart = i+1;
				trimmedFwdTrace.setAlignedRegionStart(ap.getFwdTraceIndex());
				fwdFound = true;
			}
			if(!revFound && ap.getRevChar() != Formatter.gapChar) { 
				revTraceStart = i+1;
				trimmedRevTrace.setAlignedRegionStart(ap.getRevTraceIndex());
				revFound = true;
			}
			if(revFound && fwdFound) break;
		}

		fwdFound = false; 
		revFound = false;
		for(int i=alignedPoints.size()-1;i>=0; i--) {
			AlignedPoint ap = alignedPoints.get(i);
			if(!fwdFound && ap.getFwdChar() != Formatter.gapChar) { 
				fwdTraceEnd = i+1;
				trimmedFwdTrace.setAlignedRegionEnd(ap.getFwdTraceIndex());
				fwdFound = true;
			}
			if(!revFound && ap.getRevChar() != Formatter.gapChar) { 
				revTraceEnd = i+1;
				trimmedRevTrace.setAlignedRegionEnd(ap.getRevTraceIndex());
				revFound = true;
			}
			if(revFound && fwdFound) break;
		}

		startRange = Integer.max(fwdTraceStart,revTraceStart);
		endRange = Integer.min(fwdTraceEnd, revTraceEnd);

	}


	private Vector<Variant> detectHeteroIndel() {
		Vector<Variant> heteroIndelList = new Vector<Variant>();
		Variant fwdIndel=null, revIndel = null;

		fwdHeteroTrace = null;
		revHeteroTrace = null;

		char tempFwdChar = 'N';
		int tempFwdIndex = 0;

		if(fwdLoaded) {
			fwdHeteroTrace = new HeteroTrace(trimmedFwdTrace);
			fwdIndel = fwdHeteroTrace.detectHeteroIndel();

			if(fwdIndel != null) {
				tempFwdChar = fwdIndel.getFwdTraceChar();
				tempFwdIndex = fwdIndel.getFwdTraceIndex();
				heteroIndelList.add(fwdIndel);
				fwdHeteroBtn.setVisible(true);
			}
			else fwdHeteroTrace = null;
		}


		if(revLoaded) {
			revHeteroTrace = new HeteroTrace(trimmedRevTrace);
			revIndel = revHeteroTrace.detectHeteroIndel();
			if(revIndel != null) {
				//fwd, rev 양쪽에서 같은 variant 찾을 경우, fwd, revpane 모두에서 볼 수 있게. --> 나중에 fwd꺼 지우기. range 설정한 다음 지워야 함. VariantCollerFilter.makeVariantList()
				if(fwdIndel != null) 
					if(fwdIndel.getHGVS().equals(revIndel.getHGVS())) {
						revIndel.setHitCount(2);
						revIndel.setFwdTraceChar(tempFwdChar);
						revIndel.setFwdTraceIndex(tempFwdIndex);
					}
				heteroIndelList.add(revIndel);
				revHeteroBtn.setVisible(true);
			}
			else revHeteroTrace = null;
		}
		return heteroIndelList;
	}

	/**
	 * Prints the result of alignment on the alignment pane
	 */
	private void printAlignedResult() {
		labels = new Label[3][alignedPoints.size()];
		gridPane = new GridPane();

		Label refTitle = new Label("Reference : ");
		refTitle.setFont(new Font("Consolas", 14));
		refTitle.setMinSize(130,15);
		refTitle.setPrefSize(130, 15);
		gridPane.add(refTitle, 0,  1);

		if(fwdLoaded) {
			Label fwdTitle = new Label("Forward   : ");
			fwdTitle.setFont(new Font("Consolas", 14));
			fwdTitle.setMinSize(130,15);
			fwdTitle.setPrefSize(130, 15);
			gridPane.add(fwdTitle, 0,  2);
		}

		if(revLoaded) {
			Label revTitle = new Label("Reverse   : ");
			revTitle.setFont(new Font("Consolas", 14));
			revTitle.setMinSize(130,15);
			revTitle.setPrefSize(130, 15);
			gridPane.add(revTitle, 0,  3);
		}

		for (int i=0;i<alignedPoints.size();i++) {
			AlignedPoint point = alignedPoints.get(i);

			//Tooltip 설정
			String tooltipText = (i+1) + "\nCoding DNA : " + point.getStringCIndex() + "\nBase # in reference : " + point.getGIndex() + "\n";

			Tooltip tooltip = new Tooltip(tooltipText);
			//tooltip.setOpacity(0.7);
			tooltip.setAutoHide(false);
			TooltipDelay.activateTooltipInstantly(tooltip);
			TooltipDelay.holdTooltip(tooltip);

			Label refLabel = new Label();
			Label fwdLabel = new Label();
			Label revLabel = new Label();
			Label discrepencyLabel = new Label();
			Label indexLabel = new Label();

			refLabel.setFont(new Font("Consolas", fontSize));
			fwdLabel.setFont(new Font("Consolas", fontSize));
			revLabel.setFont(new Font("Consolas", fontSize));
			discrepencyLabel.setFont(new Font("Consolas", fontSize));
			indexLabel.setFont(new Font("Consolas", fontSize));

			int fwdTraceIndex = point.getFwdTraceIndex();
			int revTraceIndex = point.getRevTraceIndex();

			refLabel.setTooltip(tooltip);
			discrepencyLabel.setTooltip(tooltip);
			indexLabel.setTooltip(tooltip);
			fwdLabel.setTooltip(tooltip);
			revLabel.setTooltip(tooltip);

			//Index  
			if(i%10==0 && alignedPoints.size()-i >= 5) {
				indexLabel.setText(String.valueOf(i+1));
				GridPane.setColumnSpan(indexLabel, 10);
				indexLabel.setPrefSize(100, 10);
				indexLabel.setOnMouseClicked(new ClickEventHandler(i, fwdTraceIndex, revTraceIndex, point.getFwdChar(), point.getRevChar()));
				gridPane.add(indexLabel, i+1, 0);
			}

			//Reference
			String sRefChar = Character.toString(point.getRefChar());
			if(!point.isCoding()) sRefChar = sRefChar.toLowerCase();
			refLabel.setText(sRefChar);
			refLabel.setPrefSize(10, 10);
			refLabel.setOnMouseClicked(new ClickEventHandler(i, fwdTraceIndex, revTraceIndex, point.getFwdChar(), point.getRevChar()));


			gridPane.add(refLabel,  i+1, 1);
			labels[0][i] = refLabel;

			//Forward
			if(fwdLoaded) {
				fwdLabel.setText(Character.toString(point.getFwdChar()));
				//fwdLabel.setTextFill(Color.web("#8BBCFF"));

				if(point.getFwdChar() == Formatter.gapChar || point.getFwdQuality()>=40)
					fwdLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getFwdQuality()>=30)
					fwdLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFFC6"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getFwdQuality()>=20)
					fwdLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFF5A"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getFwdQuality()>=10)
					fwdLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFBB00"), CornerRadii.EMPTY, Insets.EMPTY)));
				else 
					fwdLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFBB00"), CornerRadii.EMPTY, Insets.EMPTY)));

				fwdLabel.setPrefSize(10, 10);
				//System.out.println("forward trace index : " + point.getFwdTraceIndex());
				fwdLabel.setOnMouseClicked(new ClickEventHandler(i, fwdTraceIndex, revTraceIndex, point.getFwdChar(), point.getRevChar()));
				gridPane.add(fwdLabel,  i+1, 2);
				labels[1][i] = fwdLabel;
			}

			//Reverse
			if(revLoaded) {
				revLabel.setText(Character.toString(point.getRevChar()));
				if(point.getRevChar() == Formatter.gapChar || point.getRevQuality()>=40)
					revLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getRevQuality()>=30)
					revLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFFC6"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getRevQuality()>=20)
					revLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFFF5A"), CornerRadii.EMPTY, Insets.EMPTY)));
				else if(point.getRevQuality()>=10)
					revLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFBB00"), CornerRadii.EMPTY, Insets.EMPTY)));
				else 
					revLabel.setBackground(new Background(new BackgroundFill(Color.web("#FFBB00"), CornerRadii.EMPTY, Insets.EMPTY)));

				revLabel.setPrefSize(10, 10);
				revLabel.setOnMouseClicked(new ClickEventHandler(i, fwdTraceIndex, revTraceIndex, point.getFwdChar(), point.getRevChar()));
				gridPane.add(revLabel,  i+1, 3);
				labels[2][i] = revLabel;
			}

			//Discrepency
			discrepencyLabel.setText(Character.toString(point.getDiscrepency()));
			discrepencyLabel.setPrefSize(10, 10);
			discrepencyLabel.setOnMouseClicked(new ClickEventHandler(i, fwdTraceIndex, revTraceIndex, point.getFwdChar(), point.getRevChar()));
			gridPane.add(discrepencyLabel,  i+1, 4);
		}

		alignmentPane.setContent(gridPane);
	}

	/**
	 * Focuses the designated point on the forward trace pane
	 * @param fwdTraceIndex : the point to be focused
	 */
	private void adjustFwdPane(int fwdTraceIndex) {
		if(Formatter.fwdNewLength <= 1280) return;
		if(fwdTraceIndex > trimmedFwdTrace.getBaseCalls().length)
			fwdTraceIndex = trimmedFwdTrace.getBaseCalls().length;
		double coordinate = Formatter.fwdStartOffset + trimmedFwdTrace.getBaseCalls()[fwdTraceIndex-1]*2;
		System.out.println("fwdTrace Index : " + fwdTraceIndex);
		System.out.println("p = " + coordinate);
		double hValue = (coordinate - 640.0) / (Formatter.fwdNewLength - 1280);
		fwdPane.setHvalue(hValue);
	}

	/**
	 * Focuses the designated point on the reverse trace pane
	 * @param revTraceIndex : the point to be focused
	 */
	private void adjustRevPane(int revTraceIndex) {
		if(Formatter.revNewLength <= 1280) return;
		if(revTraceIndex > trimmedRevTrace.getBaseCalls().length)
			revTraceIndex = trimmedRevTrace.getBaseCalls().length;

		double coordinate = Formatter.revStartOffset + trimmedRevTrace.getBaseCalls()[revTraceIndex-1]*2;
		System.out.println("revTrace Index : " + revTraceIndex);
		System.out.println("p = " + coordinate);

		double hValue = (coordinate - 640.0) / (Formatter.revNewLength - 1280);
		revPane.setHvalue(hValue);

	}

	/**
	 * Focuses the designated point on the alignment pane
	 * @param index : the point to be focused
	 */
	private void adjustAlignmentPane(int index) {
		if(labels==null) return;
		if(labels[0]==null) return;

		double length = labels[0][labels[0].length-1].getLayoutX();
		if(length<=1280) return;
		double coordinate = labels[0][index].getLayoutX();
		double hValue = (coordinate - 640.0) / (length - 1280.0);
		alignmentPane.setHvalue(hValue);

	}

	/**
	 * Focus method for Homo deletion variants (highlight range)
	 * Homo insertion : When test data is available
	 * Will be finished Later
	 * @param indel 
	 */
	public void focus2(Indel indel) {
		//다 1부터 시작하는 좌표
		System.out.println("fwd loaded : " + fwdLoaded);
		System.out.println("rev loaded : " + revLoaded);
		int startAlignmentPos=0, endAlignmentPos=0;	
		int startFwdTracePos=0, endFwdTracePos=0;
		int startRevTracePos=0, endRevTracePos=0;

		AlignedPoint ap = null;
		if(indel.getType() == Indel.duplication) {
			startAlignmentPos = indel.getAlignmentIndex();
			ap = alignedPoints.get(startAlignmentPos-1);
		}

		else {
			if(indel.getAlignmentIndex() > 1) 	//이미 맨 왼쪽 아니라면 한칸 왼쪽의 점 선택
				startAlignmentPos = indel.getAlignmentIndex() - 1;
			else
				startAlignmentPos = indel.getAlignmentIndex();
			ap = alignedPoints.get(startAlignmentPos-1);
		}


		startFwdTracePos =  ap.getFwdTraceIndex();
		startRevTracePos = ap.getRevTraceIndex();

		int counter = 0;
		AlignedPoint ap2 = null;

		int endOffset = 0;
		if(indel.getType()==Indel.deletion || indel.getType() == Indel.duplication)
			endOffset = 1;


		System.out.println(String.format("g1 : %d, g2 : %d",  indel.getgIndex(), indel.getgIndex2()));

		while(indel.getAlignmentIndex()-1+counter < alignedPoints.size()) {
			ap2 = alignedPoints.get(indel.getAlignmentIndex()-1+counter);
			if(ap2.getGIndex() == indel.getgIndex2()+endOffset) {
				counter++;
				break;
			}
			counter++;
		}
		counter--;


		endAlignmentPos =  indel.getAlignmentIndex()+counter;
		endFwdTracePos = ap2.getFwdTraceIndex();
		endRevTracePos = ap2.getRevTraceIndex();

		if(indel.getType() == Indel.duplication) {
			endAlignmentPos--;
			endFwdTracePos--;
			endRevTracePos--;
		}


		adjustAlignmentPane(startAlignmentPos-1);
		for(int i=0; i<alignedPoints.size();i++) {
			Label boxedLabel = labels[0][i];
			if(boxedLabel == null) continue;
			if(i >= startAlignmentPos-1 && i<= endAlignmentPos-1) {
				boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
			}
			else {
				boxedLabel.setBorder(Border.EMPTY);
			}
		}
		if(fwdLoaded) {
			for(int i=0; i<alignedPoints.size();i++) {
				Label boxedLabel = labels[1][i];
				if(boxedLabel == null) continue;
				if(i >= startAlignmentPos-1 && i<= endAlignmentPos-1 && (i+1) >= fwdTraceStart && (i+1) <= fwdTraceEnd) {
					boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				}
				else {
					boxedLabel.setBorder(Border.EMPTY);
				}
			}


			int colorStart = Integer.max(0, startFwdTracePos-1);
			int colorEnd = Integer.max(0, endFwdTracePos-1);

			java.awt.image.BufferedImage awtImage = trimmedFwdTrace.getShadedImage(2, colorStart, colorEnd);
			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);

			adjustFwdPane(startFwdTracePos);
		}
		if(revLoaded) {
			for(int i=0; i<alignedPoints.size();i++) {
				Label boxedLabel = labels[2][i];
				if(boxedLabel == null) continue;
				if(i >= startAlignmentPos-1 && i<= endAlignmentPos-1 && (i+1) >= revTraceStart && (i+1) <= revTraceEnd) {
					boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				}
				else {
					boxedLabel.setBorder(Border.EMPTY);
				}
			}

			int colorStart = Integer.max(0, startRevTracePos-1);
			int colorEnd = Integer.max(0, endRevTracePos-1);

			java.awt.image.BufferedImage awtImage2 = trimmedRevTrace.getShadedImage(2, colorStart, colorEnd);
			javafx.scene.image.Image fxImage2 = SwingFXUtils.toFXImage(awtImage2, null);
			ImageView imageView2 = new ImageView(fxImage2);
			revPane.setContent(imageView2);

			adjustRevPane(startRevTracePos);
		}
	}

	/**
	 * Focuses on the designated points (Alignment pane, forward trace pane, reverse trace pane)
	 * @param selectedAlignmentPos : position to be focused on the alignment pane
	 * @param selectedFwdPos : position to be focused on the forward trace pane
	 * @param selectedRevPos : position to be focused on the reverse trace pane
	 * @param fwdChar : If it is gap, not focused
	 * @param revChar : If it is gap, not focused
	 */
	public void focus(int selectedAlignmentPos, int selectedFwdPos, int selectedRevPos, char fwdChar, char revChar) {
		//selectedAlignmentPos : 이것만 0부터 시작하는 index
		//selectedFwdPos, selectedRevPos : 1부터 시작하는 index
		boolean fwdGap = (fwdChar == Formatter.gapChar); 
		boolean revGap = (revChar == Formatter.gapChar);

		for(int i=0; i<alignedPoints.size();i++) {
			Label boxedLabel = labels[0][i];
			if(boxedLabel == null) continue;
			if(i==selectedAlignmentPos) {
				boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				adjustAlignmentPane(i);
			}
			else {
				boxedLabel.setBorder(Border.EMPTY);
			}
		}
		if(fwdLoaded) {
			for(int i=0; i<alignedPoints.size();i++) {
				Label boxedLabel = labels[1][i];
				if(boxedLabel == null) continue;
				if(i==selectedAlignmentPos) {
					boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, 
							BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				}
				else {
					boxedLabel.setBorder(Border.EMPTY);
				}
			}

			int tempFwdPos = selectedFwdPos;

			BufferedImage awtImage = null;
			if(fwdGap == true) awtImage = trimmedFwdTrace.getShadedImage(0,0,0);
			else awtImage = trimmedFwdTrace.getShadedImage(1, tempFwdPos-1, tempFwdPos-1);

			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);

			adjustFwdPane(selectedFwdPos);
		}
		if(revLoaded) {
			for(int i=0; i<alignedPoints.size();i++) {
				Label boxedLabel = labels[2][i];
				if(boxedLabel == null) continue;
				if(i==selectedAlignmentPos) {
					boxedLabel.setBorder(new Border(new BorderStroke(Color.BLUE, 
							BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
				}
				else {
					boxedLabel.setBorder(Border.EMPTY);
				}
			}

			int tempRevPos = selectedRevPos;
			BufferedImage awtImage2 = null;
			if(revGap == true) awtImage2 = trimmedRevTrace.getShadedImage(0,0,0);
			else awtImage2 = trimmedRevTrace.getShadedImage(1, tempRevPos-1, tempRevPos-1);
			javafx.scene.image.Image fxImage2 = SwingFXUtils.toFXImage(awtImage2, null);
			ImageView imageView2 = new ImageView(fxImage2);
			revPane.setContent(imageView2);

			adjustRevPane(selectedRevPos);
		}
	}

	/**
	 * Title : ClickEventHandler
	 * Click event handler for focusing
	 * @author Young-gon Kim
	 */
	class ClickEventHandler implements EventHandler<MouseEvent> {
		private int selectedAlignmentPos = 0, selectedFwdPos = 0, selectedRevPos = 0;
		char fwdChar, revChar;
		public ClickEventHandler(int selectedAlignmentPos, int selectedFwdPos, int selectedRevPos, char fwdChar, char revChar) {
			super();
			this.selectedAlignmentPos = selectedAlignmentPos;
			this.selectedFwdPos = selectedFwdPos;
			this.selectedRevPos = selectedRevPos;
			this.fwdChar = fwdChar;
			this.revChar = revChar;
		}

		@Override
		public void handle(MouseEvent t) {
			focus(selectedAlignmentPos, selectedFwdPos, selectedRevPos, fwdChar, revChar);
		}
	}

	/**
	 * Handler for remove button
	 */
	public void handleRemoveVariant() {
		//if(!variantListViewFocused) return;
		int index = variantTable.getSelectionModel().getSelectedIndex();
		System.out.println(String.format("remove index : %d",  index));
		if(index == -1) return;
		int newSelectedIdx = (index == variantTable.getItems().size() - 1)
				? index - 1
						: index;
		//o_variantList.remove(index);
		//v_variantList.remove(index);
		variantTable.getItems().remove(index);

		variantTable.getSelectionModel().select(newSelectedIdx); // 지워지면 그 위에꺼 자동으로 가리키니까 그냥 그 자리에 있도록

		if(index == 0 && variantTable.getItems().size()>0) {	//맨위에꺼 지우면 그위에꺼 자동으로 못 가리킴. changeListener 호출 안함 --> 강제로  focus
			Variant variant = variantTable.getItems().get(0);
			if(variant instanceof Indel && ((Indel) variant).getZygosity().equals("homo"))
				focus2((Indel)variant);
			else 
				focus(variant.getAlignmentIndex()-1, variant.getFwdTraceIndex(), variant.getRevTraceIndex(), variant.getFwdTraceChar(), variant.getRevTraceChar());
		}
	}


	/**
	 * Getters for member variables
	 */
	public static ReferenceSeq getRefFile() {
		return refFile;
	}

	public GanseqTrace getTrimmedFwdTrace() {
		return trimmedFwdTrace;
	}

	public GanseqTrace getTrimmedRevTrace() {
		return trimmedRevTrace;
	}

	public boolean getFwdLoaded() {
		return fwdLoaded;
	}


	public boolean getRevLoaded() {
		return revLoaded;
	}


}



