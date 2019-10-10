/**
 * A thread-safe version of {@link IndexedSet} using a read/write lock.
 *
 * @param <E> element type
 * @see IndexedSet
 * @see SimpleReadWriteLock
 */
public class ThreadSafeIndexedSet<E> extends IndexedSet<E> {

	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;

	/**
	 * Initializes an unsorted thread-safe indexed set.
	 */
	public ThreadSafeIndexedSet() {
		// NOTE: DO NOT MODIFY THIS METHOD
		this(false);
	}

	/**
	 * Initializes a thread-safe indexed set.
	 *
	 * @param sorted whether the set should be sorted
	 */
	public ThreadSafeIndexedSet(boolean sorted) {
		// NOTE: DO NOT MODIFY THIS METHOD
		super(sorted);
		lock = new SimpleReadWriteLock();
	}

	// TODO: OVERRIDE AND IMPLEMENT NECESSARY METHODS

}
