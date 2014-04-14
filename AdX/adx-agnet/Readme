  ______    ___    ______           ___     ____    _  __
 /_  __/   /   |  / ____/          /   |   / __ \  | |/ /
  / /     / /| | / /      ______  / /| |  / / / /  |   / 
 / /     / ___ |/ /___   /_____/ / ___ | / /_/ /  /   |  
/_/     /_/  |_|\____/          /_/  |_|/_____/  /_/|_|  
                                                         
                                                         
This folder contains all needed code and executables to run the	ADX agnet.

How to run the agnet:
	1. To run the client, start the "runAgent.sh" executable.
	2. The agent will start automatically and will attempt to connect to the ADX server.
	   The default settings will attemp to connect to the local server with default values
	   as specified in the config file.
	
How to configure the agnet:
	1. The agent's configuration file is located at "config/aw-1.conf" (which is also configurable
	   from the "runAgent.sh" file.

How to implement an agent:
	1. You will need to create a Java class implementing se.sics.tasim.aw.Agent.
	2. This class will contain your "business logic" and will be responsible for
	   communicating with the ADX server.
	3. After you have finished implementing your agent, you will need to register it
	   in the configuration file "config/aw-1.conf", for it to be loaded by the agentwate.
	   (i.e. agentImpl=tau.tac.adx.agents.SampleAdNetwork).
	4. An example implementing class is available at "SimpleAdNetwork.java"
	5. To assure that your code will be available to run, generate a "jar" file from your
	   code base and insert it into the "lib" folder.
How to run the log parser:
	1. Edit the runLogParser.sh and change the "-file LOG_FILE_PATH/game.slg.gz" to point to your desired log file.
	2. Enable different log messages with these flags:
		-ucs
			User classification messages
		-rating 
			User rating score messages
		-bank
			Bank status messages
		-campaign
			Campaign information messages
		-adnet
			Ad Network messages
		-all
			Show all messags (unformatted)