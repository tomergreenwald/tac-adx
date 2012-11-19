/*
 * PermutationOfEightGenerator.java
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

/**
 * Generator for permutations of ordered set (0,1,2,3,4,5,6,7).
 *
 * @author Patrick R. Jordan
 */
public class PermutationOfEightGenerator {
    /**
     * Total number of distinct permutations (8!).
     */
    public static final int TOTAL_PERMUTATIONS = 40320;
    /**
     * The current permutation.
     */
    private int[] current;
    /**
     * Number of remaining distrint permutations.
     */
    private int remaining;

    public PermutationOfEightGenerator() {
        current = new int[8];

        for (int i = 0; i < 8; i++) {
            current[i] = i;
        }

        remaining = TOTAL_PERMUTATIONS;
    }

    /**
     * Returns <code>true</code> if there are additional permutations.
     * @return <code>true</code> if there are additional permutations.
     */
    public boolean hasNext() {
        return remaining > 0;
    }

    /**
     * Returns the next permutation of the ordered set.
     *
     * <p>Rosen's next permutation function. Kenneth H. Rosen, Discrete Mathematics and Its Applications, 2nd edition
     * (NY: McGraw-Hill, 1991), pp. 282-284.</p>
     *        
     * @return the next permutation of the ordered set.
     */
    public int[] next() {

        if (remaining < TOTAL_PERMUTATIONS) {
            int swap;

            // Find largest index j with current[j] < current[j+1]

            int j = current.length - 2;
            while (current[j] > current[j + 1]) {
                j--;
            }

            // Find index k such that current[k] is smallest integer
            // greater than current[j] to the right of current[j]

            int k = current.length - 1;
            while (current[j] > current[k]) {
                k--;
            }

            // Interchange current[j] and current[k]

            swap = current[k];
            current[k] = current[j];
            current[j] = swap;

            // Put tail end of permutation after jth position in increasing order

            int r = current.length - 1;
            int s = j + 1;

            while (r > s) {
                swap = current[s];
                current[s] = current[r];
                current[r] = swap;
                r--;
                s++;
            }
        }

        remaining--;
        
        return current.clone();
    }
}
