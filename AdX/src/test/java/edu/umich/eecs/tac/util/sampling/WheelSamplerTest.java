/*
 * WheelSamplerTest.java
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
package edu.umich.eecs.tac.util.sampling;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/**
 * @author Patrick Jordan
 */
public class WheelSamplerTest {

	@Test
	public void testConstructors() {
		WheelSampler sampler = new WheelSampler();
		assertNotNull(sampler);

		sampler = new WheelSampler(new Random());
		assertNotNull(sampler);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeWeight() {
		WheelSampler sampler = new WheelSampler();

		sampler.addState(-1.0, new Object());
	}

	@Test
	public void testSample() {
		Random r = new Random(1);
		WheelSampler<Number> sampler = new WheelSampler<Number>(r);

		// Test zero-slot sampler
		assertNull(sampler.getSample());

		// Test zero-slot sampler
		sampler.addState(1.0, 1);
		assertEquals(1, sampler.getSample());

		// Test zero-slot sampler
		sampler.addState(1.1, 2);
		assertEquals(1, sampler.getSample());
		assertEquals(2, sampler.getSample());

		sampler.addState(1.2, 3);
		assertEquals(3, sampler.getSample());
		assertEquals(3, sampler.getSample());
		assertEquals(1, sampler.getSample());
	}
}
