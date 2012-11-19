package edu.umich.eecs.tac.auction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)
@SuiteClasses(value = { AuctionUtilsTest.class, BidManagerImplTest.class,
		BidTrackerImplTest.class, LahaiePennockAuctionFactoryTest.class,
		QueryReportManagerImplTest.class, SpendTrackerImplTest.class })
public class AuctionTestSuite {

}
