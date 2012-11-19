/*
 * AuctionUtils.java
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

/**
 * @author Patrick Jordan
 */
public class AuctionUtils {
	private AuctionUtils() {
	}

	public static void hardSort(double[] scores, int[] indices) {
		for (int i = 0; i < indices.length - 1; i++) {
			for (int j = i + 1; j < indices.length; j++) {
				if (scores[indices[i]] < scores[indices[j]]
						|| Double.isNaN(scores[indices[i]])) {
					int sw = indices[i];
					indices[i] = indices[j];
					indices[j] = sw;
				}
			}
		}
	}

	public static void generalizedSecondPrice(int[] indices, double[] weights,
			double[] bids, double[] cpc, boolean[] promoted, int promotedSlots,
			double promotedReserve, int regularSlots, double regularReserve) {
		int positions = Math.min(indices.length, regularSlots);

		int promotedCount = 0;

		for (int i = 0; i < positions; i++) {
			double weight = weights[indices[i]];
			double bid = bids[indices[i]];
			double secondWeight;
			double secondBid;

			if (i < indices.length - 1) {
				secondWeight = weights[indices[i + 1]];
				secondBid = bids[indices[i + 1]];
			} else {
				secondWeight = Double.NaN;
				secondBid = Double.NaN;
			}

			// Check if the ad can be a promoted slot
			if (promotedCount < promotedSlots
					&& weight * bid >= promotedReserve) {
				cpc[indices[i]] = calculateSecondPriceWithReserve(weight,
						secondWeight, secondBid, promotedReserve);
				promoted[indices[i]] = true;
				promotedCount++;

				// Check if the ad can be in a normal slot
			} else if (weight * bid >= regularReserve) {
				cpc[indices[i]] = calculateSecondPriceWithReserve(weight,
						secondWeight, secondBid, regularReserve);
				promoted[indices[i]] = false;

				// Reject the ad
			} else {
				cpc[indices[i]] = Double.NaN;
				promoted[indices[i]] = false;
			}
		}

		for (int i = positions; i < indices.length; i++) {
			cpc[indices[i]] = Double.NaN;
		}
	}

	public static double calculateSecondPriceWithReserve(double weight,
			double secondWeight, double secondBid, double reserve) {
		double price;

		if (reserve <= secondWeight * secondBid) {
			price = secondWeight / weight * secondBid;
		} else {
			price = reserve / weight;
		}

		return price;
	}
}
