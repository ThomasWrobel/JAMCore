package com.lostagain.Jam;

import lostagain.nl.spiffyresources.client.IsSpiffyGenericLogger;

public interface BasicGameFunctions {

	/**
	 * sets the title of the game window, or webpage
	 * @param string
	 */
	void setWindowTitle(String string);
	
	
	
	/**
	 * This will be triggered after the core reset.
	 * You should reset any needed visuals or other implementation specific settings so a fresh game can be loaded
	 */
	void resetGame();


	/**
	 * should return a logger 
	 * You can have a dummy implementation that has the methods if you dont need this
	 * @return
	 */
	IsSpiffyGenericLogger getLogger();
	
	/**
	 * Fired after the controll script and gametextdatabase has been loaded.<br>
	 * You can use this to run any other needed setup that relays on one or both of these things.<br>
	 * ie. Maybe you were waiting for a list of supported languages before filling a language select dropdown?<br>
	 */
	public void postDatabaseLoadSetup();
	
}
