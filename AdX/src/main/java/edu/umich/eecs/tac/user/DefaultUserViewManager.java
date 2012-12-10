/*
 * DefaultUserViewManager.java
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

import static edu.umich.eecs.tac.user.UserUtils.calculateClickProbability;
import static edu.umich.eecs.tac.user.UserUtils.calculateConversionProbability;
import static edu.umich.eecs.tac.user.UserUtils.findAdvertiserEffect;
import static edu.umich.eecs.tac.user.UserUtils.modifySalesProfitForManufacturerSpecialty;

import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Pricing;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.RecentConversionsTracker;

/**
 * @author Patrick Jordan, Ben Cassell, Lee Callender
 */
public class DefaultUserViewManager implements UserViewManager {
    private Logger log = Logger.getLogger(DefaultUserViewManager.class
            .getName());

    private UserEventSupport eventSupport;

    private Map<String, AdvertiserInfo> advertiserInfo;

    private SlotInfo slotInfo;

    private RetailCatalog catalog;

    private Random random;

    private UserClickModel userClickModel;

    private RecentConversionsTracker recentConversionsTracker;

    public DefaultUserViewManager(RetailCatalog catalog,
                                  RecentConversionsTracker recentConversionsTracker,
                                  Map<String, AdvertiserInfo> advertiserInfo, SlotInfo slotInfo) {
        this(catalog, recentConversionsTracker, advertiserInfo, slotInfo,
                new Random());
    }

    public DefaultUserViewManager(RetailCatalog catalog,
                                  RecentConversionsTracker recentConversionsTracker,
                                  Map<String, AdvertiserInfo> advertiserInfo, SlotInfo slotInfo,
                                  Random random) {
        if (catalog == null) {
            throw new NullPointerException("Retail catalog cannot be null");
        }

        if (slotInfo == null) {
            throw new NullPointerException("Auction info cannot be null");
        }

        if (recentConversionsTracker == null) {
            throw new NullPointerException(
                    "Recent conversions tracker cannot be null");
        }

        if (advertiserInfo == null) {
            throw new NullPointerException(
                    "Advertiser information cannot be null");
        }

        if (random == null) {
            throw new NullPointerException("Random generator cannot be null");
        }

        this.catalog = catalog;
        this.random = random;
        this.recentConversionsTracker = recentConversionsTracker;
        this.advertiserInfo = advertiserInfo;
        this.slotInfo = slotInfo;
        eventSupport = new UserEventSupport();
    }

    public void nextTimeUnit(int timeUnit) {

    }

    public boolean processImpression(User user, Query query, Auction auction) {
        fireQueryIssued(query);

        boolean converted = false;
        boolean clicking = true;

        // Grab the continuation probability from the user click model.
        double continuationProbability = 0.0;

        int queryIndex = userClickModel.queryIndex(query);

        if (queryIndex < 0) {
            log.warning(String.format("Query: %s does not have a click model.",
                    query));
        } else {
            continuationProbability = userClickModel
                    .getContinuationProbability(queryIndex);
        }

        Ranking ranking = auction.getRanking();
        Pricing pricing = auction.getPricing();

        // Users will view all adLinks, but may only click on some.
        for (int i = 0; i < ranking.size(); i++) {

            AdLink ad = ranking.get(i);
            boolean isPromoted = ranking.isPromoted(i);

            fireAdViewed(query, ad, i + 1, isPromoted);

            // If the user is still considering clicks, process the attempt
            if (clicking) {

                AdvertiserInfo info = advertiserInfo.get(ad.getAdvertiser());

                double promotionEffect = ranking.isPromoted(i) ? slotInfo
                        .getPromotedSlotBonus() : 0.0;

                double clickProbability = calculateClickProbability(user, ad.getAd(), info.getTargetEffect(),
                        promotionEffect, findAdvertiserEffect(query, ad, userClickModel));

                if (random.nextDouble() <= clickProbability) {
                    // Users has clicked on the ad

                    fireAdClicked(query, ad, i + 1, pricing.getPrice(ad));

                    double conversionProbability = calculateConversionProbability( user, query, info,
                            recentConversionsTracker.getRecentConversions(ad.getAdvertiser()));
                    if(user.isTransacting()){
                        if (random.nextDouble() <= conversionProbability) {
                            // User has converted and will no longer click

                            double salesProfit = catalog.getSalesProfit(user
                                    .getProduct());

                            fireAdConverted(query, ad, i + 1, modifySalesProfitForManufacturerSpecialty(user,
                                            info.getManufacturerSpecialty(), info.getManufacturerBonus(), salesProfit));

                            converted = true;
                            clicking = false;
                        }
                    }
                }
            }

            if (random.nextDouble() > continuationProbability) {
                clicking = false;
            }
        }

        return converted;
    }

    public boolean addUserEventListener(UserEventListener listener) {
        return eventSupport.addUserEventListener(listener);
    }

    public boolean containsUserEventListener(UserEventListener listener) {
        return eventSupport.containsUserEventListener(listener);
    }

    public boolean removeUserEventListener(UserEventListener listener) {
        return eventSupport.removeUserEventListener(listener);
    }

    private void fireQueryIssued(Query query) {
        eventSupport.fireQueryIssued(query);
    }

    private void fireAdViewed(Query query, AdLink ad, int slot, boolean isPromoted) {
        eventSupport.fireAdViewed(query, ad, slot, isPromoted);
    }

    private void fireAdClicked(Query query, AdLink ad, int slot, double cpc) {
        eventSupport.fireAdClicked(query, ad, slot, cpc);
    }

    private void fireAdConverted(Query query, AdLink ad, int slot, double salesProfit) {
        eventSupport.fireAdConverted(query, ad, slot, salesProfit);
    }

    public UserClickModel getUserClickModel() {
        return userClickModel;
    }

    public void setUserClickModel(UserClickModel userClickModel) {
        this.userClickModel = userClickModel;
    }
}
