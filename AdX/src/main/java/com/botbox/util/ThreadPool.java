/*
 * @(#)ThreadPool.java	Created date: 98-12-18
 * $Revision: 4088 $, $Date: 2008-04-11 19:13:08 -0500 (Fri, 11 Apr 2008) $
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

import java.util.Hashtable;

/**
 * This class implements a thread pool for reusing threads. Since only a maximal
 * number of threads may exist in the pool it is important that the threads in
 * the pool is not absorbed by long duration tasks that drains the pool.
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 4088 $, $Date: 2008-04-11 19:13:08 -0500 (Fri, 11 Apr
 *          2008) $
 */
public final class ThreadPool {

	private static Hashtable poolTable = new Hashtable();

	// WILL (PROBABLY) BE REMOVED!!!
	public static java.util.Enumeration getThreadPools() {
		return poolTable.elements();
	}

	public static ThreadPool getThreadPool(String name) {
		synchronized (poolTable) {
			ThreadPool pool = (ThreadPool) poolTable.get(name);
			if (pool == null) {
				pool = new ThreadPool(name);
				poolTable.put(name, pool);
			}
			return pool;
		}
	}

	// WILL (PROBABLY) BE REMOVED!!!
	public static ThreadPool getDefaultThreadPool() {
		return getThreadPool("default");
	}

	public static JobStatus getJobStatus() {
		Thread thread = Thread.currentThread();
		return (thread instanceof PoolThread) ? (JobStatus) thread : null;
	}

	private String name;

	/** The idle (waiting) threads */
	private PoolThread[] idleThreads = null;
	private int idleThreadCount = 0;

	/** All living threads */
	private PoolThread[] poolThreads = null;
	private int threadCount;

	private int threadID = 0;

	private int minThreads = 1;
	private int maxThreads = 255;
	private int maxIdleThreads = 100;

	private boolean isDaemon = true;

	private ArrayQueue pendingJobs = null;
	private int pendingJobCount = 0;

	private int millisBetweenChecks;
	private int millisBeforeInterrupt = 0;
	private long nextWorkingThreadCheck;

	private Object lock = new Object();

	public ThreadPool(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getThreads() {
		return threadCount;
	}

	/**
	 * Returns the current number of idle threads
	 */
	public int getIdleThreads() {
		return idleThreadCount;
	}

	/**
	 * Returns the status of all existing pool threads or <CODE>null</CODE> if
	 * no pool threads exists.
	 */
	public String getThreadStatus() {
		return getThreadStatus(new StringBuffer()).toString();
	}

	public StringBuffer getThreadStatus(StringBuffer sb) {
		int count = threadCount;
		sb.append("ThreadPool ").append(getName()).append(" (threads=").append(
				count).append(" idle=").append(idleThreadCount).append(')');
		if (count > 0) {
			PoolThread[] threads = this.poolThreads;
			int index = 0;
			for (int i = 0; i < count; i++) {
				PoolThread pt = threads[i];
				if (pt != null) {
					sb.append('\n').append(++index).append(": ");
					pt.getStatus(sb);
				}
			}
		}
		return sb;
	}

	// -------------------------------------------------------------------
	// Thread pool configuration
	// -------------------------------------------------------------------

	public boolean isDaemon() {
		return isDaemon;
	}

	public void setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
	}

	public int getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(int minThreads) {
		this.minThreads = minThreads;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public int getMaxIdleThreads() {
		return maxIdleThreads;
	}

	public void setMaxIdleThreads(int maxIdleThreads) {
		this.maxIdleThreads = maxIdleThreads;
	}

	/**
	 * Sets the time after which a running job may be interrupted if it still
	 * has not finished. A time of 0 (default) means the jobs can run for
	 * unlimited time and will never be interrupted.
	 * 
	 * @param milliSeconds
	 *            the time after which a running job may be interrupted if it
	 *            still has not finished.
	 */
	public void setInterruptThreadsAfter(int milliSeconds) {
		if (milliSeconds > 0) {
			int timeBetweenChecks = milliSeconds / 3;
			this.millisBetweenChecks = timeBetweenChecks < 1000 ? 1000
					: timeBetweenChecks;
		}
		this.millisBeforeInterrupt = milliSeconds;
	}

	// -------------------------------------------------------------------
	// Thread job queue information
	// -------------------------------------------------------------------

	public int getQueueSize() {
		return pendingJobCount;
	}

	public long getQueueTime() {
		return 0L;
	}

	// -------------------------------------------------------------------
	// Job handling
	// -------------------------------------------------------------------

	/**
	 * Invokes the specified job as soon as possible. New threads are created if
	 * none is available in the thread pool.
	 * 
	 * @param job
	 *            the job to invoke
	 */
	public void invokeLater(Runnable job) {
		invokeLater(job, null);
	}

	public void invokeLater(Runnable job, String description) {
		PoolThread thread;

		synchronized (lock) {
			if (idleThreadCount > 0) {
				thread = idleThreads[--idleThreadCount];
				thread.setIdleIndex(-1);
				idleThreads[idleThreadCount] = null;

			} else if (threadCount < maxThreads) {
				thread = createThread(false);

			} else {
				// All threads busy and no more threads allowed => add to job
				// queue
				if (pendingJobs == null) {
					pendingJobs = new ArrayQueue();
				}
				pendingJobs.add(job);
				pendingJobs.add(description);
				pendingJobCount++;
				thread = null;
			}
		}

		if (thread != null) {
			thread.invoke(job, description);
		} else {
			checkWorkingThreads(System.currentTimeMillis());
		}
	}

	// NOTE: MAY ONLY BE CALLED SYNCHRONIZED ON LOCK
	private PoolThread createThread(boolean isIdle) {
		PoolThread thread;

		if (poolThreads == null) {
			poolThreads = new PoolThread[10];
		} else if (poolThreads.length == threadCount) {
			poolThreads = (PoolThread[]) ArrayUtils.setSize(poolThreads,
					threadCount + 100);
		}

		String threadName = getName() + '.' + (++threadID);
		thread = poolThreads[threadCount] = new PoolThread(this, threadName,
				isIdle);
		thread.setThreadIndex(threadCount);
		threadCount++;

		return thread;
	}

	// -------------------------------------------------------------------
	// Interface to the PoolThreads
	// -------------------------------------------------------------------

	/**
	 * Adds a PoolThread object to the thread pool.
	 * 
	 * @param thread
	 *            the thread to add to the thread pool.
	 * @return true if the thread was added to the pool and false if the pool
	 *         was full and the thread should die
	 */
	final boolean addThread(PoolThread thread) {
		int idleIndex = thread.getIdleIndex();
		if (idleIndex >= 0) {
			return true;
		}

		Runnable job;
		String description;
		synchronized (lock) {
			if (pendingJobCount > 0) {
				pendingJobCount--;
				job = (Runnable) pendingJobs.remove(0);
				description = (String) pendingJobs.remove(0);

			} else if (idleThreadCount < maxIdleThreads) {
				if (idleThreads == null) {
					idleThreads = new PoolThread[maxIdleThreads];
				} else if (idleThreads.length <= idleThreadCount) {
					idleThreads = (PoolThread[]) ArrayUtils.setSize(
							idleThreads, maxIdleThreads);
				}
				idleThreads[idleThreadCount] = thread;
				thread.setIdleIndex(idleThreadCount);
				idleThreadCount++;
				return true;

			} else {
				return false;
			}
		}

		thread.invoke(job, description);
		return true;
	}

	final void threadDied(PoolThread thread) {
		synchronized (lock) {
			threadCount--;

			// Remove the thread if it is among the idle threads
			int idleIndex = thread.getIdleIndex();
			if (idleIndex >= 0 && idleIndex < idleThreadCount
					&& idleThreads[idleIndex] == thread) {
				idleThreadCount--;
				idleThreads[idleIndex] = idleThreads[idleThreadCount];
				idleThreads[idleThreadCount] = null;
				if (idleThreads[idleIndex] != null) {
					idleThreads[idleIndex].setIdleIndex(idleIndex);
				}
				thread.setIdleIndex(-1);
			}

			// Remove the thread from the existing pool threads
			int index = thread.getThreadIndex();
			if (index < threadCount && index >= 0
					&& poolThreads[index] == thread) {
				threadCount--;
				poolThreads[index] = poolThreads[threadCount];
				poolThreads[threadCount] = null;
				if (poolThreads[index] != null) {
					poolThreads[index].setThreadIndex(index);
				}
				thread.setThreadIndex(-1);
			}
		}
	}

	final void checkWorkingThreads(long currentTime) {
		if (millisBeforeInterrupt > 0 && currentTime > nextWorkingThreadCheck) {
			nextWorkingThreadCheck = currentTime + millisBetweenChecks;

			// Time to check all working threads
			synchronized (lock) {
				for (int i = 0; i < threadCount; i++) {
					PoolThread pt = poolThreads[i];
					if (pt.isWorking() && pt.addActive(1) > 3) {
						pt.log.warning("interrupting overdue job "
								+ pt.getStatus());
						pt.interrupt();
						// Do not interrupt immediately again at next check
						pt.stillAlive();
					}
				}
			}
		}
	}

}
