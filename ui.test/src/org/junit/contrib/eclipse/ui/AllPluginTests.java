package org.junit.contrib.eclipse.ui;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	WorkbenchUtilTest.class,
	WorkspaceUtilTest.class })
public class AllPluginTests {

}
