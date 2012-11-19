/*
 * CapacityAssignmentPermutation.java
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


import static edu.umich.eecs.tac.sim.CapacityType.*;
import edu.umich.eecs.tac.sim.CapacityType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Patrick R. Jordan
 */
public final class CapacityAssignmentPermutation {

    private static CapacityType[][] GROUP_CAPACITIES = {{LOW,LOW,MED,MED,MED,MED,HIGH,HIGH},
                                                        {MED,MED,LOW,LOW,HIGH,HIGH,MED,MED},
                                                        {MED,MED,HIGH,HIGH,LOW,LOW,MED,MED},
                                                        {HIGH,HIGH,MED,MED,MED,MED,LOW,LOW}};

    private CapacityAssignmentPermutation() {
    }

    public static CapacityType[] permutation(int group, int groupOffset) {
        CapacityType[] p = new CapacityType[8];

        int[] eightPerm = permutationOfEight(group);

        for(int i = 0; i < 8; i++) {
            p[i] = GROUP_CAPACITIES[groupOffset][eightPerm[i]];
        }

        return p;
    }
    
    private static int[] permutationOfEight(int group) {

        group = group % PermutationOfEightGenerator.TOTAL_PERMUTATIONS;
        if(group < 0) {
            group += PermutationOfEightGenerator.TOTAL_PERMUTATIONS;
        }

        PermutationOfEightGenerator generator = new PermutationOfEightGenerator();

        for(int i = 0; i < group; i++) {
            generator.next();
        }

        return generator.next();
    }

    public static CapacityType[] secretPermutation(int secret, int simulationId, int baseId) {

        int group = digest(secret,(simulationId - baseId)/4);

        int groupOffset = (simulationId - baseId)%4;

        if(groupOffset < 0 ) {
            groupOffset += 4;
        }

        return permutation(group, groupOffset);
    }

    private static int digest(int secret, int value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");

            md.update(((Integer)secret).byteValue());
            md.update(((Integer)value).byteValue());

            return bytesToInt(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static int bytesToInt(byte[] bytes) {
        int d = 0;

        for(int i = 0; i < Math.max(bytes.length,4); i++) {
            d += ((bytes[i] & 0xFF) << (24-i*8));
        }

        return d;
    }
}
