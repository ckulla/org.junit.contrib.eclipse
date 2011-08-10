package org.junit.contrib.eclipse.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WorkbenchUtilTest {

	private static final String VIEW_ID = "org.eclipse.ui.views.ContentOutline";

	@Test
	public void testCloseAllViews() throws Throwable {
		final WorkbenchUtil workbenchUtil = new WorkbenchUtil ();
		try {
			workbenchUtil.apply (new Statement () {

				@Override
				public void evaluate() throws Throwable {
					assertNotNull (workbenchUtil.getActivePage ().showView (VIEW_ID));
					// Note: we use our own exception type here, otherwise it may
					// shadow other exceptions thrown by the above invocation of
					// showView()
					throw new LeaveEvaluateException ();
				}

			}, new FrameworkMethod (this.getClass ().getMethods ()[0]),
						this).evaluate ();
		} catch (LeaveEvaluateException e) {
			assertNull (workbenchUtil.getActivePage ().findView (VIEW_ID));
			return;
		}
		fail ("An exception should have been thrown");
	}
}
