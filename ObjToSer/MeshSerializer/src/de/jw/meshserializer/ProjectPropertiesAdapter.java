package de.jw.meshserializer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;

/**
 * This class provides methods to store properties relevant to the mesh
 * serializer in the persistent properties of a project.
 */
public class ProjectPropertiesAdapter {

	private static QualifiedName ENABLE_SERIALIZER = new QualifiedName(Constants.PLUGIN_ID, Constants.PLUGIN_ID + ".enable");
	private static QualifiedName SOURCE_FOLDER = new QualifiedName(Constants.PLUGIN_ID, Constants.PLUGIN_ID + ".sourcefolder");
	private static QualifiedName TARGET_FOLDER = new QualifiedName(Constants.PLUGIN_ID, Constants.PLUGIN_ID + ".targetfolder");

	private IProject project;

	private String getProperty(String key) throws CoreException {
		return getProject().getPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, Constants.PLUGIN_ID + "." + key));
	}

	private void setProperty(String key, String value) throws CoreException {
		getProject().setPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, Constants.PLUGIN_ID + "." + key), value);
	}

	public IProject getProject() {
		return project;
	}

	public ProjectPropertiesAdapter(IJavaProject javaProject) {
		this.project = javaProject.getProject();
	}

	public ProjectPropertiesAdapter(IProject project) {
		this.project = project;
	}

	public boolean isSerializerEnabled() throws CoreException {
		return "true".equals(project.getPersistentProperty(ENABLE_SERIALIZER));
	}

	public String getSourceFolder() throws CoreException {
		if (project.getPersistentProperty(SOURCE_FOLDER) != null) {
			return project.getPersistentProperty(SOURCE_FOLDER);
		} else {
			return "";
		}

	}

	public String getTargetFolder() throws CoreException {
		if (project.getPersistentProperty(TARGET_FOLDER) != null) {
			return project.getPersistentProperty(TARGET_FOLDER);
		} else {
			return "";
		}
	}

	public List<String> getFiles(IFile modelFile) throws CoreException {
		try {
			int elementCount = Integer.parseInt(getProperty(modelFile.getProjectRelativePath().toString() + ".element_count"));
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < elementCount; i++) {
				result.add(getProperty(modelFile.getProjectRelativePath().toString() + "." + i));
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public void setFiles(IFile modelFile, List<String> files) throws CoreException {
		// cleanup first
		try {
			String elementCount = getProperty(modelFile.getProjectRelativePath().toString() + ".element_count");
			if (elementCount != null) {
				for (int i = 0; i < Integer.parseInt(elementCount); i++) {
					setProperty(modelFile.getProjectRelativePath().toString() + "." + i, null);
				}
			}
			setProperty(modelFile.getProjectRelativePath().toString() + ".element_count", null);
		} catch (NumberFormatException e) {
			// Don't worry, be happy!
		}

		if (files != null) {
			setProperty(modelFile.getProjectRelativePath().toString() + ".element_count", new Integer(files.size()).toString());
			for (int i = 0; i < files.size(); i++) {
				setProperty(modelFile.getProjectRelativePath().toString() + "." + i, files.get(i));
			}
		}
	}

	public void setSerializerEnabled(boolean enabled) throws CoreException {
		project.setPersistentProperty(ENABLE_SERIALIZER, enabled ? "true" : "false");
	}

	public void setSourceFolder(String sourceFolder) throws CoreException {
		project.setPersistentProperty(SOURCE_FOLDER, sourceFolder);
	}

	public void setTargetFolder(String targetFolder) throws CoreException {
		project.setPersistentProperty(TARGET_FOLDER, targetFolder);
	}
}
