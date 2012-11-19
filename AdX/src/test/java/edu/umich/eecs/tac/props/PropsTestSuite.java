package edu.umich.eecs.tac.props;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)
@SuiteClasses(value = { AAInfoTest.class,
		AbstractAdvertiserKeyedReportTransportableTest.class, AdLinkTest.class,
		AdTest.class, AdvertiserInfoTest.class, AuctionInfoTest.class,
		AuctionTest.class, BankStatusTest.class, BidBundleTest.class,
		CompositeIteratorTest.class, ManufacturerComponentComposableTest.class,
		PricingTest.class, ProductTest.class, PublisherInfoTest.class,
		QueryReportTest.class, QueryTest.class, QueryTypeTest.class,
		RankingTest.class, RetailCatalogTest.class, SalesReportTest.class,
		SlotInfoTest.class, UserClickModelTest.class })
public class PropsTestSuite {

}
