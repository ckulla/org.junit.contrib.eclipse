package org.junit.contrib.eclipse.ui;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WorkspaceUtil implements MethodRule {

	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement () {

			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate ();
				} finally {
					cleanWorkspace ();
					waitForAutoBuild ();
				}
			}
		};
	}

	public IProject createProject(String name) throws CoreException {
		IProject project = root ().getProject (name);
		return createProject (project);
	}

	public IProject createProject(IProject project) throws CoreException {
		if (!project.exists ())
			project.create (monitor ());
		project.open (monitor ());
		return project;
	}

	public IProject importExistingProject(Path projectPath) throws CoreException {
		IProjectDescription description = ResourcesPlugin.getWorkspace ().loadProjectDescription (
					projectPath.append ("/.project"));
		IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (description.getName ());
		project.create (description, monitor ());
		project.open (monitor ());
		return project;
	}

	public static void addNature(IProject project, String nature) throws CoreException {
		IProjectDescription description = project.getDescription ();
		String[] natures = description.getNatureIds ();

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy (natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = nature;
		description.setNatureIds (newNatures);
		project.setDescription (description, null);
	}

	public static void removeNature(IProject project, String nature) throws CoreException {
		IProjectDescription description = project.getDescription ();
		String[] natures = description.getNatureIds ();

		for (int i = 0; i < natures.length; ++i) {
			if (nature.equals (natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy (natures, 0, newNatures, 0, i);
				System.arraycopy (natures, i + 1, newNatures, i, natures.length - i - 1);
				description.setNatureIds (newNatures);
				project.setDescription (description, null);
				return;
			}
		}
	}

	public void setReference(final IProject from, final IProject to) throws CoreException {
		workspace ().run (new IWorkspaceRunnable () {

			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription projectDescription = from.getDescription ();
				IProject[] projects = projectDescription.getReferencedProjects ();
				IProject[] newProjects = new IProject[projects.length + 1];
				System.arraycopy (projects, 0, newProjects, 0, projects.length);
				newProjects[projects.length] = to;
				projectDescription.setReferencedProjects (newProjects);
				from.setDescription (projectDescription, monitor ());
			}
		}, monitor ());
	}

	public void removeReference(final IProject from, final IProject to) throws CoreException, InterruptedException {
		workspace ().run (new IWorkspaceRunnable () {

			public void run(IProgressMonitor monitor) throws CoreException {
				IProjectDescription projectDescription = from.getDescription ();
				IProject[] projects = projectDescription.getReferencedProjects ();
				for (int i = 0; i < projects.length; ++i) {
					if (to.equals (projects[i])) {
						// Remove the nature
						IProject[] newProjects = new IProject[projects.length - 1];
						System.arraycopy (projects, 0, newProjects, 0, i);
						System.arraycopy (projects, i + 1, newProjects, i, projects.length - i - 1);
						projectDescription.setReferencedProjects (newProjects);
						from.setDescription (projectDescription, null);
						return;
					}
				}
			}
		}, monitor ());
	}

	public IFile createFile(String wsRelativePath, String s) throws CoreException {
		return createFile (new Path (wsRelativePath), s);
	}

	public IFile createFile(IPath wsRelativePath, final String s) throws CoreException {
		final IFile file = root ().getFile (wsRelativePath);
		workspace ().run (new IWorkspaceRunnable () {

			public void run(IProgressMonitor monitor) throws CoreException {
				create (file.getParent ());
				file.delete (true, monitor ());
				file.create (new ByteArrayInputStream (s.getBytes ()), true, monitor ());
			}
		}, monitor ());
		return file;
	}

	public IResource file(String path) {
		return root ().findMember (new Path (path));
	}

	private void create(final IContainer container) throws CoreException {
		workspace ().run (new IWorkspaceRunnable () {

			public void run(IProgressMonitor monitor) throws CoreException {
				if (!container.exists ()) {
					create (container.getParent ());
					if (container instanceof IFolder) {
						((IFolder) container).create (true, true, monitor ());
					} else {
						IProject iProject = (IProject) container;
						createProject (iProject);
					}
				}
			}
		}, monitor ());
	}

	public IWorkspace workspace() {
		return ResourcesPlugin.getWorkspace ();
	}

	public IWorkspaceRoot root() {
		return workspace ().getRoot ();
	}

	protected IProgressMonitor monitor() {
		return new NullProgressMonitor ();
	}

	protected void cleanWorkspace() throws CoreException {
		IProject[] projects = root ().getProjects ();
		for (IProject iProject : projects) {
			if (iProject.exists ()) {
				iProject.delete (true, true, monitor ());
			}
		}
	}

	public void waitForFullBuild() throws CoreException {
		ResourcesPlugin.getWorkspace ().build (IncrementalProjectBuilder.FULL_BUILD, monitor ());
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager ().join (ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace ();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	public void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager ().join (ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace ();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
}
