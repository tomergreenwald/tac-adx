package tau.tac.adx.system;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SystemTestCodePersistency.class,
		SystemTestMessageValidation.class })
public class SystemTestSuite {

}
