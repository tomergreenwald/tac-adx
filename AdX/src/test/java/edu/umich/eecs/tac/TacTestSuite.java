package edu.umich.eecs.tac;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.umich.eecs.tac.agents.AgentsTestSuite;
import edu.umich.eecs.tac.auction.AuctionTestSuite;
import edu.umich.eecs.tac.props.PropsTestSuite;
import edu.umich.eecs.tac.sim.SimTestSuite;
import edu.umich.eecs.tac.user.UserTestSuite;

@RunWith(value = Suite.class)
@SuiteClasses(value = { AgentsTestSuite.class, AuctionTestSuite.class,
		PropsTestSuite.class, SimTestSuite.class, UserTestSuite.class,
		UserTestSuite.class })
public class TacTestSuite {

}
