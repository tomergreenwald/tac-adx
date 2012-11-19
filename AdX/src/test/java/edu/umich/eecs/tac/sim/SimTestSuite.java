package edu.umich.eecs.tac.sim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)
@SuiteClasses(value = { BankTest.class, DefaultSalesAnalystTest.class,
		DummySimulationAgent.class, DummyTACAASimulation.class })
public class SimTestSuite {

}
