package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Execution flow results. Used to wait and obtain results from different threads.
 *
 * @author Vedran Vatavuk
 */
public interface Results
{
    /**
     * Signal successful execution.
     */
    void addSuccess();

    /**
     * Signal failed execution.
     */
    void addFailure(Throwable throwable);

    /**
     * Waits for a single execution result. It will wait until interrupted if no result received.
     *
     * @throws InterruptedException If interrupted
     */
    void await() throws InterruptedException;

    /**
     * Waits for a single execution result. Raises AssertionError after period expires if no result received.
     *
     * @param period Time period in milliseconds
     * @throws InterruptedException If interrupted
     */
    void await(long period) throws InterruptedException;

    /**
     * Waits for a single execution result. Raises AssertionError after period expires if no result received.
     *
     * @param period Time period
     * @param timeUnit Time unit
     * @throws InterruptedException If interrupted
     */
    void await(long period, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Waits until given number of execution results are obtained. Raises AssertionError after period expires if number of
     * received results is less than expected.
     *
     * @param period       Time period in milliseconds
     * @param numOfResults Number of expected results
     * @throws InterruptedException If interrupted
     */
    void await(long period, int numOfResults) throws InterruptedException;

    /**
     * Waits until given number of execution results are obtained. Raises AssertionError after period expires if number of
     * received results is less than expected.
     *
     * @param period       Time timeout
     * @param timeUnit     Time unit
     * @param numOfResults Number of expected thread executions
     * @throws InterruptedException If interrupted
     */
    void await(long period, TimeUnit timeUnit, int numOfResults) throws InterruptedException;


    /**
     * Execution flow results synced by semaphore. It can receive execution results from different threads and wait until all
     * executions are received or until timeout expires.
     *
     * @author Vedran Vatavuk
     */
    class Synced implements Results
    {

        private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

        private final AtomicBoolean waiting = new AtomicBoolean(false);

        private Semaphore semaphore = new Semaphore(0);

        @Override
        public void addSuccess()
        {
            semaphore.release();
        }

        @Override
        public void addFailure(Throwable throwable)
        {
            errors.add(throwable);
            semaphore.release();
        }

        @Override
        public void await() throws InterruptedException
        {
            await(0);
        }

        @Override
        public void await(long period) throws InterruptedException
        {
            await(period, TimeUnit.MILLISECONDS, 1);
        }

        @Override
        public void await(long period, TimeUnit timeUnit) throws InterruptedException
        {
            await(period, timeUnit, 1);
        }

        @Override
        public void await(long period, int numOfResults) throws InterruptedException
        {
            await(period, TimeUnit.MILLISECONDS, numOfResults);
        }

        @Override
        public void await(long period, TimeUnit timeUnit, int numOfResults) throws InterruptedException
        {
            synchronized (this)
            {
                if (waiting.get())
                {
                    IllegalStateException exception = new IllegalStateException(
                        "Cannot wait for results, some other thread is already awaiting.");
                    errors.add(exception);
                    return;
                }
                waiting.set(true);
            }
            try
            {
                if (period == 0)
                {
                    semaphore.acquire(numOfResults);
                }
                else if (!semaphore.tryAcquire(numOfResults, period, timeUnit))
                {
                    throw new AssertionError(notEnoughExecutions(numOfResults));
                }
            }
            finally
            {
                semaphore = new Semaphore(0);
                waiting.set(false);
                throwOnError();
            }
        }

        /**
         * Constructs error message.
         *
         * @param expected Expected number of executions
         * @return Error message
         */
        private String notEnoughExecutions(int expected)
        {
            return String.format(
                "Number of flow executions was %d instead of %d", semaphore.availablePermits(), expected
            );
        }

        /**
         * Throws throwable if any of threads ended with an exception.
         */
        private void throwOnError()
        {
            try
            {
                if (!errors.isEmpty())
                {
                    sneakyThrow(errors.peek());
                }
            }
            finally
            {
                errors.clear();
            }
        }

        @SuppressWarnings("unchecked")
        private static <E extends Throwable> void sneakyThrow(Throwable e) throws E
        {
            throw (E) e;
        }
    }
}
