package edu.umich.eecs.tac.util.sampling;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)
@SuiteClasses(value = { SynchronizedMutableSamplerTest.class,
		SynchronizedSamplerTest.class, WheelSamplerTest.class })
public class SamplingTestSuite {

}
