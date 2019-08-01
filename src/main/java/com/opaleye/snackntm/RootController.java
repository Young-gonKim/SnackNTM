package com.opaleye.snackntm;

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

import com.opaleye.snackntm.mmalignment.AlignedPair;
import com.opaleye.snackntm.mmalignment.MMAlignment;
import com.opaleye.snackntm.reference.ReferenceSeq;
import com.opaleye.snackntm.settings.SettingsController;
import com.opaleye.snackntm.tools.TooltipDelay;

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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
import javafx.stage.Modality;
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

	private Vector<Sample> sampleList = new Vector<Sample>();

	/**
	selectedSample : sampleListView 선택 바뀔때 (+ 처음 읽을때 : iteration 한 후 0으로 원상복귀)
	context : 밑에 targetTable 선택 바뀔때.  (+처음 읽을때 : localContext따로 씀. 이것도 iteration 시키고 원상복귀?)
	이거 두개로 panel 내용 정해지는것.

	<개요>
	- actualRun에서 speciesList, selectedSpeciesList를 만들고
	- fillResults에서 화면에 뿌림.

	 */

	private int selectedSample = 0;
	private int context = 0;


	private Vector<NTMSpecies>[] globalSpeciesList = new Vector[3];	//이건 공통으로 사요


	//error 줄이기 위해 context는 Global 개념으로 사용.
	//0: 16S rRNA, 1: rpoB, 2: tuf

	//constants
	private static final double paneWidth = 907; 
	private static final String s16 = "16sRNA";
	private static final String rpo = "rpo";
	private static final String tuf = "tuf";
	public static final int defaultGOP = 30;
	public static final String version = "1.1";
	private static final double tableRowHeight = 25.0;
	private static String icSeq = null;
	private static String chSeq = null;
	public static TreeSet<String> rgmSet = new TreeSet<String>(); 
	public static TreeSet<String> sgmSet = new TreeSet<String>();

	private static final String settingsFileName = "settings/settings.properties";
	private int fontSize = 0;
	private int sampleIdLength = 0; 
	private String keyword_16sF, keyword_16sR, keyword_rpoF, keyword_rpoR, keyword_tufF, keyword_tufR;
	//private TreeSet<String> keywordSet_F[0], keywordSet_R[0], keywordSet_F[1], keywordSet_R[1], keywordSet_F[2], keywordSet_R[2];
	private TreeSet<String> keywordSet_F[] = new TreeSet[3];
	private TreeSet<String> keywordSet_R[] = new TreeSet[3];

	@FXML private ScrollPane  fwdPane, revPane, alignmentPane, newAlignmentPane;
	@FXML private GridPane headerPane;
	@FXML private Label fwdTraceFileLabel, revTraceFileLabel, icSeqLabel, chimaeraSeqLabel;
	@FXML private Button removeRef, removeFwd, removeRev, removeVariant;
	@FXML private Button btnEditBase;
	@FXML private Button btn_settings;
	@FXML private Button fwdZoomInButton, fwdZoomOutButton, revZoomInButton, revZoomOutButton; 

	@FXML private TableView<NTMSpecies> speciesTable, s16Table, rpoTable, tufTable, finalTable;
	@FXML private ListView<String> sampleListView;

	ToggleGroup toggleGroup = new ToggleGroup();

	@FXML private RadioButton s16Radio, rpoRadio, tufRadio;
	private String lastVisitedDir="f:\\GoogleDrive\\SnackNTM";
	private Stage primaryStage;
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	private GridPane gridPane = null;
	private Label[][] labels = null;
	private Label[] headerLabel = new Label[4];

	private int headerHeight = 0;

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
	//context-specific variables

	/*
	private Vector<NTMSpecies>[] speciesList = new Vector[3];
	public static Vector<AlignedPoint>[] alignedPoints = new Vector[3];
	private File[] fwdTraceFile = new File[3], revTraceFile = new File[3];
	private GanseqTrace[] trimmedFwdTrace = new GanseqTrace[3], trimmedRevTrace = new GanseqTrace[3];
	private boolean fwdLoaded[] = {false, false, false}, revLoaded[] = {false, false, false};
	private String[] fwdTraceFileName = new String[3];
	private String[] revTraceFileName = new String[3];
	//edit base 용
	private int[] selectedAlignmentPos = {-1, -1, -1};
	 */


	private void checkVersion() {
		String homepage = "", email = "", copyright = "";
		String comment = "SnackNTM Ver " + version;
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

			for(int i=0;i<3;i++) {
				keywordSet_F[i] = new TreeSet<String>();
				keywordSet_R[i] = new TreeSet<String>();
			}

			String[] keywordList;
			keyword_16sF = props.getProperty("keyword_16sF");
			keywordList = keyword_16sF.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_F[0].add(keyword);
			}

			keyword_16sR = props.getProperty("keyword_16sR");
			keywordList = keyword_16sR.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_R[0].add(keyword);
			}

			keyword_rpoF = props.getProperty("keyword_rpoF");
			keywordList = keyword_rpoF.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_F[1].add(keyword);
			}

			keyword_rpoR = props.getProperty("keyword_rpoR");
			keywordList = keyword_rpoR.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_R[1].add(keyword);
			}

			keyword_tufF = props.getProperty("keyword_tufF");
			keywordList = keyword_tufF.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_F[2].add(keyword);
			}

			keyword_tufR = props.getProperty("keyword_tufR");
			keywordList = keyword_tufR.split(",");
			for(String keyword:keywordList) {
				keyword = keyword.replace(" ",  "");
				keywordSet_R[2].add(keyword);
			}

			sampleIdLength = Integer.parseInt(props.getProperty("sample_id_length"));
			headerHeight = Integer.parseInt(props.getProperty("header_height"));

		}
		catch(Exception e) {
			e.printStackTrace();
			textPopUp("Error in reading configuration file. (settings/settings.properties) Recover configuration file or reinstall the program.");
			System.exit(0);
		}

	}


	private void fillResults() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.alignmentPerformed[context] == false) {
			printUnAlignedData();
		}
		else {
			printAlignedResult();

			speciesTable.setItems(FXCollections.observableArrayList(sample.speciesList[context]));
			finalTable.setItems(FXCollections.observableArrayList(sample.finalList));

			if(sample.alignmentPerformed[0])  
				s16Table.setItems(FXCollections.observableArrayList(sample.selectedSpeciesList[0]));
			else 
				s16Table.setItems(FXCollections.observableArrayList(new Vector<NTMSpecies>()));

			if(sample.alignmentPerformed[1]) 
				rpoTable.setItems(FXCollections.observableArrayList(sample.selectedSpeciesList[1]));
			else 
				rpoTable.setItems(FXCollections.observableArrayList(new Vector<NTMSpecies>()));

			if(sample.alignmentPerformed[2]) 
				tufTable.setItems(FXCollections.observableArrayList(sample.selectedSpeciesList[2]));
			else 
				tufTable.setItems(FXCollections.observableArrayList(new Vector<NTMSpecies>()));
		}

		//radio Button 색깔
		if(sample.fwdLoaded[0] || sample.revLoaded[0])  
			s16Radio.setTextFill(Color.BLUE);
		else 
			s16Radio.setTextFill(Color.BLACK);

		if(sample.fwdLoaded[1] || sample.revLoaded[1]) 
			rpoRadio.setTextFill(Color.BLUE);
		else 
			rpoRadio.setTextFill(Color.BLACK);

		if(sample.fwdLoaded[2] || sample.revLoaded[2]) 
			tufRadio.setTextFill(Color.BLUE);
		else 
			tufRadio.setTextFill(Color.BLACK);


	}

	/**
	 * Initializes required settings
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		checkVersion();

		File tempFile = new File(lastVisitedDir);
		if(!tempFile.exists())
			lastVisitedDir=".";

		gapOpenPenalty = defaultGOP;

		s16Radio.setToggleGroup(toggleGroup);
		s16Radio.setSelected(true);
		rpoRadio.setToggleGroup(toggleGroup);
		tufRadio.setToggleGroup(toggleGroup);

		s16Radio.setUserData(s16);
		rpoRadio.setUserData(rpo);
		tufRadio.setUserData(tuf);

		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle old_toggle, Toggle new_toggle) {
				if (toggleGroup.getSelectedToggle() != null) {
					String t1 = (String)toggleGroup.getSelectedToggle().getUserData();
					if(t1.equals(s16)) {
						context = 0;
					}
					else if (t1.equals(rpo)) { 
						context = 1;
					}
					else if (t1.equals(tuf)) { 
						context = 2;
					}

					System.out.println("context switched to " + context);

					if(sampleList.size()>0) {	//alignment 누르기전에 radiobutton 클릭하는 경우 방지위해 if문.
						fillResults();
					}
				}                
			}
		});

		sampleListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				selectedSample = newValue.intValue();
				context = 0;
				s16Radio.setSelected(true);

				fillResults();
			}
		});


		for(int i=0;i<3;i++) {
			try {
				makeGlobalSpeciesList(i);
			}
			catch(Exception ex) {
				popUp(ex.getMessage());
				ex.printStackTrace();
			}
		}
		initTableViews();
		makeEmptyHeader();

		Tooltip zoomInTooltip = new Tooltip("Zoom In");
		Tooltip zoomOutTooltip = new Tooltip("Zoom Out");
		TooltipDelay.activateTooltipInstantly(zoomInTooltip);
		TooltipDelay.activateTooltipInstantly(zoomOutTooltip);

		fwdZoomInButton.setTooltip(zoomInTooltip);
		fwdZoomOutButton.setTooltip(zoomOutTooltip);
		revZoomInButton.setTooltip(zoomInTooltip);
		revZoomOutButton.setTooltip(zoomOutTooltip);
	}

	private void printUnAlignedData() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.fwdLoaded[context]) {
			BufferedImage awtImage = sample.trimmedFwdTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			fwdTraceFileLabel.setText(sample.fwdTraceFileName[context]);
		}
		else if(sample.fwdTraceFileName[context] != null) {
			fwdTraceFileLabel.setText(sample.fwdTraceFileName[context]);
			fwdPane.setContent(new Label("Poor Quality Trace File"));
		}
		else {
			fwdTraceFileLabel.setText("No file exists");
			fwdPane.setContent(new Label("No file exists"));
		}

		if(sample.revLoaded[context]) {
			BufferedImage awtImage = sample.trimmedRevTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			revPane.setContent(imageView);
			revTraceFileLabel.setText(sample.revTraceFileName[context]);
		}
		else if(sample.revTraceFileName[context] != null) {
			revTraceFileLabel.setText(sample.revTraceFileName[context]);
			revPane.setContent(new Label("Poor Quality Trace File"));
		}
		else {
			revTraceFileLabel.setText("No file exists");
			revPane.setContent(new Label("No file exists"));
		}
	}

	private void initTableViews() {
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

		speciesTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(newValue.intValue()<0) return;
				System.out.println("selected Index : " + newValue.intValue());
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

		s16Table.setEditable(true);
		TableColumn s16_tcSpecies = s16Table.getColumns().get(0);
		TableColumn s16_tcScore = s16Table.getColumns().get(1);
		s16_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
		s16_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
		s16_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());

		rpoTable.setEditable(true);
		TableColumn rpo_tcSpecies = rpoTable.getColumns().get(0);
		TableColumn rpo_tcScore = rpoTable.getColumns().get(1);
		rpo_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
		rpo_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
		rpo_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());

		tufTable.setEditable(true);
		TableColumn tuf_tcSpecies = tufTable.getColumns().get(0);
		TableColumn tuf_tcScore = tufTable.getColumns().get(1);
		tuf_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
		tuf_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
		tuf_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());

		speciesTable.setFixedCellSize(tableRowHeight);
		s16Table.setFixedCellSize(tableRowHeight);
		rpoTable.setFixedCellSize(tableRowHeight);
		tufTable.setFixedCellSize(tableRowHeight);
		finalTable.setFixedCellSize(tableRowHeight);

		finalTable.setEditable(true);
		TableColumn final_tcSpecies = finalTable.getColumns().get(0);
		TableColumn final_tcScore = finalTable.getColumns().get(1);
		final_tcSpecies.setCellValueFactory(new PropertyValueFactory("speciesNameProperty"));
		final_tcScore.setCellValueFactory(new PropertyValueFactory("scoreProperty"));
		final_tcSpecies.setCellFactory(TextFieldTableCell.<NTMSpecies>forTableColumn());
	}

	public void setProperties(int gapOpenPenalty) {
		RootController.gapOpenPenalty = gapOpenPenalty;

	}


	public void handleEditBase() {
		Sample sample = sampleList.get(selectedSample);
		if(!sample.alignmentPerformed[context]) {
			return;
		}
		if(sample.selectedAlignmentPos[context] == -1) {
			return;
		}
		AlignedPoint ap = sample.alignedPoints[context].get(sample.selectedAlignmentPos[context]);

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
			stage.initModality(Modality.WINDOW_MODAL);	

			stage.initOwner(primaryStage);
			stage.show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	public void updateBase(char newFwdChar, char newRevChar) {
		Sample sample = sampleList.get(selectedSample);
		AlignedPoint ap = sample.alignedPoints[context].get(sample.selectedAlignmentPos[context]);

		if(sample.fwdLoaded[context]) {
			sample.trimmedFwdTrace[context].editBase(ap.getFwdTraceIndex(), ap.getFwdChar(), newFwdChar);
		}
		if(sample.revLoaded[context]) {
			sample.trimmedRevTrace[context].editBase(ap.getRevTraceIndex(), ap.getRevChar(), newRevChar);
		}

		//새로 alignment 실행 && 원래 보여주고 있던 곳 보여주기
		NTMSpecies selectedSpecies = speciesTable.getSelectionModel().getSelectedItem();

		//speciesTable에서 클릭하기 전에는 맨 위에꺼에 맞추어서 printAlignedResult 되어있으므로.
		if(selectedSpecies == null) 
			selectedSpecies = sample.speciesList[context].get(0);

		int oldAlignmentPos = sample.selectedAlignmentPos[context];
		String selectedSpeciesName = selectedSpecies.getSpeciesName();
		//System.out.println("selected species : " + selectedSpeciesName);

		actualRun();
		fillResults();

		for(int i=0;i<sample.speciesList[context].size();i++) {
			NTMSpecies ntm = sample.speciesList[context].get(i);
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



	private void makeGlobalSpeciesList(int target) throws Exception {
		globalSpeciesList[target] = new Vector<NTMSpecies>();

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
		if(target == 0)
			file = new File("reference/ref16s.fasta");
		else if(target == 1)
			file = new File("reference/refrpob.fasta");
		else if(target == 2)
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
				globalSpeciesList[target].add(tempSpecies);
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


		TreeSet<String> forwardTarget = null, reverseTarget = null;

		TreeSet<Sample> tempList = new TreeSet<Sample>();
		TreeSet<String> idList = new TreeSet<String>();

		//sampleListView.setItems
		for(File file:fileList) {
			String fileName = file.getName();
			if(fileName.length()<9) continue;

			Sample sample = new Sample(fileName.substring(0,  sampleIdLength));
			tempList.add(sample);
		}
		sampleList = new Vector<Sample>(tempList);
		for(Sample sample:sampleList) {
			//System.out.println(sample.sampleId);
			idList.add(sample.sampleId);
		}

		sampleListView.setItems(FXCollections.observableArrayList(idList));

		for(selectedSample=0; selectedSample<sampleList.size(); selectedSample++) {
			Sample sample = sampleList.get(selectedSample);
			String sampleId = sample.sampleId;
			for(File file:fileList) {
				String fileName = file.getName();
				if(fileName.contains(sampleId)) {
					for(context=0;context<3;context++) {
						forwardTarget = keywordSet_F[context];
						reverseTarget = keywordSet_R[context];

						for(String target:forwardTarget) {
							if(fileName.contains(target)) {
								sample.fwdTraceFile[context] = file;
								try {
									GanseqTrace tempTrace = new GanseqTrace(sample.fwdTraceFile[context]);
									if(tempTrace.getSequenceLength()<30) {
										popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
										return;
									}
									int startTrimPosition = tempTrace.getFrontTrimPosition();
									int endTrimPosition = tempTrace.getTailTrimPosition();
									if(startTrimPosition >= endTrimPosition) {
										//popUp("Forward trace file cannot be used due to poor quality");
										poorFwdTrace(tempTrace);
									}
									else {
										tempTrace.makeTrimmedTrace(startTrimPosition, endTrimPosition);
										confirmFwdTrace(tempTrace, false);
									}

								}
								catch (Exception ex) {
									ex.printStackTrace();
									popUp("Error in loading trace files\n" + ex.getMessage());
									return;
								}
								break;
							}
						}

						for(String target:reverseTarget) {
							if(fileName.contains(target)) {
								sample.revTraceFile[context] = file;
								try {
									GanseqTrace tempTrace = new GanseqTrace(sample.revTraceFile[context]);
									if(tempTrace.getSequenceLength()<30) {
										popUp("Invalid trace file: too short sequence length(<30bp) or too poor quality of sequence");
										return;
									}
									int startTrimPosition = tempTrace.getFrontTrimPosition();
									int endTrimPosition = tempTrace.getTailTrimPosition();

									if(startTrimPosition >= endTrimPosition) {
										//popUp("Reverse trace file cannot be used due to poor quality");
										poorRevTrace(tempTrace);
									}
									else {
										tempTrace.makeTrimmedTrace(startTrimPosition, endTrimPosition);
										//make complement
										tempTrace.makeComplement();
										confirmRevTrace(tempTrace, false);
									}
								}
								catch (Exception ex) {
									ex.printStackTrace();
									popUp("Error in loading trace files\n" + ex.getMessage());
									return;
								}
								break;
							}
						}
					}
				}
			}
		}

		//원상복귀. 읽은 다음 맨 위 가리키게.
		selectedSample = 0;
		context = 0;

		fillResults();
	}

	public void handleFwdEditTrimming() {
		GanseqTrace tempTrace = null;
		try {
			tempTrace = new GanseqTrace(sampleList.get(selectedSample).fwdTraceFile[context]);
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
			stage.initModality(Modality.WINDOW_MODAL);
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
			tempTrace = new GanseqTrace(sampleList.get(selectedSample).revTraceFile[context]);
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
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setTitle("Trim sequences");
			stage.show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			popUp(ex.getMessage());
			return;
		}
	}


	/**
	 * Loads the image of trimmed forward trace file
	 * @param trace : trimmed forward trace file
	 */
	public void confirmFwdTrace(GanseqTrace trimmedTrace, boolean resetImage) {
		Sample sample = sampleList.get(selectedSample);
		sample.trimmedFwdTrace[context] = trimmedTrace;

		try {

			if(resetImage) {
				BufferedImage awtImage = sample.trimmedFwdTrace[context].getDefaultImage();
				Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
				ImageView imageView = new ImageView(fxImage);
				fwdPane.setContent(imageView);
			}

			//fwdRuler.setImage(trimmedFwdTrace[context].getRulerImage());

			String fileName = sample.fwdTraceFile[context].getName();
			sample.fwdTraceFileName[context] = fileName;
			//fwdTraceFileLabel.setText(fileName);
			sample.fwdLoaded[context] = true;
		}
		catch(Exception ex) {
			popUp("Error in loading forward trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {

		}
		resetParameters();
	}


	public void poorFwdTrace(GanseqTrace poorTrace) {

		try {
			//fwdPane.setContent(new Label("Poor Quality Trace File"));
			String fileName = sampleList.get(selectedSample).fwdTraceFile[context].getName();
			sampleList.get(selectedSample).fwdTraceFileName[context] = fileName;
			//fwdTraceFileLabel.setText(fileName);
			sampleList.get(selectedSample).fwdLoaded[context] = false;
		}
		catch(Exception ex) {
			popUp("Error in loading forward trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {

		}
		resetParameters();
	}



	private void resetParameters() {
		Sample sample = sampleList.get(selectedSample);
		sample.alignmentPerformed[context] = false;
		sample.alignedPoints[context] = null;
		sample.selectedAlignmentPos[context] = -1;
		gridPane = null;
		labels = null;
		alignmentPane.setContent(new Label(""));
	}


	public void handleReset() {

		/*
		speciesList = new Vector[3];
		alignmentPerformed = false;
		alignedPoints = new Vector[3];
		fwdTraceFile = new File[3]; 
		revTraceFile = new File[3];
		trimmedFwdTrace = new GanseqTrace[3];
		trimmedRevTrace = new GanseqTrace[3];
		fwdLoaded = new boolean[3];
		revLoaded = new boolean[3];
		fwdTraceFileName = new String[3];
		revTraceFileName = new String[3];

		cb_targetRegion.setValue(s16);

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
		 */
	}


	/**
	 * Remove forward trace file
	 */
	public void handleRemoveFwd() {
		/*
		resetParameters();
		fwdTraceFileLabel.setText("");
		fwdPane.setContent(new Label(""));
		fwdTraceFile[context] = null;
		trimmedFwdTrace[context] = null;
		fwdLoaded[context] = false;
		 */
	}


	/**
	 * Loads the image of trimmed reverse trace file
	 * @param trace : trimmed reverse trace file
	 */
	public void confirmRevTrace(GanseqTrace trimmedTrace, boolean resetImage) {
		Sample sample = sampleList.get(selectedSample);
		sample.trimmedRevTrace[context] = trimmedTrace;

		try {
			if(resetImage) {
				BufferedImage awtImage = sample.trimmedRevTrace[context].getDefaultImage();
				Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
				ImageView imageView = new ImageView(fxImage);
				revPane.setContent(imageView);
			}

			//revRuler.setImage(trimmedRevTrace[context].getRulerImage());
			String fileName = sample.revTraceFile[context].getName();
			sample.revTraceFileName[context] = fileName;
			//revTraceFileLabel.setText(fileName);
			sample.revLoaded[context] = true;

		}
		catch(Exception ex) {
			popUp("Error in loading reverse trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		resetParameters();
	}


	public void poorRevTrace(GanseqTrace poorTrace) {
		try {
			//revPane.setContent(new Label("Poor Quality Trace File"));
			String fileName = sampleList.get(selectedSample).revTraceFile[context].getName();
			sampleList.get(selectedSample).revTraceFileName[context] = fileName;
			//revTraceFileLabel.setText(fileName);
			sampleList.get(selectedSample).revLoaded[context] = false;
		}
		catch(Exception ex) {
			popUp("Error in loading reverse trace file\n" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
		}
		resetParameters();
	}


	/**
	 * Remove reverse trace file
	 */
	public void handleRemoveRev() {
		/*
		resetParameters();
		revTraceFileLabel.setText("");
		revPane.setContent(new Label(""));

		revTraceFile[context] = null;
		trimmedRevTrace[context] = null;
		revLoaded[context] = false;
		 */
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
		Sample sample = sampleList.get(selectedSample);
		Stage dialog = new Stage(StageStyle.DECORATED);
		dialog.initOwner(primaryStage);
		dialog.setTitle("CSV");
		Parent parent;
		try {
			parent = FXMLLoader.load(getClass().getResource("csv.fxml"));
			TextArea ta_csv = (TextArea)parent.lookup("#ta_csv");


			ta_csv.setText(sample.csvContents[context]);
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
		Sample sample = sampleList.get(selectedSample);
		sample.csvContents[context] = "";
		for(NTMSpecies ntm : sample.speciesList[context]) {
			if(ntm.getScore()>=98) {
				//csvContents += ntm.getQlen() + "\t\t" + ntm.getScoreProperty() + "\t" + ntm.getAccession() + "\t" + ntm.getSpeciesName() + "\n";
				sample.csvContents[context] += ntm.getQlen() + "\t" + ntm.getScoreProperty() + "\t" + ntm.getAccession() + "\t" + ntm.getSpeciesName() + "\n";
			}
		}
	}


	private void doAlignment(int selectedSpecies) throws Exception{
		Sample sample = sampleList.get(selectedSample);
		
		sample.selectedAlignmentPos[context] = -1;
		
		Formatter formatter = new Formatter();
		formatter.init();
		ReferenceSeq refFile = sample.speciesList[context].get(selectedSpecies).getRefSeq();
		//When only fwd trace is given as input

		MMAlignment mma = new MMAlignment();
		AlignedPair fwdAp = null;
		AlignedPair revAp = null;

		if(sample.fwdLoaded[context] == true) {
			fwdAp = mma.localAlignment(refFile.getRefString(), sample.trimmedFwdTrace[context].getSequence());
		}

		if(sample.revLoaded[context] == true) {
			revAp = mma.localAlignment(refFile.getRefString(), sample.trimmedRevTrace[context].getSequence());
		}


		if(sample.fwdLoaded[context] == true && sample.revLoaded[context] == false) {
			sample.alignedPoints[context] = formatter.format2(fwdAp, refFile, sample.trimmedFwdTrace[context], 1);
		}

		//When only rev trace is given as input
		else if(sample.fwdLoaded[context] == false && sample.revLoaded[context] == true) {
			sample.alignedPoints[context] = formatter.format2(revAp, refFile, sample.trimmedRevTrace[context], -1);
		}

		//When both of fwd trace and rev trace are given
		else  if(sample.fwdLoaded[context] == true && sample.revLoaded[context] == true) {
			sample.alignedPoints[context] = formatter.format3(fwdAp, revAp, refFile, sample.trimmedFwdTrace[context], sample.trimmedRevTrace[context]);
		}
		sample.formatter[context] = formatter;
	}

	private Vector<NTMSpecies> updateFinalList() {
		Vector<NTMSpecies> s16List = new Vector<NTMSpecies>();
		Vector<NTMSpecies> rpoList = new Vector<NTMSpecies>();
		Vector<NTMSpecies> tufList = new Vector<NTMSpecies>();

		Vector<NTMSpecies> s16_100List = new Vector<NTMSpecies>();
		Vector<NTMSpecies> retList = new Vector<NTMSpecies>();


		Sample sample = sampleList.get(selectedSample);

		if(sample.alignmentPerformed[0]) {
			s16List = sample.selectedSpeciesList[0];
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

			if(retList.size() > 1 && sample.alignmentPerformed[1]) {
				rpoList = sample.speciesList[1];
				retList.retainAll(rpoList);
				if(retList.size() > 1 && sample.alignmentPerformed[2]) {
					tufList = sample.speciesList[2];
					retList.retainAll(tufList);
				}
			}

			Vector<NTMSpecies> tempList = new Vector<NTMSpecies>();

			//consensus sequence에 icSeq, chimaeraSeq 있는지 여부 , 이것도 있고 list에도 있으면 그냥 그걸로 final list 만들어보리고 끝.

			boolean icInTheList = false, chimaeraPresent = false; 

			for(NTMSpecies ntm : retList) {
				if(ntm.getSpeciesName().equals("Mycobacterium_intracellulare"))
					icInTheList = true;
				if(ntm.getSpeciesName().equals("Mycobacterium_chimaera"))
					chimaeraPresent = true;
				NTMSpecies temp = new NTMSpecies(ntm.getSpeciesName(), strScore);
				tempList.add(temp);
			}
			retList = tempList;


			icSeqLabel.setText("");
			chimaeraSeqLabel.setText("");

			if((icInTheList || chimaeraPresent) && context == 0) {
				boolean containsIcSeq = false;
				boolean containsChSeq = false;
				if(sample.fwdLoaded[context]) {
					if(sample.trimmedFwdTrace[context].getSequence().contains(icSeq)) 
						containsIcSeq = true;
					if(sample.trimmedFwdTrace[context].getSequence().contains(chSeq)) 
						containsChSeq = true;
				}
				if(sample.revLoaded[context]) {
					if(sample.trimmedRevTrace[context].getSequence().contains(icSeq)) 
						containsIcSeq = true;
					if(sample.trimmedRevTrace[context].getSequence().contains(chSeq)) 
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
		for(selectedSample=0;selectedSample<sampleList.size();selectedSample++) {
			System.out.println(selectedSample+1 + "th sample processing..");
			Sample sample = sampleList.get(selectedSample);
			for(context=0;context<3;context++) {
				if(sample.fwdLoaded[context] || sample.revLoaded[context]) {
					actualRun();
				}
			}
		}
		System.out.println("Finished");

		//다 돌리고 나면 첫번째 sample 16s로 채우기.
		selectedSample = 0;
		context = 0;
		s16Radio.setSelected(true);
		fillResults();
	}


	public void actualRun() {
		Sample sample = sampleList.get(selectedSample);

		//sample.speciesList[context] = new Vector(globalSpeciesList[context]);

		//speciesList 초기화. NTMSpecies 객체부터 새로 만들어야 함. globalSpeciesList 건드리면 안됨.
		sample.speciesList[context] = new Vector<NTMSpecies>();
		for(NTMSpecies ntm : globalSpeciesList[context]) {
			try {
				sample.speciesList[context].add((NTMSpecies)ntm.clone());
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		int inputLength = 0;
		if(sample.fwdLoaded[context] && !sample.revLoaded[context]) 
			inputLength = sample.trimmedFwdTrace[context].getSequenceLength();
		else if(!sample.fwdLoaded[context] && sample.revLoaded[context]) 
			inputLength = sample.trimmedRevTrace[context].getSequenceLength();

		//fwd, rev 같이있을때는 fwd, rev 두개를 align 시켜서 나오는 길이를 inputLength로. 
		else if (sample.fwdLoaded[context] && sample.revLoaded[context]) {	 
			MMAlignment mma = new MMAlignment();
			AlignedPair ap = null;
			ap = mma.localAlignment(sample.trimmedFwdTrace[context].getSequence(), sample.trimmedRevTrace[context].getSequence());
			inputLength = Integer.max(ap.getStart1(), ap.getStart2()) 
					+ Integer.max(sample.trimmedFwdTrace[context].getSequenceLength()-ap.getStart1(),  sample.trimmedRevTrace[context].getSequenceLength()-ap.getStart2());
		}
		if(inputLength == 0) return;

		Vector<NTMSpecies> removeList = new Vector<NTMSpecies>();
		for(int i=0;i<sample.speciesList[context].size();i++) {
			NTMSpecies thisSpecies = sample.speciesList[context].get(i);
			try {
				doAlignment(i);
			}
			catch(Exception ex) {
				//sSystem.out.println("alignment failure : " + thisSpecies.getSpeciesName());
				removeList.add(thisSpecies);
				continue;
			}

			//너무 짧게 align된 것들은 버림.
			double alignedPortion = sample.alignedPoints[context].size() / (double)inputLength;
			if(alignedPortion < 0.5) {
				removeList.add(thisSpecies);
				continue;
			}

			//score 계산
			int i_score = 0;
			double d_score = 0;
			for(int j=0;j<sample.alignedPoints[context].size();j++) {
				AlignedPoint ap = sample.alignedPoints[context].get(j);
				//if(ap.getDiscrepency()!='*')
				if(ap.getConsensusChar() == ap.getRefChar())
					i_score++;
			}

			d_score = (double)i_score / (double)sample.alignedPoints[context].size();
			d_score*=100;

			//score 너무 낮은것들 버림
			if(d_score < 90) {
				removeList.add(thisSpecies);
				continue;
			}

			//System.out.println("score : " + d_score);
			thisSpecies.setScore(d_score);
			thisSpecies.setQlen(inputLength);
			thisSpecies.setAlen(sample.alignedPoints[context].size());
		}

		sample.speciesList[context].removeAll(removeList);	//align 안된 것들, score 낮은것들 remove



		if(sample.speciesList[context].size()>0) {
			Collections.sort(sample.speciesList[context]);
			// 점수 제일 높은걸로 align
			try {
				//doAlignment 위에서 한번 했던거니까 error 날 일 없음.  
				doAlignment(0);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}

			makeCsvContents();

			sample.selectedSpeciesList[context] = new Vector<NTMSpecies>();
			for(NTMSpecies ntm:sample.speciesList[context]) {
				double cutoff = 99;
				if(context == 1) {
					if(ntm.isRgm()) 
						cutoff = 98.3;
					else cutoff = 99.3;
				}
				if(ntm.getScore() >= cutoff)
					sample.selectedSpeciesList[context].add(ntm);
				else
					break;
			}
			sample.alignmentPerformed[context] = true;
			sample.finalList = updateFinalList();
		}
	}




	private void makeEmptyHeader() {
		Label emptyLabel = new Label(" ");
		emptyLabel.setFont(new Font("Consolas", 14));
		emptyLabel.setMinSize(130,headerHeight);
		emptyLabel.setPrefSize(130, headerHeight);
		headerPane.add(emptyLabel, 0,  0);

		headerLabel[0] = new Label("Reference : ");
		headerLabel[0].setFont(new Font("Consolas", 14));
		headerLabel[0].setMinSize(130,headerHeight);
		headerLabel[0].setPrefSize(130, headerHeight);
		headerPane.add(headerLabel[0], 0,  1);

		headerLabel[1] = new Label("Forward   : ");
		headerLabel[1].setFont(new Font("Consolas", 14));
		headerLabel[1].setMinSize(130,headerHeight);
		headerLabel[1].setPrefSize(130, headerHeight);
		headerPane.add(headerLabel[1], 0,  2);

		headerLabel[2] = new Label("Reverse   : ");
		headerLabel[2].setFont(new Font("Consolas", 14));
		headerLabel[2].setMinSize(130,headerHeight);
		headerLabel[2].setPrefSize(130, headerHeight);
		headerPane.add(headerLabel[2], 0,  3);

		Label consensusTitle = new Label("Consensus : ");
		consensusTitle.setFont(new Font("Consolas", 14));
		consensusTitle.setMinSize(130,headerHeight);
		consensusTitle.setPrefSize(130, headerHeight);
		headerPane.add(consensusTitle, 0,  4);
	}

	/**
	 * Prints the result of alignment on the alignment pane
	 */
	private void printAlignedResult() {
		Sample sample = sampleList.get(selectedSample);

		//Alignment panel

		if(sample.fwdLoaded[context] || sample.revLoaded[context])
			labels = new Label[4][sample.alignedPoints[context].size()];

		gridPane = new GridPane();


		headerPane.getChildren().remove(headerLabel[1]);
		if(sample.fwdLoaded[context]) {
			headerLabel[1] = new Label("Forward   : ");
			headerLabel[1].setFont(new Font("Consolas", 14));
			headerLabel[1].setMinSize(130,headerHeight);
			headerLabel[1].setPrefSize(130, headerHeight);
		}
		else {
			headerLabel[1] = new Label();
			headerLabel[1].setMinSize(130,1);
			headerLabel[1].setPrefSize(130,1);
		}
		headerPane.add(headerLabel[1], 0,  2);


		headerPane.getChildren().remove(headerLabel[2]);
		if(sample.revLoaded[context]) {
			headerLabel[2] = new Label("Reverse   : ");
			headerLabel[2].setFont(new Font("Consolas", 14));
			headerLabel[2].setMinSize(130,headerHeight);
			headerLabel[2].setPrefSize(130, headerHeight);
		}
		else {
			headerLabel[2] = new Label();
			headerLabel[2].setMinSize(130,1);
			headerLabel[2].setPrefSize(130,1);
		}
		headerPane.add(headerLabel[2], 0,  3);
		

		
		
		for (int i=0;i<sample.alignedPoints[context].size();i++) {
			AlignedPoint point = sample.alignedPoints[context].get(i);

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
			Label consensusLabel = new Label();
			Label discrepencyLabel = new Label();
			Label indexLabel = new Label();

			refLabel.setFont(new Font("Consolas", fontSize));
			fwdLabel.setFont(new Font("Consolas", fontSize));
			revLabel.setFont(new Font("Consolas", fontSize));
			consensusLabel.setFont(new Font("Consolas", fontSize));
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
			if(i%10==0 && sample.alignedPoints[context].size()-i >= 5) {
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
			if(sample.fwdLoaded[context]) {
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
			if(sample.revLoaded[context]) {
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

			//Consensus
			consensusLabel.setText(Character.toString(point.getConsensusChar()));
			consensusLabel.setPrefSize(10, 10);
			if(point.getConsensusChar() != point.getRefChar())
				consensusLabel.setBackground(new Background(new BackgroundFill(Color.web("#FF3300"), CornerRadii.EMPTY, Insets.EMPTY)));

			consensusLabel.setOnMouseClicked(new ClickEventHandler(i));
			gridPane.add(consensusLabel,  i+1, 4);
			labels[3][i] = consensusLabel;

		}

		alignmentPane.setContent(gridPane);



		//fwdTracePane
		Formatter formatter = sample.formatter[context];

		if(sample.fwdLoaded[context]) {
			// 새로운 좌표로 update (fwdPane, revPane)
			java.awt.image.BufferedImage awtImage = sample.trimmedFwdTrace[context].getShadedImage(0,0,0, sample.formatter[context]);
			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
			// 시작점에 화면 align
			adjustFwdPane(formatter.fwdTraceAlignStartPoint);
		}
		else if(sample.fwdTraceFileName[context] != null) {
			fwdTraceFileLabel.setText(sample.fwdTraceFileName[context]);
			fwdPane.setContent(new Label("Poor Quality Trace File"));
		}
		else {
			fwdTraceFileLabel.setText("No file exists");
			fwdPane.setContent(new Label("No file exists"));
		}

		if(sample.revLoaded[context]) {
			java.awt.image.BufferedImage awtImage2 = sample.trimmedRevTrace[context].getShadedImage(0,0,0, sample.formatter[context]);
			javafx.scene.image.Image fxImage2 = SwingFXUtils.toFXImage(awtImage2, null);
			ImageView imageView2 = new ImageView(fxImage2);
			revPane.setContent(imageView2);
			adjustRevPane(formatter.revTraceAlignStartPoint);
		}
		else if(sample.revTraceFileName[context] != null) {
			revTraceFileLabel.setText(sample.revTraceFileName[context]);
			revPane.setContent(new Label("Poor Quality Trace File"));
		}
		else {
			revTraceFileLabel.setText("No file exists");
			revPane.setContent(new Label("No file exists"));
		}
	}

	/**
	 * Focuses the designated point on the forward trace pane
	 * @param fwdTraceIndex : the point to be focused
	 */
	private void adjustFwdPane(int fwdTraceIndex) {
		Sample sample = sampleList.get(selectedSample);
		Formatter formatter = sample.formatter[context];
		if(formatter.fwdNewLength <= paneWidth) return;
		if(fwdTraceIndex >sample.trimmedFwdTrace[context].getBaseCalls().length)
			fwdTraceIndex = sample.trimmedFwdTrace[context].getBaseCalls().length;
		double coordinate = formatter.fwdStartOffset + sample.trimmedFwdTrace[context].getBaseCalls()[fwdTraceIndex-1]*2;
		double hValue = (coordinate - paneWidth/2) / (formatter.fwdNewLength - paneWidth);
		fwdPane.setHvalue(hValue);
	}

	/**
	 * Focuses the designated point on the reverse trace pane
	 * @param revTraceIndex : the point to be focused
	 */
	private void adjustRevPane(int revTraceIndex) {
		Sample sample = sampleList.get(selectedSample);
		Formatter formatter = sample.formatter[context];
		if(formatter.revNewLength <= paneWidth) return;
		if(revTraceIndex > sample.trimmedRevTrace[context].getBaseCalls().length)
			revTraceIndex = sample.trimmedRevTrace[context].getBaseCalls().length;

		double coordinate = formatter.revStartOffset + sample.trimmedRevTrace[context].getBaseCalls()[revTraceIndex-1]*2;

		double hValue = (coordinate - paneWidth/2) / (formatter.revNewLength - paneWidth);
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
		Sample sample = sampleList.get(selectedSample);
		sample.selectedAlignmentPos[context] = selectedAlignmentPos;
		AlignedPoint ap = sample.alignedPoints[context].get(selectedAlignmentPos);
		char fwdChar = Formatter.gapChar;
		char revChar = Formatter.gapChar;
		int selectedFwdPos = 0;
		int selectedRevPos = 0;

		if(sample.fwdLoaded[context]) {
			selectedFwdPos = ap.getFwdTraceIndex();
			fwdChar = ap.getFwdChar();
		}
		if(sample.revLoaded[context]) {
			selectedRevPos = ap.getRevTraceIndex();
			revChar = ap.getRevChar();
		}


		//selectedAlignmentPos : 이것만 0부터 시작하는 index
		//selectedFwdPos, selectedRevPos : 1부터 시작하는 index

		//boolean fwdGap = (fwdChar == Formatter.gapChar); 
		//boolean revGap = (revChar == Formatter.gapChar);

		for(int i=0; i<sample.alignedPoints[context].size();i++) {
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
		if(sample.fwdLoaded[context]) {
			for(int i=0; i<sample.alignedPoints[context].size();i++) {
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
				awtImage = sample.trimmedFwdTrace[context].getShadedImage(3,tempFwdPos-1,tempFwdPos-1, sample.formatter[context]);

			}
			else awtImage = sample.trimmedFwdTrace[context].getShadedImage(1, tempFwdPos-1, tempFwdPos-1, sample.formatter[context]);

			javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);

			adjustFwdPane(selectedFwdPos);
		}
		if(sample.revLoaded[context]) {
			for(int i=0; i<sample.alignedPoints[context].size();i++) {
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
				awtImage2 = sample.trimmedRevTrace[context].getShadedImage(3,tempRevPos-1,tempRevPos-1, sample.formatter[context]);
			}

			else 
				awtImage2 = sample.trimmedRevTrace[context].getShadedImage(1, tempRevPos-1, tempRevPos-1, sample.formatter[context]);
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

	public void handleFwdZoomIn() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.fwdLoaded[context]) {
			sample.trimmedFwdTrace[context].zoomIn();
			BufferedImage awtImage = sample.trimmedFwdTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
		}
	}
	public void handleFwdZoomOut() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.fwdLoaded[context]) {
			sample.trimmedFwdTrace[context].zoomOut();
			BufferedImage awtImage = sample.trimmedFwdTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			fwdPane.setContent(imageView);
		}
	}
	public void handleRevZoomIn() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.revLoaded[context]) {
			sample.trimmedRevTrace[context].zoomIn();
			BufferedImage awtImage = sample.trimmedRevTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			revPane.setContent(imageView);
		}
	}
	public void handleRevZoomOut() {
		Sample sample = sampleList.get(selectedSample);
		if(sample.revLoaded[context]) {
			sample.trimmedRevTrace[context].zoomOut();
			BufferedImage awtImage = sample.trimmedRevTrace[context].getDefaultImage();
			Image fxImage = SwingFXUtils.toFXImage(awtImage, null);
			ImageView imageView = new ImageView(fxImage);
			revPane.setContent(imageView);
		}
	}



}



