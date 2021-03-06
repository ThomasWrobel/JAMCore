package com.lostagain.Jam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.darkflame.client.SuperSimpleSemantics;
import com.darkflame.client.interfaces.SSSGenericFileManager.FileCallbackError;
import com.darkflame.client.interfaces.SSSGenericFileManager.FileCallbackRunnable;
import com.lostagain.Jam.CollisionMap.PolySide;
import com.lostagain.Jam.CollisionMap.PolygonCollisionMap;
import com.lostagain.Jam.CollisionMap.SceneCollisionMap;
import com.lostagain.Jam.Factorys.NamedActionSetTimer;
import com.lostagain.Jam.InstructionProcessing.ActionList;
import com.lostagain.Jam.InstructionProcessing.ActionSet;
import com.lostagain.Jam.InstructionProcessing.CommandList;
import com.lostagain.Jam.InstructionProcessing.CommandParameterSet;
import com.lostagain.Jam.InstructionProcessing.ConditionalLine;
import com.lostagain.Jam.InstructionProcessing.GamesAnswerStore;
import com.lostagain.Jam.InstructionProcessing.InstructionProcessor;
import com.lostagain.Jam.InstructionProcessing.ActionSet.TriggerType;
import com.lostagain.Jam.Interfaces.HasScreenManagement;
import com.lostagain.Jam.Interfaces.IsPopupPanel;
import com.lostagain.Jam.Interfaces.JamChapterControl;
import com.lostagain.Jam.Interfaces.TEMP_FunctionInterface;
import com.lostagain.Jam.Interfaces.hasInventoryButtonFunctionality;
import com.lostagain.Jam.InventoryItems.ItemMixRequirement;
import com.lostagain.Jam.Movements.MovementPath;
import com.lostagain.Jam.SaveMangement.HasCompressionSystem.CompressionResult;
import com.lostagain.Jam.SaveMangement.JamSaveGameManager;
import com.lostagain.Jam.Scene.SceneWidget;
import com.lostagain.Jam.SceneObjects.CollisionBox;
import com.lostagain.Jam.SceneObjects.PropertySet;
import com.lostagain.Jam.SceneObjects.SceneDialogueObjectState;
import com.lostagain.Jam.SceneObjects.SceneDivObjectState;
import com.lostagain.Jam.SceneObjects.SceneInputObjectState;
import com.lostagain.Jam.SceneObjects.SceneLabelObjectState;
import com.lostagain.Jam.SceneObjects.SceneObject;
import com.lostagain.Jam.SceneObjects.SceneObjectDatabase;
import com.lostagain.Jam.SceneObjects.SceneObjectDatabase_newcore;
import com.lostagain.Jam.SceneObjects.SceneObjectFactory;
import com.lostagain.Jam.SceneObjects.SceneObjectState;
import com.lostagain.Jam.SceneObjects.SceneSpriteObjectState;
import com.lostagain.Jam.SceneObjects.SceneVectorObjectState;
import com.lostagain.Jam.SceneObjects.Interfaces.IsSceneObject;
import com.lostagain.Jam.SceneObjects.Interfaces.IsInventoryItem.IconMode;

import lostagain.nl.spiffyresources.client.IsSpiffyGenericLogger;
import lostagain.nl.spiffyresources.client.spiffycore.FramedAnimationManager;
import lostagain.nl.spiffyresources.client.spiffycore.SpiffyCalculator;

/**
 * Various global game settings and utilities are managed here.
 * Like all core stuff, no visual or gwt specific implementation belongs here - normal (but gwt compatible) java only is allowed
 * 
 * @author darkflame
 *
 */
public class JAMcore {

	public static Logger Log = Logger.getLogger("JAMcore");

	/** 
	 * Determines if debug tools open
	 * this is set from the above string straight after loading
	 */
	public static Boolean DebugMode = false;

	/** The home directory of the host page or directory **/
	public static String homeurl = "";

	//Default locations of semantic data
	public static  String defaultNSFolder= "Semantics/";    //relative to html or code? if code should be ../
	public static  String defaultNSFile  = "OnotologyIndex.ntlist";     //This used to be "DefaultOntology.n3#" to match the SSSs default location before it auto-guessed it correctly from the url
	public static  String defaultNS      = "not correct"; //JAM.homeurl+defaultNSFolder+defaultNSFile+"#";//This was recently made absolute using homeurl, it used to be relative 


	// Inventory location (note; when the homeurl is set, this will be relative to it)
	// nb: any non-default inventory's will be in subdirectorys of the base url,
	// based on their name
	public static String inventory_url="InventoryItems/";


	// Player data
	final public static String DefaultUsername = "The Player"; //default player name (might be used in the game to refer to a non-logged in player genericly)
	public static String Username = DefaultUsername;
	public static String Organisation = "";
	public static String Client = "";
	public static String InterfaceSize = "Default";


	// Game Settings
	/** localloc should be set to "" in the html for when the game is hosted online,
	 * during development, however, it should be set to the directory of the games script **/	

	public  static String  GameName;
	public  static String  LocalFolderLocation;
	public  static String  LanguageExtension;
	public  static Boolean RequiredLogin;
	public  static Boolean HasServerSave;

	/**for some games with long save times autosave should be disabled from the interface **/
	public  static Boolean DisableAutoSave;


	/** Optional quality/cpu settings
	 * A value of 1 to 10 is expected.
	 * Optional items (ie, animations) in the game will be given quality values of 1 to 10, and only ones less then or equal too the current quality level will be displayed **/
	public static String Quality;
	
	/** 
	 *  "true" = on with logs
	 *  "false" = off no logs
	 *  "trueNoLog" = on with no logs 
	 *  "falseWithLogs" = off with logs
	 *  
	 ***/
	public static String DebugSetting;
	

	public static final String defaultorganisation = "";


	public static final String defaultname = "Knuffelmans";


	public static boolean loggedIn = false;


	/** keeps track of loading inventory items **/
	public static int itemsLeftToLoad = 0;

	//Temp functions to help migration to core
	//These should be removed once the relevant functions and classes are in the core
	//------------------------------
	//public static TempGetValueInterface getValueFunction;
	//public static TEMP_FunctionInterface runInstrunctionsFunction;	
	
	
	public static String usersCurrentLocation = "";

	/**
	 * Stores all the players inventories, letting them be looked up by name
	 * 
	 * @param String -  the inventory's name
	 * @param InventoryPanel - the inventory itself
	 * 
	 ***/
	public final static HashMap<String, InventoryPanelCore> allInventorys = new HashMap<String, InventoryPanelCore>();

	/**
	 * Stores the frames associated with each inventory
	 */
	public final static HashMap<String, IsPopupPanel> allInventoryFrames = new HashMap<String, IsPopupPanel>();
	
	/** 
	 * keeps track of commands to run after loading items.
	 * These are commands from the main script, not from scenes 
	 * **/
	public static ArrayList<CommandList> CommandsToRunAfterLoadingArray    = new ArrayList<CommandList>();
	public static ArrayList<String>      CommandsToRunAfterLoadingArrayIDs = new ArrayList<String>();
	public static boolean BusyAddingDivs = false;
	
	
	
	// max popup zdepth
	public static int z_depth_max = HasScreenManagement.INITIAL_MAX_ZINDEX; //1050;




	//--------------------------------------

	/** list of opened pages in active chapter**/
	public final static ArrayList<String> currentOpenPages = new ArrayList<String>();

	public final static ArrayList<String> locationpuzzlesActive = new ArrayList<String>();

//	private static final String SceneVectorObjectState = null;





	/**
	 * currently loaded scenes and pages.
	 * Switching between these should be fast.
	 * 
	 */
	public static SceneAndPageSet CurrentScenesAndPages;// = SceneObjectFactory.createSceneAndPageSet();

	public static boolean triggerSelectCheck;

	public static String pageToSelect;



	/**
	 * If your game uses a answer box for the player to, well, type answers into
	 * You should extend the JamAnswerBox class, then set this variable equal to it in your setup
	 */
	public static Optional<? extends JamAnswerBox> AnswerBox = Optional.absent();
	

	public static JamAnswerButton AnswerButton;

	/**
	 * stores the games chapters, each of which is its own tab panel containing scenes/pages for that chapter
	 */
	public static JamChapterControl GamesChaptersPanel;

	// Get size string from css
	public static String iconsizestring = ""; //$NON-NLS-1$



	// Scoreboard
	public static ScoreControll PlayersScore;
	
	// Clue box
	public static JamClueControll playersClues;// 
	public static PlayersNotebookCore PlayersNotepad; //has to



	public static boolean storyPageLoading = false;



	/**
	 * creates default inventory's and pagesets.
	 * Must be run at start for JamEngine to work
	 */
	public static void standardSetup() {
		//create default inventory and its frame
		//add the default inventory
		//------------
		String name = "Inventory Items";
		
		InventoryPanelCore.defaultInventory = SceneObjectFactory.createInventoryPanel(name, IconMode.Image);

		JAMcore.allInventorys.put(name, InventoryPanelCore.defaultInventory);

		Log.info("Making new inventory popup called"+name);


		PlayersInventoryFrame = SceneObjectFactory.createTitledPopUp(
				null,
				"50%", "25%", 
				GamesInterfaceTextCore.MainGame_Inventory, 
				InventoryPanelCore.defaultInventory ); 



		
		//add the default frame
		JAMcore.allInventoryFrames.put(name, PlayersInventoryFrame);
		
		//create the button for the default inventory and add to interface
		DefaultInventorysButton = InventoryPanelCore.createAndPlaceNewInventoryButton(InventoryPanelCore.defaultInventory,JAMcore.PlayersInventoryFrame ,"<BigDefaultInventoryButton>", 0);

		//----------
		//create default chapter
		JAMcore.CurrentScenesAndPages = SceneObjectFactory.createSceneAndPageSet("defaultChapter");
		
		
		
		//(more to move here)
		
		

	}
	

	static public hasInventoryButtonFunctionality DefaultInventorysButton;

	/** add a html story page to the loading queue **/
	static public void AddStoryTextToLoader(String newStoryTextHTML) {

		Log.info("story text:" + newStoryTextHTML+ " is queued to be loaded");

		PageLoadingData newPageLoadData = new PageLoadingData(newStoryTextHTML,	CurrentScenesAndPages);
		PageLoadingData.PageLoadingQueue.add(newPageLoadData);

		CheckLoadingQueue();

	}


	//public static void AnswerGiven(String ans){

	//	JAMcore.runInstrunctionsFunction.AnswerGiven(ans);

	//}


	/** if there's pages still to load, we load them here!**/
	static public void CheckLoadingQueue() {

		Log.info("checking loading queue");
		Log.info("current contents of queue is;");

		Iterator<PageLoadingData> loadingQueueContents = PageLoadingData.PageLoadingQueue.iterator();
		//display queue to log;
		while (loadingQueueContents.hasNext()) {
			Log.info("->" + loadingQueueContents.next().pageURL);
		}


		if (storyPageLoading) {
			return;
		} else {
			// check preloader status
			Log.info("checking loading queue and loading next item");
			if (PageLoadingData.PageLoadingQueue.size() > 0) {
				Log.info("loading:" + PageLoadingData.PageLoadingQueue.get(0));

				RequiredImplementations.screenManager.get().setNewPage(PageLoadingData.PageLoadingQueue.get(0));
			}
		}

	}

	public static void deseraliseAndRestoreStateFromString(String savestring){


		RequiredImplementations.saveManager.get().deseraliseAndRestoreStateFromString(savestring);
		
	//	JAMcore.runInstrunctionsFunction.deseraliseAndRestoreStateFromString(savestring);

	}
	//----
	//------------------
	//------------------------
	//--------------------------------
	//Optional functions

	/**
	 *  To help language support, all loaded text files are processed by this function 
	 * Any time a "__LAN" is found, it is replaced by the current language specific extension.
	 * eg. welcome__LAN.html becomes welcome_EN.html is the languageextension is set to EN
	 * 
	 * If no extension is set, the __LAN is removed completely.
	 * eg. welcome__LAN.html becomes welcome.html 
	 * 
	 * Remember to supply files for all the language extensions you support!
	 * **/
	public static String parseForLanguageSpecificExtension(String sourceString) {

		if (LanguageExtension.length()>1){
			sourceString=sourceString.replaceAll("__LAN","__"+LanguageExtension);
		} else {
			sourceString=sourceString.replaceAll("__LAN","");
		}

		return sourceString;
	}

	/** This will use the loaded GameTextDatabase to swap text IDs with the text they represented.
	 * To support multi language games all your user-visible text should be replaced with IDs and controlled
	 * via a CSV database file that the GameTextDatabase loads.
	 * You can then have different IDs for different languages. **/
	public static String parseForTextIDs(String sourceString) {

		//first we check if the text database is loaded.
		//the main game script should NOT be loaded before the text database is
		//thus is the text database is not loaded, we quit with a error

		if (!GameTextDatabase.isLoaded()){
			Log.severe("TEXT DATABASE NOT LOADED YET ERROR when trying to swap:"+sourceString);
			return sourceString;
		}


		sourceString = GameTextDatabase.replaceIDsWithText(sourceString,LanguageExtension);

		return sourceString;

	}

	public static void processInstructions(Object commandsToRun,String UniqueTriggerIndent, IsSceneObject ObjectThatCalledThis)
	{
		//JAMcore.runInstrunctionsFunction.processInstructionsImpl(commandsToRun, UniqueTriggerIndent, ObjectThatCalledThis);
		InstructionProcessor.processInstructionsImpl(commandsToRun,UniqueTriggerIndent,ObjectThatCalledThis);

	}

	//TODO: we might eventually get rid of IsSceneObject? Why not use SceneObject directly?
	public static void processInstructions(String command,String UniqueTriggerIndent, IsSceneObject ObjectThatCalledThis)
	{
		//JAMcore.runInstrunctionsFunction.processInstructionsImpl(command, UniqueTriggerIndent, ObjectThatCalledThis);
		InstructionProcessor.processInstructions(command,UniqueTriggerIndent,(SceneObject) ObjectThatCalledThis);

	}

	public static void processInstructionsWhenReady(String commands,
			String uniquetriggerstring){

		CommandList commandsToRun = InstructionProcessor.StringToCommandList(commands);

		processInstructionsWhenInventorysReady(commandsToRun,uniquetriggerstring);

	}

	/**
	 * eventually we should replace all calls to this with calls to the instruction processor directly
	 * 
	 * @param instructionset
	 * @param UniqueTriggerIndent
	 * @param currentObject
	 */
	public static void processInstructions(String instructionset,
			String UniqueTriggerIndent, SceneObject currentObject) {

		InstructionProcessor.processInstructions(instructionset,
				UniqueTriggerIndent, currentObject);

	}


	// this will wait till loading of all items is done, then runs the command
	// currently only TIGs and the items themselves are waited for
	public static void processInstructionsWhenInventorysReady(CommandList commands,
			String uniquetriggerstring) {

		if (itemsLeftToLoad == 0 && !BusyAddingDivs) {

			// run the commands
			//	InstructionProcessor.processInstructions(commands, uniquetriggerstring, null);

			processInstructions(commands, uniquetriggerstring, null);


		} else {
			Log.info("itemsLeftToLoad = " + itemsLeftToLoad + " ");	
			Log.info("adding commands to run after loading");

			// add to CommandsToRunAfterLoadingArray
			CommandsToRunAfterLoadingArray.add(commands);
			CommandsToRunAfterLoadingArrayIDs.add(uniquetriggerstring);

		}

	}

	/** removes a language specific extension from a given filename 
	 * Used purely for interface displaying filenames **/
	public static String removeLanguageSpecificExtension(String fileName) {

		if (LanguageExtension.length()>1){
			fileName=fileName.replaceAll("__"+LanguageExtension,"");
		} 

		return fileName;
	}

	public static void setDefaultNSFile(String defaultNSFile) {
		JAMcore.defaultNSFile = defaultNSFile;
	}
	/**
	 * Should be set before any semantic loading
	 *
	 * @param url    - the root home folder, either absolute or relative to the filemanagers default location. (ie, giving this to the filemanager should get to the home folder)
	 */
	public static void setHomeURL(String url){
		homeurl = url;

		defaultNSFolder= "Semantics/"; //relative to html or code? if code should be ../
		
		String specifiedSemanticIndex = RequiredImplementations.BasicGameInformationImplemention.get().getSemanticsLocation();
		if (specifiedSemanticIndex.length()>3){
			defaultNSFile  = specifiedSemanticIndex;     //This used to be "DefaultOntology.n3#" to match the SSSs default location before it auto-guessed it correctly from the url
			
		} else {
			defaultNSFile  = "OnotologyIndex.ntlist";     //This used to be "DefaultOntology.n3#" to match the SSSs default location before it auto-guessed it correctly from the url
			
		}
		
		
		
		
		defaultNS      = homeurl+defaultNSFolder+defaultNSFile+"#";//This was recently made absolute using homeurl, it used to be relative 

		inventory_url  = homeurl + "InventoryItems/"; //homeurl + "InventoryItems/"

	}

	//public static void setTempGetValueFunction(TempGetValueInterface getValueFunction) {
	///	JAMcore.getValueFunction = getValueFunction;
	//}

	//public static void setTempRunInstructionsInterface(TEMP_FunctionInterface runthisfunctions) {
	//	JAMcore.runInstrunctionsFunction = runthisfunctions;
	//}




	//should be run asap as many things depend on these variables
	static JAMcore setupEngineVariables(
			String gamename,
			String Homedirectory,
			String LocalFolderLocation, 
			String LanguageExtension, 
			boolean RequiredLogin, 
			boolean HasServerSave,
			boolean DisableAutoSave, 
			String Quality, 
			String DebugSetting) {

		JAMcore.setHomeURL(Homedirectory);

		JAMcore.GameName = gamename;
		JAMcore.LocalFolderLocation = LocalFolderLocation;
		JAMcore.LanguageExtension   = LanguageExtension;
		JAMcore.RequiredLogin       = RequiredLogin;		
		JAMcore.HasServerSave       = HasServerSave;
		JAMcore.DisableAutoSave     = DisableAutoSave;
		JAMcore.Quality             = Quality;
		JAMcore.DebugSetting        = DebugSetting;

		return null;
	}

	static public String SwapCustomWords(String input_string) {
		return SwapCustomWords(input_string,true);
	}

	/** swaps game variables with text eg, <USERNAME> will play the username in the text **/
	static public String SwapCustomWords(String input_string,boolean replaceGameVariablesWithValues) {

		if (loggedIn) {
			// SwapUserSpecificWords();
			input_string = input_string.replaceAll("<USERNAME>", Username); //$NON-NLS-1$
			input_string = input_string.replaceAll(
					"<ORGANISATION>", Organisation); //$NON-NLS-1$
		} else {
			input_string = input_string.replaceAll("<USERNAME>", defaultname); //$NON-NLS-1$
			input_string = input_string.replaceAll(
					"<ORGANISATION>", defaultorganisation); //$NON-NLS-1$

		}

		// replace variables
		if (replaceGameVariablesWithValues){
			input_string = GameVariableManagement.replaceGameVariablesWithValues(input_string);
		}

		// bookshelf link swap
		// <bookshelf="www.blah.com">
		// <a class="bookshelf" href="www.blah.com"></a>
		// get location
		int starthere = 0;
		// int BookShelfNum = 0; // change to global later
		while (input_string.indexOf("<bookshelf=\"", starthere) > 0) { //$NON-NLS-1$

			starthere = starthere + 1;
			int bookshelfloc = input_string.indexOf("<bookshelf=\""); //$NON-NLS-1$
			int bookshelfendloc = input_string.indexOf("\">", bookshelfloc); //$NON-NLS-1$
			String stringbefore = input_string.substring(0, bookshelfloc);
			String stringurl = input_string.substring(bookshelfloc + 12,
					bookshelfendloc);
			String stringafter = input_string.substring(bookshelfendloc + 2);

			// input_string =
			// stringbefore+"<a class=\"bookshelf\" href=\""+stringurl+"\"
			// target=\"_blank\"><img border=\"0\" src=\"blank.gif\"
			// width=\"30\" height=\"29\"></a>"
			// +stringafter;

			input_string = stringbefore
					+ "<a style=\"height:32px\" href=\"" + stringurl + "\" target=\"_blank\"><img height=\"29\" width=\"32\" border=\"0\" class=\"bookshelf\" src=\"blank.gif\"/></a>" + stringafter; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return input_string;
	}
	public static void testForGlobalActions(Object type, String Parameter, IsSceneObject sourceObject){

		//JAMcore.runInstrunctionsFunction.testForGlobalActions( type,  Parameter,  sourceObject);

		InstructionProcessor.testForGlobalActions( (TriggerType)type,  Parameter, (SceneObject) sourceObject);


	}
	/**Determines if we are ignoring the keyboard at the moment
	 * This is used mostly when the user is on a textbox, as game shortcuts shouldnt work 
	 * while your typeing **/
	public static boolean IgnoreKeyPresses =false;




	static public boolean isIgnoreKeyPresses() {
		return IgnoreKeyPresses;
	}


	public static void setIgnoreKeyPresses(boolean ignoreKeyPresses) {
		IgnoreKeyPresses = ignoreKeyPresses;
	}
	// keyboard detection:
	public enum KeyState {
		NewlyDown,
		CurrentlyDown,
		NewlyReleased //not used yet
	}
	/** Stores a list of what keys are current held down on the keyboard
	 * Keys are identified by their keycodes **/
	public static HashMap<Integer,KeyState> HeldKeys = new HashMap<Integer,KeyState>();

	/** debug window <br> An overall debug for the game. Opened by typing "DebugWindow" into the answer box **/
	//public final static debugWindow DebugWindow = new debugWindow();

	public static IsSpiffyGenericLogger GameLogger;// = SpiffyLogBox.createLogBox(true);

	//final static Button EnterAns = new Button(GamesInterfaceText.MainGame_Submit);
	public static Optional<? extends JamAnswerButton> EnterAns = Optional.absent();// = new GWTAnswerButton(GamesInterfaceText.MainGame_Submit);

	/** Overall control script stored here once loaded **/
	public static String controllscript = "";

	public static boolean autoserversaveon = false;




	public static void CorrectAnswerProcess(int StartHere) {

		// set unique action identifier (has to be manually set elsewhere)
		// This is to stop duplicate scoring
		int posOfLastNewline  = controllscript.lastIndexOf("ans=", StartHere);

		int endOfAnsLine      = controllscript.indexOf("\n", posOfLastNewline);

		String CurrentAnsLine = controllscript.substring(posOfLastNewline,	endOfAnsLine);

		// System.out.print("-->"+CurrentAnsLine+"/n");
		// Window.alert("-->"+CurrentAnsLine+"/n");

		// get position of next answer
		int NextAnsPos = controllscript.indexOf("ans=", StartHere); //$NON-NLS-1$
		//	System.out.print("/n ans next pos=" + NextAnsPos); //$NON-NLS-1$

		// Isolate instructions to process
		final String instructionset = controllscript.substring(StartHere,
				NextAnsPos);


		processInstructions(instructionset, CurrentAnsLine.trim(), null);

		// save the game
		if (autoserversaveon == true) {
			silentlySaveGameToServer();
		};

		Log.info("setting text pointlessly");

		if (EnterAns.isPresent()){	
			EnterAns.get().setText(GamesInterfaceTextCore.MainGame_Submit);
		}
	}



	public static void silentlySaveGameToServer() {
		//SaveGameManager.SEVER_OPTIONS.saveGameToServer(); //need optional interface to this
		String saveGameData = RequiredImplementations.saveManager.get().CreateSaveString();
		
		if (OptionalImplementations.StringCompressionMethod.isPresent()){
			OptionalImplementations.StringCompressionMethod.get().compress(saveGameData, new CompressionResult() {					
				@Override
				public void gotResult(String result) {
					RequiredImplementations.saveManager.get().saveGameToServer(result);
				}
			});
		} else {
			RequiredImplementations.saveManager.get().saveGameToServer(saveGameData);
		}
	}


	public static void specialCodeEntered(String ans) {
		// goto special code function

		Log.info("Special code entered");

		FileCallbackRunnable onSuccess =  new FileCallbackRunnable() {

			@Override
			public void run(String responseData, int responseCode) {
				Log.info("special response recieved");

				GameLogger.log("Response to special code check;"
						+ responseData,"green");

				if (responseData.endsWith("VALID ID")) {

					// we look for where to send the gamer next;

					// search for {SPECIAL CODE} on current chapter
					String searchfor = "ans="
							+ usersCurrentLocation.toLowerCase()
							+ ": {special code}";

					GameLogger.info("looking for "
							+ searchfor);

					int AnswerIndex = controllscript.toLowerCase()
							.indexOf(searchfor);

					if (AnswerIndex == -1) {
						GameLogger.info("not found ");
						return;
					}

					Log.info("processing instructions:");

					// process instructions;
					CorrectAnswerProcess(AnswerIndex + 14
							+ usersCurrentLocation.length() + 6);

				}

			}
		};

		FileCallbackError onError = new FileCallbackError() {

			@Override
			public void run(String errorData, Throwable exception) {
				GameLogger.info(" error checking special code");
			}
		};


		RequiredImplementations.getFileManager().getText("scripts/checkcode.php",false,onSuccess,onError , true);


		/*

		// check code
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST,		"scripts/checkcode.php");

		// "filename=blahblah.txt&contents=meeeepmeeep"
		try {
			requestBuilder.sendRequest(
					"checkthis=" + ans, new RequestCallback() { //$NON-NLS-1$
						@Override
						public void onError(Request request, Throwable exception) {
							JAMcore.GameLogger.info("\n error");
						}

						@Override
						public void onResponseReceived(Request request,
								Response response) {

							Log.info("special response recieved");

							JAMcore.GameLogger.info("\n response"
									+ response.getText());

							if (response.getText().endsWith("VALID ID")) {

								// we look for where to send the gamer next;

								// search for {SPECIAL CODE} on current chapter
								String searchfor = "ans="
										+ JAMcore.usersCurrentLocation.toLowerCase()
										+ ": {special code}";

								JAMcore.GameLogger.info("\n looking for "
										+ searchfor);

								int AnswerIndex = JAMcore.controllscript.toLowerCase()
										.indexOf(searchfor);

								if (AnswerIndex == -1) {
									JAMcore.GameLogger.info("\n not found ");
									return;
								}

								Log.info("processing instructions:");

								// process instructions;
								CorrectAnswerProcess(AnswerIndex + 14
										+ JAMcore.usersCurrentLocation.length() + 6);

							}

						}
					});
		} catch (RequestException ex) {
			JAMcore.GameLogger.info("\n error");
		}*/

	}
	/** Control script is parsed into the answer store for quicker
	 * look up of answers and correct response script for them.   **/
	public static GamesAnswerStore gamesAnswerStore;




	//Has to eventually be moved to the core
	//Currently depends on a few other functions being moved over first
	/** Runs instructions based on a typed in answer **/
	public static void AnswerGiven(String Ans) {

		// if the ans is a special code;
		if ((Ans.startsWith("SPC")) && (Ans.length() == 12)) {
			specialCodeEntered(Ans);
			return;
		}

		//if we have a enter ans box we set its text to reflect we are testing the answer
		if (EnterAns.isPresent()){
			EnterAns.get().setText(GamesInterfaceTextCore.MainGame_Sending);
		}

		// trim and lower case the answer;
		Ans = Ans.trim().toLowerCase();

		// NEW ANS test;
		boolean isCalc = SpiffyCalculator.isCalculation(Ans);

		CommandList RunCode = gamesAnswerStore.checkAns(Ans, usersCurrentLocation.toLowerCase(),	isCalc);

		if (RunCode != null) {

			Log.info("processing instructions:" + RunCode);



			InstructionProcessor.processInstructions(RunCode,
					Ans + "Given@" + usersCurrentLocation.toLowerCase(), null);

			// reset message on button
			if (EnterAns.isPresent()){				
				EnterAns.get().setText(GamesInterfaceTextCore.MainGame_Submit);
			}

			// save the game
			if (autoserversaveon == true) {
				silentlySaveGameToServer();
				// DebugWindow.setText("SAVEING GAME....");
				//	SaveGameManager.SEVER_OPTIONS.saveGameToServer(); 
			};

			return;
		}

		// Else its a calculation
		if (isCalc) {

			String result = "" + SpiffyCalculator.AdvanceCalculation(Ans);
			System.out.println("-" + result + "-");

			if (result.endsWith(".0")) {
				//System.out.println("=" + result + "=");

				result = result.replaceFirst("\\.0", "");
				//	System.out.println("=" + result + "=");

			}


			//	messagehistory.AddNewMessage_notrecorded("-- " + Ans);



			if (FeedbackHistoryCore.feebackHistoryVisualiser.isPresent()){
				FeedbackHistoryCore.feebackHistoryVisualiser.get().AddNewMessage_notrecorded("-- " + Ans);

			}

			//Feedback.setText(GamesInterfaceText.MainGame_Mathsbeforeans + " "
			//		+ result + " " + GamesInterfaceText.MainGame_Mathsafterans);


			RequiredImplementations.setCurrentFeedbackText(GamesInterfaceTextCore.MainGame_Mathsbeforeans + " "
					+ result + " " + GamesInterfaceTextCore.MainGame_Mathsafterans);


			//messagehistory.AddNewMessage("<div class=\"MessageHistoryReplyStyle\" >  " + GamesInterfaceText.MainGame_Mathsbeforeans + result + GamesInterfaceText.MainGame_Mathsafterans + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$



			FeedbackHistoryCore.AddNewMessage("<div class=\"MessageHistoryReplyStyle\" >  " + GamesInterfaceTextCore.MainGame_Mathsbeforeans + result + GamesInterfaceTextCore.MainGame_Mathsafterans + "</div>"); //$NON-NLS-1$ //$NON-NLS-2$


			// reset message on button
			if (EnterAns.isPresent()){	
				EnterAns.get().setText(GamesInterfaceTextCore.MainGame_Submit);
			}

			return;
		}


	}
	/** this logger lets you globally disable all the other console logs **/
	public static Logger rootlogger = Logger.getLogger("");



	/**
	 * despite the name, this will not yet contain all loggers. 
	 * In future it will allow finer grain controll from the object inspector
	 */
	public static HashSet<Logger> ALL_LOGGERS = Sets.newHashSet();

	/** Determines if the games logs are shown or not.
	 * No longer turn soff the root logger
	 * you can do that yourself with; 
			//rootlogger.setLevel(Level.OFF);	<br><br><br>
			 * This function also populates the set storing all loggers (ALL_LOGGERS)
	 *  **/
	public static void setLoggingEnable(boolean b) {

		Log.info("setting JamCore log active:"+b);

		if (b){

			rootlogger.setLevel(Level.INFO);	
			Log.setLevel(Level.INFO);

			//Logger.getLogger("JAMCore").setLevel(Level.INFO);

			JAMcore.Log.setLevel(Level.INFO);
			
			//manually turn some off here

			//Logger.getLogger("JAM.SceneDataBox").setLevel(Level.OFF);
			//Logger.getLogger("JAM.InstructionProcessor").setLevel(Level.OFF);

			//Disable some of the loggers for inherited projects and other class's we don't need to debug right not
			Logger.getLogger("sss").setLevel(Level.SEVERE); //disable super simple semantics log (new)
			//Logger.getLogger("JAM").setLevel(Level.OFF);
			//Logger.getLogger("JAM").setLevel(Level.OFF);




			//Logger.getLogger("ParagraphCollection").setLevel(Level.OFF);
			PropertySet.Log.setLevel(Level.SEVERE);//disable log for PropertySet class
			CollisionBox.Log.setLevel(Level.SEVERE);//disable log for JAMCore.CollisionBox class	
			
			//Logger.getLogger("JAMCore.ActionList").setLevel(Level.SEVERE); //disable log for ActionSet class
			//Logger.getLogger("JAMCore.GameVariableManagement").setLevel(Level.SEVERE); //disable log for GameVariableManagement class
			ActionList.Log.setLevel(Level.SEVERE);//disable log for ActionSet class
			
			//SpiffyDragPanel.Log.setLevel(Level.SEVERE);//disable drag panels log
			
			
			GameVariableManagement.Log.setLevel(Level.SEVERE);
			PolygonCollisionMap.GreyLog.setLevel(Level.INFO);
			

			//disable logs on states
			SceneSpriteObjectState.Log.setLevel(Level.SEVERE);
			SceneDivObjectState.Log.setLevel(Level.SEVERE);
			SceneLabelObjectState.Log.setLevel(Level.SEVERE);
			SceneObjectState.Log.setLevel(Level.SEVERE);
			SceneInputObjectState.Log.setLevel(Level.SEVERE);
			SceneVectorObjectState.Log.setLevel(Level.SEVERE);
			SceneLabelObjectState.Log.setLevel(Level.SEVERE);
			SceneDialogueObjectState.Log.setLevel(Level.SEVERE);
			
			GameTextDatabase.Log.setLevel(Level.SEVERE);
			SceneObject.SOLog.setLevel(Level.SEVERE);
			
			/*
			Logger.getLogger("SpiffyDragPanel").setLevel(Level.OFF);//disable drag panels log

			//internal loggers
			//com.darkflame.client.JargScene.PropertySet.Log.setLevel(Level.OFF);
			//com.darkflame.client.JargScene.SceneObjectData.Log.setLevel(Level.OFF);

			Logger.getLogger("JAMCore.PropertySet").setLevel(Level.OFF);//disable log for PropertySet class
			Logger.getLogger("JAMCore.LocationTabSet").setLevel(Level.OFF);//disable log for LocationTabSet class
			Logger.getLogger("JAMCore.MovementPath").setLevel(Level.OFF);//disable log for MovementPath class
			Logger.getLogger("JAMCore.ActionSet").setLevel(Level.OFF); //disable log for ActionSet class
			Logger.getLogger("JAMCore.ActionList").setLevel(Level.OFF); //disable log for ActionSet class

			Logger.getLogger("JAM.AnimatedIcon").setLevel(Level.OFF); //disable log for Animated Icon class
			Logger.getLogger("JAM.GameTextDatabase").setLevel(Level.OFF); //disable for the text database
			Logger.getLogger("JAM.TypedLabel").setLevel(Level.OFF);
			Logger.getLogger("JAM.SceneWidget").setLevel(Level.OFF);
			Logger.getLogger("ParagraphCollection").setLevel(Level.OFF);
			Logger.getLogger("JAM.SceneDialogObject").setLevel(Level.OFF);
			Logger.getLogger("JAMCore.SceneObject").setLevel(Level.OFF);
			Logger.getLogger("JAM.PopUpWithShadow").setLevel(Level.OFF);

			Logger.getLogger("JAM.SaveStateManager").setLevel(Level.OFF);	


			Logger.getLogger("SceneDivObjectData").setLevel(Level.OFF);
			Logger.getLogger("SceneObjectData").setLevel(Level.OFF);
			Logger.getLogger("SceneDialogObjectData").setLevel(Level.OFF);
			Logger.getLogger("SceneVectorObjectData").setLevel(Level.OFF);

			Logger.getLogger("SceneVectorObject").setLevel(Level.OFF);
			Logger.getLogger("JAM.ConditionalLine").setLevel(Level.OFF);

			Logger.getLogger("JAM.ChangePasswordBox").setLevel(Level.OFF);
			Logger.getLogger("JAM.GamesAnswerStore").setLevel(Level.OFF);
			Logger.getLogger("JAM.InstructionProcessor").setLevel(Level.OFF);

			Logger.getLogger("JamMain").setLevel(Level.OFF);

			Logger.getLogger("JAM.SceneWidget").setLevel(Level.OFF);

			Logger.getLogger("JAM.LoginBox").setLevel(Level.OFF);
			Logger.getLogger("Jam.AssArray").setLevel(Level.OFF);
			Logger.getLogger("JAM.TypedLabel").setLevel(Level.OFF);*////
			
			
			InstructionProcessor.Log.setLevel(Level.INFO);
			
			
			//populate logger set so they can be turned on/off while its running

			ALL_LOGGERS.add(InstructionProcessor.Log);
			
			ALL_LOGGERS.add(SceneObject.SOLog);
			ALL_LOGGERS.add(ActionList.Log);
			ALL_LOGGERS.add(ActionSet.Log);
			ALL_LOGGERS.add(SceneWidget.Log);
			ALL_LOGGERS.add(GameVariableManagement.Log);

			ALL_LOGGERS.add(ConditionalLine.Log);
			ALL_LOGGERS.add(GamesAnswerStore.Log);
			
			ALL_LOGGERS.add(InventoryPanelCore.Log);

			ALL_LOGGERS.add(ItemMixRequirement.Log);
			
			ALL_LOGGERS.add(MovementPath.Log);
			ALL_LOGGERS.add(SceneCollisionMap.Log);
			ALL_LOGGERS.add(PolySide.Log);
			
			
			//

			ALL_LOGGERS.add(FramedAnimationManager.Log);
			ALL_LOGGERS.add(NamedActionSetTimer.Log);
			ALL_LOGGERS.add(JamSaveGameManager.Log);
			ALL_LOGGERS.add(SceneObjectDatabase_newcore.DBncLog);
			

			
			
			//
			
			ALL_LOGGERS.add(CommandParameterSet.Log);
			ALL_LOGGERS.add(PolygonCollisionMap.GreyLog);
			ALL_LOGGERS.add(SceneObjectDatabase.DBLog);
			ALL_LOGGERS.add(AssArray.Log);
			ALL_LOGGERS.add(PropertySet.Log);
			
			ALL_LOGGERS.add(DialogueCollection.Log);
			ALL_LOGGERS.add(TypedLabelCore.Log);
			
			
			//
			
		} else {


			Log.info("setting log off");

			rootlogger.setLevel(Level.OFF);	
			Log.setLevel(Level.SEVERE);
			Logger.getLogger("SSS").setLevel(Level.SEVERE);	
			Logger.getLogger("JAMCore").setLevel(Level.SEVERE);

			JAMcore.Log.setLevel(Level.SEVERE);

			GameVariableManagement.Log.setLevel(Level.SEVERE);

		}


	}


	/** Called a loop out of tradition more then anything else.
	 * It isnt a loop, it just sets up the handlers if they have not already been set up. 
	 * This used to be the  main way to interact with the game - specifically typeing answers
	 * in the main box and hitting enter.
	 * Now its just one of many methods for possible game interaction.
	 * Still, if your wondering where the answer box is set up, here it is!**/
	public static void maingameloop() {

		if (GamesAnsBoxSetup) {
			return;
		}
		GamesAnsBoxSetup = true; //this ensures this stuff is only run once.



		// We add the answer listener to the main answer box;
		// Window.setTitle("adding click handle for entering answers");
		if (EnterAns.isPresent()){
		EnterAns.get().addOnClickRunnable(new Runnable() {			
			@Override
			public void run() {
				// if over one letters scan if its correct answer

				//(answerbox should be present if enter ans is)
				if (AnswerBox.get().getText().length() > 1) {
					Log.info("answer given from enter");
					AnswerGiven(AnswerBox.get() .getText());
				}


			}
		});
		}
		/*
			EnterAns.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// if over one letters scan if its correct answer
					if (JAMcore.AnswerBox.getText().length() > 1) {
						Log.info("answer given from enter");
						AnswerGiven(JAMcore.AnswerBox.getText());
					}
				}
			});*/

		//detects "Enter" being pressed (which is KeyCode13)
		//		AnswerBox.addKeyDownHandler(new KeyDownHandler() {
		//			@Override
		//			public void onKeyDown(KeyDownEvent event) {
		//
		//				int Key = event.getNativeKeyCode();
		//				
		//				if (Key == 13) {
		//					AnswerBox.setText(RemoveCartReturns(AnswerBox.getText())
		//							.trim());
		//
		//					// if over one letters scan if its correct answer
		//					if (AnswerBox.getText().length() > 1) {
		//						Log.info("answer given from keydown");
		//						AnswerGiven(AnswerBox.getText());
		//					}
		//
		//				}
		//
		//			}
		//
		//		});

		//advanceGameLoadingBar(); //TODO: make interface for this?

		if (OptionalImplementations.gamesLoadMonitor.isPresent()){
			OptionalImplementations.gamesLoadMonitor.get().advanceGameLoadingBar();
		}

	}
	// global games running flag
	public static boolean GamesAnsBoxSetup = false;        // This is only false at the start while loading.

	public static boolean StartOfScriptProcessed = false;   //only  false before startofgamescript() is run




	/**
	 * Update Game state from string <br><br>
	 * This is mostly used in html games for two situations;<br>
	 * a) Loading a game from a link, in which case a savegame starting with JamSaveGameManager.START_OF_GAMEDATA_MARKER is expected<br><br>
	 * b) A link in the game triggering a answer, in which case that answeer string is expected <br>
	 * We can also use this status to triggger debug commands, but these will change from game to game so shouldn't ever
	 * be considered part of the engine<br>
	 * <br>
	 * @param PostString
	 */
	static public void updateGameState(String GameUpdateString) {


		if (GameUpdateString.startsWith("DEBUG")) { //$NON-NLS-1$

			// processInstructions("\n - AddItem = 1 Amuse \n- AddItem = 1 Cuypersmusical \n- AddItem = 1 Krantenartikel \n- AddItem = 1 Moordenaar \n- AddItem = 1 Portretcollage \n- AddItem = Loep \n- AddProfile = Pierre Cuypers junior.html \n- AddProfile = Pierre J.H. Cuypers.html \n- NewChapter = \"1 Welkom\" \n- StoryBox = 1 Welkom.html \n- AddMusicTrack = 1 INTRO.mp3 \n- SetScore = 2500 \n- PointsAwardedFor =  \n- SetLocation = Start \n- SetBackgroundImage = GameTextures/CCmonPCdark.jpg \n- SetClockLadyIcon = .png,1 \n- SetInventoryIcon = .png,6  \n","debug");

			processInstructions(
					"- AddItem = newspaper \n - AddItem = TextScrollTest \n - AddItem = whokilledhans \n - AddItem = magnifyingglass \n - AddItem = TigDemo2 \n - AddProfile = Pierre J H Cuypers.html \n - StoryBox = 1 Welkom.html \n - StoryBox = 1.1 Help.html \n - StoryBox = 1.2 Score.html \n - SelectPage = 1 Welkom.html", "Debug", null); //$NON-NLS-1$
		}

		if (GameUpdateString.startsWith(JamSaveGameManager.START_OF_GAMEDATA_MARKER)) { //currently "LoadGameData=" in the savestate manager

			//currentLoadingText = "Loading savegame data from string..";

			if (OptionalImplementations.gamesLoadMonitor.isPresent()){
				//	OptionalImplementations.gamesLoadMonitor.get().setGameLoadStatus(JAM.currentLoadingText);
				OptionalImplementations.gamesLoadMonitor.get().advanceGameLoadingBar();
			}

			final String GameData = GameUpdateString.substring(13);//used to be 12

			// de-encrypt string and load the data
			JamSaveGameManager.decompressAndLoad(GameData,true);
			// ------

		} else {
			Log.info("answer given from updatestatus");
			// Window.alert(PostString);
			AnswerGiven(GameUpdateString);
		}

	}


	/** 
	 * This will trigger the games script once its loaded
	 * (Determined by its length being greater then 5)
	 *  - This probably should be tested manually rather then repeated like this
	 **/
	public static void start_game_script_when_ready() {

		//ensure we have everything we need and we havn't been run already
		if (controllscript.length() < 5 || 
				!GameTextDatabase.isLoaded()    ||
				StartOfScriptProcessed  == true) 
		{
			Log.info("waiting for controllscript or GameTextDatabase to load.");			
			Log.info("controllscript length ="+controllscript.length());
			return;
		}

		//we are ready to start
		StartOfScriptProcessed = true; //should be true only when all the prerequisites are loaded

		//Now we know the database and controll script is loaded we trigger any post-load commands that might have been set
		RequiredImplementations.BasicGameFunctionsImplemention.get().postDatabaseLoadSetup();

		Log.info("Start_of_game_script triggered");


		if (OptionalImplementations.gamesLoadMonitor.isPresent())
		{				
			OptionalImplementations.gamesLoadMonitor.get().setGameLoadStatus("start of game script triggered");
			OptionalImplementations.gamesLoadMonitor.get().advanceGameLoadingBar();			
		}

		// Set loading text
		//MainStoryText.setText(GamesInterfaceText.MainGame_StoryText_Loading);
		//Feedback.setText(GamesInterfaceText.MainGame_LoadingNewGame);
		RequiredImplementations.interfaceVisualElements.get().setCurrentFeedbackText(GamesInterfaceTextCore.MainGame_LoadingNewGame);


		//-------------
		//MainStoryText.setText(GamesInterfaceText.MainGame_StoryText_Loading + "...");		
		//Feedback.setText(GamesInterfaceText.MainGame_LoadingNewGame
		//		+ "...",true); //the true disables the sound effect);

		RequiredImplementations.interfaceVisualElements.get().setCurrentFeedbackText(GamesInterfaceTextCore.MainGame_LoadingNewGame + "...",true);


		// grab start commands
		int FirstBitsStart = controllscript.indexOf("Start:", 0) + 6; //$NON-NLS-1$
		int FirstBitsEnd   = controllscript.indexOf("ans=", 0); //$NON-NLS-1$

		// Isolate instructions to process
		final String instructionset = controllscript.substring(FirstBitsStart, FirstBitsEnd);

		Log.info("Startup instruction set : " + instructionset + " \n -instruction set- \n"); 
		GameLogger.log("Startup instruction set : ","green"); 
		GameLogger.info(instructionset); 

		// if theres no preloaded instructions, then we process the
		// first lot
		if (StartFromURL.length()<2) {
			processInstructions(instructionset, "Start", null);

		} else {
			//we run the start from url data rather then the start of game script 
			updateGameState(StartFromURL);
			//clear the variable to ensure it cant be run again
			StartFromURL="";
		}

		maingameloop();



	}
	public static String StartFromURL = "";



	/**
	 *  Starts the game from the controll script specified
	 *  
	 * @param fileurl - the location of the game controll script
	 * @param loadStandardOtherFiles - will trigger the loading of the item mix script and the semantic database at their standard locations
	 */
	static public void startGameFromControllFile(final String fileurl,boolean loadStandardOtherFiles) {

		if (loadStandardOtherFiles){
			try {			
				JAMcore.LoadSemanticDatabase();
			} catch (Exception e2) {
				Log.info("no semantic database found");

			}

			// Load item mix script
			try {
				InventoryPanelCore.LoadItemMixScript();
			} catch (Exception e2) {
				Log.info("item mix script file error");
			}
		}


		FileCallbackRunnable onResponse = new FileCallbackRunnable(){
			@Override
			public void run(String responseData, int responseCode) {


				controllscript = responseData;

				//advance loadingdiv, if theres one
				//	currentLoadingText="Preparing page";
				//	advanceGameLoadingBar();
				if (OptionalImplementations.gamesLoadMonitor.isPresent()){					
					OptionalImplementations.gamesLoadMonitor.get().setGameLoadStatus("Preparing page");
					OptionalImplementations.gamesLoadMonitor.get().advanceGameLoadingBar();

				}


				// crop till start;
				// "Game Controll Starts Here:"

				int StartIndex = controllscript.indexOf("Game Controll Starts Here:"); 

				// catch error //TODO: replace with different error method
				if (StartIndex == -1) {
					Log.severe("controll file not recieved;" + responseData); 
					//MainStoryText.setText("controll file not recieved;" + responseData); 
					return;
				}

				controllscript = controllscript.substring(StartIndex,controllscript.length());

				// swap language specific words
				controllscript = parseForLanguageSpecificExtension(controllscript);

				// swap custom words
				Log.info("swapping custome words in controllscript ");

				controllscript = SwapCustomWords(controllscript,false); //false as we dont want to burn in dynamic values


				//we have to ensure the text database is loaded before continuing.
				//so we make a runable of the remaining commands	

				Runnable remainingSetup = new Runnable(){
					@Override
					public void run() {

						//put text IDs into control panel (as we have to wait for the database to load in order to know what lans are avliable!)
						//JAM.ControlPanel.updateLans(); //TODO: we probably need a generic callback that other implementations can access when the text database is loaded

						// swap TextIds for text
						controllscript=parseForTextIDs(controllscript);

						Log.info("setting up answers");

						// set up answers
						gamesAnswerStore = new GamesAnswerStore(controllscript);
						Log.info("ans set up");

						//advance loadingdiv, if theres one	
						//	currentLoadingText="setup done";
						//	advanceGameLoadingBar();

						if (OptionalImplementations.gamesLoadMonitor.isPresent()){

							OptionalImplementations.gamesLoadMonitor.get().setGameLoadStatus("setup done");
							OptionalImplementations.gamesLoadMonitor.get().advanceGameLoadingBar();

						}

						// once loaded we start the main game loop
						//JAMcore.maingameloop();

						//fire trigger main game script start now we have loaded the controll script
						//Note; this might still have to wait for other things. So those things should trigger this too.
						Log.info("start_game_script_when_ready");

						start_game_script_when_ready();
					}

				};

				//then we test if we can run those commands straight away or not.
				if (GameTextDatabase.isLoaded()){
					//we run that code straight away if its already loaded

					Log.info("GameTextDatabase already loaded, continueing to setup answers");
					remainingSetup.run();

				} else {

					Log.info("GameTextDatabase not loaded, waiting to setup answers");
					//else we set the rest of the code to run after it is loaded
					GameTextDatabase.setRunOnCompleteCallback(remainingSetup);
				}

			}
		};

		//what to do if theres an error
		FileCallbackError onError = new FileCallbackError(){
			@Override
			public void run(String errorData, Throwable exception) {
			//	System.out.println("http failed"); //$NON-NLS-1$
				Log.severe("Retrieving control file failed:"+fileurl);

			}

		};



		Log.info("Retrieving control file:"+fileurl);



		RequiredImplementations.getFileManager().getText(fileurl,
				true,
				onResponse,
				onError,
				false);



	}


	/**
	 * Using SuperSimpleSemantics this code loads the games semantic data. This
	 * data, if present, will be used in scene files to define property and sub
	 * property For example, the game will know that a Apple is a green fruit or
	 * that Oak is Wood and wood is flammable and floats Property can be set or
	 * tested at any time ( see SceneObjects and property sets)
	 **/
	public static void LoadSemanticDatabase() {

		//ensure first time setup is run on green fruit engine
		//this does stuff like add the default prefixs (rdfs etc) used for SubClassOf, and any other
		//"logicaly significant" nodes.


		//GamesSemanticIndexs = new SSSIndex("Semantics/OnotologyIndex.ntlist");

		Log.info("setting up semantics");
		//setup the semantics
		SuperSimpleSemantics.setFileManager(RequiredImplementations.getFileManager());
		SuperSimpleSemantics.setup();
		SuperSimpleSemantics.setPreloadIndexs(true);
		SuperSimpleSemantics.setAutoRefreshNodeParentCaches(true);

		ArrayList<String> indexs = new ArrayList<String>();

		indexs.add(defaultNSFolder+defaultNSFile);	

		Log.info("loading semantics at:"+defaultNSFolder+defaultNSFile);

		SuperSimpleSemantics.loadIndexsAt(indexs);

		/*
		SuperSimpleSemantics.setLoadedRunnable(new Runnable() {			
			@Override
			public void run() {

				//ensure semantic caches are fresh seeing as we dont want it to have to load
				//anything once the game is running.
				SSSNode.refreshAllCaches();	
			}
		});
		 */

		/*
		SSSIndex.setGlobalCallback(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Log.info("Semantic indexs load failed:");
			}

			@Override
			public void onSuccess(String result) {
				Log.info("Semantic indexs load done:");

				SSSIndex.parseAllKnownSSSFilesFromIndexs();

			}

		});*/



		/*

		SSSNodesWithCommonProperty
		.setGlobalCallback(new AsyncCallback<String>() {


			@Override
			public void onSuccess(String result) {

				// This is all just for testing
				// In future, this will flag a variable just saying
				// Semantics is GO!

				Log.info("_____________________________________________________loaded "
						+ result);


				//	Iterator<SSSNode> all = SSSNode.getAllKnownNodes().iterator();

				//	while (all.hasNext()) {

				//		SSSNode current = all.next();
				//		GreyLog.info("_____SSSNodes: Label:" + current.getPLabel()+" PURI:"+current.getPURI());
				//		GreyLog.info("_____SSSNodes: Alt PURIs:"+current.getEquivilentsAsString());


				//	}

				//final SSSNode green = SSSNode.createSSSNode("green",
				//		"green", defaultNS);
				//final SSSNode color = SSSNode.createSSSNode("colour",
				//		"colour", defaultNS);

				//final SSSNode flamable = SSSNode.createSSSNode("Flamable",
				//		"flamable", defaultNS);
				// final SSSNode fruit = SSSNode.createSSSNode("fruit",
				// "fruit",baseURI);

				// final SSSNode apple = SSSNode.createSSSNode("apple",
				// "apple",baseURI);

				//HashSet<SSSNode> testresult = QueryEngine
				//		.getNodesWithProperty(color, green);

				// HashSet<SSSNode> testresult2 =
				// QueryEngine.getNodesWhichAre(flamable);

				// testresult.retainAll(testresult2);

				//Iterator<SSSNode> tri = testresult.iterator();
				//while (tri.hasNext()) {

				//	SSSNode sssNode = tri.next();

				//GreyLog.info("_____Green Thing: " + sssNode.getPURI());

				//}
				//Iterator<SSSNode> flamit = testresult2.iterator();
				//

				//while (flamit.hasNext()) {

				//SSSNode sssNode = flamit.next();

				//GreyLog.info("_____Flamable Thing: " + sssNode.getPURI());

				//}
			}

			@Override
			public void onFailure(Throwable caught) {

			}
		});
		 */
	}
	public static IsPopupPanel PlayersInventoryFrame = null;/* = new PopUpWithShadow(			null,

			"50%", "25%", GamesInterfaceText.MainGame_Inventory, defaultInventory); //$NON-NLS-1$ 



	public final static HashMap<String, IsPopupPanel> allInventoryFrames = new HashMap<String, IsPopupPanel>() {
		{
			put("Inventory Items", PlayersInventoryFrame);

		}
	};*/

	public static int NumberOfHTMLsLeftToLoad;



	public static void testForActionsToTriggerOnKeyPress(int keycode) {
		
		//first test for global keyboard actions
		InstructionProcessor.testForGlobalActions(TriggerType.OnKeyPress, "" + keycode, null);

		//Log.info("____________key press current Scene = "+SceneObjectDatabase.currentScene.SceneFileName);							

		// then get current scene and test for scene specific keyboard actions
		//used to be currentactivescene
		if (SceneObjectDatabase.currentScene!=null){
			SceneObjectDatabase.currentScene.testForSceneActions(ActionSet.TriggerType.OnKeyPress, "" + keycode);
		}
	}


	// overlay popup list ;
	//	final static ArrayList<TitledPopUpWithShadow> overlayPopUpsOpen = new ArrayList<TitledPopUpWithShadow>();
		public final static ArrayList<IsPopupPanel> popupPanelCurrentlyOpen = new ArrayList<IsPopupPanel>();

		public static boolean ScoreBoxVisible_CuypersMode = false;

















}
