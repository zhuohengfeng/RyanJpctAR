package de.jw.meshserializer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * This class sets and clears problem markers for a specific file in the workspace.
 */
public class ProblemMarkerAdapter {
	
	private IFile file;
	
	private static String MODEL_ERROR_MARKER_ID = "de.jw.meshserializer.modelfilemarker";
	
	private IMarker[] markers() throws CoreException {
		return file.findMarkers(ProblemMarkerAdapter.MODEL_ERROR_MARKER_ID, false, IResource.DEPTH_ZERO);
	}
	
	public ProblemMarkerAdapter(IFile file) {
		this.file = file;
	}
	
	public boolean hasMarker() throws CoreException {
		if (!file.exists()) {
			return false;
		}
		return markers().length > 0;
	}

	public void clearMarkers() throws CoreException {
		if (!file.exists()) {
			return;
		}
		for (IMarker marker : markers()) {
			marker.delete();
		}
	}
	
	public void addError(String message) throws CoreException {
		if (!file.exists()) {
			return;
		}
		if (!hasMarker()) {
			IMarker marker = file.createMarker(ProblemMarkerAdapter.MODEL_ERROR_MARKER_ID);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.MESSAGE, message);
		}
	}
	
	public void addWarning(String message) throws CoreException {
		if (!file.exists()) {
			return;
		}
		if (!hasMarker()) {
			IMarker marker = file.createMarker(ProblemMarkerAdapter.MODEL_ERROR_MARKER_ID);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.MESSAGE, message);
		}
	}

}
