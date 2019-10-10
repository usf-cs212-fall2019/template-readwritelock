import java.util.ConcurrentModificationException;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive, but
 * also tracks which thread holds the lock. If unlock is called by any other
 * thread, a {@link ConcurrentModificationException} is thrown.
 *
 * @see SimpleLock
 * @see SimpleReadWriteLock
 */
public class SimpleReadWriteLock {

	/** The lock used for reading. */
	private final SimpleLock readerLock;

	/** The lock used for writing. */
	private final SimpleLock writerLock;

	/*
	 * TODO: ADD MEMBERS AS NECESSARY
	 * Need to track number of readers and writers.
	 * May need to create a lock object.
	 */

	/**
	 * Initializes a new simple read/write lock.
	 */
	public SimpleReadWriteLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();

		// TODO: FILL IN AS NECESSARY
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return writerLock;
	}

	/**
	 * Determines whether the thread running this code and the other thread are
	 * in fact the same thread.
	 *
	 * @param other the other thread to compare
	 * @return true if the thread running this code and the other thread are not
	 * null and have the same ID
	 *
	 * @see Thread#getId()
	 * @see Thread#currentThread()
	 */
	public static boolean sameThread(Thread other) {
		// NOTE: DO NOT MODIFY THIS METHOD
		return other != null && other.getId() == Thread.currentThread().getId();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {

		/**
		 * Will wait until there are no active writers in the system, and then will
		 * increase the number of active readers.
		 */
		@Override
		public void lock() {
			/*
			 * TODO: FILL IN THIS METHOD
			 * Use code from lecture slides as a start point.
			 */
		}

		/**
		 * Will decrease the number of active readers, and notify any waiting threads if
		 * necessary.
		 */
		@Override
		public void unlock() {
			// TODO: FILL IN THIS METHOD
		}

	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {

		/*
		 * TODO: ADD MEMBERS AS NECESSARY
		 * Need to track which thread holds the write lock
		 */

		/**
		 * Will wait until there are no active readers or writers in the system, and
		 * then will increase the number of active writers and update which thread
		 * holds the write lock.
		 */
		@Override
		public void lock() {
			/*
			 * TODO: FILL IN THIS METHOD
			 * 1) Wait until safe to write
			 * 2) Update number of active writers
			 * 3) Update which thread "holds" the write lock
			 */
		}

		/**
		 * Will decrease the number of active writers, and notify any waiting threads if
		 * necessary. If unlock is called by a thread that does not hold the lock, then
		 * a {@link ConcurrentModificationException} is thrown.
		 *
		 * @see #sameThread(Thread)
		 *
		 * @throws ConcurrentModificationException if unlock is called without previously
		 * calling lock or if unlock is called by a thread that does not hold the write lock
		 */
		@Override
		public void unlock() throws ConcurrentModificationException {
			/*
			 * TODO: FILL IN THIS METHOD
			 * 1) Throw a ConcurrentModificationException it the wrong thread is calling
			 * unlock() or if lock() has not yet been called. Use sameThread(...) for
			 * this part.
			 *
			 * 2) Update the number of active writers
			 *
			 * 3) Update which thread "holds" the write lock (in this case, no thread
			 * should hold the lock after unlock() finishes)
			 */
		}
	}
}
