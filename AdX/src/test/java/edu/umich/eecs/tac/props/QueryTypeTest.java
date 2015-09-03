/*
 * QueryTypeTest.java
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
package edu.umich.eecs.tac.props;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author Kemal Eren
 */
public class QueryTypeTest {

	@Test
	public void testValue() {
		Query query = new Query();
		assertEquals(QueryType.value(query), QueryType.FOCUS_LEVEL_ZERO);
		query.setComponent("c1");
		assertEquals(QueryType.value(query), QueryType.FOCUS_LEVEL_ONE);
		query.setManufacturer("m1");
		assertEquals(QueryType.value(query), QueryType.FOCUS_LEVEL_TWO);

		QueryType[] s = new QueryType[3];
		s = QueryType.values();
		assertEquals(s[0], QueryType.FOCUS_LEVEL_ZERO);
		assertEquals(s[1], QueryType.FOCUS_LEVEL_ONE);
		assertEquals(s[2], QueryType.FOCUS_LEVEL_TWO);
	}

	@Test
	public void testValueOf() {
		String name = "";
		int thrown = 0;
		try {
			QueryType result = QueryType.valueOf(name);
		} catch (IllegalArgumentException e) {
			thrown++;
		}

		if (thrown != 1) {
			fail("Empty strings should not work");
		}

		name = "FOCUS_LEVEL_ZERO";
		QueryType expResult = QueryType.FOCUS_LEVEL_ZERO;
		QueryType result = QueryType.valueOf(name);
		assertEquals(expResult, result);

		name = "FOCUS_LEVEL_ONE";
		expResult = QueryType.FOCUS_LEVEL_ONE;
		result = QueryType.valueOf(name);
		assertEquals(expResult, result);

		name = "FOCUS_LEVEL_TWO";
		expResult = QueryType.FOCUS_LEVEL_TWO;
		result = QueryType.valueOf(name);
		assertEquals(expResult, result);
	}
}