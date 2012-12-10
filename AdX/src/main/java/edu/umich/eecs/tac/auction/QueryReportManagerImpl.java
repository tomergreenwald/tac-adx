/*
 * QueryReportManagerImpl.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.sim.QueryReportSender;

/**
 * @author Patrick Jordan, Lee Callender, Akshat Kaul
 */
public class QueryReportManagerImpl implements QueryReportManager {
      	protected Logger log = Logger.getLogger(QueryReportManagerImpl.class.getName());

	/**
	 * The advertisers
	 */
	private String[] advertisers;

	/**
	 * The advertisers indexed so far
	 */
	private int advertisersCount;

	/**
	 * The query reports
	 */
	private QueryReport[] queryReports;
        /**
	 * The query counter - used for calculating sample averages
	 */

//---------------------------Sampling Averages for different Query Classes----------------------------------

        private class QueryClass
        {
            private int queryClassCount;//how many queries does this QueryClass have?
             
            private class MapQueryClassCounterToPositions
            {
                private Map<Integer,Integer> positions;
                public MapQueryClassCounterToPositions()
                {
                    positions=new HashMap<Integer,Integer>();
                }
                public MapQueryClassCounterToPositions(Integer queryClassCount, Integer slot)
                {
                    positions=new HashMap<Integer,Integer>();
                    positions.put(queryClassCount, slot);
                }
                public void adPosition(Integer queryClassCount, Integer slot)
                {
                    positions.put(queryClassCount, slot);
//                    if(positions.size()>1){log.info("Position size is too big!!!!!! "+queryClassCount);}

                }
                public Map<Integer,Integer> getPositions()
                {
                    return positions;
                }
                public double average()
                {
                    
                    double sum=0;
                    Iterator<Integer> it = positions.values().iterator();
                    while (it.hasNext())
                    {
                        sum+=it.next();
                    }
//                    log.info("size:"+positions.size());
                    return sum/positions.size();
                }
            }

            private Map<String,MapQueryClassCounterToPositions> adMap;//position average and impression count for each advertiser
            public QueryClass()
            {                
                this.queryClassCount=0;
                adMap=new HashMap<String,MapQueryClassCounterToPositions>();
            }
            public int getQueryClassCount()
            {
                return queryClassCount;
            }
            public Map<String,MapQueryClassCounterToPositions> getadMap()
            {
                return adMap;
            }
            public void setQueryClassCount(int count)
            {
                this.queryClassCount=count;
            }
            public void addtoadMap(String advertiserName, Integer count, Integer slot)
            {
                if(!adMap.containsKey(advertiserName))
                    adMap.put(advertiserName, new MapQueryClassCounterToPositions(count,slot));
                else
                {
                    adMap.get(advertiserName).adPosition(count, slot);
                }
            }
        }
        /**
	 * Map of Sample Averages
	 */
	private Map<Query, QueryClass> querySampler;

        /**
	 * Properties used by method sampleQuery
	 */
        private Query lastQuery;
        private boolean substitute=false;
        int randomNumber=-1;
//--------------------------------------------------------------------------------
	/**
	 * Query report sender
	 */
	private QueryReportSender queryReportSender;

	/**
	 * Create a new query report manager
	 * 
	 * @param queryReportSender
	 *            the query report sender
	 * @param advertisersCount
	 *            the initial advertiser count
	 */
	public QueryReportManagerImpl(QueryReportSender queryReportSender,
			int advertisersCount) {
		this.queryReportSender = queryReportSender;
		advertisers = new String[advertisersCount];
		queryReports = new QueryReport[advertisersCount];
		this.advertisersCount = advertisersCount;
                querySampler = new HashMap<Query,QueryClass>();
                lastQuery = new Query();                
                log.info("QueryReportManager reset");
	}

	/**
	 * Add an advertiser to the dataset
	 * 
	 * @param name
	 *            the name of the advertiser to add
	 */
	public void addAdvertiser(String name) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount, name);
		if (index < 0) {
			doAddAccount(name);
		}
	}

	private synchronized int doAddAccount(String name) {
		if (advertisersCount == advertisers.length) {
			int newSize = advertisersCount + 8;
			advertisers = (String[]) ArrayUtils.setSize(advertisers, newSize);
			queryReports = (QueryReport[]) ArrayUtils.setSize(queryReports,
					newSize);
		}
		advertisers[advertisersCount] = name;

		return advertisersCount++;
	}

	protected void addClicks(String name, Query query, int clicks, double cost) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount, name);
		if (index < 0) {
			index = doAddAccount(name);
		}

		if (queryReports[index] == null) {
			queryReports[index] = new QueryReport();
		}

		queryReports[index].addClicks(query, clicks, cost);
	}

	protected void addImpressions(String name, Query query, int regular, int promoted, Ad ad, double positionSum) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount, name);

        if (index < 0) {
			index = doAddAccount(name);
		}

		if (queryReports[index] == null) {
			queryReports[index] = new QueryReport();
		}

		queryReports[index].addImpressions(query, regular, promoted, ad, positionSum);
	}

	/**
	 * Send each advertiser their report.
	 */
	public void sendQueryReportToAll() {
		// Make sure all query reports are non-null
		for (int i = 0; i < advertisersCount; i++) {
			if (queryReports[i] == null) {
				queryReports[i] = new QueryReport();
			}
		}

		// For each advertiser, tell the other advertisers about their positions
		// and adLinks
		for (int advertiserIndex = 0; advertiserIndex < advertisersCount; advertiserIndex++) {

			QueryReport baseReport = queryReports[advertiserIndex];

			String baseAdvertiser = advertisers[advertiserIndex];

			for (int index = 0; index < baseReport.size(); index++) {
				Query query = baseReport.getQuery(index);

				double position = baseReport.getPosition(index);
                                double sampleAvgPosition=-1;
				Ad ad = baseReport.getAd(index);
//                                log.info(""+query+" "+baseAdvertiser+" "+position);
                                if(querySampler.get(query)==null)
                                {
                                    sampleAvgPosition=Double.NaN;
                                }
                                else
                                {
                                    if(querySampler.get(query).getadMap().get(baseAdvertiser)==null)
                                    {
                                        sampleAvgPosition=Double.NaN;
                                    }
                                    else
                                    {
                                        sampleAvgPosition=querySampler.get(query).getadMap().get(baseAdvertiser).average();
                                    }
                                }
//                                if(!(position==sampleAvgPosition||(new Double(position).isNaN()&& new Double(sampleAvgPosition).isNaN())))
//                                {
//                                    log.info(""+query+" "+baseAdvertiser+" "+position);
//                                    log.info("Sample:"+sampleAvgPosition);
//                                }
				for (int otherIndex = 0; otherIndex < advertisersCount; otherIndex++) {
					queryReports[otherIndex].setAdAndPosition(query,
							baseAdvertiser, ad, sampleAvgPosition);
				}
			}
		}

		// Send the query reports
		for (int i = 0; i < advertisersCount; i++) {
			QueryReport report = queryReports[i];

			queryReports[i] = null;

			queryReportSender.sendQueryReport(advertisers[i], report);

                        int impressions = 0;
                        int clicks = 0;

                        for(int index = 0; index < report.size(); index++) {
                            impressions += report.getImpressions(index);
                            clicks += report.getClicks(index);
                        }

                        queryReportSender.broadcastImpressions(advertisers[i], impressions);
                        queryReportSender.broadcastClicks(advertisers[i], clicks);
                }
                querySampler = new HashMap<Query,QueryClass>();

        }

//--------------------------------Calculate Sample Averages for each advertiser in each Query Class--------------
        //Reservoir Sampling
        protected void sampleQuery(Query newQuery, int slot, String advertiser)
        {
//            if(check>0){check--;
            int samplecount=10;
            if(querySampler.containsKey(newQuery))
            {
                QueryClass i=querySampler.get(newQuery);
                int count=i.getQueryClassCount();//last time's count
                
                    if((!lastQuery.equals(newQuery))||(lastQuery.equals(newQuery)&&slot==1))
                    {
                        //this is a new query. increase counter for this query class.
                        count++;
                        i.setQueryClassCount(count);
                        if(count>samplecount)
                        {
                            randomNumber=new Random().nextInt(count)+1;
                            
                            if(randomNumber<=samplecount)
                            {
                                substitute=true;
                                //for this query class, remove all advertiser positions
                                //corresponding to count r
                                Iterator<String> iter=i.getadMap().keySet().iterator();
                                while(iter.hasNext())
                                {
                                    i.getadMap().get(iter.next()).getPositions().remove(randomNumber);

                                }
                            }
                            else
                            {
                                substitute=false;
                            }
                        }
                    }
                    else
                    {
                        //dont increase counter for this query class.
                    }
                    if(count<=samplecount)
                    {
                        i.addtoadMap(advertiser, count, new Integer(slot));
                        querySampler.put(newQuery, i);
                    }
                    else
                    {
                        if(substitute)
                        {
                            i.addtoadMap(advertiser, randomNumber, new Integer(slot));
                            querySampler.put(newQuery, i);
                        }
                        else
                        {
                            //do nothing
                        }
                    }                
            }
            else
            {
                QueryClass i=new QueryClass();
                i.setQueryClassCount(1);
                i.addtoadMap(advertiser,1,new Integer(slot));
                querySampler.put(newQuery, i);
            }
            lastQuery=newQuery;
//         if(querySampler.get(newQuery).getadMap().get(advertiser)==null)   log.info("not substituted "+newQuery+slot+advertiser+" "+re);
//         else log.info("SAMPLER:"+newQuery+slot+advertiser+" "+querySampler.get(newQuery).getadMap().get(advertiser).average()+" "+re);
//        }
        }
//------------------------------------------------------------------------------------------------------
	public int size() {
		return advertisersCount;
	}

	public void queryIssued(Query query) {
	}

	public void viewed(Query query, Ad ad, int slot, String advertiser, boolean isPromoted) {
                sampleQuery(query,slot,advertiser);

		if (isPromoted) {
			addImpressions(advertiser, query, 0, 1, ad, slot);
		} else {
			addImpressions(advertiser, query, 1, 0, ad, slot);
		}
	}

	public void clicked(Query query, Ad ad, int slot, double cpc, String advertiser) {
		addClicks(advertiser, query, 1, cpc);
	}

	public void converted(Query query, Ad ad, int slot, double salesProfit, String advertiser) {
	}
}
