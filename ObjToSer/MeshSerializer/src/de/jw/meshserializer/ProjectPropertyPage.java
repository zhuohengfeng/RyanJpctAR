package de.jw.meshserializer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private ProjectPropertiesAdapter propertiesAdapter;
	
	private Button enableSerializer;
	private Text sourceFolder;
	private Text targetFolder;

	private IProject getProject() {
		return ((IJavaProject) getElement()).getProject();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		propertiesAdapter = new ProjectPropertiesAdapter(getProject());
		
		Composite comp = new Composite(parent, 0);
		
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		
		new Label(comp, 0).setText("Enable serializer");
		enableSerializer = new Button(comp, SWT.CHECK);
		new Label(comp, 0).setText("Source folder");
		sourceFolder = new Text(comp, SWT.BORDER | SWT.SINGLE);
		sourceFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(comp, 0).setText("Target folder");
		targetFolder = new Text(comp, SWT.BORDER | SWT.SINGLE);
		targetFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			enableSerializer.setSelection( propertiesAdapter.isSerializerEnabled() );
			sourceFolder.setText(propertiesAdapter.getSourceFolder());
			targetFolder.setText(propertiesAdapter.getTargetFolder());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return comp;
	}

	@Override
	public boolean performOk() {
		
		if (enableSerializer.getSelection() && !getProject().getFolder(sourceFolder.getText()).exists()) {
			new MessageDialog(getShell(), "Error", null, "The source folder does not exist in your project.", MessageDialog.ERROR, new String[]{"OK"}, 0).open();
			return false;
		}
		
		if (enableSerializer.getSelection() && !getProject().getFolder(targetFolder.getText()).exists()) {
			int result = new MessageDialog(getShell(), "Confirmation", null, "The target folder does not exist in you project. Create it now?", MessageDialog.QUESTION, new String[]{"Yes", "Cancel"}, 0).open();
			
			if (result == 0) {
				try {
					createFolderWithParents(getProject().getFolder(targetFolder.getText()));
				} catch (CoreException e) {
					new MessageDialog(getShell(), "Error", null, "There was an error while trying to create the folder. ("+ e.getMessage() + ")", MessageDialog.ERROR, new String[]{"OK"}, 0).open();
					return false;
				}
			} else {
				return false;
			}
		}
		
		String oldSourceFolder = null;
		try {
			oldSourceFolder = propertiesAdapter.getSourceFolder();
			propertiesAdapter.setSerializerEnabled(enableSerializer.getSelection());
			propertiesAdapter.setSourceFolder(sourceFolder.getText());
			propertiesAdapter.setTargetFolder(targetFolder.getText());
		} catch (CoreException e1) {
			e1.printStackTrace();
			return false;
		}

		SerializerJob serializerJob = new SerializerJob();
		try {
			if (getProject().getFolder(oldSourceFolder).exists()) {
				serializerJob.generateCleanupTasks(getProject().getFolder(oldSourceFolder));
			}
		} catch (Exception e) {
			//propably an error with the old source folder. Path is most likely not valid and so we're just ignoring it.
		}
		try {
			if (enableSerializer.getSelection()) {
				serializerJob.generateInitTasks(getProject().getFolder(propertiesAdapter.getSourceFolder()));
			}
		} catch (CoreException e) {
			new MessageDialog(getShell(), "Error", null, "There was an error while trying to set up the serializer. ("+ e.getMessage() + ")", MessageDialog.ERROR, new String[]{"OK"}, 0).open();
			return false;
		}
		serializerJob.schedule();
		return true;
	}

	private void createFolderWithParents(IFolder folder) throws CoreException {
		if (!folder.getParent().exists() && folder.getParent() instanceof IFolder) {
			createFolderWithParents((IFolder) folder.getParent());
		}
		folder.create(false, true, null);
	}
	
	
}
