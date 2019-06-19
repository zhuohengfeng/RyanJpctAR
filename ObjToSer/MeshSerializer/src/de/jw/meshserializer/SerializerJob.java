package de.jw.meshserializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.threed.jpct.DeSerializer;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;

import de.jw.meshserializer.SerializeTask.TaskType;

public class SerializerJob extends Job {

	private List<SerializeTask> tasks = new ArrayList<SerializeTask>();
	private PrintStream consoleOutputStream = new PrintStream(new EclipseConsoleWriter("Mesh serializer").getMessageStream());

	public List<SerializeTask> getTasks() {
		return tasks;
	}

	public SerializerJob() {
		super("Serializing meshes");
	}

	/**
	 * Scans a folder for model files and adds cleanup tasks to this job.
	 * Scheduling a job with these cleanup tasks will delete all generated .ser
	 * S * files.
	 * 
	 * @param sourceFolder
	 * @return
	 * @throws CoreException
	 */
	public SerializerJob generateCleanupTasks(final IFolder sourceFolder) throws CoreException {
		if (!sourceFolder.exists()) {
			// nothing to clean up.
			return this;
		}
		sourceFolder.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {

				if (resource.getType() == IResource.FILE) {

					if (Constants.supportedExtensions.contains(resource.getFileExtension().toLowerCase()) && resource.getParent().equals(sourceFolder)) {
						// Remove any markers for this file
						tasks.add(new SerializeTask(TaskType.REMOVE_MARKERS, (IFile) resource));
						// Remove all .ser files for this file
						tasks.add(new SerializeTask(TaskType.REMOVE_ORPHAN, (IFile) resource));
					}

				}

				return true;
			}
		});
		return this;
	}

	public SerializerJob generateInitTasks(final IFolder sourceFolder) throws CoreException {
		ProjectPropertiesAdapter propertiesAdapter = new ProjectPropertiesAdapter(sourceFolder.getProject());

		if (!propertiesAdapter.isSerializerEnabled()) {
			return this;
		}

		if (!sourceFolder.exists()) {
			// No models to use
			return this;
		}

		sourceFolder.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) throws CoreException {

				if (resource.getType() == IResource.FILE) {

					if (Constants.supportedExtensions.contains(resource.getFileExtension()) && resource.getParent().equals(sourceFolder)) {

						tasks.add(new SerializeTask(TaskType.CREATE, (IFile) resource));
					}

				}

				return true;
			}
		});
		return this;
	}

	public SerializerJob generateDeltaTasks(IResourceDelta rootDelta) throws CoreException {
		rootDelta.accept(new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource res = delta.getResource();

				// Project might got deleted, so lets check if it still exists. Project needs to be opened too.
				if (res.getType() == IResource.FILE && res.getProject().exists() && res.getProject().isOpen()) {

					ProjectPropertiesAdapter propertiesAdapter = new ProjectPropertiesAdapter(res.getProject());

					if (propertiesAdapter.isSerializerEnabled() && Constants.supportedExtensions.contains(res.getFileExtension())) {

						if (res.getParent().getProjectRelativePath().toString().equals(propertiesAdapter.getSourceFolder())) {
							// Delta contains a resource inside the source
							// folder.
							switch (delta.getKind()) {
							case IResourceDelta.ADDED:
								// serialize newly created file
								tasks.add(new SerializeTask(TaskType.CREATE, (IFile) res));
								break;
							case IResourceDelta.CHANGED:
								if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
									// serialize again upon content change
									tasks.add(new SerializeTask(TaskType.UPDATE, (IFile) res));
								}
								break;
							case IResourceDelta.REMOVED:
								// remove serialized files too
								tasks.add(new SerializeTask(TaskType.DELETE, (IFile) res));
								break;
							}
						} else {
							// Something outside the source folder changed
							if (delta.getKind() == IResourceDelta.ADDED) {
								// It might be a model file that previously was
								// in our source folder, so lets remove any
								// markers.
								tasks.add(new SerializeTask(TaskType.REMOVE_MARKERS, (IFile) res));
							}

						}

					}

				}

				return true;
			}

		});
		return this;
	}

	/**
	 * Removes any data for a file that does not exist anymore. This includes
	 * markers and serialized files.
	 * 
	 * @param modelFile
	 * @throws CoreException
	 */
	private void removeOrphanData(IFile modelFile) throws CoreException {

		ProjectPropertiesAdapter propertiesAdapter = new ProjectPropertiesAdapter(modelFile.getProject());

		List<String> serFiles = propertiesAdapter.getFiles(modelFile);

		if (serFiles != null) {
			for (String file : serFiles) {
				if (modelFile.getProject().getFile(file).exists()) {
					modelFile.getProject().getFile(file).delete(true, null);
				}
			}
			propertiesAdapter.setFiles(modelFile, null);
		}

	}

	private void serialize(IFile modelFile, IFolder targetFolder) throws CoreException {
		ProblemMarkerAdapter problemMarkerAdapter = new ProblemMarkerAdapter(modelFile);
		ProjectPropertiesAdapter propertiesAdapter = new ProjectPropertiesAdapter(modelFile.getProject());

		// Redirect System.out to capture output from Loader...
		PrintStream realSysout = System.out;
		System.setOut(consoleOutputStream);
		consoleOutputStream.println("Now processing " + modelFile + "...");
		consoleOutputStream.println();

		try {
			Object3D[] objs = null;

			// Load the model file...
			InputStream contents = modelFile.getContents();
			if ("3ds".equalsIgnoreCase(modelFile.getFileExtension())) {
				objs = Loader.load3DS(contents, 1.0f);
			} else if ("md2".equalsIgnoreCase(modelFile.getFileExtension())) {
				objs = new Object3D[] { Loader.loadMD2(contents, 1.0f) };
			} else if ("asc".equalsIgnoreCase(modelFile.getFileExtension())) {
				objs = new Object3D[] { Loader.loadASC(contents, 1.0f, false) };
			} else if ("obj".equalsIgnoreCase(modelFile.getFileExtension())) {
				objs = Loader.loadOBJ(contents, null, 1.0f);
			} else if ("jaw".equalsIgnoreCase(modelFile.getFileExtension())) {
				objs = new Object3D[] { Loader.loadJAW(contents, 1.0f, false) };
			}

			contents.close();

			if (objs.length == 0 || (objs.length == 1 && objs[0] == null)) {
				problemMarkerAdapter.addWarning("No models generated for this file, please check the console for details.");
			} else {

				if (!targetFolder.exists()) {
					targetFolder.create(false, true, null);
				}

				List<String> serFiles = new ArrayList<String>();

				int i = 0;
				for (Object3D o : objs) {
					o.build();

					// TODO: rework
					String filename = modelFile.getName().substring(0, modelFile.getName().lastIndexOf("."));
					IFile targetFile = null;
					if (objs.length == 1) {
						targetFile = targetFolder.getFile(filename + ".ser");
					} else {
						targetFile = targetFolder.getFile(filename + "_" + i + ".ser");
					}

					if (targetFile.exists()) {
						targetFile.delete(true, null);
					}

					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					new DeSerializer().serialize(o, baos, true);

					targetFile.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
					i++;

					serFiles.add(targetFile.getProjectRelativePath().toString());
				}

				propertiesAdapter.setFiles(modelFile, serFiles);

				//if we've made it here, everything went fine. remove all markers.
				problemMarkerAdapter.clearMarkers();
			}
			
		} catch (IOException e) {
			problemMarkerAdapter.addError("An IO error occured while processing the model file. (" + e.getMessage() + ")");
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (Exception e) {
			problemMarkerAdapter.addError("An internal error occured while processing the model file. (" + e.getMessage() + ")");
			throw new CoreException(Status.CANCEL_STATUS);
		} finally {

			System.setOut(realSysout);
			consoleOutputStream.println();

		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		for (SerializeTask task : tasks) {

			try {

				if (task.getType() == TaskType.REMOVE_MARKERS) {
					new ProblemMarkerAdapter(task.getModelFile()).clearMarkers();
				} else if (task.getType() == TaskType.REMOVE_ORPHAN) {
					removeOrphanData(task.getModelFile());
				} else {
					ProjectPropertiesAdapter propertiesAdapter = new ProjectPropertiesAdapter(task.getModelFile().getProject());

					IFolder targetFolder = task.getModelFile().getProject().getFolder(propertiesAdapter.getTargetFolder());

					removeOrphanData(task.getModelFile());

					if (task.getType() == TaskType.CREATE || task.getType() == TaskType.UPDATE) {
						serialize(task.getModelFile(), targetFolder);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Status.OK_STATUS;
	}

}
