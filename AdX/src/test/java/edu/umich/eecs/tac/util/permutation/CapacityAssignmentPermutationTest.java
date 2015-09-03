/*
 * CapacityAssignmentPermutationTest.java
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
import edu.umich.eecs.tac.sim.CapacityType;
import static edu.umich.eecs.tac.sim.CapacityType.LOW;
import static edu.umich.eecs.tac.sim.CapacityType.MED;
import static edu.umich.eecs.tac.sim.CapacityType.HIGH;

import java.util.Arrays;

/**
 * @author Patrick R. Jordan
 */
public class CapacityAssignmentPermutationTest {

    @Test
    public void testFirstPermutation() {
        CapacityType[] types0 = CapacityAssignmentPermutation.permutation(0,0);
        CapacityType[] types1 = CapacityAssignmentPermutation.permutation(0,1);
        CapacityType[] types2 = CapacityAssignmentPermutation.permutation(0,2);
        CapacityType[] types3 = CapacityAssignmentPermutation.permutation(0,3);

        assertArrayEquals(types0, new CapacityType[]{LOW,LOW,MED,MED,MED,MED,HIGH,HIGH});
        assertArrayEquals(types1, new CapacityType[]{MED,MED,LOW,LOW,HIGH,HIGH,MED,MED});
        assertArrayEquals(types2, new CapacityType[]{MED,MED,HIGH,HIGH,LOW,LOW,MED,MED});
        assertArrayEquals(types3, new CapacityType[]{HIGH,HIGH,MED,MED,MED,MED,LOW,LOW});
    }

    @Test
    public void testGroupPermutation() {
        CapacityType[] types0 = CapacityAssignmentPermutation.permutation(1,0);
        CapacityType[] types1 = CapacityAssignmentPermutation.permutation(1,1);
        CapacityType[] types2 = CapacityAssignmentPermutation.permutation(1,2);
        CapacityType[] types3 = CapacityAssignmentPermutation.permutation(1,3);

        int[][] counts = new int[8][3];

        for(int i = 0; i < 8; i++) {
            counts[i][types0[i].ordinal()]++;
            counts[i][types1[i].ordinal()]++;
            counts[i][types2[i].ordinal()]++;
            counts[i][types3[i].ordinal()]++;
        }

        for(int i = 0; i < 8; i++) {
            assertEquals(counts[i][0],1,0);
            assertEquals(counts[i][1],2,0);
            assertEquals(counts[i][2],1,0);
        }        
    }

    @Test
    public void testDistinct() {
        CapacityType[] types0 = CapacityAssignmentPermutation.permutation(0,0);
        CapacityType[] types1 = CapacityAssignmentPermutation.permutation(100,0);

        assertFalse(Arrays.equals(types0,types1));        
    }

    @Test
    public void testSecretPermutation() {
        CapacityType[] types0 = CapacityAssignmentPermutation.secretPermutation(1234123412,1,1);
        CapacityType[] types1 = CapacityAssignmentPermutation.secretPermutation(1234123412,2,1);
        CapacityType[] types2 = CapacityAssignmentPermutation.secretPermutation(1234123412,3,1);
        CapacityType[] types3 = CapacityAssignmentPermutation.secretPermutation(1234123412,4,1);

        int[][] counts = new int[8][3];

        for(int i = 0; i < 8; i++) {
            counts[i][types0[i].ordinal()]++;
            counts[i][types1[i].ordinal()]++;
            counts[i][types2[i].ordinal()]++;
            counts[i][types3[i].ordinal()]++;
        }

        for(int i = 0; i < 8; i++) {
            assertEquals(counts[i][0],1,0);
            assertEquals(counts[i][1],2,0);
            assertEquals(counts[i][2],1,0);
        }
    }
}
