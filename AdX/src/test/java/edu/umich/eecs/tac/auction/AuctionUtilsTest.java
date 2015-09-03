/*
 * AuctionUtilsTest.java
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

import static edu.umich.eecs.tac.auction.AuctionUtils.calculateSecondPriceWithReserve;
import static edu.umich.eecs.tac.auction.AuctionUtils.generalizedSecondPrice;
import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class AuctionUtilsTest {
	@Test
	public void testHardSort() {
		double[] scores = new double[] { 0.5, 1.0, Double.NaN, 0.25 };
		int[] indices = new int[] { 0, 1, 2, 3 };

		hardSort(scores, indices);

		assertEquals(indices[0], 1, 0);
		assertEquals(indices[1], 0, 0);
		assertEquals(indices[2], 3, 0);
		assertEquals(indices[3], 2, 0);
	}

	@Test
	public void testGeneralizedSecondPrice() {
		double[] scores = new double[] { 0.5, 1.0, Double.NaN, 0.25 };
		double[] bids = new double[] { 1.0, 1.0, 1.0, 1.0 };
		int[] indices = new int[] { 1, 0, 3, 2 };
		double[] cpc = new double[4];
		boolean[] promoted = new boolean[4];

		generalizedSecondPrice(indices, scores, bids, cpc, promoted, 1, 0.05,
				4, 0.01);

		assertEquals(cpc[0], 0.25 / 0.5, 0.0);
		assertEquals(cpc[1], 0.5, 0.0);
		assertTrue(Double.isNaN(cpc[2]));
		assertEquals(cpc[3], 0.01 / 0.25, 0.0);
	}

    @Test
	public void testAdditionalGeneralizedSecondPrice() {
		double[] scores = new double[] { 0.5, 1.0, Double.NaN, 0.25 };
		double[] bids = new double[] { 4.0, 3.0, 2.0, 1.0 };
		int[] indices = new int[] { 1, 0, 3, 2 };
		double[] cpc = new double[4];
		boolean[] promoted = new boolean[4];

		generalizedSecondPrice(indices, scores, bids, cpc, promoted, 1, 0.05,
				4, 0.01);

		assertEquals(cpc[0], 0.5, 0.0);
		assertEquals(cpc[1], 2.0, 0.0);
		assertTrue(Double.isNaN(cpc[2]));
		assertEquals(cpc[3], 0.01 / 0.25, 0.0);
	}

	@Test
	public void testCalculateSecondPriceWithReservee() {
		assertEquals(calculateSecondPriceWithReserve(1.0, 0.5, 1.0, 0.1), 0.5,
				0.0);
		assertEquals(calculateSecondPriceWithReserve(1.0, 0.5, 0.0, 0.1), 0.1,
				0.0);
		assertEquals(
				calculateSecondPriceWithReserve(1.0, 0.5, Double.NaN, 0.1),
				0.1, 0.0);
	}
}
