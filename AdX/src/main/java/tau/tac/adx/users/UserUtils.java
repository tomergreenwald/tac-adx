/*
 * UserUtils.java
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
package tau.tac.adx.users;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.user.User;

/**
 * @author Patrick Jordan
 */
public class UserUtils {
	private UserUtils() {
	}

	public static double modifyOdds(double probability, double effect) {
		return probability * effect
				/ (effect * probability + (1.0 - probability));
	}

	public static double modifySalesProfitForManufacturerSpecialty(User user,
			String manufacturerSpecialty, double MSB, double salesProfit) {
		if (manufacturerSpecialty.equals(user.getProduct().getManufacturer()))
			salesProfit *= (1.0 + MSB);

		return salesProfit;
	}

	public static double modifyOddsForComponentSpecialty(User user,
			String componentSpecialty, double effect, double probability) {
		if (user.getProduct().getComponent().equals(componentSpecialty)) {
			probability = modifyOdds(probability, 1.0 + effect);
		}

		return probability;
	}

	public static double calculateConversionProbability(User user, Query query,
			AdvertiserInfo advertiserInfo, double sales) {
		double criticalSales = advertiserInfo.getDistributionCapacity();

		double probability = advertiserInfo.getFocusEffects(query.getType())
				* Math.pow(advertiserInfo.getDistributionCapacityDiscounter(),
						Math.max(0.0, sales - criticalSales));

		probability = modifyOddsForComponentSpecialty(user,
				advertiserInfo.getComponentSpecialty(),
				advertiserInfo.getComponentBonus(), probability);

		return probability;
	}

	public static double calculateClickProbability(User user, Ad ad,
			double targetEffect, double promotionEffect, double advertiserEffect) {

		double probability = advertiserEffect;

		if (!ad.isGeneric()) {
			if (user.getProduct().equals(ad.getProduct())) {
				probability = modifyOdds(probability, (1.0 + promotionEffect)
						* (1.0 + targetEffect));
			} else {
				probability = modifyOdds(probability, (1.0 + promotionEffect)
						/ (1.0 + targetEffect));
			}
		} else {
			probability = modifyOdds(probability, 1.0 + promotionEffect);
		}

		return probability;
	}

}
