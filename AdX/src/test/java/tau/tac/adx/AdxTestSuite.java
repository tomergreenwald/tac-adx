package tau.tac.adx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tau.tac.adx.users.UsersTestSuite;
import tau.tac.adx.util.UtilsTestSuite;

@RunWith(value = Suite.class)
@SuiteClasses(value = { UtilsTestSuite.class, UsersTestSuite.class })
public class AdxTestSuite {

}
