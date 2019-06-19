package de.jw.meshserializer;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		
		ws.addResourceChangeListener(new MeshSerializerResourceListener());
	}

}
