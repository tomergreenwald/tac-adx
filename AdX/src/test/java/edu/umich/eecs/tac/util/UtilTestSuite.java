package edu.umich.eecs.tac.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.umich.eecs.tac.util.permutation.PermutationTestSuite;
import edu.umich.eecs.tac.util.sampling.SamplingTestSuite;

@RunWith(value = Suite.class)
@SuiteClasses(value = { PermutationTestSuite.class, SamplingTestSuite.class })
public class UtilTestSuite {

}
