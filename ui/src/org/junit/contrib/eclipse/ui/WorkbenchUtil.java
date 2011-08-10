package org.junit.contrib.eclipse.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WorkbenchUtil implements MethodRule {

	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement () {

			@Override
			public void evaluate() throws Throwable {
				closeWelcomePage ();
				try {
					base.evaluate ();
				} finally {
					tearDown ();
				}
			}
		};
	}

	void tearDown() {
		closeAllEditors ();
		closeAllViews ();
	}

	public IWorkbenchPage getActivePage() {
		if (getWorkbenchWindow () != null)
			return getWorkbenchWindow ().getActivePage ();
		return null;
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		if (getWorkbench () != null) {
			return getWorkbench ().getActiveWorkbenchWindow ();
		}
		return null;
	}

	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench ();
	}

	public IViewPart openView(String viewId) {
		try {
			IViewPart viewPart = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().showView (viewId);
			if (viewPart == null) {
				org.junit.Assert.fail ("Could not open a view of id: " + viewId);
			}
			return viewPart;
		} catch (PartInitException e) {
			throw new RuntimeException (e);
		}
	}

	public void closeAllEditors() {
		if (getActivePage () != null)
			getActivePage ().closeAllEditors (false);
	}

	public void closeAllViews() {
		if (getActivePage () != null) {
			IViewReference[] viewRefs = getActivePage ().getViewReferences ();
			for (IViewReference viewRef : viewRefs) {
				getActivePage ().hideView (viewRef.getView (false));
			}
		}
	}

	public void closeWelcomePage() {
		if (getWorkbench ().getIntroManager ().getIntro () != null) {
			getWorkbench ().getIntroManager ().closeIntro (getWorkbench ().getIntroManager ().getIntro ());
		}
	}

	public void sleep(long i) throws InterruptedException {
		Display displ = Display.getCurrent ();
		if (displ != null) {
			long timeToGo = System.currentTimeMillis () + i;
			while (System.currentTimeMillis () < timeToGo) {
				if (!displ.readAndDispatch ()) {
					displ.sleep ();
				}
			}
			displ.update ();
		} else {
			Thread.sleep (i);
		}

	}
}
