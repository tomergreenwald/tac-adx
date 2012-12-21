/*
 * GameInfo.java
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
package edu.umich.eecs.tac.logviewer.info;

import edu.umich.eecs.tac.logviewer.util.SimulationParser;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.UserPopulationState;
import edu.umich.eecs.tac.props.SlotInfo;

import java.util.ArrayList;
import java.util.Set;



/**
 * To not be confused with SimulationInfo.java
 * @author- Lee Callender 
 */
public class GameInfo {
  private String server;
  private int numberOfDays;
  private int simulationID;
  private String simulationType;
  private int secondsPerDay;
  private double squashingParameter;

  private Advertiser[] advertisers;
  private Set<Query> querySpace;
  private RetailCatalog catalog;
  private SlotInfo slotInfo;

  private UserPopulationState[] ups;

  public GameInfo(SimulationParser sp) {
    simulationID = sp.getSimulationID();
    simulationType = sp.getSimulationType();
    numberOfDays = sp.getNumberOfDays();
    secondsPerDay = sp.getSecondsPerDay();
    squashingParameter = sp.getSquashingParameter();
    server = sp.getServer();

    advertisers = sp.getAdvertisers();
    querySpace = sp.getQuerySpace();
    catalog = sp.getRetailCatalog();
    slotInfo = sp.getSlotInfo();
    ups = sp.getUserPopulationState();
  }

  public String getServer() {
    return server;
  }

  public int getSimulationID() {
    return simulationID;
  }

  public String getSimulationType() {
    return simulationType;
  }

  public int getSecondsPerDay() {
    return secondsPerDay;
  }

  public int getNumberOfDays() {
    return numberOfDays;
  }

  public int getAdvertiserCount() {
    return advertisers.length;
  }

  public double getSquashingParameter(){
    return squashingParameter;
  }

  public Set<Query> getQuerySpace() {
    return querySpace;
  }

  public RetailCatalog getRetailCatalog() {
    return catalog;
  }

  public SlotInfo getSlotInfo() {
    return slotInfo;
  }

  public Advertiser[] getAdvertisers(){
    return advertisers;
  }

  public Advertiser getAdvertiser(int index) {
    return advertisers[index];
  }

  public int getAdvertiserIndex(Advertiser m) {
    for (int i = 0, n = advertisers.length; i < n; i++) {
      if(m == advertisers[i])
	      return i;
    }
    return -1;
  }

  public final UserPopulationState getUserPopulationOnDay(int day){
     try{
       return ups[day];
     }catch(Exception e){
       return null;
     }
  }

  public final UserPopulationState[] getUserPopulationState(){
    return ups;
  }

  private boolean isValidDay(int day) {
    return !(day < 0 || day > numberOfDays);
  }
}
