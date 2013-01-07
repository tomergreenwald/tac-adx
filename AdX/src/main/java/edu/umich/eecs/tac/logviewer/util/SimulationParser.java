/*
 * SimulationParser.java
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

package edu.umich.eecs.tac.logviewer.util;

/**
 * @author Lee Callender
 */

import edu.umich.eecs.tac.Parser;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.logtool.LogHandler;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.props.ServerConfig;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.*;
import edu.umich.eecs.tac.logviewer.monitor.ParserMonitor;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import com.botbox.util.ArrayUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SimulationParser extends Parser{
  private LogHandler logHandler;

  private ParserMonitor[] monitors;

  private int numberOfDays;
  private int simulationID;
  private String simulationType;
  private long startTime;
  private int secondsPerDay;
  private double squashingParameter;
  private String serverName;
  private String serverVersion;

  private ParticipantInfo[] participants;
  private int[] newParticipantIndex;

  private int publisherCount;
  private int advertiserCount;
  private int usersCount;

  private Advertiser[] advertisers;
  private Set<Query> querySpace;

  private int currentDay;

  private RetailCatalog retailCatalog;
  private ServerConfig serverConfig;
  private SlotInfo slotInfo; 
  private UserPopulationState[] ups;

  private boolean errorParsing = false;
  private boolean isParserWarningsEnabled = true;

  public SimulationParser(LogHandler logHandler, LogReader lr) {
    super(lr);
    this.logHandler = logHandler;
    simulationID = lr.getSimulationID();
    simulationType = lr.getSimulationType();
    startTime = lr.getStartTime();
    serverName = lr.getServerName();
    serverVersion = lr.getServerVersion();
    isParserWarningsEnabled =
      logHandler.getConfig().getPropertyAsBoolean("visualizer.parserWarnings",
						  true);

    // Set up new indexing for participants [0..] for each type
    participants = lr.getParticipants();
    newParticipantIndex = new int[participants.length];

    for (int i = 0, n = participants.length; i < n; i++) {
      switch(participants[i].getRole()) {
        case TACAdxConstants.PUBLISHER : newParticipantIndex[i] = publisherCount++; break;
        case TACAdxConstants.ADVERTISER : newParticipantIndex[i] = advertiserCount++; break;
        case TACAdxConstants.USERS : newParticipantIndex[i] = usersCount++; break;
      }
    }
  }

  protected void parseStarted() {
	  if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
		    monitors[i].parseStarted();
  }

  protected void parseStopped() {
    System.err.println(); //??

    if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
		    monitors[i].parseStopped();
  }

  /**********************************************************************
   * Message handling dispatching
   *  - a number of methods that gets a chuck, enterperates it and
   *    dispatches it for further processing.
   **********************************************************************/

  protected void message(int sender, int receiver, Transportable content) {
    if (content instanceof BankStatus)
      handleMessage(sender, receiver, (BankStatus) content);
    else if(content instanceof PublisherInfo)
      handleMessage(sender, receiver, (PublisherInfo) content);
    else if(content instanceof AdvertiserInfo)
      handleMessage(sender, receiver, (AdvertiserInfo) content);
    else if(content instanceof BidBundle)
      handleMessage(sender, receiver, (BidBundle) content);
    else if(content instanceof QueryReport)
      handleMessage(sender, receiver, (QueryReport) content);
    else if(content instanceof SalesReport)
      handleMessage(sender, receiver, (SalesReport) content);
    else if(content instanceof SlotInfo)
      handleMessage(sender, receiver, (SlotInfo) content);

    if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
	      monitors[i].message(sender, receiver, content);
  }

  protected void dataUpdated(int type, Transportable content) {

    if (content instanceof StartInfo)
      handleData((StartInfo) content);
    else if(content instanceof RetailCatalog)
      handleData((RetailCatalog) content);
    
    if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
		    monitors[i].dataUpdated(type, content);
  }

  protected void dataUpdated(int sender, int type, Transportable content){
    if(content instanceof UserPopulationState){
      handleData((UserPopulationState) content);
    }
  }

  protected void data(Transportable object) {

    if (object instanceof ServerConfig)
      handleData((ServerConfig) object);

	  if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
		    monitors[i].data(object);
  }

  private void handleMessage(int sender, int receiver, BankStatus content){
    if(participants[receiver].getRole() == TACAdxConstants.ADVERTISER){
      int index = newParticipantIndex[receiver];
      advertisers[index].setAccountBalance(currentDay - 1, content.getAccountBalance());
    }
  }

  private void handleMessage(int sender, int receiver, PublisherInfo content){
    this.squashingParameter = content.getSquashingParameter();
  }

  private void handleMessage(int sender, int receiver, AdvertiserInfo content){
    if(participants[receiver].getRole() == TACAdxConstants.ADVERTISER){
      int index = newParticipantIndex[receiver];
      advertisers[index].setManufacturerSpecialty(content.getManufacturerSpecialty());
      advertisers[index].setComponentSpecialty(content.getComponentSpecialty());
      advertisers[index].setDistributionCapacity(content.getDistributionCapacity());
      advertisers[index].setDistributionWindow(content.getDistributionWindow());
    }
  }

  private void handleMessage(int sender, int receiver, BidBundle content){
    if(participants[sender].getRole() == TACAdxConstants.ADVERTISER){
      int index = newParticipantIndex[sender];
      advertisers[index].setBidBundle(content, currentDay);
    }
  }

  private void handleMessage(int sender, int receiver, QueryReport content){
    if(participants[receiver].getRole() == TACAdxConstants.ADVERTISER){
      int index = newParticipantIndex[receiver];
      advertisers[index].setQueryReport(content, currentDay);
    }
  }

  private void handleMessage(int sender, int receiver, SalesReport content){
   if(participants[receiver].getRole() == TACAdxConstants.ADVERTISER){
      int index = newParticipantIndex[receiver];
      advertisers[index].setSalesReport(content, currentDay);
    }
  }

  private void handleMessage(int sender, int receiver, SlotInfo content){
    if(this.slotInfo == null)
      this.slotInfo = content;
  }

  private void handleData(RetailCatalog content){
    if(retailCatalog == null)
      this.retailCatalog = content;

    if(querySpace == null){
      generatePossibleQueries(content);
    }

  }

  private void generatePossibleQueries(RetailCatalog retailCatalog) {
		if (retailCatalog != null && querySpace == null) {
			querySpace = new HashSet<Query>();

			for (Product product : retailCatalog) {
				Query f0 = new Query();
				Query f1_manufacturer = new Query(product.getManufacturer(),
						null);
				Query f1_component = new Query(null, product.getComponent());
				Query f2 = new Query(product.getManufacturer(), product
						.getComponent());

				querySpace.add(f0);
				querySpace.add(f1_manufacturer);
				querySpace.add(f1_component);
				querySpace.add(f2);
			}

		}
	}

  private void handleData(ServerConfig content) {
      serverConfig = content;

      secondsPerDay = content.getAttributeAsInt("game.secondsPerDay", -1);

  }

  private void handleData(StartInfo startInfo) {
    numberOfDays = startInfo.getNumberOfDays();
    initActors();
  }

  private void handleData(UserPopulationState userPopulationState){
    if(this.ups == null){
      this.ups = new UserPopulationState[numberOfDays+1];
    }

    ups[currentDay] = userPopulationState;
  }

 /**
	 * Invoked when a new day notification is encountered in the log file.
	 *
	 * @param date
	 *            the new day in the simulation
	 * @param serverTime
	 *            the server time at that point in the simulation
	 */
	protected void nextDay(int date, long serverTime) {
	  currentDay = date;

    int done = (int) (10 * (double) (currentDay+1) / numberOfDays);
    int notDone = 10 - done;

    if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
	      monitors[i].nextDay(date, serverTime);

    System.err.print("Parsing game " + simulationID + ": [");
    for (int i = 0, n = done; i < n; i++)
	    System.err.print("*");
    for (int i = 0, n = notDone; i < n; i++)
	  System.err.print("-");
    System.err.print("]");
    System.err.print((char)13);
  }

  public void unhandledNode(String nodeName) {
    if(monitors != null)
	    for (int i = 0, n = monitors.length; i < n; i++)
	      monitors[i].unhandledNode(nodeName);
  }


  /**********************************************************************
   *
   * Access methods - to get data from the parser
   *
   **********************************************************************/

   public void addMonitor(ParserMonitor monitor) {
	    monitors = (ParserMonitor[]) ArrayUtils.add(ParserMonitor.class,
						    monitors, monitor);
   }

   public void removeMonitor(ParserMonitor monitor) {
	    monitors = (ParserMonitor[]) ArrayUtils.remove(monitors, monitor);
   }

   public ParserMonitor[] getMonitors() {
	    return monitors;
   }

  /**
   * Get methods
   */
   public int getCurrentDay() {
	    return currentDay;
   }

   public boolean errorParsing() {
      return errorParsing;
   }

   public int getSecondsPerDay() {
      return secondsPerDay;
   }

   public int getNumberOfDays() {
      return numberOfDays;
   }

   public int getSimulationID() {
      return simulationID;
   }

   public String getSimulationType() {
      return simulationType;
   }

  public long getStartTime() {
      return startTime;
   }

  public String getServer() {
     return serverName + " (version " + serverVersion + ')';
   }

  public double getSquashingParameter(){
     return squashingParameter;
   }

  public RetailCatalog getRetailCatalog() {
    return retailCatalog;
  }

  public SlotInfo getSlotInfo() {
    return slotInfo;
  }

  public UserPopulationState[] getUserPopulationState(){
    return ups;
  }

  public Set<Query> getQuerySpace() {
    return querySpace;
  }

  public Advertiser[] getAdvertisers(){
     return advertisers;
   }
  
  /**********************************************************************
   *
   * Parser log handling (perhaps should be direct calls to LogHandler?)
   *
   **********************************************************************/

   private void warn(String message) {
    if (isParserWarningsEnabled) {
      int timeunit = currentDay;
      logHandler.warn("Parse: [Day " + (timeunit < 10 ? " " : "")
		      + timeunit + "] " + message);
    }
  }

  /**********************************************************************
   *
   *
   *
   **********************************************************************/

  // Keys to hash comMessages with
  protected static class CommMessageKey implements Comparable {
    int id;
    int sender;
    int day;

    CommMessageKey(int id, int sender, int day) {
      this.id = id;
      this.sender = sender;
      this.day = day;
    }

    public int compareTo(Object o) {
      CommMessageKey cm = (CommMessageKey) o;
      if(cm.day < day)
	      return -1;
      else if(cm.day > day)
	      return 1;

      if(cm.sender < sender)
	      return -1;
      else if(cm.sender > sender)
	      return 1;

      if(cm.id < id)
	      return -1;
      else if(cm.id > id)
	      return 1;

      return 0;
    }
  }

  /**********************************************************************
   *
   * Inititalization methods for internal data structures
   *
   **********************************************************************/

  private void initActors() {
    Color[] c_array = {Color.BLUE, Color.CYAN,  Color.GREEN,  new Color(75, 0, 130),
                       Color.RED, Color.MAGENTA, Color.ORANGE, Color.PINK};

    
    advertisers = new Advertiser[advertiserCount];
 
    for (int i = 0, n = participants.length; i < n; i++) {
      switch(participants[i].getRole()) {

        case TACAdxConstants.ADVERTISER :
	        advertisers[newParticipantIndex[i]] = new Advertiser(participants[i].getIndex(),
			              participants[i].getAddress(),participants[i].getName(),
                    numberOfDays, c_array[newParticipantIndex[i]]);

	        break;
        case TACAdxConstants.PUBLISHER :
	        break;
        case TACAdxConstants.USERS :
	        break;

      }
    }
  }//initActors
}
