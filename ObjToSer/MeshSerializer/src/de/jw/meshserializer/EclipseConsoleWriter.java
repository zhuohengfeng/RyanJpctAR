package de.jw.meshserializer;

import java.io.OutputStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * This class allows easy access to the console plugin.
 */
public class EclipseConsoleWriter {

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	private MessageConsole console;
	private MessageConsoleStream consoleOutputStream;

	/**
	 * Creates a new instance of <code>EclipseConsoleWriter</code>. Calling this
	 * constructor will open a new console in the ConsoleView and activate this
	 * console. If the console view is not part of the current perspective, it
	 * wont open automatically.
	 * 
	 * @param name
	 *            the name of the console.
	 */
	public EclipseConsoleWriter(String name) {
		console = findConsole(name);
		consoleOutputStream = console.newMessageStream();
	}

	public void print(String msg) {
		consoleOutputStream.print(msg);
	}

	public void println(String msg) {
		consoleOutputStream.println(msg);
	}

	public OutputStream getMessageStream() {
		return consoleOutputStream;
	}

	public void clear() {
		console.clearConsole();
	}
}
