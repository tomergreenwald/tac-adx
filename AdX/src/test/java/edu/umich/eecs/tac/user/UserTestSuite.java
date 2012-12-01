package edu.umich.eecs.tac.user;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(value = Suite.class)
@SuiteClasses(value = { DefaultUserManagerBuilderTest.class,
		DefaultUserManagerTest.class, DefaultUserQueryManagerBuilderTest.class,
		DefaultUserQueryManagerTest.class, DefaultUsersBehaviorTest.class,
		DefaultUsersInitializerTest.class,
		DefaultUserTransitionManagerBuilderTest.class,
		DefaultUserTransitionManagerTest.class,
		DefaultUserViewManagerBuilderTest.class,
		DefaultUserViewManagerTest.class, QueryStateTest.class,
		UserEventSupportTest.class, UserTest.class, UserUtilsTest.class})
public class UserTestSuite {

}
