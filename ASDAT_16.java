package asdat_16;



import processing.core.*;
import processing.opengl.*;
import processing.serial.Serial;
import controlP5.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import java.math.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFileChooser;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import arb.soundcipher.*;

import java.awt.Desktop;
import java.awt.Event;


public class ASDAT_16 extends PApplet {

	//The code below is organized based on 5 major groups (color, brain, sound, menu and spectrogram (its graph actually LOL)) 
	
	//-------- Variables for color group --------------------------------START------------------------//
	
	ControlP5 mainAppGroups;
	ControlP5 jControlR, jControlG, jControlB, jControlGrey;
	ControlP5 buttons; //This object is used also to create the arrow buttons in brainGroup
	ControlP5 buttonBrain; //This object is used to open threshold controller window
	//Draw and pasNewColors buttons MUST be global!
	Button drawButton, passNewColors;
	
	int R = 255;
	int G = 255;
	int B = 255;
	
	int transparency = 255;
	int colorMix = color(0,0,0,0);
	int colorRed = color(0,0,0);
	int colorGreen = color(0,0,0);
	int colorBlue = color(0,0,0);
	
	//Colors for the 4 sliders in colorGroup
	int colorRedBack = color (175,0,0);
	int colorRedFore = color (255,0,0);
	int colorRedActive = color (255,0,0);
	
	int colorGreenBack = color (0,175,0);
	int colorGreenFore = color (0,255,0);
	int colorGreenActive = color (0,255,0);
	
	int colorBlueBack = color (0,0,175);
	int colorBlueFore = color (0,0,255);
	int colorBlueActive = color (0,0,255);

	int colorGreyBack = color (128,128,128);
	int colorGreyFore = color (192,192,192);
	int colorGreyActive = color (192,192,192);
	int colorGreyActive2 = color (210,210,210);
	
	//Different colors that are used in more than one groups
	int black = color(0,0,0);
	int white = color (255,255,255);
	int greyTr = color(124,124,124,100);
	int greyColor = color (124,124,124);
	int darkGrey = color (192,192,192);
	
	//Drawing Window Variables (passing data and flags)
	drawingWindow windowForDraw;
	int[] colorArray = new int[8];
	boolean drawWindowFlag = true;
	int colorCounter;
	
	//The color of the background (Tr behind the image)
	int colorBack = color (255,255,255);
	PImage backgroundImage;
	
	
	//Autism Window Variables
	drawingWindowAutism windowForDrawAutism;
	
	//About Window Variables
	drawingWindowAbout windowForDrawAbout; 
	
	//About Window Variables
		drawingWindowDocumentation windowForDrawDocumentation; 
		
	//Coherence of patient
		drawingWindowCoherence windowForDrawCoherence;
		
	
		boolean flagWindowColorFirstTime = false;
		
	//-------- Variables for color group --------------------------------FINISH------------------------//
	
	//-------- Variables for brain group---------------------------------START-------------------------//
	PShape model;
	PShape groupShape;
	PShape light1,light2,light3,light4,light5,light6,light7,light8;
	float rotateAngleY = 0f;
	float rotateAngleX = 0f;

	
	float[] DownThreshold;
	float[] UpThreshold;
	
	drawingWindowThreshold windowForDrawThreshold;
	drawingWindowTime windowForDrawTime;
	
	boolean[] lightFlag = new boolean[16];
	
	int[][] timeWindowArray = new int[16][40];
	int[] timeWindowArrayCounter = new int[40];
	
	//-------- Variables for brain group---------------------------------FINISH-------------------------//
	
	//-------- Variables and classes for Spectro group--------------------------------START-------------------------//
	ControlP5 spectroMyGroup;
	ControlP5 SpecButton;
	ControlP5 WaveButton;
	
	Toggle toggleMu;
	ControlP5 MuToggle;
	boolean channelMuFlag = true;
	
	boolean switch_to_16 = false; //false -> 0-7 channel true-> 8-15 channel
	final int channel_number = 16;
	
	boolean state = false;
	Chart[] myChart = new Chart[channel_number];
	String[] dataset = {"FP1", " FP2" ,  " C3", " C4", " T5"," T6", " O1", " O2", " F7"," F8"," F3",
			" F4"," T3"," T4"," P3"," P4"};
	Serial myPort;
	int baud = 115200;
	final String command_stop = "s";
    final String command_startText = "x";
    final String command_startBinary = "b";
    final String[] command_deactivate_channel = {"1", "2", "3", "4", "5", "6", "7", "8", "q", "w", "e", "r", "t", "y", "u", "i"};
    final String[] command_activate_channel = {"!", "@", "#", "$", "%", "^", "&", "*","Q", "W", "E", "R", "T", "Y", "U", "I"};
    final static byte BYTE_START = (byte)0xA0;   
    final static byte BYTE_END   = (byte)0xC0;  
    final static byte BYTE_ONE   = (byte)0xA1;
    float Vref = 4.5f ;
    float gain = 24f;
    private float scale_fac_uVolts_per_count = (Vref/ gain/ ((float)(pow(2,23)-1)))  ;
    //Vref / ((float)(pow(2,23)-1)) / ADS1299_gain  * 1000000.f; //ADS1299 datasheet Table 7, confirmed through experiment
    private float fs_Hz=250f;  //sample rate

    int buff;
    float []validc= new float[750];
    int counterfromzero=0;
    int counter=0;
    int channelCounter=0;
    int localCounter=0;
    int flagBuffer =-1;
    int packetCounter;
    byte[] bufferOfThree = new byte[3];
    int[] bufferData= new int[8];
    boolean flagForDraw=false;
    boolean flagForDraw2=false;
    float[][] dataPacket  = new float[channel_number][750];
	
   // private int colorLineChart			 = color(255,255,0); 
    private int colorBackgroundChart	 = color(100,100,100);
    
    int flagBP       = 0;
    int flagNotch    = 2;
    
    public class filterClass {
    	  public double[] a;
    	  public double[] b;  	 
    	  filterClass(double[] b_given, double[] a_given) {
    	    b = new double[b_given.length];a = new double[b_given.length];
    	    for (int i=0; i<b.length;i++) { b[i] = b_given[i];}
    	    for (int i=0; i<a.length;i++) { a[i] = a_given[i];}   	   
    	  }
    	}
 
    filterClass[] BPproperties 		 = new filterClass[6];
    filterClass[] notchProperties 	 = new filterClass[3];   
    filterClass[] muProperties		 = new filterClass[2]; //muRhythm properties
    
   // final int N_AUTISM_CONFIGS = 2;
    
    
    
    filterClass[] deltaProperties = new filterClass[2];
    filterClass[] thetaProperties = new filterClass[2];  
    filterClass[] alphaProperties = new filterClass[2];  
    filterClass[] betaProperties  = new filterClass[2];  
    filterClass[] gammaProperties = new filterClass[2];
    
    int currentBP = 5;
    int currentNotch = 1;  // set to 0 to default to 60Hz, set to 1 to default to 50Hz
    boolean flagFilterUsed1 = false;
    boolean flagFilterUsed2 = false;
    boolean flagFilterUsed3 = false;
    int counterFile=0;
    
    int Nfft = 256; 
    FFT fftBuff[] = new FFT[channel_number];   //from the minim library
    float bandDB;	 
    float bandHeight; 
    FFT fftLin;
    FFT fftLog;
    float[] maxAmp= new float[channel_number];
    
	//-------- Variables and classes for Spectro group--------------------------------FINISH-------------------------//
	
	
	
	//-------Variables for sound group------------------------------------START--------------------------//
	
	//ControlP5 objects for different controllers with different settings
	ControlP5 myGroup;
	ControlP5 soundOptions;
	ControlP5 soundToggle;
	ControlP5 soundLabel;
	ControlP5 soundButtons;
	ControlP5 docButtons;
	ControlP5 soundRadioButtons;
	ControlP5 soundNumberBox;
	
	
	SCScore score = new SCScore();
	float counterMidi = 0;
	float[] pitches = new float[8];
	
	float[][] chooseNoteSpectrumSum = new float[16][128];
	int[] chooseNoteSpectrumSumCounter = new int[16];
	
	boolean flagSoundGroup = false;
	//Sliders
	Slider speedOfSound, meanSampleNumber;
	
	//Start/Stop sound
	Button startSound, stopSound;
	boolean soundIsPlaying = false;
	
	//Start Recording
	Button buttonMIDI, startRecording, stopRecording, saveRecording, readMore, exitFromWindow, exitDoc;
	boolean RecordingIsPlaying = false;
	
	//Toggles
	Toggle chOO1, chOO2, chOO3, chOO4, chOO5, chOO6, chOO7, chOO8; 
	int toggleON = color(0,255,0);
	int toggleOFF = color(255,0,0);
	//Text captions
	Textlabel chLabel1, chLabel2, chLabel3, chLabel4, chLabel5, chLabel6, chLabel7, chLabel8, octaveHead,volumeHead; 
	//Radio Buttons 
	RadioButton radio1,radio2,radio3,radio4,radio5,radio6,radio7,radio8;
	//Number Boxes
	Numberbox chBox1, chBox2,chBox3,chBox4,chBox5,chBox6,chBox7,chBox8;
	
	//Slider colors (sound group)
	int greyBackSound = color (100,100,100);
	int greyForeSound = color (170,170,170);
	int greyActiveSound = color (170,170,170);
	
	//Minim variables 
	Minim myMinim;
	AudioSample[][] pianoSamples;
	
	//buffers - input signal values
	float[][] musicDataBuffer = new float[8][50];
	int musicDataBufferCounter = 0;
	
	//Mean values per channel
	float[] meanBuffer = new float[8];
	int numberOfValuesForMean = 25;
	
	//Time
	int step = 0;
	int soundSpeedValue = 1000;
	
	//Flags to set sound channels on//off
	boolean[] channelSoundFlag = new boolean[8];
	//Array to know which octave each channel plays
	int[] channelOctave = new int[8];
	//Channel Volume
	int[] channelVolume = new int[8];
	
	//-------Variables for sound group------------------------------------FINISH------------------------//
	
	//-------Menu Variables ----------------------------------------------START-------------------------//
	float stepArd = 0;
	
	
	boolean flagP    = false;
	boolean flagN    = false;
	int flagG    =         1;
	int flagIT   =         1;
	boolean flagB    = true;
	boolean flagRefEl1 = false;
	boolean flagRefEl2 = true;
	
	
	String stringG    = "Gain                         x24";
	
	String stringB    = "Ground                         On";
	String stringRefEl1 = "Ref El 1                        Off";
	String stringRefEl2 = "Ref El 2                       On";

	
	String stringBP       = "BandPass      Filter           0.3  - 30 Hz";
	String stringNotch    = "Notch              Filter           50 Hz";
	
	String stringPower_Down 		= "1";
	String stringGain_Set 			= "6";
	String stringInput_Type_Set		= "1";
	String stringGround_Set			= "0";
	String stringRefEl2_Set			= "1";
	String stringRefEl1_Set			= "0";
	
	
	
	ControlP5 scrollList;
	List<String> fileAdds = Arrays.asList("Neurocognitive Test","New Project","Open File","Save","Exit");
	List<String> settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
	List<String> filterAdds    = Arrays.asList(stringBP,stringNotch);	
	List<String> helpAdds      = Arrays.asList("About","Documentation");	
	ScrollableList fileList,	 settingsList,	 filterList,		helpList, gapList ;
	
	
	
	
	int backColorScroll = color(125,125,125);
	int activeColorScroll = color(200,200,200);
	int foreColorScroll = color(100,100,100);
	int backgroundColor = color(255,255,255);
	
	//Files handle variables-------------------------START------------------------------------------//
	String file_name="";
	JFileChooser file_chooser = new JFileChooser(new File("C:\\"));
	File theFile;
	File tempFile;
	float dataFileHolder[][] = null;
	Formatter x;
	
	//---Menu Variable--------------------------------FINISH----------------------------------------//
	
	//---MenuAutism Variable--------------------------------START----------------------------------------//
	ControlP5 scrollListAutism;
	List<String> autismAdds = Arrays.asList("Add Patient Group","Add control group","Save Tests","Exit from window");
	ScrollableList autismList;
	//---MenuAutism Variable--------------------------------FINISH----------------------------------------//
	
	
	//---Variables for Playback--------------------------------START----------------------------------------//
		boolean playbackFlag = false;
		boolean playbackLiveFlag = false;
		boolean playbackTrigger = false;
		Scanner sPlayback;
		int switchForBuffer=1;
	//---Variables for Playback--------------------------------FINISH----------------------------------------//
	
		
		
    //---Variables for autism window--------------------------------START----------------------------------------//
		
		int numberFilesADS = 0;
		int numberFilesControlGroup = 0;
		ControlP5 testButton;
		String indicator = "Please add the files to begin the test";
		boolean displayTest = false;
		File[] filesADS;
		File[] filesControlGroup;
		boolean[][] chooseTestADS 		    = new boolean[6][4]; //true-->t-test,false-->U-test
		boolean[][] chooseTestControlGroup  = new boolean[6][4];
		float[][][] regionADS;
		float[][][] regionControlGroup;
		float[][]   PvaluesTable1 = new float[6][4];
		String disTest[][] = new String[6][4];
		float[][]   aValuetTest		 		= new float[6][4];
		boolean[][] tTestSignificant 		= new boolean[6][4];
		
		float rankTable[];
		float[][]   aValueUtest		 		= new float[6][4];
		boolean[][] uTestSignificant 		= new boolean[6][4];
		String[][] PvalueU = new String[6][4]; 
		float[][] meanTable1ASD = new float[6][4];
		float[][] SDTable1ASD   = new float[6][4];
		float[][] meanTable1CG = new float[6][4];
		float[][] SDTable1CG   = new float[6][4];
		float[][] meanTable2ASD = new float[6][4];
		float[][] SDTable2ASD   = new float[6][4];
		float[][] meanTable2CG = new float[6][4];
		float[][] SDTable2CG   = new float[6][4];
		float patient[][][];							
		float controlGroup[][][];
		
		float aValue = 0f;		
		float[][] aValueWS = new float[6][4]; 
		boolean[][] SignificantWS = new boolean[6][4];
		
		
		//coherence variables
		float patientCoherence[][][];							
		float controlGroupCoherence[][][];
		float[][] deltaCoherenceASD; //delta in patient in a single pair
		float[][] thetaCoherenceASD; //theta in patient in a single pair
		float[][] alphaCoherenceASD; //alpha in patient in a single pair
		float[][] betaCoherenceASD; //beta  in patient in a single pair
		float[][] deltaCoherenceControlGroup; //delta in patient in a single pair
		float[][] thetaCoherenceControlGroup; //theta in patient in a single pair
		float[][] alphaCoherenceControlGroup; //alpha in patient in a single pair
		float[][] betaCoherenceControlGroup; //beta  in patient in a single pair
		
		float[] meanDeltaASD_Coherence = new float[120];
		float[] sdDeltaASD_Coherence   = new float[120];
		float[] meanThetaASD_Coherence = new float[120];
		float[] sdThetaASD_Coherence   = new float[120];
		float[] meanAlphaASD_Coherence = new float[120];
		float[] sdAlphaASD_Coherence   = new float[120];
		float[] meanBetaASD_Coherence  = new float[120];
		float[] sdBetaASD_Coherence    = new float[120];
		
		float[] meanDeltaCG_Coherence  = new float[120];
		float[] sdDeltaCG_Coherence    = new float[120];	
		float[] meanThetaCG_Coherence  = new float[120];
		float[] sdThetaCG_Coherence    = new float[120];
		float[] meanAlphaCG_Coherence  = new float[120];
		float[] sdAlphaCG_Coherence    = new float[120];
		float[] meanBetaCG_Coherence   = new float[120];
		float[] sdBetaCG_Coherence     = new float[120];
		
		
		boolean[] chooseTestASDcoherenceDelta 		     = new boolean[120]; //true-->t-test,false-->U-test
		boolean[] chooseTestASDcoherenceTheta 		     = new boolean[120]; 
		boolean[] chooseTestASDcoherenceAlpha 		     = new boolean[120]; 
		boolean[] chooseTestASDcoherenceBeta 		     = new boolean[120]; 
		
		boolean[] chooseTestControlGroupCoherenceDelta     = new boolean[120];
		boolean[] chooseTestControlGroupCoherenceTheta     = new boolean[120];
		boolean[] chooseTestControlGroupCoherenceAlpha     = new boolean[120];
		boolean[] chooseTestControlGroupCoherenceBeta      = new boolean[120];
		
		String disTestCohDelta[] = new String[120];
		String disTestCohTheta[] = new String[120];
		String disTestCohAlpha[] = new String[120];
		String disTestCohBeta[]  = new String[120];
		
		String[] PvalueCoherenceDelta = new String[120]; 
		String[] PvalueCoherenceTheta = new String[120];
		String[] PvalueCoherenceAlpha = new String[120];
		String[] PvalueCoherenceBeta = new String[120];
		
		
		float[]   aValuetTestCoherenceDelta		 		= new float[120];
		float[]   aValuetTestCoherenceTheta		 		= new float[120];
		float[]   aValuetTestCoherenceAlpha		 		= new float[120];
		float[]   aValuetTestCoherenceBeta		 		= new float[120];
		boolean[] tTestSignificantCoherenceDelta 		= new boolean[120];
		boolean[] tTestSignificantCoherenceTheta 		= new boolean[120];
		boolean[] tTestSignificantCoherenceAlpha 		= new boolean[120];
		boolean[] tTestSignificantCoherenceBeta 		= new boolean[120];
		
		float[]  aValueUtestCoherenceDelta		 		= new float[120];
		float[]  aValueUtestCoherenceTheta		 		= new float[120];
		float[]  aValueUtestCoherenceAlpha		 		= new float[120];
		float[]  aValueUtestCoherenceBeta		 		= new float[120];
		
		boolean[] uTestSignificantCoherenceDelta 		= new boolean[120];
		boolean[] uTestSignificantCoherenceTheta 		= new boolean[120];
		boolean[] uTestSignificantCoherenceAlpha 		= new boolean[120];
		boolean[] uTestSignificantCoherenceBeta 		= new boolean[120];
		
		
		
		
				
		float[][] aValueWSdelta = new float[120][4]; 
		float[][] aValueWStheta = new float[120][4]; 
		float[][] aValueWSalpha = new float[120][4]; 
		float[][] aValueWSbeta  = new float[120][4]; 
		
		boolean[][] SignificantWSdelta = new boolean[120][4];
		boolean[][] SignificantWStheta = new boolean[120][4];
		boolean[][] SignificantWSalpha = new boolean[120][4];
		boolean[][] SignificantWSbeta  = new boolean[120][4];
		
		// arrays and values for statistical tests
		boolean falseImputs = false; //checking if n exists in the array of critical value	
		String significant = "non-significant"; //variable for checking the significance of the result
		
		//initializing the arrays for tests
		float[][] arrayUtest1 = new float[19][16];
		float[][] arrayUtest2 = new float[19][16];
		float[][] arrayWS = new float[38][12];
		float[][] arraytTest = new float[200][6];
		
		//---Variables for autism window--------------------------------FINISH----------------------------------------//
		
		final static String ICON  = "favicon.png";
		final static String TITLE = "favicon";
			
		
		
		
	public void settings()
	{
		size(1300,690,P3D);
		PJOGL.setIcon(ICON);
		//ICON MUST BE PNG 
	}
	
	
	
	
	public void setup() 
	{
		
		noLoop();
		  surface.setTitle("NASESE (Neurocognitive Assessment Software for Enrichment Sensory Environments)");
		
		createFile();
		for(int i=0; i<750; i++){
			for(int j=0; j< channel_number; j++){
				dataPacket[j][i] = 0f;
		}
			
		}
		for(int i=0; i<256; i++){
		fooData[i] = 0f;
		}
		
		//surface.setResizable(false);
		mainAppGroups = new ControlP5(this);
		colorGroupSetup();
	
		spectroGroupSetup();
	
		brainGroupSetup();
	
		soundGroupSetup();		
		
		menuSetup();
		
		backgroundImage = loadImage("back.png");
		
		frameRate(10);
		loop();
		println("end");
	}

	
	int startTime,startTime2;
	int thisTime,thisTime2;
	public void draw()
	{	
		
		
		thisTime = millis();
		
		
		if    (   ( playbackLiveFlag == true) && ( playbackFlag == true)  &&  ((thisTime - startTime) >=1000) ){
			
			startTime = thisTime;
			
			playback();
			
		}	
			
		
		hint(ENABLE_DEPTH_TEST);
		//Block for background color and image
		background(colorBack);
		
		pushMatrix(); 
		noStroke();
		fill(backColorScroll); 
		rect(0,0,1200,20);
		popMatrix();
		
		pushMatrix();
		tint(255,20);
		translate(-200,-70,-100);
		image(backgroundImage,0,0,backgroundImage.width*2f,backgroundImage.height*2f);
		popMatrix();
		//---------
		
		//Lights and tint back to normal (no TR)
		pushMatrix();
		lights();
		tint(255,255);
		popMatrix();
		
		
		hint(DISABLE_DEPTH_TEST);
		noLights();
		
		
	}
	
	//Lights function 
	public void lights() 
	{ 	
		int w=125, m=-125, p=+125; 
		ambientLight(w,w,w);
		
		directionalLight(w, w, w, m, m, m); 
		directionalLight(w, w, w, p, p, p); 
		directionalLight(w, w, w, m, m, p); 
		directionalLight(w, w, w, p, p, m); 
	}
	
	//----Functions for Color Group-------------------------START---------------------------------------//
	public void colorGroupSetup()
	{
		colorCounter = 0;
		
		for(int i = 0; i < 8; i++)
		{
			colorArray[i] = color(255,255,255,255);
		}
		
		controlP5.Group colorGroup = mainAppGroups.addGroup("colorGroup",width/120,height - (height/3 - height/8) - 10 -(height/3 - 19*height/83) - 10 ,width/2-2*width/120);
		colorGroup.setBackgroundHeight(height/3 - 19*height/83);
		colorGroup.hideBar();
		colorGroup.setColorForeground(black);
		colorGroup.setColorLabel(white);
		colorGroup.setColorBackground(greyColor);
		colorGroup.setBackgroundColor(greyTr);
		
		//Sliders
		jControlR = new ControlP5(this);
		jControlR.setColorForeground(colorRedFore);
		jControlR.setColorBackground(colorRedBack);
		jControlR.setColorActive(colorRedActive);
		jControlR.setColorCaptionLabel(black);
		Slider r = jControlR.addSlider("R");
		r.setGroup(colorGroup);
		r.setPosition(width/120,10);
		r.setWidth(width/10);
		r.setHeight(height/50);
		r.setRange(0, 255);
		r.setValue(255);
		
		jControlG = new ControlP5(this);
		jControlG.setColorForeground(colorGreenFore);
		jControlG.setColorBackground(colorGreenBack);
		jControlG.setColorActive(colorGreenActive);
		jControlG.setColorCaptionLabel(black);
		Slider g = jControlG.addSlider("G");
		g.setGroup(colorGroup);
		g.setPosition(width/120,height/50 + 10);
		g.setWidth(12*width/120);
		g.setHeight(height/50);
		g.setRange(0, 255);
		g.setValue(255);
		
		jControlB = new ControlP5(this);
		jControlB.setColorForeground(colorBlueFore);
		jControlB.setColorBackground(colorBlueBack);
		jControlB.setColorActive(colorBlueActive);
		jControlB.setColorCaptionLabel(black);
		Slider b = jControlB.addSlider("B");
		b.setGroup(colorGroup);
		b.setPosition(width/120,height/50 + height/50 + 10);
		b.setWidth(12*width/120);
		b.setHeight(height/50);
		b.setRange(0, 255);
		b.setValue(255);
		
		jControlGrey = new ControlP5(this);
		jControlGrey.setColorForeground(colorGreyFore);
		jControlGrey.setColorBackground(colorGreyBack);
		jControlGrey.setColorActive(colorGreyActive);
		jControlGrey.setColorCaptionLabel(black);
		Slider grey = jControlGrey.addSlider("");
		grey.setGroup(colorGroup);
		grey.setPosition(width/120,height/50 + height/50 + height/50 + 10);
		grey.setWidth(12*width/120);
		grey.setHeight(height/50);
		grey.setRange(0, 255);
		grey.setValue(255);
		
		//Buttons
		buttons = new ControlP5(this);
		buttons.setColorForeground(colorGreyFore);
		buttons.setColorBackground(colorGreyBack);
		buttons.setColorActive(colorGreyActive2);
		buttons.setColorCaptionLabel(black);
		
		Button changeColor = buttons.addButton("changeColor");
		changeColor.setGroup(colorGroup);
		changeColor.setLabel("Background Color");
		changeColor.setPosition(width/7.1f,6.8f*height/120);
		changeColor.setSize(8*width/130,2*height/60);
		
		Button saveButton = buttons.addButton("saveButton");
		saveButton.setGroup(colorGroup);
		saveButton.setLabel("Save Color");
		saveButton.setPosition(width/4.775f,6.8f*height/120);
		saveButton.setSize(8*width/130,2*height/60);
		
		Button undoButton = buttons.addButton("undoButton");
		undoButton.setGroup(colorGroup);
		undoButton.setLabel("Undo Color");
		undoButton.setPosition(width/3.6f,6.8f*height/120);
		undoButton.setSize(8*width/130,2*height/60);
		
		Button clearButton = buttons.addButton("clearButton");
		clearButton.setGroup(colorGroup);
		clearButton.setLabel("Clear Colors");
		clearButton.setPosition(width/2.8925f,6.8f*height/120);
		clearButton.setSize(8*width/130,2*height/60);
		
		drawButton = buttons.addButton("drawButton");
		drawButton.setGroup(colorGroup);
		drawButton.setLabel("Start Draw");
		drawButton.setPosition(49.7f*width/120,6.8f*height/120);
		drawButton.setSize(8*width/130,2*height/60);
		
		passNewColors = buttons.addButton("passColors");
		passNewColors.setGroup(colorGroup);
		passNewColors.setLabel("Refresh Colors");
		passNewColors.setPosition(49.7f*width/120,6.8f*height/120);
		passNewColors.setSize(8*width/130,2*height/60);
		passNewColors.setVisible(false);
		
		colorGroup.addDrawable(new CDrawable(){
			public void draw(PGraphics p) 
			{
				colorRed = color(R,0,0);
				colorGreen = color(0,G,0);
				colorBlue = color(0,0,B);
				colorMix = color (R,G,B,transparency);
				
				p.pushMatrix();
				p.fill(black);
				p.rect(12*width/120 + width/120 + 17,height/59,width/120,10);
				p.rect(12*width/120 + width/120 + 17 ,height/47f  + 10 ,width/120,10);
				p.rect(12*width/120 + width/120 + 17 ,height/58 + height/40 + 10,width/120,10);
				
				p.rect(19.5f*width/120, 11,2f*width/112f -1,4*height/120 -1);
				
				
				p.fill(greyColor);
				p.rect(width/2.84f,10,14.9f*width/120f,4*height/120);
				p.fill(black);
				p.rect(width/2.84f +1 ,11,14.9f*width/120f -2 ,4*height/120 -2);
				/**/
				//menu color1
				p.fill(colorArray[0]);
				p.rect(width/2.829f  ,12,2f*width/112f -4,4*height/120 -4);
				//menu color2
				p.fill(colorArray[1]);
				p.rect(width/2.713f  ,12,2f*width/112f -4,4*height/120 -4);
				//menu color3
				p.fill(colorArray[2]);
				p.rect(width/2.605f  ,12,2f*width/112f -4,4*height/120 -4);
				//menu color 4
				p.fill(colorArray[3]);
				p.rect(width/2.506f  ,12,2f*width/112f -4,4*height/120 -4);	
				//menu color5
				p.fill(colorArray[4]);
				p.rect(width/2.415f  ,12,2f*width/112f -4,4*height/120 -4);		
				//menu color6
				p.fill(colorArray[5]);
				p.rect(width/2.33f  ,12,2f*width/112f -4,4*height/120 -4);		
				//menu color7
				p.fill(colorArray[6]);
				p.rect(width/2.25f  ,12,2f*width/112f -4,4*height/120 -4);	
				//menu color 8
				p.fill(colorArray[7]);
				p.rect(width/2.174f  ,12,2f*width/112f -4,4*height/120 -4);
			
				p.fill(colorRed);
				p.rect(12*width/120 + width/120 + 17 +1 ,height/59 +1,width/120 -2,10 -2);
				p.fill(colorGreen);
				p.rect(12*width/120 + width/120 + 17 +1,height/47f +1 + 10 ,width/120-2,10 -2);
				p.fill(colorBlue);
				p.rect(12*width/120 + width/120 + 17 +1, height/58 + height/40  + 11, width/120-2,10 -2);
			
				p.fill(colorMix);
				p.rect(19.5f*width/120+1,12,2f*width/112f -3,4*height/120 -3);
				p.popMatrix();
				/**/
		      }
		    }
		);
	}
	
	public void changeColor()
	{
		colorBack = colorMix;
	}
	
	public void saveButton()
	{
		if(colorCounter < 8)
		{
			//colorArray[colorCounter] = colorMix;
			colorArray[colorCounter] = color(R,G,B,transparency);
			colorCounter++;
		}
		
	}
	
	public void undoButton()
	{
		if(colorCounter >=1)
		{
			colorArray[colorCounter-1] = 255;
			colorCounter--;
		}
	}
	
	public void clearButton()
	{
		for(int i = 0; i < 8; i++)
		{
			colorArray[i] = 255;
		}
		
		colorCounter = 0;
		
	}
	
	public void drawButton()
	{
		
		
		if(flagWindowColorFirstTime==false){
			if(drawWindowFlag)
			{
				String[] args = {"Painting"};
			
				windowForDraw = new drawingWindow(colorArray);
				PApplet.runSketch(args, windowForDraw);
				drawWindowFlag = !drawWindowFlag;
				drawButton.setVisible(false);
				passNewColors.setVisible(true);
				flagWindowColorFirstTime = true;
			}
		}
		else{
			if(drawWindowFlag){
					String[] args = {"Painting"};
					windowForDraw = new drawingWindow(colorArray);
					PApplet.runSketch(args, windowForDraw);
					drawWindowFlag = !drawWindowFlag;
					drawButton.setVisible(false);
					passNewColors.setVisible(true);
				}
		}
		
			
		
	}
	
	public void passColors()
	{
		windowForDraw.getNewColors(colorArray);
	}
	
	//----Functions for Color Group-------------------------FINISH---------------------------------------//
	
	//---Functions for Brain Group-------------------------START-----------------------------------------//
	
	public void brainGroupSetup()
	{
		controlP5.Group brainGroup = mainAppGroups.addGroup("brainGroup",width/120,(height-20)/60+20,width/2-2*width/120 );
		brainGroup.setBackgroundHeight(2*(height-20)/3 - 3*(height-20)/60);
		brainGroup.hideBar();
		brainGroup.setColorForeground(white);
		brainGroup.setColorLabel(white);
		brainGroup.setColorBackground(color(255,255,255,0));
		brainGroup.setBackgroundColor(color(255,255,255,0));
		
		//Brain arrows for navigation
		PImage leftImage = loadImage("left2.png");
			
		Button left = buttons.addButton("leftButton");
		left.setSize(2*width/120,3*height/120);
		left.setPosition(0,10*height/38);
		left.setImage(leftImage);
		left.setGroup(brainGroup);
		left.updateSize();
		
		PImage rightImage = loadImage("right2.png");
		
		Button right = buttons.addButton("rightButton");
		right.setSize(2*width/120,3*height/120);
		right.setPosition(53*width/120,10*height/38);
		right.setImage(rightImage);
		right.setGroup(brainGroup);
		right.updateSize();
		
		PImage upImage = loadImage("up2.png");
		
		Button up = buttons.addButton("upButton");
		up.setSize(2*width/120,2*height/120);
		up.setPosition(26.5f*width/120,0);
		up.setImage(upImage);
		up.setGroup(brainGroup);
		up.updateSize();
		
		PImage downImage = loadImage("down2.png");
		
		Button down = buttons.addButton("downButton");
		down.setSize(2*width/120,2*(height-20)/120);
		down.setPosition(26.5f*width/120,26f*height/50);
		down.setImage(downImage);
		down.setGroup(brainGroup);
		down.updateSize();
		
		groupShape = createShape(GROUP);
		
		model = loadShape("the_new_brain.obj");
		
		model.rotateX(PI);
		model.rotateY(PI);
		
		
	
		
		
		
		light1 = loadShape("the_new_light.obj");
		light2 = loadShape("the_new_light.obj");
		light3 = loadShape("the_new_light.obj");
		light4 = loadShape("the_new_light.obj");
		light5 = loadShape("the_new_light.obj");
		light6 = loadShape("the_new_light.obj");
		light7 = loadShape("the_new_light.obj");
		light8 = loadShape("the_new_light.obj");

		groupShape.addChild(model);
		groupShape.addChild(light1);
		groupShape.addChild(light2);
		groupShape.addChild(light3);
		groupShape.addChild(light4);
		groupShape.addChild(light5);
		groupShape.addChild(light6);
		groupShape.addChild(light7);
		groupShape.addChild(light8);
		
		
				//metopieos deksia
				light1.scale(0.25f);
				light1.translate(6.1f,-11.6f,4.6f);
				light1.rotateX(PI);
				light1.rotateX(-PI/7);
				light1.rotateZ(-PI/6);
				
				//vregmatikos deksia
				light2.scale(0.25f);
				light2.translate(-5.6f,-11.5f,5.5f);
				light2.rotateX(PI);
				light2.rotateZ(PI/8);
				light2.rotateX(-PI/10);
				
				//iniakos deksia
				light3.scale(0.25f);
				light3.translate(-11.2f,-4.9f,4f);
				light3.rotateZ(PI/2);
				light3.rotateX(PI/6);
				
				//krotafikos deksia
				light4.scale(0.25f);
				light4.translate(-1.1f, -2.85f,8.5f);
				light4.rotateX(PI/2);
				light4.rotateX(-PI/20);
				light4.rotateZ(-PI/30);
				
				//metopieos aristera
				light5.scale(0.25f);
				light5.translate(6.1f,-11.6f,-4.6f);
				light5.rotateX(PI);
				light5.rotateX(PI/5);
				light5.rotateZ(-PI/8);
				
				//vregmatikos aristera
				light6.scale(0.25f);
				light6.translate(-5.6f,-11.5f,-5.5f);
				light6.rotateX(PI);
				light6.rotateZ(PI/8);
				light6.rotateX(PI/8);
				
				//iniakos aristera
				light7.scale(0.25f);
				light7.translate(-11.2f,-4.8f,-4.4f);
				light7.rotateZ(PI/2);
				light7.rotateX(-PI/7);
				
				//krotafikos aristera
				light8.scale(0.25f);
				light8.translate(-1.1f, -2.85f,-8.5f);
				light8.rotateX(-PI/2);
				light8.rotateX(PI/20);
				light8.rotateZ(-PI/30);
		
		
	
				
				
				
				//Buttons
				buttonBrain = new ControlP5(this);
				buttonBrain.setColorForeground(colorGreyFore);
				buttonBrain.setColorBackground(colorGreyBack);
				buttonBrain.setColorActive(colorGreyActive2);
				buttonBrain.setColorCaptionLabel(black);
				
				Button changeColor = buttonBrain.addButton("threshold");
				changeColor.setGroup(brainGroup);
				changeColor.setLabel("threshold");
				changeColor.setPosition(width/2f - width/13f,6.8f*height/12);
				changeColor.setSize(8*width/130,2*height/60);
				
				
				
				//in mV
				DownThreshold = new float[16]; 
				UpThreshold = new float[16]; 
				for( int i=0; i< 16; i++){		    
				    DownThreshold[i] = -3f;
				    UpThreshold[i]   = 3f;
				  }
				
		
		brainGroup.addDrawable(new CDrawable(){
			public void draw(PGraphics p)
			{
				
				
				
				
				
				
				hint(ENABLE_DEPTH_TEST);
				p.lights();
				p.pushMatrix();
				p.translate(30*width/120,23*(height-20)/60 );
				p.scale(40);
				groupShape.draw(p);
				groupShape.rotateX(rotateAngleX);
				groupShape.rotateY(rotateAngleY);
				p.popMatrix();
				rotateAngleX = 0;
				rotateAngleY = 0;
				p.noLights();
				
				 //---------------------------Here checks values from channels in volts--------------------
				
	
				updateLights();
			
				hint(DISABLE_DEPTH_TEST);
				up.draw(p);
				down.draw(p);
				left.draw(p);
				right.draw(p);
				
			}
		});
		
		
	
	}

	
	public void threshold(){

		
		
		//here I open a new window for coherence/patient
		String[] args = {"threshold"};
		windowForDrawThreshold = new drawingWindowThreshold();
		PApplet.runSketch(args, windowForDrawThreshold);
		
		//opening the array with false values in sec
		String[] name = {"time"};
		windowForDrawTime = new drawingWindowTime();
		PApplet.runSketch(name, windowForDrawTime);
		
	}

	int color1 = color(125,125,125),color2  = color(125,125,125),color3  = color(125,125,125),color4  = color(125,125,125),color5  = color(125,125,125),color6  = color(125,125,125),color7  = color(125,125,125),color8  = color(125,125,125);
	public void updateLights(){
		
		thisTime = millis();
		
		
	
		
		
		if(flagForDraw2 == true){
			flagForDraw2 =false;
			int start,n;
			if( flagBuffer == 1 ){
                start =  0;
                n     = 250;
                
		                if( flagFilterUsed1 == false){
		                	flagFilterUsed1 = true;
			                 for(  int j = 0; j < channel_number; j++){  
			                 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
			                 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass		                 
			                 
			                 filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
		                	 filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
		                	 filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
		                	 filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
		                	 filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
		                	 
		                	 filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
			                 }
			                 println("2A");
			             	}
		                
               }
              else if( flagBuffer ==2 ) {          	 
                start = 250;
                n     = 500;
		                if( flagFilterUsed2 == false){
		                	flagFilterUsed2 = true;
			                 for(  int j = 0; j < channel_number; j++){  
			                 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
			                 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass
			                
			                 filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
		                	 filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
		                	 filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
		                	 filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
		                	 filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
				             
		                	 filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
			                 }
			                 println("2B");
			             	}
		                
              }
               else {
            	   start = 500;
	               n     = 750;
	            	   if(flagFilterUsed3 == false){
	            		   flagFilterUsed3 = true;
	            		   for(  int j = 0; j < channel_number; j++){  
	  		                 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
	  		                 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass
	  		              
	  		                 filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
	                		 filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
	                		 filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
	                	 	 filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
	                	 	 filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
			                  
	                	 	 filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
	  		                 }
	            		   println("2C");
		             	}
	            	   
   				}
                  

			
			
	
		 	//loop for each light
		 	for(int j=0;j<8;j++){	 				
						if(j==0){
							//Light 1
							//Right frontal lobe 
							//metopieos deksia FP2 = 1,F4 = 9,F8 = 11	
							
							
							
							  if(( lightFlag[1] == false ) || ( lightFlag[9] == false ) || ( lightFlag[11] == false ) ){				  
								  		color1 = color(255,0,0);						  		
									}							 
							  else  //IF VALUE IS VALID
							  {
									color1 = color(0,255,0);
							  }				
							  light1.setFill(color1);					  
						}
						else if(j==1){
							//Light 2							
							//Right parietal lobe & central lobe C3,P3
							//vregmatikos deksia C4 =3 ,P4 =15			
							
						
							  if(( lightFlag[3] == false ) || ( lightFlag[15] == false ) ){				  
							  			color2 = color(255,0,0);						  		
								}		  
							  else  //IF VALUE IS VALID
							  {
									    color2 = color(0,255,0);
							  }				
							light2.setFill(color2);
						}	
						else if(j==2){				
								//Light 3
								//Right Occipital Lobe 
								//iniakos deksia O1 = 6
							
						
								if( lightFlag[6] == false ){		
									color3 = color(255,0,0);
								}
								 else  //IF VALUE IS VALID
								 	{
									    color3 = color(0,255,0);
							 		}				
									 light3.setFill(color3);
								}
					   else if( j ==3 ){
								//Light 4	
								//Right Temporal lobe
								//krotafikos deksia T3 = 12,T5 = 4
						   	
							
							  			   						   
						   
							   if( (lightFlag[12] == false) || ( lightFlag[4] == false )){
								   color4 = color(255,0,0);
							    }							  
							    else  //IF VALUE IS VALID
							    {
									    color4 = color(0,255,0);
							    }				
									 light4.setFill(color4);
							}
					   else if( j == 4 ){
								//Light 5	
								//Left frontal lobe 
								//metopieos aristera FP1 = 0,F3 = 8,F7 = 10							
							    
								if( (lightFlag[0] == false) || ( lightFlag[8] == false ) || ( lightFlag[10] == false )){
									   color5 = color(255,0,0);
								}
							    else  //IF VALUE IS VALID
							    {
									    color5 = color(0,255,0);
							    }				
								 light5.setFill(color5);
						}
						else if( j == 5){
								//Light 6	
								//Left parietal lobe 
								//vregmatikos aristera C4 =2 ,P4 =14				
									
									if( (lightFlag[2] == false) || ( lightFlag[14] == false )){
										color6 = color(255,0,0);
									}
								    else  //IF VALUE IS VALID
								    {
										color6 = color(0,255,0);
								    }				
								 light6.setFill(color6);
						}
						else if( j == 6){
								//Light 7	
								//Left Occipital Lobe 
								//iniakos aristera O2 = 7
					
								
								if((lightFlag[7] == false)){
									color7 = color(255,0,0);
								}
							    else  //IF VALUE IS VALID
							    {
									    color7 = color(0,255,0);
							    }			
										 light7.setFill(color7);
								}
						else if( j == 7){
								//Light 8
								//Left Temporal lobe
								//krotafikos aristera T4 = 13,T6 = 5
							
						
								if((lightFlag[13] == false) || ( lightFlag[5] == false )){
									color8 = color(255,0,0);
								}
							    else  //IF VALUE IS VALID
							    {
									    color8 = color(0,255,0);
							    }				
								   light8.setFill(color8);
						}
		 		}//for j loop
	 		
}//if flagForDraw is true
		else //CHANNEL IS NOT ACTIVE
		{
			light1.setFill(color1);
			light2.setFill(color2);
			light3.setFill(color3);
			light4.setFill(color4);
			light5.setFill(color5);
			light6.setFill(color6);
			light7.setFill(color7);
			light8.setFill(color8);
		}
		//println("wht2");
	}
	
	
	//Rotate brain left
	public void leftButton()
	{

		rotateAngleY = -0.1f;
		
	}
	
	//Rotate brain right
	public void rightButton()
	{
		rotateAngleY = 0.1f;
	}
	
	//Rotate brain up
	public void upButton()
	{
		rotateAngleX = 0.1f;
	}
	
	//rotate brain down
	public void downButton()
	{
		rotateAngleX = -0.1f;
	}
	
	//---Functions for Brain Group-------------------------FINISH-----------------------------------------//
	
	//for recording
	Minim minimWav;
	AudioInput in;
	AudioRecorder recorder;
	
	// for playing back
	AudioOutput out;
	FilePlayer player;
	
	public void soundGroupSetup()
	  {
	    myGroup = new ControlP5(this);
	    soundToggle = new ControlP5(this);
	    soundLabel = new ControlP5(this);
	    soundButtons = new ControlP5(this);
	    soundRadioButtons = new ControlP5(this);
	    soundNumberBox = new ControlP5(this);
	    
	    myMinim = new Minim(this);
	    
	    for(int i=0; i<8; i++)
	    {
	      channelSoundFlag[i] = true;
	      channelOctave[i] = 4;
	      channelVolume[i] = 0;
	    }
	    
	    
	    for(int i=0; i<16; i++){
	    	 for(int j=0; j<128; j++){
	    	chooseNoteSpectrumSum[i][j] = 0f;
	 	    }
	    	 chooseNoteSpectrumSumCounter[i] = 0;
	    }
	    
	    initializePianoArray();
	    
	   
	    
	    
	    
	    minimWav = new Minim(this);
	    // get a stereo line-in: sample buffer length of 2048
	    // default sample rate is 44100, default bit depth is 16
	    in = minimWav.getLineIn(Minim.STEREO, 2048);
	    
	    // create an AudioRecorder that will record from in to the filename specified.
	    // the file will be located in the sketch's main folder.
	    recorder = minimWav.createRecorder(in, "myrecording.wav");
	    
	    // get an output we can playback the recording on
	    out = minimWav.getLineOut( Minim.STEREO );    
	    textFont(createFont("Arial", 12));
	    
	    
	    
	    //Create of the soundGroup
	    controlP5.Group soundGroup = myGroup.addGroup("soundGroup",width/120,height - (height/3 - height/8) - 10,width/2-2*width/120);
	    soundGroup.setBackgroundHeight(height/3 - height/8);
	    soundGroup.hideBar();
	    soundGroup.setColorForeground(black);
	    soundGroup.setColorLabel(white);
	    soundGroup.setColorBackground(greyColor);
	    soundGroup.setBackgroundColor(greyTr);

	    //Draw loop of soundGroup
	    soundGroup.addDrawable(new CDrawable(){
	      
	      public void draw(PGraphics p)
	      {
	    	  	
	  
	        if((( playbackLiveFlag == false) && (state == true))|| ((playbackLiveFlag == true)&&(playbackTrigger == true))){
	        	
	        	 //EDO KALOUSA TIN fillDataArray GIA TA DEDOMENA TOY BUFFER IXOY -- PIO KATO THA DEIS POY SOY LEO NA TA PERNEIS APO SERIAL EVENT
		        fillDataArray();
	        if(millis() >= step && soundIsPlaying)
	        {  
	        	
	        	
	        	 for(int i = 0; i < 8; i++)
		   	      {
	        		 
	        		 if(i==0){ 
	 	    			meanBuffer[i] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/3;
	 	    		}
	 	    		else if( i == 1){    			
	 	    			meanBuffer[i]= (maxAmp[1] + maxAmp[9] + maxAmp[11])/3;	    		
	 	    		}
	 	    		else if( i == 2){    			
	 	    			meanBuffer[i] = (maxAmp[2] + maxAmp[14])/2;	    		
	 	    		}
	 	    		else if( i == 3){    			
	 	    			meanBuffer[i] = (maxAmp[3] + maxAmp[15])/2;	    		
	 	    		}
	 	    		else if( i == 4){    			
	 	    			meanBuffer[i]= (maxAmp[4] + maxAmp[12])/2;	    		
	 	    		}
	 	    		else if( i == 5){    			
	 	    			meanBuffer[i] = (maxAmp[5] + maxAmp[13])/2;	    		
	 	    		}
	 	    		else if( i == 6){    			
	 	    			meanBuffer[i] = maxAmp[6] ;	    		
	 	    		}
	 	    		else if( i == 7){    			
	 	    			meanBuffer[i] = maxAmp[7] ;	    		
	 	    		}	    		
		   	      }
	        	 findMean(numberOfValuesForMean);
	        	
	          	octaveManager();
	            
	          	for(int i =0; i<8;i++)
		          {
		            print(meanBuffer[i], " ");
		          }
	          println(" ");
	          println(musicDataBufferCounter);  
	          println(" ");
	          for(int k = 0; k < 8; k++)
	          {
	        	  
	            playSound(k);
           
	          }
	          
	            counterMidi = counterMidi + 1f;
	            score.addChord(counterMidi, pitches,60,4);
	            
	          println(" ");
	          println(" ");
	          step = step + soundSpeedValue;
	        }
	        }
	      }
	    });
	    
	    //Start - Stop buttons START HERE
	    soundButtons.setColorForeground(colorGreyFore);
	    soundButtons.setColorBackground(colorGreyBack);
	    soundButtons.setColorActive(colorGreyActive2);
	    soundButtons.setColorCaptionLabel(black);
	    
	    startSound = soundButtons.addButton("startSound");
	    startSound.setGroup(soundGroup);
	    startSound.setLabel("Start Sound");
	    startSound.setPosition(6.8f*width/82,13.6f*(height)/87);
	    startSound.setSize(8*width/120,2*height/60);
	    
	    stopSound = soundButtons.addButton("stopSound");
	    stopSound.setGroup(soundGroup);
	    stopSound.setLabel("Stop Sound");
	    stopSound.setPosition(6.8f*width/82,13.6f*(height)/87);
	    stopSound.setSize(8*width/120,2*height/60);
	    stopSound.setVisible(false);
	    
	    //MIDI BUTTON
	    buttonMIDI = soundButtons.addButton("buttonMIDI");
	    buttonMIDI .setGroup(soundGroup);
	    buttonMIDI .setLabel("Save MIDI");
	    buttonMIDI .setPosition(6.8f*width/82,2f*(height)/85);
	    buttonMIDI .setSize(8*width/120,2*height/60);
	    
	    
	    
	    
	    //RECORDING BUTTON START HERE
	    startRecording = soundButtons.addButton("startRecording");
	    startRecording .setGroup(soundGroup);
	    startRecording .setLabel("Start Recording");
	    startRecording .setPosition(6.8f*width/82,5.9f*(height)/87);
	    startRecording .setSize(8*width/120,2*height/60);
	    
	    stopRecording = soundButtons.addButton("stopRecording");
	    stopRecording.setGroup(soundGroup);
	    stopRecording.setLabel("Stop Recording");
	    stopRecording.setPosition(6.8f*width/82,5.9f*(height)/87);
	    stopRecording.setSize(8*width/120,2*height/60);
	    stopRecording.setVisible(false);
	    
	    //SAVE RECORDING START HERE 
	    saveRecording = soundButtons.addButton("saveRecording");
	    saveRecording.setGroup(soundGroup);
	    saveRecording.setLabel("Save Recording");
	    saveRecording.setPosition(6.8f*width/82,9.7f*(height)/87);
	    saveRecording.setSize(8*width/120,2*height/60);
	   // saveRecording.setVisible(false);
	    
	    //SLIDERS START HERE
	    soundOptions = new ControlP5(this);
	    soundOptions.setColorForeground(greyForeSound);
	    soundOptions.setColorBackground(greyBackSound);
	    soundOptions.setColorActive(greyActiveSound);
	    soundOptions.setColorCaptionLabel(black);
	    
	    speedOfSound =  soundOptions.addSlider("speedOfSound");
	    speedOfSound.setGroup(soundGroup);
	    speedOfSound.setPosition(width/120,height/43f);
	    speedOfSound.setWidth(2*width/160);
	    speedOfSound.setHeight(20*height/120);
	    speedOfSound.setRange(100, 5000);
	    speedOfSound.setValue(1000);
	    speedOfSound.setNumberOfTickMarks(50);
	    speedOfSound.showTickMarks(false);
	    speedOfSound.setCaptionLabel("Speed");
	    speedOfSound.setColorValue(0);
	    
	    meanSampleNumber =  soundOptions.addSlider("meanSampleNumber");
	    meanSampleNumber.setGroup(soundGroup);
	    meanSampleNumber.setPosition(7*width/145,height/43f);
	    meanSampleNumber.setWidth(2*width/160);
	    meanSampleNumber.setHeight(20*height/120);
	    meanSampleNumber.setRange(1, 50);
	    meanSampleNumber.setValue(25);
	    meanSampleNumber.setNumberOfTickMarks(50);
	    meanSampleNumber.showTickMarks(false);
	    meanSampleNumber.setCaptionLabel("Mean");
	    meanSampleNumber.setColorValue(0);
	    
	    //TOGGLES (their Text labels too) AND RADIO BUTTONS  START HERE 
	    
	    soundNumberBox.setColorForeground(colorGreyFore);
	    soundNumberBox.setColorBackground(colorGreyBack);
	    soundNumberBox.setColorActive(colorGreyFore);
	    soundNumberBox.setColorCaptionLabel(black);
	    
	    //For channel 1
	    chOO1 = soundToggle.addToggle("toggleChannelOne");
	    chOO1.setGroup(soundGroup);
	    chOO1.setPosition(29.2f*width/120,1.7f*height/80); //<------
	    chOO1.setWidth(3*width/120);
	    chOO1.setHeight((int)(height/61));
	    chOO1.setMode(ControlP5.SWITCH);
	    chOO1.setValue(true);
	    chOO1.setColorActive(toggleON);
	    chOO1.setColorCaptionLabel(black);
	    chOO1.setColorBackground(greyBackSound);
	    chOO1.setLabelVisible(false);
	    
	    radio1 = soundRadioButtons.addRadioButton("radioButton1");
	    radio1.setGroup(soundGroup);
	    radio1.setPosition(29.5f*width/120,1.7f*height/80); //<-------
	    radio1.setSize((int)(width/120),(int)(height/60));
	    radio1.setItemsPerRow(7);
	    radio1.setSpacingColumn((int)2*width/120);
	    radio1.setColorForeground(greyForeSound);
	    radio1.setColorBackground(greyBackSound);
	    radio1.setColorActive(toggleON);
	    radio1.setColorLabel(black);
	    radio1.addItem("r1t1",1);
	    radio1.addItem("r1t2",2);
	    radio1.addItem("r1t3",3);
	    radio1.addItem("r1t4",4);
	    radio1.addItem("r1t5",5);
	    radio1.addItem("r1t6",6);
	    radio1.addItem("r1t7",7);
	    for(int i=0; i<7; i++) 
	    {
	      radio1.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
	      radio1.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio1.activate(3);
	    
	    chBox1 = soundNumberBox.addNumberbox("channelBoxOne");
	    chBox1.setGroup(soundGroup);
	    chBox1.setPosition(51.75f*width/120,1.7f*height/80);
	    chBox1.setSize((int)5*width/120,(int)(height/60));
	    chBox1.setCaptionLabel("");
	    chBox1.setMin(0);
	    chBox1.setMax(100);
	    chBox1.setScrollSensitivity(1.0f);
	    chBox1.setMultiplier(1);
	    chBox1.setDecimalPrecision(0);
	    chBox1.setValue(50f);
	    
	    chLabel1 = soundLabel.addLabel("labelChannelOne");
	    chLabel1.setText("LT FRONTAL");
	    chLabel1.setGroup(soundGroup);
	    chLabel1.setPosition(20*width/128, 1.8f*height/80); //<-------
	    chLabel1.setColor(black);
	    
	    //For channel 2
	    chOO2 = soundToggle.addToggle("toggleChannelTwo");
	    chOO2.setGroup(soundGroup);
	    chOO2.setPosition(29.2f*width/120,3.4f*height/80); //<-----
	    chOO2.setWidth(3*width/120);
	    chOO2.setHeight((int)(height/60));
	    chOO2.setMode(ControlP5.SWITCH);
	    chOO2.setValue(true);
	    chOO2.setColorActive(toggleON);
	    chOO2.setColorCaptionLabel(black);
	    chOO2.setColorBackground(greyBackSound);
	    chOO2.setLabelVisible(false);
	    
	    radio2 = soundRadioButtons.addRadioButton("radioButton2");
	    radio2.setGroup(soundGroup);
	    radio2.setPosition(29.5f*width/120,3.4f*height/80);//<-------
	    radio2.setSize((int)(width/120),(int)(height/60));
	    radio2.setItemsPerRow(7);
	    radio2.setSpacingColumn((int)2*width/120);
	    radio2.setColorForeground(greyForeSound);
	    radio2.setColorBackground(greyBackSound);
	    radio2.setColorActive(toggleON);
	    radio2.setColorLabel(black);
	    radio2.addItem("r2t1",1);
	    radio2.addItem("r2t2",2);
	    radio2.addItem("r2t3",3);
	    radio2.addItem("r2t4",4);
	    radio2.addItem("r2t5",5);
	    radio2.addItem("r2t6",6);
	    radio2.addItem("r2t7",7);
	    for(int i=0; i<7; i++)
	    {
	    	radio2.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio2.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio2.activate(3);
	    
	    chBox2 = soundNumberBox.addNumberbox("channelBoxTwo");
	    chBox2.setGroup(soundGroup);
	    chBox2.setPosition(51.75f*width/120,3.4f*height/80);
	    chBox2.setSize((int)5*width/120,(int)(height/60));
	    chBox2.setCaptionLabel("");
	    chBox2.setMin(0);
	    chBox2.setMax(100);
	    chBox2.setScrollSensitivity(1.0f);
	    chBox2.setMultiplier(1);
	    chBox2.setDecimalPrecision(0);
	    chBox2.setValue(50f);
	    
	    chLabel2 = soundLabel.addLabel("labelChannelTwo");
	    chLabel2.setText("RT FRONTAL");
	    chLabel2.setGroup(soundGroup);
	    chLabel2.setPosition(20*width/128,3.5f*height/80); //<-----
	    chLabel2.setColor(black);
	    
	    //For channel 3
	    chOO3 = soundToggle.addToggle("toggleChannelThree");
	    chOO3.setGroup(soundGroup);
	    chOO3.setPosition(29.2f*width/120,5.2f*height/81); //<--------
	    chOO3.setWidth(3*width/120);
	    chOO3.setHeight((int)(height/60));
	    chOO3.setMode(ControlP5.SWITCH);
	    chOO3.setValue(true);
	    chOO3.setColorActive(toggleON);
	    chOO3.setColorCaptionLabel(black);
	    chOO3.setColorBackground(greyBackSound);
	    chOO3.setLabelVisible(false);
	    
	    radio3 = soundRadioButtons.addRadioButton("radioButton3");
	    radio3.setGroup(soundGroup);
	    radio3.setPosition(29.5f*width/120,5.2f*height/81); //<------
	    radio3.setSize((int)(width/120),(int)(height/60));
	    radio3.setItemsPerRow(7);
	    radio3.setSpacingColumn((int)2*width/120);
	    radio3.setColorForeground(greyForeSound);
	    radio3.setColorBackground(greyBackSound);
	    radio3.setColorActive(toggleON);
	    radio3.setColorLabel(black);
	    radio3.addItem("r3t1",1);
	    radio3.addItem("r3t2",2);
	    radio3.addItem("r3t3",3);
	    radio3.addItem("r3t4",4);
	    radio3.addItem("r3t5",5);
	    radio3.addItem("r3t6",6);
	    radio3.addItem("r3t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio3.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio3.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio3.activate(3);
	    
	    chBox3 = soundNumberBox.addNumberbox("channelBoxThree");
	    chBox3.setGroup(soundGroup);
	    chBox3.setPosition(51.75f*width/120,5.2f*height/81);
	    chBox3.setSize((int)5*width/120,(int)(height/60));
	    chBox3.setCaptionLabel("");
	    chBox3.setMin(0);
	    chBox3.setMax(100);
	    chBox3.setScrollSensitivity(1.0f);
	    chBox3.setMultiplier(1);
	    chBox3.setDecimalPrecision(0);
	    chBox3.setValue(50f);
	    
	    chLabel3 = soundLabel.addLabel("labelChannelThree");
	    chLabel3.setText("LT CENTRAL/PARIETAL");
	    chLabel3.setGroup(soundGroup);
	    chLabel3.setPosition(20*width/128,5.3f*height/81); //<--------
	    chLabel3.setColor(black);
	
	    
	    //For channel 4
	    chOO4 = soundToggle.addToggle("toggleChannelFour");
	    chOO4.setGroup(soundGroup);
	    chOO4.setPosition(29.2f*width/120,6.9f*height/80); //<------
	    chOO4.setWidth(3*width/120);
	    chOO4.setHeight((int)(height/60));
	    chOO4.setMode(ControlP5.SWITCH);
	    chOO4.setValue(true);
	    chOO4.setColorActive(toggleON);
	    chOO4.setColorCaptionLabel(black);
	    chOO4.setColorBackground(greyBackSound);
	    chOO4.setLabelVisible(false);
	    
	    radio4 = soundRadioButtons.addRadioButton("radioButton4");
	    radio4.setGroup(soundGroup);
	    radio4.setPosition(29.5f*width/120,6.9f*height/80); //<--------
	    radio4.setSize((int)(width/120),(int)(height/60));
	    radio4.setItemsPerRow(7);
	    radio4.setSpacingColumn((int)2*width/120);
	    radio4.setColorForeground(greyForeSound);
	    radio4.setColorBackground(greyBackSound);
	    radio4.setColorActive(toggleON);
	    radio4.setColorLabel(black);
	    radio4.addItem("r4t1",1);
	    radio4.addItem("r4t2",2);
	    radio4.addItem("r4t3",3);
	    radio4.addItem("r4t4",4);
	    radio4.addItem("r4t5",5);
	    radio4.addItem("r4t6",6);
	    radio4.addItem("r4t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio4.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio4.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio4.activate(3);
	    
	    chBox4 = soundNumberBox.addNumberbox("channelBoxFour");
	    chBox4.setGroup(soundGroup);
	    chBox4.setPosition(51.75f*width/120,6.9f*height/80);
	    chBox4.setSize((int)5*width/120,(int)(height/60));
	    chBox4.setCaptionLabel("");
	    chBox4.setMin(0);
	    chBox4.setMax(100);
	    chBox4.setScrollSensitivity(1.0f);
	    chBox4.setMultiplier(1);
	    chBox4.setDecimalPrecision(0);
	    chBox4.setValue(50f);
	    
	    chLabel4 = soundLabel.addLabel("labelChannelFour");
	    chLabel4.setText("RT CENTRAL/PARIETAL");
	    chLabel4.setGroup(soundGroup);
	    chLabel4.setPosition(20*width/128,7f*height/80); //<-------
	    chLabel4.setColor(black);
	    
	    //For channel 5
	    chOO5 = soundToggle.addToggle("toggleChannelFive");
	    chOO5.setGroup(soundGroup);
	    chOO5.setPosition(29.2f*width/120,8.7f*height/81); //<-------
	    chOO5.setWidth(3*width/120);
	    chOO5.setHeight((int)(height/60));
	    chOO5.setMode(ControlP5.SWITCH);
	    chOO5.setValue(true);
	    chOO5.setColorActive(toggleON);
	    chOO5.setColorCaptionLabel(black);
	    chOO5.setColorBackground(greyBackSound);
	    chOO5.setLabelVisible(false);
	    
	    radio5 = soundRadioButtons.addRadioButton("radioButton5");
	    radio5.setGroup(soundGroup);
	    radio5.setPosition(29.5f*width/120,8.7f*height/81); //<------------
	    radio5.setSize((int)(width/120),(int)(height/60));
	    radio5.setItemsPerRow(7);
	    radio5.setSpacingColumn((int)2*width/120);
	    radio5.setColorForeground(greyForeSound);
	    radio5.setColorBackground(greyBackSound);
	    radio5.setColorActive(toggleON);
	    radio5.setColorLabel(black);
	    radio5.addItem("r5t1",1);
	    radio5.addItem("r5t2",2);
	    radio5.addItem("r5t3",3);
	    radio5.addItem("r5t4",4);
	    radio5.addItem("r5t5",5);
	    radio5.addItem("r5t6",6);
	    radio5.addItem("r5t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio5.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio5.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio5.activate(3);
	    
	    chBox5 = soundNumberBox.addNumberbox("channelBoxFive");
	    chBox5.setGroup(soundGroup);
	    chBox5.setPosition(51.75f*width/120,8.7f*height/81);
	    chBox5.setSize((int)5*width/120,(int)(height/60));
	    chBox5.setCaptionLabel("");
	    chBox5.setMin(0);
	    chBox5.setMax(100);
	    chBox5.setScrollSensitivity(1.0f);
	    chBox5.setMultiplier(1);
	    chBox5.setDecimalPrecision(0);
	    chBox5.setValue(50f);
	    
	    chLabel5 = soundLabel.addLabel("labelChannelFive");
	    chLabel5.setText("LT TEMPORAL");
	    chLabel5.setGroup(soundGroup);
	    chLabel5.setPosition(20*width/128,8.75f*height/80); //<---------
	    chLabel5.setColor(black);
	    
	    //For channel 6
	    chOO6 = soundToggle.addToggle("toggleChannelSix");
	    chOO6.setGroup(soundGroup);
	    chOO6.setPosition(29.2f*width/120,10.4f*height/80); //<------
	    chOO6.setWidth(3*width/120);
	    chOO6.setHeight((int)(height/60));
	    chOO6.setMode(ControlP5.SWITCH);
	    chOO6.setValue(true);
	    chOO6.setColorActive(toggleON);
	    chOO6.setColorCaptionLabel(black);
	    chOO6.setColorBackground(greyBackSound);
	    chOO6.setLabelVisible(false);
	    
	    radio6 = soundRadioButtons.addRadioButton("radioButton6");
	    radio6.setGroup(soundGroup);
	    radio6.setPosition(29.5f*width/120,10.4f*height/80); //<---------
	    radio6.setSize((int)(width/120),(int)(height/60));
	    radio6.setItemsPerRow(7);
	    radio6.setSpacingColumn((int)2*width/120);
	    radio6.setColorForeground(greyForeSound);
	    radio6.setColorBackground(greyBackSound);
	    radio6.setColorActive(toggleON);
	    radio6.setColorLabel(black);
	    radio6.addItem("r6t1",1);
	    radio6.addItem("r6t2",2);
	    radio6.addItem("r6t3",3);
	    radio6.addItem("r6t4",4);
	    radio6.addItem("r6t5",5);
	    radio6.addItem("r6t6",6);
	    radio6.addItem("r6t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio6.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio6.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio6.activate(3);
	    
	    chBox6 = soundNumberBox.addNumberbox("channelBoxSix");
	    chBox6.setGroup(soundGroup);
	    chBox6.setPosition(51.75f*width/120,10.4f*height/80);
	    chBox6.setSize((int)5*width/120,(int)(height/60));
	    chBox6.setCaptionLabel("");
	    chBox6.setMin(0);
	    chBox6.setMax(100);
	    chBox6.setScrollSensitivity(1.0f);
	    chBox6.setMultiplier(1);
	    chBox6.setDecimalPrecision(0);
	    chBox6.setValue(50f);
	    
	    chLabel6 = soundLabel.addLabel("labelChannelSix");
	    chLabel6.setText("RT TEMPORAL");
	    chLabel6.setGroup(soundGroup);
	    chLabel6.setPosition(20*width/128,10.5f*height/80); //<-----
	    chLabel6.setColor(black);
	    
	    //For channel 7
	    chOO7 = soundToggle.addToggle("toggleChannelSeven");
	    chOO7.setGroup(soundGroup);
	    chOO7.setPosition(29.2f*width/120,12.2f*height/80); //<-------
	    chOO7.setWidth(3*width/120);
	    chOO7.setHeight((int)(height/60));
	    chOO7.setMode(ControlP5.SWITCH);
	    chOO7.setValue(true);
	    chOO7.setColorActive(toggleON);
	    chOO7.setColorCaptionLabel(black);
	    chOO7.setColorBackground(greyBackSound);
	    chOO7.setLabelVisible(false);
	    
	    radio7 = soundRadioButtons.addRadioButton("radioButton7");
	    radio7.setGroup(soundGroup);
	    radio7.setPosition(29.5f*width/120,12.2f*height/80); //<--------
	    radio7.setSize((int)(width/120),(int)(height/60));
	    radio7.setItemsPerRow(7);
	    radio7.setSpacingColumn((int)2*width/120);
	    radio7.setColorForeground(greyForeSound);
	    radio7.setColorBackground(greyBackSound);
	    radio7.setColorActive(toggleON);
	    radio7.setColorLabel(black);
	    radio7.addItem("r7t1",1);
	    radio7.addItem("r7t2",2);
	    radio7.addItem("r7t3",3);
	    radio7.addItem("r7t4",4);
	    radio7.addItem("r7t5",5);
	    radio7.addItem("r7t6",6);
	    radio7.addItem("r7t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio7.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio7.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio7.activate(3);
	    
	    chBox7 = soundNumberBox.addNumberbox("channelBoxSeven");
	    chBox7.setGroup(soundGroup);
	    chBox7.setPosition(51.75f*width/120,12.2f*height/80);
	    chBox7.setSize((int)5*width/120,(int)(height/60));
	    chBox7.setCaptionLabel("");
	    chBox7.setMin(0);
	    chBox7.setMax(100);
	    chBox7.setScrollSensitivity(1.0f);
	    chBox7.setMultiplier(1);
	    chBox7.setDecimalPrecision(0);
	    chBox7.setValue(50f);
	    
	    chLabel7 = soundLabel.addLabel("labelChannelSeven");
	    chLabel7.setText("LT OCCIPITAL");
	    chLabel7.setGroup(soundGroup);
	    chLabel7.setPosition(20*width/128,12.3f*height/80); //<-------
	    chLabel7.setColor(black);
	    
	    //For channel 8
	    chOO8 = soundToggle.addToggle("toggleChannelEight");
	    chOO8.setGroup(soundGroup);
	    chOO8.setPosition(29.2f*width/120,13.95f*height/80); //<--------
	    chOO8.setWidth(3*width/120);
	    chOO8.setHeight((int)(height/60));
	    chOO8.setMode(ControlP5.SWITCH);
	    chOO8.setValue(true);
	    chOO8.setColorActive(toggleON);
	    chOO8.setColorCaptionLabel(black);
	    chOO8.setColorBackground(greyBackSound);
	    chOO8.setLabelVisible(false);
	    
	    radio8 = soundRadioButtons.addRadioButton("radioButton8");
	    radio8.setGroup(soundGroup);
	    radio8.setPosition(29.5f*width/120,13.95f*height/80); //<------
	    radio8.setSize((int)(width/120),(int)(height/60));
	    radio8.setItemsPerRow(7);
	    radio8.setSpacingColumn((int)2*width/120);
	    radio8.setColorForeground(greyForeSound);
	    radio8.setColorBackground(greyBackSound);
	    radio8.setColorActive(toggleON);
	    radio8.setColorLabel(black);
	    radio8.addItem("r8t1",1);
	    radio8.addItem("r8t2",2);
	    radio8.addItem("r8t3",3);
	    radio8.addItem("r8t4",4);
	    radio8.addItem("r8t5",5);
	    radio8.addItem("r8t6",6);
	    radio8.addItem("r8t7",7);
	    for(int i=0; i<7; i++) 
	    {
	    	radio8.getItem(i).setLabel(String.valueOf("  ")+String.valueOf(i+1));      
		    radio8.getItem(i).setPosition((i+1.6f)*28f ,0);
	      }
	    radio8.activate(3);
	    
	    chBox8 = soundNumberBox.addNumberbox("channelBoxEight");
	    chBox8.setGroup(soundGroup);
	    chBox8.setPosition(51.75f*width/120,13.95f*height/80);
	    chBox8.setSize((int)5*width/120,(int)(height/60));
	    chBox8.setCaptionLabel("");
	    chBox8.setMin(0);
	    chBox8.setMax(100);
	    chBox8.setScrollSensitivity(1.0f);
	    chBox8.setMultiplier(1);
	    chBox8.setDecimalPrecision(0);
	    chBox8.setValue(50f);
	    
	    chLabel8 = soundLabel.addLabel("labelChannelEight");
	    chLabel8.setText("RT OCCIPITAL");
	    chLabel8.setGroup(soundGroup);
	    chLabel8.setPosition(20*width/128,14.05f*height/80); //<-------
	    chLabel8.setColor(black);
	    
	    //Text labels for octave and volume section 
	    octaveHead = soundLabel.addLabel("PianoOctaves");
	    octaveHead.setText("PIANO OCTAVES");
	    octaveHead.setGroup(soundGroup);
	    octaveHead.setPosition(38.7f*width/120, 0.4f*(height-20)/62);
	    octaveHead.setColor(black);
	    
	    volumeHead = soundLabel.addLabel("volumeHead");
	    volumeHead.setText("VOLUME");
	    volumeHead.setGroup(soundGroup);
	    volumeHead.setPosition(52.5f*width/120,0.4f*(height-20)/62);
	    volumeHead.setColor(black);
	  }
	  
	  //This function loads all the 7 octaves samples
	  public void initializePianoArray()
	  {
	    pianoSamples = new AudioSample[8][12];
	    String[] notesName = {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"};
	    
	    for(int i=0; i<7; i++)
	    {
	      for(int j=0; j<12; j++)
	      {
	        pianoSamples[i][j] = myMinim.loadSample("octaves//octave " + (i+1) + "//" + notesName[j] +(i+1)+".wav",256);
	        if(pianoSamples[i][j] == null)
	        {
	          println("problem "+ notesName[j] + (i+1));
	        }
	      }
	      println("octave " + (i+1) + "loaded!");
	    }
	  }
	  
	  //Function for the button controller that starts the sound 
	  public void startSound()
	  {
	    stopSound.setVisible(true);
	    startSound.setVisible(false);
	    step = millis();
	    soundIsPlaying = !soundIsPlaying;
	  }
	  
	  //Function for the button controller that stops the sound 
	  public void stopSound()
	  {
	    startSound.setVisible(true);
	    stopSound.setVisible(false);
	    soundIsPlaying = !soundIsPlaying;
	  }
	  
	  
	  
	  public void buttonMIDI(){
	  
	    //stopRecording.setVisible(true);
	    //startRecording.setVisible(false);
	    //recorder.beginRecord();
		//  score.play();
	// score.writeMidiFile("C:/Users/Stelios/Documents/processing-3.1.1/libraries/soundcipher/examples/MidiFileWriter/arpeggio.mid");
	   // step = millis();
	   // soundIsPlaying = !soundIsPlaying;
		  score.play();
		  score.writeMidiFile("arpeggio.mid");
		//  println("ssssssssssssssssssssssss");
	  }
	  
	  
	  
	  //Function for the button controller that starts the recording
	  public void startRecording()
	  {
	    stopRecording.setVisible(true);
	    startRecording.setVisible(false);
	    recorder.beginRecord();

	   // step = millis();
	   // soundIsPlaying = !soundIsPlaying;
	  }
	  
	//Function for the button controller that stops the recording
	  public void stopRecording()
	  {
	    startRecording.setVisible(true);
	    stopRecording.setVisible(false);
	    recorder.endRecord();
	    
	   // step = millis();
	  //  soundIsPlaying = !soundIsPlaying;
	  }
	  
	//Function for the button controller that stops the recording
	  public void saveRecording()
	  {
		  if ( player != null )
		    {
		        player.unpatch( out );
		        player.close();
		    }
		 // 	saveFileRec();
		  	 
		    player = new FilePlayer( recorder.save() );
		/*    File ooo = new File("C:/Users/Stelios/proclipsing/ASDAT/recording.wav");
		    Path source = null;
		    source.resolve("C:/Users/Stelios/proclipsing/ASDAT/recording.wav");
		    Path newDir = null;
		    newDir.resolve(file_name);
		     
		  	  try {
				Files.copy(source, newDir.resolve(source.getFileName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		    player.patch( out );
		   // player.play();
	  }
	  
	  
	  
	  
	  public void saveFileRec(){
				 
			  try {
			    SwingUtilities. invokeLater(new Runnable() {
			      public void run() {
			    	  
			    	file_chooser.setDialogTitle("Save a file");  
			    	file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    	FileFilter filter = new FileNameExtensionFilter("Waveform Audio File Format (*.wav)", "wav");
			    	file_chooser.setFileFilter(filter);
			        int return_val = file_chooser.showSaveDialog(null);
			        
			        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
			        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
			        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
			        if ( return_val == JFileChooser.APPROVE_OPTION ) {
			        	
			        	File file = new File("rec.wav");
			        	BufferedWriter writer;
						try {
							writer = new BufferedWriter( new FileWriter( file.getAbsolutePath()+".wav"));
						
			          
			          file.getAbsolutePath();		          
			          file = file_chooser.getSelectedFile();		       
			          file_name = file.getAbsolutePath();   
			         // need = file.getAbsolutePath(); 
			         // AudioRecorder tempRecorder = minimWav.createRecorder(in, "uii.wav");
			          writer.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  
			          System.out.println(file_name);
			        } else {
			          file_name = "none";
			        }
			      }
			    }
			    );
			  }
			  catch (Exception e) {
			    e.printStackTrace();
			  }
			
	  }
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  //This slider controller changes 
	  public void speedOfSound()
	  {
	    soundSpeedValue = (int)(speedOfSound.getValue());
	  }
	  
	  //This slider controller sends the number of values that should be take part in mean calculation
	  public void meanSampleNumber()
	  {
	    numberOfValuesForMean = (int) meanSampleNumber.getValue();
	  }
	  
	  //This controller changes the ON/OFF state of channel 1
	  public void toggleChannelOne()
	  {
	    channelSoundFlag[0] = chOO1.getBooleanValue();
	    if(chOO1.getBooleanValue())
	    {
	      chOO1.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO1.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 1
	  public void channelBoxOne()
	  {
	    channelVolume[0] = -60+60*((int)chBox1.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 2
	  public void toggleChannelTwo()
	  {
	    channelSoundFlag[1] = chOO2.getBooleanValue();
	    if(chOO2.getBooleanValue())
	    {
	      chOO2.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO2.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 2
	  public void channelBoxTwo()
	  {
	    channelVolume[1] = -60+60*((int)chBox2.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 3
	  public void toggleChannelThree()
	  {
	    channelSoundFlag[2] = chOO3.getBooleanValue();
	    if(chOO3.getBooleanValue())
	    {
	      chOO3.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO3.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 3
	  public void channelBoxThree()
	  {
	    channelVolume[2] = -60+60*((int)chBox3.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 4
	  public void toggleChannelFour()
	  {
	    channelSoundFlag[3] = chOO4.getBooleanValue();
	    if(chOO4.getBooleanValue())
	    {
	      chOO4.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO4.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 4
	  public void channelBoxFour()
	  {
	    channelVolume[3] = -60+60*((int)chBox4.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 5
	  public void toggleChannelFive()
	  {
	    channelSoundFlag[4] = chOO5.getBooleanValue();
	    if(chOO5.getBooleanValue())
	    {
	      chOO5.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO5.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 5
	  public void channelBoxFive()
	  {
	    channelVolume[4] = -60+60*((int)chBox5.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 6
	  public void toggleChannelSix()
	  {
	    channelSoundFlag[5] = chOO6.getBooleanValue();
	    if(chOO6.getBooleanValue())
	    {
	      chOO6.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO6.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 6
	  public void channelBoxSix()
	  {
	    channelVolume[5] = -60+60*((int)chBox6.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 7
	  public void toggleChannelSeven()
	  {
	    channelSoundFlag[6] = chOO7.getBooleanValue();
	    if(chOO7.getBooleanValue())
	    {
	      chOO7.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO7.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 7
	  public void channelBoxSeven()
	  {
	    channelVolume[6] = -60+60*((int)chBox7.getValue())/100;
	  }
	  
	  //This controller changes the ON/OFF state of channel 8
	  public void toggleChannelEight()
	  {
	    channelSoundFlag[7] = chOO8.getBooleanValue();
	    if(chOO8.getBooleanValue())
	    {
	      chOO8.setColorActive(toggleON);
	    }
	    else
	    {
	      chOO8.setColorActive(toggleOFF);
	    }
	  }
	  
	  //This box controller changes the volume of channel 8
	  public void channelBoxEight()
	  {
	    channelVolume[7] = -60+60*((int)chBox8.getValue())/100;
	  }
	  
	  //The octave manager checks the octave that user chose for each channel and updates the results in draw function of the group
	  public void octaveManager()
	  {
	    
	    channelOctave[0] = (int)radio1.getValue();
	    channelOctave[1] = (int)radio2.getValue();
	    channelOctave[2] = (int)radio3.getValue();
	    channelOctave[3] = (int)radio4.getValue();
	    channelOctave[4] = (int)radio5.getValue();
	    channelOctave[5] = (int)radio6.getValue();
	    channelOctave[6] = (int)radio7.getValue();
	    channelOctave[7] = (int)radio8.getValue();
	    
	  }
	  
	  
	 
	  //Function that produces the sound from samples (all settings for each channel are used here)
	  public void playSound(int channel)
	  {
	    if(channelSoundFlag[channel])
	    {
	      if(meanBuffer[channel] > 0f && meanBuffer[channel] <= 2f)
	      {
	        pianoSamples[channelOctave[channel]-1][0].setGain(channelVolume[0]);
	        pianoSamples[channelOctave[channel]-1][0].trigger();
	        print("C" + (channelOctave[channel]) + " ");
	        
	        //soundCipher
	        pitches[channel] = 60;
	      //  float pScore = score.pcRandom(60, 60, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    
	      }
	      else if(meanBuffer[channel] > 2f && meanBuffer[channel] <= 7.4599245f)
	      {
	        pianoSamples[channelOctave[channel]-1][1].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][1].trigger();
	        print("Db" + (channelOctave[channel]) + " ");
	      //soundCipher
	        //float pScore = score.pcRandom(61, 61, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 61;
	      }
	      else if(meanBuffer[channel] > 7.4599245f && meanBuffer[channel] <= 11.469654f)
	      {
	        pianoSamples[channelOctave[channel]-1][2].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][2].trigger();
	        print("D" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(62, 62, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 62;
	      }
	      else if(meanBuffer[channel] > 11.469654f && meanBuffer[channel] <= 15.7177125f)
	      {
	        pianoSamples[channelOctave[channel]-1][3].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][3].trigger();
	        print("Eb" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(62, 62, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 63;
	      }
	      else if(meanBuffer[channel] > 15.7177125f && meanBuffer[channel] <= 20.2183695f)
	      {
	        pianoSamples[channelOctave[channel]-1][4].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][4].trigger();
	        print("E" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(63, 63, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 64;
	      }
	      else if(meanBuffer[channel] > 20.2183695f && meanBuffer[channel] <= 24.986745f)
	      {
	        pianoSamples[channelOctave[channel]-1][5].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][5].trigger();
	        print("F" + (channelOctave[channel]) + " ");
	      //soundCipher
	      //  float pScore = score.pcRandom(64, 64, score.CHROMATIC);
    	  //  score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 65;
	      }
	      else if(meanBuffer[channel] > 24.986745f && meanBuffer[channel] <= 30.0386205f)
	      {
	        pianoSamples[channelOctave[channel]-1][6].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][6].trigger();
	        print("Gb" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(65, 65, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 66;
	      }
	      else if(meanBuffer[channel] > 30.0386205f && meanBuffer[channel] <= 35.3909115f)
	      {
	        pianoSamples[channelOctave[channel]-1][7].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][7].trigger();
	        print("G" + (channelOctave[channel]) + " ");
	      //soundCipher
	        //float pScore = score.pcRandom(66, 66, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 67;
	      }
	      else if(meanBuffer[channel] > 35.3909115f && meanBuffer[channel] <= 41.0614785f)
	      {
	        pianoSamples[channelOctave[channel]-1][8].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][8].trigger();
	        print("Ab" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(67, 67, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 68;
	      }
	      else if(meanBuffer[channel] > 41.0614785f && meanBuffer[channel] <= 47.0692215f)
	      {
	        pianoSamples[channelOctave[channel]-1][9].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][9].trigger();
	        print("A" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(68, 68, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 69;
	      }
	      else if(meanBuffer[channel] > 47.0692215f && meanBuffer[channel] <= 53.4341745f)
	      {
	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][10].trigger();
	        print("Bb" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(69, 69, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 70;
    	  
	      }
	      else if(meanBuffer[channel]> 53.4341745f && meanBuffer[channel] <= 60.0f)
	      {
	        pianoSamples[channelOctave[channel]-1][11].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][11].trigger();
	        print("B" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(70, 70, score.CHROMATIC);
    	  //  score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 71;
	      }
	      else if(meanBuffer[channel]> 60.0f && meanBuffer[channel] <= 65.41f){
	    	    pianoSamples[channelOctave[channel]-1][0].setGain(channelVolume[channel]);
		        pianoSamples[channelOctave[channel]-1][0].trigger();
		        print("C" + (channelOctave[channel]) + " ");
		        pitches[channel] = 60;
	      }
	      else if(meanBuffer[channel] > 65.41 && meanBuffer[channel] <= 69.30f)
	      {
	        pianoSamples[channelOctave[channel]-1][1].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][1].trigger();
	        print("Db" + (channelOctave[channel]) + " ");
	      //soundCipher
	        //float pScore = score.pcRandom(61, 61, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 61;
	      }
	      else if(meanBuffer[channel] > 69.30f && meanBuffer[channel] <= 73.42f)
	      {
	        pianoSamples[channelOctave[channel]-1][2].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][2].trigger();
	        print("D" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(62, 62, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 62;
	      }
	      else if(meanBuffer[channel] > 73.42f && meanBuffer[channel] <= 77.78f)
	      {
	        pianoSamples[channelOctave[channel]-1][3].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][3].trigger();
	        print("Eb" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(62, 62, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 63;
	      }
	      else if(meanBuffer[channel] > 77.78f && meanBuffer[channel] <= 82.41f)
	      {
	        pianoSamples[channelOctave[channel]-1][4].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][4].trigger();
	        print("E" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(63, 63, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 64;
	      }
	      else if(meanBuffer[channel] > 82.41f&& meanBuffer[channel] <= 87.31f)
	      {
	        pianoSamples[channelOctave[channel]-1][5].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][5].trigger();
	        print("F" + (channelOctave[channel]) + " ");
	      //soundCipher
	      //  float pScore = score.pcRandom(64, 64, score.CHROMATIC);
    	  //  score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 65;
	      }
	      else if(meanBuffer[channel] > 87.31f && meanBuffer[channel] <= 92.50f)
	      {
	        pianoSamples[channelOctave[channel]-1][6].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][6].trigger();
	        print("Gb" + (channelOctave[channel]) + " ");
	      //soundCipher
	       // float pScore = score.pcRandom(65, 65, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
	        pitches[channel] = 66;
	      }
	      else if(meanBuffer[channel] > 92.50f && meanBuffer[channel] <= 98.00f)
	      {
	        pianoSamples[channelOctave[channel]-1][7].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][7].trigger();
	        print("G" + (channelOctave[channel]) + " ");
	      //soundCipher
	        //float pScore = score.pcRandom(66, 66, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 67;
	      }
	      else if(meanBuffer[channel] > 98.00f && meanBuffer[channel] <= 103.83f)
	      {
	        pianoSamples[channelOctave[channel]-1][8].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][8].trigger();
	        print("Ab" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(67, 67, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 68;
	      }
	      else if(meanBuffer[channel] > 103.83f && meanBuffer[channel] <= 110.00f)
	      {
	        pianoSamples[channelOctave[channel]-1][9].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][9].trigger();
	        print("A" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(68, 68, score.CHROMATIC);
    	    //score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 69;
	      }
	      else if(meanBuffer[channel] > 116.54f && meanBuffer[channel] <= 123.47f)
	      {
	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][10].trigger();
	        print("Bb" + (channelOctave[channel]) + " ");
	        //soundCipher
	       // float pScore = score.pcRandom(69, 69, score.CHROMATIC);
    	   // score.addNote(counterMidi, pScore, 60, 1);
    	    pitches[channel] = 70;
	      }
    	  else if(meanBuffer[channel] > 123.47f && meanBuffer[channel] <= 130.81f)
  	      {
  	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
  	        pianoSamples[channelOctave[channel]-1][10].trigger();
  	        print("C" + (channelOctave[channel]) + " ");
  	        //soundCipher
  	       // float pScore = score.pcRandom(69, 69, score.CHROMATIC);
      	   // score.addNote(counterMidi, pScore, 60, 1);
      	    pitches[channel] = 71;
	   
	      }
	      else
	      {
	    	  println(meanBuffer[channel]);
	        println("Invalid Hz value!");
	      }
	    }
	    else
	    {
	      println("Channel ", channel+1," is off!");
	    }
	  }
	  
	  //Function to fill the buffer for music, gets Hz values for each channel from serialEvent buffer
	  public void fillDataArray()
	  {
	    if(musicDataBufferCounter < 50)
	    {
	      for(int i=0; i<8;i++)
	      {
	        //EDO GEMIZEIS TON BUFFER TIS MOUSIKIS TIN PROTI FORA
	    	 if (   ( playbackLiveFlag == true) && ( playbackFlag == true)  &&  ((thisTime - startTime) >=1000) ){
	        
	    		if(i==0){ 
	    			musicDataBuffer[0][musicDataBufferCounter] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/3;
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][musicDataBufferCounter] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/3;	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][musicDataBufferCounter] = (maxAmp[2] + maxAmp[14])/2;	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][musicDataBufferCounter] = (maxAmp[3] + maxAmp[15])/2;	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][musicDataBufferCounter] = (maxAmp[4] + maxAmp[12])/2;	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][musicDataBufferCounter] = (maxAmp[5] + maxAmp[13])/2;	    		
	    		}
	    		else if( i == 6){    			
	    			musicDataBuffer[6][musicDataBufferCounter] = maxAmp[6] ;	    		
	    		}
	    		else if( i == 7){    			
	    			musicDataBuffer[7][musicDataBufferCounter] = maxAmp[7] ;	    		
	    		}	    		
	    		
	        }
	        else{
	        	if(i==0){ 
	    			musicDataBuffer[0][musicDataBufferCounter] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/3;
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][musicDataBufferCounter] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/3;	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][musicDataBufferCounter] = (maxAmp[2] + maxAmp[14])/2;	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][musicDataBufferCounter] = (maxAmp[3] + maxAmp[15])/2;	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][musicDataBufferCounter] = (maxAmp[4] + maxAmp[12])/2;	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][musicDataBufferCounter] = (maxAmp[5] + maxAmp[13])/2;	    		
	    		}
	    		else if( i == 6){    			
	    			musicDataBuffer[6][musicDataBufferCounter] = maxAmp[6] ;	    		
	    		}
	    		else if( i == 7){    			
	    			musicDataBuffer[7][musicDataBufferCounter] = maxAmp[7] ;	    		
	    		}	
	        }
	      }
	      musicDataBufferCounter++;
	      //println(" ");
	      //println(" ");
	    }
	    else
	    {
	    	
	    if (   ( playbackLiveFlag == true) && ( playbackFlag == true)  &&  ((thisTime - startTime) >=1000) ){
	      for(int i=0; i< 8;i++)
	      {
	    	  
	    	 // fooData = dataPacket[i];
	        //  fooData = Arrays.copyOfRange(fooData, fooData.length-Nfft, fooData.length); 
	        //  fftBuff[i].forward(fooData); //compute FFT on this channel of data
	        for(int j=0;j<49;j++)
	        {
	          musicDataBuffer[i][j]=musicDataBuffer[i][j+1];
	        }
	      }
	      
	     
	      for(int i = 0; i <8; i++)
	      {
	        // fooData = dataPacket[i];
	        //  fooData = Arrays.copyOfRange(fooData, fooData.length-Nfft, fooData.length); 
	        //  fftBuff[i].forward(fooData); //compute FFT on this channel of data
	        //EDO GEMIZEIS TON BUFFER TIS MOUSIKIS SAN OURA
	    		if(i==0){ 
	    			musicDataBuffer[0][49] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/3;
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][49] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/3;	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][49] = (maxAmp[2] + maxAmp[14])/2;	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][49] = (maxAmp[3] + maxAmp[15])/2;	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][49] = (maxAmp[4] + maxAmp[12])/2;	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][49] = (maxAmp[5] + maxAmp[13])/2;	    		
	    		}
	    		else if( i == 6){    			
	    			musicDataBuffer[6][49] = maxAmp[6] ;	    		
	    		}
	    		else if( i == 7){    			
	    			musicDataBuffer[7][49] = maxAmp[7] ;	    		
	    		}	
	     
	      }
	      }
	    }
	    /*
	   
	      */
	  }
	  
	  //Function to find the mean of the buffer [numberOfSamples changes the total samples calculated in mean]
	  public void findMean(int numberOfSamples)
	  {
	    
	    if(musicDataBufferCounter-1 < 50) //0-49
	    {
	      if(numberOfSamples >= musicDataBufferCounter-1)
	      {
	        float sum =0;
	        for(int i = 0; i<8; i++)
	        {
	          
	          for(int j =0; j<= musicDataBufferCounter-1;j++)
	          {
	            sum = sum + musicDataBuffer[i][j];
	          }
	          meanBuffer[i] = (sum/(musicDataBufferCounter));
	          sum = 0;
	        }
	      }
	      else if(numberOfSamples < musicDataBufferCounter-1)
	      {
	        float sum = 0;
	        for(int i = 0; i<8; i++)
	        {
	         
	          for(int j = 0; j<numberOfSamples;j++)
	          {
	            sum = sum + musicDataBuffer[i][musicDataBufferCounter-1-numberOfSamples];
	          }
	          meanBuffer[i] = ((float)sum/numberOfSamples);
	          sum = 0;
	        }
	      }
	      else
	      {
	        println("impossible!");
	      }

	    }
	    else if (musicDataBufferCounter >= 50)
	    {
	      float sum =0;
	      for(int i = 0; i<8; i++)
	      {
	       
	        for(int j =(50-numberOfSamples); j < 50;j++)
	        {
	          sum = sum + musicDataBuffer[i][j];
	        }
	        meanBuffer[i] = ((float)sum/numberOfSamples);
	        sum = 0;
	      }
	    }
	    else
	    {
	      println("impossible!");
	    }
	    
	    //TESTING MEAN VALUES!
	    //print("Mean: ");
	    //for(int i =0; i<8;i++)
	    //{
	    //  print(meanBuffer[i], " ");
	    //}
	    //println(" ");
	    //println(" ");
	    
	  }
	  
	  //---Functions for Sound Group-------------------------FINISH-----------------------------------------//
	

	
	//---Functions for Spectro Group-----------------------START------------------------------------------//
	
	 // Button StreamButton;
	 // Button StreamButtonPause;
	  float timeCounter=0;
	  
	public void spectroGroupSetup()
	{	
		initializeFiltersters();
		for(int i=0;i< channel_number;i++)
			maxAmp[i] = 0;
		
		
		MuToggle = new ControlP5(this);
		
		//int bl = color(0,90,150);		
		spectroMyGroup = new ControlP5(this);	
		spectroMyGroup.setColorBackground(colorBackgroundChart); // <------color of the background/chart
		spectroMyGroup.setColorForeground(color(250,250,250)); // <--- color of the line/chart
				
		controlP5.Group spectroGroup = spectroMyGroup.addGroup("chartGroup",width/120 + width/2,(height-20)/60 +10,width- (width/120 + width/2)-10);
		spectroGroup.setBackgroundColor(colorBackgroundChart);
		spectroGroup.setBackgroundHeight(height - height/22 );
		spectroGroup.hideBar();
		spectroGroup.setColorForeground(black);
		spectroGroup.setColorLabel(white);
		spectroGroup.setColorBackground(greyColor);
		spectroGroup.setBackgroundColor(greyTr);
		
		
		 //For channel 7
		
	    toggleMu = MuToggle.addToggle("toggleMuRhythm");
	    toggleMu.setGroup(spectroGroup);
	    toggleMu.setPosition(6*width/22.2f,48f*height/53.1f); //<-------
	    toggleMu.setWidth(4*width/60);
	    toggleMu.setHeight((int)(height/32));
	    toggleMu.setMode(ControlP5.SWITCH);
	    toggleMu.setValue(false);
	    toggleMu.setColorActive(toggleOFF);
	    toggleMu.setColorCaptionLabel(black);
	    toggleMu.setColorBackground(color(125,125,125));
	    //toggleMu.setLabelVisible(false);
	    toggleMu.setLabel("          MU RYTHM");
	    
	   
		
		
		
		for(int j=0;  j< channel_number;  j++){			  
			  myChart[j] = spectroMyGroup.addChart(dataset[j])
						   .setPosition(width/25 ,10 + 10*(j+2.5f*j) )
						   .setSize(250, height/13 )
			               .setRange(-0.1f, 0.1f)
			               .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
			               .setStrokeWeight(1f)		
			              
			               ;
			              			               
			  myChart[j].addDataSet(dataset[j]);
			  myChart[j].setData(dataset[j], new float[1250]);
			   j++;
			  }  
		
		for(int j=1;  j< channel_number;  j++){			  
			  myChart[j] = spectroMyGroup.addChart(dataset[j])
						   .setPosition(width/25 + width/4.65f,10 + 10*((j-1)+ 2.5f*(j-1)) )
						   .setSize(250, height/13 )
			               .setRange(-0.1f, 0.1f)
			               .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
			               .setStrokeWeight(1f)	
			               
			               ;
			              			               
			  myChart[j].addDataSet(dataset[j]);
			  myChart[j].setData(dataset[j], new float[1250]);
			   j++;
			  }  
			  
			  for(int j =0; j < channel_number; j++ ){
				  myChart[j].setGroup(spectroGroup);}
			  
			  
			  
			  
			  	SpecButton = new ControlP5(this);
				SpecButton.setColorForeground(color(255,0,0));
				SpecButton.setColorBackground(color(255,255,0));
				SpecButton.setColorActive(colorGreyActive2);
				SpecButton.setColorCaptionLabel(black);
				
				Button StreamButton = SpecButton.addButton("StreamButton");
				StreamButton.setGroup("spectroMyGroup");
				StreamButton.setLabel("Start");				
				StreamButton.setPosition(width/3.7f ,height/2 + height/3f );
				StreamButton.setSize(8*width/120,2*height/60);				
				StreamButton.setGroup(spectroGroup);
				
				
				WaveButton = new ControlP5(this);
				WaveButton.setColorForeground(color(255,255,255,100));
				WaveButton.setColorBackground( color(255,255,255));
				WaveButton.setColorActive(colorGreyActive2);
				WaveButton.setColorCaptionLabel(black);
				
				Button allWave = WaveButton.addButton("allWave");
				allWave.setGroup("spectroMyGroup");
				allWave.setLabel("All");				
				allWave.setPosition(width/4f  + width/12f  + width/90f,height/2 + height/3 );
				allWave.setSize(8*width/120,2*height/60);			
				allWave.setGroup(spectroGroup);
				
				
				deltaWaveButton = new ControlP5(this);
				deltaWaveButton.setColorForeground(color(255,255,255,100));
				deltaWaveButton.setColorBackground(color(255,255,255));
				deltaWaveButton.setColorActive(colorGreyActive2);
				deltaWaveButton.setColorCaptionLabel(black);
				
				Button deltaWave = deltaWaveButton.addButton("deltaWave");
				deltaWave.setGroup("spectroMyGroup");
				deltaWave.setLabel("Delta");				
				deltaWave.setPosition(width/4f  + width/12f  + width/90f ,height/2 + height/2.71f  );
				deltaWave.setSize(8*width/120,2*height/60);			
				deltaWave.setGroup(spectroGroup);
				
				thetaWaveButton = new ControlP5(this);
				thetaWaveButton.setColorForeground(color(255,255,255,100));
				thetaWaveButton.setColorBackground(color(255,255,255));
				thetaWaveButton.setColorActive(colorGreyActive2);
				thetaWaveButton.setColorCaptionLabel(black);
				
				
				Button thetaWave = thetaWaveButton.addButton("thetaWave");
				thetaWave.setGroup("spectroMyGroup");
				thetaWave.setLabel("Theta");				
				thetaWave.setPosition(width/4f  + width/12f  + width/90f,height/2 + height/3 + height/14   );
				thetaWave.setSize(8*width/120,2*height/60);			
				thetaWave.setGroup(spectroGroup);
				
				//width/4f + width/12.8f + width/12f ,height/2 + height/2.71f  
				alphaWaveButton = new ControlP5(this);
				alphaWaveButton.setColorForeground(color(255,255,255,100));
				alphaWaveButton.setColorBackground(color(255,255,255));
				alphaWaveButton.setColorActive(colorGreyActive2);
				alphaWaveButton.setColorCaptionLabel(black);
				
				Button alphaWave = alphaWaveButton.addButton("alphaWave");
				alphaWave.setGroup("spectroMyGroup");
				alphaWave.setLabel("Alpha");				
				alphaWave.setPosition(width/4f + width/12.8f + width/12f  ,height/2 + height/3);
				alphaWave.setSize(8*width/120,2*height/60);			
				alphaWave.setGroup(spectroGroup);				
				
				betaWaveButton = new ControlP5(this);
				betaWaveButton.setColorForeground(color(255,255,255,100));
				betaWaveButton.setColorBackground(color(255,255,255));
				betaWaveButton.setColorActive(colorGreyActive2);
				betaWaveButton.setColorCaptionLabel(black);
				//width/4f + width/12.8f ,height/2 + height/3 + height/20 
				Button betaWave = betaWaveButton.addButton("betaWave");
				betaWave.setGroup("spectroMyGroup");
				betaWave.setLabel("Beta");				
				betaWave.setPosition(width/4f + width/12.8f + width/12f ,height/2 + height/2.71f   );
				betaWave.setSize(8*width/120,2*height/60);			
				betaWave.setGroup(spectroGroup);
				
				gammaWaveButton = new ControlP5(this);
				gammaWaveButton.setColorForeground(color(255,255,255,100));
				gammaWaveButton.setColorBackground(color(255,255,255));
				gammaWaveButton.setColorActive(colorGreyActive2);
				gammaWaveButton.setColorCaptionLabel(black);
				
				Button gammaWave = gammaWaveButton.addButton("gammaWave");
				gammaWave.setGroup("spectroMyGroup");
				gammaWave.setLabel("Gamma");				
				gammaWave.setPosition(width/4f + width/12.8f + width/12f ,height/2 + height/3 + height/14   );
				gammaWave.setSize(8*width/120,2*height/60);			
				gammaWave.setGroup(spectroGroup);
				
				coherenceButton = new ControlP5(this);
				coherenceButton.setColorForeground(color(255,255,255,100));
				coherenceButton.setColorBackground(color(255,255,255));
				coherenceButton.setColorActive(colorGreyActive2);
				coherenceButton.setColorCaptionLabel(black);
				
				Button coherenceWindow = coherenceButton.addButton("ECP");
				coherenceWindow.setGroup("spectroMyGroup");
				coherenceWindow.setLabel("Open Coherence");				
				coherenceWindow.setPosition(width/5.1f ,height/2 + height/3 + height/14   );
				coherenceWindow.setSize(8*width/120,2*height/60);	
				coherenceWindow.setColorBackground(color(140,140,140));
				//coherenceWindow.setColorForeground(color(140,140,140));
				coherenceWindow.setVisible(false);
				coherenceWindow.setGroup(spectroGroup);
				
				
				
				
				
	
				initializeFFT();
				
	     PFont arial = createFont("Arial", 12);	     
		 spectroGroup.addDrawable(new CDrawable(){			
			public void draw(PGraphics p){
				
				// enable button for coherence 
				if( playbackLiveFlag  == true ){
					coherenceWindow.setVisible(true);
				}
				else 
					coherenceWindow.setVisible(false);
				
				if(state == true){
					timeCounter = millis() - tempTimeCounter;
				}
				
				//time counter
				p.fill(color(125,125,125,200));
				p.rect( p.width/3.7f, height/2 + height/2.71f  , width/15.1f,2*height/60);
				p.fill(color(125,125,125,150));
				p.rect( p.width/3.7f,height/2 + height/2.71f ,width/15.1f,2*height/60);
				p.fill(color(0));
				p.textFont(arial);		
				p.fill(255,255,255);
				int timeRound = (int)(timeCounter/1000f);
				p.text(timeRound,  p.width/3.5f, height/2 + height/2.51f);
				p.textSize(9);
				p.text("s",  p.width/3.2f, height/2 + height/2.51f);
				p.textSize(9);
				
				
				//FFT plot
				p.pushMatrix();
				p.fill(greyColor);
				p.rect(10,p.height/2 + p.height/3,width/10f  ,height/9.6f );
				p.fill(255,255, 255);
				p.textFont(arial);		
				p.fill(255,255,255);
				p.text("FFT Plot", width/5f - width/7.5f, p.height/2 + p.height/2.85f); 
				p.textSize(9);
				
				
			
				p.popMatrix();
				
				
				
				
				
				if( flagForDraw==true ){
					first = true;
			           flagForDraw=false;
			           int n,start;
			           if( flagBuffer == 1 ){
			                start =  0;
			                n     = 250;
			                
					                if( flagFilterUsed1 == false){
					                	 flagFilterUsed1 = true;
						                 for(  int j = 0; j < channel_number; j++){  
							                 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
							                 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass		                 
							                 		
							                filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
							                filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
							                filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
							                filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
							                filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
							     
							                filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]); 
						                 }
						                 println("1A");
						             	}
					                
			               }
			              else if( flagBuffer ==2 ) {          	 
			                start = 250;
			                n     = 500;
					                if( flagFilterUsed2 == false){
					                	flagFilterUsed2 = true;
						                 for(  int j = 0; j < channel_number; j++){  
						                 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
						                 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass
						                 
						                 filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
					                	 filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
					                	 filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
					                	 filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
					                	 filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
							                 
					                	 filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
						                 }
						                 println("1B");
						             	}
					               
			              }
			               else {
			            	   start = 500;
				               n     = 750;
				            	   if(flagFilterUsed3 == false){
				            		   flagFilterUsed3 = true;
				            		   for(  int j = 0; j < channel_number; j++){  
				  		                filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
				  		                filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass				  		              
				  		                filterIIR(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
				                		filterIIR(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
				                		filterIIR(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
				                		filterIIR( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
				                		filterIIR(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
						                   
				                		filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
				  		                 }
				            		   println("1C");
					             	}
				            	   
			   				}
			        
			         
			             
			      float     spectrumScale   = 10f; 
				// float centerFrequency = 0;
		         
			      
		                  float[] fooData;
		                  
		                  for(int i=0;i< channel_number;i++){	    		    		   
		  		  		    maxAmp[i] = 0;	 
		  		  		    }     
		             for (int currChannel=0; currChannel < channel_number; currChannel++) {	
					            //do what to do for each channel 	 		            	 
					              fftBuff[currChannel] = new FFT(Nfft, fs_Hz);  
					              fftBuff[currChannel].window(FFT.HAMMING);	         
					              fooData = dataPacket[currChannel];
					              float highestDifferenceSpectrum = 0f; 
					              
					            //  fooData = Arrays.copyOfRange(fooData, fooData.length-Nfft, fooData.length); 
					              //do padding
					              fooData = Arrays.copyOfRange(fooData, fooData.length-Nfft, fooData.length);
					              for(int y=fooData.length-6;y<fooData.length; y++){
					            	  fooData[y] = 0f;
					              }
					              
					              
					              fftBuff[currChannel].forward(fooData);	             
					              fftLin.forward( fooData );
					              fftLog.forward( fooData );		              
					             
					              
					              // draw the full spectrum of the chan channel						            
					                 p.noFill();
						              for(int i = 0; i < fftLin.specSize(); i++)
						              {
						                // if the mouse is over the spectrum value we're about to draw
						                // set the stroke color to red
						                if ( i == mouseX )
						                {
						            //      centerFrequency = fftLin.indexToFreq(i);
						                  p.stroke(color(255,255,255));
						                }
						                else
						                {
						                	 p.stroke(color(255,255,255));
						                }
						             //   line(i, p.height/2 + p.height/3 + p.height/7.9f , i, p.height/2 + p.height/3 + p.height/7.9f   - fftLog.getBand(i)*spectrumScale);
						                float trew = fftLin.getBand(i);
					                    trew = trew;
					                    trew = trew*spectrumScale;
					                    
						               if(trew>height/9.6f )
						            	   trew=height/9.6f;  
						              
						              
						                p.line(11 + i, p.height/2 + p.height/3  + p.height/9.65f , 11 + i, p.height/2 + p.height/3 + p.height/9.65f - trew);				                						      
						              }
						            	   
		              
		              fill(0);
		            //  text("Spectrum Center Frequency: " + centerFrequency, 0, p.height/1.5f - height/12);		
		              
		            //set the arrays back to 0
		  		      
		              //chooseNoteSpectrumSumCounter[currChannel]++;
		              for(int i = 0; i < fftBuff[currChannel].specSize()/2; i++)
		              {
				              //check for the highest standard deviation 
		            	  	 
		            	  	  //formula for comparison  currAmp/meanCurrentAmp is the most different)
		            	  	  float meanCurrentAmp = abs( chooseNoteSpectrumSum[currChannel][i]/ (float)(chooseNoteSpectrumSumCounter[currChannel] +1 ) );
		            	  	  //if amplitude is not 0
		            	  	  if(chooseNoteSpectrumSum[currChannel][i]!=0){
		            	  		  
		            	  		  	 float comp = abs(chooseNoteSpectrumSum[currChannel][i] - meanCurrentAmp)/chooseNoteSpectrumSum[currChannel][i];
			            	  		 if(( abs(meanCurrentAmp) == 0f)&&( fftBuff[currChannel].getBand(i) != 0f ) ){
				            	  		 maxAmp[currChannel] =  random(0,128);
				            	  		 highestDifferenceSpectrum =   fftBuff[currChannel].getBand(i);
				            	  	  }
			            	  		 else  if(( abs(meanCurrentAmp) == 0f)&&( fftBuff[currChannel].getBand(i) == 0f ) ){
			            	  			maxAmp[currChannel] =  random(0,128);
			            	  		 }
				            	  	  else if( comp > highestDifferenceSpectrum ){				
						            	  maxAmp[currChannel] =  i;
						            	  highestDifferenceSpectrum =  fftBuff[currChannel].getBand(i);
						            	 
				            	  	  }
		            	  	  }
		            	  	  else{
		            	  		 maxAmp[currChannel] =  random(0,128);
		            	  		 highestDifferenceSpectrum =  fftBuff[currChannel].getBand(i);
		            	  	  }
		            	  	 
		            	  	 maxAmp[currChannel] =  random(0,128);
				               chooseNoteSpectrumSum[currChannel][i] = abs( fftBuff[currChannel].getBand(i)) + chooseNoteSpectrumSum[currChannel][i];
				               
		              }
		             println("MAX "+ maxAmp[currChannel]);
		              chooseNoteSpectrumSumCounter[currChannel]++;
			                 for(  int i=start; i < n; i++){ 		                	           	 
			             myChart[currChannel].push(dataset[currChannel], dataPacket[currChannel][i]);		            
			         }
			                 
		             }       
			       flagSoundGroup = true;      
			          
			    }
				
				
					 float spectrumScale = 10f;
					// float centerFrequency =0;
	                  noFill();
	                  for(int i = 0; i < fftLog.specSize(); i++)
	                  {
	                    // if the mouse is over the spectrum value we're about to draw
	                    // set the stroke color to red
	                    if ( i == mouseX )
	                    {
	                 //   centerFrequency = fftLog.indexToFreq(i);
	                      p.stroke(color(255,255,255));
	                    }
	                    else
	                    {
	                        p.stroke(color(255,255,255));
	                    }
	                   // line(i, p.height/2 + p.height/3 + p.height/7.9f , i, p.height/2 + p.height/3 + p.height/7.9f   - fftLog.getBand(i)*spectrumScale);
	                    
	                    float trew =fftLin.getBand(i);
	                    trew = trew;
	                    trew = trew*spectrumScale;
	                   
			              // if(i<10){
			            	//   trew = 0 ;
			              ///  }
	                    if(trew>height/9.6f )
			            	   trew=height/9.6f; 
	                   
	                   
			                p.line(11 + i, p.height/2 + p.height/3 + p.height/9.65f , 11 + i, p.height/2 + p.height/3 + p.height/9.65f - trew);			
	                  }
	                  
	                  fill(0);
	                //  text("Spectrum Center Frequency: " + centerFrequency, 0, p.height/1.5f - height/12);			
	        
			
			
			
			
			/*
			
	                //array with values from spectrogram
	  				p.fill(180,180,180);
	  				p.rect( p.width/9f ,p.height/2 + p.height/3,width/8.8f  ,height/9.6f );
	  				
	  				p.fill(255,255, 255);
	  			    p.stroke(255,255, 255);
	  				p.line( p.width/9f ,p.height/2 + p.height/3 + height/19.2f ,p.width/9f + width/8.8f  ,p.height/2 + p.height/3 + height/19.2f );
	  				
	  				p.fill(255,255, 255);
	  				p.line( p.width/8f ,p.height/2 + p.height/3 ,p.width/8f  ,p.height/2 + p.height/3 + 2*height/19.2f );
	  				p.line( p.width/7.2f ,p.height/2 + p.height/3 ,p.width/7.2f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/6.5f ,p.height/2 + p.height/3 ,p.width/6.5f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/7.2f ,p.height/2 + p.height/3 ,p.width/7.2f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/5.95f ,p.height/2 + p.height/3 ,p.width/5.95f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/5.5f ,p.height/2 + p.height/3 ,p.width/5.5f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/5.1f ,p.height/2 + p.height/3 ,p.width/5.1f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				p.line( p.width/4.75f ,p.height/2 + p.height/3 ,p.width/4.75f  ,p.height/2 + p.height/3 + 2*height/19.2f);
	  				
	  				
	  				p.pushMatrix();
	  				//electrodes
	  				p.textFont(arial);
	  				p.textSize(9);
	  				p.fill(0,0,0);
	  				p.text("Fp1", p.width/9f +1, p.height/2 + p.height/3 + p.height/34); 
	  				p.text("Fp2", p.width/9f + p.width/65, p.height/2 + p.height/3 + p.height/34);
	  				p.text("C3" , p.width/7.05f , p.height/2 + p.height/3 + p.height/34); 
	  				p.text("C4" , p.width/6.95f + p.width/80, p.height/2 + p.height/3 + p.height/34);
	  				p.text("T5" , p.width/5.85f , p.height/2 + p.height/3 + p.height/34); 	
	  				p.text("T6" , p.width/5.4f , p.height/2 + p.height/3 + p.height/34); 
	  				p.text("O1" , p.width/5.35f + p.width/80, p.height/2 + p.height/3 + p.height/34);
	  				p.text("O2" , p.width/5f + p.width/80, p.height/2 + p.height/3 + p.height/34);
	  				stroke(0);
	  				
	  				
	  				//values of electrodes
	  				p.text(round(dataPacket[0][0]*100) , p.width/9f +1, p.height/2 + p.height/3 + p.height/12); 
	  				p.text(round(dataPacket[1][0]*100) , p.width/9f + p.width/65, p.height/2 + p.height/3 + p.height/12);
	  				p.text(round(dataPacket[2][0]*100) , p.width/7.05f , p.height/2 + p.height/3 + p.height/12); 
	  				p.text(round(dataPacket[3][0]*100) , p.width/6.95f + p.width/80, p.height/2 + p.height/3 + p.height/12);
	  				p.text(round(dataPacket[4][0]*100) , p.width/5.85f , p.height/2 + p.height/3 + p.height/12); 
	  				
	  				p.text(round(dataPacket[5][0]*100) , p.width/5.4f , p.height/2 + p.height/3 + p.height/12); 
	  				p.text(round(dataPacket[6][0]*100) , p.width/5.35f + p.width/80, p.height/2 + p.height/3 + p.height/12);
	  				p.text(round(dataPacket[7][0]*100) , p.width/5f + p.width/80, p.height/2 + p.height/3 + p.height/12);
	  				*/
	                  stroke(0);
	                  p.pushMatrix();
	  				fill(colorButtonAll);
	  				p.rect(width/4f + width/10.59f,height/2 + height/3 				   	 ,8*width/120,2*height/60);	
	  				fill(colorButtonDelta);
	  				p.rect(width/4f + width/10.59f  ,height/2 + height/3  + height/28.3f  ,8*width/120,2*height/60);								
	  				fill(colorButtonTheta);
	  				p.rect(width/4f + width/10.59f ,height/2 + height/3 + height/14  ,8*width/120,2*height/60);
	  				fill(colorButtonAlpha);			
	  				p.rect(width/4f + width/12.8f + width/12f  ,height/2 + height/3 								 ,8*width/120,2*height/60);						
	  				fill(colorButtonBeta);			
	  				p.rect(width/4f + width/12.8f + width/12f  ,height/2 + height/3  + height/28.3f  , 8*width/120,2*height/60);				
	  				fill(colorButtonGamma);			
	  				p.rect(width/4f + width/12.8f + width/12f  ,height/2 + height/3 + height/14   ,8*width/120,2*height/60);
	  			
			
	  				  
	                  //p.textFont(arial);
	                  p.fill(0,0, 0);
		  			  p.stroke(0,0,0);
		  		      p.textSize(11);
		  		     // p.line( p.width/20f + p.width/180f  ,p.height/10 + p.height/100 -20,p.width/20f + p.width/180f		    , p.height/2 + p.height/4 + p.height/14  );
	                  p.text((dataPacket[0][0]*1000f), p.width/20f + p.width/180f , p.height/10 + p.height/100 ); 
	                  p.text((dataPacket[1][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/10 + p.height/100 );
	                  p.text((dataPacket[2][0]*1000f), p.width/20f + p.width/180f , p.height/5 + p.height/100 + p.height/400);
	                  p.text((dataPacket[3][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/5 + p.height/100 + p.height/400);
	                  p.text((dataPacket[4][0]*1000f), p.width/20f + p.width/180f , p.height/4 + p.height/16 );
	                  p.text((dataPacket[5][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/4 + p.height/16 );
	                  p.text((dataPacket[6][0]*1000f), p.width/20f + p.width/180f 		   ,  p.height/4 + p.height/8 + p.height/25 );
	                  p.text((dataPacket[7][0]*1000f), p.width/5f + p.width/20f + p.width/48f ,  p.height/4 + p.height/8 + p.height/25 );
	                  
	                 // p.line( p.width/5f + p.width/20f + p.width/48f   ,p.height/10 + p.height/100 -20,p.width/5f + p.width/20f + p.width/48f    , p.height/2 + p.height/4 + p.height/14  );
	                  p.text((dataPacket[8][0]*1000f), p.width/20f + p.width/180f ,             p.height/2 + p.height/60);  
	                  p.text((dataPacket[9][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/2 + p.height/60 );
	                  p.text((dataPacket[10][0]*1000f), p.width/20f + p.width/180f		    , p.height/2 + p.height/16 + p.height/18);
	                  p.text((dataPacket[11][0]*1000f), p.width/5f + p.width/20f + p.width/48f, p.height/2 + p.height/16 + p.height/18 );
	                  p.text((dataPacket[12][0]*1000f), p.width/20f + p.width/180f		    , p.height/2 + p.height/8 + p.height/12 + p.height/80);
	                  p.text((dataPacket[13][0]*1000f), p.width/5f + p.width/20f + p.width/48f, p.height/2 + p.height/8 + p.height/12 + p.height/80);
	                  p.text((dataPacket[14][0]*1000f), p.width/20f + p.width/180f		    , p.height/2 + p.height/4 + p.height/14);
	                  p.text((dataPacket[15][0]*1000f), p.width/5f + p.width/20f + p.width/48f, p.height/2 + p.height/4 + p.height/14);
	                  
	                  p.popMatrix();  
	                  
			}	
			
			
			
			
		});
			
	}
	boolean chooseWave  = true;
	boolean chooseDelta = false;
	boolean chooseTheta = false;
	boolean chooseAlpha = false;
	boolean chooseBeta  = false;
	boolean chooseGamma = false;
	int   currentDelta  = 1;
	int   currentTheta  = 1;
	int   currentAlpha  = 1;
	int   currentBeta   = 1;
	int   currentGamma  = 1;
	int   colorButtonAll   = color(255,255,255,100);
	int   colorButtonDelta = color(0,0,0,100);
	int   colorButtonTheta = color(0,0,0,100);
	int   colorButtonAlpha = color(0,0,0,100);
	int   colorButtonBeta  = color(0,0,0,100);
	int   colorButtonGamma = color(0,0,0,100);
	int   colorButtonOther = color(0,0,0,100);
	
	int currentMu = 1;
	
	
	ControlP5 deltaWaveButton;
	ControlP5 thetaWaveButton;
	ControlP5 alphaWaveButton;
	ControlP5 betaWaveButton;
	ControlP5 gammaWaveButton;
	ControlP5 coherenceButton;
	
	public void initializeFFT(){					  
			  // create an FFT object that has a time-domain buffer the same size as jingle's sample buffer
			  // note that this needs to be a power of two 
			  // and that it means the size of the spectrum will be 128. 			 
			fftLin = new FFT( 256, 250 );		  
			// calculate the averages by grouping frequency bands linearly. use 30 averages.
			fftLin.linAverages( 30 );					
			  // create an FFT object for calculating logarithmically spaced averages
			  fftLog = new FFT( 256, 250 );			 			
	}
	
	
	
	 float[] fooData = new float[256];
	boolean first = false;
	
	
	void filterIIR(double[] filt_b, double[] filt_a, float[] data) {
		  double[] prev_y = new double[filt_b.length];
		  double[] prev_x = new double[filt_b.length];
		  
		  //step through data points
		  for (int i = 0; i < data.length; i++) {   
		    //shift the previous outputs
		    for (int j = filt_b.length-1; j > 0; j--) {
		      prev_y[j] = prev_y[j-1];
		      prev_x[j] = prev_x[j-1];
		    }
		    
		    //add in the new point
		    prev_x[0] = data[i];
		    
		    //compute the new data point
		    double out = 0;
		    for (int j = 0; j < filt_b.length; j++) {
		      out += filt_b[j]*prev_x[j];
		      if (j > 0) {
		        out -= filt_a[j]*prev_y[j];
		      }
		    }
		    
		    //save output value
		    prev_y[0] = out;
		    data[i] = (float)out;
		  }
		}
	
	
	private void initializeFiltersters() {	  
	    double[] b, a, b2, a2;	   

	   //Notch filters	  
	    for (int i=0; i < notchProperties.length; i++) {	      
		        if( i==0 ){
			          //60 Hz notch filter,  a = signal.butter(2,[59.0 61.0]/(fs_Hz / 2.0), 'bandstop')
			          b2 = new double[] { 9.650809863447347e-001, -2.424683201757643e-001, 1.945391494128786e+000, -2.424683201757643e-001, 9.650809863447347e-001 };
			          a2 = new double[] { 1.000000000000000e+000, -2.467782611297853e-001, 1.944171784691352e+000, -2.381583792217435e-001, 9.313816821269039e-001  }; 			        
			        }
		        else if( i==1 ){	       
			          //50 Hz notch filter a = signal.butter(2,[49.0 51.0]/(fs_Hz / 2.0), 'bandstop')
			          b2 = new double[] { 0.96508099, -1.19328255,  2.29902305, -1.19328255,  0.96508099 };
			          a2 = new double[] { 1.0       , -1.21449348,  2.29780334, -1.17207163,  0.93138168 }; 			          
			        }
		        else{	   
			          //no notch filter
			          b2 = new double[] { 1.0 };
			          a2 = new double[] { 1.0 };		         
			        }        
		        notchProperties[i] =  new filterClass(b2, a2);
	    } 
	  
	    for (int i=0;i<BPproperties.length;i++) {
	      //Bandpass filters
	      
	        if(i == 0 ){
		          //[b,a]=butter(2,[1 50]/(250/2));  %bandpass filter
		          b = new double[] { 2.001387256580675e-001, 0.0f, -4.002774513161350e-001, 0.0f, 2.001387256580675e-001};
		          a = new double[] { 1.0f, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};          
		        }
	        else if(i == 1 ){
		          //[b,a]=butter(2,[7 13]/(250/2));
		          b = new double[] {  5.129268366104263e-003, 0.0f, -1.025853673220853e-002, 0.0f, 5.129268366104263e-003};
		          a = new double[] { 1.0f, -3.678895469764040e+000, 5.179700413522124e+000, -3.305801890016702e+000, 8.079495914209149e-001};	         	               
	        }
		    else if(i == 2 ){
		          //[b,a]=butter(2,[15 50]/(250/2)); %matlab command
		          b = new double[] { 1.173510367246093e-001, 0.0f, -2.347020734492186e-001, 0.0f, 1.173510367246093e-001};
		          a = new double[] { 1.0f, -2.137430180172061e+000, 2.038578008108517e+000, -1.070144399200925e+000, 2.946365275879138e-001}; 
	        }
            else if(i == 3 ){
		          //[b,a]=butter(2,[5 50]/(250/2)); %matlab command
		          b = new double[] {  1.750876436721012e-001, 0.0f, -3.501752873442023e-001, 0.0f, 1.750876436721012e-001};       
		          a = new double[] {  1.0f, -2.299055356038497e+000, 1.967497759984450e+000, -8.748055564494800e-001, 2.196539839136946e-001};
		    }
	        else if(i == 4 ){
	          //[b,a]=butter(2,[0.3 30]/(250/2));
	  	       b = new double[]{ 8.9819001234212e-002f, 0.0f, -1.79638002468425e-001f, 0.0f, 8.9819001234212e-002f};
	  	       a = new double[]{ 1.0f, -2.983022136008243e+000f, 3.321149361017554e+000f, -1.689322898421e+000f, 3.51216492642182e-001f};	          
	        }
  	       else{        	      
	          //no filtering
	          b = new double[] {1.0};
	          a = new double[] {1.0};
	  	    }     
	      BPproperties[i] =  new filterClass(b, a);
	    } //end of Bandpass filters

	    
	    for (int i=0;i<deltaProperties.length;i++) {
		   
	    	 if (i == 0) {
		     
		          //butter(2,[0.5 4]/(250/2));  %delta band filter
		          b = new double[] { 0.001820128710717, 0.0f, -0.003640257421435, 0.0f, 0.001820128710717};
		          a = new double[] { 1.0f, -3.873296116978567, 5.629767675866390, -3.639496144117842, 0.883026086553440};          
	    	 }
	    	 else{		       
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		      }  		    
		      deltaProperties[i] =  new filterClass(b, a);
		    }
	    
	    for (int i=0;i<thetaProperties.length;i++) {
		     
		      if (i == 0) {
		    
		          //butter(2,[4 8]/(250/2));  %theta band filter
		          b = new double[] { 0.002357208772847, 0.0f, -0.004714417545693f, 0f, 0.002357208772847f};
		          a = new double[] { 1.0f, -3.819084908499169,5.508698361351956, -3.55670569986647f, 0.86747213379167f};          
		      }      
		      else{
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		      }  		    
		      thetaProperties[i] =  new filterClass(b, a);
		    } 
	    
	    
	    for (int i=0; i < alphaProperties.length; i++) {
		
	    	if (i == 0) {
		          //butter(2,[8 12]/(250/2));  %alpha band filter
		          b = new double[] { 0.002357208772854f, 0.0f, -0.004714417545708f, 0.0f, 0.002357208772854f};
		          a = new double[] { 1.0f, -3.741561528598107, 5.361993738129343f, -3.484508340087064f, 0.867472133791671};          
	    	}			      
	    	else{
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		    }  		    
		    alphaProperties[i] =  new filterClass(b, a);
		    } 
	    
	    
	    
	    for (int i=0;i<betaProperties.length;i++) {
		
	    	if (i == 0) {
		          //butter(2,[13 25]/(250/2));  %bandpass filter
		          b = new double[] { 0.018650396227835, 0.0f, -0.037300792455670, 0.0f, 0.018650396227835};
		          a = new double[] { 1.0f, -3.214440261194531, 4.185707917233462, -2.590707106197416, 0.652837763407545};          
	    	}
	    	else{	    		
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		      }  		    
		    betaProperties[i] =  new filterClass(b, a);
		    } 
	    
	    
	    for (int i=0;i<gammaProperties.length;i++) {
		 
	    	if (i == 0) {
		          //butter(2,[25 100]/(250/2));  %bandpass filter
		          b = new double[] { 0.391335772501769, 0.0f, -4.002774513161350e-001, 0.0f, 0.391335772501769};
		          a = new double[] { 1.0f, -1.998401444325282e-15, -0.369527377351241, 7.494005416219807e-16, 0.195815712655833};          
	    	}
	    	else{		       
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		      }  		    
		    gammaProperties[i] =  new filterClass(b, a);
		    } 
	
	    for (int i=0;i < muProperties.length;i++) {
	    	if (i == 0) {
		          // butter(2,[7.5 12.5]/(250/2))  %bandpass filter
		          b = new double[] { 0.003621681514928f,0f,-0.007243363029856f,0f,0.003621681514928f};
		          a = new double[] { 1.0f, -3.709918611606726f, 5.269811129902000f, -3.393888777086079f, 0.837181651256023f};          
	    	}
	    	else{		       
		          //no filtering
		          b = new double[] {1.0};
		          a = new double[] {1.0};
		      }  		
	    	muProperties[i] =  new filterClass(b, a);
	    }
	
	}
	
	
	
	
	public void allWave(){
		if( chooseWave == false){
			chooseDelta = false;
			chooseTheta = false;
			chooseAlpha = false;
			chooseBeta  = false;
			chooseGamma = false;
			chooseWave  = true;	
			currentDelta = 1;
			currentTheta = 1;
			currentAlpha = 1;
			currentBeta  = 1;
			currentGamma = 1;
			
			
			colorButtonDelta = color(0,0,0,100);
			colorButtonTheta = color(0,0,0,100);
			colorButtonAlpha = color(0,0,0,100);
			colorButtonBeta  = color(0,0,0,100);
			colorButtonGamma = color(0,0,0,100);
			colorButtonAll   = color(255,255,255,100);			
		}	
		
	}
	
	public void deltaWave(){
		
		if( chooseDelta == false){
			chooseDelta = true;
			chooseTheta = false;
			chooseAlpha = false;
			chooseBeta  = false;
			chooseGamma = false;
			chooseWave  = false;
			
			currentDelta = 0;
			currentTheta = 1;
			currentAlpha = 1;
			currentBeta  = 1;
			currentGamma = 1;
			
			colorButtonDelta = color(255,255,255,100);		
			colorButtonTheta = color(0,0,0,100);
			colorButtonAlpha = color(0,0,0,100);
			colorButtonBeta  = color(0,0,0,100);
			colorButtonGamma = color(0,0,0,100);			
			colorButtonAll   = color(0,0,0,100);
		}	
			
		
	}
	
	public void thetaWave(){
		/*
		if( chooseTheta == true){
			chooseDelta = false;		
			chooseTheta = false;
			chooseAlpha = false;
			chooseBeta  = false;
			chooseGamma = false;
			chooseWave  = true;
			colorButtonDelta = (color(0,255,255,100));
			colorButtonTheta = (color(0,255,255,100));
			colorButtonAlpha = (color(0,255,255,100));
			colorButtonBeta = (color(0,255,255,100));
			colorButtonGamma = (color(0,255,255,100));
			colorButtonAll = color(255,199,0,100);			
		}	
		*/
		if( chooseTheta == false) {
			chooseDelta = false;
			chooseTheta = true;
			chooseAlpha = false;
			chooseBeta  = false;
			chooseGamma = false;
			chooseWave  = false;
			
			
			currentDelta = 1;
			currentTheta = 0;
			currentAlpha = 1;
			currentBeta  = 1;
			currentGamma = 1;
			
			colorButtonDelta = color(0,0,0,100);		
			colorButtonTheta = color(255,255,255,100);	
			colorButtonAlpha = color(0,0,0,100);
			colorButtonBeta  = color(0,0,0,100);
			colorButtonGamma = color(0,0,0,100);			
			colorButtonAll   = color(0,0,0,100);			
		}	
		
	}
	
	public void alphaWave(){
		
		
		if( chooseAlpha == false) {
			chooseDelta = false;
			chooseTheta = false;
			chooseAlpha = true;
			chooseBeta  = false;
			chooseGamma = false;
			chooseWave  = false;
			
			currentDelta = 1;
			currentTheta = 1;
			currentAlpha = 0;
			currentBeta  = 1;
			currentGamma = 1;
			
			colorButtonDelta = color(0,0,0,100);
			colorButtonTheta = color(0,0,0,100);	
			colorButtonAlpha = color(255,255,255,100);
			colorButtonBeta =  color(0,0,0,100);
			colorButtonGamma = color(0,0,0,100);			
			colorButtonAll   = color(0,0,0,100);			
		}	
					
	}
	
	public void betaWave(){
		if( chooseBeta == false) {
			chooseDelta = false;
			chooseTheta = false;
			chooseAlpha = false;
			chooseBeta  = true;
			chooseGamma = false;
			chooseWave  = false;
			
			currentDelta = 1;
			currentTheta = 1;
			currentAlpha = 1;
			currentBeta  = 0;
			currentGamma = 1;
			
			colorButtonDelta = color(0,0,0,100);
			colorButtonTheta = color(0,0,0,100);	
			colorButtonAlpha = color(0,0,0,100);
			colorButtonBeta  = color(255,255,255,100);
			colorButtonGamma = color(0,0,0,100);			
			colorButtonAll   = color(0,0,0,100);			
		}	
	}	
	
	public void gammaWave(){	
		if( chooseGamma == false) {
			chooseDelta = false;
			chooseTheta = false;
			chooseAlpha = false;
			chooseBeta  = false;
			chooseGamma = true;
			chooseWave  = false;
			
			currentDelta = 1;
			currentTheta = 1;
			currentAlpha = 1;
			currentBeta  = 1;
			currentGamma = 0;
			
			colorButtonDelta = color(0,0,0,100);
			colorButtonTheta = color(0,0,0,100);	
			colorButtonAlpha = color(0,0,0,100);
			colorButtonBeta  = color(0,0,0,100);
			colorButtonGamma = color(255,255,255,100);			
			colorButtonAll   = color(0,0,0,100);			
		}		
		
	}
	
	
	
	
	Scanner coherenceScanner;
	public void ECP(){

		
		
		//here I open a new window for coherence/patient
		String[] args = {"Coherence in patient"};
		windowForDrawCoherence = new drawingWindowCoherence();
		PApplet.runSketch(args, windowForDrawCoherence);	
	
	}
	
	
	public void StreamButton(){
		 StreamFunction();
	}
	public void StreamButtonPause(){
		 StreamFunction();
	}
	
	float tempTimeCounter = 0;
	public void StreamFunction(){
		//choose play-back or live
			if( playbackLiveFlag == false){
						if(state == false){	
							
							if(toggleMu.getBooleanValue() == false)
								EnablePort();
							else{
								MuMode();
							}
								
							tempTimeCounter = millis();
							timeCounter = tempTimeCounter - millis();
						//	StreamButton.setVisible(false);
						//	StreamButtonPause.setVisible(true);
							
						}
						else
					      {
							state=false;
					        stopSendingData();
					        delay(10);    
					        ClosePort();    
					        delay(1000); // delay to ensure that the last data were drawn
					        flagBuffer 		= -1;
							flagForDraw		= false;
							flagForDraw2	= false;
							flagFilterUsed1 = false;
							flagFilterUsed2 = false;
							flagFilterUsed3 = false;
							
						//	StreamButtonPause.setVisible(false);
						//	StreamButton.setVisible(true);
					      }							
				}
			else{
				if(playbackTrigger == false){
					playbackFormatter();
					playbackFlag = true;
					//print("all right here");
					startTime = millis();
					playbackTrigger = true;
					
					tempTimeCounter = millis();
					timeCounter = tempTimeCounter - millis();
				//	StreamButton.setVisible(false);
				//	StreamButtonPause.setVisible(true);
				}
				else{
					playbackFlag = false;
					sPlayback.close();//close Formatter
					playbackTrigger = false;
					//StreamButtonPause.setVisible(false);
				//	StreamButton.setVisible(true);
					
					
					//playbackLiveFlag = false;
				}
			}
				
		}
	
	
			  String check=null;
			  public void EnablePort(){	
				
	          check = OpenPort();  
	          
	          
	          delay(100);      
	          if(check == null){
	        	  
	            println("nothing happens");          
	          }
	          else{  
	        	state = true;
	            startSendingData();  
	            delay(100);
	               }           		
	}
			  
			  public void MuMode(){
				  check = OpenPort();  
		          
		          
		          delay(100);      
		          if(check == null){
		        	  
		            println("nothing happens");          
		          }
		          else{  
		        	state = true;
		            
		        	 println("start sending Mu data"); 	      
		        	 myPort.write("d"); // -->"d" for default settings  
			   	     delay(100);
			   	     
			   	     
			   	  
			   	     for(int i=0;i<channel_number;i++){
			   	    	 
			   	    	 if((i>=10)&&(i<12)){
			   	    		delay(200);
			   		        ActivateChannel(i);	
			   	    	 }
			   	    	 else{
			   	    		delay(200);
			   	    		DeactivateChannel(i);
			   	    		 
			   	    	 }
			   		                
			   		      }
			   	     
			   	     delay(1000);
			   	     myPort.write(command_startBinary); //send signal to start streaming  
			        	
		        	
		             delay(100);
		         }           		
			  }
	
	
	
	
	public String OpenPort() {
	      
		
		
		
        String portName = SeekPort(); // Seek the port you are using and open it
        portName = "COM7";
        if(portName!=null){
	           
          
          
           myPort = new Serial(this,portName,baud); //define myPort as the previously founded port  
           myPort.write(command_stop); //send signal to stop streaming   
           myPort.clear(); // clear anything in the com port's buffer          
            
          
           println(portName);
           System.out.println("Port is ");
           System.out.println(portName);
           System.out.println(myPort);
      
           return portName;
           
        }
        else{
           System.out.println("Please insert the Arduino device and restart the program ");
           textSize(20);
           fill(255, 200, 153);
           text("Please connect the device",CENTER, 1);
           textSize(20);
           fill(255, 200, 153);
           delay(100);
        }     
        return null;     
    }
	
	public void ClosePort(){	
		
		  println("closing the port");
		  SerialPort trnOff = myPort.port;
		  try {
			trnOff.closePort();
		} catch (SerialPortException e) {
			
			e.printStackTrace();
		}
		  println("closing the port");
		 
		}
	
	
	
	
	 static final String SeekPort() {
	        String[] ports = Serial.list();
	        for (String p : ports)  for (int i = 1; i <= 5; ++i)
	          if (p.equals("COM" + i))  return p;
	       
	        return null;
	      }
	
	
	 public void startSendingData(){
	      println("start sending data");
	      
	     
	      
	      myPort.write("d"); // -->"d" for default settings  
	      delay(100);
	      for(int i=0;i<channel_number;i++){
		        delay(200);
		        ActivateChannel(i);	        
		      }
	      delay(1000);
	      myPort.write(command_startBinary); //send signal to start streaming  
	      //delay(1000);
	      /*for(int i=0;i<channel_number;i++){
		        delay(200);
		        ActivateChannel(i);	        
		      }
	      delay(1000);
	      myPort.write(command_startBinary); //send signal to start streaming  
	      delay(100);
	      
	     */
	      
	      
	      
	     
	     
	      
	      
	     
	      
	    }
	    

	    public void stopSendingData(){
	      println("stop sending data");
	      
	      
	      for(int i=0;i< channel_number;i++)
	        DeactivateChannel(i);
	      delay(100);
	      myPort.write(command_stop); //send signal to stop streaming    
	      delay(100);
	    }
	 
	    
	    public void ActivateChannel(int i) { //activates channel
	        delay(100);
	         myPort.write(command_activate_channel[i]);
	        
	      }

	      public void DeactivateChannel(int i) { //activates channel
	         myPort.write(command_deactivate_channel[i]);
	         delay(100);
	      }
	    
	      public int interpret24bitAsInt32(byte[] byteArray) {     
	          //little endian
	          int newInt = ( 
	            ((0xFF & byteArray[0]) << 16) |
	            ((0xFF & byteArray[1]) << 8) | 
	            (0xFF & byteArray[2])
	            );
	          if ((newInt & 0x00800000) > 0) {
	            newInt |= 0xFF000000;
	          } else {
	            newInt &= 0x00FFFFFF;
	          }
	          return newInt;
	        }
	      
	      
	      int current_packet = 0;
	      
	      
	     public void serialEvent(Serial port) {   
	      if ( myPort.available() > 0 ) 
	       { 
	               
	                buff =  port.read(); //Returns the first byte of incoming serial data available (or -1 if no data is available) - int
	                byte byteRead =(byte) buff;
	               
	                 validc[counterfromzero]++; //counter for checking if packet is full
	           
	            if(BYTE_START == byteRead){                          
	                  //  println(" first byte ");
	                  //  print(" ");
	                    counter=0;
	                    localCounter=0;
	                    
	                   
	                    
	                  //  print ("       first         ");
	                  }           
	            else if(BYTE_END == byteRead){           
	             /**/
	            //	println();
	            //	print("channels used are "); println(channelCounter);
	            	
	            	int temp_channel_number = 8;
			            	if(switch_to_16 == true ){
			            		temp_channel_number = 16;
			            	}
			            	
			            		
	            		//    print("channels used from "); println(temp_channel_number-8 + " to " + temp_channel_number);
	            			for(int y = temp_channel_number - 8; y< temp_channel_number ;y++){
	            			    x.format("%f ", dataPacket[y][counterfromzero]); 
	            				x.format("%s"," ");
	            			}
	            			
	            			
	            			
	            			if(switch_to_16 == true){
	            				
				              if(counterfromzero==249){
				                flagBuffer = 1;
				                 flagForDraw=true;
				                 flagForDraw2=true;
				                 flagFilterUsed1 = false;
				                // print("1");
				              }
				              else if(counterfromzero==499){
				            	  flagBuffer = 2;
				                  flagForDraw=true;
				                  flagForDraw2=true;
				                  flagFilterUsed2 = false;
				               // print("2");
				              }
				              else if(counterfromzero==749){
				                 flagBuffer = 3;
				                 flagForDraw=true;
				                 flagForDraw2=true;
				                 flagFilterUsed3 = false;
				               // print("3");
				                counterfromzero=-1;
				              }
				              
				              
				              counterfromzero++;  
	            			}
	            		
	            			
	            		//	 print("counterfromzero is "); println(counterfromzero);
				              counter=0;
				              localCounter=0;
				              channelCounter=0;
				              
				              
				              
				              
	                //   print ("       last         ");	                
	             //   println();                  
	            }
		            else
				       {
			              counter++;
				              if(  counter == 1 ){
				             // println("2");                
				              packetCounter = (int)buff;         
				              //Bytes 1: Packet counter  
				              
				             
					            //	println();
					            //	print("PACKETCOUNTER IS ");
					           // 	println(packetCounter);
					            
					            	if(packetCounter%2 == 0){
					            		switch_to_16 = false;
					            		channelCounter = 0;
					            	}
					            	else{
					            	switch_to_16 = true;
					            	channelCounter = 0;
					            	}
				              }   
					              else if( counter < 26 ){
					            	  
							               // println("3");
							                //if channel bytes
							                bufferOfThree[localCounter] = byteRead; //store the byte in the buffer	              
								            if( localCounter == 2 ){
								                  
								                  bufferData[channelCounter] = interpret24bitAsInt32(bufferOfThree);   //24 to 32
								                  
								                  int temp_channel_counter = 0;
								                  if( switch_to_16 == true)
								                	  temp_channel_counter = 8;
								                  dataPacket[channelCounter + temp_channel_counter][counterfromzero] = scale_fac_uVolts_per_count * (float) bufferData[channelCounter]; //change into volts
								                 // x.format("%f", dataPacket[channelCounter][counterfromzero]); 
								               //   x.format("%s"," ");
												 
								                  // print(" " + dataPacket[channelCounter + temp_channel_counter][counterfromzero]);
								                   
								                   int ggfggf = channelCounter + temp_channel_counter;
								                //   print( "   pos is -->" + ggfggf + "   ");
								                  localCounter=0;
								                  
								                  if( counter == 25){
									            	  channelCounter = 0;
									            	  
									              }
								                  else
								                  channelCounter++;
								              
								                }
							                else           
							                  localCounter++;  	             
					              	}  
					               
			             }   
	            
	            
	          //  println();
	         //   print("channelCounter-->");print(channelCounter);print("   ");
	         //   print("switch_to_16-->");print(switch_to_16);print("   ");
	         //   print("counterfromzero-->");print(counterfromzero);print("   ");
	           
	            
	            }    
	    }
	      
	     
	     
	     public void toggleMuRhythm()
		  {
		    channelMuFlag = toggleMu.getBooleanValue();
		    if(toggleMu.getBooleanValue())
		    { 	
		    	toggleMu.setColorActive(toggleON);
		    	currentMu = 0;
		    }
		    else
		    {
		    	toggleMu.setColorActive(toggleOFF);
		    	currentMu = 1;
		    }
		  }
	      
	
	//---Functions for Spectro Group-----------------------FINISH------------------------------------------//
	
	
	//---Functions for the Menu----------------------------START -----------------------------------------//
	
	public void menuSetup()
	{
		scrollList = new ControlP5(this);
		scrollList.setColorCaptionLabel(white);
		
		fileList = scrollList.addScrollableList("File");
	    fileList.setPosition(0,0);
	    fileList.setSize(200, 100);
	    fileList.setBarHeight(20);
	    fileList.setItemHeight(20);
	    fileList.addItems(fileAdds);
	    fileList.setType(ScrollableList.LIST);
	    fileList.isBarVisible();
	    fileList.setColorBackground(backColorScroll);
	    fileList.setColorActive(activeColorScroll);
	    fileList.setColorForeground(foreColorScroll);
	    fileList.close();
	    
	    settingsList = scrollList.addScrollableList("Settings");
		settingsList.setPosition(201,0);
		settingsList.setSize(200, 100);
		settingsList.setBarHeight(20);
		settingsList.setItemHeight(20);
		settingsList.addItems(settingsAdds);
		settingsList.setType(ScrollableList.LIST);
		settingsList.isBarVisible();
		settingsList.setColorBackground(backColorScroll);
		settingsList.setColorActive(activeColorScroll);
		settingsList.setColorForeground(foreColorScroll);
		settingsList.close();		    
	    
	    
		
		filterList = scrollList.addScrollableList("Filters");
		filterList.setPosition(402,0);
		filterList.setSize(200, 100);
		filterList.setBarHeight(20);
		filterList.setItemHeight(20);
		filterList.addItems(filterAdds);
		filterList.setType(ScrollableList.LIST);
		filterList.isBarVisible();
		filterList.setColorBackground(backColorScroll);
		filterList.setColorActive(activeColorScroll);
		filterList.setColorForeground(foreColorScroll);
		filterList.close();		    
		
	    
	    
		helpList = scrollList.addScrollableList("Help");
		helpList.setPosition(603,0);
		helpList.setSize(200, 100);
		helpList.setBarHeight(20);
		helpList.setItemHeight(20);
		helpList.addItems(helpAdds);
		helpList.setType(ScrollableList.LIST);
		helpList.isBarVisible();
		helpList.setColorBackground(backColorScroll);
		helpList.setColorActive(activeColorScroll);
		helpList.setColorForeground(foreColorScroll);
		helpList.close();
		
		
		gapList = scrollList.addScrollableList("");
		gapList.setPosition(804,0);
		gapList.setSize(width - 400 , 100);
		gapList.setBarHeight(20);
		gapList.setItemHeight(20);
		gapList.setType(ScrollableList.LIST);
		gapList.isBarVisible();	
		gapList.setColorForeground(foreColorScroll);
		gapList.close();
		
	}
	
	public void File(int n)
	{
		
		println(n, scrollList.get(ScrollableList.class, "File").getItem(n));
		System.out.println(n);
		
		
		if(n == 0)
		{
			fileList.close();
			//here I open a new window for autistic tests
			String[] args = {"Statistical Evaluation Test"};
			windowForDrawAutism = new drawingWindowAutism();
			PApplet.runSketch(args, windowForDrawAutism);		
		}
		else if(n == 1)
		{
			fileList.close();
			Live();
		}
		else if(n == 2)
		{
			fileList.close();
			openFile();
		}
		else if(n == 3)
		{
			fileList.close();
			saveFile();
		}
		else if(n == 4)
		{
			fileList.close();
			exit();
		}
		

	}
	
	public void Settings(int n){
		println(n, scrollList.get(ScrollableList.class, "Settings").getItem(n));
		System.out.println(n);
	
		if(n == 0)
		{
			if( (stepArd < millis() - 500) && (state == true) )
			{
				stepArd = millis();
			if( flagG == 0 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x24";
				settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1,stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				gain = 24;
				stringGain_Set = "6";
				
				
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);				
					myPort.write("x");
				}
				}
				
			}
			else if( flagG == 1 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x1";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				stringGain_Set = "0";
				gain = 1;
			  if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
			  }
			}
			else if( flagG == 2 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x2";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				
				stringGain_Set = "1";
				gain = 2;	
				if( myPort.available() > 0){
					//send the updates to arduino			
					for(int i=0;i<channel_number;i++){	
						myPort.write("x");
						char ch = (char) i;
						myPort.write(ch);
						myPort.write(stringPower_Down);
						myPort.write(stringGain_Set);
						myPort.write(stringInput_Type_Set);
						myPort.write(stringGround_Set);
						myPort.write(stringRefEl2_Set);
						myPort.write(stringRefEl1_Set);
						myPort.write("x");
					}
				}
			}
			else if( flagG== 3 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x4";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				
				stringGain_Set = "2";
				gain = 4;
				if( myPort.available() > 0){
				//send the updates to arduino			
					for(int i=0;i<channel_number;i++){	
						myPort.write("x");
						char ch = (char) i;
						myPort.write(ch);
						myPort.write(stringPower_Down);
						myPort.write(stringGain_Set);
						myPort.write(stringInput_Type_Set);
						myPort.write(stringGround_Set);
						myPort.write(stringRefEl2_Set);
						myPort.write(stringRefEl1_Set);
						myPort.write("x");
					}
				}
			}
			else if( flagG== 4 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x6";
			    settingsAdds  = Arrays.asList( stringG, stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				
				stringGain_Set = "3";
				gain = 6;
				if( myPort.available() > 0){
				//send the updates to arduino			
					for(int i=0;i<channel_number;i++){	
						myPort.write("x");
						char ch = (char) i;
						myPort.write(ch);
						myPort.write(stringPower_Down);
						myPort.write(stringGain_Set);
						myPort.write(stringInput_Type_Set);
						myPort.write(stringGround_Set);
						myPort.write(stringRefEl2_Set);
						myPort.write(stringRefEl1_Set);
						myPort.write("x");
					}
				}
			}
			else if( flagG== 5 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x8";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG++;
				
				stringGain_Set = "4";
				gain = 8;
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			else if( flagG== 6 ){
				settingsList.removeItems(settingsAdds);
				stringG    = "Gain                         x12";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagG = 0;
				
				stringGain_Set = "5";
				gain = 12;
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			}
			
		}
		else if(n == 1)
		{
			
			backgroundColor = color(255,255,255);
			if( (stepArd < millis() - 500) && (state == true) )
			{
				stepArd = millis();
			if( flagB == true ){
				settingsList.removeItems(settingsAdds);
				stringB    = "Ground                         Off";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagB = false;
				
				stringGround_Set = "0";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			else {
				settingsList.removeItems(settingsAdds);
				stringB    = "Ground                         On";
				settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagB = true;
				
				stringGround_Set = "1";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			}
		}
		else if(n == 2)
		{
			if( (stepArd < millis() - 500) && (state == true) )
			{
				stepArd = millis();
			backgroundColor = color(255,255,255);
			if( flagRefEl1 == true ){
				settingsList.removeItems(settingsAdds);
				stringRefEl1 = "Ref El 1                        Off";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagRefEl1 = false;
				
				stringRefEl1_Set = "1";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			else {
				
				settingsList.removeItems(settingsAdds);
				stringRefEl1 = "Ref El 1                        On";
				settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagRefEl1 = true;
				
				stringRefEl1_Set = "0";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			}
		}
		else if(n == 3)
		{
			if( (stepArd < millis() - 500) && (state == true) )
			{
				stepArd = millis();
			backgroundColor = color(255,255,255);
			if( flagRefEl2 == true ){
				settingsList.removeItems(settingsAdds);
			    stringRefEl2 = "Ref El 2                        Off";
			    settingsAdds  = Arrays.asList( stringG,  stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagRefEl2 = false;
				
				stringRefEl2_Set = "0";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			else {
				settingsList.removeItems(settingsAdds);
				stringRefEl2 = "Ref El 2                        On";
				settingsAdds  = Arrays.asList( stringG, stringB, stringRefEl1, stringRefEl2);
				settingsList.addItems(settingsAdds);
				flagRefEl2 = true;
				
				stringRefEl2_Set = "1";
				if( myPort.available() > 0){
				//send the updates to arduino			
				for(int i=0;i<channel_number;i++){	
					myPort.write("x");
					char ch = (char) i;
					myPort.write(ch);
					myPort.write(stringPower_Down);
					myPort.write(stringGain_Set);
					myPort.write(stringInput_Type_Set);
					myPort.write(stringGround_Set);
					myPort.write(stringRefEl2_Set);
					myPort.write(stringRefEl1_Set);
					myPort.write("x");
				}
				}
			}
			}
		}
	}
	

	
	
	
	
	public void Filters( int n ){
		println(n, scrollList.get(ScrollableList.class, "Filters").getItem(n));
		System.out.println(n);
		if(n == 0)
		{
			if( (stepArd < millis() - 500)  )
			{
				stepArd = millis();
			if( flagBP == 0 ){
				
				filterList.removeItems(filterAdds);
				stringBP    = "BandPass      Filter           1   - 50 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP++;
				currentBP = 0;
				
			}
			else if( flagBP == 1 ){
				
				filterList.removeItems(filterAdds);
				stringBP   = "BandPass      Filter           7  - 13 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP++;
				currentBP = 1;
			
				
			}
			else if( flagBP == 2 ){
				filterList.removeItems(filterAdds);
				stringBP    = "BandPass      Filter           15  - 50 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP++;
				currentBP = 2;
				
			}
			else if( flagBP == 3 ){
				filterList.removeItems(filterAdds);
				stringBP    = "BandPass      Filter           5   - 50 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP++;
				currentBP = 3;
		        
			}
			else if( flagBP == 4 ){
				filterList.removeItems(filterAdds);
				stringBP    = "No                     BandPass     filter";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP ++;
				currentBP = 4;
		       
			}
			else if( flagBP == 5 ){
				filterList.removeItems(filterAdds);
				stringBP    = "BandPass      Filter           0.3  - 30 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagBP = 0;
				currentBP = 5;
			}
			
			}
		}
		else if(n == 1)
		{
			if( (stepArd < millis() - 500) )
			{
				stepArd = millis();
			if( flagNotch == 0 ){
				filterList.removeItems(filterAdds);
				stringNotch   = "Notch              Filter           60 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagNotch ++;
				currentNotch = 0;
			}
			else if( flagNotch == 1 ){
				filterList.removeItems(filterAdds);
				stringNotch     = "Notch              Filter           50 Hz";
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagNotch ++;
				currentNotch = 1;
	       
			}
			else if( flagNotch == 2 ){
				filterList.removeItems(filterAdds);
				stringNotch     = "No                     Notch            Filter";				
				filterAdds  = Arrays.asList(stringBP, stringNotch);
				filterList.addItems(filterAdds);
				flagNotch = 0;
				currentNotch = 2;
		        
			}
			}
		}
		
	}
	
	
	
	
	
	
	public void Help(int n)
	{
		
		println(n, scrollList.get(ScrollableList.class, "Help").getItem(n));
		System.out.println(n);
		if(n == 0)
		{
			backgroundColor = color(255,255,255);
			helpList.close();
			String[] args = {"About"};
			windowForDrawAbout = new drawingWindowAbout();
			PApplet.runSketch(args, windowForDrawAbout);
		}
		else if(n ==1){
			backgroundColor = color(255,255,255);
			helpList.close();
			
				String[] args = {"Documentation"};
				windowForDrawDocumentation = new drawingWindowDocumentation();
				PApplet.runSketch(args, windowForDrawDocumentation);
				
			
		}
		
	}
	
	
	
	public void Live(){
		playbackLiveFlag = false;
		playbackTrigger = false;
		createFile();
		
		
			for(  int i=0; i < 1250; i++){ 	
				for(int j =0;j< channel_number;j++){
					//dataPacket[i][j] = 0f;
	             myChart[j].push(dataset[j], 0f);
			}
			
		}
		/**/
	}
	
	
	public void openFile()
	{
		try {
			SwingUtilities. invokeLater(new Runnable() {
				public void run() {
					
					file_chooser.setDialogTitle("Load text file");
					file_chooser.setFileSelectionMode(TEXT);
					FileFilter filter = new FileNameExtensionFilter("Only *.txt files...","txt");
					file_chooser.setFileFilter(filter);
					
			        int return_val = file_chooser.showOpenDialog(null);
			        
			        if ( return_val == JFileChooser.CANCEL_OPTION )
			        {
			        	System.out.println("canceled");
			        }
			        if ( return_val == JFileChooser.ERROR_OPTION )
			        {
			        	System.out.println("error");
			        }
			        if ( return_val == JFileChooser.APPROVE_OPTION )
			        {  
			        	System.out.println("approved");
			        }
			        if ( return_val == JFileChooser.APPROVE_OPTION ) 
			        {
			          
			          theFile = file_chooser.getSelectedFile();
			          file_name = theFile.getAbsolutePath();
			          System.out.println(file_name);
			          //You opened a file,so we have playback now
			          
			          //close fileFormatter for live
			          x.close(); // close formatter	 
			          playbackLiveFlag = true;
			          playbackTrigger = false;
			          //Open formatter for the read file
			          playbackFormatter();
			          
			          
			          for(  int i=0; i < 1250; i++){ 	
							for(int j =0;j<channel_number;j++){							
				             myChart[j].push(dataset[j], 0f);
						}
			          }
			          
			       
			        } 
			        else 
			        {
			          file_name = "none";
			        }
				}
			});
		}
		catch (Exception e) 
		{
			    e.printStackTrace();
		}		
	}
	
	
	
	 void saveFile() {
		 
		  try {
		    SwingUtilities. invokeLater(new Runnable() {
		      public void run() {
		    	  
		    	file_chooser.setDialogTitle("Save a file");  
		    	file_chooser.setFileSelectionMode(TEXT);
		    	FileFilter filter = new FileNameExtensionFilter("Save as *.txt","txt");
		    	file_chooser.setFileFilter(filter);
		        int return_val = file_chooser.showSaveDialog(null);
		        
		        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
		        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
		        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
		        if ( return_val == JFileChooser.APPROVE_OPTION ) {
		        	
		     
		        	
		          File file = file_chooser.getSelectedFile();
		         
		         	try  
		            {  
         		
		            BufferedWriter writer = new BufferedWriter( new FileWriter( file.getAbsolutePath()+".txt"));  
		  
		          //  for(int yui=0; yui<1;yui++){
		
						writer.write("0,000000");
						writer.write(" ");
		           // }
		            Scanner s;
	         		 s = new Scanner(tempFile);
	  
					String l = s.next();
					println(l);
					while(s.hasNext())
					{
						l = s.next(); 
						writer.write(l);
						writer.write(" "); 
					}
					s.close();
					writer.close( ); 
		           
		            
		            print("The file was Saved Successfully!");
		                     
		            }  
		            catch (IOException e)  
		            {  
			            print("The Text could not be Saved!");  
		            }  


		
		          file_name = file.getAbsolutePath();
		          System.out.println(file_name);
		        } else {
		          file_name = "none";
		        }
		      }
		    }
		    );
		  }
		  catch (Exception e) {
		    e.printStackTrace();
		  }
		}
	

	
	private void createFile() {
		
		try {
			tempFile = new File("temp.txt");
			x = new Formatter(tempFile);
			
		} catch (FileNotFoundException e) {
			
			System.out.println("An error occured");
		}
	}
	
		
	
	public void playbackFormatter(){
		try {
			sPlayback = new Scanner(theFile);
		 }
		 catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	public void playback(){
		 
		
			timeCounter = millis() - tempTimeCounter;
	
			int start;
		
			float arrayString[][] = new float[channel_number][250];
		 
			
					 for(int i=0;  i<250;  i++){
						 for(int j=0; j<channel_number; j++ ){
							 if(   sPlayback.hasNextFloat() == false  ) {
								 for(int kly=0; kly < 250;kly++){
									 arrayString[j][kly] = 0f; 
								 }
								 arrayString[j][i] = 0f;	
								 //terminate
							 }
							 else
							   arrayString[j][i] = sPlayback.nextFloat();			 
						 	}
					 	}
					 
					 if(sPlayback.hasNextFloat()){
						 
						 //int n;
						 
						 if(switchForBuffer==1){
						 		//n=250;
						 		start=0;
						 	}
						 	else if(switchForBuffer==2){
						 	//	n=500;
						 		start=250;
						 	}
						 	else {
						 		//n=750;
						 		start=500;						 	
						 	}
						 	
						 		
							 for(int i=0;  i<250;  i++){
									 for(int j=0; j< channel_number; j++ ){
										 dataPacket[j][i+start] = arrayString[j][i];	
										 print(dataPacket[j][i]);
									 }
								//	 println();
							 	}
							 
							 if(switchForBuffer==1){							 
							 		flagBuffer = 1;
							 		flagForDraw=true;
					                flagForDraw2=true;
					                flagFilterUsed1 = false;
					                switchForBuffer=2;
							 	}
							 	else if(switchForBuffer==2){							 	
							 		flagBuffer = 2;
					                flagForDraw=true;
					                flagForDraw2=true;
					                flagFilterUsed2 = false;
					                switchForBuffer=3;
							 	}
							 	else {						 
							 		flagBuffer = 3;
					                flagForDraw=true;
					                flagForDraw2=true;
					                flagFilterUsed3 = false;
					                switchForBuffer=1;
							 	}							
					 	}		 	
	}
	
	
	
		
	
	//---Functions for the Menu----------------------------FINISH -----------------------------------------//
	
	
	 static Event ap;
	//--------------Functions for AboutWindow------------------------------------//
	public class drawingWindowAbout extends PApplet
	{
	
		public void settings(){
			size(1000,600,P3D);
		}
		
		
		ControlP5 aboutButtons;
		public void setup(){
			frameRate(4);
			surface.setTitle("About");
			aboutButtons = new ControlP5(this);
			aboutButtons.setColorForeground(colorGreyFore);
			aboutButtons.setColorBackground(colorGreyBack);
			aboutButtons.setColorActive(colorGreyActive2);
			aboutButtons.setColorCaptionLabel(white);
			
			
				}
		
		
		public void exit(){
			stop();
		}
		
		
		
		
		public void draw(){
			
			background(255,255,255);
			
			stroke(0);
			fill(0);
			
			//for(int i = 0;i < Desc.length ; i++){ 
				text("Electroencephalography is a  common   technique that is widely used for the observation of brain activity and the detection of"
				 + "\ndiseases and malfunctions. In fact nowadays, EEG functionalities  are not just limited to signals measurement, processing and"
				 + "\nfiltering  but also are guided to more sophisticated computational analysis and imaging representations  for the extraction of"
				 + "\nmultivariate data that reflect the correlation between Neurodegeneration and cognitive impairment. For the Autism case, even"
				 + "\nthough  it is  not yet  classified  as  a   neurodegenerative  disorder, several compelling  evidences  underlie the correlation of"
				 + "\nneuronal damage with the  reduction  of  abilities and skills  in  Autistic  population, revealing any  case of medical,  social or"
				 + "\neducational  treatment  to an  arduous task. In  this  study  a new software based on  the  JAVA  and  Processing  Programming "
				 + "\nLanguages  environments is presented, that detects, displays and analyze EEG signals in order to be  compared and handled"
				 + "\nin terms of music and color. This Art Therapy automated procedure, has two main functionalities, organized in four different"
				 + "\nmodules. The firstincludes the EEG signals real time recording, filtering and visualization to a 3d brain model and the second"
				 + "\nguides to the execution of statistical tests between control and autism groups along with the transformation of brain activity to"
				 + "\n sounds sculptures for individualized  psychological treatment and manipulation of Autistic patients.",20,50);
				fill(10, 0, 10);
				textSize(14);
				
			//}
			
		}
		
		
		
		String Desc[] ={"Electroencephalography is a common technique that is widely used for "
				+ "the observation of brain activity and the detection of diseases and malfunctions. "
				+ "In fact nowadays, EEG functionalities are not just limited to signals measurement, "
				+ "processing and filtering but also are guided to more sophisticated computational analysis "
				+ "and imaging representations for the extraction of multivariate data that reflect the correlation"
				+ "between Neurodegeneration and cognitive impairment. For the Autism case, even though it is not yet"
				+ "classified as a neurodegenerative disorder, several compelling evidences underlie the correlation "
				+ "of neuronal damage with the reduction of abilities and skills in Autistic population, revealing any "
				+ "case of medical, social or educational treatment to an arduous task. In this study a new software "
				+ "based on the JAVA and Processing Programming Languages environments is presented, that detects, "
				+ "displays and analyze EEG signals in order to be compared and handled in terms of music and color."
				+ "This Art Therapy automated procedure, has two main functionalities, organized in four different "
				+ "modules. The first includes the EEG signals real time recording, filtering and visualization to a "
				+ "3d brain model and the second guides to the execution of statistical tests between control and "
				+ "autism groups along with the transformation of brain activity to sounds sculptures for individualized"
				+ "psychological treatment and manipulation of Autistic patients." 
		};
		
	}
	//--------------Functions for DocumentationWindow------------------------------------//
	public class drawingWindowDocumentation extends PApplet
	{
		
		public void settings(){
			size(800,600,P3D);
			
		}
		
		public void setup(){
					frameRate(4);
				    surface.setTitle("Documentation");			    
					docButtons = new ControlP5(this);
					docButtons.setColorForeground(colorGreyFore);
					docButtons.setColorBackground(colorGreyBack);
					docButtons.setColorActive(colorGreyActive2);
					docButtons.setColorCaptionLabel(white);
					readMore = docButtons.addButton("readMore");
					readMore.setLabel("Read More");
					readMore.setPosition(width/3,2*height/3);
					readMore.setSize(10*width/120,2*height/60);
					
				}
		public void draw(){
			background(255);
			text("Electroencephalography is a  common   technique that is widely used for the observation of brain activity and the detection of"
					 + "\ndiseases and malfunctions. In fact nowadays, EEG functionalities  are not just limited to signals measurement, processing and"
					 + "\nfiltering  but also are guided to more sophisticated computational analysis and imaging representations  for the extraction of"
					 + "\nmultivariate data that reflect the correlation between Neurodegeneration and cognitive impairment. For the Autism case, even"
					 + "\nthough  it is  not yet  classified  as  a   neurodegenerative  disorder, several compelling  evidences  underlie the correlation of"
					 + "\nneuronal damage with the  reduction  of  abilities and skills  in  Autistic  population, revealing any  case of medical,  social or"
					 + "\neducational  treatment  to an  arduous task. In  this  study  a new software based on  the  JAVA  and  Processing  Programming "
					 + "\nLanguages  environments is presented, that detects, displays and analyze EEG signals in order to be  compared and handled"
					 + "\nin terms of music and color. This Art Therapy automated procedure, has two main functionalities, organized in four different"
					 + "\nmodules. The firstincludes the EEG signals real time recording, filtering and visualization to a 3d brain model and the second"
					 + "\nguides to the execution of statistical tests between control and autism groups along with the transformation of brain activity to"
					 + "\n sounds sculptures for individualized  psychological treatment and manipulation of Autistic patients.",20,50);
					fill(10, 0, 10);
					textSize(14);
		}
	
		
		
		public void exit(){
			stop();		
		}
		public void readMore(){
			
			if (Desktop.isDesktopSupported()) {
			    try {
			        File myFile = new File("xxxxxxxxxx.pdf");
			        Desktop.getDesktop().open(myFile);
			        
			    } catch (IOException ex) {
			        // no application registered for PDFs
			    }
			}
					 
		}
	}
	
	
	public class drawingWindowThreshold extends PApplet{
		
		public void exit(){
			stop();		
		}

		ControlP5 cp5Threshold;
		int[] channelAr;
		

		String textChannel,textHigh,textLow;

		public void settings(){
			 size(800, 600,P3D);
			
		}
		
		public void setup() {
		 
			surface.setTitle("Threshold Controller");
		  channelAr = new int[16];
		  
		  
		  for( int i=0; i< 16; i++){
		    channelAr[i] = i;
		    
		  }
		  
		  for(int i=0; i < 40; i++){
			  timeWindowArrayCounter[i] = 0;
		  }
		  int white = color(255,255,255,255);
		  int sub1 = color(125,255,125,255);
		  int sub2 = color(255,0,0,255);
		  
		  cp5Threshold = new ControlP5(this);

		  cp5Threshold.addTextfield("Channel").setPosition(10, 520).setSize(100, 50).setColorBackground(white).setColorForeground(0).setColor(0).setAutoClear(false);
		  cp5Threshold.addTextfield("Low_Threshold").setPosition(110, 520).setColorBackground(white).setColorForeground(0).setColor(0).setSize(100, 50).setAutoClear(false);
		  cp5Threshold.addTextfield("High_Threshold").setPosition(210, 520).setColorBackground(white).setColorForeground(0).setColor(0).setSize(100, 50).setAutoClear(false);

		  cp5Threshold.addBang("submit").setPosition(311, 521).setSize(100, 48).setColorForeground(sub1).setColorBackground(sub2);
		}

		public void draw() {
		  
		  
		  
		  background(0,0,0,255);
		  textSize(16);
		  fill(255,255,255,255);
		  text("Channel", 10, 20);
		  fill(255,255,255,255);
		  text("Low  bound", 120, 20);
		  text("High bound", 250, 20);
		  textSize(14);
		  fill(0,255,0,255);
		  text("Green", 400, 20);
		  fill(255,255,255,255);
		  text("shows that current threshold was not breached ", 445, 20);
		  fill(255,0,0,255);
		  text("Red", 400, 40);
		  fill(255,255,255,255);
		  text("shows that current threshold was breached ", 430, 40); 
		  text("Values used are scaled in uV ", 400, 60); 
		  
		  
		  
		  textSize(20);
		  String[] elecNames = {"(Fp1)","(Fp2)","(C3)","(C4)","(T5)","(T6)","(O1)","(O2)","(F7)","(F8)","(F3)","(F4)","(T3)","(T4)","(P3)","(P4)"};
		  
		  for(int i=0; i < 16;i++){
			  		//text(channelAr[i] +1, 10, 20 + 30*(i+1)); 
			        fill(255,255,255,255);			      
			        	text((i+1)+" ", 3, 20 + 30*(i+1) ); 
			        	text(elecNames[i], 33, 20 + 30*(i+1) ); 
			        	
					if(DownThreshold[i] <= UpThreshold[i]) {
						
							int temporary;
							boolean flagThresholdLow=true;
							boolean flagThresholdHigh=true;
							
							lightFlag[i] = true;
							
							if(flagBuffer == 1){
								temporary = 0;
							}
							else if(flagBuffer == 2){
								temporary = 250;
							}
							else{
								temporary = 500;
							}
							
							for(int k = temporary; k< temporary+250; k++){
								if((DownThreshold[i] > dataPacket[i][temporary]*100f) && ( dataPacket[i][temporary]*100f) <=UpThreshold[i]){
									flagThresholdLow = false;
									 lightFlag[i] = false;
									 int timeRound = (int)(timeCounter/1000f);
									 println("timeRound is "+timeRound);
									if((timeWindowArrayCounter[i]!=0)){
										if((timeWindowArrayCounter[i]<40)){											
											if(timeWindowArray[i][timeWindowArrayCounter[i]-1] != timeRound ){
												timeWindowArray[i][timeWindowArrayCounter[i]] = timeRound;
												timeWindowArrayCounter[i]++;
											}
										}
										
									}else if (timeWindowArrayCounter[i]==0){
											timeWindowArray[i][timeWindowArrayCounter[i]] = timeRound;
											timeWindowArrayCounter[i]++;
										}	
								}
								else if((DownThreshold[i] <= dataPacket[i][temporary]*100f) && (UpThreshold[i] < dataPacket[i][temporary]*100f)){
									flagThresholdHigh = false;
									lightFlag[i] = false;
									int timeRound = (int)(timeCounter/1000f);
									println("timeRound is "+timeRound);
									if((timeWindowArrayCounter[i]!=0)){
										
										if((timeWindowArrayCounter[i]<40)){	
											
											if(timeWindowArray[i][timeWindowArrayCounter[i]-1] != timeRound){
												timeWindowArray[i][timeWindowArrayCounter[i]] = timeRound;
												timeWindowArrayCounter[i]++;
											}
										}
										
									}
									else if (timeWindowArrayCounter[i]==0){
											timeWindowArray[i][timeWindowArrayCounter[i]] =  timeRound;
											timeWindowArrayCounter[i]++;
										}	
								}
							}
						
									if((flagThresholdLow == true)&&(flagThresholdHigh == true)){
										 fill(0,255,0,255);
									     text(DownThreshold[i], 120, 20 + 30*(i+1));
									     fill(0,255,0,255);
									     text(UpThreshold[i], 250, 20 + 30*(i+1)); 
									}
									else if((flagThresholdLow == false)&&(flagThresholdHigh == true)){
										 fill(255,0,0,255);
									     text(DownThreshold[i], 120, 20 + 30*(i+1));
									  	 fill(0,255,0,255);
									     text(UpThreshold[i], 250, 20 + 30*(i+1)); 
									}
									else if((flagThresholdLow == true)&&(flagThresholdHigh == false)){
										 fill(0,255,0,255);
									     text(DownThreshold[i], 120, 20 + 30*(i+1));
									     fill(255,0,0,255);
									     text(UpThreshold[i], 250, 20 + 30*(i+1)); 
									}
						
					}
					else{
						//invalid values for tresholds
						 fill(255,0,0,255);
					     text(DownThreshold[i], 120, 20 + 30*(i+1));
					     fill(255,0,0,255);
					     text(UpThreshold[i], 250, 20 + 30*(i+1));
					     fill(255,255,255,255);
					     text("invalid values", 380, 20 + 30*(i+1));
					     lightFlag[i] = false;
					}
					
		  
		  }
		  
		  
		
		  
		}

		public void submit() {
		  
		  print("this is the text you typed :");
		  textChannel=cp5Threshold.get(Textfield.class, "Channel").getText();
		  textLow = cp5Threshold.get(Textfield.class, "Low_Threshold").getText();
		  textHigh = cp5Threshold.get(Textfield.class, "High_Threshold").getText();
		  
		  if( ( ( parseFloat(textChannel))  > 0) && (parseFloat(textChannel)) <= 16) { 
		  UpThreshold[parseInt(textChannel) -1] = parseFloat(textHigh)  ;
		  DownThreshold[parseInt(textChannel) -1] = parseFloat(textLow) ;
		  }
		  
		    
		  
		  
		  print(textChannel);
		  print(" "+textLow);
		  print(" "+textHigh);
		}
	}
	
	public class drawingWindowTime extends PApplet{
		
		public void settings(){
			 size(1200, 600,P3D);	
		}
		
		public void exit(){
			stop();		
		}
		
		public void setup(){
			 	surface.setTitle("Time");
			 	frameRate(1);
			 	for(int i =0; i< channel_number; i++){
				 	for(int j =0; j < 40; j++){
				 		timeWindowArray[i][j] = 0;
				 	}
			 	}
		}
		
		public void draw(){
			String[] elecNames = {"(Fp1)","(Fp2)","(C3)","(C4)","(T5)","(T6)","(O1)","(O2)","(F7)","(F8)","(F3)","(F4)","(T3)","(T4)","(P3)","(P4)"};
			background(0);
			fill(255,255,255,255);
			text("Channel", 10, 20);
			  
		     
		 	for(int i=0; i < 16; i++){
		 		textSize(12);
		 		fill(255,255,255,255);
		 		text((i+1)+" ", 2, 20 + 30*(i+1) ); 
	        	text(elecNames[i], 22, 20 + 30*(i+1) ); 	        	
			   
	        	
	        	for(int j=0; j < timeWindowArrayCounter[i] ; j++){
	        		fill(255,255,255,255);
	        		text( timeWindowArray[i][j]+", ", 32 + 26f*(j+1), 20 + 30*(i+1) ); 
	        			
	        	}
	        	/**/
		 	}
		 	
		 	
		 	
		}
		
	}
	
	public class drawingWindowCoherence extends PApplet{

		ControlP5 cp5;
		int channel_number = 16;
		
		Chart[] myChart = new Chart[120];
		String [] El_name = { "Fp1","Fp2","C3","C4","T5","T6","O1","O2","F7","F8","F3","F4","T3","T4","P3","P4"};
		
		
		
		public void formatterCoherence(){
			try {
				coherenceScanner = new Scanner(theFile);
			 }
			 catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		
		
		public void exit(){
			stop();
			//frame.dispose();
		}
		
		
		
		public void settings(){
			
			 size(1340, 692,P3D);
		}
		
		public void setup() {
			frameRate(1);
			surface.setTitle("Coherence in Patient");
			//first part--reading the file
			float detl;
			
			
			formatterCoherence();
			int counterSizeFile=0;
			while(coherenceScanner.hasNextFloat()){
				counterSizeFile++;
				detl = coherenceScanner.nextFloat();	
			}
			coherenceScanner.close();//close Formatter
			
			
			formatterCoherence();			
			float arrayCoherence[][] = new float[channel_number][counterSizeFile + 1];
			for(int i=0; i<channel_number;i++){
				for(int j=0; j<counterSizeFile+1;j++)
					arrayCoherence[i][j] = 0;
			}
			println("size of file is "+counterSizeFile);
			int counterEachChannelLength = 0;
			int counterChannel =0;		
			
			while(coherenceScanner.hasNextFloat()){		
				arrayCoherence[counterChannel][counterEachChannelLength] = coherenceScanner.nextFloat();	
				
				if(counterChannel == 15){
					counterChannel = 0;
					counterEachChannelLength++;
				}
				else
					counterChannel++;
				
				
			}
			
			
			
			
			println("counterEachChannelLength is "+counterEachChannelLength);
			coherenceScanner.close();//close Formatter
			
			
			//second part, use coherence
			// vriskoume poia dinami tou 2 einai, meta exoume Cxx[][] Cxy[][] Cyy[][]
			   float Vpow = 1;
			   for(int ia = 0; ia < counterEachChannelLength; ia++){
				   if(Vpow < ia){
					   Vpow = Vpow*2;		   
				   }
			   }
			  
			 println("Vpow is "+Vpow);
			 
			 
			 FFT[][] fft;			 
			 int N = 256;             //length of segment
			 int L;                   //number of segments
			 int M;                  //stepsize
			 float[][][] segment;
			 float[][][] Sxx, Sxy;
			

			 
			 
			
			 M = N/2;
			 L = ((int)Vpow - N )/M  + 1;
			 
			 segment = new float[channel_number][L][N];
			 
			 //initialize segments
			 for( int ch = 0; ch < channel_number; ch++){
			 for( int l = 0; l < L; l++){  
			       for( int k = 0; k < N; k++){
			       segment[ch][l][k] = 0;			    		      
			     }
			 }
			 }
			 
			 
			 
			//find all segments from all signals
			 for( int ch = 0; ch < channel_number; ch++){
			       for( int l = 0; l < L; l++){  
			           for( int k = 0; k < N; k++){
			             segment[ch][l][k] = arrayCoherence[ch][k + l*M] * hammingWindow(arrayCoherence[ch][k],((float)k),((float)N));                
			             println("ch is "+ch+" l is "+l+ " k is "+  k +" M is "+M);	         
			         }
			     }
			 }
		 
			 
			 
			
			 fft = new FFT[channel_number][L];
			
			 for( int i = 0; i < channel_number; i++){
				 for( int j = 0; j < L; j++){  
				  fft[i][j] = new FFT(N, 256); 
				  // create an Oscil we'll use to fill up our buffer  
				   fft[i][j].forward(segment[i][j]);		       
				  }
			 }
				 
			 
				Sxx = new float[channel_number][L][N];
				Sxy = new float[120][L][N];
				  
				
				
				//finding Sxy
				// twisting the counter
				int stdyCounter=0;
				for( int ch = 0; ch< channel_number - 1; ch++ ){
					for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int i=0; i< L; i++ ){
								for(int j=0; j< fft[ch][i].specSize(); j++ ){	
											     			      
									Sxy[stdyCounter][i][j] =  ( fft[ch][i].getBand(j)*fft[inCounter][i].getBand(j) )/ ((float)N) ;
									//println("Sxy[stdyCounter][i][j] is "+Sxy[stdyCounter][i][j]);	
								}
							}
							stdyCounter ++;
					}
				}
			 
				//Finding Sxx,Syy 
				for( int ch = 0; ch< channel_number ; ch++ ){
					   for(int i=0; i< L; i++ ){
					     for(int j=0; j< fft[ch][i].specSize(); j++ ){
					    	 Sxx[ch][i][j] =  ( fft[ch][i].getBand(j)*fft[ch][i].getBand(j) )/ ((float)N ) ;	
					    	// println("Sxx[stdyCounter][i][j] is "+Sxx[ch][i][j]);	
					     }
					   }
				}
				
				
				
				//================================= ???
				
				     float[][] AVxx   = new float[channel_number][N];
				     float[][] AVxy   = new float[120][N];
				   //initialize average
				     for(int i=0; i< channel_number; i++ ){
				       for(int j=0; j< N; j++ ){
				         AVxx[i][j] = 0;
				        
				       }
				     }
				     //initialize average
				     for(int i=0; i< 120; i++ ){
					       for(int j=0; j< N; j++ ){
					    	   AVxy[i][j] = 0;					        
					       }
					     }
			     
				       
				       float temp1=0f,temp3=0f;
				      		
				       // last series summarize the spectrums
				       for(int ch=0; ch<channel_number;ch++){
				    	   for(int j=0; j< N; j++ ){
				    		   for(int i=0; i< L; i++ ){	
				    		   temp1 = Sxx[ch][i][j] + temp1;   
				    	   }
				    		   AVxx[ch][j] = temp1/ ((float)L); 	   
				    		   temp1 = 0f;
				       }
				       }
				       
				       
				       for(int ch=0; ch<120;ch++){
				       for(int j=0; j< N; j++ ){
				         for(int i=0; i< L; i++ ){			         
				         temp3 = Sxy[ch][i][j] + temp3;
				       }
				       
	        
				        AVxy[ch][j] = temp3/ ((float)L);
				       // println(" Sxx is "+AVxx[j]);				        
				        temp3 =0f;
				        
				        }    		
				       }
				       
				       
				       
				        float[][] Coherence;
				       Coherence = new float[120][N/2];
				       //println("AVxx[ch][j] is "+AVxx[ch][j]);	
				       //find coherence
				       stdyCounter=0;
					for( int ch = 0; ch< channel_number - 1; ch++ ){
						for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int j=0; j< Coherence.length; j++ ){
								
					    	    Coherence[stdyCounter][j] = ((AVxy[stdyCounter][j])*(AVxy[stdyCounter][j])) / (AVxx[ch][j]*AVxx[inCounter][j] );
					    	
					    		 println("coherence is "+ Coherence[stdyCounter][j]);
					    		 //println("stdCounter is "+stdyCounter);
					    	 
					    	    
					       }
							stdyCounter++;	
					       }
						}
		
				     
				       
			  
			
			//last part GUI
			    cp5 = new ControlP5(this);	        
		        //cp5.setColorBackground(color(0,0,150));
		        cp5.setColorForeground(color(255,255,255));
		        cp5.setColorBackground(color(110,110,110));	
		        //0 to 15
		        for(int i=0;i<15;i++){       
		             myChart[i] = cp5.addChart(El_name[0]+"-"+El_name[i+1])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2 ;y++){
		           myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		         //15 to 29
		        for(int i=15;i<29;i++){       
		             myChart[i] = cp5.addChart(El_name[1]+"-"+El_name[i%15 + 2])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //29 to 42
		        for(int i=29;i<42;i++){       
		             myChart[i] = cp5.addChart(El_name[2]+"-"+El_name[i%29+3])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		        
		          //44 to 54
		        for(int i=42;i<54;i++){       
		             myChart[i] = cp5.addChart(El_name[3]+"-"+El_name[i%42+4])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		          //54 to 65
		        for(int i = 54;i < 65;i++){       
		             myChart[i] = cp5.addChart(El_name[4]+"-"+El_name[i%54+5])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //65 to 75
		        for(int i = 65;i < 75;i++){       
		             myChart[i] = cp5.addChart(El_name[5]+"-"+El_name[i%65+6])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //75 to 84
		        for(int i = 75;i < 84;i++){       
		             myChart[i] = cp5.addChart(El_name[6]+"-"+El_name[i%75+7])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		         //84 to 92
		        for(int i = 84;i < 92;i++){       
		             myChart[i] = cp5.addChart(El_name[7]+"-"+El_name[i%84+8])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		         
		         //92 to 99
		        for(int i = 92;i < 99;i++){       
		             myChart[i] = cp5.addChart(El_name[8]+"-"+El_name[i%92+9])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		         //99 to 105
		        for(int i = 99;i < 105;i++){       
		             myChart[i] = cp5.addChart(El_name[9]+"-"+El_name[i%99+10])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        //105 to 110
		        for(int i = 105;i < 110;i++){       
		             myChart[i] = cp5.addChart(El_name[10]+"-"+El_name[i%105+11])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        //110 to 114
		        for(int i = 110;i < 114;i++){       
		             myChart[i] = cp5.addChart(El_name[11]+"-"+El_name[i%110+12])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		          //114 to 117
		        for(int i = 114;i < 117;i++){       
		             myChart[i] = cp5.addChart(El_name[12]+"-"+El_name[i%114+13])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		           //117 to 119
		        for(int i = 117;i < 119;i++){       
		             myChart[i] = cp5.addChart(El_name[13]+"-"+El_name[i%117+14])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[i].addDataSet("incoming");
		          myChart[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChart[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		     
		      myChart[119] = cp5.addChart(El_name[14]+"-"+El_name[15])
		                       .setPosition(10 + (119/10)*110, 10 + (119%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChart[119].addDataSet("incoming");
		          myChart[119].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		           myChart[119].push("incoming", Coherence[119][y]);
		          }
		     
		 
		}
		
		
		float hammingWindow(float wn, float n, float N){
		     float a = 25f/46f;
		     float b = 21f/46f;
		      
		     wn = a - b*cos( ( 2f*3.14f*n) / N );
		     //println(" wn is "+wn);
		      return wn;
		    }
		
		
		public void draw() {
		  //background(125);
		  // unshift: add data from left to right (first in)
		  //myChart.unshift("incoming", (sin(frameCount*0.1)*20));
		  
		  // push: add data from right to left (last in)
		 
		}
				
				
				
			
			}
	
	
	//--------Functions for AutismWindow----------------------------START-----------------------------------//
	public class drawingWindowAutism extends PApplet{
		
		public void settings()
		{
			size(1200,600,P2D);
	
		}
				
		
		public void exit(){
			stop();
			//frame.dispose();
		}
	
		public void setup()
		{		
			frameRate(10);
			surface.setTitle("Statictical Evaluation Test");
			background(0);
			scrollListAutism = new ControlP5(this);
			scrollListAutism.setColorCaptionLabel(white);
			autismList = scrollListAutism.addScrollableList("Menu");
			autismList.setPosition(0,0);
			autismList.setSize(200, 100);
			autismList.setBarHeight(20);
			autismList.setItemHeight(20);
			autismList.addItems(autismAdds);
			autismList.setType(ScrollableList.LIST);
			autismList.isBarVisible();
			autismList.setColorBackground(backColorScroll);
			autismList.setColorActive(activeColorScroll);
			autismList.setColorForeground(foreColorScroll);
			autismList.close();	
			
			
			
			testButton = new ControlP5(this);
			testButton.setColorForeground(color(255,255,0));
			testButton.setColorBackground( color(0,255,255));
			testButton.setColorActive(colorGreyActive2);
			testButton.setColorCaptionLabel(black);
			
			Button testAutism = testButton.addButton("testAutism");
			//allWave.setGroup("spectroMyGroup");
			testAutism.setLabel("Begin Test");				
			testAutism.setPosition(width/60,height/5f );
			testAutism.setSize(width/10,2*height/35);			
		
			//Fill array of the W/S test
			fillarrayWS();
			fillarraytTest();
			fillarrayUtest1();
			fillarrayUtest2();	
			
			
		}
	
		public void draw(){			
			background(255);
			fill(0);
			text("number of Patient Group files    " + numberFilesADS,0,200);
			fill(0);
			text("number of Control Group files    " + numberFilesControlGroup,0,250);
			fill(0,0,0);
			text(" " + indicator,0,300);
	
			if(displayTest == true){
				stroke(0);
				line(width/5,height/20,width/3f,height/20);				
				testDisplay();
				
			}
			
		}
		
		public void testDisplay(){
			  stroke(0);
			  textSize(12);
		      line(width/4 - width/20,1,width -10,1);
		      line(width/4,height/20,width -10,height/20);
		   
		      fill(0);
		     
		      text("Brain regions",width/4 - width/20,height/20 - height/40);
		      
		      text("Delta(mean -/+ SD)",width/4 + width/12,height/20 - height/40 - 1);
		      line(width/4f + width/12f,height/20f - height/40f ,width/4f + width/5.6f,height/20f  - height/40f );
		      text("Patients",width/800+width/4 + width/12,height/20  -1);
		      text("Controls",width/800+width/4 + width/12 + width/16 - width/128,height/20  -1);
		        
		      text("P-value",width/4 + width/5,height/20 - height/40 - 1);   
		      
		      text("Theta(mean -/+ SD)",width/2,height/20 - height/40 - 1 );
		      line(width/2f,height/20f - height/40f ,width/2f + width/10.3f,height/20  - height/40 );
		      text("Patients",width/400+width/2,height/20  -1);
		      text("Controls",width/400+width/2 + width/16 - width/128,height/20  -1);    
		      
		      text("P-value",width/2 + width/8.5f ,height/20 - height/40 - 1);     
		      
		      
		      text("Alpha(mean -/+ SD)",width/2  + width/8.5f + width/16 - width/100,height/20 - height/40 - 1 );
		      line(width/2f  + width/8.5f + width/16 - width/100,height/20 - height/40 ,width/2  + width/5.2f  - width/100 + width/12,height/20  - height/40 );
		      text("Patients",width/400+width/2  + width/8.5f + width/16 - width/100,height/20  -1);
		      text("Controls",width/2  + width/8.5f + width/16 - width/100+ width/16 - width/128,height/20  -1);    
		    
		      text("P-value",width/2  +width/8+width/10 +width/16,height/20 - height/40 - 1);  
		     
		      text("Beta(mean -/+ SD)",width/2  + width/6 + width/8 + width/16 - width/100,height/20 - height/40 - 1 );       
		      line(width/2  + width/6 + width/8 + width/16 - width/100,height/20 - height/40 ,width/2  + width/6 + width/8 + width/11 - width/100+width/16,height/20  - height/40 );
		      text("Patients",width/400+width/2  + width/6 + width/8 + width/16 - width/100,height/20  -1);
		      text("Controls",width/400+width/2  + width/5 + width/7 + width/16 - width/100,height/20  -1);    
		     
		      text("P-value",width/2  + width/3 + width/8.5f  ,height/20 - height/40 - 1);   
		    
		    
		      text("LT Frontal   (Fp1,F3,F7)",width/4 - width/20,   height/14);
		      text("RT Frontal   (Fp2,F4,F8)",width/4 - width/20,   height/20 +  height/24 + height/64);
		      text("LT Central   (C3,T3)",width/4 - width/20 ,   height/20 +  height/24 + height/24 + height/64);
		      text("RT Central   (C4,T4)",width/4 - width/20,   height/20 +  height/24 + height/24 + height/24+ height/64);      
		      text("LT Posterial (T5,P3,O1)",width/4 - width/20, height/20 +  height/24 + height/24 + height/24+ height/24+ height/64);
		      text("RT Posterial (T6,P4,O2)",width/4 - width/20, height/20 +  height/24 + height/24 + height/24+ height/24+ height/24 + height/64);
		  
		    //2nd Table  
		      text("Coherence",width/4 - width/20,height/8+ height/6 +height/8+ height/6 +height/20 - height/40+ height/32 -height/3.2f );
		      //text("distances",width/4 - width/20,height/8+ height/6 +height/8+ height/6 +height/20 -1+ height/32 -height/3.2f);
		      line(width/4- width/20,height/8+ height/32+ height/6 +height/8+ height/6 -height/3.2f,width-10,height/8+ height/6 +height/8 + height/6+ height/32 -height/3.2f);
		      line(width/4- width/20,height/8+ height/32+ height/6 +height/8+ height/6+height/20 -height/3.2f,width -10,height/8+ height/6 +height/8+ height/6+height/20+ height/32 - height/3.2f);
		      
		      text("Delta(mean -/+ SD)",width/4 + width/12,height/8+ height/6 +height/8+ height/6 +height/20 - height/40 - 1+ height/32  - height/3.2f);
		      line(width/4f + width/12f,height/8+ height/6 +height/8+ height/6 +height/20 - height/40 + height/32  - height/3.2f,width/4f + width/5.6f,height/8+ height/6 +height/8+ height/6 +height/20  - height/40 + height/32  - height/3.2f);
		      text("Patients",width/800+width/4 + width/12,height/8+ height/6 +height/8+ height/6 +height/20  -1+ height/32  - height/3.2f);
		      text("Controls",width/800+width/4 + width/12 + width/16 - width/128,height/8+ height/6 +height/8+ height/6 +height/20  -1+ height/32  - height/3.2f);
		      
		      text("P-value",width/4 + width/5 ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20 - height/40 - 1  - height/3.2f);   
		      
		      text("Theta(mean -/+ SD)",width/2 ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20 - height/40 - 1  - height/3.2f);
		      line(width/2 ,height/8+ height/6 +height/8+ height/32+ height/6 +height/20 - height/40  - height/3.2f,width/2 + width/10.3f,height/8+ height/6 +height/8+ height/32+ height/6 +height/20  - height/40  - height/3.2f);
		      text("Patients",width/400+width/2 ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1  - height/3.2f);
		      text("Controls",width/400+width/2 + width/16 - width/128 ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1  - height/3.2f);    
		      
		      text("P-value",width/2 + width/8.5f  ,height/8+ height/6 +height/8+ height/32+ height/6 +height/20 - height/40 - 1  - height/3.2f);     
		      
		      
		      text("Alpha(mean -/+ SD)",width/2  + width/8.5f + width/16 - width/100,height/8+ height/32+ height/6 +height/8+ height/6 +height/20 - height/40 - 1  - height/3.2f);
		      line(width/2  + width/8.5f + width/16 - width/100 ,height/8+ height/6+ height/32 +height/8+ height/6 +height/20 - height/40  - height/3.2f,width/2  + width/5.2f  - width/100 + width/12,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  - height/40  - height/3.2f);
		      text("Patients",width/400f+width/2  + width/8.5f + width/16f - width/100f ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1 - height/3.2f);
		      text("Controls",width/2f  + width/8.5f + width/16 - width/100f+ width/16f - width/128 ,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1 - height/3.2f);    
		    
		      text("P-value",width/2  +width/8+width/10 +width/16 ,height/8+ height/6+ height/32 +height/8+ height/6 +height/20 - height/40 - 1 - height/3.2f);  
		     
		      text("Beta(mean -/+ SD)",width/2  + width/6 + width/8 + width/16 - width/100,height/8+ height/32+ height/6 +height/8+ height/6 +height/20 - height/40 - 1  - height/3.2f);       
		      line(width/2  + width/6 + width/8 + width/16 - width/100,height/8+ height/32+ height/6 +height/8+ height/6 +height/20 - height/40  - height/3.2f,width/2  + width/6 + width/8 + width/11 - width/100+width/16,height/8+ height/6 +height/8+ height/6+ height/32 +height/20  - height/40 - height/3.2f );
		      text("Patients",width/400+width/2  + width/6 + width/8 + width/16 - width/100,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1 - height/3.2f);
		      text("Controls",width/400+width/2  + width/5 + width/7 + width/16 - width/100,height/8+ height/32+ height/6 +height/8+ height/6 +height/20  -1 - height/3.2f);    
		     
		      text("P-value",width/2  + width/3 + width/8.5f  ,height/8+ height/6+height/8+ height/32+ height/6 +height/20 - height/40 - 1 - height/3.2f);   
		     
		      
		      
		      
		     
		      text("(FP1-FP2)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);
		      text("(F3 - F4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);
		      text("(F3 - F8)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);		      
		      text("(T3 - T4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);		 
		      text("(T5 - T6)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6);	
		      text("(C3 - C4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/24);		     
		      text("(P3 - P4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);		
		      text("(O1 - O2)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);			      
		      text("(FP1- F3)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3);	
		      text("(FP2- F4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);	
		      text("(T3 - T5)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		      text("(T4 - T6)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		      text("(C3 - P3)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		      text("(C4 - P4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);	
		
		        //values Table1
		      textSize(10);
		      DecimalFormat df = new DecimalFormat("#.##");
		      df.setRoundingMode(RoundingMode.CEILING);
		      
		      double doul1 = (double)meanTable1ASD[0][0];	         
		      double doul2 = (double)SDTable1ASD[0][0];		      
		      String b = "±";		
		      String a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/14f);			      
		      doul1 = (double)meanTable1CG[0][0];	         
		      doul2 = (double)SDTable1CG[0][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/14f);	
		      text(PvalueU[0][0],width/3 + width/8.5f ,height/14);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[0][1];	         
		      doul2 = (double)SDTable1ASD[0][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256 ,height/14);  
		      doul1 = (double)meanTable1CG[0][1];	         
		      doul2 = (double)SDTable1CG[0][1];		  
		      b = "|";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/14);  
		      text(PvalueU[0][1],width/2  + width/8.25f,height/14);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[0][2];	         
		      doul2 = (double)SDTable1ASD[0][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/14);  
		      doul1 = (double)meanTable1CG[0][2];	         
		      doul2 = (double)SDTable1CG[0][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/14);  
		      text(PvalueU[0][2],width/2  + width/8.25f+ width/5.9f,height/14);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[0][3];	         
		      doul2 = (double)SDTable1ASD[0][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/14);  
		      doul1 = (double)meanTable1CG[0][3];	         
		      doul2 = (double)SDTable1CG[0][3];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/14);  
		      text(PvalueU[0][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/14);  
		      
		     //--------------line2-----------------
		      doul1 = (double)meanTable1ASD[1][0];	         
		      doul2 = (double)SDTable1ASD[1][0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/9.5f);			      
		      doul1 = (double)meanTable1CG[1][0];	         
		      doul2 = (double)SDTable1CG[1][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/9.5f);	
		      text(PvalueU[1][0],width/3 + width/8.5f ,height/9.5f);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[1][1];	         
		      doul2 = (double)SDTable1ASD[1][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256,height/9.5f);  
		      doul1 = (double)meanTable1CG[1][1];	         
		      doul2 = (double)SDTable1CG[1][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/9.5f);  
		      text(PvalueU[1][1],width/2  + width/8.25f,height/9.5f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[1][2];	         
		      doul2 = (double)SDTable1ASD[1][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/9.5f);  
		      doul1 = (double)meanTable1CG[1][2];	         
		      doul2 = (double)SDTable1CG[1][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/9.5f);  
		      text(PvalueU[1][2],width/2  + width/8.25f+ width/5.9f,height/9.5f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[1][3];	         
		      doul2 = (double)SDTable1ASD[1][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/9.5f);  
		      doul1 = (double)meanTable1CG[1][3];	         
		      doul2 = (double)SDTable1CG[1][3];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/9.5f);  
		      text(PvalueU[1][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/9.5f);  
		 
		      
		      //line 3
		      textSize(10);
		      doul1 = (double)meanTable1ASD[2][0];	         
		      doul2 = (double)SDTable1ASD[2][0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/6.825f);			      
		      doul1 = (double)meanTable1CG[2][0];	         
		      doul2 = (double)SDTable1CG[2][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/6.825f);	
		      text(PvalueU[2][0],width/3 + width/8.5f ,height/6.825f);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[2][1];	         
		      doul2 = (double)SDTable1ASD[2][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/248,height/6.825f);  
		      doul1 = (double)meanTable1CG[2][1];	         
		      doul2 = (double)SDTable1CG[2][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/6.825f);  
		      text(PvalueU[2][1],width/2  + width/8.25f,height/6.825f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[2][2];	         
		      doul2 = (double)SDTable1ASD[2][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/6.825f);  
		      doul1 = (double)meanTable1CG[2][2];	         
		      doul2 = (double)SDTable1CG[2][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/6.825f);  
		      text(PvalueU[2][2],width/2  + width/8.25f+ width/5.9f,height/6.825f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[2][3];	         
		      doul2 = (double)SDTable1ASD[2][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/6.825f);  
		      doul1 = (double)meanTable1CG[2][3];	         
		      doul2 = (double)SDTable1CG[2][3];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/6.825f);  
		      text(PvalueU[2][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/6.825f);  
		      
		      
		      
		      
		      
		      
		      //line4
		      textSize(10);
		      doul1 = (double)meanTable1ASD[3][0];	         
		      doul2 = (double)SDTable1ASD[3][0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/5.3f);			      
		      doul1 = (double)meanTable1CG[3][0];	         
		      doul2 = (double)SDTable1CG[3][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/5.3f);	
		      text(PvalueU[3][0],width/3 + width/8.5f ,height/5.3f);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[3][1];	         
		      doul2 = (double)SDTable1ASD[3][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 +width/400,height/5.3f);  
		      doul1 = (double)meanTable1CG[3][1];	         
		      doul2 = (double)SDTable1CG[3][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/5.3f);  
		      text(PvalueU[3][1],width/2  + width/8.25f,height/5.3f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[3][2];	         
		      doul2 = (double)SDTable1ASD[3][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/5.3f);  
		      doul1 = (double)meanTable1CG[3][2];	         
		      doul2 = (double)SDTable1CG[3][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/5.3f);  
		      text(PvalueU[3][2],width/2  + width/8.25f+ width/5.9f,height/5.3f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[3][3];	         
		      doul2 = (double)SDTable1ASD[3][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/5.3f);  
		      doul1 = (double)meanTable1CG[3][3];	         
		      doul2 = (double)SDTable1CG[3][3];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/5.3f);  
		      text(PvalueU[3][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/5.3f);  
		      
		      
		      
		      
		      
		      
		      
		      //line 5
		      textSize(10);
		      doul1 = (double)meanTable1ASD[4][0];	         
		      doul2 = (double)SDTable1ASD[4][0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4.32f);			      
		      doul1 = (double)meanTable1CG[4][0];	         
		      doul2 = (double)SDTable1CG[4][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4.32f);	
		      text(PvalueU[4][1],width/3 + width/8.5f ,height/4.32f);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[4][1];	         
		      doul2 = (double)SDTable1ASD[4][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4.32f); 	
		      doul1 = (double)meanTable1CG[4][1];	         
		      doul2 = (double)SDTable1CG[4][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4.32f);  
		      text(PvalueU[4][1],width/2  + width/8.25f,height/4.32f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[4][2];	         
		      doul2 = (double)SDTable1ASD[4][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4.32f);  
		      doul1 = (double)meanTable1CG[4][2];	         
		      doul2 = (double)SDTable1CG[4][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4.32f);  
		      text(PvalueU[4][2],width/2  + width/8.25f+ width/5.9f,height/4.32f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[4][3];	         
		      doul2 = (double)SDTable1ASD[4][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4.32f);  
		      doul1 = (double)meanTable1CG[4][3];	         
		      doul2 = (double)SDTable1CG[4][3];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4.32f);  
		      text(PvalueU[4][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4.32f);  
		      
		      
		      
		      //line 6
		      textSize(10);
		      doul1 = (double)meanTable1ASD[5][0];	         
		      doul2 = (double)SDTable1ASD[5][0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/3.65f);			      
		      doul1 = (double)meanTable1CG[5][0];	         
		      doul2 = (double)SDTable1CG[5][0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/3.65f);	
		      text(PvalueU[5][0],width/3 + width/8.5f ,height/3.65f);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[5][1];	         
		      doul2 = (double)SDTable1ASD[5][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/3.65f); 	
		      doul1 = (double)meanTable1CG[5][1];	         
		      doul2 = (double)SDTable1CG[5][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/3.65f);  
		      text(PvalueU[5][1],width/2  + width/8.25f,height/3.65f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[5][2];	         
		      doul2 = (double)SDTable1ASD[5][2];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/3.65f);  
		      doul1 = (double)meanTable1CG[5][2];	         
		      doul2 = (double)SDTable1CG[5][2];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/3.65f);  
		      text(PvalueU[5][2],width/2  + width/8.25f+ width/5.9f,height/3.65f);  
		      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[5][3];	         
		      doul2 = (double)SDTable1ASD[5][3];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/3.65f);  
		      doul1 = (double)meanTable1CG[5][3];	         
		      doul2 = (double)SDTable1CG[5][3];		  
		      b = "|";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/3.65f);  
		      text(PvalueU[5][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/3.65f);  
		      
		      
//--------------------------coherence table----------------------------------------------------
		//--------------------------coherence table----------------------------------------------------
		      
		      
		     
		   
		     
		     
		   
		      
		   
		
		      
		      //line 1 -----------------------FP1 - FP2------------------
		      doul1 = (double)meanDeltaASD_Coherence[0];	         
		      doul2 = (double)sdDeltaASD_Coherence[0];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      		     
		      text(a, width/4f + width/12f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);			      
		      doul1 = (double)meanDeltaCG_Coherence[0];	         
		      doul2 = (double)sdDeltaCG_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);	
		      text(PvalueCoherenceDelta[0],width/3 + width/8.5f , height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[0];	         
		      doul2 = (double)sdThetaASD_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256 , height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)meanThetaCG_Coherence[0];	         
		      doul2 = (double)sdThetaCG_Coherence[0];		 	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceTheta[1],width/2  + width/8.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[0];	         
		      doul2 = (double)sdAlphaASD_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)meanAlphaCG_Coherence[0];	         
		      doul2 = (double)sdAlphaCG_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceAlpha[0],width/2  + width/8.25f+ width/5.9f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[0];	         
		      doul2 = (double)sdBetaASD_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)meanBetaCG_Coherence[0];	         
		      doul2 = (double)sdBetaCG_Coherence[0];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceBeta[0],width/2  + width/8.25f+ width/5.9f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      
		  //line 2 -----------------------F3 - F4-->106----------------
		      doul1 = (double)meanDeltaASD_Coherence[106];	         
		      doul2 = (double)sdDeltaASD_Coherence[106];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);			      
		      doul1 = (double)meanDeltaCG_Coherence[106];	         
		      doul2 = (double)sdDeltaCG_Coherence[106];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);	
		      text(PvalueCoherenceDelta[106],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
	    
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[106];	         
		      doul2 = (double)sdThetaASD_Coherence[106];			  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)meanThetaCG_Coherence[106];	         
		      doul2 = (double)sdThetaCG_Coherence[106];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text(PvalueCoherenceTheta[106],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[106];	         
		      doul2 = (double)sdAlphaASD_Coherence[106];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)meanAlphaCG_Coherence[106];	         
		      doul2 = (double)sdAlphaCG_Coherence[106];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text(PvalueCoherenceAlpha[106],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[106];	         
		      doul2 = (double)sdBetaASD_Coherence[106];				  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)meanBetaCG_Coherence[106];	         
		      doul2 = (double)sdBetaCG_Coherence[106];	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text(PvalueCoherenceBeta[106],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		 
		      
	//line 3 -----------------------F3 - F8->100-----------------
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[100];	         
		      doul2 = (double)sdDeltaASD_Coherence[100];				     
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);			      
		      doul1 = (double)meanBetaCG_Coherence[100];	         
		      doul2 = (double)sdBetaCG_Coherence[100];	 
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);	
		      text(PvalueCoherenceDelta[100],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[100];	         
		      doul2 = (double)sdThetaASD_Coherence[100];		
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/248,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)meanThetaCG_Coherence[100];	         
		      doul2 = (double)sdThetaCG_Coherence[100];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text(PvalueCoherenceTheta[100],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[100];	         
		      doul2 = (double)sdAlphaASD_Coherence[100];			  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)meanAlphaCG_Coherence[100];	         
		      doul2 = (double)sdAlphaCG_Coherence[100];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text(PvalueCoherenceAlpha[100],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[100];	         
		      doul2 = (double)sdBetaASD_Coherence[100];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)meanBetaCG_Coherence[100];	         
		      doul2 = (double)sdBetaCG_Coherence[100];	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text(PvalueCoherenceBeta[100],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
	      
		      
		      
		//line 4 -----------------------T3 - T4-->115----------------
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[115];	         
		      doul2 = (double)sdDeltaASD_Coherence[115];	      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);			      
		      doul1 = (double)meanDeltaCG_Coherence[115];	         
		      doul2 = (double)sdDeltaCG_Coherence[115];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);	
		      text(PvalueCoherenceDelta[115],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[115];	         
		      doul2 = (double)sdThetaASD_Coherence[115];	   
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 +width/400,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)meanThetaCG_Coherence[115];	         
		      doul2 = (double)sdThetaCG_Coherence[115];	    
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text(PvalueCoherenceTheta[115],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[115];	         
		      doul2 = (double)sdAlphaASD_Coherence[115];	   	  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)meanAlphaCG_Coherence[115];	         
		      doul2 = (double)sdAlphaCG_Coherence[115];	 
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text(PvalueCoherenceAlpha[115],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[115];	         
		      doul2 = (double)sdBetaASD_Coherence[115];	   		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)meanBetaCG_Coherence[115];	         
		      doul2 = (double)sdBetaCG_Coherence[115];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text(PvalueCoherenceBeta[115],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      //==============
		      

		      
		      
	     
	 //line 5 -----------------------T5 - T6----->50-------------
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[50];	         
		      doul2 = (double)sdDeltaASD_Coherence[50];	 	      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);			      
		      doul1 = (double)meanDeltaCG_Coherence[50];	         
		      doul2 = (double)sdDeltaCG_Coherence[50];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);	
		      text(PvalueCoherenceDelta[50],width/3 + width/8.5f ,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
	      
		      textSize(10);
		      
		      doul1 = (double)meanThetaASD_Coherence[50];	         
		      doul2 = (double)sdThetaASD_Coherence[50];			     
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f); 	
		      doul1 = (double)meanThetaCG_Coherence[50];	         
		      doul2 = (double)sdThetaCG_Coherence[50];	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128, height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceBeta[50],width/2  + width/8.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[50];	         
		      doul2 = (double)sdAlphaASD_Coherence[50];	  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)meanAlphaCG_Coherence[50];	         
		      doul2 = (double)sdAlphaCG_Coherence[50];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceAlpha[50],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[50];	         
		      doul2 = (double)sdBetaASD_Coherence[50];	 	  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)meanBetaCG_Coherence[50];	         
		      doul2 = (double)sdBetaCG_Coherence[50];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueCoherenceBeta[50],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      
		      
		      
	//------line 6----------------C3-C4----->30------------------
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[30];	         
		      doul2 = (double)sdDeltaASD_Coherence[30];	 	 		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);			      
		      doul1 = (double)meanDeltaCG_Coherence[30];	         
		      doul2 = (double)sdDeltaCG_Coherence[30];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);	
		      text(PvalueCoherenceDelta[30],width/3 + width/8.5f ,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[30];	         
		      doul2 = (double)sdThetaASD_Coherence[30];	 
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24); 	
		      doul1 = (double)meanThetaCG_Coherence[30];	         
		      doul2 = (double)sdThetaCG_Coherence[30];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      text(PvalueCoherenceTheta[30],width/2  + width/8.25f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[30];	         
		      doul2 = (double)sdAlphaASD_Coherence[30];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      doul1 = (double)meanAlphaCG_Coherence[30];	         
		      doul2 = (double)sdAlphaCG_Coherence[30];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      text(PvalueCoherenceAlpha[30],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[30];	         
		      doul2 = (double)sdBetaASD_Coherence[30];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      doul1 = (double)meanBetaCG_Coherence[30];	         
		      doul2 = (double)sdBetaCG_Coherence[30];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		      text(PvalueCoherenceBeta[30],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/6 + height/32+ height/3 + height/20 +  height/60 - height/3.2f +   height/24);  
		     
		      
		//------------line 7 --------P3 - P4--->120-----------------------  
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[119];	         
		      doul2 = (double)sdDeltaASD_Coherence[119];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);			      
		      doul1 = (double)meanDeltaCG_Coherence[119];	         
		      doul2 = (double)sdDeltaCG_Coherence[119];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);	
		      text(PvalueCoherenceDelta[119],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[119];	         
		      doul2 = (double)sdThetaASD_Coherence[119];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12); 	
		      doul1 = (double)meanThetaCG_Coherence[119];	         
		      doul2 = (double)sdThetaCG_Coherence[119];	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      text(PvalueCoherenceTheta[119],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[119];	         
		      doul2 = (double)sdAlphaASD_Coherence[119];			  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      doul1 = (double)meanAlphaCG_Coherence[119];	         
		      doul2 = (double)sdAlphaCG_Coherence[119];	  	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      text(PvalueCoherenceAlpha[119],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[119];	         
		      doul2 = (double)sdBetaASD_Coherence[119];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      doul1 = (double)meanBetaCG_Coherence[119];	         
		      doul2 = (double)sdBetaCG_Coherence[119];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      text(PvalueCoherenceBeta[119],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);  
		      
	
		      
	 //--------line 8------------------------O1-O2--->76--------------
   
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[76];	         
		      doul2 = (double)sdDeltaASD_Coherence[76];		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);			      
		      doul1 = (double)meanDeltaCG_Coherence[76];	         
		      doul2 = (double)sdDeltaCG_Coherence[76];	    
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);	
		      text(PvalueCoherenceDelta[76],width/3 + width/8.5f ,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[76];	         
		      doul2 = (double)sdThetaASD_Coherence[76];		     
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24); 	
		      doul1 = (double)meanThetaCG_Coherence[76];	         
		      doul2 = (double)sdThetaCG_Coherence[76];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      text(PvalueCoherenceTheta[76],width/2  + width/8.25f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[76];	         
		      doul2 = (double)sdAlphaASD_Coherence[76];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      doul1 = (double)meanAlphaCG_Coherence[76];	         
		      doul2 = (double)sdAlphaCG_Coherence[76];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      text(PvalueCoherenceAlpha[76],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[76];	         
		      doul2 = (double)sdBetaASD_Coherence[76];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      doul1 = (double)meanBetaCG_Coherence[76];	         
		      doul2 = (double)sdBetaCG_Coherence[76];	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		      text(PvalueCoherenceBeta[76],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+height/3+height/32+height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);  
		       
		      
		      
		     	
		      
		      
		      //--------line 9------------------------FP1-FP3---->10-------------------------
		      
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[10];	         
		      doul2 = (double)sdDeltaASD_Coherence[10];		
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);			      
		      doul1 = (double)meanDeltaCG_Coherence[10];	         
		      doul2 = (double)sdDeltaCG_Coherence[10];	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);	
		      text(PvalueCoherenceDelta[10],width/3 + width/8.5f ,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[10];	         
		      doul2 = (double)sdThetaASD_Coherence[10];		
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3); 	
		      doul1 = (double)meanThetaCG_Coherence[10];	         
		      doul2 = (double)sdThetaCG_Coherence[10];	 
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      text(PvalueCoherenceTheta[10],width/2  + width/8.25f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[10];	         
		      doul2 = (double)sdAlphaASD_Coherence[10];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      doul1 = (double)meanAlphaCG_Coherence[10];	         
		      doul2 = (double)sdAlphaCG_Coherence[10];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      text(PvalueCoherenceAlpha[10],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[10];	         
		      doul2 = (double)sdBetaASD_Coherence[10];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      doul1 = (double)meanBetaCG_Coherence[10];	         
		      doul2 = (double)sdBetaCG_Coherence[10];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		      text(PvalueCoherenceBeta[10],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3+ height/32+height/20 + height/60 - height/3.2f + height/3);  
		       
		      
    //--------line 10------------------------FP2-F4--->25--------------------------
		      
		      textSize(10);
		      doul1 = (double)meanDeltaASD_Coherence[25];	         
		      doul2 = (double)sdDeltaASD_Coherence[25];		  	      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);		      
		      doul1 = (double)meanDeltaCG_Coherence[25];	         
		      doul2 = (double)sdDeltaCG_Coherence[25];	    
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      text(PvalueCoherenceDelta[25],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24); 
	      
		      textSize(10);
		      doul1 = (double)meanThetaASD_Coherence[25];	         
		      doul2 = (double)sdThetaASD_Coherence[25];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      doul1 = (double)meanThetaCG_Coherence[25];	         
		      doul2 = (double)sdThetaCG_Coherence[25];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24); 
		      text(PvalueCoherenceTheta[25],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      
		      textSize(10);
		      doul1 = (double)meanAlphaASD_Coherence[25];	         
		      doul2 = (double)sdAlphaASD_Coherence[25];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      doul1 = (double)meanAlphaCG_Coherence[25];	         
		      doul2 = (double)sdAlphaCG_Coherence[25];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);  
		      text(PvalueCoherenceAlpha[25],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      
		      textSize(10);
		      doul1 = (double)meanBetaASD_Coherence[25];	         
		      doul2 = (double)sdBetaASD_Coherence[25];		   
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		      doul1 = (double)meanBetaCG_Coherence[25];	         
		      doul2 = (double)sdBetaCG_Coherence[25];	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24); 
		      text(PvalueCoherenceBeta[25],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);
		       
		      
		      
		      //--------line 11------------------------T3 - T5--->60--------------------------
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanDeltaASD_Coherence[60];	         
				      doul2 = (double)sdDeltaASD_Coherence[60];		  
		  		      b = "±";		
		  		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		  		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      doul1 = (double)meanDeltaCG_Coherence[60];	         
				      doul2 = (double)sdDeltaCG_Coherence[60];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);	
		  		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      text(PvalueCoherenceDelta[60],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  	      
		  		      textSize(10);
		  		      doul1 = (double)meanThetaASD_Coherence[60];	         
				      doul2 = (double)sdThetaASD_Coherence[60];		  
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      doul1 = (double)meanThetaCG_Coherence[60];	         
				      doul2 = (double)sdThetaCG_Coherence[60];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      text(PvalueCoherenceTheta[60],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanAlphaASD_Coherence[60];	         
				      doul2 = (double)sdAlphaASD_Coherence[60];		  	  
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      doul1 = (double)meanAlphaCG_Coherence[60];	         
				      doul2 = (double)sdAlphaCG_Coherence[60];	     
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	 
		  		      text(PvalueCoherenceAlpha[60],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	  
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanBetaASD_Coherence[60];	         
				      doul2 = (double)sdBetaASD_Coherence[60];		 
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	 
		  		      doul1 = (double)meanBetaCG_Coherence[60];	         
				      doul2 = (double)sdBetaCG_Coherence[60];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		  		      text(PvalueCoherenceBeta[60],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	 
		  		       
		      
   //--------line 12------------------------T4 - T6-->--73-------------------------
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanDeltaASD_Coherence[73];	         
				      doul2 = (double)sdDeltaASD_Coherence[73];		 
		  		      b = "±";		
		  		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		  		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);		
		  		      doul1 = (double)meanDeltaCG_Coherence[73];	         
				      doul2 = (double)sdDeltaCG_Coherence[73];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);	
		  		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      text(PvalueCoherenceDelta[73],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  	      
		  		      textSize(10);
		  		      doul1 = (double)meanThetaASD_Coherence[73];	         
				      doul2 = (double)sdThetaASD_Coherence[73];		  
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      doul1 = (double)meanThetaCG_Coherence[73];	         
				      doul2 = (double)sdThetaCG_Coherence[73];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      text(PvalueCoherenceTheta[73],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanAlphaASD_Coherence[73];	         
				      doul2 = (double)sdAlphaASD_Coherence[73];		  	  
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      doul1 = (double)meanAlphaCG_Coherence[73];	         
				      doul2 = (double)sdAlphaCG_Coherence[73];	     
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      text(PvalueCoherenceAlpha[73],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	  
		  		      
		  		      textSize(10);
		  		      doul1 = (double)meanBetaASD_Coherence[73];	         
				      doul2 = (double)sdBetaASD_Coherence[73];		 
		  		      b = "±";
		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      doul1 = (double)meanBetaCG_Coherence[73];	         
				      doul2 = (double)sdBetaCG_Coherence[73];	   
		  		      b = "±";
		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		      text(PvalueCoherenceBeta[73],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		  		         
		  		      
		  		      
		  		      
				      
		  		    //--------line 13------------------------C3 - P3--->41--------------------------
		  		 		  		      
		  		 		  		      textSize(10);
		  		 		  		      doul1 = (double)meanDeltaASD_Coherence[41];	         
		  		 				      doul2 = (double)sdDeltaASD_Coherence[41];		  
		  		 		  		      b = "±";		
		  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		  		 		  		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      doul1 = (double)meanDeltaCG_Coherence[41];	         
		  		 				      doul2 = (double)sdDeltaCG_Coherence[41];	   
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1) +b+df.format(doul2);	
		  		 		  		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      text(PvalueCoherenceDelta[41],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  	      
		  		 		  		      textSize(10);
		  		 		  		      doul1 = (double)meanThetaASD_Coherence[41];	         
		  		 				      doul2 = (double)sdThetaASD_Coherence[41];		  
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		 		  		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      doul1 = (double)meanThetaCG_Coherence[41];	         
		  		 				      doul2 = (double)sdThetaCG_Coherence[41];	   
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		 		  		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      text(PvalueCoherenceTheta[41],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      
		  		 		  		      textSize(10);
		  		 		  		      doul1 = (double)meanAlphaASD_Coherence[41];	         
		  		 				      doul2 = (double)sdAlphaASD_Coherence[41];		  	  
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		 		  		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      doul1 = (double)meanAlphaCG_Coherence[41];	         
		  		 				      doul2 = (double)sdAlphaCG_Coherence[41];	     
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		 		  		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      text(PvalueCoherenceAlpha[41],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      
		  		 		  		      textSize(10);
		  		 		  		      doul1 = (double)meanBetaASD_Coherence[41];	         
		  		 				      doul2 = (double)sdBetaASD_Coherence[41];		 
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  		 		  		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      doul1 = (double)meanBetaCG_Coherence[41];	         
		  		 				      doul2 = (double)sdBetaCG_Coherence[41];	   
		  		 		  		      b = "±";
		  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  		 		  		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		  		 		  		      text(PvalueCoherenceBeta[41],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	 
		  		 		  		         
		  		 		  	      
		  						      
		  				  		    //--------line 14------------------------C4 - P4-->54---------------------------
		  				  		 		  		      
		  				  		 		  		      textSize(10);
		  				  		 		  		      doul1 = (double)meanDeltaASD_Coherence[54];	         
		  				  		 				      doul2 = (double)sdDeltaASD_Coherence[54];		  
		  				  		 		  		      b = "±";		
		  				  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		  				  		 		  		      text(a, width/4f + width/12f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      doul1 = (double)meanDeltaCG_Coherence[54];	         
		  				  		 				      doul2 = (double)sdDeltaCG_Coherence[54];	   
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1) +b+df.format(doul2);	
		  				  		 		  		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      text(PvalueCoherenceDelta[54],width/3 + width/8.5f ,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  	      
		  				  		 		  		      textSize(10);
		  				  		 		  		      doul1 = (double)meanThetaASD_Coherence[54];	         
		  				  		 				      doul2 = (double)sdThetaASD_Coherence[54];		  
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2+width/400,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      doul1 = (double)meanThetaCG_Coherence[54];	         
		  				  		 				      doul2 = (double)sdThetaCG_Coherence[54];	   
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2 + width/20f + width/128,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      text(PvalueCoherenceTheta[54],width/2  + width/8.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      
		  				  		 		  		      textSize(10);
		  				  		 		  		      doul1 = (double)meanAlphaASD_Coherence[54];	         
		  				  		 				      doul2 = (double)sdAlphaASD_Coherence[54];		  	  
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      doul1 = (double)meanAlphaCG_Coherence[54];	         
		  				  		 				      doul2 = (double)sdAlphaCG_Coherence[54];	     
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      text(PvalueCoherenceAlpha[54],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      
		  				  		 		  		      textSize(10);
		  				  		 		  		      doul1 = (double)meanBetaASD_Coherence[54];	         
		  				  		 				      doul2 = (double)sdBetaASD_Coherence[54];		 
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1)+b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);
		  				  		 		  		      doul1 = (double)meanBetaCG_Coherence[54];	         
		  				  		 				      doul2 = (double)sdBetaCG_Coherence[54];	   
		  				  		 		  		      b = "±";
		  				  		 		  		      a = df.format(doul1) +b+df.format(doul2);		      	
		  				  		 		  		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);	
		  				  		 		  		      text(PvalueCoherenceBeta[54],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);	 
		  				  		 		  		         
		  				  		 		  		      
		  		 		  		      
		  		 		  		        
		  		      
		  		      
		  		      
		  		      
		  		      
		  		    /*    
				      
		   
	
		   12   height/4+ height/128+ height/3 + height/32 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		   13   height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		   14   height/4+ height/128+ height/3 + height/32+ height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);	
		
				    */  
		  		      
		  		      
		  		      
		  		      
		  		      
		      /*
		      textSize(11);
		      for( int h=0; h<6; h++){
			    	  for( int l=0; l<4; l++){
			    		  fill(0,0,255);
			    		  text(disTest[h][l],h,l); 
			    		  if(disTest[h][l]=="tTest"){
			    			  text( "t",(h+1)*50 -10,(l+1)*50+height/1.5f);
			    			  text( aValuetTest[h][l] ,(h+1)*50,(l+1)*50+height/1.5f);
			    			  if(tTestSignificant[h][l]==true){
			    				  text("*",(h+1)*40+ +35,(l+1)*40+height/1.55f +5); 
			    			  }
			    		  }
			    		  else{
			    			  text( "u",(h+1)*50-10,(l+1)*50+height/1.5f);
			    			  text( aValueUtest[h][l] ,(h+1)*50,(l+1)*50+height/1.5f);
			    			  if(uTestSignificant[h][l]==true){
			    				  text("*",(h+1)*50 +35,(l+1)*50+height/1.55f +5); 
			    			  }
			    		  }
			    		
			      }
		      }
		    */
		      
	
			
		}
		
		public void testAutism(){
			
				
			if(  (numberFilesADS != 0)  && 	(numberFilesControlGroup != 0)  ){
				if( numberFilesADS == numberFilesControlGroup){
					indicator = "Testing...";
					 controlGroup = new float[numberFilesControlGroup][channel_number][4];
					 patient = new float[numberFilesADS][channel_number][4];
					 controlGroupCoherence = new float[numberFilesControlGroup][120][128];
					 patientCoherence = new float[numberFilesADS][120][128];
					 runTheTest();
					 
					 indicator = "Results Displayed";
				}
				else{
					indicator = "The ADS and ControlGroup files must be of the same amount";
					
				}
			}
			else if ( ( numberFilesADS == 0 )  && ( numberFilesControlGroup == 0 ) ){
				
				indicator = "Please add files to begin the test";
			}
			else if ( numberFilesADS == 0 ){
				
				indicator = "Please add ADS files to begin the test";
			}
			else{
				
				indicator = "Please add ControlGroup files to begin the test";
			}
			
			
		}
	
		public void Menu(int n)
		{
			
			println(n, scrollListAutism.get(ScrollableList.class, "Menu").getItem(n));
			System.out.println(n);
			
			
			if(n == 0)
			{
				autismList.close();
				add_ADS_patient();			
			}
			else if(n==1){
				autismList.close();
				add_Control_Group();	
			}
			else if(n == 2)
			{
				
				printScreenFunction();
				autismList.close();	
			}
			else if(n == 3)
			{
				autismList.close();			
				stop();
				//frame.setVisible(false);
				
			}		

		}
	
		public void add_ADS_patient(){
			println("add_ADS_patient");
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			FileFilter filter = new FileNameExtensionFilter("Only *.txt files...","txt");
			chooser.setFileFilter(filter);						
			int return_val = chooser.showOpenDialog(frame);
			
			 if ( return_val == JFileChooser.CANCEL_OPTION )
		        {
		        	System.out.println("canceled");
		        }
		        if ( return_val == JFileChooser.ERROR_OPTION )
		        {
		        	System.out.println("error");
		        }
		        if ( return_val == JFileChooser.APPROVE_OPTION )
		        {  
		        	System.out.println("approved");
		        }
		        
		        
		        
		        
		        if ( return_val == JFileChooser.APPROVE_OPTION )
		        { 
		          filesADS = chooser.getSelectedFiles();
		        
		        
		        
		       for(int i= 0; i < filesADS.length; i++ ){
		    	   String files_name = filesADS[i].getAbsolutePath();
		    	   System.out.println(files_name);
		    	   
		       }
		          
		       numberFilesADS = filesADS.length;
		        
		        
		        
		        }
		}
		
		public void add_Control_Group(){
			println("add_Control_Group()");
			
			
			
			JFileChooser chooser2 = new JFileChooser();
			chooser2.setMultiSelectionEnabled(true);
			FileFilter filter = new FileNameExtensionFilter("Only *.txt files...","txt");
			chooser2.setFileFilter(filter);						
			int return_val2 = chooser2.showOpenDialog(frame);
			
			 if ( return_val2 == JFileChooser.CANCEL_OPTION )
		        {
		        	System.out.println("canceled");
		        }
		        if ( return_val2 == JFileChooser.ERROR_OPTION )
		        {
		        	System.out.println("error");
		        }
		        if ( return_val2 == JFileChooser.APPROVE_OPTION )
		        {  
		        	System.out.println("approved");
		        }
		        
		        
		        
		        
		        if ( return_val2 == JFileChooser.APPROVE_OPTION )
		        { 
		          filesControlGroup = chooser2.getSelectedFiles();
		        
		        
		        
		       for(int i= 0; i < filesControlGroup.length; i++ ){
		    	   String files_name = filesControlGroup[i].getAbsolutePath();
		    	   System.out.println(files_name);
		       }
		        }
		        numberFilesControlGroup = filesControlGroup.length;
		}
	
		public void printScreenFunction(){
			save("line.tif");
			redraw(); //redraw solves thread/death problem
			
		}

		public void runTheTest(){		
			println("runTheTest");
			
			
			
			//check patients
			for(int t = 0; t < numberFilesADS; t++){
				readFilesTest(t,filesADS,"patient");		
			}
			
			//check ControlGroup
			for(int t = 0; t < numberFilesControlGroup; t++){
				readFilesTest(t,filesControlGroup,"controlGroup");		
			}
			
			/**/
			
			
			//mean absolute power-----------------------------------------------------------
			//now compute the regions
			computeRegions();
			
			/*
			thesis1();
			thesis2();
			*/
			
			//extract all data before performing any test
			beforeTestADS();
			beforeTestControlGroup();	
			//we have the data to perform test for the table1		
			performTable1Test();
			
			
			//Coherence functions
			
			coherenceBeforeTtestUtest();
			//beforeTestADS();
			//beforeTestControlGroup();
			//performTable2Test();
			
			performTable2Test();
			
			 
			 
		
			 
			
			 
			displayTest = true;
		}
				
		public void thesis1(){
			//insert data for thesis----------------------
					
							for(int y = 0; y < numberFilesADS; y++){
								for(int g = 0; g < 6; g++){
									for(int t = 0; t < 4; t++){
										if(t==0){
											if(g==0){
												regionADS[y][g][t] = random(9.2f,13.2f);
											}
											else if(g==1){
												regionADS[y][g][t] = random(7.4f,15.4f);
											}
											else if(g==2){
												regionADS[y][g][t] = random(6.6f,13.6f);
											}
											else if(g==3){
												regionADS[y][g][t] = random(4.3f,14.3f);
											}
											else if(g==4){
												regionADS[y][g][t] = random(7.6f,17.3f);
											}
											else{
												regionADS[y][g][t] = random(7f,17f);
											}
											
										}
										else if(t==1){
											if(g==0){
												regionADS[y][g][t] = random(4.8f,6.8f);
											}
											else if(g==1){
												regionADS[y][g][t] = random(5.2f,7.2f);
											}
											else if(g==2){
												regionADS[y][g][t]  = random(4.2f,10.2f);
											}
											else if(g==3){
												regionADS[y][g][t]  = random(4f,8f);
											}
											else if(g==4){
												regionADS[y][g][t] = random(5.5f,11.5f);
											}
											else{
												regionADS[y][g][t] = random(3.7f,11.7f);
											}
										}
										else if(t==2){
											if(g==0){
												regionADS[y][g][t] = random(2.8f,6.2f);
											}
											else if(g==1){
												regionADS[y][g][t] = random(3f,6.4f);
											}
											else if(g==2){
												regionADS[y][g][t] = random(3.7f,5.7f);
											}
											else if(g==3){
												regionADS[y][g][t] = random(4.1f,6.1f);
											}
											else if(g==4){
												regionADS[y][g][t] = random(4.1f,12.1f);
											}
											else{
												regionADS[y][g][t] = random(4f,9f);
											}
										}
										else{
											if(g==0){
												regionADS[y][g][t] = random(0.7f,2.7f);
											}
											else if(g==1){
												regionADS[y][g][t] = random(1f,2.6f);
											}
											else if(g==2){
												regionADS[y][g][t] = random(0.7f,3.3f);
											}
											else if(g==3){
												regionADS[y][g][t] = random(0.7f,2.2f);
											}
											else if(g==4){
												regionADS[y][g][t] = random(1.4f,2.4f);
											}
											else{
												regionADS[y][g][t] = random(1.2f,2.2f);
											}
										}
									}
								}
							}
		}
		
		public void thesis2(){
			//insert data for thesis----------------------
					thesis1();
							for(int y = 0; y < numberFilesControlGroup; y++){
								for(int g = 0; g < 6; g++){
									for(int t = 0; t < 4; t++){
										if(t==0){
											if(g==0){
												regionControlGroup[y][g][t] = random(6f,8f);
											}
											else if(g==1){
												regionControlGroup[y][g][t] = random(5.2f,9.2f);
											}
											else if(g==2){
												regionControlGroup[y][g][t] = random(4.4f,10.4f);
											}
											else if(g==3){
												regionControlGroup[y][g][t] = random(6.5f,10.5f);
											}
											else if(g==4){
												regionControlGroup[y][g][t] = random(5.9f,11.9f);
											}
											else{
												regionControlGroup[y][g][t] = random(6.7f,12.7f);
											}
											
										}
										else if(t==1){
											if(g==0){
												regionControlGroup[y][g][t] = random(2.8f,4.8f);
											}
											else if(g==1){
												regionControlGroup[y][g][t] = random(3f,5f);
											}
											else if(g==2){
												regionControlGroup[y][g][t] = random(2.7f,6.7f);
											}
											else if(g==3){
												regionControlGroup[y][g][t] = random(2.4f,8.4f);
											}
											else if(g==4){
												regionControlGroup[y][g][t] = random(2.7f,8.7f);
											}
											else{
												regionControlGroup[y][g][t] = random(3.1f,9.1f);
											}
										}
										else if(t==2){
											if(g==0){
												regionControlGroup[y][g][t] = random(1.9f,5.9f);
											}
											else if(g==1){
												regionControlGroup[y][g][t] = random(1.4f,7.4f);
											}
											else if(g==2){
												regionControlGroup[y][g][t] = random(1.6f,11.6f);
											}
											else if(g==3){
												regionControlGroup[y][g][t] = random(2.5f,13.5f);
											}
											else if(g==4){
												regionControlGroup[y][g][t] = random(2.2f,13.6f);
											}
											else{
												regionControlGroup[y][g][t] = random(3.2f,12.6f);
											}
										}
										else{
											if(g==0){
												regionControlGroup[y][g][t] = random(1.4f,2f);
											}
											else if(g==1){
												regionControlGroup[y][g][t] = random(1.4f,2.1f);
											}
											else if(g==2){
												regionControlGroup[y][g][t] = random(1.5f,2f);
											}
											else if(g==3){
												regionControlGroup[y][g][t] = random(1.7f,2.7f);
											}
											else if(g==4){
												regionControlGroup[y][g][t] = random(1.5f,2.5f);
											}
											else{
												regionControlGroup[y][g][t] = random(1.4f,2.4f);
											}
										}
									}
								}
							}
		}
				
		public void performTable1Test(){
			for(int i=0; i < 6; i++){
				
			
				for(int j=0; j < 4; j++){
					if(( chooseTestADS[i][j] == true ) && (chooseTestControlGroup[i][j] == true) ){
						disTest[i][j] = "tTest";
						print(" tTest");
						tTest(i,j);
						
					}
					else{
						disTest[i][j] = "uTest";
						print(" uTest");
						uTest(i,j);					
					}
								
			}
				
			
		}
			println();
			for(int i=0;i<6;i++){
				for(int j=0;j<4;j++){
					print(PvalueU[i][j]); 
				}
				println();
			}
			
			
		
		}
				
		public void tTest(int currRegion , int currBand){
			float mean1 = findMeanADS(currRegion,currBand);
			float mean2 = findMeanControlGroup(currRegion,currBand);
			float SD1 =   findSDADS(currRegion,currBand,mean1);
			float SD2 =   findSDControlGroup(currRegion,currBand,mean2);
			int df1 = numberFilesADS - 1 ;
			int df2 = numberFilesControlGroup - 1 ;
			int df = numberFilesADS-1 + numberFilesControlGroup - 1 ;
			float SS1 = SD1*SD1*df1;
			float SS2 = SD2*SD2*df2;
			float SPsquare  = ( SS1 + SS2 ) / (df1 + df2);
			float  tCalculated = (mean1 - mean2)/ sqrt( SPsquare/numberFilesADS + SPsquare/numberFilesControlGroup );
			
			float tCritical = arraytTest[df][5];
			 tTestSignificant[currRegion][currBand] = false;
			
			//critical value 0.0001
			if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
				if( tCalculated > tCritical){
					//significant
					 aValuetTest[currRegion][currBand]      = 0.0001f;
					 tTestSignificant[currRegion][currBand] = true;
				}
			
				//critical value 0.002
				if( tTestSignificant[currRegion][currBand] == false){
					tCritical = arraytTest[df][4];		
					if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
						if( tCalculated > tCritical){
							//significant
							 aValuetTest[currRegion][currBand] = 0.002f;
							 tTestSignificant[currRegion][currBand] = true;
						}
				}
					
				}	
				//critical value 0.005	
				if( tTestSignificant[currRegion][currBand] == false){					
						tCritical = arraytTest[df][3];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculated > tCritical){
								//significant
								 aValuetTest[currRegion][currBand] = 0.005f;
								 tTestSignificant[currRegion][currBand] = true;
							}
					}	
				}
				//critical value 0.01
				if( tTestSignificant[currRegion][currBand] == false){	
						
						tCritical = arraytTest[df][2];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculated > tCritical){
								//significant
								 aValuetTest[currRegion][currBand] = 0.01f;
								 tTestSignificant[currRegion][currBand] = true;
							}
					}
				}
				//critical value 0.02
				if( tTestSignificant[currRegion][currBand] == false){			
						
						tCritical = arraytTest[df][1];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculated > tCritical){
								//significant
								 aValuetTest[currRegion][currBand] = 0.02f;
								 tTestSignificant[currRegion][currBand] = true;
							}
						}	
				}		
					
						//critical value 0.05
						if( tTestSignificant[currRegion][currBand] == false){
							tCritical = arraytTest[df][0];			
							if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
								if( tCalculated > tCritical){
									//significant
									 aValuetTest[currRegion][currBand] = 0.05f;
									 tTestSignificant[currRegion][currBand] = true;
								}
					}
						
				}		
			}		
			else{
				System.out.println("False Inputs");
			}
			
	
		
		}
		
		public void uTest(int currRegion , int currBand){
			
			//firstly we copy the data to other arrays
				float[][] copyArrayADS = new float[numberFilesADS][2];
				for(int i=0;i<numberFilesADS;i++){
					copyArrayADS[i][0] = regionADS[i][currRegion][currBand];
				}
				
				float[][] copyArrayControlGroup = new float[numberFilesControlGroup][2];
				for(int i=0;i<numberFilesControlGroup;i++){
					copyArrayControlGroup[i][0] = regionControlGroup[i][currRegion][currBand];
				}
				
				//secondly we sort the arrays
				 for (int c = 0; c < ( numberFilesADS -1); c++) {
				      for (int d = 0; d < numberFilesADS - c -1; d++) {
				        if (copyArrayADS[d][0] > copyArrayADS[d+1][0]) 
				        {
				          float swap        = copyArrayADS[d][0];
				          copyArrayADS[d][0]   = copyArrayADS[d+1][0];
				          copyArrayADS[d+1][0] = swap;
				        }
				      }
				 }
				
				 
				 
				 for (int c = 0; c < (numberFilesControlGroup  -1); c++) {
				      for (int d = 0; d < numberFilesControlGroup - c -1; d++) {
				        if (copyArrayControlGroup[d][0] > copyArrayControlGroup[d+1][0]) 
				        {
				          float swap       = copyArrayControlGroup[d][0];
				          copyArrayControlGroup[d][0]   = copyArrayControlGroup[d+1][0];
				          copyArrayControlGroup[d+1][0] = swap;
				        }
				      }
				 }
				 
				 
				 
				 
				 
				 
				//thirdly we pass the data to a single array sorted and rank them
				float[][] theLongArray = new float[numberFilesADS + numberFilesControlGroup][3];
				
				
				 //now we use the rank
				  int c=0,d=0;
				  //fill the long array with the elements of the two main arrays sorted
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  if((d<numberFilesControlGroup)&&(c<numberFilesADS)){
						  if(copyArrayControlGroup[d][0] < copyArrayADS[c][0] ){
							  theLongArray[tr][0] = copyArrayControlGroup[d][0];
							  theLongArray[tr][1] = 1;
							  d++;
						  }
						  else{
							  theLongArray[tr][0] = copyArrayADS[c][0];
							  theLongArray[tr][1] = 0;
							  c++;
						  }		  
					  }
					  else if(d<numberFilesControlGroup){
						  theLongArray[tr][0] = copyArrayControlGroup[d][0];
						  theLongArray[tr][1] = 1;
						  d++;
					  }
					  else if(c<numberFilesADS){
						  theLongArray[tr][0] = copyArrayADS[c][0];
						  theLongArray[tr][1] = 0;
						  c++;
					  }
				  }			  
				  float tempSum=0;
				  //when you are finished find the ranks
				  
				  
				 
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  tempSum=0;
					  int cccc=tr;
					  float compare = theLongArray[cccc][0];
					  if(tr<(numberFilesADS+numberFilesControlGroup-1)){
						  
						 do //check if there are elements of the same value
						 { 
							  tempSum++;
							  cccc++;		 
						  }while((theLongArray[cccc][0] == compare)&&(cccc<(numberFilesADS+numberFilesControlGroup-1)));
						  	
						  if(cccc == numberFilesADS+numberFilesControlGroup-1){
								if(theLongArray[cccc][0] == theLongArray[cccc-1][0]){
							  tempSum++;
							  cccc++;
							  }
						  }				  
							  int hold = tr+1;
							 
							  float variable=0;
							  
								  while( hold <=cccc){
									  variable = variable + hold;
									  hold++;
								  }						 				  
							  
							  
							  while(tr<cccc){
								  theLongArray[tr][2]= variable/tempSum;
								  tr++;
							  }
							  tr--;							
					  }
					 else{				 
						 theLongArray[tr][2] = tr+1;					
					 }			  
				  }
				  
				
				  
				  
					 
					 
				  float Ta=0,Tb=0;
				  for(int i=0;i<numberFilesADS + numberFilesControlGroup;i++){
					  if(theLongArray[i][1] == 1){
						  Ta = theLongArray[i][2] +Ta;
						  
					  }
					  else{
						  Tb = theLongArray[i][2] +Tb;
					  }
				  }
				 
	
			
				  float Ua = Ta - (numberFilesADS*(numberFilesADS + 1 ))/2;
				  float Ub = Tb - (numberFilesControlGroup*(numberFilesControlGroup + 1 ))/2;
				  
				  float Testvalue;
				  
				  if(Ua < Ub){			  
					 Testvalue = Ua;
				  }
				  else{
					  Testvalue = Ub;
				  }
				  uTestSignificant[currRegion][currBand] = false;
				  
				  //0.01
				  //prosoxi edo
				  float Ucritical = arrayUtest2[numberFilesADS-2][numberFilesControlGroup-5];
				  if( Testvalue < Ucritical  ){
					  //singificant
					 PvalueU[currRegion][currBand] = "0.01*";
					  uTestSignificant[currRegion][currBand] = true;
					  aValueUtest[currRegion][currBand] = 0.01f;
				  }
				
				  
				  //0.05
				  if(uTestSignificant[currRegion][currBand] == false){
						  Ucritical = arrayUtest1[numberFilesADS-2][numberFilesControlGroup-5];
						  if(  Testvalue < Ucritical ){
							  //singificant
							  uTestSignificant[currRegion][currBand] = true;
							  PvalueU[currRegion][currBand] = "0.05*"; 
							  
						  }
						  else{
							  PvalueU[currRegion][currBand] = "0.05"; 
						  }
						  aValueUtest[currRegion][currBand] = 0.05f;
						  
				  }
				  
		}
		
		public float findMeanADS(int currRegion , int currBand){
			float sum= 0f;
			for(int i=0;i<numberFilesADS;i++){
				sum = regionADS[i][currRegion][currBand] + sum;
			}
			float mean = sum/numberFilesADS;
			return mean;
		}
				
		public float findMeanControlGroup(int currRegion , int currBand){
			float sum= 0f;
			for(int i=0;i<numberFilesControlGroup;i++){
				sum = regionControlGroup[i][currRegion][currBand] + sum;
			}
			float mean = sum/numberFilesControlGroup;
			return mean;
		}
		
		public float findSDADS(int currRegion , int currBand, float mean){
			float sum= 0f;
			for(int i=0;i<numberFilesADS;i++){
				sum = (regionADS[i][currRegion][currBand] - mean)*(regionADS[i][currRegion][currBand] - mean) + sum;
			}
			float variance = sum/numberFilesADS;
			float SD = sqrt(variance);
			return SD;
		}
		
		public float findSDControlGroup(int currRegion , int currBand, float mean){
			float sum= 0f;
			for(int i=0;i<numberFilesControlGroup;i++){
				sum = (regionControlGroup[i][currRegion][currBand] - mean)*(regionControlGroup[i][currRegion][currBand] - mean) + sum;
			}
			float variance = sum/numberFilesControlGroup;
			float SD = sqrt(variance);
			return SD;
		}
				
		public void beforeTestADS(){
	
			//Now we calculate each corresponding test for each field of array
			
			//loop over controlGroup function
			//loop over patients function
	
			float range[][]  = new float[6][4];
			float SD[][]     = new float[6][4];
			float mean[][]   = new float[6][4];
		
			
			// find range for mean absolute power
			for(int j=0;j<6;j++){
			    float maxDelta=regionADS[0][j][0],maxTheta=regionADS[0][j][1],maxAlpha=regionADS[0][j][2],maxBeta=regionADS[0][j][3];
				float minDelta=regionADS[0][j][0],minTheta=regionADS[0][j][1],minAlpha=regionADS[0][j][2],minBeta=regionADS[0][j][3];
				
			
					for(int i=0;i<numberFilesADS;i++){
						
						if( maxDelta < regionADS[i][j][0]){
							maxDelta = regionADS[i][j][0];
						}
						
						if( minDelta > regionADS[i][j][0]){
							minDelta = regionADS[i][j][0];
						}
						
						if( maxTheta < regionADS[i][j][1]){
							maxTheta = regionADS[i][j][1];
						}
						
						if( minTheta > regionADS[i][j][1]){
							minTheta = regionADS[i][j][1];
						}
						
						if( maxAlpha < regionADS[i][j][2]){
							maxAlpha = regionADS[i][j][2];
						}
						
						if( minAlpha > regionADS[i][j][2]){
							minAlpha = regionADS[i][j][2];
						}
						
						if( maxBeta < regionADS[i][j][3]){
							maxBeta = regionADS[i][j][3];
						}
						
						if( minBeta > regionADS[i][j][3]){
							minBeta = regionADS[i][j][3];
						}
						
						
						
					}
					range[j][0] = maxDelta - minDelta;
					range[j][1] = maxTheta - minTheta;
					range[j][2] = maxAlpha - minAlpha;
					range[j][3] = maxBeta  -  minBeta;
					
			}
			
			
			// find mean for mean absolute power
					for(int j=0;j<6;j++){
					   
						mean[j][0] = 0;
						mean[j][1] = 0;
						mean[j][2] = 0;
						mean[j][3] = 0;
					
							for(int i=0;i<numberFilesADS;i++){
										mean[j][0] = regionADS[i][j][0]  + mean[j][0];
										mean[j][1] = regionADS[i][j][1]  + mean[j][1];
										mean[j][2] = regionADS[i][j][2]  + mean[j][2];
										mean[j][3] = regionADS[i][j][3]  + mean[j][3];				
							}
							mean[j][0] = mean[j][0] / numberFilesADS;
							mean[j][1] = mean[j][1] / numberFilesADS;
							mean[j][2] = mean[j][2] / numberFilesADS;
							mean[j][3] = mean[j][3] / numberFilesADS;
							
							meanTable1ASD[j][0] = mean[j][0];
							meanTable1ASD[j][1] = mean[j][1];
							meanTable1ASD[j][2] = mean[j][2];
							meanTable1ASD[j][3] = mean[j][3];
							
					}
			
			
			
			// find mean for SD absolute power
			for(int j=0;j<6;j++){		   
					for(int i=0;i<numberFilesADS;i++){
								SD[j][0] = ( regionADS[i][j][0] - mean[j][0] ) * ( regionADS[i][j][0] - mean[j][0] ); 
								SD[j][1] = ( regionADS[i][j][1] - mean[j][1] ) * ( regionADS[i][j][1] - mean[j][1] );
								SD[j][2] = ( regionADS[i][j][2] - mean[j][2] ) * ( regionADS[i][j][2] - mean[j][2] );
								SD[j][3] = ( regionADS[i][j][3] - mean[j][3] ) * ( regionADS[i][j][3] - mean[j][3] );	
					}
					SD[j][0] = sqrt( SD[j][0]/numberFilesADS );
					SD[j][1] = sqrt( SD[j][1]/numberFilesADS );
					SD[j][2] = sqrt( SD[j][2]/numberFilesADS );
					SD[j][3] = sqrt( SD[j][3]/numberFilesADS );
					
					SDTable1ASD[j][0]  =  SD[j][0];
					SDTable1ASD[j][1]  =  SD[j][1];	
					SDTable1ASD[j][2]  =  SD[j][2];
					SDTable1ASD[j][3]  =  SD[j][3];
								
			}
			
			
			
				//mean absolute power-test normality ADS
				for(int j = 0 ; j< 6; j++){
						for(int k =0; k< 4; k++){											
								chooseTestADS[j][k] = testNormality( range[j][k] , SD[j][k] , numberFilesADS);										
				}
				
			}
				
				
			
			
					
			
		
		}
			
		public void beforeTestControlGroup(){
			//Now we calculate each corresponding test for each field of array
			
					//loop over controlGroup function
					//loop over patients function
					float range[][]  = new float[6][4];
					float SD[][]     = new float[6][4];
					float mean[][]   = new float[6][4];
				
					
					// find range for mean absolute power
					for(int j=0;j<6;j++){
					    float maxDelta=regionControlGroup[0][j][0],maxTheta=regionControlGroup[0][j][1],maxAlpha=regionControlGroup[0][j][2],maxBeta=regionControlGroup[0][j][3];
						float minDelta=regionControlGroup[0][j][0],minTheta=regionControlGroup[0][j][1],minAlpha=regionControlGroup[0][j][2],minBeta=regionControlGroup[0][j][3];
						
					
							for(int i=0;i<numberFilesControlGroup;i++){
								
								if( maxDelta < regionControlGroup[i][j][0]){
									maxDelta = regionControlGroup[i][j][0];
								}
								
								if( minDelta > regionControlGroup[i][j][0]){
									minDelta = regionControlGroup[i][j][0];
								}
								
								if( maxTheta < regionControlGroup[i][j][1]){
									maxTheta = regionControlGroup[i][j][1];
								}
								
								if( minTheta > regionControlGroup[i][j][1]){
									minTheta = regionControlGroup[i][j][1];
								}
								
								if( maxAlpha < regionControlGroup[i][j][2]){
									maxAlpha = regionControlGroup[i][j][2];
								}
								
								if( minAlpha > regionControlGroup[i][j][2]){
									minAlpha = regionControlGroup[i][j][2];
								}
								
								if( maxBeta < regionControlGroup[i][j][3]){
									maxBeta = regionControlGroup[i][j][3];
								}
								
								if( minBeta > regionControlGroup[i][j][3]){
									minBeta = regionControlGroup[i][j][3];
								}
								
								
								
							}
							range[j][0] = maxDelta - minDelta;
							range[j][1] = maxTheta - minTheta;
							range[j][2] = maxAlpha - minAlpha;
							range[j][3] = maxBeta  -  minBeta;
							
					}
					
					
					// find mean for mean absolute power
							for(int j=0;j<6;j++){
							   
								mean[j][0] = 0;
								mean[j][1] = 0;
								mean[j][2] = 0;
								mean[j][3] = 0;
							
									for(int i=0;i<numberFilesControlGroup;i++){
												mean[j][0] = regionControlGroup[i][j][0]  + mean[j][0];
												mean[j][1] = regionControlGroup[i][j][1]  + mean[j][1];
												mean[j][2] = regionControlGroup[i][j][2]  + mean[j][2];
												mean[j][3] = regionControlGroup[i][j][3]  + mean[j][3];				
									}
									mean[j][0] = mean[j][0] / numberFilesControlGroup;
									mean[j][1] = mean[j][1] / numberFilesControlGroup;
									mean[j][2] = mean[j][2] / numberFilesControlGroup;
									mean[j][3] = mean[j][3] / numberFilesControlGroup;
									
									meanTable1CG[j][0] = mean[j][0];
									meanTable1CG[j][1] = mean[j][1];
									meanTable1CG[j][2] = mean[j][2];
									meanTable1CG[j][3] = mean[j][3];
									print(meanTable1CG[j][0]); 
									print(" "); 
									print(meanTable1CG[j][1]);
									print(" "); 
									print(meanTable1CG[j][2]);
									print(" "); 
									print(meanTable1CG[j][3]); 
									println();
							}
					
					
					
					// find mean for SD absolute power
					for(int j=0;j<6;j++){		   
							for(int i=0;i<numberFilesControlGroup;i++){
										SD[j][0] = ( regionControlGroup[i][j][0] - mean[j][0] ) * ( regionControlGroup[i][j][0] - mean[j][0] ); 
										SD[j][1] = ( regionControlGroup[i][j][1] - mean[j][1] ) * ( regionControlGroup[i][j][1] - mean[j][1] );
										SD[j][2] = ( regionControlGroup[i][j][2] - mean[j][2] ) * ( regionControlGroup[i][j][2] - mean[j][2] );
										SD[j][3] = ( regionControlGroup[i][j][3] - mean[j][3] ) * ( regionControlGroup[i][j][3] - mean[j][3] );	
							}
							SD[j][0] = sqrt( SD[j][0]/numberFilesControlGroup );
							SD[j][1] = sqrt( SD[j][1]/numberFilesControlGroup );
							SD[j][2] = sqrt( SD[j][2]/numberFilesControlGroup );
							SD[j][3] = sqrt( SD[j][3]/numberFilesControlGroup );
							
							SDTable1CG[j][0]  =  SD[j][0];
							SDTable1CG[j][1]  =  SD[j][1];	
							SDTable1CG[j][2]  =  SD[j][2];
							SDTable1CG[j][3]  =  SD[j][3];
					}
					
				
						//mean absolute power-test normality ADS
						for(int j = 0 ; j< 6; j++){
								for(int k =0; k< 4; k++){											
										chooseTestControlGroup[j][k] = testNormality( range[j][k] , SD[j][k] , numberFilesControlGroup);												
						}
						
					}
						
						
		}
			
		
		//problem here 16
		public void computeRegions(){
			regionADS = new float[numberFilesADS][6][4];
			regionControlGroup = new float[numberFilesControlGroup][6][4];
			
				
					for(int k = 0;k<4;k++){
						for ( int i = 0; i < numberFilesADS; i++){
							
							//LT frontal FP1–F3–F7 -->0,10,8
							regionADS[i][0][k] = abs( patient[i][0][k] + patient[i][10][k] + patient[i][8][k] )/3; //
							
							//RT frontal FP2–F4–F8 -->1,11,9
							regionADS[i][1][k] = abs( patient[i][1][k] + patient[i][11][k] + patient[i][9][k] )/3; //		
									
							//LT central T3–C3 -->12,2
							regionADS[i][2][k] = abs( patient[i][12][k] + patient[i][2][k]  )/2; //			
									
							//RT central T4–C4 -->13,3
							regionADS[i][3][k] = abs( patient[i][3][k] + patient[i][13][k]  )/2; //			
									
							//LT posterior T5–P3–O1	-->4,14,6
							regionADS[i][4][k] = abs( patient[i][4][k] + patient[i][14][k] + patient[i][6][k] )/3; //												
									
							//RT posterior T6–P4–O2 -->5,15,7
							regionADS[i][5][k] = abs( patient[i][5][k] + patient[i][15][k] + patient[i][7][k] )/3; //	
							
							
							
					
					}
				}
				
				
				
				for(int j=0;j<6;j++){
					for(int k=0;k<4;k++){
						for ( int i = 0; i < numberFilesControlGroup; i++){
							//LT frontal FP1–F3–F7 -->0,10,8
							regionControlGroup[i][0][k] = abs( controlGroup[i][0][k] + controlGroup[i][10][k] + controlGroup[i][8][k] )/3; //
							
							//RT frontal FP2–F4–F8 -->1,11,9
							regionControlGroup[i][1][k] = abs( controlGroup[i][1][k] + controlGroup[i][11][k] + controlGroup[i][9][k] )/3; //		
									
							//LT central T3–C3 -->12,2
							regionControlGroup[i][2][k] = abs( controlGroup[i][12][k] + controlGroup[i][2][k]  )/2; //			
									
							//RT central T4–C4 -->13,3
							regionControlGroup[i][3][k] = abs( controlGroup[i][3][k] + patient[i][13][k]  )/2; //			
									
							//LT posterior T5–P3–O1	-->4,14,6
							regionControlGroup[i][4][k] = abs( controlGroup[i][4][k] + controlGroup[i][14][k] + controlGroup[i][6][k] )/3; //												
									
							//RT posterior T6–P4–O2 -->5,15,7
							regionControlGroup[i][5][k] = abs( controlGroup[i][5][k] + controlGroup[i][15][k] + controlGroup[i][7][k] )/3; //	
								
						}		
					}
				}
		}
		
	
			//this routine read the files and do the FFT plot
		public void readFilesTest(int pos ,File[] fileArray, String option){
				
		int n,start;	
		float arrayString[][] = new float[channel_number][250];
	 
		int counterLength=0; // length of file
		
		useScannerFirst(pos,fileArray);
		while(sfTest.hasNextFloat()){
			float what = sfTest.nextFloat();
			System.out.print(what);
			counterLength++;
		}
		
		sfTest.close();
		useScanner(pos,fileArray);
		
		
		
		//size of each array is the number of values used
		float theLongArrayDelta[][]         = new float[channel_number][(counterLength/256 )*5];
		float theLongArrayTheta[][]         = new float[channel_number][(counterLength/256 )*4];
		float theLongArrayAlpha[][]         = new float[channel_number][(counterLength/256 )*4];
		float theLongArrayBeta[][]          = new float[channel_number][(counterLength/256 )*13];
		counterLength=0;
		
		
		//Loop over the current file	
		 		switchForBuffer=1;
				while(sTest.hasNextFloat()){
						for(int i=0;  i<250;  i++){
								 for(int j=0; j< channel_number; j++ ){
									 if(   sTest.hasNextFloat() == false  ) {
										 arrayString[j][i] = 0f;	
									 }
									 else
									   arrayString[j][i] = sTest.nextFloat();			 
								 	}
							 	}
						
						 if(sTest.hasNextFloat()){
							 
							 if(switchForBuffer==1){
							 		n=250;
							 		start=0;
							 		switchForBuffer=2;
							 	}
							 	else if(switchForBuffer==2){
							 		n=500;
							 		start=250;
							 	}
							 	else {
							 		n=750;
							 		start=500;		
							 		switchForBuffer=3;
							 	}
							 	
							 		//stage of filtering
								 for(int i=0;  i<250;  i++){
										 for(int j=0; j< channel_number; j++ ){
											 //Filters for that test are set for 0.3-30 Hz BP and 50 Notch Filter
											 dataPacket[j][i+start] = arrayString[j][i];	
											 filterIIR(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); 
											 filterIIR(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]);	
											 filterIIR(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
										 }
								 	}
								 
								 int counterDelta =0;
									int counterTheta =0;
									int counterAlpha =0;
									int counterBeta =0;
								 //FFT	stage
								 for (int currChannel =0; currChannel < channel_number; currChannel++) {
									 	 counterDelta =0;
										 counterTheta =0;
										 counterAlpha =0;
										 counterBeta  =0;
									 
									 
							            //do what to do for each channel 	 		            	 
							              fftBuff[currChannel] = new FFT(Nfft, fs_Hz);  
							              fftBuff[currChannel].window(FFT.HAMMING);	         
							              fooData = dataPacket[currChannel];
							              if((n== 750)|| ( n==500)){
							              fooData = Arrays.copyOfRange(fooData, n-Nfft, n); 
							              }
							              else{
							            	  fooData = Arrays.copyOfRange(fooData, start, Nfft); 
							              }
							            	  
							              fftBuff[currChannel].forward(fooData);	             
							              fftLin.forward( fooData );
							              println();
							              //save stage  
							              for(int i = 0; i < fftBuff[currChannel].specSize(); i++){
								       
							            	  //get the bands  
							            	  	if(i<=4){
							            	  		theLongArrayDelta[currChannel][counterDelta] = fftBuff[currChannel].getBand(i);	
								            		counterDelta++;  
								            		print(fftBuff[currChannel].getBand(i));
								            	  	print(" ");
							            	  	}
							            	  	 else if(( i >= 5 ) && (i <= 8) ){
							            	  		theLongArrayTheta[currChannel][counterTheta] = fftBuff[currChannel].getBand(i);
							            	  		counterTheta++;  
							            	  		print(fftBuff[currChannel].getBand(i));
								            	  	print("_");
								            	  	
							            	  	 }
							            	  	else if(( i > 8 ) && (i <= 12)  ){
							            	  		theLongArrayAlpha[currChannel][counterAlpha] = fftBuff[currChannel].getBand(i);					            	  		counterAlpha++;  
								            	  }
								            	  else if(( i >= 13 ) && (i <= 25) ){
								            		  theLongArrayBeta[currChannel][counterBeta] = fftBuff[currChannel].getBand(i);
								            		  counterBeta++;  
								            	  }
							            	  	 
							            	  	 }
							             
							              counterLength++;
							              }		
								 println(); println();
								 } 
								 					 	
				} // loop that read all data from file of the patient
						 
				float sumArrayDelta;
				float sumArrayTheta;
				float sumArrayAlpha;
				float sumArrayBeta ;				 
						//now we extract the data
						for(int j =0 ; j< channel_number; j++){ 
								//find mean,SD of the delta of the current patient
							sumArrayDelta=0;
							sumArrayTheta=0;
							sumArrayAlpha=0;
							sumArrayBeta =0;
								
								//Find sum of Delta
								 for( int i=0; i < theLongArrayDelta.length; i++ ){
									 sumArrayDelta = theLongArrayDelta[j][i] + sumArrayDelta;								 
								 }
								
								
								//Find sum of Theta
								 for( int i=0; i < theLongArrayTheta.length; i++ ){
									 sumArrayTheta = theLongArrayTheta[j][i] + sumArrayTheta;							 
								 }
								 
								//Find sum of Alpha
								 for( int i=0; i < theLongArrayAlpha.length; i++ ){
									 sumArrayAlpha = theLongArrayAlpha[j][i] + sumArrayAlpha;							 
								 }
								 
								//Find sum of Beta
								 for( int i=0; i < theLongArrayBeta.length; i++ ){
									 sumArrayBeta = theLongArrayBeta[j][i] + sumArrayBeta ;							 
								 }
							 
								 //Find means for the current patient	// patient[whichPatient][whichChannel/electrode][whichband]		
									 if(option == "patient"){	 
										 patient[pos][j][0] = sumArrayDelta/theLongArrayDelta.length;
										 patient[pos][j][1] = sumArrayTheta/theLongArrayTheta.length;
										 patient[pos][j][2] = sumArrayAlpha/theLongArrayAlpha.length;
										 patient[pos][j][3] =   sumArrayBeta/theLongArrayBeta.length;		
										
									 }
									 else if(option == "controlGroup"){
										 controlGroup[pos][j][0] = sumArrayDelta/theLongArrayDelta.length;
										 controlGroup[pos][j][1] = sumArrayTheta/theLongArrayTheta.length;
										 controlGroup[pos][j][2] = sumArrayAlpha/theLongArrayAlpha.length;
										 controlGroup[pos][j][3] =   sumArrayBeta/theLongArrayBeta.length;	
										 
										 
										 print( controlGroup[pos][j][0] );
										 print(" ");
										 print( controlGroup[pos][j][1] );
										 print(" ");
										 print( controlGroup[pos][j][2] );
										 print(" ");
										 print( controlGroup[pos][j][3] );
										 println();
										 
									 }
									 
						}//println();print("---------------------------------");
						sTest.close();
		}
		
		

		Scanner sTest;
		public void useScanner(int pos,File[] arrayFile){
			try {
				sTest = new Scanner(arrayFile[pos]);
			 }
			 catch (FileNotFoundException e) {
					e.printStackTrace();
				}	
		}
		
		Scanner sfTest;
		public void useScannerFirst(int pos,File[] arrayFile){
			try {
				sfTest = new Scanner(arrayFile[pos]);
			 }
			 catch (FileNotFoundException e) {
					e.printStackTrace();
				}	
		}
		
		
		
		
		
		//------------------functions for coherence tests-------------------------------
		public void coherenceBeforeTtestUtest(){
				//check patients
				for(int t = 0; t < numberFilesADS; t++){
					readFilesTestCoherence(t,filesADS,"patient");		
				}
				
				//check ControlGroup
				for(int t = 0; t < numberFilesControlGroup; t++){
					readFilesTestCoherence(t,filesControlGroup,"controlGroup");		
				}
				
				coherenceDeltaThetaAlphaBetaASD();
				coherenceDeltaThetaAlphaBetaControlGroup();
				coherenceMeanSDTestNormality();
				
				
	
		}
		
	
		public void readFilesTestCoherence(int pos ,File[] fileArray, String option){
			
			
			
			
			
			
			//first part--reading the file
		 
			int counterLength=0; // length of file
			
			useScannerFirst(pos,fileArray);
			while(sfTest.hasNextFloat()){
				float what = sfTest.nextFloat();
				System.out.print(what);
				counterLength++;
			}
			
			sfTest.close();
			useScanner(pos,fileArray);
			
			
			//first we find the coherence in the file through welch method		
			float arrayCoherence[][] = new float[channel_number][counterLength + 1];
			for(int i=0; i<channel_number;i++){
				for(int j=0; j<counterLength+1;j++)
					arrayCoherence[i][j] = 0;
			}
			println("size of file is "+counterLength);
			int counterEachChannelLength = 0;
			int counterChannel =0;		
			while(sTest.hasNextFloat()){		
				arrayCoherence[counterChannel][counterEachChannelLength] = sTest.nextFloat();	
				
				if(counterChannel == 15){
					counterChannel = 0;
					counterEachChannelLength++;
				}
				else
					counterChannel++;
				
				
			}
			
			
			
			
			println("counterEachChannelLength is "+counterEachChannelLength);
			sTest.close();//close Formatter
			
			
			//second part, use coherence
			// vriskoume poia dinami tou 2 einai, meta exoume Cxx[][] Cxy[][] Cyy[][]
			   float Vpow = 1;
			   for(int ia = 0; ia < counterEachChannelLength; ia++){
				   if(Vpow < ia){
					   Vpow = Vpow*2;		   
				   }
			   }
			  
			 println("Vpow is "+Vpow);
			 
			 //welch method
			 FFT[][] fft;			 
			 int N = 256;             //length of segment
			 int L;                   //number of segments
			 int M;                  //stepsize
			 float[][][] segment;
			 float[][][] Sxx, Sxy;
		

		
			 
			 
			
			 M = N/2;
			 L = ((int)Vpow - N )/M  + 1;
			 
			 segment = new float[channel_number][L][N];
			 
			 //initialize segments
			 for( int ch = 0; ch < channel_number; ch++){
			 for( int l = 0; l < L; l++){  
			       for( int k = 0; k < N; k++){
			       segment[ch][l][k] = 0;			    		      
			     }
			 }
			 }
			 
			 
			 
			//find all segments from all signals
			 for( int ch = 0; ch < channel_number; ch++){
			       for( int l = 0; l < L; l++){  
			           for( int k = 0; k < N; k++){
			             segment[ch][l][k] = arrayCoherence[ch][k + l*M] * hammingWindow(arrayCoherence[ch][k],((float)k),((float)N));                
			            // println("ch is "+ch+" l is "+l+ " k is "+  k +" M is "+M);	         
			         }
			     }
			 }
		 
			 
			 
			
			 fft = new FFT[channel_number][L];
			
			 for( int i = 0; i < channel_number; i++){
				 for( int j = 0; j < L; j++){  
				  fft[i][j] = new FFT(N, 256); 
				  // create an Oscil we'll use to fill up our buffer  
				   fft[i][j].forward(segment[i][j]);		       
				  }
			 }
				 
			 
				Sxx = new float[channel_number][L][N];
				Sxy = new float[120][L][N];
				  
				
				
				//finding Sxy
				// twisting the counter
				int stdyCounter=0;
				for( int ch = 0; ch< channel_number - 1; ch++ ){
					for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int i=0; i< L; i++ ){
								for(int j=0; j< fft[ch][i].specSize(); j++ ){	
											     			      
									Sxy[stdyCounter][i][j] =  ( fft[ch][i].getBand(j)*fft[inCounter][i].getBand(j) )/ ((float)N) ;
									//println("Sxy[stdyCounter][i][j] is "+Sxy[stdyCounter][i][j]);	
								}
							}
							stdyCounter ++;
					}
				}
			 
				//Finding Sxx,Syy 
				for( int ch = 0; ch< channel_number ; ch++ ){
					   for(int i=0; i< L; i++ ){
					     for(int j=0; j< fft[ch][i].specSize(); j++ ){
					    	 Sxx[ch][i][j] =  ( fft[ch][i].getBand(j)*fft[ch][i].getBand(j) )/ ((float)N ) ;	
					    	// println("Sxx[stdyCounter][i][j] is "+Sxx[ch][i][j]);	
					     }
					   }
				}
				
				
				
				//================================= 
				
				     float[][] AVxx   = new float[channel_number][N];
				     float[][] AVxy   = new float[120][N];
				   //initialize average
				     for(int i=0; i< channel_number; i++ ){
				       for(int j=0; j< N; j++ ){
				         AVxx[i][j] = 0;
				        
				       }
				     }
				     //initialize average
				     for(int i=0; i< 120; i++ ){
					       for(int j=0; j< N; j++ ){
					    	   AVxy[i][j] = 0;					        
					       }
					     }
			     
				       
				       float temp1=0f,temp3=0f;
				      		
				       // last series summarize the spectrums
				       for(int ch=0; ch<channel_number;ch++){
				    	   for(int j=0; j< N; j++ ){
				    		   for(int i=0; i< L; i++ ){	
				    		   temp1 = Sxx[ch][i][j] + temp1;   
				    	   }
				    		   AVxx[ch][j] = temp1/ ((float)L); 	   
				    		   temp1 = 0f;
				       }
				       }
				       
				       
				       for(int ch=0; ch<120;ch++){
				       for(int j=0; j< N; j++ ){
				         for(int i=0; i< L; i++ ){			         
				         temp3 = Sxy[ch][i][j] + temp3;
				       }
				       
	        
				        AVxy[ch][j] = temp3/ ((float)L);
				       // println(" Sxx is "+AVxx[j]);				        
				        temp3 =0f;
				        
				        }    		
				       }
				       
				       
				       
				        float[][] Coherence;
				       Coherence = new float[120][N/2];
				       //println("AVxx[ch][j] is "+AVxx[ch][j]);	
				       //find coherence
				       stdyCounter=0;
					for( int ch = 0; ch< channel_number - 1; ch++ ){
						for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int j=0; j< 128; j++ ){
								if (  AVxx[ch][j] == 0){
									 Coherence[stdyCounter][j] = 1;
								}
					    	    Coherence[stdyCounter][j] = ((AVxy[stdyCounter][j])*(AVxy[stdyCounter][j])) / (AVxx[ch][j]*AVxx[inCounter][j] );					    	
					    		// println("coherence is "+ Coherence[stdyCounter][j]);
					    		 //println("stdCounter is "+stdyCounter);			    	    
					       }
							stdyCounter++;	
					       }
						}
		
		
		
			//coherence pio aplo pairno olous tous pinakes atrismenous kai meta brisko to meso oro
			//prepei na ftiakso: gia kathe pinaka coherence na ginetai kataxwrisi se delta,theta,alpha ktl
			
						
						
							 
					//now we insert the data in a long array for all groups
					for(int i =0 ; i< 120; i++){ 
							for(int j =0 ; j< 128; j++){ 							 
									 //Find means for the current patient	// patient[whichPatient][whichPair][which value]		
										 if(option == "patient"){	 
											 patientCoherence[pos][i][j] = Coherence[i][j];
										 }
										 else if(option == "controlGroup"){
											 controlGroupCoherence[pos][i][j] = Coherence[i][j];								 	 
											 print( controlGroupCoherence[pos][i][j] );
											 println();
											 
										 }
							}
										 
							}//println();print("---------------------------------");
		
			}
		
		
		float hammingWindow(float wn, float n, float N){
		     float a = 25f/46f;
		     float b = 21f/46f;
		      
		     wn = a - b*cos( ( 2f*3.14f*n) / N );
		     //println(" wn is "+wn);
		      return wn;
		    }
		
		
		
		public void coherenceDeltaThetaAlphaBetaASD(){		
			//find delta(average first 5 bands),theta,alpha,beta for each patient
			deltaCoherenceASD  = new float[numberFilesADS][120]; //delta in patient in a single pair
			thetaCoherenceASD  = new float[numberFilesADS][120]; //theta in patient in a single pair
			alphaCoherenceASD  = new float[numberFilesADS][120]; //alpha in patient in a single pair
			betaCoherenceASD   = new float[numberFilesADS][120]; //beta  in patient in a single pair
			
			for(int i=0; i < numberFilesADS; i++){
				for(int j=0; j < 120; j++){
					deltaCoherenceASD[i][j] = ( patientCoherence[i][j][0] + patientCoherence[i][j][1] + patientCoherence[i][j][2] + patientCoherence[i][j][3] + patientCoherence[i][j][4])/5 ;				
					thetaCoherenceASD[i][j] = ( patientCoherence[i][j][5] + patientCoherence[i][j][6] + patientCoherence[i][j][7] + patientCoherence[i][j][8] )/4 ;				
					alphaCoherenceASD[i][j] = ( patientCoherence[i][j][9] + patientCoherence[i][j][10] + patientCoherence[i][j][11] + patientCoherence[i][j][12] )/4 ;	
					betaCoherenceASD[i][j]  = 0;
					for(int k=13; k <=25; k++ ){
						betaCoherenceASD[i][j]  = betaCoherenceASD[i][j] + patientCoherence[i][j][k];					
					}	
					betaCoherenceASD[i][j]  =  betaCoherenceASD[i][j]/13f;
				}
			}	   
			
		}
		
		public void coherenceDeltaThetaAlphaBetaControlGroup(){
			//find delta(average first 5 bands),theta,alpha,beta for each patient
			deltaCoherenceControlGroup  = new float[numberFilesControlGroup][120]; //delta in patient in a single pair
			thetaCoherenceControlGroup  = new float[numberFilesControlGroup][120]; //theta in patient in a single pair
			alphaCoherenceControlGroup  = new float[numberFilesControlGroup][120]; //alpha in patient in a single pair
			betaCoherenceControlGroup   = new float[numberFilesControlGroup][120]; //beta  in patient in a single pair
			
			for(int i=0; i < numberFilesADS; i++){
				for(int j=0; j < 120; j++){
					deltaCoherenceControlGroup[i][j] = ( controlGroupCoherence[i][j][0] + controlGroupCoherence[i][j][1] + controlGroupCoherence[i][j][2] + controlGroupCoherence[i][j][3] + controlGroupCoherence[i][j][4])/5 ;				
					thetaCoherenceControlGroup[i][j] = ( controlGroupCoherence[i][j][5] + controlGroupCoherence[i][j][6] + controlGroupCoherence[i][j][7] + controlGroupCoherence[i][j][8] )/4 ;				
					alphaCoherenceControlGroup[i][j] = ( controlGroupCoherence[i][j][9] + controlGroupCoherence[i][j][10] + controlGroupCoherence[i][j][11] + controlGroupCoherence[i][j][12] )/4 ;	
					betaCoherenceControlGroup[i][j]  = 0;
					for(int k=13; k <=25; k++ ){
						betaCoherenceControlGroup[i][j]  = betaCoherenceControlGroup[i][j] + controlGroupCoherence[i][j][k];					
					}			
					betaCoherenceControlGroup[i][j] = betaCoherenceControlGroup[i][j]/13f;
				}
			}	   
			
		}
		
		public void coherenceMeanSDTestNormality(){
			
			
			
			
		//------------ASD group------------------------------------------------------------------
			//find mean of patient in each frequency band
			
			//initialize arrays
			for(int i =0; i< 120; i++){
				
					meanDeltaASD_Coherence[i]  = 0f;
					meanThetaASD_Coherence[i]  = 0f;
					meanAlphaASD_Coherence[i]  = 0f;
					meanBetaASD_Coherence[i]   = 0f;
					sdDeltaASD_Coherence[i]    = 0f;
					sdThetaASD_Coherence[i]    = 0f;
					sdAlphaASD_Coherence[i]    = 0f;
					sdBetaASD_Coherence[i]     = 0f;	
				
			}
			
			//find 120 mean values of patients
			for(int i =0; i< 120; i++){
				for(int j =0; j <  numberFilesADS; j++){
					meanDeltaASD_Coherence[i] = meanDeltaASD_Coherence[i] + deltaCoherenceASD[j][i];
					meanThetaASD_Coherence[i] = meanThetaASD_Coherence[i] + thetaCoherenceASD[j][i];
					meanAlphaASD_Coherence[i] = meanAlphaASD_Coherence[i] + alphaCoherenceASD[j][i];
					meanBetaASD_Coherence[i]  = meanBetaASD_Coherence[i] +   betaCoherenceASD[j][i];					
				}
				meanDeltaASD_Coherence[i] =  meanDeltaASD_Coherence[i] / (( float)numberFilesADS);
				meanThetaASD_Coherence[i] =  meanThetaASD_Coherence[i] / (( float)numberFilesADS);
				meanAlphaASD_Coherence[i] =  meanAlphaASD_Coherence[i] / (( float)numberFilesADS);
				meanBetaASD_Coherence[i]  =  meanBetaASD_Coherence[i] /  (( float)numberFilesADS);
			}
			
			
			
			
			//find 120 Standard Deviation values of patient		
			for(int i =0; i< 120; i++){
				float sumDelta=0, sumTheta=0, sumAlpha=0, sumBeta=0;
				for(int j =0; j <  numberFilesADS; j++){
					sumDelta = (deltaCoherenceASD[j][i] - meanDeltaASD_Coherence[i])*(deltaCoherenceASD[j][i] - meanDeltaASD_Coherence[i]) + sumDelta;
					sumTheta = (thetaCoherenceASD[j][i] - meanThetaASD_Coherence[i])*(thetaCoherenceASD[j][i] - meanThetaASD_Coherence[i]) + sumTheta;
					sumAlpha = (alphaCoherenceASD[j][i] - meanAlphaASD_Coherence[i])*(alphaCoherenceASD[j][i] - meanAlphaASD_Coherence[i]) + sumAlpha;
					sumBeta = (betaCoherenceASD[j][i]  - meanBetaASD_Coherence[i])*(betaCoherenceASD[j][i]    - meanBetaASD_Coherence[i]) + sumBeta;
			}
			
			float varianceDelta = sumDelta/ ((float)numberFilesADS);
			float varianceTheta = sumTheta/((float)numberFilesADS);
			float varianceAlpha = sumAlpha/((float)numberFilesADS);
			float varianceBeta  = sumBeta/((float)numberFilesADS);
			sdDeltaASD_Coherence[i] = sqrt(varianceDelta);
			sdThetaASD_Coherence[i] = sqrt(varianceTheta);
			sdAlphaASD_Coherence[i] = sqrt(varianceAlpha);
			sdBetaASD_Coherence[i]  = sqrt(varianceBeta);			
			}
			
			
			//find range of values
			float rangeDelta[]  = new float[120];
			float rangeTheta[]  = new float[120];
			float rangeAlpha[]  = new float[120];
			float rangeBeta[]   = new float[120];
						for(int j=0;j<120;j++){
						    float maxDelta=deltaCoherenceASD[0][j],maxTheta=thetaCoherenceASD[0][j],maxAlpha=alphaCoherenceASD[0][j],maxBeta=betaCoherenceASD[0][j];
							float minDelta=deltaCoherenceASD[0][j],minTheta=thetaCoherenceASD[0][j],minAlpha=alphaCoherenceASD[0][j],minBeta=betaCoherenceASD[0][j];
							
						
								for(int i=0;i<numberFilesADS;i++){
									
									if( maxDelta < deltaCoherenceASD[i][j]){
										maxDelta = deltaCoherenceASD[i][j];
									}
									
									if( minDelta > deltaCoherenceASD[i][j]){
										minDelta = deltaCoherenceASD[i][j];
									}
									
									if( maxTheta < thetaCoherenceASD[i][j]){
										maxTheta = thetaCoherenceASD[i][j];
									}
									
									if( minTheta > thetaCoherenceASD[i][j]){
										minTheta = thetaCoherenceASD[i][j];
									}
									
									if( maxAlpha < alphaCoherenceASD[i][j]){
										maxAlpha = alphaCoherenceASD[i][j];
									}
									
									if( minAlpha > alphaCoherenceASD[i][j]){
										minAlpha = alphaCoherenceASD[i][j];
									}
									
									if( maxBeta < betaCoherenceASD[i][j]){
										maxBeta = betaCoherenceASD[i][j];
									}
									
									if( minBeta > betaCoherenceASD[i][j]){
										minBeta = betaCoherenceASD[i][j];
									}
									
									
									
								}
								rangeDelta[j] = maxDelta - minDelta;
								rangeTheta[j] = maxTheta - minTheta;
								rangeAlpha[j] = maxAlpha - minAlpha;
								rangeBeta[j]  = maxBeta  -  minBeta;
								
						}
						
						
						//mean absolute power-test normality ADS
						for(int j = 0 ; j< 120; j++){											
									chooseTestASDcoherenceDelta[j] = testNormality( rangeDelta[j] , sdDeltaASD_Coherence[j] , numberFilesADS);					
									chooseTestASDcoherenceTheta[j] = testNormality( rangeTheta[j] , sdThetaASD_Coherence[j] , numberFilesADS);					
									chooseTestASDcoherenceAlpha[j] = testNormality( rangeAlpha[j] , sdAlphaASD_Coherence[j] , numberFilesADS);					
									chooseTestASDcoherenceBeta[j]  = testNormality( rangeBeta[j]  , sdBetaASD_Coherence[j]  , numberFilesADS);					
								
						}
								 
		
						
						
						
			//------------Control group------------------------------------------------------------------
							//find mean of patient in each frequency band
							
							//initialize arrays
							for(int i =0; i< 120; i++){
								
									meanDeltaCG_Coherence[i]  = 0f;
									meanThetaCG_Coherence[i]  = 0f;
									meanAlphaCG_Coherence[i]  = 0f;
									meanBetaCG_Coherence[i]   = 0f;
									sdDeltaCG_Coherence[i]    = 0f;
									sdThetaCG_Coherence[i]    = 0f;
									sdAlphaCG_Coherence[i]    = 0f;
									sdBetaCG_Coherence[i]     = 0f;									
							}
							
							//find 120 mean values of patients
							for(int i =0; i< 120; i++){
								for(int j =0; j <  numberFilesADS; j++){
									meanDeltaCG_Coherence[i] = meanDeltaCG_Coherence[i] + deltaCoherenceControlGroup[j][i];
									meanThetaCG_Coherence[i] = meanThetaCG_Coherence[i] + thetaCoherenceControlGroup[j][i];
									meanAlphaCG_Coherence[i] = meanAlphaCG_Coherence[i] + alphaCoherenceControlGroup[j][i];
									meanBetaCG_Coherence[i]  = meanBetaCG_Coherence[i] +   betaCoherenceControlGroup[j][i];					
								}
								meanDeltaCG_Coherence[i] =  meanDeltaCG_Coherence[i] / (( float)numberFilesControlGroup);
								meanThetaCG_Coherence[i] =  meanThetaCG_Coherence[i] / (( float)numberFilesControlGroup);
								meanAlphaCG_Coherence[i] =  meanAlphaCG_Coherence[i] / (( float)numberFilesControlGroup);
								meanBetaCG_Coherence[i]  =  meanBetaCG_Coherence[i] /  (( float)numberFilesControlGroup);
							}
							
							
							
							
							//find 120 Standard Deviation values of patient		
							for(int i =0; i< 120; i++){
								float sumDelta=0, sumTheta=0, sumAlpha=0, sumBeta=0;
								for(int j =0; j <  numberFilesADS; j++){
									sumDelta = (deltaCoherenceControlGroup[j][i] - meanDeltaCG_Coherence[i])*(deltaCoherenceControlGroup[j][i] - meanDeltaCG_Coherence[i]) + sumDelta;
									sumTheta = (thetaCoherenceControlGroup[j][i] - meanThetaCG_Coherence[i])*(thetaCoherenceControlGroup[j][i] - meanThetaCG_Coherence[i]) + sumTheta;
									sumAlpha = (alphaCoherenceControlGroup[j][i] - meanAlphaCG_Coherence[i])*(alphaCoherenceControlGroup[j][i] - meanAlphaCG_Coherence[i]) + sumAlpha;
									sumBeta =  (betaCoherenceControlGroup[j][i]  - meanBetaCG_Coherence[i]) *(betaCoherenceControlGroup[j][i]  - meanBetaCG_Coherence[i])  + sumBeta;
							}
							
							float varianceDelta = sumDelta/ ((float)numberFilesControlGroup);
							float varianceTheta = sumTheta/((float)numberFilesControlGroup);
							float varianceAlpha = sumAlpha/((float)numberFilesControlGroup);
							float varianceBeta  = sumBeta/((float)numberFilesControlGroup);
							sdDeltaCG_Coherence[i] = sqrt(varianceDelta);
							sdThetaCG_Coherence[i] = sqrt(varianceTheta);
							sdAlphaCG_Coherence[i] = sqrt(varianceAlpha);
							sdBetaCG_Coherence[i]  = sqrt(varianceBeta);			
							}
							
							
							//find range of values
					
										for(int j=0;j<120;j++){
										    float maxDelta=deltaCoherenceControlGroup[0][j],maxTheta=thetaCoherenceControlGroup[0][j],maxAlpha=alphaCoherenceControlGroup[0][j],maxBeta=betaCoherenceControlGroup[0][j];
											float minDelta=deltaCoherenceControlGroup[0][j],minTheta=thetaCoherenceControlGroup[0][j],minAlpha=alphaCoherenceControlGroup[0][j],minBeta=betaCoherenceControlGroup[0][j];
											
										
												for(int i=0;i<numberFilesControlGroup;i++){
													
													if( maxDelta < deltaCoherenceControlGroup[i][j]){
														maxDelta = deltaCoherenceControlGroup[i][j];
													}
													
													if( minDelta > deltaCoherenceControlGroup[i][j]){
														minDelta = deltaCoherenceControlGroup[i][j];
													}
													
													if( maxTheta < thetaCoherenceControlGroup[i][j]){
														maxTheta = thetaCoherenceControlGroup[i][j];
													}
													
													if( minTheta > thetaCoherenceControlGroup[i][j]){
														minTheta = thetaCoherenceControlGroup[i][j];
													}
													
													if( maxAlpha < alphaCoherenceControlGroup[i][j]){
														maxAlpha = alphaCoherenceControlGroup[i][j];
													}
													
													if( minAlpha > alphaCoherenceControlGroup[i][j]){
														minAlpha = alphaCoherenceControlGroup[i][j];
													}
													
													if( maxBeta < betaCoherenceControlGroup[i][j]){
														maxBeta = betaCoherenceControlGroup[i][j];
													}
													
													if( minBeta > betaCoherenceControlGroup[i][j]){
														minBeta = betaCoherenceControlGroup[i][j];
													}
													
													
													
												}
												rangeDelta[j] = maxDelta - minDelta;
												rangeTheta[j] = maxTheta - minTheta;
												rangeAlpha[j] = maxAlpha - minAlpha;
												rangeBeta[j]  = maxBeta  -  minBeta;
												
										}
										
										
										//mean absolute power-test normality ADS
										for(int j = 0 ; j< 120; j++){																					
													chooseTestControlGroupCoherenceDelta[j] = testNormality( rangeDelta[j] , sdDeltaCG_Coherence[j] , numberFilesControlGroup);					
													chooseTestControlGroupCoherenceDelta[j] = testNormality( rangeTheta[j] , sdThetaCG_Coherence[j] , numberFilesControlGroup);					
													chooseTestControlGroupCoherenceDelta[j] = testNormality( rangeAlpha[j] , sdAlphaCG_Coherence[j] , numberFilesControlGroup);					
													chooseTestControlGroupCoherenceDelta[j]  = testNormality( rangeBeta[j]  , sdBetaCG_Coherence[j]  , numberFilesControlGroup);	
													println("( "+chooseTestControlGroupCoherenceDelta[j]+" )");
										}
						
						
						
						
						
						
						
						
			//testNormality( range[j][k] , SD[j][k] , numberFilesADS);
			
		}
		
		
	
		public void performTable2Test(){
			for(int i=0; i < 120; i++){
				
			
				
					if(( chooseTestASDcoherenceDelta[i] == true ) && (chooseTestControlGroupCoherenceDelta[i] == true) ){
						disTestCohDelta[i] = "tTest";
						print(" tTest");
						tTestCoherence(i);
						
					}
					else{
						disTestCohDelta[i] = "uTest";
						print(" uTest");
						uTestCoherence(i);					
					}
								
			
					if(( chooseTestASDcoherenceTheta[i] == true ) && (chooseTestControlGroupCoherenceTheta[i] == true) ){
						disTestCohTheta[i] = "tTest";
						print(" tTest");
						tTestCoherence(i);
						
					}
					else{
						disTestCohTheta[i] = "uTest";
						print(" uTest");
						uTestCoherence(i);			
					}
					
					if(( chooseTestASDcoherenceAlpha[i] == true ) && (chooseTestControlGroupCoherenceAlpha[i] == true) ){
						disTestCohAlpha[i] = "tTest";
						print(" tTest");
						tTestCoherence(i);
						
					}
					else{
						disTestCohAlpha[i] = "uTest";
						print(" uTest");
						uTestCoherence(i);				
					}
					
					if(( chooseTestASDcoherenceBeta[i] == true ) && (chooseTestASDcoherenceBeta[i] == true) ){
						disTestCohBeta[i] = "tTest";
						print(" tTest");
						tTestCoherence(i);
						
					}
					else{
						disTestCohBeta[i] = "uTest";
						print(" uTest");
						uTestCoherence(i);						
					}
				
			
		}
			println();
			for(int i=0;i<120;i++){
				
					print(PvalueCoherenceDelta[i]); 
					print(PvalueCoherenceTheta[i]); 
					print(PvalueCoherenceAlpha[i]); 
					print(PvalueCoherenceBeta[i]); 
				println();
			}
			
			
		
		}
				
		
	
		
		public void tTestCoherence(int currPair){
			float mean1delta = meanDeltaASD_Coherence[currPair];
			float mean2delta = meanDeltaCG_Coherence[currPair];
			float mean1theta = meanThetaASD_Coherence[currPair];
			float mean2theta = meanThetaCG_Coherence[currPair];
			float mean1alpha = meanAlphaASD_Coherence[currPair];
			float mean2alpha = meanAlphaCG_Coherence[currPair];
			float mean1beta  = meanBetaASD_Coherence[currPair];
			float mean2beta  = meanBetaCG_Coherence[currPair];
			
			float SD1delta = sdDeltaASD_Coherence[currPair];
			float SD2delta = sdDeltaCG_Coherence[currPair];
			float SD1theta = sdThetaASD_Coherence[currPair];
			float SD2theta = sdThetaCG_Coherence[currPair];
			float SD1alpha = sdAlphaASD_Coherence[currPair];
			float SD2alpha = sdAlphaCG_Coherence[currPair];
			float SD1beta  = sdBetaASD_Coherence[currPair];
			float SD2beta  = sdBetaCG_Coherence[currPair];
			
			int df1 = numberFilesADS - 1 ;
			int df2 = numberFilesControlGroup - 1 ;
			int df = numberFilesADS-1 + numberFilesControlGroup - 1 ;
			
			
			float SS1delta = SD1delta*SD1delta*df1;
			float SS2delta = SD2delta*SD2delta*df2;		
			float SS1theta = SD1theta*SD1theta*df1;
			float SS2theta = SD2theta*SD2theta*df2;
			float SS1alpha = SD1alpha*SD1alpha*df1;
			float SS2alpha = SD2alpha*SD2alpha*df2;
			float SS1beta = SD1beta*SD1beta*df1;
			float SS2beta = SD2beta*SD2beta*df2;
			
			float SPsquareDelta  = ( SS1delta + SS2delta ) / (df1 + df2);
			float SPsquareTheta  = ( SS1theta + SS2theta ) / (df1 + df2);
			float SPsquareAlpha  = ( SS1alpha + SS2alpha ) / (df1 + df2);
			float SPsquareBeta  =  ( SS1beta  +  SS2beta ) / (df1 + df2);
			float  tCalculatedDelta = (mean1delta - mean2delta)/  sqrt( SPsquareDelta/((float)numberFilesADS) + SPsquareDelta/((float)numberFilesControlGroup));
			float  tCalculatedTheta = (mean1theta - mean2theta)/  sqrt( SPsquareTheta/((float)numberFilesADS) + SPsquareTheta/((float)numberFilesControlGroup));
			float  tCalculatedAlpha  = (mean1alpha - mean2alpha)/ sqrt( SPsquareAlpha/((float)numberFilesADS) + SPsquareAlpha/((float)numberFilesControlGroup));
			float  tCalculatedBeta = (mean1beta - mean2beta)/ sqrt( SPsquareBeta/((float)numberFilesADS)      + SPsquareBeta/((float)numberFilesControlGroup));
			
			
			
			
			float tCritical = arraytTest[df][5];
			 tTestSignificantCoherenceDelta[currPair] = false;
			 tTestSignificantCoherenceTheta[currPair] = false;
			 tTestSignificantCoherenceAlpha[currPair] = false;
			 tTestSignificantCoherenceBeta[currPair]  = false;
			
			 
			 
			
			if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
				
				//critical value 0.0001
				if( tCalculatedDelta > tCritical){
					//significant
					 aValuetTestCoherenceDelta[currPair]      = 0.0001f;
					 tTestSignificantCoherenceDelta[currPair] = true;
				}				
			
				//critical value 0.002
				if( tTestSignificantCoherenceDelta[currPair] == false){
					tCritical = arraytTest[df][4];		
					if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
						if( tCalculatedDelta > tCritical){
							//significant
							aValuetTestCoherenceDelta[currPair] = 0.002f;
							tTestSignificantCoherenceDelta[currPair] = true;
						}
				}
					
				}	
				//critical value 0.005	
				if( tTestSignificantCoherenceDelta[currPair] == false){					
						tCritical = arraytTest[df][3];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculatedDelta > tCritical){
								//significant
								 aValuetTestCoherenceDelta[currPair] = 0.005f;
								 tTestSignificantCoherenceDelta[currPair] = true;
							}
					}	
				}
				//critical value 0.01
				if( tTestSignificantCoherenceDelta[currPair] == false){	
						
						tCritical = arraytTest[df][2];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculatedDelta > tCritical){
								//significant
								 aValuetTestCoherenceDelta[currPair] = 0.01f;
								 tTestSignificantCoherenceDelta[currPair] = true;
							}
					}
				}
				//critical value 0.02
				if( tTestSignificantCoherenceDelta[currPair] == false){			
						
						tCritical = arraytTest[df][1];
						if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
							if( tCalculatedDelta > tCritical){
								//significant
								 aValuetTestCoherenceDelta[currPair] = 0.02f;
								 tTestSignificantCoherenceDelta[currPair] = true;
							}
						}	
				}		
					
						//critical value 0.05
						if( tTestSignificantCoherenceDelta[currPair] == false){
							tCritical = arraytTest[df][0];			
							if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
								if( tCalculatedDelta > tCritical){
									//significant
									 aValuetTestCoherenceDelta[currPair] = 0.05f;
									 tTestSignificantCoherenceDelta[currPair] = true;
								}
					}
						
				}
						
			//---------------------theta t test----------------------------------
						
						//critical value 0.0001
						if( tCalculatedTheta > tCritical){
							//significant
							 aValuetTestCoherenceTheta[currPair]      = 0.0001f;
							 tTestSignificantCoherenceTheta[currPair] = true;
						}				
					
						//critical value 0.002
						if( tTestSignificantCoherenceTheta[currPair] == false){
							tCritical = arraytTest[df][4];		
							if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
								if( tCalculatedTheta > tCritical){
									//significant
									aValuetTestCoherenceTheta[currPair] = 0.002f;
									tTestSignificantCoherenceTheta[currPair] = true;
								}
						}
							
						}	
						//critical value 0.005	
						if( tTestSignificantCoherenceTheta[currPair] == false){					
								tCritical = arraytTest[df][3];
								if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
									if( tCalculatedTheta > tCritical){
										//significant
										 aValuetTestCoherenceTheta[currPair] = 0.005f;
										 tTestSignificantCoherenceTheta[currPair] = true;
									}
							}	
						}
						//critical value 0.01
						if( tTestSignificantCoherenceTheta[currPair] == false){	
								
								tCritical = arraytTest[df][2];
								if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
									if( tCalculatedTheta > tCritical){
										//significant
										 aValuetTestCoherenceTheta[currPair] = 0.01f;
										 tTestSignificantCoherenceTheta[currPair] = true;
									}
							}
						}
						//critical value 0.02
						if( tTestSignificantCoherenceTheta[currPair] == false){			
								
								tCritical = arraytTest[df][1];
								if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
									if( tCalculatedTheta > tCritical){
										//significant
										 aValuetTestCoherenceTheta[currPair] = 0.02f;
										 tTestSignificantCoherenceTheta[currPair] = true;
									}
								}	
						}		
							
								//critical value 0.05
								if( tTestSignificantCoherenceTheta[currPair] == false){
									tCritical = arraytTest[df][0];			
									if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
										if( tCalculatedTheta > tCritical){
											//significant
											 aValuetTestCoherenceTheta[currPair] = 0.05f;
											 tTestSignificantCoherenceTheta[currPair] = true;
										}
							}
								
						}
						
				//-------------------------------alpha t test----------------------------------
								
								//critical value 0.0001
								if( tCalculatedAlpha > tCritical){
									//significant
									 aValuetTestCoherenceAlpha[currPair]      = 0.0001f;
									 tTestSignificantCoherenceAlpha[currPair] = true;
								}				
							
								//critical value 0.002
								if( tTestSignificantCoherenceAlpha[currPair] == false){
									tCritical = arraytTest[df][4];		
									if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
										if( tCalculatedAlpha > tCritical){
											//significant
											aValuetTestCoherenceAlpha[currPair] = 0.002f;
											tTestSignificantCoherenceAlpha[currPair] = true;
										}
								}
									
								}	
								//critical value 0.005	
								if( tTestSignificantCoherenceAlpha[currPair] == false){					
										tCritical = arraytTest[df][3];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedAlpha > tCritical){
												//significant
												 aValuetTestCoherenceAlpha[currPair] = 0.005f;
												 tTestSignificantCoherenceAlpha[currPair] = true;
											}
									}	
								}
								//critical value 0.01
								if( tTestSignificantCoherenceAlpha[currPair] == false){	
										
										tCritical = arraytTest[df][2];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedAlpha > tCritical){
												//significant
												 aValuetTestCoherenceAlpha[currPair] = 0.01f;
												 tTestSignificantCoherenceAlpha[currPair] = true;
											}
									}
								}
								//critical value 0.02
								if( tTestSignificantCoherenceAlpha[currPair] == false){			
										
										tCritical = arraytTest[df][1];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedAlpha > tCritical){
												//significant
												 aValuetTestCoherenceAlpha[currPair] = 0.02f;
												 tTestSignificantCoherenceAlpha[currPair] = true;
											}
										}	
								}		
									
										//critical value 0.05
										if( tTestSignificantCoherenceAlpha[currPair] == false){
											tCritical = arraytTest[df][0];			
											if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
												if( tCalculatedAlpha > tCritical){
													//significant
													 aValuetTestCoherenceAlpha[currPair] = 0.05f;
													 tTestSignificantCoherenceAlpha[currPair] = true;
												}
									}
										
								}
						
										
					//-------------------------------beta t test----------------------------------
								
								//critical value 0.0001
								if( tCalculatedBeta > tCritical){
									//significant
									 aValuetTestCoherenceBeta[currPair]      = 0.0001f;
									 tTestSignificantCoherenceBeta[currPair] = true;
								}				
							
								//critical value 0.002
								if( tTestSignificantCoherenceBeta[currPair] == false){
									tCritical = arraytTest[df][4];		
									if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
										if( tCalculatedBeta > tCritical){
											//significant
											aValuetTestCoherenceBeta[currPair] = 0.002f;
											tTestSignificantCoherenceBeta[currPair] = true;
										}
								}
									
								}	
								//critical value 0.005	
								if( tTestSignificantCoherenceBeta[currPair] == false){					
										tCritical = arraytTest[df][3];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedBeta > tCritical){
												//significant
												 aValuetTestCoherenceBeta[currPair] = 0.005f;
												 tTestSignificantCoherenceBeta[currPair] = true;
											}
									}	
								}
								//critical value 0.01
								if( tTestSignificantCoherenceBeta[currPair] == false){	
										
										tCritical = arraytTest[df][2];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedBeta > tCritical){
												//significant
												 aValuetTestCoherenceBeta[currPair] = 0.01f;
												 tTestSignificantCoherenceBeta[currPair] = true;
											}
									}
								}
								//critical value 0.02
								if( tTestSignificantCoherenceBeta[currPair] == false){			
										
										tCritical = arraytTest[df][1];
										if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
											if( tCalculatedBeta > tCritical){
												//significant
												 aValuetTestCoherenceBeta[currPair] = 0.02f;
												 tTestSignificantCoherenceBeta[currPair] = true;
											}
										}	
								}		
									
										//critical value 0.05
										if( tTestSignificantCoherenceBeta[currPair] == false){
											tCritical = arraytTest[df][0];			
											if(  ((df>=1)  &&  ( df< 30 )) || (df==30) ||(df == 120) ) {
												if( tCalculatedBeta > tCritical){
													//significant
													 aValuetTestCoherenceBeta[currPair] = 0.05f;
													 tTestSignificantCoherenceBeta[currPair] = true;
												}
									}
										
								}



						
						
						
			}		
			else{
				System.out.println("False Inputs");
			}
			
	
		
		}
		
		public void uTestCoherence(int currPair){
			
			//firstly we copy the data to other arrays
				float[][] deltaCopyArrayADS = new float[numberFilesADS][2];
				float[][] thetaCopyArrayADS = new float[numberFilesADS][2];
				float[][] alphaCopyArrayADS = new float[numberFilesADS][2];
				float[][] betaCopyArrayADS = new float[numberFilesADS][2];
				
				for(int i=0;i<numberFilesADS;i++){
					deltaCopyArrayADS[i][0] = deltaCoherenceASD[i][currPair];
					thetaCopyArrayADS[i][0] = thetaCoherenceASD[i][currPair];
					alphaCopyArrayADS[i][0] = alphaCoherenceASD[i][currPair];
					betaCopyArrayADS[i][0]  =  betaCoherenceASD[i][currPair];
				}
				
				
				float[][] deltaCopyArrayControlGroup = new float[numberFilesADS][2];
				float[][] thetaCopyArrayControlGroup = new float[numberFilesADS][2];
				float[][] alphaCopyArrayControlGroup = new float[numberFilesADS][2];
				float[][] betaCopyArrayControlGroup  = new float[numberFilesADS][2];
				
				for(int i=0;i<numberFilesControlGroup;i++){
					deltaCopyArrayControlGroup[i][0] = deltaCoherenceControlGroup[i][currPair];
					thetaCopyArrayControlGroup[i][0] = thetaCoherenceControlGroup[i][currPair];
					alphaCopyArrayControlGroup[i][0] = alphaCoherenceControlGroup[i][currPair];
					betaCopyArrayControlGroup[i][0]  =  betaCoherenceControlGroup[i][currPair];					
				}
				
				
				
				//secondly we sort the arrays
				 for (int c = 0; c < ( numberFilesADS -1); c++) {
				      for (int d = 0; d < numberFilesADS - c -1; d++) {
				        if (deltaCopyArrayADS[d][0] > deltaCopyArrayADS[d+1][0]) 
				        {
				          float swap        = deltaCopyArrayADS[d][0];
				          deltaCopyArrayADS[d][0]   = deltaCopyArrayADS[d+1][0];
				          deltaCopyArrayADS[d+1][0] = swap;
				        }
				        
				        
				        if (thetaCopyArrayADS[d][0] > thetaCopyArrayADS[d+1][0]) 
				        {
				          float swap        = thetaCopyArrayADS[d][0];
				          thetaCopyArrayADS[d][0]   = thetaCopyArrayADS[d+1][0];
				          thetaCopyArrayADS[d+1][0] = swap;
				        }
				        
				        
				        if (alphaCopyArrayADS[d][0] > alphaCopyArrayADS[d+1][0]) 
				        {
				          float swap        = alphaCopyArrayADS[d][0];
				          alphaCopyArrayADS[d][0]   = alphaCopyArrayADS[d+1][0];
				          alphaCopyArrayADS[d+1][0] = swap;
				        }
				        
				        
				        if (betaCopyArrayADS[d][0] > betaCopyArrayADS[d+1][0]) 
				        {
				          float swap        = betaCopyArrayADS[d][0];
				          betaCopyArrayADS[d][0]   = betaCopyArrayADS[d+1][0];
				          betaCopyArrayADS[d+1][0] = swap;
				        }
        
				        
				      }
				 }
				
				 
				 
				 for (int c = 0; c < (numberFilesControlGroup  -1); c++) {
				      for (int d = 0; d < numberFilesControlGroup - c -1; d++) {
				        if (deltaCopyArrayControlGroup[d][0] > deltaCopyArrayControlGroup[d+1][0]) 
				        {
				          float swap       = deltaCopyArrayControlGroup[d][0];
				          deltaCopyArrayControlGroup[d][0]   = deltaCopyArrayControlGroup[d+1][0];
				          deltaCopyArrayControlGroup[d+1][0] = swap;
				        }
				        
				        if (thetaCopyArrayControlGroup[d][0] > thetaCopyArrayControlGroup[d+1][0]) 
				        {
				          float swap       = thetaCopyArrayControlGroup[d][0];
				          thetaCopyArrayControlGroup[d][0]   = thetaCopyArrayControlGroup[d+1][0];
				          thetaCopyArrayControlGroup[d+1][0] = swap;
				        }
				        
				        if (alphaCopyArrayControlGroup[d][0] > alphaCopyArrayControlGroup[d+1][0]) 
				        {
				          float swap       = alphaCopyArrayControlGroup[d][0];
				          alphaCopyArrayControlGroup[d][0]   = alphaCopyArrayControlGroup[d+1][0];
				          alphaCopyArrayControlGroup[d+1][0] = swap;
				        }
				        
				        if (betaCopyArrayControlGroup[d][0] > betaCopyArrayControlGroup[d+1][0]) 
				        {
				          float swap       = betaCopyArrayControlGroup[d][0];
				          betaCopyArrayControlGroup[d][0]   = betaCopyArrayControlGroup[d+1][0];
				          betaCopyArrayControlGroup[d+1][0] = swap;
				        }
				      }
				 }
				 
				 
				 
				 
				 
				 
				//thirdly we pass the data to a single array sorted and rank them
				float[][] theLongArrayDelta = new float[numberFilesADS + numberFilesControlGroup][3];
				float[][] theLongArrayTheta = new float[numberFilesADS + numberFilesControlGroup][3];
				float[][] theLongArrayAlpha = new float[numberFilesADS + numberFilesControlGroup][3];
				float[][] theLongArrayBeta = new float[numberFilesADS + numberFilesControlGroup][3];
				
				
				 //now we use the rank
				  int c=0,d=0;
				  
				  //fill the long array with the elements of the two main arrays sorted
				  //delta
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  if((d<numberFilesControlGroup)&&(c<numberFilesADS)){
					
						  if(deltaCopyArrayControlGroup[d][0] < deltaCopyArrayADS[c][0] ){
							  theLongArrayDelta[tr][0] = deltaCopyArrayControlGroup[d][0];
							  theLongArrayDelta[tr][1] = 1;
							  d++;
						  }
						  else{
							  theLongArrayDelta[tr][0] = deltaCopyArrayADS[c][0];
							  theLongArrayDelta[tr][1] = 0;
							  c++;
						  }				  
					  }
					  else if(d<numberFilesControlGroup){
						  theLongArrayDelta[tr][0] = deltaCopyArrayControlGroup[d][0];
						  theLongArrayDelta[tr][1] = 1;
						  d++;
					  }
					  else if(c<numberFilesADS){
						  theLongArrayDelta[tr][0] = deltaCopyArrayADS[c][0];
						  theLongArrayDelta[tr][1] = 0;
						  c++;
					  }
				  }
				  
				  //theta
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  if((d<numberFilesControlGroup)&&(c<numberFilesADS)){
		
						  if(thetaCopyArrayControlGroup[d][0] < thetaCopyArrayADS[c][0] ){
							  theLongArrayTheta[tr][0] = thetaCopyArrayControlGroup[d][0];
							  theLongArrayTheta[tr][1] = 1;
							  d++;
						  }
						  else{
							  theLongArrayTheta[tr][0] = thetaCopyArrayADS[c][0];
							  theLongArrayTheta[tr][1] = 0;
							  c++;
						  }				  
					  }
					  else if(d<numberFilesControlGroup){
						  theLongArrayTheta[tr][0] = thetaCopyArrayControlGroup[d][0];
						  theLongArrayTheta[tr][1] = 1;
						  d++;
					  }
					  else if(c<numberFilesADS){
						  theLongArrayTheta[tr][0] = thetaCopyArrayADS[c][0];
						  theLongArrayTheta[tr][1] = 0;
						  c++;
					  }
				  }
				  
				  
				  //alpha
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  if((d<numberFilesControlGroup)&&(c<numberFilesADS)){
		
						  if(alphaCopyArrayControlGroup[d][0] < alphaCopyArrayADS[c][0] ){
							  theLongArrayAlpha[tr][0] = alphaCopyArrayControlGroup[d][0];
							  theLongArrayAlpha[tr][1] = 1;
							  d++;
						  }
						  else{
							  theLongArrayAlpha[tr][0] = alphaCopyArrayADS[c][0];
							  theLongArrayAlpha[tr][1] = 0;
							  c++;
						  }				  
					  }
					  else if(d<numberFilesControlGroup){
						  theLongArrayAlpha[tr][0] = alphaCopyArrayControlGroup[d][0];
						  theLongArrayAlpha[tr][1] = 1;
						  d++;
					  }
					  else if(c<numberFilesADS){
						  theLongArrayAlpha[tr][0] = alphaCopyArrayADS[c][0];
						  theLongArrayAlpha[tr][1] = 0;
						  c++;
					  }
				  }
				  
				  //beta
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  if((d<numberFilesControlGroup)&&(c<numberFilesADS)){
		
						  if(betaCopyArrayControlGroup[d][0] < betaCopyArrayADS[c][0] ){
							  theLongArrayBeta[tr][0] = betaCopyArrayControlGroup[d][0];
							  theLongArrayBeta[tr][1] = 1;
							  d++;
						  }
						  else{
							  theLongArrayBeta[tr][0] = betaCopyArrayADS[c][0];
							  theLongArrayBeta[tr][1] = 0;
							  c++;
						  }				  
					  }
					  else if(d<numberFilesControlGroup){
						  theLongArrayBeta[tr][0] = betaCopyArrayControlGroup[d][0];
						  theLongArrayBeta[tr][1] = 1;
						  d++;
					  }
					  else if(c<numberFilesADS){
						  theLongArrayBeta[tr][0] = betaCopyArrayADS[c][0];
						  theLongArrayBeta[tr][1] = 0;
						  c++;
					  }
				  }
				    
				  
		  
				  
				  
				  
	//--------------------------------------------------------------------------------			  
				  
				  
				  
				  float tempSum=0;
				  //when you are finished find the ranks---------------------------
				  
				  
	 //------------------------------delta----------------------------------------------------
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  tempSum=0;
					  int cccc=tr;
					  float compare = theLongArrayDelta[cccc][0];
					  if(tr<(numberFilesADS+numberFilesControlGroup-1)){
						  
						 do //check if there are elements of the same value
						 { 
							  tempSum++;
							  cccc++;		 
						  }while((theLongArrayDelta[cccc][0] == compare)&&(cccc<(numberFilesADS+numberFilesControlGroup-1)));
						  	
						  if(cccc == numberFilesADS+numberFilesControlGroup-1){
								if(theLongArrayDelta[cccc][0] == theLongArrayDelta[cccc-1][0]){
							  tempSum++;
							  cccc++;
							  }
						  }				  
							  int hold = tr+1;
							 
							  float variable=0;
							  
								  while( hold <=cccc){
									  variable = variable + hold;
									  hold++;
								  }						 				  
							  
							  
							  while(tr<cccc){
								  theLongArrayDelta[tr][2]= variable/tempSum;
								  tr++;
							  }
							  tr--;							
					  }
					 else{				 
						 theLongArrayDelta[tr][2] = tr+1;					
					 }			  
				  }
				  
				
				  
				  
					 
					 
				  float Ta=0,Tb=0;
				  for(int i=0;i<numberFilesADS + numberFilesControlGroup;i++){
					  if(theLongArrayDelta[i][1] == 1){
						  Ta = theLongArrayDelta[i][2] +Ta;
						  
					  }
					  else{
						  Tb = theLongArrayDelta[i][2] +Tb;
					  }
				  }
				 
	
			
				  float Ua = Ta - (numberFilesADS*(numberFilesADS + 1 ))/2;
				  float Ub = Tb - (numberFilesControlGroup*(numberFilesControlGroup + 1 ))/2;
				  
				  float Testvalue;
				  
				  if(Ua < Ub){			  
					 Testvalue = Ua;
				  }
				  else{
					  Testvalue = Ub;
				  }
				  uTestSignificantCoherenceDelta[currPair] = false;
				  
				  //0.01
				  //prosoxi edo
				  float Ucritical = arrayUtest2[numberFilesADS-2][numberFilesControlGroup-5];
				  if( Testvalue < Ucritical  ){
					  //singificant
					  PvalueCoherenceDelta[currPair] = "0.01*";
					  uTestSignificantCoherenceDelta[currPair] = true;
					  aValueUtestCoherenceDelta[currPair] = 0.01f;
				  }
				
				  
				  //0.05
				  if(uTestSignificantCoherenceDelta[currPair] == false){
						  Ucritical = arrayUtest1[numberFilesADS-2][numberFilesControlGroup-5];
						  if(  Testvalue < Ucritical ){
							  //singificant
							  uTestSignificantCoherenceDelta[currPair] = true;
							  PvalueCoherenceDelta[currPair] = "0.05*"; 
							  
						  }
						  else{
							  PvalueCoherenceDelta[currPair] = "0.05"; 
						  }
						  aValueUtestCoherenceDelta[currPair] = 0.05f;
						  
				  }
				  
				  
				  
				  
		//---------------------------------------theta------------------------------
				  
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  tempSum=0;
					  int cccc=tr;
					  float compare = theLongArrayTheta[cccc][0];
					  if(tr<(numberFilesADS+numberFilesControlGroup-1)){
						  
						 do //check if there are elements of the same value
						 { 
							  tempSum++;
							  cccc++;		 
						  }while((theLongArrayTheta[cccc][0] == compare)&&(cccc<(numberFilesADS+numberFilesControlGroup-1)));
						  	
						  if(cccc == numberFilesADS+numberFilesControlGroup-1){
								if(theLongArrayTheta[cccc][0] == theLongArrayTheta[cccc-1][0]){
							  tempSum++;
							  cccc++;
							  }
						  }				  
							  int hold = tr+1;
							 
							  float variable=0;
							  
								  while( hold <=cccc){
									  variable = variable + hold;
									  hold++;
								  }						 				  
							  
							  
							  while(tr<cccc){
								  theLongArrayTheta[tr][2]= variable/tempSum;
								  tr++;
							  }
							  tr--;							
					  }
					 else{				 
						 theLongArrayTheta[tr][2] = tr+1;					
					 }			  
				  }
				  
				
				  
				  
					 
					 
				   Ta=0;Tb=0;
				  for(int i=0;i<numberFilesADS + numberFilesControlGroup;i++){
					  if(theLongArrayTheta[i][1] == 1){
						  Ta = theLongArrayTheta[i][2] +Ta;
						  
					  }
					  else{
						  Tb = theLongArrayTheta[i][2] +Tb;
					  }
				  }
				 
	
			
				   Ua = Ta - (numberFilesADS*(numberFilesADS + 1 ))/2;
				   Ub = Tb - (numberFilesControlGroup*(numberFilesControlGroup + 1 ))/2;
				  
				 
				  
				  if(Ua < Ub){			  
					 Testvalue = Ua;
				  }
				  else{
					  Testvalue = Ub;
				  }
				  uTestSignificantCoherenceTheta[currPair] = false;
				  
				  //0.01
				  //prosoxi edo
				   Ucritical = arrayUtest2[numberFilesADS-2][numberFilesControlGroup-5];
				  if( Testvalue < Ucritical  ){
					  //singificant
					  PvalueCoherenceTheta[currPair] = "0.01*";
					  uTestSignificantCoherenceTheta[currPair] = true;
					  aValueUtestCoherenceTheta[currPair] = 0.01f;
				  }
				
				  
				  //0.05
				  if(uTestSignificantCoherenceTheta[currPair] == false){
						  Ucritical = arrayUtest1[numberFilesADS-2][numberFilesControlGroup-5];
						  if(  Testvalue < Ucritical ){
							  //singificant
							  uTestSignificantCoherenceTheta[currPair] = true;
							  PvalueCoherenceTheta[currPair] = "0.05*"; 
							  
						  }
						  else{
							  PvalueCoherenceTheta[currPair] = "0.05"; 
						  }
						  aValueUtestCoherenceTheta[currPair] = 0.05f;
						  
				  }
				  
				  
				//----------------------------alpha----------------------------------
				  tempSum=0;
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  tempSum=0;
					  int cccc=tr;
					  float compare = theLongArrayAlpha[cccc][0];
					  if(tr<(numberFilesADS+numberFilesControlGroup-1)){
						  
						 do //check if there are elements of the same value
						 { 
							  tempSum++;
							  cccc++;		 
						  }while((theLongArrayAlpha[cccc][0] == compare)&&(cccc<(numberFilesADS+numberFilesControlGroup-1)));
						  	
						  if(cccc == numberFilesADS+numberFilesControlGroup-1){
								if(theLongArrayAlpha[cccc][0] == theLongArrayAlpha[cccc-1][0]){
							  tempSum++;
							  cccc++;
							  }
						  }				  
							  int hold = tr+1;
							 
							  float variable=0;
							  
								  while( hold <=cccc){
									  variable = variable + hold;
									  hold++;
								  }						 				  
							  
							  
							  while(tr<cccc){
								  theLongArrayAlpha[tr][2]= variable/tempSum;
								  tr++;
							  }
							  tr--;							
					  }
					 else{				 
						 theLongArrayAlpha[tr][2] = tr+1;					
					 }			  
				  }
				  
				
				  
				  
					 
					 
				   Ta=0;Tb=0;
				  for(int i=0;i<numberFilesADS + numberFilesControlGroup;i++){
					  if(theLongArrayAlpha[i][1] == 1){
						  Ta = theLongArrayAlpha[i][2] +Ta;
						  
					  }
					  else{
						  Tb = theLongArrayAlpha[i][2] +Tb;
					  }
				  }
				 
	
			
				   Ua = Ta - (numberFilesADS*(numberFilesADS + 1 ))/2;
				   Ub = Tb - (numberFilesControlGroup*(numberFilesControlGroup + 1 ))/2;
				  
				 
				  
				  if(Ua < Ub){			  
					 Testvalue = Ua;
				  }
				  else{
					  Testvalue = Ub;
				  }
				  uTestSignificantCoherenceAlpha[currPair] = false;
				  
				  //0.01
				  //prosoxi edo
				   Ucritical = arrayUtest2[numberFilesADS-2][numberFilesControlGroup-5];
				  if( Testvalue < Ucritical  ){
					  //singificant
					  PvalueCoherenceAlpha[currPair] = "0.01*";
					  uTestSignificantCoherenceAlpha[currPair] = true;
					  aValueUtestCoherenceAlpha[currPair] = 0.01f;
				  }
				
				  
				  //0.05
				  if(uTestSignificantCoherenceAlpha[currPair] == false){
						  Ucritical = arrayUtest1[numberFilesADS-2][numberFilesControlGroup-5];
						  if(  Testvalue < Ucritical ){
							  //singificant
							  uTestSignificantCoherenceAlpha[currPair] = true;
							  PvalueCoherenceAlpha[currPair] = "0.05*"; 
							  
						  }
						  else{
							  PvalueCoherenceAlpha[currPair] = "0.05"; 
						  }
						  aValueUtestCoherenceAlpha[currPair] = 0.05f;
						  
				  }
				  
				  
	//----------------------------beta----------------------------------
				  tempSum=0;
				  for(int tr = 0; tr< numberFilesADS+numberFilesControlGroup;tr++ ){
					  tempSum=0;
					  int cccc=tr;
					  float compare = theLongArrayBeta[cccc][0];
					  if(tr<(numberFilesADS+numberFilesControlGroup-1)){
						  
						 do //check if there are elements of the same value
						 { 
							  tempSum++;
							  cccc++;		 
						  }while((theLongArrayBeta[cccc][0] == compare)&&(cccc<(numberFilesADS+numberFilesControlGroup-1)));
						  	
						  if(cccc == numberFilesADS+numberFilesControlGroup-1){
								if(theLongArrayBeta[cccc][0] == theLongArrayBeta[cccc-1][0]){
							  tempSum++;
							  cccc++;
							  }
						  }				  
							  int hold = tr+1;
							 
							  float variable=0;
							  
								  while( hold <=cccc){
									  variable = variable + hold;
									  hold++;
								  }						 				  
							  
							  
							  while(tr<cccc){
								  theLongArrayBeta[tr][2]= variable/tempSum;
								  tr++;
							  }
							  tr--;							
					  }
					 else{				 
						 theLongArrayBeta[tr][2] = tr+1;					
					 }			  
				  }
				  
				
				  
				  
					 
					 
				   Ta=0;Tb=0;
				  for(int i=0;i<numberFilesADS + numberFilesControlGroup;i++){
					  if(theLongArrayBeta[i][1] == 1){
						  Ta = theLongArrayBeta[i][2] +Ta;
						  
					  }
					  else{
						  Tb = theLongArrayBeta[i][2] +Tb;
					  }
				  }
				 
	
			
				   Ua = Ta - (numberFilesADS*(numberFilesADS + 1 ))/2;
				   Ub = Tb - (numberFilesControlGroup*(numberFilesControlGroup + 1 ))/2;
				  
				 
				  
				  if(Ua < Ub){			  
					 Testvalue = Ua;
				  }
				  else{
					  Testvalue = Ub;
				  }
				  uTestSignificantCoherenceBeta[currPair] = false;
				  
				  //0.01
				  //prosoxi edo
				   Ucritical = arrayUtest2[numberFilesADS-2][numberFilesControlGroup-5];
				  if( Testvalue < Ucritical  ){
					  //singificant
					  PvalueCoherenceBeta[currPair] = "0.01*";
					  uTestSignificantCoherenceBeta[currPair] = true;
					  aValueUtestCoherenceBeta[currPair] = 0.01f;
				  }
				
				  
				  //0.05
				  if(uTestSignificantCoherenceBeta[currPair] == false){
						  Ucritical = arrayUtest1[numberFilesADS-2][numberFilesControlGroup-5];
						  if(  Testvalue < Ucritical ){
							  //singificant
							  uTestSignificantCoherenceBeta[currPair] = true;
							  PvalueCoherenceBeta[currPair] = "0.05*"; 
							  
						  }
						  else{
							  PvalueCoherenceBeta[currPair] = "0.05"; 
						  }
						  aValueUtestCoherenceBeta[currPair] = 0.05f;
						  
				  }
				  
				  
		}
		
		
		
		
		//-----------------------------initializing arrays for tests---------
		//initializing arrays for tests
		
		
		public void fillarrayUtest1(){
			arrayUtest1[0][3]  = 0;
			arrayUtest1[0][4]  = 0;
			arrayUtest1[0][5]  = 0;
			arrayUtest1[0][6]  = 0;
			arrayUtest1[0][7]  = 1;
			arrayUtest1[0][8]  = 1;
			arrayUtest1[0][9]  = 1;
			arrayUtest1[0][9]  = 1;
			arrayUtest1[0][10] = 1;
			arrayUtest1[0][11] = 2;
			arrayUtest1[0][11] = 2;
			arrayUtest1[0][12] = 2;
			arrayUtest1[0][13] = 2;
			arrayUtest1[0][14] = 2;
			
			
			arrayUtest1[1][0]  = 0;
			arrayUtest1[1][1]  = 1;
			arrayUtest1[1][2]  = 1;
			arrayUtest1[1][3]  = 2;
			arrayUtest1[1][4]  = 2;
			arrayUtest1[1][5]  = 3;
			arrayUtest1[1][6]  = 3;
			arrayUtest1[1][7]  = 4;
			arrayUtest1[1][8]  = 4;
			arrayUtest1[1][9]  = 5;
			arrayUtest1[1][10] = 5;
			arrayUtest1[1][11] = 6;
			arrayUtest1[1][12] = 6;
			arrayUtest1[1][13] = 7;
			arrayUtest1[1][14] = 7;
			arrayUtest1[1][15] = 8;
			
			arrayUtest1[2][0]  = 0;
			arrayUtest1[2][1]  = 1;
			arrayUtest1[2][2]  = 2;
			arrayUtest1[2][3]  = 3;
			arrayUtest1[2][4]  = 4;
			arrayUtest1[2][5]  = 4;
			arrayUtest1[2][6]  = 5;
			arrayUtest1[2][7]  = 6;
			arrayUtest1[2][8]  = 7;
			arrayUtest1[2][9]  = 9;
			arrayUtest1[2][10] = 10;
			arrayUtest1[2][11] = 11;
			arrayUtest1[2][12] = 11;
			arrayUtest1[2][13] = 12;
			arrayUtest1[2][14] = 13;
			arrayUtest1[2][15] = 14;
			
			arrayUtest1[3][0]  = 2;
			arrayUtest1[3][1]  = 3;
			arrayUtest1[3][2]  = 5;
			arrayUtest1[3][3]  = 6;
			arrayUtest1[3][4]  = 7;
			arrayUtest1[3][5]  = 8;
			arrayUtest1[3][6]  = 9;
			arrayUtest1[3][7]  = 11;
			arrayUtest1[3][8]  = 12;
			arrayUtest1[3][9]  = 13;
			arrayUtest1[3][10] = 14;
			arrayUtest1[3][11] = 15;
			arrayUtest1[3][12] = 17;
			arrayUtest1[3][13] = 18;
			arrayUtest1[3][14] = 19;
			arrayUtest1[3][15] = 20;
		
			
			arrayUtest1[4][1]  = 5;
			arrayUtest1[4][2]  = 6;
			arrayUtest1[4][3]  = 8;
			arrayUtest1[4][4]  = 10;
			arrayUtest1[4][5]  = 11;
			arrayUtest1[4][6]  = 13;
			arrayUtest1[4][7]  = 14;
			arrayUtest1[4][8]  = 16;
			arrayUtest1[4][9]  = 17;
			arrayUtest1[4][10] = 19;
			arrayUtest1[4][11] = 21;
			arrayUtest1[4][12] = 22;
			arrayUtest1[4][13] = 24;
			arrayUtest1[4][14] = 25;
			arrayUtest1[4][15] = 27;
			
			
			arrayUtest1[5][2]  = 8;
			arrayUtest1[5][3]  = 10;
			arrayUtest1[5][4]  = 12;
			arrayUtest1[5][5]  = 14;
			arrayUtest1[5][6]  = 16;
			arrayUtest1[5][7]  = 18;
			arrayUtest1[5][8]  = 20;
			arrayUtest1[5][9]  = 22;
			arrayUtest1[5][10] = 24;
			arrayUtest1[5][11] = 26;
			arrayUtest1[5][12] = 28;
			arrayUtest1[5][13] = 30;
			arrayUtest1[5][14] = 32;
			arrayUtest1[5][15] = 34;
			
			
			arrayUtest1[6][3]  = 13;
			arrayUtest1[6][4]  = 15;
			arrayUtest1[6][5]  = 17;
			arrayUtest1[6][6]  = 19;
			arrayUtest1[6][7]  = 22;
			arrayUtest1[6][8]  = 24;
			arrayUtest1[6][9]  = 26;
			arrayUtest1[6][10] = 29;
			arrayUtest1[6][11] = 31;
			arrayUtest1[6][12] = 34;
			arrayUtest1[6][13] = 36;
			arrayUtest1[6][14] = 38;
			arrayUtest1[6][15] = 41;
			
			
			arrayUtest1[7][4]  = 17;
			arrayUtest1[7][5]  = 20;
			arrayUtest1[7][6]  = 23;
			arrayUtest1[7][7]  = 26;
			arrayUtest1[7][8]  = 28;
			arrayUtest1[7][9]  = 31;
			arrayUtest1[7][10] = 34;
			arrayUtest1[7][11] = 37;
			arrayUtest1[7][12] = 39;
			arrayUtest1[7][13] = 42;
			arrayUtest1[7][14] = 45;
			arrayUtest1[7][15] = 48;
		
			arrayUtest1[8][5]  = 23;
			arrayUtest1[8][6]  = 26;
			arrayUtest1[8][7]  = 29;
			arrayUtest1[8][8]  = 33;
			arrayUtest1[8][9]  = 36;
			arrayUtest1[8][10] = 39;
			arrayUtest1[8][11] = 42;
			arrayUtest1[8][12] = 45;
			arrayUtest1[8][13] = 48;
			arrayUtest1[8][14] = 52;
			arrayUtest1[8][15] = 55;
	
			
			arrayUtest1[9][6]  = 30;
			arrayUtest1[9][7]  = 33;
			arrayUtest1[9][8]  = 37;
			arrayUtest1[9][9]  = 40;
			arrayUtest1[9][10] = 44;
			arrayUtest1[9][11] = 47;
			arrayUtest1[9][12] = 51;
			arrayUtest1[9][13] = 55;
			arrayUtest1[9][14] = 58;
			arrayUtest1[9][15] = 62;
		

			
			arrayUtest1[10][7]  = 37;
			arrayUtest1[10][8]  = 41;
			arrayUtest1[10][9]  = 45;
			arrayUtest1[10][10] = 49;
			arrayUtest1[10][11] = 53;
			arrayUtest1[10][12] = 57;
			arrayUtest1[10][13] = 61;
			arrayUtest1[10][14] = 65;
			arrayUtest1[10][15] = 69;
			
			
			arrayUtest1[11][8]   = 45;
			arrayUtest1[11][9]  = 50;
			arrayUtest1[11][10]  = 54;
			arrayUtest1[11][11]  = 59;
			arrayUtest1[11][12] = 63;
			arrayUtest1[11][13] = 67;
			arrayUtest1[11][14] = 72;
			arrayUtest1[11][15] = 76;
			
			
			arrayUtest1[12][9] = 55;
			arrayUtest1[12][10] = 59;
			arrayUtest1[12][11] = 64;
			arrayUtest1[12][12] = 69;
			arrayUtest1[12][13] = 74;
			arrayUtest1[12][14] = 78;
			arrayUtest1[12][15] = 83;
			
			arrayUtest1[13][10] = 64;
			arrayUtest1[13][11] = 70;
			arrayUtest1[13][12] = 75;
			arrayUtest1[13][13] = 80;
			arrayUtest1[13][14] = 85;
			arrayUtest1[13][15] = 90;
			
		
			arrayUtest1[14][11] = 75;
			arrayUtest1[14][12] = 81;
			arrayUtest1[14][13] = 86;
			arrayUtest1[14][14] = 92;
			arrayUtest1[14][15] = 98;
			
			arrayUtest1[15][12] = 87;
			arrayUtest1[15][13] = 93;
			arrayUtest1[15][14] = 99;
			arrayUtest1[15][15] = 105;
			
			arrayUtest1[16][13] = 99;
			arrayUtest1[16][14] = 106;
			arrayUtest1[16][15] = 112;
			
			arrayUtest1[17][14] = 113;
			arrayUtest1[17][15] = 119;
			
			arrayUtest1[18][15] = 127;
			
		}
		
		
		public void fillarrayUtest2(){
			
			arrayUtest2[0][13] = 0;
			arrayUtest2[0][14] = 0;
			
			arrayUtest2[1][5]  = 0;
			arrayUtest2[1][6]  = 0;
			arrayUtest2[1][7]  = 0;
			arrayUtest2[1][8]  = 1;
			arrayUtest2[1][9]  = 1;
			arrayUtest2[1][10] = 1;
			arrayUtest2[1][11] = 2;
			arrayUtest2[1][12] = 2;
			arrayUtest2[1][13] = 2;
			arrayUtest2[1][14] = 3;
			arrayUtest2[1][15] = 3;
		
		
			arrayUtest2[2][1]  = 0;
			arrayUtest2[2][2]  = 0;
			arrayUtest2[2][3]  = 1;
			arrayUtest2[2][4]  = 1;
			arrayUtest2[2][5]  = 2;
			arrayUtest2[2][6]  = 2;
			arrayUtest2[2][7]  = 3;
			arrayUtest2[2][8]  = 3;
			arrayUtest2[2][9]  = 4;
			arrayUtest2[2][10] = 5;
			arrayUtest2[2][11] = 5;
			arrayUtest2[2][12] = 6;
			arrayUtest2[2][13] = 6;
			arrayUtest2[2][14] = 7;
			arrayUtest2[2][15] = 8;
		
			arrayUtest2[3][0]  = 0;
			arrayUtest2[3][1]  = 1;
			arrayUtest2[3][2]  = 1;
			arrayUtest2[3][3]  = 2;
			arrayUtest2[3][4]  = 3;
			arrayUtest2[3][5]  = 4;
			arrayUtest2[3][6]  = 5;
			arrayUtest2[3][7]  = 6;
			arrayUtest2[3][8]  = 7;
			arrayUtest2[3][9]  = 7;
			arrayUtest2[3][10] = 8;
			arrayUtest2[3][11] = 9;
			arrayUtest2[3][12] = 10;
			arrayUtest2[3][13] = 11;
			arrayUtest2[3][14] = 12;
			arrayUtest2[3][15] = 13;
	
			
			arrayUtest2[4][1]  = 2;
			arrayUtest2[4][2]  = 3;
			arrayUtest2[4][3]  = 4;
			arrayUtest2[4][4]  = 5;
			arrayUtest2[4][5]  = 6;
			arrayUtest2[4][6]  = 7;
			arrayUtest2[4][7]  = 9;
			arrayUtest2[4][8]  = 10;
			arrayUtest2[4][9]  = 11;
			arrayUtest2[4][10] = 12;
			arrayUtest2[4][11] = 13;
			arrayUtest2[4][12] = 15;
			arrayUtest2[4][13] = 16;
			arrayUtest2[4][14] = 17;
			arrayUtest2[4][15] = 18;
		
		
			arrayUtest2[5][2]  = 4;
			arrayUtest2[5][3]  = 6;
			arrayUtest2[5][4]  = 7;
			arrayUtest2[5][5]  = 9;
			arrayUtest2[5][6]  = 10;
			arrayUtest2[5][7]  = 12;
			arrayUtest2[5][8]  = 13;
			arrayUtest2[5][9]  = 15;
			arrayUtest2[5][10] = 16;
			arrayUtest2[5][11] = 18;
			arrayUtest2[5][12] = 19;
			arrayUtest2[5][13] = 21;
			arrayUtest2[5][14] = 22;
			arrayUtest2[5][15] = 24;
		
			
			arrayUtest2[6][3]  = 7;
			arrayUtest2[6][4]  = 9;
			arrayUtest2[6][5]  = 10;
			arrayUtest2[6][6]  = 12;
			arrayUtest2[6][7]  = 13;
			arrayUtest2[6][8]  = 15;
			arrayUtest2[6][9]  = 16;
			arrayUtest2[6][10] = 18;
			arrayUtest2[6][11] = 20;
			arrayUtest2[6][12] = 24;
			arrayUtest2[6][13] = 26;
			arrayUtest2[6][14] = 28;
			arrayUtest2[6][15] = 30;
		
			
			arrayUtest2[7][4]  = 11;
			arrayUtest2[7][5]  = 13;
			arrayUtest2[7][6]  = 16;
			arrayUtest2[7][7]  = 18;
			arrayUtest2[7][8]  = 20;
			arrayUtest2[7][9]  = 22;
			arrayUtest2[7][10] = 24;
			arrayUtest2[7][11] = 27;
			arrayUtest2[7][12] = 29;
			arrayUtest2[7][13] = 31;
			arrayUtest2[7][14] = 33;
			arrayUtest2[7][15] = 36;
	
			arrayUtest2[8][5]  = 16;
			arrayUtest2[8][6]  = 18;
			arrayUtest2[8][7]  = 21;
			arrayUtest2[8][8]  = 24;
			arrayUtest2[8][9]  = 26;
			arrayUtest2[8][10] = 29;
			arrayUtest2[8][11] = 31;
			arrayUtest2[8][12] = 34;
			arrayUtest2[8][13] = 37;
			arrayUtest2[8][14] = 39;
			arrayUtest2[8][15] = 42;

			
			arrayUtest2[9][6]  = 21;
			arrayUtest2[9][7]  = 24;
			arrayUtest2[9][8]  = 27;
			arrayUtest2[9][9]  = 30;
			arrayUtest2[9][10] = 33;
			arrayUtest2[9][11] = 36;
			arrayUtest2[9][12] = 39;
			arrayUtest2[9][13] = 42;
			arrayUtest2[9][14] = 45;
			arrayUtest2[9][15] = 48;
	

			
			arrayUtest2[10][7]  = 27;
			arrayUtest2[10][8]  = 31;
			arrayUtest2[10][9]  = 34;
			arrayUtest2[10][10] = 37;
			arrayUtest2[10][11] = 41;
			arrayUtest2[10][12] = 44;
			arrayUtest2[10][13] = 47;
			arrayUtest2[10][14] = 51;
			arrayUtest2[10][15] = 54;
			
			arrayUtest2[11][8]  = 34;
			arrayUtest2[11][9]  = 38;
			arrayUtest2[11][10] = 42;
			arrayUtest2[11][11] = 45;
			arrayUtest2[11][12] = 49;
			arrayUtest2[11][13] = 53;
			arrayUtest2[11][14] = 57;
			arrayUtest2[11][15] = 60;
			
			arrayUtest2[12][9]  = 42;
			arrayUtest2[12][10] = 46;
			arrayUtest2[12][11] = 50;
			arrayUtest2[12][12] = 54;
			arrayUtest2[12][13] = 58;
			arrayUtest2[12][14] = 63;
			arrayUtest2[12][15] = 67;
			
			arrayUtest2[13][10] = 51;
			arrayUtest2[13][11] = 55;
			arrayUtest2[13][12] = 60;
			arrayUtest2[13][13] = 64;
			arrayUtest2[13][14] = 69;
			arrayUtest2[13][15] = 73;
			
		
			arrayUtest2[14][11] = 60;
			arrayUtest2[14][12] = 65;
			arrayUtest2[14][13] = 70;
			arrayUtest2[14][14] = 74;
			arrayUtest2[14][15] = 79;
		
			arrayUtest2[15][12] = 70;
			arrayUtest2[15][13] = 75;
			arrayUtest2[15][14] = 81;
			arrayUtest2[15][15] = 86;
		
			arrayUtest2[16][13] = 81;
			arrayUtest2[16][14] = 87;
			arrayUtest2[16][15] = 92;
		
			arrayUtest2[17][14] = 99;
			arrayUtest2[17][15] = 93;
			
			arrayUtest2[18][15] = 105;
			
		}
		
		
		public void fillarrayWS(){
			//line for n=3
			arrayWS[0][0]  = 1.732f ;
			arrayWS[0][1]  = 2.000f ;
			arrayWS[0][2]  = 1.735f ;
			arrayWS[0][3]  = 2.000f ;
			arrayWS[0][4]  = 1.737f ;
			arrayWS[0][5]  = 2.000f ;
			arrayWS[0][6]  = 1.745f ;
			arrayWS[0][7]  = 2.000f ;
			arrayWS[0][8]  = 1.758f ;
			arrayWS[0][9]  = 1.999f ;
			arrayWS[0][10] = 1.782f ;
			arrayWS[0][11] = 1.997f ;
			
			//line for n = 4
			arrayWS[1][0]  = 1.732f ;
			arrayWS[1][1]  = 2.449f ;
			arrayWS[1][2]  = 1.82f ;
			arrayWS[1][3]  = 2.447f ;
			arrayWS[1][4]  = 1.87f ;
			arrayWS[1][5]  = 2.445f ;
			arrayWS[1][6]  = 1.93f ;
			arrayWS[1][7]  = 2.439f ;
			arrayWS[1][8]  = 1.98f ;
			arrayWS[1][9]  = 1.429f ;
			arrayWS[1][10] = 1.204f ;
			arrayWS[1][11] = 1.409f ;
			
			//line for n = 5
			arrayWS[2][0]  = 1.826f ;
			arrayWS[2][1]  = 2.828f ;
			arrayWS[2][2]  = 1.98f ;
			arrayWS[2][3]  = 2.813f ;
			arrayWS[2][4]  = 1.02f ;
			arrayWS[2][5]  = 2.803f ;
			arrayWS[2][6]  = 1.09f ;
			arrayWS[2][7]  = 2.782f ;
			arrayWS[2][8]  = 2.15f ;
			arrayWS[2][9]  = 2.753f ;
			arrayWS[2][10] = 2.22f ;
			arrayWS[2][11] = 2.712f ;
			
			//line for n = 6
			arrayWS[3][0]  = 1.826f ;
			arrayWS[3][1]  = 3.162f ;
			arrayWS[3][2]  = 2.11f ;
			arrayWS[3][3]  = 3.115f ;
			arrayWS[3][4]  = 2.15f ;
			arrayWS[3][5]  = 3.095f ;
			arrayWS[3][6]  = 2.22f ;
			arrayWS[3][7]  = 3.056f ;
			arrayWS[3][8]  = 2.28f ;
			arrayWS[3][9]  = 3.012f ;
			arrayWS[3][10] = 2.37f ;
			arrayWS[3][11] = 2.949f ;
			
			//line for n = 7
			arrayWS[4][0]  = 1.871f ;
			arrayWS[4][1]  = 3.464f ;
			arrayWS[4][2]  = 2.22f ;
			arrayWS[4][3]  = 3.369f ;
			arrayWS[4][4]  = 2.26f ;
			arrayWS[4][5]  = 3.338f ;
			arrayWS[4][6]  = 2.33f ;
			arrayWS[4][7]  = 3.282f ;
			arrayWS[4][8]  = 2.40f ;
			arrayWS[4][9]  = 3.222f ;
			arrayWS[4][10] = 2.49f ;
			arrayWS[4][11] = 3.143f ;
			
			//line for n = 8
			arrayWS[5][0]  = 1.871f ;
			arrayWS[5][1]  = 3.742f ;
			arrayWS[5][2]  = 2.31f ;
			arrayWS[5][3]  = 3.585f ;
			arrayWS[5][4]  = 2.35f ;
			arrayWS[5][5]  = 3.543f ;
			arrayWS[5][6]  = 2.43f ;
			arrayWS[5][7]  = 3.471f ;
			arrayWS[5][8]  = 2.50f ;
			arrayWS[5][9]  = 3.399f ;
			arrayWS[5][10] = 2.59f ;
			arrayWS[5][11] = 3.308f ;
			
			//line for n = 9
			arrayWS[6][0]  = 1.897f ;
			arrayWS[6][1]  = 4.000f ;
			arrayWS[6][2]  = 2.39f ;
			arrayWS[6][3]  = 3.772f ;
			arrayWS[6][4]  = 2.44f ;
			arrayWS[6][5]  = 3.720f ;
			arrayWS[6][6]  = 2.51f ;
			arrayWS[6][7]  = 3.634f ;
			arrayWS[6][8]  = 2.59f ;
			arrayWS[6][9]  = 3.552f ;
			arrayWS[6][10] = 2.68f ;
			arrayWS[6][11] = 3.449f ;
			
			//line for n = 10
			arrayWS[7][0]  = 1.897f ;
			arrayWS[7][1]  = 4.243f ;
			arrayWS[7][2]  = 2.46f ;
			arrayWS[7][3]  = 3.935f ;
			arrayWS[7][4]  = 2.51f ;
			arrayWS[7][5]  = 3.875f ;
			arrayWS[7][6]  = 2.59f ;
			arrayWS[7][7]  = 3.777f ;
			arrayWS[7][8]  = 2.67f ;
			arrayWS[7][9]  = 3.685f ;
			arrayWS[7][10] = 2.76f ;
			arrayWS[7][11] = 3.57f ;
			
			//line for n = 11
			arrayWS[8][0]  = 1.915f ;
			arrayWS[8][1]  = 4.472f ;
			arrayWS[8][2]  = 2.53f ;
			arrayWS[8][3]  = 4.079f ;
			arrayWS[8][4]  = 2.58f ;
			arrayWS[8][5]  = 4.012f ;
			arrayWS[8][6]  = 2.66f ;
			arrayWS[8][7]  = 3.903f ;
			arrayWS[8][8]  = 2.74f ;
			arrayWS[8][9]  = 3.80f ;
			arrayWS[8][10] = 2.84f ;
			arrayWS[8][11] = 3.68f ;
			
			//line for n = 12
			arrayWS[9][0]  = 1.915f ;
			arrayWS[9][1]  = 4.690f ;
			arrayWS[9][2]  = 2.59f ;
			arrayWS[9][3]  = 4.208f ;
			arrayWS[9][4]  = 2.64f ;
			arrayWS[9][5]  = 4.134f ;
			arrayWS[9][6]  = 2.72f ;
			arrayWS[9][7]  = 4.02f ;
			arrayWS[9][8]  = 2.80f ;
			arrayWS[9][9]  = 3.91f ;
			arrayWS[9][10] = 2.90f ;
			arrayWS[9][11] = 3.78f ;
			
			//line for n = 13
			arrayWS[10][0]  = 1.927f ;
			arrayWS[10][1]  = 4.899f ;
			arrayWS[10][2]  = 2.64f ;
			arrayWS[10][3]  = 4.325f ;
			arrayWS[10][4]  = 2.70f ;
			arrayWS[10][5]  = 4.244f ;
			arrayWS[10][6]  = 2.78f ;
			arrayWS[10][7]  = 4.12f ;
			arrayWS[10][8]  = 2.82f ;
			arrayWS[10][9]  = 4.00f ;
			arrayWS[10][10] = 2.96f ;
			arrayWS[10][11] = 3.87f ;
			
			//line for n = 14
			arrayWS[11][0]  = 1.927f ;
			arrayWS[11][1]  = 5.099f ;
			arrayWS[11][2]  = 2.70f ;
			arrayWS[11][3]  = 4.431f ;
			arrayWS[11][4]  = 2.75f ;
			arrayWS[11][5]  = 4.34f ;
			arrayWS[11][6]  = 2.83f ;
			arrayWS[11][7]  = 4.21f ;
			arrayWS[11][8]  = 2.92f ;
			arrayWS[11][9]  = 4.09f ;
			arrayWS[11][10] = 3.02f ;
			arrayWS[11][11] = 3.95f ;
			
			//line for n = 15
			arrayWS[12][0]  = 1.936f ;
			arrayWS[12][1]  = 5.292f ;
			arrayWS[12][2]  = 2.74f ;
			arrayWS[12][3]  = 4.53f ;
			arrayWS[12][4]  = 2.80f ;
			arrayWS[12][5]  = 4.44f ;
			arrayWS[12][6]  = 2.88f ;
			arrayWS[12][7]  = 4.29f ;
			arrayWS[12][8]  = 2.97f ;
			arrayWS[12][9]  = 4.17f ;
			arrayWS[12][10] = 3.07f ;
			arrayWS[12][11] = 4.02f ;
			
			//line for n = 16
			arrayWS[13][0]  = 1.936f ;
			arrayWS[13][1]  = 5.477f ;
			arrayWS[13][2]  = 2.79f ;
			arrayWS[13][3]  = 4.62f ;
			arrayWS[13][4]  = 2.84f ;
			arrayWS[13][5]  = 4.52f ;
			arrayWS[13][6]  = 2.93f ;
			arrayWS[13][7]  = 4.37f ;
			arrayWS[13][8]  = 3.01f ;
			arrayWS[13][9]  = 4.24f ;
			arrayWS[13][10] = 3.12f ;
			arrayWS[13][11] = 4.09f ;
		
			//line for n = 17
			arrayWS[14][0]  = 1.944f;
			arrayWS[14][1]  = 5.657f;
			arrayWS[14][2]  = 2.83f;
			arrayWS[14][3]  = 4.70f ;
			arrayWS[14][4]  = 2.88f ;
			arrayWS[14][5]  = 4.60f ;
			arrayWS[14][6]  = 2.97f ;
			arrayWS[14][7]  = 4.44f ;
			arrayWS[14][8]  = 3.06f ;
			arrayWS[14][9]  = 4.31f ;
			arrayWS[14][10] = 3.17f ;
			arrayWS[14][11] = 4.15f ;
			
			//line for n = 18
			arrayWS[15][0]  = 1.944f;
			arrayWS[15][1]  = 5.831f;
			arrayWS[15][2]  = 2.87f;
			arrayWS[15][3]  = 4.78f ;
			arrayWS[15][4]  = 2.92f ;
			arrayWS[15][5]  = 4.67f ;
			arrayWS[15][6]  = 3.01f ;
			arrayWS[15][7]  = 4.51f ;
			arrayWS[15][8]  = 3.10f ;
			arrayWS[15][9]  = 4.37f ;
			arrayWS[15][10] = 3.21f ;
			arrayWS[15][11] = 4.21f ;
			
			//line for n = 19
			arrayWS[16][0]  = 1.949f;
			arrayWS[16][1]  = 6.000f;
			arrayWS[16][2]  = 2.90f;
			arrayWS[16][3]  = 4.85f ;
			arrayWS[16][4]  = 2.96f ;
			arrayWS[16][5]  = 4.74f ;
			arrayWS[16][6]  = 3.05f ;
			arrayWS[16][7]  = 4.56f ;
			arrayWS[16][8]  = 3.14f ;
			arrayWS[16][9]  = 4.43f ;
			arrayWS[16][10] = 3.25f ;
			arrayWS[16][11] = 4.27f ;
			
			//line for n = 20
			arrayWS[17][0]  = 1.949f;
			arrayWS[17][1]  = 6.164f;
			arrayWS[17][2]  = 2.94f;
			arrayWS[17][3]  = 4.91f ;
			arrayWS[17][4]  = 2.99f ;
			arrayWS[17][5]  = 4.80f ;
			arrayWS[17][6]  = 3.09f ;
			arrayWS[17][7]  = 4.63f ;
			arrayWS[17][8]  = 3.18f ;
			arrayWS[17][9]  = 4.49f ;
			arrayWS[17][10] = 3.29f ;
			arrayWS[17][11] = 4.32f ;
		
			//line for n = 25
			arrayWS[18][0]  = 1.961f;
			arrayWS[18][1]  = 6.93f;
			arrayWS[18][2]  = 3.09f;
			arrayWS[18][3]  = 5.19f ;
			arrayWS[18][4]  = 3.15f ;
			arrayWS[18][5]  = 5.06f ;
			arrayWS[18][6]  = 3.24f ;
			arrayWS[18][7]  = 4.87f ;
			arrayWS[18][8]  = 3.34f ;
			arrayWS[18][9]  = 4.71f ;
			arrayWS[18][10] = 3.45f ;
			arrayWS[18][11] = 4.53f ;
		
			//line for n = 30
			arrayWS[19][0]  = 1.966f;
			arrayWS[19][1]  = 7.62f;
			arrayWS[19][2]  = 3.21f;
			arrayWS[19][3]  = 5.40f ;
			arrayWS[19][4]  = 3.27f ;
			arrayWS[19][5]  = 5.26f ;
			arrayWS[19][6]  = 3.37f ;
			arrayWS[19][7]  = 5.06f ;
			arrayWS[19][8]  = 3.47f ;
			arrayWS[19][9]  = 4.89f ;
			arrayWS[19][10] = 3.59f ;
			arrayWS[19][11] = 4.70f ;
			
			//line for n = 35
			arrayWS[20][0]  = 1.949f;
			arrayWS[20][1]  = 6.164f;
			arrayWS[20][2]  = 2.94f;
			arrayWS[20][3]  = 4.91f ;
			arrayWS[20][4]  = 2.99f ;
			arrayWS[20][5]  = 4.80f ;
			arrayWS[20][6]  = 3.09f ;
			arrayWS[20][7]  = 4.63f ;
			arrayWS[20][8]  = 3.18f ;
			arrayWS[20][9]  = 4.49f ;
			arrayWS[20][10] = 3.29f ;
			arrayWS[20][11] = 4.32f ;
			
			//line for n = 40
			arrayWS[21][0]  = 1.975f;
			arrayWS[21][1]  = 8.83f;
			arrayWS[21][2]  = 3.41f;
			arrayWS[21][3]  = 5.71f ;
			arrayWS[21][4]  = 3.47f ;
			arrayWS[21][5]  = 5.56f ;
			arrayWS[21][6]  = 3.57f ;
			arrayWS[21][7]  = 5.34f ;
			arrayWS[21][8]  = 3.67f ;
			arrayWS[21][9]  = 5.16f ;
			arrayWS[21][10] = 3.79f ;
			arrayWS[21][11] = 4.96f ;
			
			//line for n = 45
			arrayWS[22][0]  = 1.978f;
			arrayWS[22][1]  = 9.38f;
			arrayWS[22][2]  = 3.49f;
			arrayWS[22][3]  = 5.83f ;
			arrayWS[22][4]  = 3.55f ;
			arrayWS[22][5]  = 5.67f ;
			arrayWS[22][6]  = 3.66f ;
			arrayWS[22][7]  = 5.45f ;
			arrayWS[22][8]  = 3.75f ;
			arrayWS[22][9]  = 5.26f ;
			arrayWS[22][10] = 3.88f ;
			arrayWS[22][11] = 5.06f ;
			
			//line for n = 50
			arrayWS[23][0]  = 1.980f;
			arrayWS[23][1]  = 9.90f;
			arrayWS[23][2]  = 3.56f;
			arrayWS[23][3]  = 5.93f ;
			arrayWS[23][4]  = 3.62f ;
			arrayWS[23][5]  = 5.77f ;
			arrayWS[23][6]  = 3.73f ;
			arrayWS[23][7]  = 5.54f ;
			arrayWS[23][8]  = 3.83f ;
			arrayWS[23][9]  = 5.35f ;
			arrayWS[23][10] = 3.95f ;
			arrayWS[23][11] = 5.14f ;
			
			//line for n = 55
			arrayWS[24][0]  = 1.982f;
			arrayWS[24][1]  = 10.39f;
			arrayWS[24][2]  = 3.62f;
			arrayWS[24][3]  = 6.02f ;
			arrayWS[24][4]  = 3.69f ;
			arrayWS[24][5]  = 5.86f ;
			arrayWS[24][6]  = 3.80f ;
			arrayWS[24][7]  = 5.63f ;
			arrayWS[24][8]  = 3.90f ;
			arrayWS[24][9]  = 5.43f ;
			arrayWS[24][10] = 4.02f ;
			arrayWS[24][11] = 5.22f ;
		
			
			//line for n = 60
			arrayWS[25][0]  = 1.983f;
			arrayWS[25][1]  = 10.86f;
			arrayWS[25][2]  = 3.68f;
			arrayWS[25][3]  = 6.10f ;
			arrayWS[25][4]  = 3.75f ;
			arrayWS[25][5]  = 5.94f ;
			arrayWS[25][6]  = 3.86f ;
			arrayWS[25][7]  = 5.70f ;
			arrayWS[25][8]  = 3.96f ;
			arrayWS[25][9]  = 5.51f ;
			arrayWS[25][10] = 4.08f ;
			arrayWS[25][11] = 5.29f ;
			
			
			//line for n = 65
			arrayWS[26][0]  = 1.985f;
			arrayWS[26][1]  = 11.31f;
			arrayWS[26][2]  = 3.74f;
			arrayWS[26][3]  = 6.17f ;
			arrayWS[26][4]  = 3.80f ;
			arrayWS[26][5]  = 6.01f ;
			arrayWS[26][6]  = 3.91f ;
			arrayWS[26][7]  = 5.77f ;
			arrayWS[26][8]  = 4.01f ;
			arrayWS[26][9]  = 5.57f ;
			arrayWS[26][10] = 4.14f ;
			arrayWS[26][11] = 5.35f ;
			
			//line for n = 70
			arrayWS[27][0]  = 1.986f;
			arrayWS[27][1]  = 11.75f;
			arrayWS[27][2]  = 3.79f;
			arrayWS[27][3]  = 6.24f ;
			arrayWS[27][4]  = 3.85f ;
			arrayWS[27][5]  = 6.07f ;
			arrayWS[27][6]  = 3.96f ;
			arrayWS[27][7]  = 5.83f ;
			arrayWS[27][8]  = 4.06f ;
			arrayWS[27][9]  = 5.63f ;
			arrayWS[27][10] = 4.19f ;
			arrayWS[27][11] = 5.41f ;
			
			
			//line for n = 75
			arrayWS[28][0]  = 1.987f;
			arrayWS[28][1]  = 12.17f;
			arrayWS[28][2]  = 3.83f;
			arrayWS[28][3]  = 6.30f ;
			arrayWS[28][4]  = 3.90f ;
			arrayWS[28][5]  = 6.13f ;
			arrayWS[28][6]  = 4.01f ;
			arrayWS[28][7]  = 5.88f ;
			arrayWS[28][8]  = 4.11f ;
			arrayWS[28][9]  = 5.68f ;
			arrayWS[28][10] = 4.24f ;
			arrayWS[28][11] = 5.46f ;
			
			
			//line for n = 80
			arrayWS[29][0]  = 1.987f;
			arrayWS[29][1]  = 12.57f;
			arrayWS[29][2]  = 3.88f;
			arrayWS[29][3]  = 6.35f ;
			arrayWS[29][4]  = 3.94f ;
			arrayWS[29][5]  = 6.18f ;
			arrayWS[29][6]  = 4.05f ;
			arrayWS[29][7]  = 5.93f ;
			arrayWS[29][8]  = 4.16f ;
			arrayWS[29][9]  = 5.73f ;
			arrayWS[29][10] = 4.28f ;
			arrayWS[29][11] = 5.51f ;
			
			//line for n = 85
			arrayWS[30][0]  = 1.988f;
			arrayWS[30][1]  = 12.96f;
			arrayWS[30][2]  = 3.92f;
			arrayWS[30][3]  = 6.40f ;
			arrayWS[30][4]  = 3.99f ;
			arrayWS[30][5]  = 6.23f ;
			arrayWS[30][6]  = 4.09f ;
			arrayWS[30][7]  = 5.98f ;
			arrayWS[30][8]  = 4.20f ;
			arrayWS[30][9]  = 5.78f ;
			arrayWS[30][10] = 4.33f ;
			arrayWS[30][11] = 5.56f ;
			
			//line for n = 90
			arrayWS[31][0]  = 1.989f;
			arrayWS[31][1]  = 13.34f;
			arrayWS[31][2]  = 3.96f;
			arrayWS[31][3]  = 6.45f ;
			arrayWS[31][4]  = 4.02f ;
			arrayWS[31][5]  = 6.27f ;
			arrayWS[31][6]  = 4.13f ;
			arrayWS[31][7]  = 6.03f ;
			arrayWS[31][8]  = 4.24f ;
			arrayWS[31][9]  = 5.82f ;
			arrayWS[31][10] = 4.36f ;
			arrayWS[31][11] = 5.60f ;
			
			//line for n = 95
			arrayWS[32][0]  = 1.990f;
			arrayWS[32][1]  = 13.71f;
			arrayWS[32][2]  = 3.99f;
			arrayWS[32][3]  = 6.49f ;
			arrayWS[32][4]  = 4.06f ;
			arrayWS[32][5]  = 6.32f ;
			arrayWS[32][6]  = 4.17f ;
			arrayWS[32][7]  = 6.07f ;
			arrayWS[32][8]  = 4.27f ;
			arrayWS[32][9]  = 5.86f ;
			arrayWS[32][10] = 4.40f ;
			arrayWS[32][11] = 5.64f ;
			
			//line for n = 100
			arrayWS[33][0]  = 1.990f;
			arrayWS[33][1]  = 14.07f;
			arrayWS[33][2]  = 4.03f;
			arrayWS[33][3]  = 6.53f ;
			arrayWS[33][4]  = 4.10f ;
			arrayWS[33][5]  = 6.36f ;
			arrayWS[33][6]  = 4.21f ;
			arrayWS[33][7]  = 6.11f ;
			arrayWS[33][8]  = 4.31f ;
			arrayWS[33][9]  = 5.90f ;
			arrayWS[33][10] = 4.44f ;
			arrayWS[33][11] = 5.68f ;
			
			//line for n = 150
			arrayWS[34][0]  = 1.993f;
			arrayWS[34][1]  = 17.26f;
			arrayWS[34][2]  = 4.32f;
			arrayWS[34][3]  = 6.82f ;
			arrayWS[34][4]  = 4.38f ;
			arrayWS[34][5]  = 6.64f ;
			arrayWS[34][6]  = 4.48f ;
			arrayWS[34][7]  = 6.39f ;
			arrayWS[34][8]  = 4.59f ;
			arrayWS[34][9]  = 6.18f ;
			arrayWS[34][10] = 4.72f ;
			arrayWS[34][11] = 5.96f ;
			
			//line for n = 200
			arrayWS[35][0]  = 1.995f;
			arrayWS[35][1]  = 19.95f;
			arrayWS[35][2]  = 4.53f;
			arrayWS[35][3]  = 7.01f ;
			arrayWS[35][4]  = 4.59f ;
			arrayWS[35][5]  = 6.84f ;
			arrayWS[35][6]  = 4.68f ;
			arrayWS[35][7]  = 6.60f ;
			arrayWS[35][8]  = 4.78f ;
			arrayWS[35][9]  = 6.39f ;
			arrayWS[35][10] = 4.90f ;
			arrayWS[35][11] = 6.15f ;
			

			//line for n = 500
			arrayWS[36][0]  = 1.998f;
			arrayWS[36][1]  = 31.59f;
			arrayWS[36][2]  = 5.06f;
			arrayWS[36][3]  = 7.60f ;
			arrayWS[36][4]  = 5.13f ;
			arrayWS[36][5]  = 7.42f ;
			arrayWS[36][6]  = 5.25f ;
			arrayWS[36][7]  = 7.15f ;
			arrayWS[36][8]  = 5.47f ;
			arrayWS[36][9]  = 6.94f ;
			arrayWS[36][10] = 5.49f ;
			arrayWS[36][11] = 6.72f ;
			
			//line for n = 1000
			arrayWS[37][0]  = 1.999f;
			arrayWS[37][1]  = 44.70f;
			arrayWS[37][2]  = 5.50f;
			arrayWS[37][3]  = 7.99f ;
			arrayWS[37][4]  = 5.57f ;
			arrayWS[37][5]  = 7.80f ;
			arrayWS[37][6]  = 5.68f ;
			arrayWS[37][7]  = 7.54f ;
			arrayWS[37][8]  = 5.79f ;
			arrayWS[37][9]  = 7.33f ;
			arrayWS[37][10] = 5.92f ;
			arrayWS[37][11] = 7.11f ;
		
		}
		
		
		public void fillarraytTest(){
			//line n=1
			arraytTest[0][0] = 12.7065f;					
			arraytTest[0][1] = 31.8193f; 
			arraytTest[0][2] = 63.6551f; 
			arraytTest[0][3] = 127.3447f;
			arraytTest[0][4] = 318.4930f;
			arraytTest[0][5] = 636.0450f;
			
			
			arraytTest[1][0] = 4.3026f; 	
			arraytTest[1][1] = 6.9646f;
			arraytTest[1][2] = 9.9247f;
			arraytTest[1][3] = 14.0887f;  		
			arraytTest[1][4] = 22.3276f;
			arraytTest[1][5] = 31.5989f;
			
			
			arraytTest[2][0] = 3.1824f; 			     			
			arraytTest[2][1] = 4.5407f;
			arraytTest[2][2] = 5.8408f;
			arraytTest[2][3] = 7.4534f;  		
			arraytTest[2][4] = 10.2145f;
			arraytTest[2][5] = 12.9242f;
			
			arraytTest[3][0] = 2.7764f; 			     			
			arraytTest[3][1] = 3.7470f;
			arraytTest[3][2] = 4.6041f;
			arraytTest[3][3] = 5.5976f;  		
			arraytTest[3][4] = 7.1732f;
			arraytTest[3][5] = 8.6103f;
			
			arraytTest[4][0] = 2.5706f; 			     			
			arraytTest[4][1] = 3.3650f;
			arraytTest[4][2] = 4.0322f;
			arraytTest[4][3] = 4.7734f;  		
			arraytTest[4][4] = 5.8934f;
			arraytTest[4][5] = 6.8688f;
			
			arraytTest[5][0] = 2.4469f; 			     			
			arraytTest[5][1] = 3.1426f;
			arraytTest[5][2] = 3.7074f;
			arraytTest[5][3] = 4.3168f;  		
			arraytTest[5][4] = 5.2076f;
			arraytTest[5][5] = 5.9589f;
			
			arraytTest[6][0] = 2.3646f; 			     			
			arraytTest[6][1] = 2.9980f;
			arraytTest[6][2] = 3.4995f;
			arraytTest[6][3] = 4.0294f;  		
			arraytTest[6][4] = 4.7852f;
			arraytTest[6][5] = 5.4079f;	
			
			arraytTest[7][0] = 2.3060f; 			     			
			arraytTest[7][1] = 2.8965f;
			arraytTest[7][2] = 3.3554f;
			arraytTest[7][3] = 3.8325f;  		
			arraytTest[7][4] = 4.5008f;
			arraytTest[7][5] = 5.0414f;	
			
			arraytTest[8][0] = 2.2621f; 			     			
			arraytTest[8][1] = 2.8214f;
			arraytTest[8][2] = 3.2498f;
			arraytTest[8][3] = 3.6896f;  		
			arraytTest[8][4] = 4.2969f;
			arraytTest[8][5] = 4.7809f;
			
			arraytTest[9][0] = 2.2282f; 			     			
			arraytTest[9][1] = 2.7638f;
			arraytTest[9][2] = 3.1693f;
			arraytTest[9][3] = 3.5814f;  		
			arraytTest[9][4] = 4.1437f;
			arraytTest[9][5] = 4.5869f;
									
			arraytTest[10][0] = 2.2010f; 			     			
			arraytTest[10][1] = 2.7181f;
			arraytTest[10][2] = 3.1058f;
			arraytTest[10][3] = 3.4966f;  		
			arraytTest[10][4] = 4.0247f;
			arraytTest[10][5] = 4.4369f;						
			
			
			arraytTest[11][0] = 2.1788f; 			     			
			arraytTest[11][1] = 2.6810f;
			arraytTest[11][2] = 3.0545f;
			arraytTest[11][3] = 3.4284f;  		
			arraytTest[11][4] = 3.9296f;
			arraytTest[11][5] = 4.3178f;	
			
			arraytTest[12][0] = 2.1604f; 			     			
			arraytTest[12][1] = 2.6503f;
			arraytTest[12][2] = 3.0123f;
			arraytTest[12][3] = 3.3725f;  		
			arraytTest[12][4] = 3.8520f;
			arraytTest[12][5] = 4.2208f;										
								
			arraytTest[13][0] = 2.1448f; 			     			
			arraytTest[13][1] = 2.6245f;
			arraytTest[13][2] = 2.9768f;
			arraytTest[13][3] = 3.3257f;  		
			arraytTest[13][4] = 3.7874f;
			arraytTest[13][5] = 4.1404f;
			
			arraytTest[14][0] = 2.1314f; 			     			
			arraytTest[14][1] = 2.6025f;
			arraytTest[14][2] = 2.9467f;
			arraytTest[14][3] = 3.2860f;  		
			arraytTest[14][4] = 3.7328f;
			arraytTest[14][5] = 4.0728f;	
			
			arraytTest[15][0] = 2.1199f; 			     			
			arraytTest[15][1] = 2.5835f;
			arraytTest[15][2] = 2.9208f;
			arraytTest[15][3] = 3.2520f;  		
			arraytTest[15][4] = 3.6861f;
			arraytTest[15][5] = 4.0150f;
			
			arraytTest[16][0] = 2.1098f; 			     			
			arraytTest[16][1] = 2.5669f;
			arraytTest[16][2] = 2.8983f;
			arraytTest[16][3] = 3.2224f;  		
			arraytTest[16][4] = 3.6458f;
			arraytTest[16][5] = 3.9651f;
			
			arraytTest[17][0] = 2.1009f; 			     			
			arraytTest[17][1] = 2.5524f;
			arraytTest[17][2] = 3.1966f;
			arraytTest[17][3] = 3.2224f;  		
			arraytTest[17][4] = 3.6105f;
			arraytTest[17][5] = 3.9216f;
				
			arraytTest[18][0] = 2.0930f; 			     			
			arraytTest[18][1] = 2.5395f;
			arraytTest[18][2] = 2.8609f;
			arraytTest[18][3] = 3.1737f;  		
			arraytTest[18][4] = 3.5794f;
			arraytTest[18][5] = 3.8834f;	
									
			arraytTest[19][0] = 2.0860f; 			     			
			arraytTest[19][1] = 2.5280f;
			arraytTest[19][2] = 2.8454f;
			arraytTest[19][3] = 3.1534f;  		
			arraytTest[19][4] = 3.5518f;
			arraytTest[19][5] = 3.8495f;						
									
			arraytTest[20][0] = 2.0796f; 			     			
			arraytTest[20][1] = 2.5176f;
			arraytTest[20][2] = 2.8314f;
			arraytTest[20][3] = 3.1352f;		
			arraytTest[20][4] = 3.5272f;
			arraytTest[20][5] =	3.819f;				
									
			arraytTest[21][0] = 	2.0739f;     			
			arraytTest[21][1] =		2.5083f;
			arraytTest[21][2] =		2.8188f;
			arraytTest[21][3] =  	3.1188f; 		
			arraytTest[21][4] = 	3.5050f;
			arraytTest[21][5] =		3.7921f;						
									
			arraytTest[22][0] = 	2.0686f;     			
			arraytTest[22][1] =		2.4998f;
			arraytTest[22][2] =		2.8073f;
			arraytTest[22][3] =  	3.1040f; 		
			arraytTest[22][4] = 	3.4850f;
			arraytTest[22][5] =		3.7676f;		
			
			arraytTest[23][0] = 	2.0639f;     			
			arraytTest[23][1] =		2.4922f;
			arraytTest[23][2] =		2.7970f;
			arraytTest[23][3] =  	3.0905f; 		
			arraytTest[23][4] = 	3.4668f;
			arraytTest[23][5] =		3.7454f;		
									
			arraytTest[24][0] = 	2.0596f;     			
			arraytTest[24][1] =		2.4851f;
			arraytTest[24][2] =		2.7874f;
			arraytTest[24][3] =  	3.0782f; 		
			arraytTest[24][4] = 	3.4502f;
			arraytTest[24][5] =		3.7251f;	
			
			arraytTest[25][0] = 	2.0555f;     			
			arraytTest[25][1] =		2.4786f;
			arraytTest[25][2] =		2.7787f;
			arraytTest[25][3] =  	3.0669f; 		
			arraytTest[25][4] = 	3.4350f;
			arraytTest[25][5] =		3.7067f;	
					
			
			arraytTest[26][0] = 	2.0518f;     			
			arraytTest[26][1] =		2.4727f;
			arraytTest[26][2] =		2.7707f;
			arraytTest[26][3] =  	3.0565f; 		
			arraytTest[26][4] = 	3.4211f;
			arraytTest[26][5] =		3.6896f;	
			
			arraytTest[27][0] = 	2.0484f;     			
			arraytTest[27][1] =		2.4671f;
			arraytTest[27][2] =		2.7633f;
			arraytTest[27][3] =  	3.0469f; 		
			arraytTest[27][4] = 	3.4082f;
			arraytTest[27][5] =		3.6739f;	
			
			arraytTest[28][0] = 	2.0452f;     			
			arraytTest[28][1] =		2.4620f;
			arraytTest[28][2] =		2.7564f;
			arraytTest[28][3] =  	3.0380f; 		
			arraytTest[28][4] = 	3.3962f;
			arraytTest[28][5] =		3.6594f;	
			
			arraytTest[29][0] = 	2.0423f;     			
			arraytTest[29][1] =		2.4572f;
			arraytTest[29][2] =		2.7500f;
			arraytTest[29][3] =  	3.0298f; 		
			arraytTest[29][4] = 	3.3852f;
			arraytTest[29][5] =		3.6459f;
			
			arraytTest[30][0] = 	2.0395f;     			
			arraytTest[30][1] =		2.4528f;
			arraytTest[30][2] =		2.7440f;
			arraytTest[30][3] =  	3.0221f; 		
			arraytTest[30][4] = 	3.3749f;
			arraytTest[30][5] =		3.6334f;
								
			arraytTest[31][0] = 	2.0369f;     			
			arraytTest[31][1] =		2.4487f;
			arraytTest[31][2] =		2.7385f;
			arraytTest[31][3] =  	3.0150f; 		
			arraytTest[31][4] = 	3.3653f;
			arraytTest[31][5] =		3.6218f;						
									
			arraytTest[32][0] = 	2.0345f;     			
			arraytTest[32][1] =		2.4448f;
			arraytTest[32][2] =		2.7333f;
			arraytTest[32][3] =  	3.0082f; 		
			arraytTest[32][4] = 	3.3563f;
			arraytTest[32][5] =		3.6109f;						
								
			arraytTest[33][0] = 	2.0322f;     			
			arraytTest[33][1] =		2.4411f;
			arraytTest[33][2] =		2.7284f;
			arraytTest[33][3] =  	3.0019f; 		
			arraytTest[33][4] = 	3.3479f;
			arraytTest[33][5] =		3.6008f;	
			
			arraytTest[34][0] = 	2.0301f;     			
			arraytTest[34][1] =		2.4377f;
			arraytTest[34][2] =		2.7238f;
			arraytTest[34][3] =  	2.9961f; 		
			arraytTest[34][4] = 	3.3400f;
			arraytTest[34][5] =		3.5912f;	
				
			arraytTest[35][0] = 	2.0281f;     			
			arraytTest[35][1] =		2.4345f;
			arraytTest[35][2] =		2.7195f;
			arraytTest[35][3] =  	2.9905f; 		
			arraytTest[35][4] = 	3.3326f;
			arraytTest[35][5] =		3.5822f;	
			
			arraytTest[36][0] = 	2.0262f;     			
			arraytTest[36][1] =		2.4315f;
			arraytTest[36][2] =		2.7154f;
			arraytTest[36][3] =  	2.9853f; 		
			arraytTest[36][4] = 	3.3256f;
			arraytTest[36][5] =		3.5737f;	
									
			arraytTest[37][0] = 	2.0244f;     			
			arraytTest[37][1] =		2.4286f;
			arraytTest[37][2] =		2.7115f;
			arraytTest[37][3] =  	2.9803f; 		
			arraytTest[37][4] = 	3.3190f;
			arraytTest[37][5] =		3.5657f;							
									
			arraytTest[38][0] = 	2.0227f;     			
			arraytTest[38][1] =		2.4258f;
			arraytTest[38][2] =		2.7079f;
			arraytTest[38][3] =  	2.9756f; 		
			arraytTest[38][4] = 	3.3128f;
			arraytTest[38][5] =		3.5581f;							
									
			arraytTest[39][0] = 	2.0211f;     			
			arraytTest[39][1] =		2.4233f;
			arraytTest[39][2] =		2.7045f;
			arraytTest[39][3] =  	2.9712f; 		
			arraytTest[39][4] = 	3.3069f;
			arraytTest[39][5] =		3.5510f;		
			
			arraytTest[40][0] = 	2.0196f;     			
			arraytTest[40][1] =		2.4208f;
			arraytTest[40][2] =		2.7012f;
			arraytTest[40][3] =  	2.9670f; 		
			arraytTest[40][4] = 	3.3013f;
			arraytTest[40][5] =		3.5442f;		
									
			arraytTest[41][0] = 	2.0181f;     			
			arraytTest[41][1] =		2.4185f;
			arraytTest[41][2] =		2.6981f;
			arraytTest[41][3] =  	2.9630f; 		
			arraytTest[41][4] = 	3.2959f;
			arraytTest[41][5] =		3.5378f;	
			
			arraytTest[42][0] = 	2.0167f;     			
			arraytTest[42][1] =		2.4162f;
			arraytTest[42][2] =		2.6951f;
			arraytTest[42][3] =  	2.9591f; 		
			arraytTest[42][4] = 	3.2909f;
			arraytTest[42][5] =		3.5316f;	
									
					
			arraytTest[43][0] = 	2.0154f;     			
			arraytTest[43][1] =		2.4142f;
			arraytTest[43][2] =		2.6923f;
			arraytTest[43][3] =  	2.9555f; 		
			arraytTest[43][4] = 	3.2861f;
			arraytTest[43][5] =		3.5258f;	
			
			arraytTest[44][0] = 	2.0141f;     			
			arraytTest[44][1] =		2.4121f;
			arraytTest[44][2] =		2.6896f;
			arraytTest[44][3] =  	2.9521f; 		
			arraytTest[44][4] = 	3.2815f;
			arraytTest[44][5] =		3.5202f;	
			
			arraytTest[45][0] = 	2.0129f;     			
			arraytTest[45][1] =		2.4102f;
			arraytTest[45][2] =		2.6870f;
			arraytTest[45][3] =  	2.9488f; 		
			arraytTest[45][4] = 	3.2771f;
			arraytTest[45][5] =		3.5149f;	
			
			arraytTest[46][0] = 	2.0117f;     			
			arraytTest[46][1] =		2.4083f;
			arraytTest[46][2] =		2.6846f;
			arraytTest[46][3] =  	2.9456f; 		
			arraytTest[46][4] = 	3.2729f;
			arraytTest[46][5] =		3.5099f;	
									
			arraytTest[47][0] = 	2.0106f;     			
			arraytTest[47][1] =		2.4066f;
			arraytTest[47][2] =		2.6822f;
			arraytTest[47][3] =  	2.9426f; 		
			arraytTest[47][4] = 	3.2689f;
			arraytTest[47][5] =		3.5051f;							
									
			arraytTest[48][0] = 	2.0096f;     			
			arraytTest[48][1] =		2.4049f;
			arraytTest[48][2] =		2.6800f;
			arraytTest[48][3] =  	2.9397f; 		
			arraytTest[48][4] = 	3.2651f;
			arraytTest[48][5] =		3.5004f;							
									
			arraytTest[49][0] = 	2.0086f;     			
			arraytTest[49][1] =		2.4033f;
			arraytTest[49][2] =		2.6778f;
			arraytTest[49][3] =  	2.9370f; 		
			arraytTest[49][4] = 	3.2614f;
			arraytTest[49][5] =		3.4960f;
			
			arraytTest[50][0] = 	2.0076f;     			
			arraytTest[50][1] =		2.4017f;
			arraytTest[50][2] =		2.6757f;
			arraytTest[50][3] =  	2.9343f; 		
			arraytTest[50][4] = 	3.2579f;
			arraytTest[50][5] =		3.4917f;
			
			arraytTest[51][0] = 	2.0066f;     			
			arraytTest[51][1] =		2.4002f;
			arraytTest[51][2] =		2.6737f;
			arraytTest[51][3] =  	2.9318f; 		
			arraytTest[51][4] = 	3.2545f;
			arraytTest[51][5] =		3.4877f;
															
			arraytTest[52][0] = 	2.0057f;     			
			arraytTest[52][1] =		2.3988f;
			arraytTest[52][2] =		2.6718f;
			arraytTest[52][3] =  	2.9293f; 		
			arraytTest[52][4] = 	3.2513f;
			arraytTest[52][5] =		3.4838f;
															
			arraytTest[53][0] = 	2.0049f;     			
			arraytTest[53][1] =		2.3974f;
			arraytTest[53][2] =		2.6700f;
			arraytTest[53][3] =  	2.9270f; 		
			arraytTest[53][4] = 	3.2482f;
			arraytTest[53][5] =		3.4800f;						
									
			arraytTest[54][0] = 	2.0041f;     			
			arraytTest[54][1] =		2.3961f;
			arraytTest[54][2] =		2.6682f;
			arraytTest[54][3] =  	2.9247f; 		
			arraytTest[54][4] = 	3.2451f;
			arraytTest[54][5] =		3.4764f;		
			
			arraytTest[55][0] = 	2.0032f;     			
			arraytTest[55][1] =		2.3948f;
			arraytTest[55][2] =		2.6665f;
			arraytTest[55][3] =  	2.9225f; 		
			arraytTest[55][4] = 	3.2423f;
			arraytTest[55][5] =		3.4730f;		
			
			arraytTest[56][0] = 	2.0025f;     			
			arraytTest[56][1] =		2.3936f;
			arraytTest[56][2] =		2.6649f;
			arraytTest[56][3] =  	2.9204f; 		
			arraytTest[56][4] = 	3.2394f;
			arraytTest[56][5] =		3.4696f;		
								
			arraytTest[57][0] = 	2.0017f;     			
			arraytTest[57][1] =		2.3924f;
			arraytTest[57][2] =		2.6633f;
			arraytTest[57][3] =  	2.9184f; 		
			arraytTest[57][4] = 	3.2368f;
			arraytTest[57][5] =		3.4663f;	
			
			arraytTest[58][0] = 	2.0010f;     			
			arraytTest[58][1] =		2.3912f;
			arraytTest[58][2] =		2.6618f;
			arraytTest[58][3] =  	2.9164f; 		
			arraytTest[58][4] = 	3.2342f;
			arraytTest[58][5] =		3.4632f;	
			
			arraytTest[59][0] = 	2.0003f;     			
			arraytTest[59][1] =		2.3901f;
			arraytTest[59][2] =		2.6603f;
			arraytTest[59][3] =  	2.9146f; 		
			arraytTest[59][4] = 	3.2317f;
			arraytTest[59][5] =		3.4602f;	
			
			arraytTest[60][0] = 	1.9996f;     			
			arraytTest[60][1] =		2.3890f;
			arraytTest[60][2] =		2.6589f;
			arraytTest[60][3] =  	2.9127f; 		
			arraytTest[60][4] = 	3.2293f;
			arraytTest[60][5] =		3.4573f;	
									
			arraytTest[61][0] = 	1.9990f;     			
			arraytTest[61][1] =		2.3880f;
			arraytTest[61][2] =		2.6575f;
			arraytTest[61][3] =  	2.9110f; 		
			arraytTest[61][4] = 	3.2269f;
			arraytTest[61][5] =		3.4545f;							
									
			arraytTest[62][0] = 	1.9983f;     			
			arraytTest[62][1] =		2.3870f;
			arraytTest[62][2] =		2.6561f;
			arraytTest[62][3] =  	2.9092f; 		
			arraytTest[62][4] = 	3.2247f;
			arraytTest[62][5] =		3.4518f;							
								
			arraytTest[63][0] = 	1.9977f;     			
			arraytTest[63][1] =		2.3860f;
			arraytTest[63][2] =		2.6549f;
			arraytTest[63][3] =  	2.9076f; 		
			arraytTest[63][4] = 	3.2225f;
			arraytTest[63][5] =		3.4491f;	
		
			arraytTest[64][0] = 	1.9971f;     			
			arraytTest[64][1] =		2.3851f;
			arraytTest[64][2] =		2.6536f;
			arraytTest[64][3] =  	2.9060f; 		
			arraytTest[64][4] = 	3.2204f;
			arraytTest[64][5] =		3.4466f;	
								
			arraytTest[65][0] = 	1.9966f;     			
			arraytTest[65][1] =		2.3842f;
			arraytTest[65][2] =		2.6524f;
			arraytTest[65][3] =  	2.9045f; 		
			arraytTest[65][4] = 	3.2184f;
			arraytTest[65][5] =		3.4441f;							
								
			arraytTest[66][0] = 	1.9960f;     			
			arraytTest[66][1] =		2.3833f;
			arraytTest[66][2] =		2.6512f;
			arraytTest[66][3] =  	2.9030f; 		
			arraytTest[66][4] = 	3.2164f;
			arraytTest[66][5] =		3.4417f;							
									
			arraytTest[67][0] = 	1.9955f;     			
			arraytTest[67][1] =		2.3824f;
			arraytTest[67][2] =		2.6501f;
			arraytTest[67][3] =  	2.9015f; 		
			arraytTest[67][4] = 	3.2144f;
			arraytTest[67][5] =		3.4395f;							
															
			arraytTest[68][0] = 	1.9950f;     			
			arraytTest[68][1] =		2.3816f;
			arraytTest[68][2] =		2.6490f;
			arraytTest[68][3] =  	2.9001f; 		
			arraytTest[68][4] = 	3.2126f;
			arraytTest[68][5] =		3.4372f;	
			
			arraytTest[69][0] = 	1.9944f;     			
			arraytTest[69][1] =		2.3808f;
			arraytTest[69][2] =		2.6479f;
			arraytTest[69][3] =  	2.8987f; 		
			arraytTest[69][4] = 	3.2108f;
			arraytTest[69][5] =		3.4350f;	
									
			arraytTest[70][0] = 	1.9939f;     			
			arraytTest[70][1] =		2.3800f;
			arraytTest[70][2] =		2.6468f;
			arraytTest[70][3] =  	2.8974f; 		
			arraytTest[70][4] = 	3.2090f;
			arraytTest[70][5] =		3.4329f;
			
			
			arraytTest[71][0] = 	1.9935f;     			
			arraytTest[71][1] =		2.3793f;
			arraytTest[71][2] =		2.6459f;
			arraytTest[71][3] =  	2.8961f; 		
			arraytTest[71][4] = 	3.2073f;
			arraytTest[71][5] =		3.4308f;
			
			arraytTest[72][0] = 	1.9930f;     			
			arraytTest[72][1] =		2.3785f;
			arraytTest[72][2] =		2.6449f;
			arraytTest[72][3] =  	2.8948f; 		
			arraytTest[72][4] = 	3.2056f;
			arraytTest[72][5] =		3.4288f;
								
			arraytTest[73][0] = 	1.9925f;     			
			arraytTest[73][1] =		2.3778f;
			arraytTest[73][2] =		2.6439f;
			arraytTest[73][3] =  	2.8936f; 		
			arraytTest[73][4] = 	3.2040f;
			arraytTest[73][5] =		3.4269f;
			
			arraytTest[74][0] = 	1.9921f;     			
			arraytTest[74][1] =		2.3771f;
			arraytTest[74][2] =		2.6430f;
			arraytTest[74][3] =  	2.8925f; 		
			arraytTest[74][4] = 	3.2025f;
			arraytTest[74][5] =		3.4250f;
									
			arraytTest[75][0] = 	1.9917f;     			
			arraytTest[75][1] =		2.3764f;
			arraytTest[75][2] =		2.6421f;
			arraytTest[75][3] =  	2.8913f; 		
			arraytTest[75][4] = 	3.2010f;
			arraytTest[75][5] =		3.4232f;
			
			arraytTest[76][0] = 	1.9913f;     			
			arraytTest[76][1] =		2.3758f;
			arraytTest[76][2] =		2.6412f;
			arraytTest[76][3] =  	2.8902f; 		
			arraytTest[76][4] = 	3.1995f;
			arraytTest[76][5] =		3.4214f;
										
			arraytTest[77][0] = 	1.9909f;     			
			arraytTest[77][1] =		2.3751f;
			arraytTest[77][2] =		2.6404f;
			arraytTest[77][3] =  	2.8891f; 		
			arraytTest[77][4] = 	3.1980f;
			arraytTest[77][5] =		3.4197f;
			
			arraytTest[78][0] = 	1.9904f;     			
			arraytTest[78][1] =		2.3745f;
			arraytTest[78][2] =		2.6395f;
			arraytTest[78][3] =  	2.8880f; 		
			arraytTest[78][4] = 	3.1966f;
			arraytTest[78][5] =		3.4180f;
											
			arraytTest[79][0] = 	1.9901f;     			
			arraytTest[79][1] =		2.3739f;
			arraytTest[79][2] =		2.6387f;
			arraytTest[79][3] =  	2.8870f; 		
			arraytTest[79][4] = 	3.1953f;
			arraytTest[79][5] =		3.4164f;
			
			arraytTest[80][0] = 	1.9897f;     			
			arraytTest[80][1] =		2.3733f;
			arraytTest[80][2] =		2.6379f;
			arraytTest[80][3] =  	2.8859f; 		
			arraytTest[80][4] = 	3.1939f;
			arraytTest[80][5] =		3.4147f;
								
			
			arraytTest[81][0] = 	1.9893f;     			
			arraytTest[81][1] =		2.3727f;
			arraytTest[81][2] =		2.6371f;
			arraytTest[81][3] =  	2.8850f; 		
			arraytTest[81][4] = 	3.1926f;
			arraytTest[81][5] =		3.4132f;
			
			arraytTest[82][0] = 	1.9889f;     			
			arraytTest[82][1] =		2.3721f;
			arraytTest[82][2] =		2.6364f;
			arraytTest[82][3] =  	2.8840f; 		
			arraytTest[82][4] = 	3.1913f;
			arraytTest[82][5] =		3.4117f;
									
									
									
			
			arraytTest[83][0] = 	1.9886f;     			
			arraytTest[83][1] =		2.3716f;
			arraytTest[83][2] =		2.6356f;
			arraytTest[83][3] =  	2.8831f; 		
			arraytTest[83][4] = 	3.1901f;
			arraytTest[83][5] =		3.4101f;
			
			arraytTest[84][0] = 	1.9883f;     			
			arraytTest[84][1] =		2.3710f;
			arraytTest[84][2] =		2.6349f;
			arraytTest[84][3] =  	2.8821f; 		
			arraytTest[84][4] = 	3.1889f;
			arraytTest[84][5] =		3.4087f;
								
			
			arraytTest[85][0] = 	1.9879f;     			
			arraytTest[85][1] =		2.3705f;
			arraytTest[85][2] =		2.6342f;
			arraytTest[85][3] =  	2.8813f; 		
			arraytTest[85][4] = 	3.1877f;
			arraytTest[85][5] =		3.4073f;
			
			arraytTest[86][0] = 	1.9876f;     			
			arraytTest[86][1] =		2.3700f;
			arraytTest[86][2] =		2.6335f;
			arraytTest[86][3] =  	2.8804f; 		
			arraytTest[86][4] = 	3.1866f;
			arraytTest[86][5] =		3.4059f;
																					
			arraytTest[87][0] = 	1.9873f;     			
			arraytTest[87][1] =		2.3695f;
			arraytTest[87][2] =		2.6328f;
			arraytTest[87][3] =  	2.8795f; 		
			arraytTest[87][4] = 	3.1854f;
			arraytTest[87][5] =		3.4046f;
			
			arraytTest[88][0] = 	1.9870f;     			
			arraytTest[88][1] =		2.3690f;
			arraytTest[88][2] =		2.6322f;
			arraytTest[88][3] =  	2.8787f; 		
			arraytTest[88][4] = 	3.1844f;
			arraytTest[88][5] =		3.4032f;
			
									
									
			
			arraytTest[89][0] = 	1.9867f;     			
			arraytTest[89][1] =		2.3685f;
			arraytTest[89][2] =		2.6316f;
			arraytTest[89][3] =  	2.8779f; 		
			arraytTest[89][4] = 	3.1833f;
			arraytTest[89][5] =		3.402f;
			
			arraytTest[90][0] = 	1.9864f;     			
			arraytTest[90][1] =		2.3680f;
			arraytTest[90][2] =		2.6309f;
			arraytTest[90][3] =  	2.8771f; 		
			arraytTest[90][4] = 	3.1822f;
			arraytTest[90][5] =		3.4006f;
									
								
			arraytTest[91][0] = 	1.9861f;     			
			arraytTest[91][1] =		2.3676f;
			arraytTest[91][2] =		2.6303f;
			arraytTest[91][3] =  	2.8763f; 		
			arraytTest[91][4] = 	3.1812f;
			arraytTest[91][5] =		3.3995f;
			
			arraytTest[92][0] = 	1.9858f;     			
			arraytTest[92][1] =		2.3671f;
			arraytTest[92][2] =		2.6297f;
			arraytTest[92][3] =  	2.8755f; 		
			arraytTest[92][4] = 	3.1802f;
			arraytTest[92][5] =		3.3982f;
									
									
			arraytTest[93][0] = 	1.9855f;     			
			arraytTest[93][1] =		2.3667f;
			arraytTest[93][2] =		2.6292f;
			arraytTest[93][3] =  	2.8748f; 		
			arraytTest[93][4] = 	3.1792f;
			arraytTest[93][5] =		3.3970f;
			
			arraytTest[94][0] = 	1.9852f;     			
			arraytTest[94][1] =		2.3662f;
			arraytTest[94][2] =		2.6286f;
			arraytTest[94][3] =  	2.8741f; 		
			arraytTest[94][4] = 	3.1782f;
			arraytTest[94][5] =		3.3959f;
					
			
			arraytTest[95][0] = 	1.9850f;     			
			arraytTest[95][1] =		2.3658f;
			arraytTest[95][2] =		2.6280f;
			arraytTest[95][3] =  	2.8734f; 		
			arraytTest[95][4] = 	3.1773f;
			arraytTest[95][5] =		3.3947f;
								
			arraytTest[96][0] = 	1.9847f;     			
			arraytTest[96][1] =		2.3654f;
			arraytTest[96][2] =		2.6275f;
			arraytTest[96][3] =  	2.8727f; 		
			arraytTest[96][4] = 	3.1764f;
			arraytTest[96][5] =		3.3936f;
			
		
									
									
			arraytTest[97][0] = 	1.9845f;     			
			arraytTest[97][1] =		2.3650f;
			arraytTest[97][2] =		2.6269f;
			arraytTest[97][3] =  	2.8720f; 		
			arraytTest[97][4] = 	3.1755f;
			arraytTest[97][5] =		3.3926f;
			
			arraytTest[98][0] = 	1.9842f;     			
			arraytTest[98][1] =		2.3646f;
			arraytTest[98][2] =		2.6264f;
			arraytTest[98][3] =  	2.8713f; 		
			arraytTest[98][4] = 	3.1746f;
			arraytTest[98][5] =		3.3915f;
									
									
			arraytTest[99][0] = 	1.9840f;     			
			arraytTest[99][1] =		2.3642f;
			arraytTest[99][2] =		2.6259f;
			arraytTest[99][3] =  	2.8706f; 		
			arraytTest[99][4] = 	3.1738f;
			arraytTest[99][5] =		3.3905f;
			
			arraytTest[100][0] = 	1.9837f;     			
			arraytTest[100][1] =	2.3638f;
			arraytTest[100][2] =	2.6254f;
			arraytTest[100][3] =  	2.8700f; 		
			arraytTest[100][4] = 	3.1729f;
			arraytTest[100][5] =	3.3894f;
			
									
									
			
			arraytTest[101][0] = 	1.9835f;     			
			arraytTest[101][1] =	2.3635f;
			arraytTest[101][2] =	2.6249f;
			arraytTest[101][3] =  	2.8694f; 		
			arraytTest[101][4] = 	3.1720f;
			arraytTest[101][5] =	3.3885f;
			
			arraytTest[102][0] = 	1.9833f;     			
			arraytTest[102][1] =	2.3631f;
			arraytTest[102][2] =	2.6244f;
			arraytTest[102][3] =  	2.8687f; 		
			arraytTest[102][4] = 	3.1712f;
			arraytTest[102][5] =	3.3875f;
									
									
			arraytTest[103][0] = 	1.9830f;     			
			arraytTest[103][1] =	2.3627f;
			arraytTest[103][2] =	2.6240f;
			arraytTest[103][3] =  	2.8682f; 		
			arraytTest[103][4] = 	3.1704f;
			arraytTest[103][5] =	3.3866f;
			
			arraytTest[104][0] = 	1.9828f;     			
			arraytTest[104][1] =	2.3624f;
			arraytTest[104][2] =	2.6235f;
			arraytTest[104][3] =  	2.8675f; 		
			arraytTest[104][4] = 	3.1697f;
			arraytTest[104][5] =	3.3856f;
									
									
			arraytTest[105][0] = 	1.9826f;     			
			arraytTest[105][1] =	2.3620f;
			arraytTest[105][2] =	2.6230f;
			arraytTest[105][3] =  	2.8670f; 		
			arraytTest[105][4] = 	3.1689f;
			arraytTest[105][5] =	3.3847f;
			
			arraytTest[106][0] = 	1.9824f;     			
			arraytTest[106][1] =	2.3617f;
			arraytTest[106][2] =	2.6225f;
			arraytTest[106][3] =  	2.8664f; 		
			arraytTest[106][4] = 	3.1681f;
			arraytTest[106][5] =	3.3838f;
									
									
			arraytTest[107][0] = 	1.9822f;     			
			arraytTest[107][1] =	2.3614f;
			arraytTest[107][2] =	2.6221f;
			arraytTest[107][3] =  	2.8658f; 		
			arraytTest[107][4] = 	3.1674f;
			arraytTest[107][5] =	3.3829f;
			
			arraytTest[108][0] = 	1.9820f;     			
			arraytTest[108][1] =	2.3611f;
			arraytTest[108][2] =	2.6217f;
			arraytTest[108][3] =  	2.8653f; 		
			arraytTest[108][4] = 	3.1667f;
			arraytTest[108][5] =	3.3820f;
									
									
			arraytTest[109][0] = 	1.9818f;     			
			arraytTest[109][1] =	2.3607f;
			arraytTest[109][2] =	2.6212f;
			arraytTest[109][3] =  	2.8647f; 		
			arraytTest[109][4] = 	3.1660f;
			arraytTest[109][5] =	3.3812f;
			
			arraytTest[110][0] = 	1.9816f;     			
			arraytTest[110][1] =	2.3604f;
			arraytTest[110][2] =	2.6208f;
			arraytTest[110][3] =  	2.8642f; 		
			arraytTest[110][4] = 	3.1653f;
			arraytTest[110][5] =	3.3803f;
									
									
			arraytTest[111][0] = 	1.9814f;     			
			arraytTest[111][1] =	2.3601f;
			arraytTest[111][2] =	2.6204f;
			arraytTest[111][3] =  	2.8637f; 		
			arraytTest[111][4] = 	3.1646f;
			arraytTest[111][5] =	3.3795f;
			
			arraytTest[112][0] = 	1.9812f;     			
			arraytTest[112][1] =	2.3598f;
			arraytTest[112][2] =	2.6200f;
			arraytTest[112][3] =  	2.8632f; 		
			arraytTest[112][4] = 	3.1640f;
			arraytTest[112][5] =	3.3787f;
									
									
			arraytTest[113][0] = 	1.9810f;     			
			arraytTest[113][1] =	2.3595f;
			arraytTest[113][2] =	2.6196f;
			arraytTest[113][3] =  	2.8627f; 		
			arraytTest[113][4] = 	3.1633f;
			arraytTest[113][5] =	3.3779f;
			
			arraytTest[114][0] = 	1.9808f;     			
			arraytTest[114][1] =	2.3592f;
			arraytTest[114][2] =	2.6192f;
			arraytTest[114][3] =  	2.8622f; 		
			arraytTest[114][4] = 	3.1626f;
			arraytTest[114][5] =	3.3771f;
									
									
			arraytTest[115][0] = 	1.9806f;     			
			arraytTest[115][1] =	2.3589f;
			arraytTest[115][2] =	2.6189f;
			arraytTest[115][3] =  	2.8617f; 		
			arraytTest[115][4] = 	3.1620f;
			arraytTest[115][5] =	3.3764f;
			
			arraytTest[116][0] = 	1.9805f;     			
			arraytTest[116][1] =	2.3586f;
			arraytTest[116][2] =	2.6185f;
			arraytTest[116][3] =  	2.8612f; 		
			arraytTest[116][4] = 	3.1614f;
			arraytTest[116][5] =	3.3756f;
									
									
			arraytTest[117][0] = 	1.9803f;     			
			arraytTest[117][1] =	2.3583f;
			arraytTest[117][2] =	2.6181f;
			arraytTest[117][3] =  	2.8608f; 		
			arraytTest[117][4] = 	3.1607f;
			arraytTest[117][5] =	3.3749f;
		
			arraytTest[118][0] = 	1.9801f;     			
			arraytTest[118][1] =	2.3581f;
			arraytTest[118][2] =	2.6178f;
			arraytTest[118][3] =  	2.8603f; 		
			arraytTest[118][4] = 	3.1601f;
			arraytTest[118][5] =	3.3741f;
									
									
			arraytTest[119][0] = 	1.9799f;     			
			arraytTest[119][1] =	2.3578f;
			arraytTest[119][2] =	2.6174f;
			arraytTest[119][3] =  	2.8599f; 		
			arraytTest[119][4] = 	3.1595f;
			arraytTest[119][5] =	3.3735f;
			
			arraytTest[120][0] = 	1.9798f;     			
			arraytTest[120][1] =	2.3576f;
			arraytTest[120][2] =	2.6171f;
			arraytTest[120][3] =  	2.8594f; 		
			arraytTest[120][4] = 	3.1589f;
			arraytTest[120][5] =	3.3727f;
									
									
			arraytTest[121][0] = 	1.9796f;     			
			arraytTest[121][1] =	2.3573f;
			arraytTest[121][2] =	2.6168f;
			arraytTest[121][3] =  	2.8590f; 		
			arraytTest[121][4] = 	3.1584f;
			arraytTest[121][5] =	3.3721f;
			
			arraytTest[122][0] = 	1.9794f;     			
			arraytTest[122][1] =	2.3571f;
			arraytTest[122][2] =	2.6164f;
			arraytTest[122][3] =  	2.8585f; 		
			arraytTest[122][4] = 	3.1578f;
			arraytTest[122][5] =	3.3714f;
									
									
			arraytTest[123][0] = 	1.9793f;     			
			arraytTest[123][1] =	2.3568f;
			arraytTest[123][2] =	2.6161f;
			arraytTest[123][3] =  	2.8582f; 		
			arraytTest[123][4] = 	3.1573f;
			arraytTest[123][5] =	3.3707f;
			
			arraytTest[124][0] = 	1.9791f;     			
			arraytTest[124][1] =	2.3565f;
			arraytTest[124][2] =	2.6158f;
			arraytTest[124][3] = 	2.8577f; 		
			arraytTest[124][4] = 	3.1567f;
			arraytTest[124][5] =	3.3700f;
									
									
			arraytTest[125][0] = 	1.9790f;     			
			arraytTest[125][1] =	2.3563f;
			arraytTest[125][2] =	2.6154f;
			arraytTest[125][3] =  	2.8573f; 		
			arraytTest[125][4] = 	3.1562f;
			arraytTest[125][5] =	3.3694f;
			
			arraytTest[126][0] = 	1.9788f;     			
			arraytTest[126][1] =	2.3561f;
			arraytTest[126][2] =	2.6151f;
			arraytTest[126][3] =  	2.8569f; 		
			arraytTest[126][4] = 	3.1556f;
			arraytTest[126][5] =	3.3688f;
								
								
			arraytTest[127][0] = 	1.9787f;     			
			arraytTest[127][1] =	2.3559f;
			arraytTest[127][2] =	2.6148f;
			arraytTest[127][3] =  	2.8565f; 		
			arraytTest[127][4] = 	3.1551f;
			arraytTest[127][5] =	3.3682f;
		
			arraytTest[128][0] = 	1.9785f;     			
			arraytTest[128][1] =	2.3556f;
			arraytTest[128][2] =	2.6145f;
			arraytTest[128][3] =  	2.8561f; 		
			arraytTest[128][4] = 	3.1546f;
			arraytTest[128][5] =	3.3676f;
									
									
			arraytTest[129][0] = 	1.9784f;     			
			arraytTest[129][1] =	2.3554f;
			arraytTest[129][2] =	2.6142f;
			arraytTest[129][3] =  	2.8557f; 		
			arraytTest[129][4] = 	3.1541f;
			arraytTest[129][5] =	3.3669f;
			
			arraytTest[130][0] = 	1.9782f;     			
			arraytTest[130][1] =	2.3552f;
			arraytTest[130][2] =	2.6139f;
			arraytTest[130][3] =  	2.8554f; 		
			arraytTest[130][4] = 	3.1536f;
			arraytTest[130][5] =	3.3663f;
									
									
			arraytTest[131][0] = 	1.9781f;     			
			arraytTest[131][1] =	2.3549f;
			arraytTest[131][2] =	2.6136f;
			arraytTest[131][3] =  	2.8550f; 		
			arraytTest[131][4] = 	3.1531f;
			arraytTest[131][5] =	3.3658f;
			
			arraytTest[132][0] = 	1.9779f;     			
			arraytTest[132][1] =	2.3547f;
			arraytTest[132][2] =	2.6133f;
			arraytTest[132][3] =  	2.8546f; 		
			arraytTest[132][4] = 	3.1526f;
			arraytTest[132][5] =	3.3652f;
									
									
			arraytTest[133][0] = 	1.9778f;     			
			arraytTest[133][1] =	2.3545f;
			arraytTest[133][2] =	2.6130f;
			arraytTest[133][3] =  	2.8542f; 		
			arraytTest[133][4] = 	3.1522f;
			arraytTest[133][5] =	3.3646f;
			
			arraytTest[134][0] = 	1.9777f;     			
			arraytTest[134][1] =	2.3543f;
			arraytTest[134][2] =	2.6127f;
			arraytTest[134][3] =  	2.8539f; 		
			arraytTest[134][4] = 	3.1517f;
			arraytTest[134][5] =	3.3641f;
									
									
			arraytTest[135][0] = 	1.9776f;     			
			arraytTest[135][1] =	2.3541f;
			arraytTest[135][2] =	2.6125f;
			arraytTest[135][3] =  	2.8536f; 		
			arraytTest[135][4] = 	3.1512f;
			arraytTest[135][5] =	3.3635f;
			
			arraytTest[136][0] = 	1.9774f;     			
			arraytTest[136][1] =	2.3539f;
			arraytTest[136][2] =	2.6122f;
			arraytTest[136][3] =  	2.8532f; 		
			arraytTest[136][4] = 	3.1508f;
			arraytTest[136][5] =	3.3630f;
									
									
			arraytTest[137][0] = 	1.9773f;     			
			arraytTest[137][1] =	2.3537f;
			arraytTest[137][2] =	2.6119f;
			arraytTest[137][3] =  	2.8529f; 		
			arraytTest[137][4] = 	3.1503f;
			arraytTest[137][5] =	3.3624f;
			
			arraytTest[138][0] = 	1.9772f;     			
			arraytTest[138][1] =	2.3535f;
			arraytTest[138][2] =	2.6117f;
			arraytTest[138][3] =  	2.8525f; 		
			arraytTest[138][4] = 	3.1499f;
			arraytTest[138][5] =	3.3619f;
			
								
									
			arraytTest[139][0] = 	1.9771f;     			
			arraytTest[139][1] =	2.3533f;
			arraytTest[139][2] =	2.6114f;
			arraytTest[139][3] =  	2.8522f; 		
			arraytTest[139][4] = 	3.1495f;
			arraytTest[139][5] =	3.3614f;
			
			arraytTest[140][0] = 	1.9769f;     			
			arraytTest[140][1] =	2.3531f;
			arraytTest[140][2] =	2.6112f;
			arraytTest[140][3] =  	2.8519f; 		
			arraytTest[140][4] = 	3.1491f;
			arraytTest[140][5] =	3.3609f;
								
									
			arraytTest[141][0] = 	1.9768f;     			
			arraytTest[141][1] =	2.3529f;
			arraytTest[141][2] =	2.6109f;
			arraytTest[141][3] =  	2.8516f; 		
			arraytTest[141][4] = 	3.1486f;
			arraytTest[141][5] =	3.3604f;
			
			arraytTest[142][0] = 	1.9767f;     			
			arraytTest[142][1] =	2.3527f;
			arraytTest[142][2] =	2.6106f;
			arraytTest[142][3] =  	2.8512f; 		
			arraytTest[142][4] = 	3.1482f;
			arraytTest[142][5] =	3.3599f;
									
								
			arraytTest[143][0] = 	1.9766f;     			
			arraytTest[143][1] =	2.3525f;
			arraytTest[143][2] =	2.6104f;
			arraytTest[143][3] =  	2.8510f; 		
			arraytTest[143][4] = 	3.1478f;
			arraytTest[143][5] =	3.3594f;
			
			arraytTest[144][0] = 	1.9765f;     			
			arraytTest[144][1] =	2.3523f;
			arraytTest[144][2] =	2.6102f;
			arraytTest[144][3] =  	2.8506f; 		
			arraytTest[144][4] = 	3.1474f;
			arraytTest[144][5] =	3.3589f;
									
									
			arraytTest[145][0] = 	1.9764f;     			
			arraytTest[145][1] =	2.3522f;
			arraytTest[145][2] =	2.6099f;
			arraytTest[145][3] =  	2.8503f; 		
			arraytTest[145][4] = 	3.1470f;
			arraytTest[145][5] =	3.3584f;
			
			arraytTest[146][0] = 	1.9762f;     			
			arraytTest[146][1] =	2.3520f;
			arraytTest[146][2] =	2.6097f;
			arraytTest[146][3] =  	2.8500f; 		
			arraytTest[146][4] = 	3.1466f;
			arraytTest[146][5] =	3.3579f;
								
									
			arraytTest[147][0] =    1.9761f;     			
			arraytTest[147][1] =	2.3518f;
			arraytTest[147][2] =	2.6094f;
			arraytTest[147][3] =  	2.8497f; 		
			arraytTest[147][4] = 	3.1462f;
			arraytTest[147][5] =	3.3575f;
			
			arraytTest[148][0] = 	1.9760f;     			
			arraytTest[148][1] =	2.3516f;
			arraytTest[148][2] =	2.6092f;
			arraytTest[148][3] =  	2.8494f; 		
			arraytTest[148][4] = 	3.1458f;
			arraytTest[148][5] =	3.3570f;
									
			arraytTest[149][0] = 	1.9759f;     			
			arraytTest[149][1] =	2.3515f;
			arraytTest[149][2] =	2.6090f;
			arraytTest[149][3] =  	2.8491f; 		
			arraytTest[149][4] = 	3.1455f;
			arraytTest[149][5] =	3.3565f;
			
			arraytTest[150][0] = 	1.9758f;     			
			arraytTest[150][1] =	2.3513f;
			arraytTest[150][2] =	2.6088f;
			arraytTest[150][3] =  	2.8489f; 		
			arraytTest[150][4] = 	3.1451f;
			arraytTest[150][5] =	3.3561f;
									
					
			arraytTest[151][0] = 	1.9757f;     			
			arraytTest[151][1] =	2.3511f;
			arraytTest[151][2] =	2.6085f;
			arraytTest[151][3] =  	2.8486f; 		
			arraytTest[151][4] = 	3.1447f;
			arraytTest[151][5] =	3.3557f;
			
			
			arraytTest[152][0] = 	1.9756f;     			
			arraytTest[152][1] =	2.3510f;
			arraytTest[152][2] =	2.6083f;
			arraytTest[152][3] =  	2.8483f; 		
			arraytTest[152][4] = 	3.1443f;
			arraytTest[152][5] =	3.3552f;						
									
			arraytTest[153][0] = 	1.9755f;     			
			arraytTest[153][1] =	2.3508f;
			arraytTest[153][2] =	2.6081f;
			arraytTest[153][3] =  	2.8481f; 		
			arraytTest[153][4] = 	3.1440f;
			arraytTest[153][5] =	3.3548f;
			
			arraytTest[154][0] = 	1.9754f;     			
			arraytTest[154][1] =	2.3507f;
			arraytTest[154][2] =	2.6079f;
			arraytTest[154][3] =  	2.8478f; 		
			arraytTest[154][4] = 	3.1436f;
			arraytTest[154][5] =	3.3544f;
			
									
									
			arraytTest[155][0] = 	1.9753f;     			
			arraytTest[155][1] =	2.3505f;
			arraytTest[155][2] =	2.6077f;
			arraytTest[155][3] =  	2.8475f; 		
			arraytTest[155][4] = 	3.1433f;
			arraytTest[155][5] =	3.3540f;
			
			arraytTest[156][0] = 	1.9752f;     			
			arraytTest[156][1] =	2.3503f;
			arraytTest[156][2] =	2.6075f;
			arraytTest[156][3] =  	2.8472f; 		
			arraytTest[156][4] = 	3.1430f;
			arraytTest[156][5] =	3.3536f;
							
			
			
			
			
			arraytTest[157][0] = 	1.9751f;     			
			arraytTest[157][1] =	2.3502f;
			arraytTest[157][2] =	2.6073f;
			arraytTest[157][3] =  	2.8470f; 		
			arraytTest[157][4] = 	3.1426f;
			arraytTest[157][5] =	3.3531f;
			
			arraytTest[158][0] = 	1.9750f;     			
			arraytTest[158][1] =	2.3500f;
			arraytTest[158][2] =	2.6071f;
			arraytTest[158][3] =  	2.8467f; 		
			arraytTest[158][4] = 	3.1423f;
			arraytTest[158][5] =	3.3528f;
									
									
			arraytTest[159][0] = 	1.9749f;     			
			arraytTest[159][1] =	2.3499f;
			arraytTest[159][2] =	2.6069f;
			arraytTest[159][3] =  	2.8465f; 		
			arraytTest[159][4] = 	3.1419f;
			arraytTest[159][5] =	3.3523f;
			
			arraytTest[160][0] = 	1.9748f;     			
			arraytTest[160][1] =	2.3497f;
			arraytTest[160][2] =	2.6067f;
			arraytTest[160][3] =  	2.8463f; 		
			arraytTest[160][4] = 	3.1417f;
			arraytTest[160][5] =	3.3520f;
			
			
			arraytTest[161][0] = 	1.9747f;     			
			arraytTest[161][1] =	2.3496f;
			arraytTest[161][2] =	2.6065f;
			arraytTest[161][3] =  	2.8460f; 		
			arraytTest[161][4] = 	3.1413f;
			arraytTest[161][5] =	3.3516f;	
				
									
								
									
			arraytTest[162][0] = 	1.9746f;     			
			arraytTest[162][1] =	2.3495f;
			arraytTest[162][2] =	2.6063f;
			arraytTest[162][3] =  	2.8458f; 		
			arraytTest[162][4] = 	3.1410f;
			arraytTest[162][5] =	3.3512f;
			
			arraytTest[163][0] = 	1.9745f;     			
			arraytTest[163][1] =	2.3493f;
			arraytTest[163][2] =	2.6062f;
			arraytTest[163][3] =  	2.8455f; 		
			arraytTest[163][4] = 	3.1407f;
			arraytTest[163][5] =	3.3508f;								
									
			arraytTest[164][0] = 	1.9744f;     			
			arraytTest[164][1] =	2.3492f;
			arraytTest[164][2] =	2.6060f;
			arraytTest[164][3] =  	2.8452f; 		
			arraytTest[164][4] = 	3.1403f;
			arraytTest[164][5] =	3.3505f;
			
			arraytTest[165][0] = 	1.9744f;     			
			arraytTest[165][1] =	2.3490f;
			arraytTest[165][2] =	2.6058f;
			arraytTest[165][3] =  	2.8450f; 		
			arraytTest[165][4] = 	3.1400f;
			arraytTest[165][5] =	3.3501f;
									
			arraytTest[166][0] = 	1.9743f;     			
			arraytTest[166][1] =	2.3489f;
			arraytTest[166][2] =	2.6056f;
			arraytTest[166][3] =  	2.8448f; 		
			arraytTest[166][4] = 	3.1398f;
			arraytTest[166][5] =	3.3497f;
			
			arraytTest[167][0] = 	1.9742f;     			
			arraytTest[167][1] =	2.3487f;
			arraytTest[167][2] =	2.6054f;
			arraytTest[167][3] =  	2.8446f; 		
			arraytTest[167][4] = 	3.1394f;
			arraytTest[167][5] =	3.3494f;
									
									
			arraytTest[168][0] = 	1.9741f;     			
			arraytTest[168][1] =	2.3486f;
			arraytTest[168][2] =	2.6052f;
			arraytTest[168][3] =  	2.8443f; 		
			arraytTest[168][4] = 	3.1392f;
			arraytTest[168][5] =	3.3490f;
			
			arraytTest[169][0] = 	1.9740f;     			
			arraytTest[169][1] =	2.3485f;
			arraytTest[169][2] =	2.6051f;
			arraytTest[169][3] =  	2.8441f; 		
			arraytTest[169][4] = 	3.1388f;
			arraytTest[169][5] =	3.3487f;
									
									
			arraytTest[170][0] = 	1.9739f;     			
			arraytTest[170][1] =	2.3484f;
			arraytTest[170][2] =	2.6049f;
			arraytTest[170][3] =  	2.8439f; 		
			arraytTest[170][4] = 	3.1386f;
			arraytTest[170][5] =	3.3483f;
			
			arraytTest[171][0] = 	1.9739f;     			
			arraytTest[171][1] =	2.3482f;
			arraytTest[171][2] =	2.6047f;
			arraytTest[171][3] =  	2.8437f; 		
			arraytTest[171][4] = 	3.1383f;
			arraytTest[171][5] =	3.3480f;
									
									
			arraytTest[172][0] = 	1.9738f;     			
			arraytTest[172][1] =	2.3481f;
			arraytTest[172][2] =	2.6046f;
			arraytTest[172][3] =  	2.8435f; 		
			arraytTest[172][4] = 	3.1380f;
			arraytTest[172][5] =	3.3477f;
			
			arraytTest[173][0] = 	1.9737f;     			
			arraytTest[173][1] =	2.3480f;
			arraytTest[173][2] =	2.6044f;
			arraytTest[173][3] =  	2.8433f; 		
			arraytTest[173][4] = 	3.1377f;
			arraytTest[173][5] =	3.3473f;
								
									
			arraytTest[174][0] = 	1.9736f;     			
			arraytTest[174][1] =	2.3478f;
			arraytTest[174][2] =	2.6042f;
			arraytTest[174][3] =  	2.8430f; 		
			arraytTest[174][4] = 	3.1375f;
			arraytTest[174][5] =	3.3470f;
			
			arraytTest[175][0] = 	1.9735f;     			
			arraytTest[175][1] =	2.3477f;
			arraytTest[175][2] =	2.6041f;
			arraytTest[175][3] =  	2.8429f; 		
			arraytTest[175][4] = 	3.1372f;
			arraytTest[175][5] =	3.3466f;
								
									
			arraytTest[176][0] = 	1.9735f;     			
			arraytTest[176][1] =	2.3476f;
			arraytTest[176][2] =	2.6039f;
			arraytTest[176][3] =  	2.8427f; 		
			arraytTest[176][4] = 	3.1369f;
			arraytTest[176][5] =	3.3464f;
			
			arraytTest[177][0] = 	1.9734f;     			
			arraytTest[177][1] =	2.3475f;
			arraytTest[177][2] =	2.6037f;
			arraytTest[177][3] =  	2.8424f; 		
			arraytTest[177][4] = 	3.1366f;
			arraytTest[177][5] =	3.3460f;
									
									
			arraytTest[178][0] = 	1.9733f;     			
			arraytTest[178][1] =	2.3474f;
			arraytTest[178][2] =	2.6036f;
			arraytTest[178][3] =  	2.8423f; 		
			arraytTest[178][4] = 	3.1364f;
			arraytTest[178][5] =	3.3457f;
			
			arraytTest[179][0] = 	1.9732f;     			
			arraytTest[179][1] =	2.3472f;
			arraytTest[179][2] =	2.6034f;
			arraytTest[179][3] =  	2.8420f; 		
			arraytTest[179][4] = 	3.1361f;
			arraytTest[179][5] =	3.3454f;
									
									
			arraytTest[180][0] = 	1.9731f;     			
			arraytTest[180][1] =	2.3471f;
			arraytTest[180][2] =	2.6033f;
			arraytTest[180][3] =  	2.8419f; 		
			arraytTest[180][4] = 	3.1358f;
			arraytTest[180][5] =	3.3451f;
			
			arraytTest[181][0] = 	1.9731f;     			
			arraytTest[181][1] =	2.3470f;
			arraytTest[181][2] =	2.6031f;
			arraytTest[181][3] =  	2.8416f; 		
			arraytTest[181][4] = 	3.1356f;
			arraytTest[181][5] =	3.3448f;
									
									
			arraytTest[182][0] = 	1.9730f;     			
			arraytTest[182][1] =	2.3469f;
			arraytTest[182][2] =	2.6030f;
			arraytTest[182][3] =  	2.8415f; 		
			arraytTest[182][4] = 	3.1354f;
			arraytTest[182][5] =	3.3445f;
			
			arraytTest[183][0] = 	1.9729f;     			
			arraytTest[183][1] =	2.3468f;
			arraytTest[183][2] =	2.6028f;
			arraytTest[183][3] =  	2.8413f; 		
			arraytTest[183][4] = 	3.1351f;
			arraytTest[183][5] =	3.3442f;
									
									
			arraytTest[184][0] = 	1.9729f;     			
			arraytTest[184][1] =	2.3467f;
			arraytTest[184][2] =	2.6027f;
			arraytTest[184][3] =  	2.8411f; 		
			arraytTest[184][4] = 	3.1349f;
			arraytTest[184][5] =	3.3439f;
			
			arraytTest[185][0] = 	1.9728f;     			
			arraytTest[185][1] =	2.3466f;
			arraytTest[185][2] =	2.6025f;
			arraytTest[185][3] =  	2.8409f; 		
			arraytTest[185][4] = 	3.1346f;
			arraytTest[185][5] =	3.3436f;
								
									
			arraytTest[186][0] = 	1.9727f;     			
			arraytTest[186][1] =	2.3465f;
			arraytTest[186][2] =	2.6024f;
			arraytTest[186][3] =  	2.8407f; 		
			arraytTest[186][4] = 	3.1344f;
			arraytTest[186][5] =	3.3433f;
		
			arraytTest[187][0] = 	1.9727f;     			
			arraytTest[187][1] =	2.3463f;
			arraytTest[187][2] =	2.6022f;
			arraytTest[187][3] =  	2.8406f; 		
			arraytTest[187][4] = 	3.1341f;
			arraytTest[187][5] =	3.3430f;
									
									
			arraytTest[188][0] = 	1.9726f;     			
			arraytTest[188][1] =	2.3463f;
			arraytTest[188][2] =	2.6021f;
			arraytTest[188][3] =  	2.8403f; 		
			arraytTest[188][4] = 	3.1339f;
			arraytTest[188][5] =	3.3428f;
			
			arraytTest[189][0] = 	1.9725f;     			
			arraytTest[189][1] =	2.3461f;
			arraytTest[189][2] =	2.6019f;
			arraytTest[189][3] =  	2.8402f; 		
			arraytTest[189][4] = 	3.1337f;
			arraytTest[189][5] =	3.3425f;
								
								
			arraytTest[190][0] = 	1.9725f;     			
			arraytTest[190][1] =	2.3460f;
			arraytTest[190][2] =	2.6018f;
			arraytTest[190][3] =  	2.8400f; 		
			arraytTest[190][4] = 	3.1334f;
			arraytTest[190][5] =	3.3422f;
			
			arraytTest[191][0] = 	1.9724f;     			
			arraytTest[191][1] =	2.3459f;
			arraytTest[191][2] =	2.6017f;
			arraytTest[191][3] =  	2.8398f; 		
			arraytTest[191][4] = 	3.1332f;
			arraytTest[191][5] =	3.3419f;
			
			
									
									
			arraytTest[192][0] = 	1.9723f;     			
			arraytTest[192][1] =	2.3458f;
			arraytTest[192][2] =	2.6015f;
			arraytTest[192][3] =  	2.8397f; 		
			arraytTest[192][4] = 	3.1330f;
			arraytTest[192][5] =	3.3417f;
									
			arraytTest[193][0] = 	1.9723f;     			
			arraytTest[193][1] =	2.3457f;
			arraytTest[193][2] =	2.6014f;
			arraytTest[193][3] =  	2.8395f; 		
			arraytTest[193][4] = 	3.1328f;
			arraytTest[193][5] =	3.3414f;
									
			arraytTest[194][0] = 	1.9722f;     			
			arraytTest[194][1] =	2.3456f;
			arraytTest[194][2] =	2.6013f;
			arraytTest[194][3] =  	2.8393f; 		
			arraytTest[194][4] = 	3.1326f;
			arraytTest[194][5] =	3.3411f;
			
									
			arraytTest[195][0] = 	1.9721f;     			
			arraytTest[195][1] =	2.3455f;
			arraytTest[195][2] =	2.6012f;
			arraytTest[195][3] =  	2.8392f; 		
			arraytTest[195][4] = 	3.1323f;
			arraytTest[195][5] =	3.3409f;
									
			arraytTest[196][0] = 	1.9721f;     			
			arraytTest[196][1] =	2.3454f;
			arraytTest[196][2] =	2.6010f;
			arraytTest[196][3] =  	2.8390f; 		
			arraytTest[196][4] = 	3.1321f;
			arraytTest[196][5] =	3.3406f;
									
			arraytTest[197][0] = 	1.9720f;     			
			arraytTest[197][1] =	2.3453f;
			arraytTest[197][2] =	2.6009f;
			arraytTest[197][3] =  	2.8388f; 		
			arraytTest[197][4] = 	3.1319f;
			arraytTest[197][5] =	3.3403f;
									
			arraytTest[198][0] = 	1.9720f;     			
			arraytTest[198][1] =	2.3452f;
			arraytTest[198][2] =	2.6008f;
			arraytTest[198][3] =  	2.8387f; 		
			arraytTest[198][4] = 	3.1317f;
			arraytTest[198][5] =	3.3401f;
									
			arraytTest[199][0] = 	1.9719f;     			
			arraytTest[199][1] =	2.3451f;
			arraytTest[199][2] =	2.6007f;
			arraytTest[199][3] =  	2.8385f; 		
			arraytTest[199][4] = 	3.1315f;
			arraytTest[199][5] =	3.3398f;
		}
		
		
		
		public boolean testNormality( float w , float s, int n){
			//returns false if needs U test or true if data are normal and use t-test
			boolean flag;
			int row;
			
			if( n < 3){
				System.out.print("false inputs");
				falseImputs = true;
				row=0;
			}
			else if ( ( n >= 3) && ( n <= 20 )  ){
				row = n-3;
				
			}
			else if( (n>20) && ( n <= 100 )  ){
				if(n % 5 == 0){
					row = 18 + n/5;		
				}
				else{
					System.out.print("false inputs");
					falseImputs = true;
					row=0;
				}
				
			}
			else if( (n==150) ){
				row = 34;
			}
			else if( (n==200) ){
				row = 35;
			}
			else if( (n==500) ){
				row = 36;
			}
			else if( (n==1000) ){
				row = 37;
			}
			else{
				System.out.print("false inputs");
				falseImputs = true;
				row=0;
			}
			
			
			 if( falseImputs == false){
				 
					 //check for aValue = 0.000
					 aValue = 0.000f;
					 float a = arrayWS[row][0];
					 float b = arrayWS[row][1];	 
					 float q = w/s;
					 
						if(( q >= a ) && ( q <= b  ) ){
						    flag = true;
						}
						else
							flag = false;
				
				
						 //check for aValue = 0.005
						if(flag == false){	
							  aValue = 0.005f;
							  a = arrayWS[row][2];
							  b = arrayWS[row][3];	 
							  q = w/s;
							 
								if(( q >= a ) && ( q <= b  ) ){
								    flag = true;
								}
								else
									flag = false;
						
							
						}
						
						//check for aValue = 0.01
						if(flag == false){
							 aValue = 0.01f;
							  a = arrayWS[row][4];
							  b = arrayWS[row][5];	 
							  q = w/s;
							 
								if(( q >= a ) && ( q <= b  ) ){
								    flag = true;
								}
								else
									flag = false;
						
							
						}
						//check for aValue = 0.025
						if(flag == false){
							
							 aValue = 0.025f;
							  a = arrayWS[row][6];
							  b = arrayWS[row][7];	 
							  q = w/s;
							 
								if(( q >= a ) && ( q <= b  ) ){
								    flag = true;
								}
								else
									flag = false;
						
							
						}
						//check for aValue = 0.05
						if(flag == false){
							aValue = 0.05f;
							  a = arrayWS[row][8];
							  b = arrayWS[row][9];	 
							  q = w/s;
							 
								if(( q >= a ) && ( q <= b  ) ){
								    flag = true;
								}
								else
									flag = false;
						
							
						}

				
			 }
			 else{
				 flag = false;
			 }
			return flag; // return the result of the test
		}
		
	}
	
	//--------------------------Functions for AutismWindow----------------------FINISH---------------------//
	
	// ----- START of Class drawingWindow ----------------------------------------------------------------//
	public class drawingWindow extends PApplet
	{
		ControlP5 colorButtons, drawingGroups,toolButtons, drawingSliders;
		
		int[] importedColors = new int[8];
		
		//Different Colors
		int black = color(0,0,0);
		int white = color(255,255,255);
		int greyTr = color(124,124,124,100);
		int greyColor = color (124,124,124);
		
		//Color Buttons 
		Button firstColorButton, secondColorButton, thirdColorButton, forthColorButton;
		Button fifthColorButton, sixthColorButton, seventhColorButton, eighthColorButton;
		
		//SplashIconColor
		int initialSplashColor = color(200,200,200);
		
		//Shapes images (tr)
		PImage circle1,circle2,circle3, circle4,circle5;
		PImage rectangle1,rectangle2,rectangle3,rectangle4,rectangle5;
		PImage pencil1,pencil2,pencil3,pencil4,pencil5;
		PImage rubber1,rubber2,rubber3,rubber4,rubber5;
		PImage previous, next;
		PImage clearImage;
		PImage saveImage;
		PImage loadingImage;

		//Previous - next variable 
		UndoRedo imageMemory;
		
		//Blured shape
		PImage bluredShape;
		//Blur flags
		boolean circleFlag = false;
		boolean rectangleFlag = false;
		boolean pencilFlag = false;
		boolean rubberFlag = false;
		
		//Sliders colors
		int sliderBack = color (125,125,125);
		int sliderFore = color (70,70,70);
		int sliderActive = color (70,70,70);
		
		//Tool Buttons MUST be global!
		Button drawCircleButton, drawRectangleButton,drawPencilButton, drawRubberButton;
		//Same for clear button!
		Button clearButton;
		//And for previous-next buttons!
		Button previousButton, nextButton;
		//Save Button
		Button saveImageButton;
		Button loadImageButton;
		
		//Sliders MUST be global!
		Slider circleSlider,rectangleSlider,pencilSlider,rubberSlider;
		
		drawingWindow(int[] colors)
		{
			importedColors = colors;
		}
		
		
		public void settings()
		{
			size(1200,600,P2D);
			
		}

		
		public void setup()
		{					
			surface.setTitle("Painting");
			
			//Call of blur function for the background of chosen tool button
			bluringShape();
			
			//Initialization of ControlP5 objects
			colorButtons = new ControlP5(this);
			drawingGroups = new ControlP5(this);
			toolButtons = new ControlP5(this);
			drawingSliders = new ControlP5(this);
			drawingSliders.setColorForeground(sliderFore);
			drawingSliders.setColorBackground(sliderBack);
			drawingSliders.setColorActive(sliderActive);
			drawingSliders.setColorCaptionLabel(black);
			
			//Groups
			controlP5.Group drawingColorGroup = drawingGroups.addGroup("drawingColorGroup",width/120,height-8*height/60,3*width/120+9*width/24 );
			drawingColorGroup.setBackgroundHeight(7*height/60);
			drawingColorGroup.hideBar();
			drawingColorGroup.setColorForeground(black);
			drawingColorGroup.setColorLabel(white);
			drawingColorGroup.setColorBackground(greyColor);
			drawingColorGroup.setBackgroundColor(greyTr);
			
			PImage splashIcon = loadImage("splash.png");
			
			drawingColorGroup.addDrawable(new CDrawable(){
				public void draw(PGraphics p)
				{
					p.pushMatrix();
					
					p.fill(initialSplashColor);
					p.rect(2*width/120+8*width/24,height/60,width/24,height/12);
					p.image(splashIcon,2*width/120+8*width/24,height/60,width/24,height/12);
					p.popMatrix();
				}
			});
			
			controlP5.Group drawingToolsGroup = drawingGroups.addGroup("drawingToolsGroup",9*width/24 + 5*width/120,height-8*height/60,17*width/120+4*width/24);
			drawingToolsGroup.setBackgroundHeight(7*height/60);
			drawingToolsGroup.hideBar();
			drawingToolsGroup.setColorForeground(black);
			drawingToolsGroup.setColorLabel(white);
			drawingToolsGroup.setColorBackground(greyColor);
			drawingToolsGroup.setBackgroundColor(greyTr);
			
			drawingToolsGroup.addDrawable(new CDrawable(){
				public void draw(PGraphics p)
				{
					p.hint(ENABLE_DEPTH_TEST);
					p.pushMatrix();
					
					if(circleFlag)
					{
						p.image(bluredShape,0,0);
					}
					if(rectangleFlag)
					{
						p.image(bluredShape,4*width/120+width/24,0);
					}
					if(pencilFlag)
					{
						p.image(bluredShape,8*width/120+2*width/24,0);
					}
					if(rubberFlag)
					{
						p.image(bluredShape,12*width/120+3*width/24,0);
					}

					p.fill(initialSplashColor);
					p.rect(width/120,height/60,width/24,height/12);
					p.rect(5*width/120+width/24,height/60,width/24,height/12);
					p.rect(9*width/120+2*width/24,height/60,width/24,height/12);
					drawCircleButton.draw(p);
					drawRectangleButton.draw(p);
					drawPencilButton.draw(p);
					drawRubberButton.draw(p);
					
					p.popMatrix();
					p.hint(DISABLE_DEPTH_TEST);
				}
			});
			
			controlP5.Group memoryGroup = drawingGroups.addGroup("memoryGroup",23*width/120+13*width/24,height-8*height/60,3*width/120+2*width/24);
			memoryGroup.setBackgroundHeight(7*height/60);
			memoryGroup.hideBar();
			memoryGroup.setColorForeground(black);
			memoryGroup.setColorLabel(white);
			memoryGroup.setColorBackground(greyColor);
			memoryGroup.setBackgroundColor(greyTr);
			
			memoryGroup.addDrawable(new CDrawable(){
				public void draw(PGraphics p)
				{
					pushMatrix();
					
					if(previousButton.isMousePressed())
					{
						p.image(bluredShape,0,0);
					}
					if(nextButton.isMousePressed())
					{	
						p.image(bluredShape,width/120+width/24,0);
					}
					previousButton.draw(p);
					nextButton.draw(p);
					popMatrix();
				}
			});
			
			controlP5.Group clearScreenGroup = drawingGroups.addGroup("clearScreenGroup",27*width/120+15*width/24,height-8*height/60,2*width/120+width/24 );
			clearScreenGroup.setBackgroundHeight(7*height/60);
			clearScreenGroup.hideBar();
			clearScreenGroup.setColorForeground(black);
			clearScreenGroup.setColorLabel(white);
			clearScreenGroup.setColorBackground(greyColor);
			clearScreenGroup.setBackgroundColor(greyTr);
			
			clearScreenGroup.addDrawable(new CDrawable(){
				public void draw(PGraphics p)
				{
					pushMatrix();
					
					if(clearButton.isMousePressed())
					{
						p.image(bluredShape,0,0);
					}
					clearButton.draw(p);
					popMatrix();
				}
			});
			
			
			controlP5.Group xGroup = drawingGroups.addGroup("xGroup",35*width/120+15*width/24,height-8*height/60,2*width/120+width/17 );
			xGroup.setBackgroundHeight(7*height/60);
			xGroup.hideBar();
			xGroup.setColorForeground(black);
			xGroup.setColorLabel(white);
			xGroup.setColorBackground(greyColor);
			xGroup.setBackgroundColor(greyTr);
			
			xGroup.addDrawable(new CDrawable(){
				public void draw(PGraphics p)
				{
					pushMatrix();
					/*
					if(saveImageButton.isMousePressed())
					{
						p.image(bluredShape,0,0);
					}
					if(loadImageButton.isMousePressed())
					{
						p.image(bluredShape,0,0);
					}
					loadImageButton.draw(p);
					saveImageButton.draw(p);
					*/
					popMatrix();
				}
			});
			
			
			//Color Buttons
			
			firstColorButton = colorButtons.addButton("FirstColor");
			firstColorButton.setLabel("First Color");
			firstColorButton.setColorForeground(importedColors[0]);
			firstColorButton.setColorBackground(importedColors[0]);
			firstColorButton.setColorCaptionLabel(importedColors[0]);
			firstColorButton.setColorActive(colorStep(importedColors[0]));	
			firstColorButton.setPosition(width/120,height/60);
			firstColorButton.setSize(width/24,height/12);
			firstColorButton.setLabelVisible(false);
			firstColorButton.setGroup(drawingColorGroup);
			
			secondColorButton = colorButtons.addButton("SecondColor");
			secondColorButton.setLabel("Second Color");
			secondColorButton.setColorForeground(importedColors[1]);
			secondColorButton.setColorBackground(importedColors[1]);
			secondColorButton.setColorCaptionLabel(importedColors[1]);
			secondColorButton.setColorActive(colorStep(importedColors[1]));
			secondColorButton.setPosition(width/120+width/24,height/60);
			secondColorButton.setSize(width/24,height/12);
			secondColorButton.setLabelVisible(false);
			secondColorButton.setGroup(drawingColorGroup);
			
			thirdColorButton = colorButtons.addButton("ThirdColor");
			thirdColorButton.setLabel("Third Color");
			thirdColorButton.setColorForeground(importedColors[2]);
			thirdColorButton.setColorBackground(importedColors[2]);
			thirdColorButton.setColorCaptionLabel(importedColors[2]);
			thirdColorButton.setColorActive(colorStep(importedColors[2]));
			thirdColorButton.setPosition(width/120+2*width/24,height/60);
			thirdColorButton.setSize(width/24,height/12);
			thirdColorButton.setLabelVisible(false);
			thirdColorButton.setGroup(drawingColorGroup);
			
			forthColorButton = colorButtons.addButton("ForthColor");
			forthColorButton.setLabel("Forth Color");
			forthColorButton.setColorForeground(importedColors[3]);
			forthColorButton.setColorBackground(importedColors[3]);
			forthColorButton.setColorCaptionLabel(importedColors[3]);
			forthColorButton.setColorActive(colorStep(importedColors[3]));
			forthColorButton.setPosition(width/120+3*width/24,height/60);
			forthColorButton.setSize(width/24,height/12);
			forthColorButton.setLabelVisible(false);
			forthColorButton.setGroup(drawingColorGroup);
			
			fifthColorButton = colorButtons.addButton("FifthColor");
			fifthColorButton.setLabel("Fifth Color");
			fifthColorButton.setColorForeground(importedColors[4]);
			fifthColorButton.setColorBackground(importedColors[4]);
			fifthColorButton.setColorCaptionLabel(importedColors[4]);
			fifthColorButton.setColorActive(colorStep(importedColors[4]));
			fifthColorButton.setPosition(width/120+4*width/24,height/60);
			fifthColorButton.setSize(width/24,height/12);
			fifthColorButton.setLabelVisible(false);
			fifthColorButton.setGroup(drawingColorGroup);
			
			sixthColorButton = colorButtons.addButton("SixthColor");
			sixthColorButton.setLabel("Fifth Color");
			sixthColorButton.setColorForeground(importedColors[5]);
			sixthColorButton.setColorBackground(importedColors[5]);
			sixthColorButton.setColorCaptionLabel(importedColors[5]);
			sixthColorButton.setColorActive(colorStep(importedColors[5]));
			sixthColorButton.setPosition(width/120+5*width/24,height/60);
			sixthColorButton.setSize(width/24,height/12);
			sixthColorButton.setLabelVisible(false);
			sixthColorButton.setGroup(drawingColorGroup);
			
			seventhColorButton = colorButtons.addButton("SeventhColor");
			seventhColorButton.setLabel("Seventh Color");
			seventhColorButton.setColorForeground(importedColors[6]);
			seventhColorButton.setColorBackground(importedColors[6]);
			seventhColorButton.setColorCaptionLabel(importedColors[6]);
			seventhColorButton.setColorActive(colorStep(importedColors[6]));
			seventhColorButton.setPosition(width/120+6*width/24,height/60);
			seventhColorButton.setSize(width/24,height/12);
			seventhColorButton.setLabelVisible(false);
			seventhColorButton.setGroup(drawingColorGroup);
			
			eighthColorButton = colorButtons.addButton("EighthColor");
			eighthColorButton.setLabel("Eighth Color");
			eighthColorButton.setColorForeground(importedColors[7]);
			eighthColorButton.setColorBackground(importedColors[7]);
			eighthColorButton.setColorCaptionLabel(importedColors[7]);
			eighthColorButton.setColorActive(colorStep(importedColors[7]));
			eighthColorButton.setPosition(width/120+7*width/24,height/60);
			eighthColorButton.setSize(width/24,height/12);
			eighthColorButton.setLabelVisible(false);
			eighthColorButton.setGroup(drawingColorGroup);
			
			//Loading shape images
			
			circle1 = loadImage("circle1.png");
			circle2 = loadImage("circle2.png");
			circle3 = loadImage("circle3.png");
			circle4 = loadImage("circle4.png");
			circle5 = loadImage("circle5.png");
			
			circle1.resize(width/24,height/12);
			circle2.resize(width/24,height/12);
			circle3.resize(width/24,height/12);
			circle4.resize(width/24,height/12);
			circle5.resize(width/24,height/12);
			
			rectangle1 = loadImage("rectangle1.png");
			rectangle2 = loadImage("rectangle2.png");
			rectangle3 = loadImage("rectangle3.png");
			rectangle4 = loadImage("rectangle4.png");
			rectangle5 = loadImage("rectangle5.png");
			
			rectangle1.resize(width/24,height/12);
			rectangle2.resize(width/24,height/12);
			rectangle3.resize(width/24,height/12);
			rectangle4.resize(width/24,height/12);
			rectangle5.resize(width/24,height/12);
			
			pencil1 = loadImage("pencil1.png");
			pencil2 = loadImage("pencil2.png");
			pencil3 = loadImage("pencil3.png");
			pencil4 = loadImage("pencil4.png");
			pencil5 = loadImage("pencil5.png");
			
			pencil1.resize(width/24, height/12);
			pencil2.resize(width/24, height/12);
			pencil3.resize(width/24, height/12);
			pencil4.resize(width/24, height/12);
			pencil5.resize(width/24, height/12);
			
			rubber1 = loadImage("rubber1.png");
			rubber2 = loadImage("rubber2.png");
			rubber3 = loadImage("rubber3.png");
			rubber4 = loadImage("rubber4.png");
			rubber5 = loadImage("rubber5.png");
			
			rubber1.resize(width/24, height/12);
			rubber2.resize(width/24, height/12);
			rubber3.resize(width/24, height/12);
			rubber4.resize(width/24, height/12);
			rubber5.resize(width/24, height/12);
			
			previous  = loadImage("previous.png");
			next 	  = loadImage ("next.png");
			
			previous.resize(width/24, height/12);
			next.resize(width/24,height/12);
			
			clearImage = loadImage("clear.png");
			
			clearImage.resize(width/24, height/12);
			
			
			saveImage = loadImage("saveButton.png");
			saveImage.resize(width/32, height/12);
			
			loadingImage = loadImage("loadButton.png");
			loadingImage.resize(width/32, height/12);
			
			//Tool buttons - sliders
			//Circle (button + slider)
			
			drawCircleButton = toolButtons.addButton("drawCircle");
			drawCircleButton.setGroup(drawingToolsGroup);
			drawCircleButton.setPosition(width/120,height/60);
			drawCircleButton.setSize(width/24,height/12);
			drawCircleButton.setLabelVisible(false);
			drawCircleButton.setImage(circle5);
			drawCircleButton.updateSize();
			
			circleSlider = drawingSliders.addSlider("sliderForCircle");
			circleSlider.setGroup(drawingToolsGroup);
			circleSlider.setPosition(2*width/120+width/24,height/60);
			circleSlider.setSize(2*width/120,height/12);
			circleSlider.setRange(1,5);
			circleSlider.setValue(5);
			circleSlider.setNumberOfTickMarks(5);
			circleSlider.showTickMarks(false);
			circleSlider.setLabelVisible(false);
			
			//Rectangle (button+slider)
			drawRectangleButton = toolButtons.addButton("drawRectangle");
			drawRectangleButton.setGroup(drawingToolsGroup);
			drawRectangleButton.setPosition(5*width/120+width/24,height/60);
			drawRectangleButton.setSize(width/24,height/12);
			drawRectangleButton.setLabelVisible(false);
			drawRectangleButton.setImage(rectangle5);
			drawRectangleButton.updateSize();
			
			rectangleSlider = drawingSliders.addSlider("sliderForRectangle");
			rectangleSlider.setGroup(drawingToolsGroup);
			rectangleSlider.setPosition(6*width/120+2*width/24,height/60);
			rectangleSlider.setSize(2*width/120,height/12);
			rectangleSlider.setRange(1, 5);
			rectangleSlider.setValue(5);
			rectangleSlider.setNumberOfTickMarks(5);
			rectangleSlider.showTickMarks(false);
			rectangleSlider.setLabelVisible(false);
			
			//Pencil (button + slider)
			drawPencilButton = toolButtons.addButton("drawPencil");
			drawPencilButton.setGroup(drawingToolsGroup);
			drawPencilButton.setPosition(9*width/120+2*width/24,height/60);
			drawPencilButton.setSize(width/24,height/12);
			drawPencilButton.setLabelVisible(false);
			drawPencilButton.setImage(pencil5);
			drawPencilButton.updateSize();
			
			pencilSlider = drawingSliders.addSlider("sliderForPencil");
			pencilSlider.setGroup(drawingToolsGroup);
			pencilSlider.setPosition(10*width/120+3*width/24,height/60);
			pencilSlider.setSize(2*width/120,height/12);
			pencilSlider.setRange(1,5);
			pencilSlider.setValue(5);
			pencilSlider.setNumberOfTickMarks(5);
			pencilSlider.showTickMarks(false);
			pencilSlider.setLabelVisible(false);
			
			//Rubber (button + slider)
			drawRubberButton = toolButtons.addButton("drawRubber");
			drawRubberButton.setGroup(drawingToolsGroup);
			drawRubberButton.setPosition(13*width/120+3*width/24,height/60);
			drawRubberButton.setSize(width/24,height/12);
			drawRubberButton.setLabelVisible(false);
			drawRubberButton.setImage(rubber5);
			drawRubberButton.updateSize();
			
			rubberSlider = drawingSliders.addSlider("sliderForRubber");
			rubberSlider.setGroup(drawingToolsGroup);
			rubberSlider.setPosition(14*width/120+4*width/24,height/60);
			rubberSlider.setSize(2*width/120,height/12);
			rubberSlider.setRange(1, 5);
			rubberSlider.setValue(5);
			rubberSlider.setNumberOfTickMarks(5);
			rubberSlider.showTickMarks(false);
			rubberSlider.setLabelVisible(false);
			
			//Previous and next buttons
			
			previousButton = toolButtons.addButton("previousButton");
			previousButton.setGroup(memoryGroup);
			previousButton.setPosition(width/120,height/60);
			previousButton.setSize(width/24,height/12);
			previousButton.setLabelVisible(false);
			previousButton.setImage(previous);
			previousButton.updateSize();
			
			nextButton = toolButtons.addButton("nextButton");
			nextButton.setGroup(memoryGroup);
			nextButton.setPosition(2*width/120+width/24,height/60);
			nextButton.setSize(width/24,height/12);
			nextButton.setLabelVisible(false);
			nextButton.setImage(next);
			nextButton.updateSize();
			
			//Clear button
			clearButton = toolButtons.addButton("clearButton");
			clearButton.setGroup(clearScreenGroup);
			clearButton.setPosition(width/120,height/60);
			clearButton.setSize(width/24,height/12);
			clearButton.setLabelVisible(false);
			clearButton.setImage(clearImage);
			clearButton.updateSize();
			
			
			saveImageButton = toolButtons.addButton("saveImageButton");
			saveImageButton.setGroup(xGroup);
			saveImageButton.setPosition(width/240,height/60);
			saveImageButton.setSize(width/24,height/12);
			saveImageButton.setLabelVisible(false);
			saveImageButton.setImage(saveImage);
			saveImageButton.updateSize();
			
			loadImageButton = toolButtons.addButton("loadImageButton");
			loadImageButton.setGroup(xGroup);
			loadImageButton.setPosition(width/240 + width/28,height/60);
			loadImageButton.setSize(width/24,height/12);
			loadImageButton.setLabelVisible(false);
			loadImageButton.setImage(loadingImage);
			loadImageButton.updateSize();
			
			
			/**/
			
			//MUST start with all white.
			
			stroke(255);
		fill(255);
		background(255);
			
			//Initialize of undo/redo object
			imageMemory = new UndoRedo(100);
			
		}
		
		public void exit() {
				  stop();		 
			  drawButton.setVisible(true);
			  passNewColors.setVisible(false);
			  drawWindowFlag = !drawWindowFlag;		
			  imageMemory.images.img = null; 
	
		}
		
		public void draw()
		{
			
			
			//mouseClicked(Event.WINDOW_DESTROY);
			//a.dmouseClicked(Event.WINDOW_DESTROY);
			 
			 /** 
			 */
			hint(ENABLE_DEPTH_TEST);
			pushMatrix();
			
			stroke(255);
			fill(255);
			rect(0,height-4*height/60-height/12,width,4*height/60+height/12);
			
			
			if(mousePressed && circleFlag && mouseY < (height-6*height/60-height/12))
			{
				stroke(initialSplashColor);
				fill(initialSplashColor);
				ellipseMode(CENTER);
				ellipse(mouseX,mouseY,circleSlider.getValue()*1.5f*width/120,circleSlider.getValue()*1.5f*height/60);

			}
			
			if(mousePressed && rectangleFlag && mouseY < (height-6*height/60-height/12))
			{
				stroke(initialSplashColor);
				fill(initialSplashColor);
				rectMode(CENTER);
				rect(mouseX,mouseY,rectangleSlider.getValue()*1.5f*width/120,rectangleSlider.getValue()*1.5f*height/60);

			}
			if(mousePressed && pencilFlag && mouseY < (height-6*height/60-height/12))
			{
				stroke(initialSplashColor);
				fill(initialSplashColor);
				strokeWeight(pencilSlider.getValue());
				line(mouseX,mouseY,pmouseX,pmouseY);

			}	
			
			if(mousePressed && rubberFlag && mouseY < (height-6*height/60-height/12))
			{
				stroke(255);
				fill(255);
				rectMode(CENTER);
				rect(mouseX,mouseY,rubberSlider.getValue()*1.5f*width/120,rubberSlider.getValue()*1.5f*height/60);

			}
			
			
			strokeWeight(1);
			rectMode(CORNER);
			
			
			popMatrix();
			hint(DISABLE_DEPTH_TEST);
		}
		
		public int colorStep(int myColor)
		{
			int r,g,b;
			int newColor = color(0,0,0);
			r = (int) red(myColor);
			g = (int) green(myColor);
			b = (int) blue(myColor);
			
			
			if(r>=g && r>b)
			{
				newColor = color (r,g+50,b+50);
			}
			else if(r>g && r>=b)
			{
				newColor = color (r,g+50,b+50);
			}
			else if(g>=r && g>b)
			{
				newColor = color(r+50,g,b+50);
			}
			else if(g>r && g>=b)
			{
				newColor = color(r+50,g,b+50);
			}
			else if(b>=r && b>g)
			{
				newColor = color(r+50,g+50,b);
			}
			else if(b>r && b>=g)
			{
				newColor = color(r+50,g+50,b);
			}
			else if(r==b && r==g)
			{
				newColor = color(r+50,g+50,b+50);
			}
			else
			{
				newColor = color(r,g,b);
			}
					
			return newColor;
		}
		
		public void bluringShape()
		{
			PGraphics pg = createGraphics(width/24+2*width/120,height/12+2*height/60,P2D);
			pg.beginDraw();
			pg.fill(0,255,0);
			pg.noStroke();
			pg.rectMode(CENTER);
			pg.rect((width/24+2*width/120)/2,(height/12+2*height/60)/2,width/24+2,height/12+2);
			pg.filter(BLUR,9);
			pg.endDraw();
			bluredShape = pg.get();
	
		}
		
		//First Color button function
		public void FirstColor()
		{
			initialSplashColor = importedColors[0];
			
		}
		
		//Second Color button function
		public void SecondColor()
		{
			initialSplashColor = importedColors[1];
		}
		
		//Third Color button function
		public void ThirdColor()
		{
			initialSplashColor = importedColors[2];
		}
		
		//Fourth Color button function
		public void ForthColor()
		{
			initialSplashColor = importedColors[3];
		}
		
		//Fifth Color button function
		public void FifthColor()
		{
			initialSplashColor = importedColors[4];
		}
		
		//Sixth Color button function
		public void SixthColor()
		{
			initialSplashColor = importedColors[5];
		}
		
		//Seventh Color button function
		public void SeventhColor()
		{
			initialSplashColor = importedColors[6];
		}
		
		//Eighth Color button function
		public void EighthColor()
		{
			initialSplashColor = importedColors[7];
		}
		
		//Circle Tool function
		public void drawCircle()
		{
			if(rectangleFlag)
			{
				rectangleFlag = !rectangleFlag;
			}
			if(pencilFlag)
			{
				pencilFlag = !pencilFlag;
			}
			if(rubberFlag)
			{
				rubberFlag = !rubberFlag;
			}
			circleFlag = !circleFlag;
			

		}
		
		//Rectangle Tool function
		public void drawRectangle()
		{
			if(circleFlag)
			{
				circleFlag = !circleFlag;
			}
			if(pencilFlag)
			{
				pencilFlag = !pencilFlag;
			}
			if(rubberFlag)
			{
				rubberFlag = !rubberFlag;
			}
			rectangleFlag = !rectangleFlag;
		}
		
		//Pencil Tool function
		public void drawPencil()
		{
			if(circleFlag)
			{
				circleFlag = !circleFlag;
			}
			if(rectangleFlag)
			{
				rectangleFlag = !rectangleFlag;
			}
			if(rubberFlag)
			{
				rubberFlag = !rubberFlag;
			}
			pencilFlag = !pencilFlag;
		}
		
		//Rubber Tool function
		public void drawRubber()
		{
			if(circleFlag)
			{
				circleFlag = !circleFlag;
			}
			if(rectangleFlag)
			{
				rectangleFlag = !rectangleFlag;
			}
			if(pencilFlag)
			{
				pencilFlag = !pencilFlag;
			}
			rubberFlag = !rubberFlag;
		}
		
		//Previous button function
		public void previousButton()
		{
			imageMemory.undo();
		}
		
		//Next button function
		public void nextButton()
		{	
			imageMemory.redo();
		}
		
		//Clear screen button function
		public void clearButton()
		{
			//redraw();
			
			//reset();
			//stroke(255);
			//fill(255);
			for(int ui=0;ui<10000;ui++){
			imageMemory.undo();
			}
			
			//ASDAT.drawingWindow.this.background(255,255,255);
			//ASDAT.drawingWindow.this.stroke(255,255,255);
			//ASDAT.drawingWindow.this.fill(255,255,255);
		//	windowForDraw.background(255,255,255);
			
			
		}
		
		
		public void saveImageButton()
		{
			//stop();
		
		}
		
		//Circle size slider function
		public void sliderForCircle()
		{
			if(circleSlider.getValue() == 1)
			{
				drawCircleButton.setImage(circle1);
			}
			else if(circleSlider.getValue() == 2)
			{
				drawCircleButton.setImage(circle2);
			}
			else if(circleSlider.getValue() == 3)
			{
				drawCircleButton.setImage(circle3);
			}
			else if(circleSlider.getValue() == 4)
			{
				drawCircleButton.setImage(circle4);
			}
			else
			{
				drawCircleButton.setImage(circle5);
			}
			
		}
		
		//Rectangle size slider function
		public void sliderForRectangle()
		{
			if(rectangleSlider.getValue() == 1)
			{
				drawRectangleButton.setImage(rectangle1);
			}
			else if(rectangleSlider.getValue() == 2)
			{
				drawRectangleButton.setImage(rectangle2);
			}
			else if(rectangleSlider.getValue() == 3)
			{
				drawRectangleButton.setImage(rectangle3);
			}
			else if(rectangleSlider.getValue() == 4)
			{
				drawRectangleButton.setImage(rectangle4);
			}
			else
			{
				drawRectangleButton.setImage(rectangle5);
			}
			
		}
		
		//Pencil size slider function
		public void sliderForPencil()
		{
			if(pencilSlider.getValue() == 1)
			{
				drawPencilButton.setImage(pencil1);
			}
			else if(pencilSlider.getValue() == 2)
			{
				drawPencilButton.setImage(pencil2);
			}
			else if(pencilSlider.getValue() == 3)
			{
				drawPencilButton.setImage(pencil3);
			}
			else if(pencilSlider.getValue() == 4)
			{
				drawPencilButton.setImage(pencil4);
			}
			else
			{
				drawPencilButton.setImage(pencil5);
			}
		}
		
		//Rubber size slider function
		
		public void sliderForRubber()
		{
			if(rubberSlider.getValue() == 1)
			{
				drawRubberButton.setImage(rubber1);
			}
			else if(rubberSlider.getValue() == 2)
			{
				drawRubberButton.setImage(rubber2);
			}
			else if(rubberSlider.getValue() == 3)
			{
				drawRubberButton.setImage(rubber3);
			}
			else if(rubberSlider.getValue() == 4)
			{
				drawRubberButton.setImage(rubber4);
			}
			else
			{
				drawRubberButton.setImage(rubber5);
			}
		}
		

		public void mouseReleased()
		{
			if(mouseY < height-4*height/60-height/12)
			{
				imageMemory.takeSnapshot();
			}
		}
		
		public void getNewColors(int[] colors)
		{
			importedColors = colors;
			
			firstColorButton.setColorForeground(importedColors[0]);
			firstColorButton.setColorBackground(importedColors[0]);
			firstColorButton.setColorCaptionLabel(importedColors[0]);
			firstColorButton.setColorActive(colorStep(importedColors[0]));
			
			secondColorButton.setColorForeground(importedColors[1]);
			secondColorButton.setColorBackground(importedColors[1]);
			secondColorButton.setColorCaptionLabel(importedColors[1]);
			secondColorButton.setColorActive(colorStep(importedColors[1]));
			
			thirdColorButton.setColorForeground(importedColors[2]);
			thirdColorButton.setColorBackground(importedColors[2]);
			thirdColorButton.setColorCaptionLabel(importedColors[2]);
			thirdColorButton.setColorActive(colorStep(importedColors[2]));
			
			forthColorButton.setColorForeground(importedColors[3]);
			forthColorButton.setColorBackground(importedColors[3]);
			forthColorButton.setColorCaptionLabel(importedColors[3]);
			forthColorButton.setColorActive(colorStep(importedColors[3]));
			
			fifthColorButton.setColorForeground(importedColors[4]);
			fifthColorButton.setColorBackground(importedColors[4]);
			fifthColorButton.setColorCaptionLabel(importedColors[4]);
			fifthColorButton.setColorActive(colorStep(importedColors[4]));
			
			sixthColorButton.setColorForeground(importedColors[5]);
			sixthColorButton.setColorBackground(importedColors[5]);
			sixthColorButton.setColorCaptionLabel(importedColors[5]);
			sixthColorButton.setColorActive(colorStep(importedColors[5]));
			
			seventhColorButton.setColorForeground(importedColors[6]);
			seventhColorButton.setColorBackground(importedColors[6]);
			seventhColorButton.setColorCaptionLabel(importedColors[6]);
			seventhColorButton.setColorActive(colorStep(importedColors[6]));
			
			eighthColorButton.setColorForeground(importedColors[7]);
			eighthColorButton.setColorBackground(importedColors[7]);
			eighthColorButton.setColorCaptionLabel(importedColors[7]);
			eighthColorButton.setColorActive(colorStep(importedColors[7]));
		}
		
		
		//Undo Redo Class 
		public class UndoRedo {
	
			int undoSteps=0;
			int redoSteps=0; 
			ImageData images;
			 
			UndoRedo(int levels) 
			{
				images = new ImageData(levels);
			}
			 
			public void takeSnapshot() 
			{
				undoSteps = min(undoSteps+1, images.amount-1);
				redoSteps = 0;
				images.next();
				images.capture();
			}
			
			public void undo() 
			{
				if(undoSteps > 0) 
				{
					undoSteps--;
					redoSteps++;
					images.prev();
					images.show();
				}
			}
			
			public void redo() 
			{
				if(redoSteps > 0) 
				{
					undoSteps++;
					redoSteps--;
					images.next();
					images.show();
				}
			}
		}//END OF CLASS! UndoRedo
			 
		
		//Image memory buffer for undo redo class
		public class ImageData {
			int amount, current;
			PImage[] img;
			ImageData(int amountOfImages) 
			{
				amount = amountOfImages;

				// Initialize all images as copies of the current display
				img = new PImage[amount];
				for (int i=0; i<amount; i++) 
				{
					img[i] = createImage(width, height, RGB);
					img[i] = get();
				}
			}
			
			void next() 
			{
				current = (current + 1) % amount;
			}
			
			void prev() 
			{
				current = (current - 1 + amount) % amount;
			}
			
			void capture() 
			{
				img[current] = get();
			}
			
			void show() 
			{
				image(img[current], 0, 0);
			}
			
		}//END OF CLASS! ImageData

	}//END OF CLASS! DrawingWindow ----------------------------------------------------------------------------
	
	
	public static void main(String _args[]) 
	{
		PApplet.main(new String[] { asdat_16.ASDAT_16.class.getName() });
	}
	
}