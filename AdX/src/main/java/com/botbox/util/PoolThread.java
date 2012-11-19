/*
 * @(#)PoolThread.java	Created date: 98-12-18
 * $Revision: 3765 $, $Date: 2008-02-24 11:03:02 -0600 (Sun, 24 Feb 2008) $
 *
 * Copyright (c) 2000 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 */

package com.botbox.util;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 3765 $, $Date: 2008-02-24 11:03:02 -0600 (Sun, 24 Feb
 *          2008) $
 */
public final class PoolThread extends Thread implements JobStatus {

	static final Logger log = Logger.getLogger(PoolThread.class.getName());

	private static final boolean VERBOSE_DEBUG = false;

	// 0 => not initialized,
	// 1 => not initialized (start idle),
	// 2 => waiting,
	// 3 => invoking,
	// 4 => dead
	private int status = 0;
	private ThreadPool pool;

	private Runnable nextJob = null;

	private String description = null;
	private Runnable runningJob = null;
	private long startTime;

	/** Information for the ThreadPool */
	private int threadIndex = -1;
	private int idleIndex = -1;

	private int activeCount = 0;

	PoolThread(ThreadPool pool, String name, boolean isIdle) {
		super(name);
		this.pool = pool;
		if (isIdle) {
			status = 1;
		}
		setDaemon(pool.isDaemon());
		start();
	}

	// -------------------------------------------------------------------
	// Information for the thread pool
	// -------------------------------------------------------------------

	final int getThreadIndex() {
		return threadIndex;
	}

	final void setThreadIndex(int threadIndex) {
		this.threadIndex = threadIndex;
	}

	final int getIdleIndex() {
		return idleIndex;
	}

	final void setIdleIndex(int idleIndex) {
		this.idleIndex = idleIndex;
	}

	final boolean isWorking() {
		return status == 3;
	}

	final int addActive(int value) {
		return this.activeCount += value;
	}

	final String getStatus() {
		return getStatus(new StringBuffer()).toString();
	}

	final StringBuffer getStatus(StringBuffer sb) {
		int status = this.status;
		sb.append(getName()).append('[');
		if (status < 2) {
			sb.append("initializing");
		} else if (status == 2) {
			sb.append("waiting");
		} else if (status == 4) {
			sb.append("died");
		} else {
			long startTime = this.startTime;
			String description = this.description;
			Runnable job = this.runningJob;
			sb.append("invoked ");
			if (description != null) {
				sb.append(description).append(" (").append(job).append(')');
			} else {
				sb.append(job);
			}
			if (startTime > 0) {
				sb.append(" at ").append(new Date(startTime));
				// Also show milliseconds
				sb.append(',').append(startTime % 1000);
			}
		}
		if (idleIndex >= 0) {
			sb.append(",idle");
		}
		sb.append(",active=").append(activeCount);
		return sb.append(']');
	}

	// -------------------------------------------------------------------
	// JobStatus
	// -------------------------------------------------------------------

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void stillAlive() {
		activeCount = 0;
	}

	// -------------------------------------------------------------------
	// Job handling
	// -------------------------------------------------------------------

	void invoke(Runnable job, String description) {
		this.description = description;
		invoke(job);
	}

	synchronized void invoke(Runnable job) {
		this.nextJob = job;
		this.startTime = System.currentTimeMillis();
		notify();
	}

	private synchronized Runnable getJob() {
		Runnable newJob;
		while ((newJob = this.nextJob) == null) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		this.nextJob = null;
		this.runningJob = newJob;
		return newJob;
	}

	// This should not need to be synchronized because it can only
	// happen after the thread has finished a job and before it has
	// become available for the thread pool.
	private void clearJob() {
		this.runningJob = null;
		this.description = null;
		this.activeCount = 0;
	}

	public void run() {
		try {
			if (status == 1 && !pool.addThread(this)) {
				// No more need for this thread because the pool is already
				// full of idle threads

			} else {
				status = 2;
				handleJobs();
			}

		} finally {
			status = 4;
			pool.threadDied(this);
		}
	}

	private void handleJobs() {
		int priority = getPriority();

		do {
			Runnable myJob = getJob();

			if (myJob != null) {
				try {
					status = 3;
					activeCount = 0;
					if (VERBOSE_DEBUG) {
						log.info(getName() + " START "
								+ (description != null ? description : "")
								+ ' ' + myJob);
					}
					myJob.run();
					if (VERBOSE_DEBUG) {
						log.info(getName() + " EXIT "
								+ (description != null ? description : "")
								+ ' ' + myJob);
					}

				} catch (ThreadDeath e) {
					log.log(Level.SEVERE, "thread was killed", e);
					// Rethrow thread death
					throw e;
				} catch (Throwable e) {
					log
							.log(Level.SEVERE, "could not execute job "
									+ (description != null ? description : "")
									+ ":", e);
				} finally {
					status = 2;
					clearJob();
				}

				// Restore the thread priority if it has changed
				// Should this be done this way?
				if (getPriority() != priority) {
					setPriority(priority);
				}

				// Clear the interrupted flag in case its been set
				if (interrupted()) {
					// Should we have debug output here???
					log.log(Level.SEVERE, "***interrupted");
				}

				long currentTime = System.currentTimeMillis();
				pool.checkWorkingThreads(currentTime);

				// \TODO Add statistics about the job!!!
			}

		} while (pool.addThread(this));
	}

}
