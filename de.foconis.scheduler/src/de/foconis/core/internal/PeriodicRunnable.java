/*
 * © Copyright Foconis AG, 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package de.foconis.core.internal;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public abstract class PeriodicRunnable implements Runnable {
	private long nextRun = 0;
	private long interval;

	private Object lock = new Object();
	private AtomicBoolean isIdle = new AtomicBoolean(true);

	protected AtomicBoolean running = new AtomicBoolean(true);
	protected AtomicBoolean isTerminated = new AtomicBoolean(false);

	private Thread runner;
	private String name = "- not started -";

	/**
	 * @param interval
	 */
	public PeriodicRunnable(final long interval) {
		super();
		this.interval = interval;
	}

	/**
	 * wake up the thread, so that lock.wait returns immediately
	 */
	public void awake() {
		synchronized (lock) {
			nextRun = 0;
			lock.notify();
		}

	}

	/**
	 * Starts the job in a new thread
	 * 
	 * @param name
	 */
	public void start(final String name) {
		if (runner != null) {
			throw new IllegalStateException("Cannot start job again after it was stopped");
		}
		this.name = name;
		runner = new Thread(this, name);
		runner.start();
	}

	/**
	 * This method interrupts the current job
	 * 
	 * @return true if the job/thread terminated. false else.
	 */
	public boolean stop(long timeout, final boolean force) {
		running.set(false);
		// wake up the thread and check again
		awake();

		if (isTerminated.get()) {
			return true;
		}

		// ok, we know that it is time to quit
		timeout += System.currentTimeMillis();
		while (System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			if (isTerminated.get()) {
				return true;
			}
		}

		if (force && runner != null) {
			System.out.println(getName() + " won't stop. Trying to interrupt thread.");
			for (int i = 0; i < 100; i++) {
				runner.interrupt();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				if (isTerminated.get()) {
					System.out.println("Puh!, could interrupt the thread");
					return true;
				}
			}
			System.out.println("NOW we have a problem");
			return false;
		} else {
			return false;
		}

	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		// TODO Auto-generated method stub
		try {
			while (isRunning()) {
				long current = System.currentTimeMillis();
				long timeOut = 0;
				synchronized (this) {
					timeOut = nextRun - current;

				}
				// System.out.println("Periodic job: Timeout: " + timeOut);
				if (timeOut > 0) {
					try {
						synchronized (lock) {
							lock.wait(timeOut);
						}
					} catch (InterruptedException e) {

					}
				} else {
					try {
						isIdle.set(false);
						runCode();
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						isIdle.set(true);
						synchronized (this) {
							nextRun = System.currentTimeMillis() + interval;
						}
					}
				}
			}
		} finally {
			isTerminated.set(true);
		}
	}

	/**
	 * 
	 */
	protected abstract void runCode();

	/**
	 * @return
	 */
	public String getStatus() {
		if (isIdle.get()) {
			return "IDLE";
		} else {
			return "RUNNING";
		}
	}
}
