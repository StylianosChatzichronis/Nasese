package asdat_server;



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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFileChooser;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import arb.soundcipher.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.awt.Desktop;
import java.awt.Event;
import java.awt.image.BufferedImage;


public class ASDAT_server extends PApplet {

	
	//code for eye blink
	
	 float[] channel_before_mean = new float[16];
	 Boolean flag_for_blink = false;
	
	 public void blink_detection(float[][] data_newest_uV,int start, int n) {
		  float[] sum_before = new float[16];

		  flag_for_blink = false;
		  //print(data_newest_uV.length);
		  for (int i=0; i<data_newest_uV.length; i++) {
			  for (int j=1+start; j<n; j++) {
				   //----- check for eye blink	  
				  //channel_before_mean[i] = data_newest_uV[i][j-1];
				  if (((abs(data_newest_uV[i][j])-channel_before_mean[i]) > 0.1) & ((abs(data_newest_uV[i][j])) > 0.1)) {
					  // println("BLINK");
					  flag_for_blink = true;
				  } 
				  sum_before[i] = sum_before[i] + abs(data_newest_uV[i][j]); 
			  }
			  channel_before_mean[i] = sum_before[i]/250;
		  }
		  
		  if(flag_for_blink == false){
			  println("NO BLINK");
			  
		  }
		  }
	 
	 

	 
	 
	 
	 
	 
	 
	 
	
	
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
	static int[]  colorArray = new int[8];
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
    int packetCounter=0;
    int previousPacket=0;
    byte[] bufferOfThree = new byte[3];
    int[] bufferData= new int[8];
    boolean flagForDraw=false;
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
	
	float[][] SpectrumSum = new float[16][128];
	int[] SpectrumSumCounter = new int[16];
	float[][] SpectrumSumDistance = new float[16][128];
	
	
	boolean flagSoundGroup = false;
	//Sliders
	Slider speedOfSound, meanSampleNumber;
	
	//Start/Stop sound
	Button startSound, stopSound;
	boolean soundIsPlaying = false;
	
	//Start Recording
	Button buttonMIDI, startRecording, stopRecording, saveRecording, readMore,  exitDoc;
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
	List<String> autismAdds = Arrays.asList("Add Patient Group","Add control group","Save Tests");
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
		//float[][]   PvaluesTable1 = new float[6][4];
		String disTest[][] = new String[6][4];
		
		
		float[][]   PvalueFloat		 		= new float[6][4];
		boolean[][] testSignificantFlag 		= new boolean[6][4];
		
		float rankTable[];
	
		String[][] PvalueString = new String[6][4]; 
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
		
		String[] PvalueStringCoherenceDelta = new String[120]; 
		String[] PvalueStringCoherenceTheta = new String[120];
		String[]  PvalueStringCoherenceAlpha = new String[120];
		String[] PvalueStringCoherenceBeta = new String[120];
		
		
		float[]   PvalueFloatCoherenceDelta		 		= new float[120];
		float[]   PvalueFloatCoherenceTheta		 		= new float[120];
		float[]   PvalueFloatCoherenceAlpha		 		= new float[120];
		float[]   PvalueFloatCoherenceBeta		 		= new float[120];
		boolean[] testSignificantFlagCoherenceDelta 		= new boolean[120];
		boolean[] testSignificantFlagCoherenceTheta 		= new boolean[120];
		boolean[] testSignificantFlagCoherenceAlpha 		= new boolean[120];
		boolean[] testSignificantFlagCoherenceBeta 		= new boolean[120];
		
				
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
		
		//initializing the arrays for normality test
		float[][] arrayWS = new float[38][12];
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
		for(int i=0; i<previousIndex.length; i++)
			previousIndex[i] = 0f;
	

		
		noLoop();
		  surface.setTitle("NASESE (Neurocognitive Assessment Software for Enrichment Sensory Environments)");
		
		//createFile();
		for(int i=0; i<750; i++){
			for(int j=0; j< channel_number; j++){
				dataPacket[j][i] = 0f;
		}
			
		}
		for(int i=0; i<256; i++){
		fooData[i] = 0f;
		}
		
	
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
	
	
	
	
	
	public void drawButton() throws IOException
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
		 		}
		 
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
	    	SpectrumSum[i][j] = 0f;
	    	SpectrumSumDistance[i][j] = 0f;
	 	    }
	    	 SpectrumSumCounter[i] = 0;
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
	 	    			meanBuffer[i] = ((float)(maxAmp[0] + maxAmp[8] + maxAmp[10]))/((float)3);
	 	    			
	 	    		}
	 	    		else if( i == 1){    			
	 	    			meanBuffer[i]= (maxAmp[1] + maxAmp[9] + maxAmp[11])/((float)3);	    		
	 	    		}
	 	    		else if( i == 2){    			
	 	    			meanBuffer[i] = (maxAmp[2] + maxAmp[14])/((float)2);	    		
	 	    		}
	 	    		else if( i == 3){    			
	 	    			meanBuffer[i] = (maxAmp[3] + maxAmp[15])/((float)2);	    		
	 	    		}
	 	    		else if( i == 4){    			
	 	    			meanBuffer[i]= (maxAmp[4] + maxAmp[12])/((float)2);	    		
	 	    		}
	 	    		else if( i == 5){    			
	 	    			meanBuffer[i] = (maxAmp[5] + maxAmp[13])/((float)2);	    		
	 	    		}
	 	    		else if( i == 6){    			
	 	    			meanBuffer[i] = maxAmp[6] ;	    		
	 	    		}
	 	    		else if( i == 7){    			
	 	    			meanBuffer[i] = maxAmp[7] ;	    		
	 	    		}	    		
	        		 println("meanBuffer"+i+" ----> "+meanBuffer[i]+" ");
		   	      }
	        	 
	        	
	          	octaveManager();
	            
	    
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
	    
	  JFileChooser file_chooser_MIDI = new JFileChooser(new File("C:\\"));
	  File theFileMIDI;
	  String file_name_MIDI="";
	  public void buttonMIDI(){
	  
		  score.play();
		  score.writeMidiFile("arpeggio.mid");
	  
		  try {
			    SwingUtilities. invokeLater(new Runnable() {
			      public void run() {
			    	  
			    	file_chooser_MIDI.setDialogTitle("Save a file");  
			    	file_chooser_MIDI.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    	FileFilter filter = new FileNameExtensionFilter("MIDI File", "mid");
			    	file_chooser_MIDI.setFileFilter(filter);
			        int return_val = file_chooser_MIDI.showSaveDialog(null);
			        
			        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
			        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
			        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
			        if ( return_val == JFileChooser.APPROVE_OPTION ) {
        
			          File file = file_chooser_MIDI.getSelectedFile();
			          file_name_MIDI = file.getAbsolutePath();
			     
			         
			          Path copy_from_1 = Paths.get("arpeggio.mid");
			          Path copy_to_1 = Paths.get(file_name_MIDI);
			          
			          
			          try {
			              Files.copy(copy_from_1, copy_to_1, REPLACE_EXISTING, COPY_ATTRIBUTES,
			                  NOFOLLOW_LINKS);
			              
			            } catch (IOException e) {			            
			            }   			          
			            print("The file was Saved Successfully!");
				            
			          System.out.println(file_name_MIDI);
			        } else {
			        	file_name_MIDI = "none";
			        }
			      }
			    }
			    );
			  }
			  catch (Exception e) {
			    e.printStackTrace();
			  }
	  }
	  	  
	  //Function for the button controller that starts the recording
	  public void startRecording()
	  {
	    stopRecording.setVisible(true);
	    startRecording.setVisible(false);
	    recorder.beginRecord();
	  }
	  
	//Function for the button controller that stops the recording
	  public void stopRecording()
	  {
	    startRecording.setVisible(true);
	    stopRecording.setVisible(false);
	    recorder.endRecord();
	  }
	  
	//Function for the button controller that stops the recording
	  
	  
	  JFileChooser file_chooser_saveRecording = new JFileChooser(new File("C:\\"));
	  File theFileSaveRec;
	  String file_name_saveRec="";
	  public void saveRecording()
	  {
		  if ( player != null )
		    {
		        player.unpatch( out );
		        player.close();
		    }
	 
		    player = new FilePlayer( recorder.save() );
		    player.patch( out );
		    try {
			    SwingUtilities. invokeLater(new Runnable() {
			      public void run() {
			    	  
			    	file_chooser_saveRecording.setDialogTitle("Save a file");  
			    	file_chooser_saveRecording.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    	FileFilter filter = new FileNameExtensionFilter("Waveform Audio File Format (*.wav)", "wav");
			    	file_chooser_saveRecording.setFileFilter(filter);
			        int return_val = file_chooser_saveRecording.showSaveDialog(null);
			        
			        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
			        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
			        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
			        if ( return_val == JFileChooser.APPROVE_OPTION ) {
         
			          File file = file_chooser_saveRecording.getSelectedFile();
			          file_name_saveRec = file.getAbsolutePath();
   
			         
			          Path copy_from_1 = Paths.get("myrecording.wav");
			          Path copy_to_1 = Paths.get(file_name_saveRec);
			          
			          
			          try {
			              Files.copy(copy_from_1, copy_to_1, REPLACE_EXISTING, COPY_ATTRIBUTES,
			                  NOFOLLOW_LINKS);
			              
			            } catch (IOException e) {			            
			            }
			          	        
			            print("The file was Saved Successfully!");
		
			            
			          System.out.println(file_name_saveRec);
			        } else {
			        	file_name_saveRec = "none";
			        }
			      }
			    }
			    );
			  }
			  catch (Exception e) {
			    e.printStackTrace();
			  }
		    
		    
	  }
	  
	  
	  
	  JFileChooser file_chooserRec = new JFileChooser(new File("C:\\"));
	  File theFileRec;
	  String file_name_rec="";
	  public void saveFileRec(){
				 
			  try {
			    SwingUtilities. invokeLater(new Runnable() {
			      public void run() {
			    	  
			    	file_chooserRec.setDialogTitle("Save a file");  
			    	file_chooserRec.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    	FileFilter filter = new FileNameExtensionFilter("Waveform Audio File Format (*.wav)", "wav");
			    	file_chooserRec.setFileFilter(filter);
			        int return_val = file_chooserRec.showSaveDialog(null);
			        
			        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
			        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
			        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
			        if ( return_val == JFileChooser.APPROVE_OPTION ) {
			        	
			        	File file = new File("rec.wav");
			        	BufferedWriter writer;
						try {
							writer = new BufferedWriter( new FileWriter( file.getAbsolutePath()+".wav"));
						
			          
			          file.getAbsolutePath();		          
			          file = file_chooserRec.getSelectedFile();		       
			          file_name_rec = file.getAbsolutePath();   
			         // need = file.getAbsolutePath(); 
			         // AudioRecorder tempRecorder = minimWav.createRecorder(in, "uii.wav");
			          writer.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  
			          System.out.println(file_name_rec);
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
	      if(meanBuffer[channel] >= 0f && meanBuffer[channel] <= 2f)
	      {
	        pianoSamples[channelOctave[channel]-1][0].setGain(channelVolume[0]);
	        pianoSamples[channelOctave[channel]-1][0].trigger();
	        print("C" + (channelOctave[channel]) + " ");
	        
	        //soundCipher
	        pitches[channel] = 60;
    	    
	      }
	      else if(meanBuffer[channel] > 2f && meanBuffer[channel] <= 7.4599245f)
	      {
	        pianoSamples[channelOctave[channel]-1][1].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][1].trigger();
	        print("Db" + (channelOctave[channel]) + " ");
    	    pitches[channel] = 61;
	      }
	      else if(meanBuffer[channel] > 7.4599245f && meanBuffer[channel] <= 11.469654f)
	      {
	        pianoSamples[channelOctave[channel]-1][2].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][2].trigger();
	        print("D" + (channelOctave[channel]) + " ");
	        pitches[channel] = 62;
	      }
	      else if(meanBuffer[channel] > 11.469654f && meanBuffer[channel] <= 15.7177125f)
	      {
	        pianoSamples[channelOctave[channel]-1][3].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][3].trigger();
	        print("Eb" + (channelOctave[channel]) + " ");
    	    pitches[channel] = 63;
	      }
	      else if(meanBuffer[channel] > 15.7177125f && meanBuffer[channel] <= 20.2183695f)
	      {
	        pianoSamples[channelOctave[channel]-1][4].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][4].trigger();
	        print("E" + (channelOctave[channel]) + " ");
	    
	        pitches[channel] = 64;
	      }
	      else if(meanBuffer[channel] > 20.2183695f && meanBuffer[channel] <= 24.986745f)
	      {
	        pianoSamples[channelOctave[channel]-1][5].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][5].trigger();
	        print("F" + (channelOctave[channel]) + " ");
	
	        pitches[channel] = 65;
	      }
	      else if(meanBuffer[channel] > 24.986745f && meanBuffer[channel] <= 30.0386205f)
	      {
	        pianoSamples[channelOctave[channel]-1][6].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][6].trigger();
	        print("Gb" + (channelOctave[channel]) + " ");
	  
	        pitches[channel] = 66;
	      }
	      else if(meanBuffer[channel] > 30.0386205f && meanBuffer[channel] <= 35.3909115f)
	      {
	        pianoSamples[channelOctave[channel]-1][7].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][7].trigger();
	        print("G" + (channelOctave[channel]) + " ");
	   
    	    pitches[channel] = 67;
	      }
	      else if(meanBuffer[channel] > 35.3909115f && meanBuffer[channel] <= 41.0614785f)
	      {
	        pianoSamples[channelOctave[channel]-1][8].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][8].trigger();
	        print("Ab" + (channelOctave[channel]) + " ");
	   
    	    pitches[channel] = 68;
	      }
	      else if(meanBuffer[channel] > 41.0614785f && meanBuffer[channel] <= 47.0692215f)
	      {
	        pianoSamples[channelOctave[channel]-1][9].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][9].trigger();
	        print("A" + (channelOctave[channel]) + " ");
	      
    	    pitches[channel] = 69;
	      }
	      else if(meanBuffer[channel] > 47.0692215f && meanBuffer[channel] <= 53.4341745f)
	      {
	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][10].trigger();
	        print("Bb" + (channelOctave[channel]) + " ");
	    
    	    pitches[channel] = 70;
    	  
	      }
	      else if(meanBuffer[channel]> 53.4341745f && meanBuffer[channel] <= 60.0f)
	      {
	        pianoSamples[channelOctave[channel]-1][11].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][11].trigger();
	        print("B" + (channelOctave[channel]) + " ");
	  
	        pitches[channel] = 71;
	      }
	      else if(meanBuffer[channel]> 60.0f && meanBuffer[channel] <= 65.41f){
	    	    pianoSamples[channelOctave[channel]-1][0].setGain(channelVolume[channel]);
		        pianoSamples[channelOctave[channel]-1][0].trigger();
		        print("C" + (channelOctave[channel]) + " ");
		        pitches[channel] = 60;
	      }
	      else if(meanBuffer[channel] > 65.41f && meanBuffer[channel] <= 69.30f)
	      {
	        pianoSamples[channelOctave[channel]-1][1].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][1].trigger();
	        print("Db" + (channelOctave[channel]) + " ");
	    
	      }
	      else if(meanBuffer[channel] > 69.30f && meanBuffer[channel] <= 73.42f)
	      {
	        pianoSamples[channelOctave[channel]-1][2].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][2].trigger();
	        print("D" + (channelOctave[channel]) + " ");
	   
	        pitches[channel] = 62;
	      }
	      else if(meanBuffer[channel] > 73.42f && meanBuffer[channel] <= 77.78f)
	      {
	        pianoSamples[channelOctave[channel]-1][3].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][3].trigger();
	        print("Eb" + (channelOctave[channel]) + " ");
	   
    	    pitches[channel] = 63;
	      }
	      else if(meanBuffer[channel] > 77.78f && meanBuffer[channel] <= 82.41f)
	      {
	        pianoSamples[channelOctave[channel]-1][4].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][4].trigger();
	        print("E" + (channelOctave[channel]) + " ");
	 
	        pitches[channel] = 64;
	      }
	      else if(meanBuffer[channel] > 82.41f&& meanBuffer[channel] <= 87.31f)
	      {
	        pianoSamples[channelOctave[channel]-1][5].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][5].trigger();
	        print("F" + (channelOctave[channel]) + " ");
	
	        pitches[channel] = 65;
	      }
	      else if(meanBuffer[channel] > 87.31f && meanBuffer[channel] <= 92.50f)
	      {
	        pianoSamples[channelOctave[channel]-1][6].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][6].trigger();
	        print("Gb" + (channelOctave[channel]) + " ");
	  
	        pitches[channel] = 66;
	      }
	      else if(meanBuffer[channel] > 92.50f && meanBuffer[channel] <= 98.00f)
	      {
	        pianoSamples[channelOctave[channel]-1][7].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][7].trigger();
	        print("G" + (channelOctave[channel]) + " ");
	    
    	    pitches[channel] = 67;
	      }
	      else if(meanBuffer[channel] > 98.00f && meanBuffer[channel] <= 103.83f)
	      {
	        pianoSamples[channelOctave[channel]-1][8].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][8].trigger();
	        print("Ab" + (channelOctave[channel]) + " ");
	      
    	    pitches[channel] = 68;
	      }
	      else if(meanBuffer[channel] > 103.83f && meanBuffer[channel] <= 110.00f)
	      {
	        pianoSamples[channelOctave[channel]-1][9].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][9].trigger();
	        print("A" + (channelOctave[channel]) + " ");
	    
    	    pitches[channel] = 69;
	      }
	      else if(meanBuffer[channel] > 116.54f && meanBuffer[channel] <= 123.47f)
	      {
	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
	        pianoSamples[channelOctave[channel]-1][10].trigger();
	        print("Bb" + (channelOctave[channel]) + " ");
	   
    	    pitches[channel] = 70;
	      }
    	  else if(meanBuffer[channel] > 123.47f && meanBuffer[channel] <= 130.81f)
  	      {
  	        pianoSamples[channelOctave[channel]-1][10].setGain(channelVolume[channel]);
  	        pianoSamples[channelOctave[channel]-1][10].trigger();
  	        print("C" + (channelOctave[channel]) + " ");
  	     
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
	    			musicDataBuffer[0][musicDataBufferCounter] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/((float)3);
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][musicDataBufferCounter] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/((float)3);	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][musicDataBufferCounter] = (maxAmp[2] + maxAmp[14])/((float)2);	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][musicDataBufferCounter] = (maxAmp[3] + maxAmp[15])/((float)2);	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][musicDataBufferCounter] = (maxAmp[4] + maxAmp[12])/((float)2);	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][musicDataBufferCounter] = (maxAmp[5] + maxAmp[13])/((float)2);	    		
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
	    			musicDataBuffer[0][musicDataBufferCounter] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/((float)2);
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][musicDataBufferCounter] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/((float)3);	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][musicDataBufferCounter] = (maxAmp[2] + maxAmp[14])/((float)2);	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][musicDataBufferCounter] = (maxAmp[3] + maxAmp[15])/((float)2);	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][musicDataBufferCounter] = (maxAmp[4] + maxAmp[12])/((float)2);	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][musicDataBufferCounter] = (maxAmp[5] + maxAmp[13])/((float)2);	    		
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
	 
	    }
	    else
	    {
	    	
	    if (   ( playbackLiveFlag == true) && ( playbackFlag == true)  &&  ((thisTime - startTime) >=1000) ){
	      for(int i=0; i< 8;i++)
	      {
	        for(int j=0;j<49;j++)
	        {
	          musicDataBuffer[i][j]=musicDataBuffer[i][j+1];
	        }
	      }
	      
	     
	      for(int i = 0; i <8; i++)
	      {
	        //EDO GEMIZEIS TON BUFFER TIS MOUSIKIS SAN OURA
	    		if(i==0){ 
	    			musicDataBuffer[0][49] = (maxAmp[0] + maxAmp[8] + maxAmp[10])/((float)3);
	    		}
	    		else if( i == 1){    			
		    			musicDataBuffer[1][49] = (maxAmp[1] + maxAmp[9] + maxAmp[11])/((float)3);	    		
	    		}
	    		else if( i == 2){    			
	    			musicDataBuffer[2][49] = (maxAmp[2] + maxAmp[14])/((float)2);	    		
	    		}
	    		else if( i == 3){    			
	    			musicDataBuffer[3][49] = (maxAmp[3] + maxAmp[15])/((float)2);	    		
	    		}
	    		else if( i == 4){    			
	    			musicDataBuffer[4][49] = (maxAmp[4] + maxAmp[12])/((float)2);	    		
	    		}
	    		else if( i == 5){    			
	    			musicDataBuffer[5][49] = (maxAmp[5] + maxAmp[13])/((float)2);	    		
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
	          meanBuffer[i] = (float)((float)sum/(float)(musicDataBufferCounter));
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
	          meanBuffer[i] = ((float)sum/((float)numberOfSamples));
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
	        meanBuffer[i] = ((float)sum/((float)numberOfSamples));
	        sum = 0;
	      }
	    }
	    else
	    {
	      println("impossible!");
	    }
	    
	    //TESTING MEAN VALUES!    
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
			maxAmp[i] = 0f;
		
		
		MuToggle = new ControlP5(this);
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
			               .setRange(-0.2f, 0.2f)
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
						   .setRange(-0.2f, 0.2f)
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
					//print("IN--------------SIDE ");
					first = true;
			           flagForDraw=false;
			           int n,start;
			           
			           
			           if( flagBuffer == 1 ){
			        	   start =  0;
			                n     = 250;   
			           }
			           else if( flagBuffer ==2 ) {        
			           
			           start = 250;
		                n     = 500;
		                
			           }else{
			        	   start = 500;
			               n     = 750;
	   
			           }
		           		blink_detection(dataPacket,start,n);
		           	    //blink and display
			           
			           
			           
			           
			           
			           if( flagBuffer == 1 ){
			               
			                
			                if(playbackFlag==false){
			                for(int io=start;io<n;io++){
					               for(int y = 0; y< 16 ;y++){    
			            			   
		    		            		output.write(dataPacket[y][io]+" ");            
		    		            	}
					               output.write("\r\n"); 
					               }
			                }
			               	
			                
			                		
					                if( flagFilterUsed1 == false){
					                	 flagFilterUsed1 = true;
						                 for(  int j = 0; j < channel_number; j++){  
							                 filter_function(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
							                 filter_function(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass		                 
							                 		
							                 filter_function(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
							                 filter_function(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
							                 filter_function(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
							                 filter_function( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
							                 filter_function(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
							     
							                 filter_function(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]); 
						                 }
						                 //println("flagBuffer A");
						             	}
					                
			               }
			              else if( flagBuffer ==2 ) {          	 
			                
			                
			                if(playbackFlag==false){
			                for(int io=start;io<n;io++){
					               for(int y = 0; y< 16 ;y++){    
			            			   
		    		            		output.write(dataPacket[y][io]+" ");            
		    		            	}
					               output.write("\r\n"); 
					               }
			                }
    			            			
			                
					                if( flagFilterUsed2 == false){
					                	flagFilterUsed2 = true;
						                 for(  int j = 0; j < channel_number; j++){  
							                 filter_function(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
							                 filter_function(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass
							                 
							                 filter_function(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
						                	 filter_function(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
						                	 filter_function(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
						                	 filter_function( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
						                	 filter_function(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
								                 
						                	 filter_function(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);  
							              }
						                // println("flagBuffer B");
						             	}
					               
			              }
			               else {
			            	  
				               if(playbackFlag==false){
				               for(int io=start;io<n;io++){
				            	   for(int y = 0; y< 16 ;y++){    
		            			   
	    		            		output.write(dataPacket[y][io]+" ");            
	    		            	}
				               output.write("\r\n"); 
				               }
				               }
	    			            			
				            	   if(flagFilterUsed3 == false){
				            		   flagFilterUsed3 = true;
				            		   for(  int j = 0; j < channel_number; j++){  
				  		                filter_function(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); //notch
				  		                filter_function(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]); //bandpass				  		              
				  		                filter_function(deltaProperties[currentDelta].b, deltaProperties[currentDelta].a, dataPacket[j]);
				                		filter_function(thetaProperties[currentTheta].b, thetaProperties[currentTheta].a, dataPacket[j]); 						        
				                		filter_function(alphaProperties[currentAlpha].b, alphaProperties[currentAlpha].a, dataPacket[j]); 			       
				                		filter_function( betaProperties[currentBeta ].b, betaProperties[  currentBeta].a, dataPacket[j]); 						          
				                		filter_function(gammaProperties[currentGamma].b, gammaProperties[currentGamma].a, dataPacket[j]);
						                   
				                		filter_function(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
				                		
				  		                 }
				            		  // println("flagBuffer C");
					             	}
				            	   
			   				}
			           
			           
			           		
			           		System.out.println("----------------------------------------------------------------");
			           		System.out.println();
			           			
		//if blink dont update anything	           		
		if(flag_for_blink==false){
			           				for(  int i=start; i < n; i++){
			           					for (int currChannel=0; currChannel < channel_number; currChannel++) {	 		                	           	 
			           					myChart[currChannel].push(dataset[currChannel], dataPacket[currChannel][i]);	       		
			           				}
			           					
			           			}        				
			           updateLights();
			           
			           
			           
			             
			      float     spectrumScale   = 10f; 
	
		         
			      
		                  float[] fooData;
		                  
		                  for(int i=0;i< channel_number;i++){	    		    		   
		  		  		    maxAmp[i] = 0f;	 
		  		  		    }     
		             for (int currChannel=0; currChannel < channel_number; currChannel++) {	
					            //do what to do for each channel 	 		            	 
					              fftBuff[currChannel] = new FFT(Nfft, fs_Hz);  
					              fftBuff[currChannel].window(FFT.HAMMING);	         
					              fooData = dataPacket[currChannel];
					              float highestDifferenceSpectrumDer = 0f; 
					              
					         
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
						          
						                  p.stroke(color(255,255,255));
						                }
						                else
						                {
						                	 p.stroke(color(255,255,255));
						                }
						            
						                float trew = fftLin.getBand(i);					               
					                    trew = trew*spectrumScale;
					                    
						               if(trew>height/9.6f )
						            	   trew=height/9.6f;  
						              
						              
						                p.line(11 + i, p.height/2 + p.height/3  + p.height/9.65f , 11 + i, p.height/2 + p.height/3 + p.height/9.65f - trew);				                						      
						              }
						            	   
		              
		              fill(0);
		     
		              
		              highestDifferenceSpectrumDer = 0f;
		              float meanCurrentAmp=0,comp2=0,comp=0,meanDistance;
		              for(int i = 0; i < fftBuff[currChannel].specSize()/2; i++)
		              {
		            	  //check for the highest standard deviation 
		            	  	 
	            	  	  //formula for comparison  currAmp/meanCurrentAmp is the most different)
		            	  //sumOfPreviousData in the i band of currChannel channel
	            	  	  meanCurrentAmp = abs( SpectrumSum[currChannel][i]/ (float)(SpectrumSumCounter[currChannel] +1f ) );       	  	  
	            	  	  meanDistance = abs( SpectrumSumDistance[currChannel][i]/ (float)(SpectrumSumCounter[currChannel] +1f ) );       	  	  
	            	  	  //if amplitude is not 0
		            	  	
		            	  	  if(SpectrumSum[currChannel][i]!=0f){
		            	  		  	//(currAmp - mean) / 
		            	  		  		//SpectrumSumDistance[currChannel][i]
		            	  		  	 comp2 = abs( fftBuff[currChannel].getBand(i) / meanCurrentAmp );
		            	  		  	comp = abs( (fftBuff[currChannel].getBand(i) - meanCurrentAmp) /  meanDistance );
		            	  		  
		            	  		  	 if(( abs(comp) == 0f)&&( fftBuff[currChannel].getBand(i) != 0f ) ){
				            	  		 maxAmp[currChannel] =  i;				            	
				            	  		 highestDifferenceSpectrumDer =   comp;
				            	  	  }
			            	  		 else  if(( abs(comp) == 0f)&&( fftBuff[currChannel].getBand(i) == 0f ) ){
			            	  			maxAmp[currChannel] =  i;
			            	  	
			            	  		 }
				            	  	  else if( comp > highestDifferenceSpectrumDer ){				
						            	  maxAmp[currChannel] =  i;
						            	
						            	  highestDifferenceSpectrumDer =  comp;
						            	 
				            	  	  }
		            	  	  }
		            	  	  else{
		            	  		//print("start when all are 0 i="+i+" ");
		            	  		 maxAmp[currChannel] =  i;
		            	  		 highestDifferenceSpectrumDer = abs( SpectrumSumDistance[currChannel][i] /  meanDistance );;
		            	  	  }
		            	  	 
		            	  	  
				               
		            	  	SpectrumSum[currChannel][i] = abs( fftBuff[currChannel].getBand(i)) + SpectrumSum[currChannel][i]; 
		            	  	SpectrumSumDistance[currChannel][i] = abs( fftBuff[currChannel].getBand(i) - meanCurrentAmp ) +SpectrumSumDistance[currChannel][i];  
		              }
     
		          
		              SpectrumSumCounter[currChannel]++;
			                 
			                 
		             }  
	
			       flagSoundGroup = true;      
			    }    
			           		
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
	            
	                      p.stroke(color(255,255,255));
	                    }
	                    else
	                    {
	                        p.stroke(color(255,255,255));
	                    }
	                 
	                    float trew =fftLin.getBand(i);
	                    trew = trew;
	                    trew = trew*spectrumScale;
	                   
			            
	                    if(trew>height/9.6f )
			            	   trew=height/9.6f; 
	                   
	                   
			                p.line(11 + i, p.height/2 + p.height/3 + p.height/9.65f , 11 + i, p.height/2 + p.height/3 + p.height/9.65f - trew);			
	                  }
	                  
	                  fill(0);
	           
	
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
	  			
			
	  				  
	                 
	                  p.fill(0,0, 0);
		  			  p.stroke(0,0,0);
		  		      p.textSize(11);
		  		 
	                  p.text((dataPacket[0][0]*1000f), p.width/20f + p.width/180f , p.height/10 + p.height/100 ); 
	                  p.text((dataPacket[1][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/10 + p.height/100 );
	                  p.text((dataPacket[2][0]*1000f), p.width/20f + p.width/180f , p.height/5 + p.height/100 + p.height/400);
	                  p.text((dataPacket[3][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/5 + p.height/100 + p.height/400);
	                  p.text((dataPacket[4][0]*1000f), p.width/20f + p.width/180f , p.height/4 + p.height/16 );
	                  p.text((dataPacket[5][0]*1000f), p.width/5f + p.width/20f + p.width/48f , p.height/4 + p.height/16 );
	                  p.text((dataPacket[6][0]*1000f), p.width/20f + p.width/180f 		   ,  p.height/4 + p.height/8 + p.height/25 );
	                  p.text((dataPacket[7][0]*1000f), p.width/5f + p.width/20f + p.width/48f ,  p.height/4 + p.height/8 + p.height/25 );
	                  
	               
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
	
	
	void filter_function(double[] filt_b, double[] filt_a, float[] data) {
		  double[] y = new double[filt_b.length];
		  double[] x = new double[filt_b.length];
		  
		  //step through data points
		  for (int i = 0; i < data.length; i++) {   
		    //shift the previous outputs
		    for (int j = filt_b.length-1; j > 0; j--) {
		      y[j] = y[j-1];
		      x[j] = x[j-1];
		    }
		    
		    //add in the new point
		    x[0] = data[i];
		    
		    //compute the new data point
		    double out = 0;
		    for (int j = 0; j < filt_b.length; j++) {
		      out += filt_b[j]*x[j];
		      if (j > 0) {
		        out -= filt_a[j]*y[j];
		      }
		    }
		    
		    //save output value
		    y[0] = out;
		    data[i] = (float)out;
		  }
		}
	
	
	private void initializeFiltersters() {	  
	    double[] bandpass_up, bandpass_down, notch_down, notch_up;	   

	   //Notch filters	  
	    for (int i=0; i < notchProperties.length; i++) {	      
		        if( i==0 ){
			          notch_down = new double[] { 9.650809863447347e-001, -2.424683201757643e-001, 1.945391494128786e+000, -2.424683201757643e-001, 9.650809863447347e-001 };
			          notch_up = new double[] { 1.000000000000000e+000, -2.467782611297853e-001, 1.944171784691352e+000, -2.381583792217435e-001, 9.313816821269039e-001  }; 			        
			        }
		        else if( i==1 ){	         
		        	notch_down = new double[] { 0.96508099, -1.19328255,  2.29902305, -1.19328255,  0.96508099 };
		        	notch_up = new double[] { 1.0       , -1.21449348,  2.29780334, -1.17207163,  0.93138168 }; 			          
			        }
		        else{	   
			          //no notch filter
		        	notch_down = new double[] { 1.0 };
		        	notch_up = new double[] { 1.0 };		         
			        }        
		        notchProperties[i] =  new filterClass(notch_down, notch_up);
	    } 
	  
	    for (int i=0;i<BPproperties.length;i++) {
	      //Bandpass filters
	      
	        if(i == 0 ){
	        	bandpass_up = new double[] { 2.001387256580675e-001, 0.0f, -4.002774513161350e-001, 0.0f, 2.001387256580675e-001};
	        	bandpass_down = new double[] { 1.0f, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};          
		        }
	        else if(i == 1 ){
	        	bandpass_up = new double[] {  5.129268366104263e-003, 0.0f, -1.025853673220853e-002, 0.0f, 5.129268366104263e-003};
	        	bandpass_down = new double[] { 1.0f, -3.678895469764040e+000, 5.179700413522124e+000, -3.305801890016702e+000, 8.079495914209149e-001};	         	               
	        }
		    else if(i == 2 ){
		    	bandpass_up = new double[] { 1.173510367246093e-001, 0.0f, -2.347020734492186e-001, 0.0f, 1.173510367246093e-001};
		    	bandpass_down = new double[] { 1.0f, -2.137430180172061e+000, 2.038578008108517e+000, -1.070144399200925e+000, 2.946365275879138e-001}; 
	        }
            else if(i == 3 ){
            	bandpass_up = new double[] {  1.750876436721012e-001, 0.0f, -3.501752873442023e-001, 0.0f, 1.750876436721012e-001};       
            	bandpass_down = new double[] {  1.0f, -2.299055356038497e+000, 1.967497759984450e+000, -8.748055564494800e-001, 2.196539839136946e-001};
		    }
	        else if(i == 4 ){
	        	bandpass_up = new double[]{ 8.9819001234212e-002f, 0.0f, -1.79638002468425e-001f, 0.0f, 8.9819001234212e-002f};
	        	bandpass_down = new double[]{ 1.0f, -2.983022136008243e+000f, 3.321149361017554e+000f, -1.689322898421e+000f, 3.51216492642182e-001f};	          
	        }
  	       else{        	      
	          //no filtering
  	    	 bandpass_up = new double[] {1.0};
  	    	bandpass_down = new double[] {1.0};
	  	    }     
	      BPproperties[i] =  new filterClass(bandpass_up, bandpass_down);
	    } //end of Bandpass filters

	    
	    for (int i=0;i<deltaProperties.length;i++) {
		   
	    	 if (i == 0) {
		     
		          //butter(2,[0.5 4]/(250/2));  %delta band filter
	    		  bandpass_up = new double[] { 0.001820128710717, 0.0f, -0.003640257421435, 0.0f, 0.001820128710717};
		          bandpass_down = new double[] { 1.0f, -3.873296116978567, 5.629767675866390, -3.639496144117842, 0.883026086553440};          
	    	 }
	    	 else{		       
		          //no filtering
	    		  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		      }  		    
		      deltaProperties[i] =  new filterClass(bandpass_up, bandpass_down);
		    }
	    
	    for (int i=0;i<thetaProperties.length;i++) {
		     
		      if (i == 0) {
		    
		          //butter(2,[4 8]/(250/2));  %theta band filter
		    	  bandpass_up = new double[] { 0.002357208772847, 0.0f, -0.004714417545693f, 0f, 0.002357208772847f};
		          bandpass_down = new double[] { 1.0f, -3.819084908499169,5.508698361351956, -3.55670569986647f, 0.86747213379167f};          
		      }      
		      else{
		          //no filtering
		    	  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		      }  		    
		      thetaProperties[i] =  new filterClass(bandpass_up, bandpass_down);
		    } 
	    
	    
	    for (int i=0; i < alphaProperties.length; i++) {
		
	    	if (i == 0) {
		          //butter(2,[8 12]/(250/2));  %alpha band filter
	    		  bandpass_up = new double[] { 0.002357208772854f, 0.0f, -0.004714417545708f, 0.0f, 0.002357208772854f};
		          bandpass_down = new double[] { 1.0f, -3.741561528598107, 5.361993738129343f, -3.484508340087064f, 0.867472133791671};          
	    	}			      
	    	else{
		          //no filtering
	    		  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		    }  		    
		    alphaProperties[i] =  new filterClass(bandpass_up, bandpass_down);
		    } 
	    
	    
	    
	    for (int i=0;i<betaProperties.length;i++) {
		
	    	if (i == 0) {
		          //butter(2,[13 25]/(250/2));  %bandpass filter
	    		  bandpass_up = new double[] { 0.018650396227835, 0.0f, -0.037300792455670, 0.0f, 0.018650396227835};
		          bandpass_down = new double[] { 1.0f, -3.214440261194531, 4.185707917233462, -2.590707106197416, 0.652837763407545};          
	    	}
	    	else{	    		
		          //no filtering
	    		  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		      }  		    
		    betaProperties[i] =  new filterClass(bandpass_up, bandpass_down);
		    } 
	    
	    
	    for (int i=0;i<gammaProperties.length;i++) {
		 
	    	if (i == 0) {
		          //butter(2,[25 100]/(250/2));  %bandpass filter
	    		  bandpass_up = new double[] { 0.391335772501769, 0.0f, -4.002774513161350e-001, 0.0f, 0.391335772501769};
		          bandpass_down = new double[] { 1.0f, -1.998401444325282e-15, -0.369527377351241, 7.494005416219807e-16, 0.195815712655833};          
	    	}
	    	else{		       
		          //no filtering
	    		  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		      }  		    
		    gammaProperties[i] =  new filterClass(bandpass_up, bandpass_down);
		    } 
	
	    for (int i=0;i < muProperties.length;i++) {
	    	if (i == 0) {
		          // butter(2,[7.5 12.5]/(250/2))  %bandpass filter
	    		  bandpass_up = new double[] { 0.003621681514928f,0f,-0.007243363029856f,0f,0.003621681514928f};
		          bandpass_down = new double[] { 1.0f, -3.709918611606726f, 5.269811129902000f, -3.393888777086079f, 0.837181651256023f};          
	    	}
	    	else{		       
		          //no filtering
	    		  bandpass_up = new double[] {1.0};
		          bandpass_down = new double[] {1.0};
		      }  		
	    	muProperties[i] =  new filterClass(bandpass_up, bandpass_down);
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
							openFileWriter();							
							counterfromzero = 0;
							if(toggleMu.getBooleanValue() == false)
								EnablePort();
							else{
								MuMode();
							}
								
							tempTimeCounter = millis();
							timeCounter = tempTimeCounter - millis();
					
						}
						else
					      {
							output.close();
							state=false;
					        stopSendingData();
					        delay(10);    
					        ClosePort();    
					        delay(1000); // delay to ensure that the last data were drawn
					        flagBuffer 		= -1;
							flagForDraw		= false;
							
							flagFilterUsed1 = false;
							flagFilterUsed2 = false;
							flagFilterUsed3 = false;
							
					
					      }							
				}
			else{
				if(playbackTrigger == false){
					playbackFormatter();
					playbackFlag = true;				
					startTime = millis();
					playbackTrigger = true;
					
					tempTimeCounter = millis();
					timeCounter = tempTimeCounter - millis();
			
				}
				else{
					playbackFlag = false;
					sPlayback.close();//close Formatter
					playbackTrigger = false;
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
	    
	        for (String p : ports){
	        	
	        	for (int i = 1; i < ports.length; ++i)
	          if (p.equals("COM" + i))  return p;
	        }
	        System.out.println("no port found");
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
	      
	      
	     // int flagIntForFalseSingals = 0;
	      float[] previousIndex = new float[16];	    
	      boolean flagFillOtherChannels = false;
	      
	      
	     PrintWriter output;
	     public void openFileWriter(){
	    	 tempFile = new File("temp.txt");
	    	 output = createWriter(tempFile); 
	     }
     
	     
	      
	     public void serialEvent(Serial port) {   
	      if ( myPort.available() > 0 ) 
	       { 
	    	  		
	                buff =  port.read(); //Returns the first byte of incoming serial data available (or -1 if no data is available) - int
	                byte byteRead =(byte) buff;    
	            //System.out.println("flagIntForFalseSingals is"+flagIntForFalseSingals);
	            if(BYTE_START == byteRead){                          
	                    counter=0;
	                    localCounter=0;
	                  }           
	            else if(BYTE_END == byteRead){      
	  	
	            	
	            	if(( packetCounter%2==0 )){   
	            		  if(counterfromzero==249){
            				
			                flagBuffer = 1;
			                 flagForDraw=true;					                 
			                 flagFilterUsed1 = false;
			                 println("flagBuffer1 "+flagForDraw);
			              }
			              else if(counterfromzero==499){
			            	  
			            	  flagBuffer = 2;
			                  flagForDraw=true;			         
			                  flagFilterUsed2 = false;
			                println("flagBuffer2 "+flagForDraw);
			              }
			              else if(counterfromzero==749){
			            	
			                 flagBuffer = 3;
			                 flagForDraw=true;               
			                 flagFilterUsed3 = false;
			                 println("flagBuffer3 "+flagForDraw);
			                counterfromzero=-1;
			              }
       					     
	            	}
	            	
	            	
	            		if(packetCounter%2 == 0 ){		     
	            			counterfromzero++;
	            		}
	            		
            	
	            	flagFillOtherChannels = false;      			
			        counter			=	0;
			        localCounter	=	0;
			        channelCounter	=	0;	              
			        previousPacket = packetCounter;	  
			       // System.out.println("cfz from end is"+counterfromzero);
	            }
		            else
				       {
			              counter++;
				              if(  counter == 1 ){
				                          
				              packetCounter = (int)buff;         
				              //Bytes 1: Packet counter  
					            	
					            
					            	channelCounter = 0;	
					            	
					            	//if we have odd samples in a streak or even samples in a streak
					            	if(previousPacket%2 ==packetCounter%2 ){
					            		flagFillOtherChannels = true;   	
					            	}
					            	
				              }   
					              else if( counter < 26 ){
							           
							                bufferOfThree[localCounter] = byteRead; //store the byte in the buffer	              
								          
							                if( localCounter == 2 ){
								            	
								                  bufferData[channelCounter] = interpret24bitAsInt32(bufferOfThree);   //24 to 32								                  
								                  int temp_channel_counter = 0;
								                  if( packetCounter%2 == 0)
								                	  temp_channel_counter = 8;						        
								                  	  dataPacket[channelCounter + temp_channel_counter][counterfromzero] = scale_fac_uVolts_per_count * (float) bufferData[channelCounter];	
								                      previousIndex[channelCounter + temp_channel_counter] = dataPacket[channelCounter + temp_channel_counter][counterfromzero];
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
            
	            }    
	    }
	      float[] serialBuff = new float[16];
	     
	     
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
	
	
	String file_name_save="";
	JFileChooser file_chooser_save= new JFileChooser(new File("C:\\"));
	File theFile_save;
	 void saveFile() {
		 
		  try {
		    SwingUtilities. invokeLater(new Runnable() {
		      public void run() {
		    	  
		    	file_chooser_save.setDialogTitle("Save a file");  
		    	file_chooser_save.setFileSelectionMode(TEXT);
		    	FileFilter filter = new FileNameExtensionFilter("Save as *.txt","txt");
		    	file_chooser_save.setFileFilter(filter);
		        int return_val = file_chooser_save.showSaveDialog(null);
		        
		        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
		        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
		        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
		        if ( return_val == JFileChooser.APPROVE_OPTION ) {
     
		        	
		          File file = file_chooser_save.getSelectedFile();
		         
		         	try  
		            {  
         		
		            BufferedWriter writer = new BufferedWriter( new FileWriter( file.getAbsolutePath()+".txt"));  	      
		            
		            Scanner s;
	         		 s = new Scanner(tempFile);
	  
					String l ;				
					while(s.hasNext())
					{
						for(int i=0;i<16;i++){
				
							
							if(s.hasNext()){
								l = s.next(); 
							writer.write(l); 
							System.out.print(l);					
							writer.write(" ");
							System.out.print(" ");
							}
							
								
						}
							writer.write("\r\n"); 
					}
					s.close();
					writer.close( ); 
		           
		            
		            print("The file was Saved Successfully!");
		                     
		            }  
		            catch (IOException e)  
		            {  
			            print("The Text could not be Saved!");  
		            }  


		
		          file_name_save = file.getAbsolutePath();
		          System.out.println(file_name_save);
		        } else {
		          file_name_save = "none";
		        }
		      }
		    }
		    );
		  }
		  catch (Exception e) {
		    e.printStackTrace();
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
							
							 if(   sPlayback.hasNext() == false  ) {
								 
								 for(int kly=0; kly < 250;kly++){
									 arrayString[j][kly] = 0f; 
								 }
								 arrayString[j][i] = 0f;	
							 }
							 else{
								 String a =sPlayback.next();
							
								  arrayString[j][i] = Float.parseFloat(a);						
							 }												  			 
						 	}						
					 	}
					 	
					 
					 if(sPlayback.hasNext()){
						 
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
									 }
								
							 	}
							 
							 if(switchForBuffer==1){							 
							 		flagBuffer = 1;
							 		flagForDraw=true;
					               
					                flagFilterUsed1 = false;
					                switchForBuffer=2;
							 	}
							 	else if(switchForBuffer==2){							 	
							 		flagBuffer = 2;
					                flagForDraw=true;
					              
					                flagFilterUsed2 = false;
					                switchForBuffer=3;
							 	}
							 	else {						 
							 		flagBuffer = 3;
					                flagForDraw=true;
					               
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
			size(1020,600,P3D);
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
			
	
				text("\nIn cases with sensory processing dysfunctions like autism spectrum disorder (ASD) and cognitive decline like Alzheimer's disease (AD), "
					+ "\noptimistic neurological improvements have been recorded after systematic sensorimotor enrichment stimulation and art therapeutic procedures. "
					+ "\nWhile art therapy may offer a cognitive-behavioral breakthrough, several latest studies have explored the effectiveness of brain signals "
					+ "\nrepresentations through audio and visual transformations of electroencephalography (EEG) measurements and the necessity of neurological "
					+ "\nevaluation of sensory integration therapies. EEG is a common technique that is widely used for the observation of brain activity and for "
					+ "\nthe detection of diseases and malfunctions. In fact, nowadays, EEG functionalities are not just limited to signals’ measurement, processing,"
					+ "\nand filtering but are also guided by more sophisticated computational analysis and imaging representations for the extraction of multivariate "
					+ "\ndata that reflect the correlation between neurodegeneration and cognitive impairment. The Neurocognitive Assessment Software for Enrichment "
					+ "\nSensory Environments (NASESE) is based on the JAVA and Processing programming languages and detects, displays and analyzes EEG signals for "
					+ "\nindividual or a group of patients. This neuroinformatics software offers a digital painting environment and a real time transformation of EEG "
					+ "\nsignals into adjustable music volume and octave configuration per electrode, for the real time observation and evaluation of therapeutic "
					+ "\nprocedures. The EEG acquisition is wireless, therefore, brain data can also be collected from the application of other sensory or sensorimotor"
					+ "\ntherapeutic sessions as well. The Neurocognitive Assessment Software for Enrichment Sensory Environments (NASESE) includes two main "
					+ "\nfunctionalities, organized in five different modules. The first functionality includes recording, filtering and visualization of the EEG "
					+ "\nsignals exported to a rotating 3D brain model and a real-time transformation of brain activity to sound sculptures, while the second "
					+ "\nfunctionality generates statistical tests and coherence calculation in a fully customizable computerized environment.",20,50);
				fill(10, 0, 10);
				textSize(14);

			
		}
	
		
	}
	//--------------Functions for DocumentationWindow------------------------------------//
	public class drawingWindowDocumentation extends PApplet
	{
		
		public void settings(){
			size(840,600,P3D);
			
		}
		
		
		PImage doc;
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
					readMore.setPosition(43*width/96,21*height/24);
					readMore.setSize(10*width/120,2*height/60);
					doc = loadImage("DocumentationImage.png");
					
				}
		public void draw(){
			
			background(255);
			image(doc, 0, 0);
			fill(10, 0, 10);
					
		}
	
		
		
		public void exit(){
			stop();		
		}
		public void readMore(){
			
			if (Desktop.isDesktopSupported()) {
			    try {
			        File myFile = new File("NASESE Description.pdf");
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
		
		Chart[] myChartCo = new Chart[120];
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
			while(coherenceScanner.hasNext()){
				counterSizeFile++;
				detl = Float.parseFloat(coherenceScanner.next());	
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
			
			while(coherenceScanner.hasNext()){		
				arrayCoherence[counterChannel][counterEachChannelLength] = Float.parseFloat(coherenceScanner.next());	
				
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
			  
			// println("Vpow is "+Vpow);
			 
			 
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
			             //println("ch is "+ch+" l is "+l+ " k is "+  k +" M is "+M);	         
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
				        temp3 =0f;
				        
				        }    		
				       }
				       
				       
				       
				        float[][] Coherence;
				       Coherence = new float[120][N/2];
				       //find coherence
				       stdyCounter=0;
				       
					for( int ch = 0; ch< channel_number - 1; ch++ ){
						for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int j=0; j< Coherence.length; j++ ){
								
					    	    Coherence[stdyCounter][j] = ((AVxy[stdyCounter][j])*(AVxy[stdyCounter][j])) / (AVxx[ch][j]*AVxx[inCounter][j] );
					    	
					    		
					       }
							stdyCounter++;	
					       }
						}
		
				     
					
			  
			
			//last part GUI
			    cp5 = new ControlP5(this);	        
		        cp5.setColorForeground(color(255,255,255));
		        cp5.setColorBackground(color(110,110,110));	
		        //0 to 15
		        for(int i=0;i<15;i++){       
		             myChartCo[i] = cp5.addChart(El_name[0]+"-"+El_name[i+1])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          
		         
		          for (int y=0;y< N/2 ;y++){
		           myChartCo[i].push("incoming", (Coherence[i][y]));           
		          }
		        }
		        
		         //15 to 29
		        for(int i=15;i<29;i++){       
		             myChartCo[i] = cp5.addChart(El_name[1]+"-"+El_name[i%15 + 2])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //29 to 42
		        for(int i=29;i<42;i++){       
		             myChartCo[i] = cp5.addChart(El_name[2]+"-"+El_name[i%29+3])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		        
		          //44 to 54
		        for(int i=42;i<54;i++){       
		             myChartCo[i] = cp5.addChart(El_name[3]+"-"+El_name[i%42+4])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		          //54 to 65
		        for(int i = 54;i < 65;i++){       
		             myChartCo[i] = cp5.addChart(El_name[4]+"-"+El_name[i%54+5])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //65 to 75
		        for(int i = 65;i < 75;i++){       
		             myChartCo[i] = cp5.addChart(El_name[5]+"-"+El_name[i%65+6])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		          //75 to 84
		        for(int i = 75;i < 84;i++){       
		             myChartCo[i] = cp5.addChart(El_name[6]+"-"+El_name[i%75+7])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        
		         //84 to 92
		        for(int i = 84;i < 92;i++){       
		             myChartCo[i] = cp5.addChart(El_name[7]+"-"+El_name[i%84+8])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		         
		         //92 to 99
		        for(int i = 92;i < 99;i++){       
		             myChartCo[i] = cp5.addChart(El_name[8]+"-"+El_name[i%92+9])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		         //99 to 105
		        for(int i = 99;i < 105;i++){       
		             myChartCo[i] = cp5.addChart(El_name[9]+"-"+El_name[i%99+10])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        //105 to 110
		        for(int i = 105;i < 110;i++){       
		             myChartCo[i] = cp5.addChart(El_name[10]+"-"+El_name[i%105+11])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		        //110 to 114
		        for(int i = 110;i < 114;i++){       
		             myChartCo[i] = cp5.addChart(El_name[11]+"-"+El_name[i%110+12])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		          //114 to 117
		        for(int i = 114;i < 117;i++){       
		             myChartCo[i] = cp5.addChart(El_name[12]+"-"+El_name[i%114+13])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		        
		           //117 to 119
		        for(int i = 117;i < 119;i++){       
		             myChartCo[i] = cp5.addChart(El_name[13]+"-"+El_name[i%117+14])
		                       .setPosition(10 + (i/10)*110, 10 + (i%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[i].addDataSet("incoming");
		          myChartCo[i].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		        	  myChartCo[i].push("incoming", (Coherence[i][y]));
		          }
		        }
		     
		      myChartCo[119] = cp5.addChart(El_name[14]+"-"+El_name[15])
		                       .setPosition(10 + (119/10)*110, 10 + (119%10)*68)
		                       .setSize(100, 50)
		                       .setRange(0, 1)
		                       .setView(Chart.LINE) // use Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
		                       .setStrokeWeight(1.5f)
		                       .setColorCaptionLabel(color(40))
		                       ;
		          myChartCo[119].addDataSet("incoming");
		          myChartCo[119].setData("incoming", new float[N/2]);
		          for (int y=0;y< N/2;y++){
		           myChartCo[119].push("incoming", Coherence[119][y]);
		          }
		     
		 
		}
		
		
		float hammingWindow(float wn, float n, float N){
		     float a = 25f/46f;
		     float b = 21f/46f;
		      
		     wn = a - b*cos( ( 2f*3.14f*n) / N );
		      return wn;
		    }
		
		
		public void draw() {
		 
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
			testAutism.setLabel("Begin Test");				
			testAutism.setPosition(width/60,height/5f );
			testAutism.setSize(width/10,2*height/35);			
		
			//Fill array of the W/S test
			fillarrayWS();
		
			
			
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
		     
		      
		      
		      
		     
		      text("(FP1-F3/T3-T5/C3-P3)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);
		      text("(FP2–F4/T4–T6/C4–P4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);
		      text("(FP1–FP2/F7–F8/F3–F4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);		      
		      text("(T3-T4/T5-T6)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);		 
		      text("(C3–C4/P3–P4/O1-O2)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6);	
		     // text("(C3 - C4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/24);		     
		    //  text("(P3 - P4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/12);		
		    //  text("(O1 - O2)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/6 +  height/12 + height/24);			      
		    //  text("(FP1- F3)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3);	
		   //   text("(FP2- F4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/24);	
		   //   text("(T3 - T5)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 );	
		   //   text("(T4 - T6)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/12 + height/24);	
		   //   text("(C3 - P3)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/6);	
		   //   text("(C4 - P4)",width/4 - width/20,  height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/3 +  height/6  + height/24);	
		
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
		      text(PvalueString[0][0],width/3 + width/8.5f ,height/14);  
	      
		      textSize(10);
		      doul1 = (double)meanTable1ASD[0][1];	         
		      doul2 = (double)SDTable1ASD[0][1];		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256 ,height/14);  
		      doul1 = (double)meanTable1CG[0][1];	         
		      doul2 = (double)SDTable1CG[0][1];		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/14);  
		      text(PvalueString[0][1],width/2  + width/8.25f,height/14);  
		      
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
		      text(PvalueString[0][2],width/2  + width/8.25f+ width/5.9f,height/14);  
		      
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
		      text(PvalueString[0][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/14);  
		      
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
		      text(PvalueString[1][0],width/3 + width/8.5f ,height/9.5f);  
	      
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
		      text(PvalueString[1][1],width/2  + width/8.25f,height/9.5f);  
		      
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
		      text(PvalueString[1][2],width/2  + width/8.25f+ width/5.9f,height/9.5f);  
		      
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
		      text(PvalueString[1][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/9.5f);  
		 
		      
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
		      text(PvalueString[2][0],width/3 + width/8.5f ,height/6.825f);  
	      
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
		      text(PvalueString[2][1],width/2  + width/8.25f,height/6.825f);  
		      
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
		      text(PvalueString[2][2],width/2  + width/8.25f+ width/5.9f,height/6.825f);  
		      
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
		      text(PvalueString[2][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/6.825f);  
		      
		      
		      
		      
		      
		      
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
		      text(PvalueString[3][0],width/3 + width/8.5f ,height/5.3f);  
	      
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
		      text(PvalueString[3][1],width/2  + width/8.25f,height/5.3f);  
		      
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
		      text(PvalueString[3][2],width/2  + width/8.25f+ width/5.9f,height/5.3f);  
		      
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
		      text(PvalueString[3][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/5.3f);  
		      
		      
		      
		      
		      
		      
		      
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
		      text(PvalueString[4][1],width/3 + width/8.5f ,height/4.32f);  
	      
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
		      text(PvalueString[4][1],width/2  + width/8.25f,height/4.32f);  
		      
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
		      text(PvalueString[4][2],width/2  + width/8.25f+ width/5.9f,height/4.32f);  
		      
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
		      text(PvalueString[4][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4.32f);  
		      
		      
		      
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
		      text(PvalueString[5][0],width/3 + width/8.5f ,height/3.65f);  
	      
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
		      text(PvalueString[5][1],width/2  + width/8.25f,height/3.65f);  
		      
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
		      text(PvalueString[5][2],width/2  + width/8.25f+ width/5.9f,height/3.65f);  
		      
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
		      text(PvalueString[5][3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/3.65f);  
		      
		      
//--------------------------coherence table----------------------------------------------------
		//--------------------------coherence table----------------------------------------------------
		      
		      
   
		   
		
		      
		      //line 1 -----------------------FP1 - FP2--10->72->51----------------
		      doul1 = (double)(meanDeltaASD_Coherence[9]+meanDeltaASD_Coherence[71]+meanDeltaASD_Coherence[50])/3;	         
		      doul2 = (double)(sdDeltaASD_Coherence[9]+sdDeltaASD_Coherence[71]+sdDeltaASD_Coherence[50])/3;		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      		     
		      text(a, width/4f + width/12f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);			      
		      doul1 = (double)(meanDeltaCG_Coherence[9]+meanDeltaCG_Coherence[71]+meanDeltaCG_Coherence[50])/3;	         
		      doul2 = (double)(sdDeltaCG_Coherence[9]+sdDeltaCG_Coherence[71]+sdDeltaCG_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);	
		      text(PvalueStringCoherenceDelta[0],width/3 + width/8.5f , height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
	      
		      textSize(10);
		      doul1 = (double)(meanThetaASD_Coherence[9]+meanThetaASD_Coherence[71]+meanThetaASD_Coherence[50])/3;	         
		      doul2 = (double)(sdThetaASD_Coherence[9]+sdThetaASD_Coherence[71]+sdThetaASD_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256 , height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)(meanThetaCG_Coherence[9]+meanThetaCG_Coherence[71]+meanThetaCG_Coherence[50])/3;	         
		      doul2 = (double)(sdThetaCG_Coherence[9]+sdThetaCG_Coherence[71]+sdThetaCG_Coherence[50])/3;		 	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueStringCoherenceTheta[1],width/2  + width/8.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)(meanAlphaASD_Coherence[9]+meanAlphaASD_Coherence[71]+meanAlphaASD_Coherence[50])/3;	         
		      doul2 = (double)(sdAlphaASD_Coherence[9]+sdAlphaASD_Coherence[71]+sdAlphaASD_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)(meanAlphaCG_Coherence[9]+meanAlphaCG_Coherence[71]+meanAlphaCG_Coherence[50])/3;	         
		      doul2 = (double)(sdAlphaCG_Coherence[9]+sdAlphaCG_Coherence[71]+sdAlphaCG_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text( PvalueStringCoherenceAlpha[0],width/2  + width/8.25f+ width/5.9f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)(meanBetaASD_Coherence[9]+meanBetaASD_Coherence[71]+meanBetaASD_Coherence[50])/3;	         
		      doul2 = (double)(sdBetaASD_Coherence[9]+sdBetaASD_Coherence[71]+sdBetaASD_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)(meanBetaCG_Coherence[9]+meanBetaCG_Coherence[71]+meanBetaCG_Coherence[50])/3;	         
		      doul2 = (double)(sdBetaCG_Coherence[9]+sdBetaCG_Coherence[71]+sdBetaCG_Coherence[50])/3;		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueStringCoherenceBeta[0],width/2  + width/8.25f+ width/5.9f+ width/6f, height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f);  
		 
		      
		  //line 2 -----------------------FP2–F4/T4–T6/C4–P4-->35-83-62----------------
		      doul1 = (double)(meanDeltaASD_Coherence[34]+meanDeltaASD_Coherence[82]+meanDeltaASD_Coherence[61])/3;	         
		      doul2 = (double)(sdDeltaASD_Coherence[34]+sdDeltaASD_Coherence[82]+sdDeltaASD_Coherence[61])/3;		      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);			      
		      doul1 = (double)(meanDeltaCG_Coherence[34]+meanDeltaCG_Coherence[82]+meanDeltaCG_Coherence[61])/3;	         
		      doul2 = (double)(sdDeltaCG_Coherence[34]+sdDeltaCG_Coherence[82]+sdDeltaCG_Coherence[61])/3;		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);	
		      text(PvalueStringCoherenceDelta[1],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
	    
		      textSize(10);
		      doul1 = (double)(meanThetaASD_Coherence[34]+meanThetaASD_Coherence[82]+meanThetaASD_Coherence[61])/3;	         
		      doul2 = (double)(sdThetaASD_Coherence[34]+sdThetaASD_Coherence[82]+sdThetaASD_Coherence[61])/3;			  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/256,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)(meanThetaCG_Coherence[34]+meanThetaCG_Coherence[82]+meanThetaCG_Coherence[61])/3;	         
		      doul2 = (double)(sdThetaCG_Coherence[34]+sdThetaCG_Coherence[82]+sdThetaCG_Coherence[61])/3;		  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text(PvalueStringCoherenceTheta[1],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      
		      textSize(10);
		      doul1 = (double)(meanAlphaASD_Coherence[34]+meanAlphaASD_Coherence[82]+meanAlphaASD_Coherence[61])/3;	         
		      doul2 = (double)(sdAlphaASD_Coherence[34]+sdAlphaASD_Coherence[82]+sdAlphaASD_Coherence[61])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)(meanAlphaCG_Coherence[34]+meanAlphaCG_Coherence[82]+meanAlphaCG_Coherence[61])/3;	         
		      doul2 = (double)(sdAlphaCG_Coherence[34]+sdAlphaCG_Coherence[82]+sdAlphaCG_Coherence[61]);	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text( PvalueStringCoherenceAlpha[1],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      
		      textSize(10);
		      doul1 = (double)(meanBetaASD_Coherence[34]+meanBetaASD_Coherence[82]+meanBetaASD_Coherence[61])/3;	         
		      doul2 = (double)(sdBetaASD_Coherence[34]+sdBetaASD_Coherence[82]+sdBetaASD_Coherence[61])/3;				  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      doul1 = (double)(meanBetaCG_Coherence[34]+meanBetaCG_Coherence[82]+meanBetaCG_Coherence[61])/3;	         
		      doul2 = (double)(sdBetaCG_Coherence[34]+sdBetaCG_Coherence[82]+sdBetaCG_Coherence[61])/3;		
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		      text(PvalueStringCoherenceBeta[1],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/24);  
		 
		       
	//line 3 -----------------------FP1–FP2/F7–F8/F3–F4->-----1--103---116------------
		      textSize(10);
		      doul1 = (double)(meanDeltaASD_Coherence[1]+meanDeltaASD_Coherence[103]+meanDeltaASD_Coherence[116])/3;	         
		      doul2 = (double)(sdDeltaASD_Coherence[1]+sdDeltaASD_Coherence[103]+sdDeltaASD_Coherence[116])/3;				     
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);			      
		      doul1 = (double)(meanDeltaCG_Coherence[0]+meanDeltaCG_Coherence[102]+meanDeltaCG_Coherence[115])/3;		         
		      doul2 = (double)(sdBetaCG_Coherence[0]+sdBetaCG_Coherence[102]+sdBetaCG_Coherence[115])/3;	 
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);	
		      text(PvalueStringCoherenceDelta[2],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
	      
		      textSize(10);
		      doul1 = (double)(meanThetaASD_Coherence[0]+meanThetaASD_Coherence[102]+meanThetaASD_Coherence[115])/3;	         
		      doul2 = (double)(sdThetaASD_Coherence[0]+sdThetaASD_Coherence[102]+sdThetaASD_Coherence[115])/3;		
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/248,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)(meanThetaCG_Coherence[0]+meanThetaCG_Coherence[102]+meanThetaCG_Coherence[115])/3;		         
		      doul2 = (double)(sdThetaCG_Coherence[0]+sdThetaCG_Coherence[102]+sdThetaCG_Coherence[115])/3;	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text(PvalueStringCoherenceTheta[2],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      
		      textSize(10);
		      doul1 = (double)(meanAlphaASD_Coherence[0]+meanAlphaASD_Coherence[102]+meanAlphaASD_Coherence[115])/3;	         
		      doul2 = (double)(sdAlphaASD_Coherence[0]+sdAlphaASD_Coherence[102]+sdAlphaASD_Coherence[115])/3;			  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)(meanAlphaCG_Coherence[0]+meanAlphaCG_Coherence[102]+meanAlphaCG_Coherence[115])/3;	         
		      doul2 = (double)(sdAlphaCG_Coherence[0]+sdAlphaCG_Coherence[102]+sdAlphaCG_Coherence[115])/3;	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text( PvalueStringCoherenceAlpha[2],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      
		      textSize(10);
		      doul1 = (double)(meanBetaASD_Coherence[0]+meanBetaASD_Coherence[102]+meanBetaASD_Coherence[115])/3;	         
		      doul2 = (double)(sdBetaASD_Coherence[0]+sdBetaASD_Coherence[102]+sdBetaASD_Coherence[115])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      doul1 = (double)(meanBetaCG_Coherence[0]+meanBetaCG_Coherence[102]+meanBetaCG_Coherence[115])/3;	         
		      doul2 = (double)(sdBetaCG_Coherence[0]+sdBetaCG_Coherence[102]+sdBetaCG_Coherence[115])/3;	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
		      text(PvalueStringCoherenceBeta[2],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12);  
	      
		        
		      
		//line 4 -----------------------T3-T4/T5-T6->----5------65-------------
		      textSize(10);
		      doul1 = (double)(meanDeltaASD_Coherence[4]+meanDeltaASD_Coherence[64])/2;	         
		      doul2 = (double)(sdDeltaASD_Coherence[4]+sdDeltaASD_Coherence[64])/2;	      
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);			      
		      doul1 = (double)(meanDeltaCG_Coherence[4]+meanDeltaCG_Coherence[64])/2;	         
		      doul2 = (double)(sdDeltaCG_Coherence[4]+meanDeltaCG_Coherence[64])/2;	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);	
		      text(PvalueStringCoherenceDelta[3],width/3 + width/8.5f ,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
	      
		      textSize(10);
		      doul1 = (double)(meanThetaASD_Coherence[4]+meanThetaASD_Coherence[64])/2;	         
		      doul2 = (double)(sdThetaASD_Coherence[4]+sdThetaASD_Coherence[64])/2;	   
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 +width/400,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)(meanThetaCG_Coherence[4]+meanThetaCG_Coherence[64])/2;	         
		      doul2 = (double)(sdThetaCG_Coherence[4]+sdThetaCG_Coherence[64])/2;	    
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/128,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text(PvalueStringCoherenceTheta[3],width/2  + width/8.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)(meanAlphaASD_Coherence[4]+meanAlphaASD_Coherence[64])/2;	         
		      doul2 = (double)(sdAlphaASD_Coherence[4]+sdAlphaASD_Coherence[64])/2;	   	  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)(meanAlphaCG_Coherence[4]+meanAlphaCG_Coherence[64])/2;	         
		      doul2 = (double)(sdAlphaCG_Coherence[4]+sdAlphaCG_Coherence[64])/2;	 
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text( PvalueStringCoherenceAlpha[2],width/2  + width/8.25f+ width/5.9f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      textSize(10);
		      doul1 = (double)(meanBetaASD_Coherence[4]+meanBetaASD_Coherence[64])/2;	         
		      doul2 = (double)(sdBetaASD_Coherence[4]+sdBetaASD_Coherence[64])/2;	   		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      doul1 = (double)(meanBetaCG_Coherence[4]+meanBetaCG_Coherence[64])/2;	         
		      doul2 = (double)(sdBetaCG_Coherence[4]+sdBetaCG_Coherence[64])/2;	   
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      text(PvalueStringCoherenceBeta[2],width/2  + width/8.25f+ width/5.9f+ width/6f,height/8+ height/128+ height/6 + height/32+height/8 + height/6 + height/20 +  height/60 - height/3.2f + height/12 + height/24);  
		      
		      //==============
		      

		      
		      
		    
	 //line 5 -----------------------C3–C4/P3–P4/O1-O2----->30---120--86-------------
		      textSize(10);
		      doul1 = (double)(meanDeltaASD_Coherence[29]+meanDeltaASD_Coherence[119]+meanDeltaASD_Coherence[85])/3;	         
		      doul2 = (double)(sdDeltaASD_Coherence[29]+sdDeltaASD_Coherence[119]+sdDeltaASD_Coherence[85])/3;	         
		      b = "±";		
		      a = df.format(doul1)+b+df.format(doul2);		      	      		     
		      text(a, width/4f + width/12f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);			      
		      doul1 = (double)(meanDeltaCG_Coherence[29]+meanDeltaCG_Coherence[119]+meanDeltaCG_Coherence[85])/3;	         
		      doul2 = (double)(sdDeltaCG_Coherence[29]+sdDeltaCG_Coherence[119]+sdDeltaCG_Coherence[85])/3;	           
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);	
		      text(a, width/4f + width/7.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);	
		      text(PvalueStringCoherenceDelta[3],width/3 + width/8.5f ,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
	      
		      textSize(10);
		      
		      doul1 = (double)(meanThetaASD_Coherence[29]+meanThetaASD_Coherence[119]+meanThetaASD_Coherence[85])/3;	         
		      doul2 = (double)(sdThetaASD_Coherence[29]+sdThetaASD_Coherence[119]+sdThetaASD_Coherence[85])/3;	 	     
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2+width/400,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f); 	
		      doul1 = (double)(meanThetaCG_Coherence[29]+meanThetaCG_Coherence[119]+meanThetaCG_Coherence[85])/3;	         
		      doul2 = (double)(sdThetaCG_Coherence[29]+sdThetaCG_Coherence[119]+sdThetaCG_Coherence[85])/3;	
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f + width/128, height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueStringCoherenceTheta[3],width/2  + width/8.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)(meanAlphaASD_Coherence[29]+meanAlphaASD_Coherence[119]+meanAlphaASD_Coherence[85])/3;	         
		      doul2 = (double)(sdAlphaASD_Coherence[29]+sdAlphaASD_Coherence[119]+sdAlphaASD_Coherence[85])/3;	 	 
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)(meanAlphaCG_Coherence[29]+meanAlphaCG_Coherence[119]+meanAlphaCG_Coherence[85])/3;	         
		      doul2 = (double)(sdAlphaCG_Coherence[29]+sdAlphaCG_Coherence[119]+sdAlphaCG_Coherence[85])/3;	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6.25f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text( PvalueStringCoherenceAlpha[3],width/2  + width/8.25f+ width/5.9f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      
		      textSize(10);
		      doul1 = (double)(meanBetaASD_Coherence[29]+meanBetaASD_Coherence[119]+meanBetaASD_Coherence[85])/3;	         
		      doul2 = (double)(sdBetaASD_Coherence[29]+sdBetaASD_Coherence[119]+sdBetaASD_Coherence[85])/3;		  
		      b = "±";
		      a = df.format(doul1)+b+df.format(doul2);		      	
		      text(a,width/2 + width/128+ width/5.8f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      doul1 = (double)(meanBetaCG_Coherence[29]+meanBetaCG_Coherence[119]+meanBetaCG_Coherence[85])/3;	         
		      doul2 = (double)(sdBetaCG_Coherence[29]+sdBetaCG_Coherence[119]+sdBetaCG_Coherence[85])/3;	  
		      b = "±";
		      a = df.format(doul1) +b+df.format(doul2);		      	
		      text(a,width/2 + width/20f+ width/64+ width/6f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		      text(PvalueStringCoherenceBeta[3],width/2  + width/8.25f+ width/5.9f+ width/6f,height/4+ height/128+ height/6 + height/32 + height/3 + height/20 +  height/60 - height/3.2f);  
		
			
		}
		
		public void testAutism(){
			
				
			if(  (numberFilesADS != 0)  && 	(numberFilesControlGroup != 0)  ){
			
					indicator = "Testing...";
					 controlGroup = new float[numberFilesControlGroup][channel_number][4];
					 patient = new float[numberFilesADS][channel_number][4];
					 controlGroupCoherence = new float[numberFilesControlGroup][120][128];
					 patientCoherence = new float[numberFilesADS][120][128];
					 runTheTest();
					 
					 indicator = "Results Displayed";
		
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
			save("line.png");
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
			
	
			
			
			//mean absolute power-----------------------------------------------------------
			//now compute the regions
			computeRegions();
			
			/*
			thesis1();
			thesis2();
			-->*/
			
			
			//extract all data before performing any test
			beforeTestADS();
			beforeTestControlGroup();	
			//we have the data to perform test for the table1		
			performTable1Test();	
			//Coherence functions
			
			coherenceBeforeChiTest();		
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
						
						tTest(i,j);
						
					}
					else{
						disTest[i][j] = "uTest";						
						uTest(i,j);
						
					}
								
			}
				
			
		}
	
		}
			
		
		
		
		
		public void tTest(int currRegion , int currBand){
			
			
			
			double [] sample1 = new double[numberFilesADS];
			double [] sample2 = new double[numberFilesControlGroup];
			
			for(int i=0;i<numberFilesADS;i++){
				sample1[i] = (double)(regionADS[i][currRegion][currBand]);
			}
			for(int i=0;i<numberFilesControlGroup;i++){
				sample2[i] = (double)(regionControlGroup[i][currRegion][currBand]);
			}
			
			double pValue;
			TTest ttest = new TTest();
			pValue = ttest.tTest(sample1, sample2);
			PvalueFloat[currRegion][currBand]      = (float)pValue;
			
			pValueFinalString(PvalueFloat[currRegion][currBand],currRegion,currBand);	
		}
		
		public void uTest(int currRegion , int currBand){
			
			double [] sample1 = new double[numberFilesADS];
			double [] sample2 = new double[numberFilesControlGroup];
			
			
			
			for(int i=0;i<numberFilesADS;i++){
				sample1[i] = (double)(regionADS[i][currRegion][currBand]);
			}
			for(int i=0;i<numberFilesControlGroup;i++){
				sample2[i] = (double)(regionControlGroup[i][currRegion][currBand]);
			}
			
			double pValue;
			
			MannWhitneyUTest utest = new MannWhitneyUTest();				
			pValue = utest.mannWhitneyUTest(sample1, sample2);		
			PvalueFloat[currRegion][currBand]      = (float)pValue;
			pValueFinalString(PvalueFloat[currRegion][currBand],currRegion,currBand);

			
		}
		
	
		public void pValueFinalString(float pValue,int currRegion,int currBand){
			
			
			DecimalFormat df = new DecimalFormat("#.##");
		    df.setRoundingMode(RoundingMode.CEILING);
		   
			if(PvalueFloat[currRegion][currBand] <= 0.05){
				testSignificantFlag[currRegion][currBand] = true;
				
				String temporary = String.valueOf(df.format(PvalueFloat[currRegion][currBand]));
				StringBuilder str = new StringBuilder(temporary);
				str.insert(temporary.length(), "*");		
				PvalueString[currRegion][currBand] = str.toString();	
			}
			else{
				PvalueString[currRegion][currBand] = String.valueOf(df.format(PvalueFloat[currRegion][currBand]));	
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
		while(sfTest.hasNext()){
			float what  = Float.parseFloat(sfTest.next());
			//System.out.print(what);
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
				while(sTest.hasNext()){
						for(int i=0;  i<250;  i++){
								 for(int j=0; j< channel_number; j++ ){
									 if(   sTest.hasNext() == false  ) {
										 arrayString[j][i] = 0f;	
									 }
									 else{
										 arrayString[j][i] = Float.parseFloat(sTest.next());
										 
									 }   			 
								 }
								
							 	}
						
						 if(sTest.hasNext()){
							 
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
											 filter_function(notchProperties[currentNotch].b, notchProperties[currentNotch].a, dataPacket[j]); 
											 filter_function(BPproperties[currentBP].b, BPproperties[currentBP].a, dataPacket[j]);	
											 filter_function(muProperties[currentMu].b, muProperties[currentMu].a, dataPacket[j]);
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
							             // println();
							              //save stage  
							              for(int i = 0; i < fftBuff[currChannel].specSize(); i++){
								       
							            	  //get the bands  
							            	  	if(i<=4){
							            	  		theLongArrayDelta[currChannel][counterDelta] = fftBuff[currChannel].getBand(i);	
								            		counterDelta++;  
								            		
							            	  	}
							            	  	 else if(( i >= 5 ) && (i <= 8) ){
							            	  		theLongArrayTheta[currChannel][counterTheta] = fftBuff[currChannel].getBand(i);
							            	  		counterTheta++;  
							            	 
								            	  	
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
								 for( int i=0; i < theLongArrayDelta[j].length ; i++ ){
									 sumArrayDelta = theLongArrayDelta[j][i] + sumArrayDelta;								 
								 }
							

								
									counterLength=0;
								//Find sum of Theta
								 for( int i=0; i < theLongArrayTheta[j].length; i++ ){
									 sumArrayTheta = theLongArrayTheta[j][i] + sumArrayTheta;							 
								 }
								 
								//Find sum of Alpha
								 for( int i=0; i < theLongArrayAlpha[j].length; i++ ){
									 sumArrayAlpha = theLongArrayAlpha[j][i] + sumArrayAlpha;							 
								 }
								 
								//Find sum of Beta
								 for( int i=0; i < theLongArrayBeta[j].length; i++ ){
									 sumArrayBeta = theLongArrayBeta[j][i] + sumArrayBeta ;							 
								 }
							 
								
								 
								 
								 //Find means for the current patient	// patient[whichPatient][whichChannel/electrode][whichband]		
									 if(option == "patient"){	 
										 patient[pos][j][0] = sumArrayDelta/((float)theLongArrayDelta[j].length);
										 patient[pos][j][1] = sumArrayTheta/((float)theLongArrayTheta[j].length);
										 patient[pos][j][2] = sumArrayAlpha/((float)theLongArrayAlpha[j].length);
										 patient[pos][j][3] =   sumArrayBeta/((float)theLongArrayBeta[j].length);		
										
									 }
									 else if(option == "controlGroup"){
										 controlGroup[pos][j][0] = sumArrayDelta/((float)theLongArrayDelta[j].length);
										 controlGroup[pos][j][1] = sumArrayTheta/((float)theLongArrayTheta[j].length);
										 controlGroup[pos][j][2] = sumArrayAlpha/((float)theLongArrayAlpha[j].length);
										 controlGroup[pos][j][3] =  sumArrayBeta/((float)theLongArrayBeta[j].length);
									
									 }
									 
						}
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
		public void coherenceBeforeChiTest(){
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
				coherenceMeanSD();
				
				
	
		}
		
	
		public void readFilesTestCoherence(int pos ,File[] fileArray, String option){
	
			
			//first part--reading the file
		 
			int counterLength=0; // length of file
			
			useScannerFirst(pos,fileArray);
			while(sfTest.hasNext()){
				float what = Float.parseFloat(sfTest.next());			
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
			//println("size of file is "+counterLength);
			int counterEachChannelLength = 0;
			int counterChannel =0;		
			while(sTest.hasNext()){		
				arrayCoherence[counterChannel][counterEachChannelLength] = Float.parseFloat(sTest.next());	
				
				if(counterChannel == 15){
					counterChannel = 0;
					counterEachChannelLength++;
				}
				else
					counterChannel++;
				
				
			}
			
			
			
			
			//println("counterEachChannelLength is "+counterEachChannelLength);
			sTest.close();//close Formatter
			
			
			//second part, use coherence
			// the exponent power 2 is calulated, then Cxx[][] Cxy[][] Cyy[][]
			   float Vpow = 1;
			   for(int ia = 0; ia < counterEachChannelLength; ia++){
				   if(Vpow < ia){
					   Vpow = Vpow*2;		   
				   }
			   }
			  
			 //println("Vpow is "+Vpow);
			 
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
				      	        
				        temp3 =0f;
				        
				        }    		
				       }
				       
				       
				       
				        float[][] Coherence;
				       Coherence = new float[120][N/2];
				      
				       //find coherence
				       stdyCounter=0;
					for( int ch = 0; ch< channel_number - 1; ch++ ){
						for( int inCounter = ch+1; inCounter< channel_number; inCounter++ ){
							for(int j=0; j< 128; j++ ){
								if (  AVxx[ch][j] == 0){
									 Coherence[stdyCounter][j] = 1;
								}
					    	    Coherence[stdyCounter][j] = ((AVxy[stdyCounter][j])*(AVxy[stdyCounter][j])) / (AVxx[ch][j]*AVxx[inCounter][j] );					    	
					    				    	    
					       }
							stdyCounter++;	
					       }
						}
		
		
		
			
			
						
						
							 
					//now we insert the data in a long array for all groups
					for(int i =0 ; i< 120; i++){ 
							for(int j =0 ; j< 128; j++){ 							 
									 //Find means for the current patient	// patient[whichPatient][whichPair][which value]		
										 if(option == "patient"){	 
											 patientCoherence[pos][i][j] = Coherence[i][j];
										 }
										 else if(option == "controlGroup"){
											 controlGroupCoherence[pos][i][j] = Coherence[i][j];								 	 
											
											 
										 }
							}
										 
							}//println();print("---------------------------------");
		
			}
		
		
		float hammingWindow(float wn, float n, float N){
		     float a = 25f/46f;
		     float b = 21f/46f;
		      
		     wn = a - b*cos( ( 2f*3.14f*n) / N );
		    
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
		
		public void coherenceMeanSD(){
			
			
			
			
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
													
										}
		
			
		}
		
		
		
		public void performTable2Test(){
			//String[] dataset
			
			
			
			
			
			
			for(int i=0; i < 4; i++){
						
						// data for chi square
						if(i==0){
							int[] chi_pair = {10,72,51};
							chiTestCoherence(i,chi_pair);
						}
						else if(i==1){		
							int[] chi_pair = {35,83,62};
							chiTestCoherence(i,chi_pair);
						}
						else if(i==2){	
							int[] chi_pair = {1,103,116};
							chiTestCoherence(i,chi_pair);
						}
						else if(i==3){	
							int[] chi_pair = {5,65};
							chiTestCoherence(i,chi_pair);
						}
						else{
							
						}
						
						
					
									
					

				
				
			
		}
		
		}
				
		
	
		
		public void chiTestCoherence(int currPair,int[] chi_pair){

			testSignificantFlagCoherenceDelta[currPair] = false;
			testSignificantFlagCoherenceTheta[currPair] = false;
			testSignificantFlagCoherenceAlpha[currPair] = false;
			testSignificantFlagCoherenceBeta[currPair]  = false;							
			
			long [][] sample = new long[chi_pair.length][2];
			
			
			
			
			
			
			double pValue;
			ChiSquareTest chi_test = new ChiSquareTest();
			
			
			//i is current file, currBand is alpha,beta,gamma,delta
							//compare mean coherence for delta between 2 channels
	//-------------------------------------------------------------------		
			
			
			sample[0][0] = (long)(100*meanDeltaASD_Coherence[chi_pair[0]]);	
			sample[1][0] = (long)(100*meanDeltaASD_Coherence[chi_pair[1]]);	
			
			if(chi_pair.length==3){
				sample[2][0] = (long)(100*meanDeltaASD_Coherence[chi_pair[2]]);
				sample[2][1] = (long)(100*meanDeltaCG_Coherence[chi_pair[2]]);
			}
			
			sample[0][1] = (long)(100*meanDeltaCG_Coherence[chi_pair[0]]);	
			sample[1][1] = (long)(100*meanDeltaCG_Coherence[chi_pair[1]]);	
			
			
			
			pValue = chi_test.chiSquareTest(sample);
			DecimalFormat dfa = new DecimalFormat("#.##");
		    dfa.setRoundingMode(RoundingMode.CEILING);
		    print("pValue is "+pValue);
			PvalueFloatCoherenceDelta[currPair]      = (float)pValue;
				
			if(PvalueFloatCoherenceDelta[currPair] <= 0.05){
				testSignificantFlagCoherenceDelta[currPair] = true;	
				String temporary = String.valueOf(dfa.format(PvalueFloatCoherenceDelta[currPair]));
				StringBuilder str = new StringBuilder(temporary);
				str.insert(temporary.length(), "*");		
				PvalueStringCoherenceDelta[currPair] = str.toString();					
			}
			else{
				PvalueStringCoherenceDelta[currPair] = String.valueOf(dfa.format(PvalueFloatCoherenceDelta[currPair]));
			}
			println("\npvalue delta is "+PvalueStringCoherenceDelta[currPair]+"\n"); 
		
	//-------------------------------------------------------------------
			 
			
			
			 				//theta_coherence		 
	//-------------------------------------------------------------------		 
			
				sample[0][0] = (long)(100*meanThetaASD_Coherence[chi_pair[0]]);	
				sample[1][0] = (long)(100*meanThetaASD_Coherence[chi_pair[1]]);	
				if(chi_pair.length==3){
				sample[2][0] = (long)(100*meanThetaASD_Coherence[chi_pair[2]]);
				sample[2][1] = (long)(100*meanThetaCG_Coherence[chi_pair[2]]);
				}
				sample[0][1] = (long)(100*meanThetaCG_Coherence[chi_pair[0]]);	
				sample[1][1] = (long)(100*meanThetaCG_Coherence[chi_pair[1]]);	
				
				
				
				
				//sample = new long[][]{ sample1 , sample2 };
				pValue = chi_test.chiSquareTest(sample);
				PvalueFloatCoherenceTheta[currPair]      = (float)pValue;
				if(PvalueFloatCoherenceTheta[currPair] <= 0.05){
					testSignificantFlagCoherenceTheta[currPair] = true;
					String temporary = String.valueOf(dfa.format(PvalueFloatCoherenceTheta[currPair]));
					StringBuilder str = new StringBuilder(temporary);
					str.insert(temporary.length(), "*");		
					PvalueStringCoherenceTheta[currPair] = str.toString();	
				}
				else	
					PvalueStringCoherenceTheta[currPair] = String.valueOf(dfa.format(PvalueFloatCoherenceTheta[currPair]));	
				println("\npvalue theta is "+PvalueStringCoherenceTheta[currPair]+"\n"); 
						//alpha coherence
		//-------------------------------------------------------------------	
			
				sample[0][0] = (long)(100*meanAlphaASD_Coherence[chi_pair[0]]);	
				sample[1][0] = (long)(100*meanAlphaASD_Coherence[chi_pair[1]]);	
				if(chi_pair.length==3){
				sample[2][0] = (long)(100*meanAlphaASD_Coherence[chi_pair[2]]);
				sample[2][1] = (long)(100*meanAlphaCG_Coherence[chi_pair[2]]);
				}
				sample[0][1] = (long)(100*meanAlphaCG_Coherence[chi_pair[0]]);	
				sample[1][1] = (long)(100*meanAlphaCG_Coherence[chi_pair[1]]);	
				
				
				//sample = new long[][]{ sample1 , sample2 };
				pValue = chi_test.chiSquareTest(sample);
				PvalueFloatCoherenceAlpha[currPair]      = (float)pValue;
				if(PvalueFloatCoherenceAlpha[currPair] <= 0.05){
					testSignificantFlagCoherenceAlpha[currPair] = true;
					String temporary = String.valueOf(dfa.format(PvalueFloatCoherenceAlpha[currPair]));
					StringBuilder str = new StringBuilder(temporary);
					str.insert(temporary.length(), "*");		
					PvalueStringCoherenceAlpha[currPair] = str.toString();	
				}
				else
					PvalueStringCoherenceAlpha[currPair] = String.valueOf(dfa.format(PvalueFloatCoherenceAlpha[currPair]));		
				println("\npvalue alpha is "+PvalueStringCoherenceAlpha[currPair]+"\n"); 
				
				
				//beta coherence
		//-------------------------------------------------------------------		
			
				
				sample[0][0] = (long)(100*meanBetaASD_Coherence[chi_pair[0]]);	
				sample[1][0] = (long)(100*meanBetaASD_Coherence[chi_pair[1]]);
				if(chi_pair.length==3){
				sample[2][0] = (long)(100*meanBetaASD_Coherence[chi_pair[2]]);
				sample[2][1] = (long)(100*meanBetaCG_Coherence[chi_pair[2]]);
				}
				sample[0][1] = (long)(100*meanBetaCG_Coherence[chi_pair[0]]);	
				sample[1][1] = (long)(100*meanBetaCG_Coherence[chi_pair[1]]);	
				
				
				
				//sample = new long[][]{ sample1 , sample2 };
				pValue = chi_test.chiSquareTest(sample);
				PvalueFloatCoherenceBeta[currPair]      = (float)pValue;
				if(PvalueFloatCoherenceBeta[currPair] <= 0.05){	
					testSignificantFlagCoherenceBeta[currPair] = true;
					String temporary = String.valueOf(dfa.format(PvalueFloatCoherenceBeta[currPair]));
					StringBuilder str = new StringBuilder(temporary);
					str.insert(temporary.length(), "*");		
					PvalueStringCoherenceBeta[currPair] = str.toString();	
				}
				else
					PvalueStringCoherenceBeta[currPair] = String.valueOf(dfa.format(PvalueFloatCoherenceBeta[currPair]));		
				println("\npvalue beta is "+PvalueStringCoherenceBeta[currPair]+"\n"); 
					
		
		}
		
		
		//initializing arrays for normality test
	
	
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
		PImage bluredShape2;
		
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
		Button clearScreenButton;
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
			bluringShape2();
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
					
					if(clearScreenButton.isMousePressed())
					{
						p.image(bluredShape,0,0);
					}
					clearScreenButton.draw(p);
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
					
					if(saveImageButton.isMousePressed())
					{
						p.image(bluredShape2,0,0);
					}
					if(loadImageButton.isMousePressed())
					{
						p.image(bluredShape2,width/28,0);
					}
					
					saveImageButton.draw(p);
					loadImageButton.draw(p);
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
			clearScreenButton = toolButtons.addButton("clearScreenButton");
			clearScreenButton.setGroup(clearScreenGroup);
			clearScreenButton.setPosition(width/120,height/60);
			clearScreenButton.setSize(width/24,height/12);
			clearScreenButton.setLabelVisible(false);
			clearScreenButton.setImage(clearImage);
			clearScreenButton.updateSize();
			
			
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
			
		
			
			//MUST start with all white.
			
			stroke(255);
			fill(255);
			background(255);
		
			//Initialize of undo/redo object
			imageMemory = new UndoRedo(100);
			clearScreenImage = new UndoRedo(1);
			
		}
		
		
		
		public void exit() {
				  stop();		 
			  drawButton.setVisible(true);
			  passNewColors.setVisible(false);
			  drawWindowFlag = !drawWindowFlag;		
			  imageMemory.images.img = null; 
			  clearScreenImage.images.img = null;
	
		}
		
		
		boolean flagClear		=	false;
		boolean flagLoadImage	=	false;
		
		public void draw()
		{
			
			
			if(flagLoadImage == true){		
				for(int in =0; in<imageMemory.images.amount; in++)
					imageMemory.images.img[in] = imageLoad;
				imageMemory.undo();
				imageMemory.redo();
				flagLoadImage = false;
			}
			
			if(flagClear==false){
				
				
				PImage blankIcon = loadImage("blankImage.png");
				blankIcon.resize(width,height);
				clearScreenImage.images.img = new PImage[1];
				clearScreenImage.images.img[0] = blankIcon;						
				flagClear = true;
			}
				
			
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
		
		public void bluringShape2()
		{
			PGraphics pg = createGraphics(width/27,height/12+2*height/60,P2D);
			pg.beginDraw();
			pg.fill(0,255,0);
			pg.noStroke();
			pg.rectMode(CENTER);
			pg.rect((width/21f)/2,(height/12+2*height/60)/2,width/24+1,height/12+2);
			pg.filter(BLUR,9);
			pg.endDraw();
			bluredShape2 = pg.get();
			
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
		
		
		
		
		UndoRedo clearScreenImage;
		//Clear screen button function
		public void clearScreenButton()
		{					
			clearScreenImage.images.show();				
		}
		
		
		
		
		
		JFileChooser file_chooser_save_image = new JFileChooser(new File("C:\\"));
		File theFileSaveImage;
		String fileNameImageSave;
		public void saveImageButton()
		{
			 save("painting.jpg");
			 try {
				    SwingUtilities. invokeLater(new Runnable() {
				      public void run() {
				    	  
				    	file_chooser_save_image.setDialogTitle("Save a file");  
				    	file_chooser_save_image.setFileSelectionMode(IMAGE);
				    	FileFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
				    	file_chooser_save_image.setFileFilter(filter);
				        int return_val = file_chooser_save_image.showSaveDialog(null);
				        
				        if ( return_val == JFileChooser.CANCEL_OPTION )   System.out.println("canceled");
				        if ( return_val == JFileChooser.ERROR_OPTION )    System.out.println("error");
				        if ( return_val == JFileChooser.APPROVE_OPTION )  System.out.println("approved");
				        if ( return_val == JFileChooser.APPROVE_OPTION ) {
				        	
				         
				          
				          File file = file_chooser_save_image.getSelectedFile();
				          fileNameImageSave = file.getAbsolutePath();
				       
				          
						  PImage painting = loadImage("painting.jpg");
						  painting.save(fileNameImageSave);
				        
				            print("The file was Saved Successfully!");
			
				            
				          System.out.println(fileNameImageSave);
				        } else {
				        	fileNameImageSave = "none";
				        }
				      }
				    }
				    );
				  }
				  catch (Exception e) {
				    e.printStackTrace();
				  }
		}
		
		
		
		
		
		JFileChooser file_chooser_load_image = new JFileChooser(new File("C:\\"));
		PImage imageLoad;
		File fileForImageLoad;
		String fileNameForImageLoad;
		
		public void loadImageButton(){
			
			
				try {
					SwingUtilities. invokeLater(new Runnable() {
						public void run() {
							
							file_chooser_load_image.setDialogTitle("Load image file");
							file_chooser_load_image.setFileSelectionMode(IMAGE);
							FileFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
							file_chooser_load_image.setFileFilter(filter);
							
					        int return_val = file_chooser_load_image.showOpenDialog(null);
					        
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
					          
					          fileForImageLoad = file_chooser_load_image.getSelectedFile();
					          fileNameForImageLoad = fileForImageLoad.getAbsolutePath();
					          System.out.println(fileNameForImageLoad);
					          
					          imageLoad = loadImage(fileNameForImageLoad);
					          image( imageLoad, 0, 0);
					          flagLoadImage = true;
					
					        }	        
					     }
					});
				}
				catch (Exception e) 
				{
					    e.printStackTrace();
				}		
			
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
	
	
	
	 static int[] colorArrayStream = new int[8];
	
	 

	 //server
	 static ServerSocket s12 ;
	 static Socket ss;
	public static void server() {
	
		try {
			s12 = new ServerSocket(2022);
			ss = s12.accept(); //accept-->accept the incoming request to the socket		
			Scanner sc = new Scanner(System.in);
			
			PrintStream p1 = new PrintStream(ss.getOutputStream());
			p1.println(colorArray[0]);	
			p1.println(colorArray[1]);		
			p1.println(colorArray[2]);
			p1.println(colorArray[3]);
			p1.println(colorArray[4]);		
			p1.println(colorArray[5]);		
			p1.println(colorArray[6]);
			p1.println(colorArray[7]);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				s12.close();
				System.out.println("closed s1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ss.close();
				System.out.println("closed ss");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			new display_client();		
		}		
	}
	

	
	public static void main(String _args[]) 
	{		
		PApplet.main(new String[] { asdat_server.ASDAT_server.class.getName() });	
			server();
	}
	
}