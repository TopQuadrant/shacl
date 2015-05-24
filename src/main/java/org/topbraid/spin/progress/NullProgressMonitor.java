package org.topbraid.spin.progress;



/**
 * A ProgressMonitor that doesn't "do" anything.
 * Support for canceling is provided via <code>setCanceled</code>.
 * 
 * @author Holger Knublauch
 */
public class NullProgressMonitor implements ProgressMonitor {
	
	private boolean canceled;

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void beginTask(String label, int totalWork) {
	}

	@Override
	public void done() {
	}

	@Override
	public void setCanceled(boolean value) {
		this.canceled = value;
	}

	@Override
	public void setTaskName(String value) {
	}

	@Override
	public void subTask(String label) {
	}

	@Override
	public void worked(int amount) {
	}
}
