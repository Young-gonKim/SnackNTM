package com.opaleye.ganseq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;

import com.opaleye.ganseq.mmalignment.AlignedPair;
import com.opaleye.ganseq.mmalignment.MMAlignment;
import com.opaleye.ganseq.reference.ReferenceSeq;
import com.opaleye.ganseq.settings.SettingsController;
import com.opaleye.ganseq.tools.TooltipDelay;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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

	//constants
	private static final double paneWidth = 907; 
	private static final String s16 = "16s rRNA";
	private static final String rpo = "rpo";
	private static final String tuf = "tuf";
	public static final int defaultGOP = 30;
	public static final String version = "1.1";
	private static final double tableRowHeight = 25.0;
	private static final String settingsFileName = "settings/settings.properties";
	private static String icSeq = "ATCGACGAAGGTCCGGGTTTTCTCGGATT";
	private static String chSeq = "ATCGACGAAGGTTCGGGTTTTCTCGGATT";
	public static TreeSet<String> rgmSet = new TreeSet<String>(); 
	public static TreeSet<String> sgmSet = new TreeSet<String>();
	private static int fontSize = 13;



	private String csvContents = "";


	private boolean s16Loaded = false;
	private boolean rpoLoaded = false;
	private boolean tufLoaded = false;

	private String targetRegion = null;



	@FXML private ScrollPane  fwdPane, revPane, alignmentPane, newAlignmentPane;
	@FXML private Label fwdTraceFileLabel, revTraceFileLabel, icSeqLabel, chimaeraSeqLabel;
	@FXML private Button removeRef, removeFwd, removeRev, removeVariant;
	@FXML private ComboBox cb_targetRegion;
	@FXML private Button btnEditBase;
	@FXML private Button btn_settings;
	//@FXML private ImageView fwdRuler, revRuler;
	@FXML private TableView<NTMSpecies> speciesTable, s16Table, rpoTable, tufTable, finalTable;
	private String lastVisitedDir="C:\\Users\\user\\Desktop\\ygkim\\1906 NTM";
	private Stage primaryStage;
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	private GridPane gridPane = null;
	private Label[][] labels = null;


	/**
	 * Settings parameters
	 */
	public static double secondPeakCutoff;
	public static int gapOpenPenalty;
	public static int filterQualityCutoff;
	public static String filteringOption;


	/**
	 * For context switching
	 */

	//0: 16S rRNA, 1: rpoB, 2: tuf
	private int context = 0;

	//context-specific variables
	private Vector<NTMSpecies>[] speciesList = new Vector[3];
	public static boolean[] alignmentPerformed = {false, false, false};
	public static Vector<AlignedPoint>[] alignedPoints = new Vector[3];
	private File[] fwdTraceFile = new File[3], revTraceFile = new File[3];
	private GanseqTrace[] trimmedFwdTrace = new GanseqTrace[3], trimmedRevTrace = new GanseqTrace[3];
	private boolean fwdLoaded[] = {false, false, false}, revLoaded[] = {false, false, false};
	//edit base 용
	private int[] selectedAlignmentPos = {-1, -1, -1};





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
			chSeq = props.getProperty("chimaera");
			icSeq = props.getProperty("intracellularae");
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


	}

	private void readDefaultProperties() {
		cb_targetRegion.getItems().addAll(s16, rpo, tuf);
		cb_targetRegion.setValue(s16);
		cb_targetRegion.valueProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue ov, String t, String t1) {
				handleRemoveFwd();
				handleRemoveRev();
				speciesTable.setItems(FXCollections.observableArrayList(new Vector<NTMSpecies>()));
				csvContents = "";
			}    
		});

		gapOpenPenalty = defaultGOP;
	}

	public void setProperties(int gapOpenPenalty) {
		RootController.gapOpenPenalty = gapOpenPenalty;

	}


	public void handleEditBase() {
		if(!alignmentPerformed[0]) return;
		if(selectedAlignmentPos[0] == -1) return;
		AlignedPoint ap = alignedPoints[0].get(selectedAlignmentPos[0]);

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("editbase.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			EditBaseController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setRootController(this);
			controller.setInitBases(String.format("%c", ap.getRefChar()), String.format("%c", ap.getFwdChar()), String.format("%c", ap.getRevChar()) );
			stage.setScene(new Scene(root1));
			stage.setTitle("Edit base");
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	public void updateBase(char newFwdChar, char newRevChar) {
		AlignedPoint ap = alignedPoints[0].get(selectedAlignmentPos[0]);

		if(fwdLoaded[0]) {
			trimmedFwdTrace[0].editBase(ap.getFwdTraceIndex(), ap.getFwdChar(), newFwdChar);
		}
		if(revLoaded[0]) {
			trimmedRevTrace[0].editBase(ap.getRevTraceIndex(), ap.getRevChar(), newRevChar);
		}

		//새로 alignment 실행 && 원래 보여주고 있던 곳 보여주기
		NTMSpecies selectedSpecies = speciesTable.getSelectionModel().getSelectedItem();
		int oldAlignmentPos = selectedAlignmentPos[0];
		String selectedSpeciesName = selectedSpecies.getSpeciesName();
		//System.out.println("selected species : " + selectedSpeciesName);

		handleRun();

		for(int i=0;i<speciesList[0].size();i++) {
			NTMSpecies ntm = speciesList[0].get(i);
			if(ntm.getSpeciesName().equals(selectedSpeciesName)) {
				speciesTable.getSelectionModel().select(i);
				focus(oldAlignmentPos);
				break;
			}
		}
	}



	public void handleSettings() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			SettingsController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);
			controller.setRootController(this);
			controller.initValues(gapOpenPenalty);
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
		alignmentPerformed[0] = false;
		alignedPoints[0] = null;
		gridPane = null;
		labels = null;
		alignmentPane.setContent(new Label(""));

	}


	private void makeSpeciesList() throws Exception {
		speciesList[0] = new Vector<NTMSpecies>();
		targetRegion = (String)cb_targetRegion.getValue();

		//read rgm list
		File file = new File("reference/rgm.txt");

		StringBuffer buffer = new StringBuffer();
		String wholeString = "";

		try (FileReader fileReader = new FileReader(file)){
			int ch;
			while ((ch = fileReader.read()) != -1) {
				char readChar = (char) ch;
				buffer.append(readChar);
			}
			wholeString = buffer.toString();

			String[] tokens = wholeString.split("\n");
			for(String token:tokens) {
				token = token.trim();
				rgmSet.add(token);
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Error in reading rgm.txt");
		}


		//read sgm list
		file = new File("reference/sgm.txt");

		buffer = new StringBuffer();
		wholeString = "";

		try (FileReader fileReader = new FileReader(file)){
			int ch;
			while ((ch = fileReader.read()) != -1) {
				char readChar = (char) ch;
				buffer.append(readChar);
			}
			wholeString = buffer.toString();

			String[] tokens = wholeString.split("\n");
			for(String token:tokens) {
				token = token.trim();
				sgmSet.add(token);
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Error in reading sgm.txt");
		}

		file = null;
		if(targetRegion.equals(s16))
			file = new File("reference/ref16s.fasta");
		else if(targetRegion.equals(rpo))
			file = new File("reference/refrpob.fasta");
		else if(targetRegion.equals(tuf))
			file = new File("reference/reftuf.fasta");

		buffer = new StringBuffer();
		wholeString = "";

		try (FileReader fileReader = new FileReader(file)){
			int ch;
			while ((ch = fileReader.read()) != -1) {
				char readChar = (char) ch;
				buffer.append(readChar);
			}
			wholeString = buffer.toString();

			String[] tokens = wholeString.split(">");

			//  맨앞에 공백 한칸 들어감 --> 1부터 시작 
			for(int i=1;i<tokens.length;i++) {
				NTMSpecies tempSpecies = new NTMSpecies(tokens[i]);
				speciesList[0].add(tempSpecies);
			}

		}catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Error in reading reference file");
		}


	}

	/** 
	 * Open forward trace file and opens trim.fxml with that file
	 */
	public void handleOpenTrace() {

		File tempFile2 = new File(lastVisitedDir);
		if(!tempFile2.exists())
			lastVisitedDir=".";

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("AB1 Files", "*.ab1"), 
				new ExtensionFilter("All Files", "*.*"));
		fileChooser.setInitialDirectory(new File(lastVisitedDir));


		List<File> fileList = fileChooser.showOpenMultipleDialog(primaryStage);
		if (fileList==null || fileList.size()==0) return;
		lastVisitedDir=fileList.get(0).getParent();

		boolean[] fwdFound = {false, false, false};
		boolean[] revFound = {false, false, false};

		for(File file:fileList) {
			String fileName = file.getName();
			//System.out.println(file.getName());
			if(fileName.contains("16s_F")) {
				fwdTraceFile[0] = file;
				fwdFound[0] = true;
			}

			else if(fileName.contains("16s_R")) {
				revTraceFile[0] = file;
				revFound[0] = true;
			}
		}

		//load fwd
		if(fwdFound[0]) {
			try {
				GanseqTrace tempTrace = new GanseqTrace(fwdTraceFile[0]);
				if(tempTrace.getSequenceLength()<30) {
					popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
					return;
				}
				int startTrimPosition = tempTrace.getFrontTrimPosition();
				int endTrimPosition = tempTrace.getTailTrimPosition();
				tempTrace.makeTrimmedTrace(startTrimPosition, endTrimPosition);

				confirmFwdTrace(tempTrace);

			}
			catch (Exception ex) {
				ex.printStackTrace();
				popUp("Error in loading trace files\n" + ex.getMessage());
				return;
			}
		}

		//Load rev
		if(revFound[0]) {
			try {
				GanseqTrace tempTrace = new GanseqTrace(revTraceFile[0]);
				if(tempTrace.getSequenceLength()<30) {
					popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
					return;
				}
				int startTrimPosition = tempTrace.getFrontTrimPosition();
				int endTrimPosition = tempTrace.getTailTrimPosition();
				tempTrace.makeTrimmedTrace(startTrimPosition, endTrimPosition);
				//make complement
				tempTrace.makeComplement();
				confirmRevTrace(tempTrace);

			}
			catch (Exception ex) {
				ex.printStackTrace();
				popUp("Error in loading trace files\n" + ex.getMessage());
				return;
			}
		}

	}

	public void handleFwdEditTrimming() {
		GanseqTrace tempTrace = null;
		try {
			tempTrace = new GanseqTrace(fwdTraceFile[0]);
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
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
	}
	
	public void handleRevEditTrimming() {
		GanseqTrace tempTrace = null;
		try {
			tempTrace = new GanseqTrace(revTraceFile[0]);
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Trim.fxml"));
			Parent root1 = (Parent) fxmlLoader.load();
			Stage stage = new Stage();
			TrimController controller = fxmlLoader.getController();
			controller.setPrimaryStage(stage);

			controller.setTargetTrace(tempTrace, GanseqTrace.REVERSE);

			controller.setRootController(this);
			controller.init();
			stage.setScene(new Scene(root1));
			//stage.setAlwaysOnTop(true);
			stage.initOwner(primaryStage);
			stage.setTitle("Trim sequences");
			stage.show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
	}


	/*
	 * 	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Trim.fxml"));
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
	 */


	/**
	 * Loads the image of trimmed forward trace file
	 * @param trace : trimmed forward trace file
	 */
	public void confirmFwdTrace(GanseqTrace trimmedTrace) {

		trimmedFwdTrace[0] = trimmedTrace;

		try {

			BufferedImage awtImage = trimmedFwdTrace[0].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			//fwdRuler.setImage(trimmedFwdTrace[0].getRulerImage());

			String fileName = fwdTraceFile[0].getName();

			fwdTraceFileLabel.setText(fileName);
			fwdLoaded[0] = true;
		}
		catch(Exception ex) {
			popUp("Error in loading forward trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {

		}
		resetParameters();
	}


	public void handleReset() {
		Vector<NTMSpecies> empty = new Vector<NTMSpecies>();
		handleRemoveFwd();
		handleRemoveRev();

		csvContents = "";
		speciesTable.setItems(FXCollections.observableArrayList(empty));
		s16Table.setItems(FXCollections.observableArrayList(empty));
		rpoTable.setItems(FXCollections.observableArrayList(empty));
		tufTable.setItems(FXCollections.observableArrayList(empty));
		finalTable.setItems(FXCollections.observableArrayList(empty));
		s16Loaded = false;
		rpoLoaded = false;
		tufLoaded = false;
	}


	/**
	 * Remove forward trace file
	 */
	public void handleRemoveFwd() {
		resetParameters();
		fwdTraceFileLabel.setText("");
		fwdPane.setContent(new Label(""));
		fwdTraceFile[0] = null;
		trimmedFwdTrace[0] = null;
		fwdLoaded[0] = false;
	}


	/**
	 * Loads the image of trimmed reverse trace file
	 * @param trace : trimmed reverse trace file
	 */
	public void confirmRevTrace(GanseqTrace trimmedTrace) {
		trimmedRevTrace[0] = trimmedTrace;

		try {
			BufferedImage awtImage = trimmedRevTrace[0].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			revPane.setContent(imageView);

			//revRuler.setImage(trimmedRevTrace[0].getRulerImage());
			String fileName = revTraceFile[0].getName();
			revTraceFileLabel.setText(fileName);
			revLoaded[0] = true;

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

		revTraceFile[0] = null;
		trimmedRevTrace[0] = null;
		revLoaded[0] = false;
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


	public void handleCSV() {
		Stage dialog = new Stage(StageStyle.DECORATED);
		dialog.initOwner(primaryStage);
		dialog.setTitle("CSV");
		Parent parent;
		try {
			parent = FXMLLoader.load(getClass().getResource("csv.fxml"));
			TextArea ta_csv = (TextArea)parent.lookup("#ta_csv");


			ta_csv.setText(csvContents);
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



	private void makeCsvContents() {
		csvContents = "";
		for(NTMSpecies ntm : speciesList[0]) {
			if(ntm.getScore()>=98) {
				//csvContents += ntm.getQlen() + "\t\t" + ntm.getScoreProperty() + "\t" + ntm.getAccession() + "\t" + ntm.getSpeciesName() + "\n";
				csvContents += ntm.getQlen() + "\t" + ntm.getScoreProperty() + "\t" + ntm.getAccession() + "\t" + ntm.getSpeciesName() + "\n";
			}
		}

	}


	private void doAlignment(int selectedSpecies) throws Exception{
		resetParameters();
		Formatter.init();
		ReferenceSeq refFile = speciesList[0].get(selectedSpecies).getRefSeq();
		//When only fwd trace is given as input

		MMAlignment mma = new MMAlignment();
		AlignedPair fwdAp = null;
		AlignedPair revAp = null;

		if(fwdLoaded[0] == true) {
			fwdAp = mma.localAlignment(refFile.getRefString(), trimmedFwdTrace[0].getSequence());
		}

		if(revLoaded[0] == true) {
			revAp = mma.localAlignment(refFile.getRefString(), trimmedRevTrace[0].getSequence());
		}


		if(fwdLoaded[0] == true && revLoaded[0] == false) {
			alignedPoints[0] = Formatter.format2(fwdAp, refFile, trimmedFwdTrace[0], 1);
		}

		//When only rev trace is given as input
		else if(fwdLoaded[0] == false && revLoaded[0] == true) {
			alignedPoints[0] = Formatter.format2(revAp, refFile, trimmedRevTrace[0], -1);
		}

		//When both of fwd trace and rev trace are given
		else  if(fwdLoaded[0] == true && revLoaded[0] == true) {

			alignedPoints[0] = Formatter.format3(fwdAp, revAp, refFile, trimmedFwdTrace[0], trimmedRevTrace[0]);
		}
	}

	private Vector<NTMSpecies> getFinalList() {
		Vector<NTMSpecies> s16List = new Vector<NTMSpecies>(s16Table.getItems());
		Vector<NTMSpecies> rpoList = new Vector<NTMSpecies>(rpoTable.getItems());
		Vector<NTMSpecies> tufList = new Vector<NTMSpecies>(tufTable.getItems());

		Vector<NTMSpecies> s16_100List = new Vector<NTMSpecies>();
		Vector<NTMSpecies> retList = new Vector<NTMSpecies>();

		if(s16Loaded) {
			for(NTMSpecies ntm : s16List) {
				if(ntm.getScore() == 100) 
					s16_100List.add(ntm);
				else 
					break;
			}

			//100 match 하는 것들 있으면 이것만 대상으로 함.
			String strScore = "";
			if(!s16_100List.isEmpty()) { 
				retList = s16_100List;
				strScore = "Exact match";
			}
			else {
				retList = s16List;
				strScore = "most closely";
			}

			if(retList.size() > 1 && rpoLoaded) {
				retList.retainAll(rpoList);
				if(retList.size() > 1 && tufLoaded)
					retList.retainAll(tufList);
			}

			Vector<NTMSpecies> tempList = new Vector<NTMSpecies>();
			boolean specificSeq = false;
			for(NTMSpecies ntm : retList) {
				if(ntm.getSpeciesName().equals("Mycobacterium_intracellulare") || ntm.getSpeciesName().equals("Mycobacterium_chimaera"))
					specificSeq = true;
				NTMSpecies temp = new NTMSpecies(ntm.getSpeciesName(), strScore);
				tempList.add(temp);
			}
			retList = tempList;

			icSeqLabel.setText("");
			chimaeraSeqLabel.setText("");

			if(specificSeq && targetRegion.equals(s16)) {
				boolean containsIcSeq = false;
				boolean containsChSeq = false;
				if(fwdLoaded[0]) {
					if(trimmedFwdTrace[0].getSequence().contains(icSeq)) 
						containsIcSeq = true;
					if(trimmedFwdTrace[0].getSequence().contains(chSeq)) 
						containsChSeq = true;
				}
				if(revLoaded[0]) {
					if(trimmedRevTrace[0].getSequence().contains(icSeq)) 
						containsIcSeq = true;
					if(trimmedRevTrace[0].getSequence().contains(chSeq)) 
						containsChSeq = true;
				}
				if(containsIcSeq)
					icSeqLabel.setText("M. intracellularae specific sequence : O");
				else
					icSeqLabel.setText("M. intracellularae specific sequence : X");
				if(containsChSeq)
					chimaeraSeqLabel.setText("M. chimaera specific sequence : O");
				else
					chimaeraSeqLabel.setText("M. chimaera specific sequence : X");


			}

		}
		return retList;
	}

	/**
	 * Performs alignment, Detects variants, Shows results
	 */
	public void handleRun() {
		context = 0;



		if(fwdLoaded[0] == false && revLoaded[0] == false) {  
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


		int inputLength = 0;
		if(fwdLoaded[0] && !revLoaded[0]) 
			inputLength = trimmedFwdTrace[0].getSequenceLength();
		else if(!fwdLoaded[0] && revLoaded[0]) 
			inputLength = trimmedRevTrace[0].getSequenceLength();

		//fwd, rev 같이있을때는 fwd, rev 두개를 align 시켜서 나오는 길이를 inputLength로. 
		else if (fwdLoaded[0] && revLoaded[0]) {	 
			MMAlignment mma = new MMAlignment();
			AlignedPair ap = null;
			ap = mma.localAlignment(trimmedFwdTrace[0].getSequence(), trimmedRevTrace[0].getSequence());
			inputLength = Integer.max(ap.getStart1(), ap.getStart2()) 
					+ Integer.max(trimmedFwdTrace[0].getSequenceLength()-ap.getStart1(),  trimmedRevTrace[0].getSequenceLength()-ap.getStart2());
		}
		if(inputLength == 0) return;


		Vector<NTMSpecies> removeList = new Vector<NTMSpecies>();
		for(int i=0;i<speciesList[0].size();i++) {

			NTMSpecies thisSpecies = speciesList[0].get(i);

			try {
				doAlignment(i);
			}
			catch(Exception ex) {
				//sSystem.out.println("alignment failure : " + thisSpecies.getSpeciesName());
				removeList.add(thisSpecies);
				continue;
			}

			//너무 짧게 align된 것들은 버림.
			double alignedPortion = alignedPoints[0].size() / (double)inputLength;
			if(alignedPortion < 0.5) {
				removeList.add(thisSpecies);
				continue;
			}

			//score 계산
			int i_score = 0;
			double d_score = 0;
			for(int j=0;j<alignedPoints[0].size();j++) {
				AlignedPoint ap = alignedPoints[0].get(j);
				if(ap.getDiscrepency()!='*') 
					i_score++;
			}

			d_score = (double)i_score / (double)alignedPoints[0].size();
			d_score*=100;

			//score 너무 낮은것들 버림
			if(d_score < 90) {
				removeList.add(thisSpecies);
				continue;
			}

			//System.out.println("score : " + d_score);
			thisSpecies.setScore(d_score);
			thisSpecies.setQlen(inputLength);
			thisSpecies.setAlen(alignedPoints[0].size());
		}

		speciesList[0].removeAll(removeList);	//align 안된 것들, score 낮은것들 remove

		if(speciesList[0].size()>0) 
			Collections.sort(speciesList[0]);


		if(speciesList[0].size()==0) popUp("No matching species!");
		else {

			// 점수 제일 높은걸로 align
			try {
				doAlignment(0);
			}
			catch(Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
			}

			printAlignedResult();

			speciesTable.setEditable(true);
			TableColumn tcSpecies = speciesTable.getColumns().get(0);
			TableColumn tcAccession = speciesTable.getColumns().get(1);
			TableColumn tcQlen = speciesTable.getColumns().get(2);
			TableColumn tcAlen = speciesTable.getColumns().get(3);
			TableColumn tcScore = speciesTable.getColumns().get(4);
			TableColumn tcRgm = speciesTable.getColumns().get(5);

			tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
			tcAccession.setCellValueFactory(new PropertyValueFactory("accessionProperty"));
			tcQlen.setCellValueFactory(new PropertyValueFactory("qlenProperty"));
			tcAlen.setCellValueFactory(new PropertyValueFactory("alenProperty"));
			tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
			tcRgm.setCellValueFactory(new PropertyValueFactory("rgmProperty"));

			tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
			speciesTable.setItems(FXCollections.observableArrayList(speciesList[0]));

			makeCsvContents();


			Vector<NTMSpecies> selectedSpeciesList = new Vector<NTMSpecies>();
			for(NTMSpecies ntm:speciesList[0]) {
				double cutoff = 99;
				if(targetRegion.equals(rpo)) {
					if(ntm.isRgm()) 
						cutoff = 98.3;
					else cutoff = 99.3;
				}
				if(ntm.getScore() >= cutoff)
					selectedSpeciesList.add(ntm);
				else
					break;
			}

			if(targetRegion.equals(s16)) {
				s16Loaded = true;
				s16Table.setEditable(true);
				TableColumn s16_tcSpecies = s16Table.getColumns().get(0);
				TableColumn s16_tcScore = s16Table.getColumns().get(1);
				s16_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
				s16_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
				s16_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
				s16Table.setItems(FXCollections.observableArrayList(selectedSpeciesList));
			}
			else if(targetRegion.equals(rpo)) {
				rpoLoaded = true;
				rpoTable.setEditable(true);
				TableColumn rpo_tcSpecies = rpoTable.getColumns().get(0);
				TableColumn rpo_tcScore = rpoTable.getColumns().get(1);
				rpo_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
				rpo_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
				rpo_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
				rpoTable.setItems(FXCollections.observableArrayList(selectedSpeciesList));
			}
			else if(targetRegion.equals(tuf)) {
				tufLoaded = true;
				tufTable.setEditable(true);
				TableColumn tuf_tcSpecies = tufTable.getColumns().get(0);
				TableColumn tuf_tcScore = tufTable.getColumns().get(1);
				tuf_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
				tuf_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
				tuf_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
				tufTable.setItems(FXCollections.observableArrayList(selectedSpeciesList));
			}

			speciesTable.setFixedCellSize(tableRowHeight);
			s16Table.setFixedCellSize(tableRowHeight);
			rpoTable.setFixedCellSize(tableRowHeight);
			tufTable.setFixedCellSize(tableRowHeight);
			finalTable.setFixedCellSize(tableRowHeight);

			//finalTable 
			finalTable.setEditable(true);
			TableColumn final_tcSpecies = finalTable.getColumns().get(0);
			TableColumn final_tcScore = finalTable.getColumns().get(1);
			final_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
			final_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
			final_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
			finalTable.setItems(FXCollections.observableArrayList(getFinalList()));

			speciesTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if(newValue.intValue()<0 || newValue.intValue()>= speciesList[0].size()) return; 

					//Sysem.out.println("selected Index : " + newValue.intValue());
					try {
						doAlignment(newValue.intValue());
					}
					catch(Exception ex) {
						popUp(ex.getMessage());
						ex.printStackTrace();
					}
					printAlignedResult();
				}
			});
		}
	}

	/**
	 * Prints the result of alignment on the alignment pane
	 */
	private void printAlignedResult() {
		labels = new Label[3][alignedPoints[0].size()];
		gridPane = new GridPane();

		Label refTitle = new Label("Reference : ");
		refTitle.setFont(new Font("Consolas", 14));
		refTitle.setMinSize(130,15);
		refTitle.setPrefSize(130, 15);
		gridPane.add(refTitle, 0,  1);

		if(fwdLoaded[0]) {
			Label fwdTitle = new Label("Forward   : ");
			fwdTitle.setFont(new Font("Consolas", 14));
			fwdTitle.setMinSize(130,15);
			fwdTitle.setPrefSize(130, 15);
			gridPane.add(fwdTitle, 0,  2);
		}

		if(revLoaded[0]) {
			Label revTitle = new Label("Reverse   : ");
			revTitle.setFont(new Font("Consolas", 14));
			revTitle.setMinSize(130,15);
			revTitle.setPrefSize(130, 15);
			gridPane.add(revTitle, 0,  3);
		}

		for (int i=0;i<alignedPoints[0].size();i++) {
			AlignedPoint point = alignedPoints[0].get(i);

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
			if(i%10==0 && alignedPoints[0].size()-i >= 5) {
				indexLabel.setText(String.valueOf(i+1));
				GridPane.setColumnSpan(indexLabel, 10);
				indexLabel.setPrefSize(100, 10);
				indexLabel.setOnMouseClicked(new ClickEventHandler(i));
				gridPane.add(indexLabel, i+1, 0);
			}

			//Reference
			String sRefChar = Character.toString(point.getRefChar());
			if(!point.isCoding()) sRefChar = sRefChar.toLowerCase();
			refLabel.setText(sRefChar);
			refLabel.setPrefSize(10, 10);
			refLabel.setOnMouseClicked(new ClickEventHandler(i));


			gridPane.add(refLabel,  i+1, 1);
			labels[0][i] = refLabel;

			//Forward
			if(fwdLoaded[0]) {
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
				fwdLabel.setOnMouseClicked(new ClickEventHandler(i));
				gridPane.add(fwdLabel,  i+1, 2);
				labels[1][i] = fwdLabel;
			}

			//Reverse
			if(revLoaded[0]) {
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
				revLabel.setOnMouseClicked(new ClickEventHandler(i));
				gridPane.add(revLabel,  i+1, 3);
				labels[2][i] = revLabel;
			}

			//Discrepency
			discrepencyLabel.setText(Character.toString(point.getDiscrepency()));
			discrepencyLabel.setPrefSize(10, 10);
			discrepencyLabel.setOnMouseClicked(new ClickEventHandler(i));
			gridPane.add(discrepencyLabel,  i+1, 4);
		}

		alignmentPane.setContent(gridPane);
		alignmentPerformed[0] = true;

		if(fwdLoaded[0]) {
			// 새로운 좌표로 update (fwdPane, revPane)
			java.awt.image.BufferedImage awtImage = trimmedFwdTrace[0].getShadedImage(0,0,0);
			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			// 시작점에 화면 align
			adjustFwdPane(Formatter.fwdTraceAlignStartPoint);
		}
		if(revLoaded[0]) {
			java.awt.image.BufferedImage awtImage2 = trimmedRevTrace[0].getShadedImage(0,0,0);
			javafx.scene.image.Image fxImage2 = SwingFXUtils.toFXImage(awtImage2, null);
			ImageView imageView2 = new ImageView(fxImage2);
			revPane.setContent(imageView2);
			adjustRevPane(Formatter.revTraceAlignStartPoint);
		}
	}

	/**
	 * Focuses the designated point on the forward trace pane
	 * @param fwdTraceIndex : the point to be focused
	 */
	private void adjustFwdPane(int fwdTraceIndex) {
		if(Formatter.fwdNewLength <= paneWidth) return;
		if(fwdTraceIndex > trimmedFwdTrace[0].getBaseCalls().length)
			fwdTraceIndex = trimmedFwdTrace[0].getBaseCalls().length;
		double coordinate = Formatter.fwdStartOffset + trimmedFwdTrace[0].getBaseCalls()[fwdTraceIndex-1]*2;
		double hValue = (coordinate - paneWidth/2) / (Formatter.fwdNewLength - paneWidth);
		fwdPane.setHvalue(hValue);
	}

	/**
	 * Focuses the designated point on the reverse trace pane
	 * @param revTraceIndex : the point to be focused
	 */
	private void adjustRevPane(int revTraceIndex) {
		if(Formatter.revNewLength <= paneWidth) return;
		if(revTraceIndex > trimmedRevTrace[0].getBaseCalls().length)
			revTraceIndex = trimmedRevTrace[0].getBaseCalls().length;

		double coordinate = Formatter.revStartOffset + trimmedRevTrace[0].getBaseCalls()[revTraceIndex-1]*2;

		double hValue = (coordinate - paneWidth/2) / (Formatter.revNewLength - paneWidth);
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
		if(length<=paneWidth) return;
		double coordinate = labels[0][index].getLayoutX();
		double hValue = (coordinate - paneWidth/2) / (length - paneWidth);
		alignmentPane.setHvalue(hValue);

	}

	/**
	 * Focuses on the designated points (Alignment pane, forward trace pane, reverse trace pane)
	 * @param selectedAlignmentPos : position to be focused on the alignment pane
	 * @param selectedFwdPos : position to be focused on the forward trace pane
	 * @param selectedRevPos : position to be focused on the reverse trace pane
	 * @param fwdChar : If it is gap, not focused
	 * @param revChar : If it is gap, not focused
	 */
	//public void focus(int selectedAlignmentPos, int selectedFwdPos, int selectedRevPos, char fwdChar, char revChar) {

	public void focus(int selectedAlignmentPos) {

		this.selectedAlignmentPos[0] = selectedAlignmentPos;
		AlignedPoint ap = alignedPoints[0].get(selectedAlignmentPos);
		char fwdChar = Formatter.gapChar;
		char revChar = Formatter.gapChar;
		int selectedFwdPos = 0;
		int selectedRevPos = 0;

		if(fwdLoaded[0]) {
			selectedFwdPos = ap.getFwdTraceIndex();
			fwdChar = ap.getFwdChar();
		}
		if(revLoaded[0]) {
			selectedRevPos = ap.getRevTraceIndex();
			revChar = ap.getRevChar();
		}


		//selectedAlignmentPos : 이것만 0부터 시작하는 index
		//selectedFwdPos, selectedRevPos : 1부터 시작하는 index

		//boolean fwdGap = (fwdChar == Formatter.gapChar); 
		//boolean revGap = (revChar == Formatter.gapChar);

		for(int i=0; i<alignedPoints[0].size();i++) {
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
		if(fwdLoaded[0]) {
			for(int i=0; i<alignedPoints[0].size();i++) {
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
			if(fwdChar == Formatter.gapChar) {
				awtImage = trimmedFwdTrace[0].getShadedImage(3,tempFwdPos-1,tempFwdPos-1);

			}
			else awtImage = trimmedFwdTrace[0].getShadedImage(1, tempFwdPos-1, tempFwdPos-1);

			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);

			adjustFwdPane(selectedFwdPos);
		}
		if(revLoaded[0]) {
			for(int i=0; i<alignedPoints[0].size();i++) {
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


			if(revChar == Formatter.gapChar) {
				awtImage2 = trimmedRevTrace[0].getShadedImage(3,tempRevPos-1,tempRevPos-1);
			}

			else 
				awtImage2 = trimmedRevTrace[0].getShadedImage(1, tempRevPos-1, tempRevPos-1);
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
		private int selectedAlignmentPos = 0;
		char fwdChar, revChar;
		public ClickEventHandler(int selectedAlignmentPos) {
			super();
			this.selectedAlignmentPos = selectedAlignmentPos;
		}

		@Override
		public void handle(MouseEvent t) {
			focus(selectedAlignmentPos);
		}
	}



}



