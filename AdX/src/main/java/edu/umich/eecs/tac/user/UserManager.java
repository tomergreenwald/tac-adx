/*
 * UserManager.java
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
package edu.umich.eecs.tac.user;

import se.sics.tasim.aw.Message;
import se.sics.tasim.aw.TimeListener;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.Auctioneer;

/**
 * UserManager provides a public interface for triggering and managing agent
 * behavior. Listeners may be added and removed through this object.
 * 
 * @author Patrick Jordan
 */
public interface UserManager extends TimeListener {

	public void initialize(int virtualDays);

	public void triggerBehavior(Auctioneer auctioneer);

	public boolean addUserEventListener(UserEventListener listener);

	public boolean containsUserEventListener(UserEventListener listener);

	public boolean removeUserEventListener(UserEventListener listener);

  /**
   * Gathers the state distribution over all product populations
   * @return
   */
  public int[] getStateDistribution();
  
  /**
   * Gathers the state distribution over the given product population
   * @param product
   * @return
   */
  public int[] getStateDistribution(Product product);

  public RetailCatalog getRetailCatalog();

  public UserClickModel getUserClickModel();

	public void setUserClickModel(UserClickModel userClickModel);

	public void messageReceived(Message message);
}
