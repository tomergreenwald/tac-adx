/*
 * SpendTracker.java
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
package edu.umich.eecs.tac.auction;

import edu.umich.eecs.tac.props.Query;

/**
 * The spend tracker tracks the daily spend each advertiser incurs.
 * 
 * @author Patrick Jordan
 */
public interface SpendTracker {
	/**
	 * Adds the advertiser
	 * 
	 * @param advertiser
	 *            the advertiser
	 */
	void addAdvertiser(String advertiser);

	/**
	 * Returns the daily spend the advertiser incurred.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * 
	 * @return the daily spend the advertiser incurred.
	 */
	double getDailyCost(String advertiser);

	/**
	 * Returns the daily spend the advertiser incurred for a given query.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param query
	 *            the query
	 * @return the daily spend the advertiser incurred for a given query.
	 */
	double getDailyCost(String advertiser, Query query);

	/**
	 * Sets the current cost to zero for all advertisers.
	 */
	void reset();

	/**
	 * Adds the cost to the advertiser and query.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param query
	 *            the query
	 * @param cost
	 *            the cost to add
	 */
	void addCost(String advertiser, Query query, double cost);

	/**
	 * Get the number of advertisers tracked.
	 * 
	 * @return the number of advertisers tracked.
	 */
	int size();
}
