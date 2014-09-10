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
package de.foconis.core.job;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a Job that runs inside a NSF and is definde in a NSFJobGroup
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public abstract class NSFJob {
	protected AtomicBoolean running = new AtomicBoolean(true);
	private static final Logger log_ = Logger.getLogger(NSFJob.class.getName());
	private static ThreadLocal<NSFJob> instance = new ThreadLocal<NSFJob>();

	/**
	 * Entry point. This method calls the {@link #runCode()} method after setting up everything Do not overwrite this method
	 */
	public final void run() throws InterruptedException {
		// we set the current instance
		instance.set(this);
		try {
			log_.info("Starting job: " + this);
			if (isRunning()) {
				runCode();
			}
			log_.info("Job finished: " + this);
		} catch (InterruptedException ie) {
			log_.log(Level.SEVERE, "Job was interrupted: " + this, ie);
			throw ie;
		} catch (Throwable t) {
			log_.log(Level.SEVERE, "Job terminated abnormally: " + this, t);
		} finally {
			instance.set(null);
		}
	}

	/**
	 * this method must be overwritten Important!!! You must check periodically this.isRunning() (or NSFJob.currentIsRunning())
	 */
	protected abstract void runCode() throws InterruptedException;

	/**
	 * Static helper to get anywhere in your code the current job
	 * 
	 * @return
	 */
	public static NSFJob currentJob() {
		return instance.get();
	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * Static helper to get anywhere in your code the current job running status
	 * 
	 * @return true if the currentJob is running (or if there is no current job that could be terminated)
	 */
	public static boolean currentIsRunning() {
		NSFJob current = currentJob();
		if (current == null) {
			return true; // if there is no current job, we must not quit any loops (hopefully currentJob will not set to null by accident)
		}
		return current.isRunning();
	}

	/**
	 * 
	 */
	public void stop() {
		running.set(false);
	}

}
