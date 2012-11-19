/*
 * Advertiser.java
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

import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;

import java.awt.*;

/**
 * @author Lee Callender
 */
public class Advertiser extends Actor {
  private int[] balance; //Agent's account balance per day. 
  private Color color;
  private String manufacturerSpecialty;
  private String componentSpecialty;
  private int distributionCapacity;
  private int distributionWindow;
  private BidBundle[] bidBundle;
  private QueryReport[] queryReport;
  private SalesReport[] salesReport;

  public Advertiser(int simulationIndex, String address, String name, int numberOfDays, Color color){
    super(simulationIndex, address, name);
    balance = new int[numberOfDays];
    bidBundle = new BidBundle[numberOfDays];
    queryReport = new QueryReport[numberOfDays+1];
    salesReport = new SalesReport[numberOfDays+1];
    this.color = color;
  }

  public void setManufacturerSpecialty(String specialty){
    manufacturerSpecialty = specialty;
  }

  public void setComponentSpecialty(String specialty){
    componentSpecialty = specialty;
  }

  public void setAccountBalance(int day, double accountBalance) {
    if(day < 0)
      return;
    
    balance[day < balance.length ? day : (balance.length - 1)] = (int) accountBalance;
  }

  public void setDistributionCapacity(int distributionCapacity) {
    this.distributionCapacity = distributionCapacity;
  }

  public void setBidBundle(BidBundle bundle, int day){
    this.bidBundle[day] = bundle; 
  }

  public void setQueryReport(QueryReport report, int day){
    this.queryReport[day] = report;
  }

  public void setSalesReport(SalesReport report, int day){
    this.salesReport[day] = report;
  }

  public String getManufacturerSpecialty() {
    return manufacturerSpecialty;
  }

  public String getComponentSpecialty() {
    return componentSpecialty;
  }

  public int getDistributionCapacity() {
    return distributionCapacity;
  }

  public int getAccountBalance(int day) {
    return balance[day];
  }

  public int[] getAccountBalance() {
    return balance;
  }

  public BidBundle[] getBidBundles() {
    return bidBundle;
  }

  public QueryReport[] getQueryReports(){
    return queryReport;
  }

  public SalesReport[] getSalesReports(){
    return salesReport;
  }

  public BidBundle getBidBundle(int day){
    return bidBundle[day];
  }

  public QueryReport getQueryReport(int day){
    return queryReport[day];
  }

  public SalesReport getSalesReport(int day){
    return salesReport[day];
  }

  public Color getColor(){
    return color;
  }

  public void setColor(Color color){
    this.color = color;
  }


  public int getDistributionWindow() {
    return distributionWindow;
  }

  public void setDistributionWindow(int distributionWindow) {
    this.distributionWindow = distributionWindow;
  }
}
