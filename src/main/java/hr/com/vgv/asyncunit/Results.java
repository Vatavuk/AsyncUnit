package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread execution results. It can receive execution results from different threads and wait until all
 * executions are received or until timeout expires.
 *
 * @author Vedran Vatavuk
 */
public class Results
{
    private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean waiting = new AtomicBoolean(false);

    private final Semaphore semaphore = new Semaphore(0);

    /**
     * Signal successful execution of a thread.
     */
    public void addSuccess()
    {
        semaphore.release();
    }

    /**
     * Add throwable that caused a thread to fail.
     */
    public void addFailure(Throwable throwable)
    {
        errors.add(throwable);
        semaphore.release();
    }

    /**
     * Waits for a single thread execution result. It will wait until interrupted.
     *
     * @throws InterruptedException If interrupted
     */
    public void await() throws InterruptedException
    {
        await(0);
    }

    /**
     * Waits for a single thread execution result. If no result received until given period, it will raise
     * AssertionError.
     *
     * @param period Time period in milliseconds
     * @throws InterruptedException If interrupted
     */
    public void await(long period) throws InterruptedException
    {
        await(period, TimeUnit.MILLISECONDS, 1);
    }

    /**
     * Waits for a given number of thread execution results. If a number of received results is not equal to
     * numOfExecutions, it will raise AssertionError.
     *
     * @param period Time period in milliseconds
     * @throws InterruptedException If interrupted
     */
    public void await(long period, int numOfExecutions) throws InterruptedException
    {
        await(period, TimeUnit.MILLISECONDS, numOfExecutions);
    }

    /**
     * Waits for a given number of thread execution results. If a number of received results is not equal to
     * numOfExecutions, it will raise AssertionError.
     *
     * @param period Time period
     * @param timeUnit Time unit
     * @param numOfExecutions Number of expected thread executions
     * @throws InterruptedException If interrupted
     */
    public void await(long period, TimeUnit timeUnit, int numOfExecutions) throws InterruptedException
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
                semaphore.acquire(numOfExecutions);
            }
            else if (!semaphore.tryAcquire(numOfExecutions, period, timeUnit))
            {
                throw new AssertionError(notEnoughExecutions(numOfExecutions));
            }
        }
        finally
        {
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
            "Number of flow executions was %d instead of %d", expected - semaphore.getQueueLength(), expected
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
