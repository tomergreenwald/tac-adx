package tau.tac.adx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.umich.eecs.tac.TacTestSuite;

@RunWith(value = Suite.class)
@SuiteClasses(value = { TacTestSuite.class, AdxTestSuite.class })
public class GlobalTestSuite {

}
