This is	version 10.1.0.1 beta of the TAC AA AgentWare for Java.

The purpose of this release is to provide a basic set of tools for
developing TAC AA agents.  This release contains:

- preliminary APIs for TAC AA agents
- basic setup for communicating with the TAC AA server

More information about TAC AA agent development is available at
http://aa.tradingagents.org

You will need Java SDK 1.5.0 or newer (you can find it at
http://java.sun.com) to be able to develop and run this AgentWare.


Getting the AgentWare to run.
------------------------------


Running
-------
Register your agent at http://localhost:8080/ and then enter
your agent name and password in the configuration file 'aw.conf'.

Then type "java -classpath $CLASSPATH edu.umich.eecs.tac.aa.agentware".

The CLASSPATH environment variable must contain all of the jars in the "lib" directory.

Simple game results can be found at http://localhost:8080/
For information about other TAC AA servers please see
http://aa.tradingagents.org


Configuring the AgentWare
-------------------------
The AgentWare reads the configuration file 'config/aw.conf' at startup. This
file allows, among other things, the configuration of agent
implementations. See the file 'aw.conf' for more information.

Note: by default the AgentWare will automatically create and join a
new game after a game has ended. You can specify how many games it
automatically should create and join by setting the 'autojoin' option
in the configuration file 'aw.conf'.


If you have any questions or comments regarding this AgentWare please
contact tac-aa-support@umich.edu

-- The University of Michigan TAC Team