import org.junit.jupiter.api.Nested;

/**
 * Runs both the {@link SimpleReadWriteLockTest} unit tests and the
 * {@link ThreadSafeIndexedSetTest} unit tests.
 */
public class ReadWriteLockTest {

	/**
	 * Unit tests for the {@code SimpleReadWriteLock}.
	 * @see SimpleReadWriteLockTest
	 */
	@Nested
	public class NestedReadWriteLockTest extends SimpleReadWriteLockTest {

	}

	/**
	 * Unit tests for the {@code ThreadSafeIndexedSet}
	 * @see ThreadSafeIndexedSetTest
	 */
	@Nested
	public class NestedThreadSafeIndexedSetTest extends ThreadSafeIndexedSetTest {

	}

}
