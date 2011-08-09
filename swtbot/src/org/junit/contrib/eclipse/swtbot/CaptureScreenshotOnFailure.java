package org.junit.contrib.eclipse.swtbot;

import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class CaptureScreenshotOnFailure implements MethodRule {

	String screenShotPath = "./";

	public CaptureScreenshotOnFailure() {

	}

	public CaptureScreenshotOnFailure(String path) {
		screenShotPath = path;
	}

	public Statement apply(final Statement base, final FrameworkMethod method, Object target) {
		return new Statement () {
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate ();
				} catch (Throwable onHold) {
					String fileName = constructFilename (method);
					SWTUtils.captureScreenshot (fileName);
					throw onHold;
				}
			}

			private String constructFilename(final FrameworkMethod method) {
				return screenShotPath + method.getMethod ().getDeclaringClass ().getCanonicalName () + "."
							+ method.getName () + ".png";
			}
		};
	}
}
