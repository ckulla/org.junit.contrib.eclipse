package org.junit.contrib.eclipse.swtbot;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class FileMatcher {

	public static org.hamcrest.Matcher<File> exists() {
		return new TypeSafeMatcher<File> () {

			File fileTested;

			@Override
			public boolean matchesSafely(File item) {
				fileTested = item;
				return item.exists ();
			}

			public void describeTo(Description description) {
				description.appendText (" that file ");
				description.appendValue (fileTested);
				description.appendText (" exists");
			}
		};
	}
}
