package org.seasar.s2jsfplugin.test;

import junit.framework.Test;

import org.pluginbuilder.autotestsuite.AllTestSuite;

public class AllTests {

	public static Test suite() {
		return new AllTestSuite("MyTests",
	               ".*(test|tests)$", // search these plugins for tests...
	               null, // without any exclusions and
	               ".*", // include all tests ...
	               ".*ServerComm.*$" // ... except this one
	               ) ; 
	}

}
