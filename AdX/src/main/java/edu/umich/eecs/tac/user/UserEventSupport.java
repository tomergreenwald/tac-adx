/*
 * UserEventSupport.java
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

import java.util.ArrayList;
import java.util.List;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.Query;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class UserEventSupport {

	private List<UserEventListener> listeners;

	public UserEventSupport() {
		listeners = new ArrayList<UserEventListener>();
	}

	public boolean addUserEventListener(UserEventListener listener) {
		return listeners.add(listener);
	}

	public boolean containsUserEventListener(UserEventListener listener) {
		return listeners.contains(listener);
	}

	public boolean removeUserEventListener(UserEventListener listener) {
		return listeners.remove(listener);
	}

	public void fireQueryIssued(Query query) {
		for (UserEventListener listener : listeners) {
			listener.queryIssued(query);
		}
	}

	public void fireAdViewed(Query query, AdLink ad, int slot, boolean isPromoted) {
		for (UserEventListener listener : listeners) {
			listener.viewed(query, ad.getAd(), slot, ad.getAdvertiser(), isPromoted);
		}
	}

	public void fireAdClicked(Query query, AdLink ad, int slot, double cpc) {
		for (UserEventListener listener : listeners) {
			listener.clicked(query, ad.getAd(), slot, cpc, ad.getAdvertiser());
		}
	}

	public void fireAdConverted(Query query, AdLink ad, int slot,
			double salesProfit) {
		for (UserEventListener listener : listeners) {
			listener
					.converted(query, ad.getAd(), slot, salesProfit, ad.getAdvertiser());
		}
	}

}
