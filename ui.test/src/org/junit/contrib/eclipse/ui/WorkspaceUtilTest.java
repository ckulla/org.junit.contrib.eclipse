package org.junit.contrib.eclipse.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WorkspaceUtilTest {

	@Test
	public void testRemoveAllProjects() throws Throwable {
		final WorkspaceUtil workspaceUtil = new WorkspaceUtil ();
		try {
			workspaceUtil.apply (new Statement () {

				@Override
				public void evaluate() throws Throwable {
					assertNotNull (workspaceUtil.createProject ("test"));
					throw new RuntimeException ();
				}

			}, new FrameworkMethod (this.getClass ().getMethods ()[0]), this).evaluate ();
		} catch (RuntimeException e) {
			assertFalse (workspaceUtil.getProject ("test").exists ());
			return;
		}
		fail ("An exception should have been thrown");
	}
}
