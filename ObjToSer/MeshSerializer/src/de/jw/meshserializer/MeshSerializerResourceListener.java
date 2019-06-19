package de.jw.meshserializer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;

public class MeshSerializerResourceListener implements IResourceChangeListener {
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			if (event.getDelta() != null) {
				new SerializerJob().generateDeltaTasks(event.getDelta()).schedule();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}

}
