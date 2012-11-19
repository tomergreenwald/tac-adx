/*
 * PermutationOfEightGeneratorTest.java
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
package edu.umich.eecs.tac.util.permutation;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * @author Patrick R. Jordan
 */
public class PermutationOfEightGeneratorTest {
    private PermutationOfEightGenerator generator;

    @Before
    public void setup() {
        generator = new PermutationOfEightGenerator();
    }

    @Test
    public void testConstructor() {
        assertNotNull(generator);
    }

    @Test
    public void testSize() {
        int count = 0;


        while(generator.hasNext()) {
            count++;
            generator.next();
        }

        assertEquals(count, 40320, 0);
    }

    @Test
    public void testDistinct() {
        int[] p1 = generator.next();
        assertEquals(p1.length,8,0);
        int[] p2 = generator.next();
        assertEquals(p2.length,8,0);

        assertFalse(Arrays.equals(p1,p2));
        Arrays.sort(p1);
        Arrays.sort(p2);
        assertTrue(Arrays.equals(p1,p2));
        assertTrue(Arrays.equals(p1,new int[] {0,1,2,3,4,5,6,7}));
        assertNotSame(p1,p2);
    }
}
