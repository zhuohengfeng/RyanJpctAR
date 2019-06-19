package de.jw.meshserializer;

import org.eclipse.core.resources.IFile;

public class SerializeTask {

	public enum TaskType {

		CREATE, UPDATE, DELETE, REMOVE_ORPHAN, REMOVE_MARKERS

	}

	public SerializeTask(TaskType type, IFile modelFile) {
		super();
		this.type = type;
		this.modelFile = modelFile;
	}

	private TaskType type;

	private IFile modelFile;

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public IFile getModelFile() {
		return modelFile;
	}

	public void setModelFile(IFile modelFile) {
		this.modelFile = modelFile;
	}
}
