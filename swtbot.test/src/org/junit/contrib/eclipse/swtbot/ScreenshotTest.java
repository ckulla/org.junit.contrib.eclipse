package org.junit.contrib.eclipse.swtbot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.contrib.eclipse.swtbot.FileMatcher.exists;

import java.io.File;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ScreenshotTest {

	@Test
	public void test() throws SecurityException, Throwable {
		try {
			CaptureScreenshotOnFailure screenshotOnFailure = new CaptureScreenshotOnFailure ("target/test/screenshot/");
			screenshotOnFailure.apply (new Statement () {

				@Override
				public void evaluate() throws Throwable {
					throw new RuntimeException ();
				}

			}, new FrameworkMethod (this.getClass ().getMethods ()[0]), this).evaluate ();
		} catch (RuntimeException e) {
			assertThat (new File ("target/test/screenshot/" + getClass ().getCanonicalName () + "." + "test.png"),
						exists ());
			return;
		}
		assertTrue (false);
	}
}
