package org.junit.contrib.eclipse.ui;

import org.junit.Test;
import org.junit.contrib.rules.Rules;
import org.junit.contrib.rules.RulesTestRunner;
import org.junit.runner.RunWith;

@RunWith(RulesTestRunner.class)
@Rules({ WorkbenchUtil.class, WorkspaceUtil.class })
public class PluginTest {

	@Test
	public void test() {
		// do nothing
	}

}
