import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests that the {@link ThreadSafeIndexedSet} is properly protected.
 */
@TestMethodOrder(OrderAnnotation.class)
public class ThreadSafeIndexedSetTest {

	/** Default timeout for each test. */
	public static final Duration TIMEOUT = Duration.ofSeconds(30);

	/**
	 * Tests that {@link ThreadSafeIndexedSet} is not using {@code synchronized}
	 * methods. This test will not detect if {@code synchronized} is used WITHIN
	 * a method, but it should not!
	 */
	@Order(1)
	@Test
	public void testSynchronized() {
		Method[] methods = ThreadSafeIndexedSet.class.getMethods();
		String debug = "%n%s() method should NOT be synchronized!%n";

		for (Method method : methods) {
			Assertions.assertFalse(
					Modifier.isSynchronized(method.getModifiers()),
					() -> String.format(debug, method.getName())
			);
		}
	}

	/**
	 * Tests that all of the required methods are overridden. This test will not
	 * detect whether the methods were overridden correctly however!
	 */
	@Order(2)
	@Test
	public void testOverridden() {
		Set<String> expected = Arrays.stream(IndexedSet.class.getDeclaredMethods())
			.map(method -> methodName(method))
			.collect(Collectors.toSet());

		Set<String> actual = Arrays.stream(ThreadSafeIndexedSet.class.getDeclaredMethods())
			.map(method -> methodName(method))
			.collect(Collectors.toSet());

		String debug = "%nThe following methods were not properly overridden: %s%n";

		// remove any method from actual that was in expected
		// anything leftover in expected was not overridden
		expected.removeAll(actual);

		Assertions.assertTrue(
				expected.isEmpty(),
				() -> String.format(debug, expected.toString())
		);
	}

	/**
	 * Tests that {@link ThreadSafeIndexedSet} works as expected with multiple
	 * threads adding at the same time. Runs several times to make sure the
	 * results are consistent.
	 *
	 * @see AddWorker
	 * @throws InterruptedException
	 */
	@Order(3)
	@RepeatedTest(3)
	public void testAddOnly() throws InterruptedException {
		int num = 1000;
		int threads = 3;
		int chunk = num / threads;
		int last = threads - 1;

		TreeSet<Integer> expected = generate(0, num, TreeSet::new);
		ThreadSafeIndexedSet<Integer> actual = new ThreadSafeIndexedSet<>();
		ArrayList<Thread> workers = new ArrayList<>();

		for (int i = 0; i < last; i++) {
			workers.add(new AddWorker(actual, i * chunk, i * chunk + chunk));
		}

		workers.add(new AddWorker(actual, last * chunk, num));

		assertConcurrent(workers, expected, actual);
	}

	/**
	 * Tests that {@link ThreadSafeIndexedSet} works as expected with multiple
	 * threads writing at the same time. Runs several times to make sure the
	 * results are consistent.
	 *
	 * @see AddAllWorker
	 * @throws InterruptedException
	 */
	@Order(4)
	@RepeatedTest(3)
	public void testAddAllOnly() throws InterruptedException {
		int num = 1000;
		int threads = 3;
		int chunk = num / threads;
		int last = threads - 1;

		TreeSet<Integer> expected = generate(0, num, TreeSet::new);
		ThreadSafeIndexedSet<Integer> actual = new ThreadSafeIndexedSet<>();
		ArrayList<Thread> workers = new ArrayList<>();

		for (int i = 0; i < last; i++) {
			workers.add(new AddAllWorker(actual, i * chunk, i * chunk + chunk));
		}

		workers.add(new AddAllWorker(actual, last * chunk, num));

		assertConcurrent(workers, expected, actual);
	}

	/**
	 * Tests that {@link ThreadSafeIndexedSet} works as expected with multiple
	 * threads reading and writing at the same time. Runs several times to make sure
	 * the results are consistent.
	 *
	 * @see AddWorker
	 * @see GetWorker
	 * @throws InterruptedException
	 */
	@Order(5)
	@RepeatedTest(3)
	public void testOneAddManyGets() throws InterruptedException {
		int num = 1000;
		int threads = 3;
		int chunk = num / threads;
		int last = threads - 1;

		TreeSet<Integer> expected = generate(0, num, TreeSet::new);
		ThreadSafeIndexedSet<Integer> actual = new ThreadSafeIndexedSet<>();
		ArrayList<Thread> workers = new ArrayList<>();

		workers.add(new AddWorker(actual, 0, num));
		workers.get(0).setPriority(Thread.MAX_PRIORITY);

		for (int i = 0; i < last; i++) {
			workers.add(new GetWorker(actual, chunk));
		}

		assertConcurrent(workers, expected, actual);
	}

	/**
	 * Tests that {@link ThreadSafeIndexedSet} works as expected with multiple
	 * threads reading and writing at the same time. Runs several times to make sure
	 * the results are consistent.
	 *
	 * @see AddWorker
	 * @see GetWorker
	 * @throws InterruptedException
	 */
	@Order(6)
	@RepeatedTest(3)
	public void testManyAddManyGets() throws InterruptedException {
		int num = 1000;
		int threads = 3;
		int chunk = num / threads;
		int last = threads - 1;

		TreeSet<Integer> expected = generate(0, num, TreeSet::new);
		ThreadSafeIndexedSet<Integer> actual = new ThreadSafeIndexedSet<>();
		ArrayList<Thread> workers = new ArrayList<>();

		for (int i = 0; i < last; i++) {
			workers.add(new AddWorker(actual, i * chunk, i * chunk + chunk));
			workers.add(new GetWorker(actual, chunk));
		}

		workers.add(new AddWorker(actual, last * chunk, num));
		workers.add(new GetWorker(actual, chunk));

		assertConcurrent(workers, expected, actual);
	}

	/**
	 * Tests that {@link ThreadSafeIndexedSet} works as expected with multiple
	 * threads reading and writing at the same time. Runs several times to make sure
	 * the results are consistent.
	 *
	 * @see AddWorker
	 * @see GetWorker
	 * @throws InterruptedException
	 */
	@Order(7)
	@RepeatedTest(3)
	public void testManyWorkers() throws InterruptedException {
		int num = 1000;

		TreeSet<Integer> expected = generate(0, num, TreeSet::new);
		ThreadSafeIndexedSet<Integer> actual = new ThreadSafeIndexedSet<>();
		ArrayList<Thread> workers = new ArrayList<>();

		workers.add(new AddWorker(actual, 0, num / 2));
		workers.add(new CopyWorker(actual));
		workers.add(new AddAllWorker(actual, num / 2, num));
		workers.add(new GetWorker(actual, num));

		assertConcurrent(workers, expected, actual);
	}

	/** Forces several write operations. */
	public static class AddWorker extends Thread {

		/** Actual results. */
		private ThreadSafeIndexedSet<Integer> actual;

		/** Starting value (inclusive). */
		private int start;

		/** Ending value (exclusive). */
		private int end;

		/**
		 * Initializes this worker.
		 *
		 * @param actual actual results
		 * @param start starting value (inclusive)
		 * @param end ending value (exclusive)
		 */
		public AddWorker(ThreadSafeIndexedSet<Integer> actual, int start, int end) {
			this.actual = actual;
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			for (int i = start; i < end; i++) {
				actual.add(Integer.valueOf(i));
			}
		}
	}

	/** Forces a single write operation. */
	public static class AddAllWorker extends Thread {

		/** Actual results. */
		private ThreadSafeIndexedSet<Integer> actual;

		/** Starting value (inclusive). */
		private int start;

		/** Ending value (exclusive). */
		private int end;

		/**
		 * Initializes this worker.
		 *
		 * @param actual actual results
		 * @param start starting value (inclusive)
		 * @param end ending value (exclusive)
		 */
		public AddAllWorker(ThreadSafeIndexedSet<Integer> actual, int start, int end) {
			this.actual = actual;
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			ArrayList<Integer> local = generate(start, end, ArrayList::new);
			Collections.shuffle(local);
			actual.addAll(local);
		}
	}

	/** Forces several read operations. */
	public static class GetWorker extends Thread {

		/** Actual results. */
		private final ThreadSafeIndexedSet<Integer> actual;

		/** The number of times to loop. */
		private final int loops;

		/** Placeholder for the result. */
		private Integer result;

		/**
		 * Initializes this worker.
		 *
		 * @param actual actual results
		 * @param loops the number of times to loop
		 */
		public GetWorker(ThreadSafeIndexedSet<Integer> actual, int loops) {
			this.actual = actual;
			this.loops = loops;
			this.result = Integer.valueOf(0);
		}

		@Override
		public void run() {
			for (int i = 0; i < loops; i++) {
				int index = actual.size() - 1; // warning: size could be changing!
				Integer last = actual.get(index);
				result = result.compareTo(last) > 0 ? result : last;
			}
		}
	}

	/** Forces several read operations. */
	public static class CopyWorker extends Thread {

		/** Source set. */
		private final ThreadSafeIndexedSet<Integer> source;

		/** Actual results. */
		public static IndexedSet<Integer> actual;

		/**
		 * Initializes this worker.
		 *
		 * @param source source values
		 */
		public CopyWorker(ThreadSafeIndexedSet<Integer> source) {
			this.source = source;
		}

		@Override
		public void run() {
			actual = source.unsortedCopy();
		}
	}

	/**
	 * Convenience method to generate collections with {@link Integer} objects from
	 * {@code start} (inclusive) to {@code end} (exclusive).
	 *
	 * @param <T> the type of collection to generate
	 * @param start the starting value (inclusive)
	 * @param end the ending value (exclusive)
	 * @param supplier the collection supplier, e.g. {@code TreeSet::new}
	 *
	 * @return a collection with the correct type and values
	 */
	public static <T extends Collection<Integer>> T generate(int start, int end, Supplier<T> supplier) {
		return IntStream.range(start, end).boxed().collect(Collectors.toCollection(supplier));
	}

	/**
	 * Returns a method name and its parameters without the enclosing class.
	 *
	 * @param method the method to get the name
	 * @return the name and parameters without the enclosing class
	 */
	public static String methodName(Method method) {
		String parameters = Arrays.stream(method.getParameters())
				.map(p -> p.getType().getSimpleName())
				.collect(Collectors.joining(", "));

		return String.format("%s(%s)", method.getName(), parameters);
	}


	/**
	 * Tests that threads run concurrently without throwing exceptions within the
	 * {@link #TIMEOUT}, and produce correct results.
	 *
	 * @param workers the threads to run
	 * @param expected the expected output
	 * @param actual  the actual output
	 */
	public static void assertConcurrent(Collection<Thread> workers, Collection<Integer> expected, ThreadSafeIndexedSet<Integer> actual) {
		Assertions.assertTimeoutPreemptively(TIMEOUT,
				() -> {
					// use built-in multithreading classes for testing
					ExecutorService pool = Executors.newFixedThreadPool(workers.size());
					List<Future<?>> results = new ArrayList<>();

					// run a bunch of threads simultaneously
					for (Runnable worker : workers) {
						results.add(pool.submit(worker));
					}

					// similar to a join call, except will cause exceptions to be thrown
					try {
						for (Future<?> future : results) {
							future.get();
						}
					}
					catch (Exception e) {
						String debug = "%nUnexpected exception. Check for unprotected data access.%n";
						Assertions.fail(String.format(debug), e);
					}

					// gracefully shutdown thread pool
					pool.shutdown();
					pool.awaitTermination(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
				},
				() -> String.format("%nTest timed out. Check for deadlock!%n")
		);

		Assertions.assertEquals(
				expected.size(), actual.size(),
				"\nUnexpected number of elements; writes may be getting lost.\n");

		Assertions.assertTrue(
				expected.toString().contentEquals(actual.sortedCopy().toString()),
				"\nUnexpected content.\n");
	}

}
