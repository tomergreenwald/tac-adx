/*
 * Main.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.agentware;

import java.io.IOException;

import se.sics.isl.util.ArgumentManager;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class Main {

	private static String DEFAULT_HOST = "localhost";
	private static int DEFAULT_PORT = 6502;

	private final static String DEFAULT_CONFIG = "config/aw.conf";

	private Main() {
	}

	public static void main(String[] args) throws IOException {
		ArgumentManager config = new ArgumentManager("aa-aw.jar", args);
		config.addOption("config", "configfile", "set the config file to use");
		config.addOption("serverHost", "host", "set the TAC server host");
		config.addOption("serverPort", "port", "set the TAC server port");
		config.addOption("agentName", "name", "set the agent name");
		config.addOption("agentPassword", "password", "set the agent password");
		config.addOption("agentImpl", "class", "set the agent implementation");
		config.addOption("autojoin", "numberOfTimes",
				"set the number of times to automatically create and join simulations");
		config.addOption("log.consoleLevel", "level",
				"set the console log level");
		config.addOption("log.fileLevel", "level", "set the file log level");
		config.addHelp("h", "show this help message");
		config.addHelp("help");
		config.validateArguments();

		String configFile = config.getArgument("config", DEFAULT_CONFIG);
		try {
			boolean loadConfiguration = config.loadConfiguration(configFile);
			config.removeArgument("config");
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			config.usage(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		String agentImpl = config.getProperty("agentImpl");
		if (agentImpl == null || agentImpl.length() == 0) {
			System.err.println("No agent implementation specified!");
			config.usage(1);
		}

		// Make sure the class for the agent can be found before
		// connecting to the server (and joining simulations).
		try {
			Class c = Class.forName(agentImpl);
		} catch (Exception e) {
			System.err.println("Could not find the agent implementation '"
					+ agentImpl + '\'');
			e.printStackTrace();
			System.exit(1);
		}

		String host = config.getProperty("serverHost", DEFAULT_HOST);
		int port = config.getPropertyAsInt("serverPort", DEFAULT_PORT);
		String name = config.getProperty("agentName", null);
		String password = config.getProperty("agentPassword", null);
		if (name == null || name.length() == 0 || password == null
				|| password.length() == 0) {
			System.err.println("=============================================");
			System.err.println("You must specify a registered agent name");
			System.err.println("and password in the configuration file");
			System.err.println("or as arguments when starting the AgentWare");
			System.err.println("");
			System.err.println("You can register your agent at:");
			System.err.println("http://" + host + ":8080/");
			System.err.println("=============================================");
			System.exit(1);
		}

		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		SimClient client = new SimClient(config, host, port, name, password,
				agentImpl);
	}

} // Main
