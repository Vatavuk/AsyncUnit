package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Results
{
    private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean waiting = new AtomicBoolean(false);

    private final Semaphore semaphore = new Semaphore(0);

    public void addSuccess()
    {
        semaphore.release();
    }

    public void addFailure(Throwable throwable)
    {
        errors.add(throwable);
        semaphore.release();
    }

    public void await() throws InterruptedException
    {
        await(0);
    }

    public void await(long period) throws InterruptedException
    {
        await(period, TimeUnit.MILLISECONDS, 1);
    }

    public void await(long period, int numOfExecutions) throws InterruptedException
    {
        await(period, TimeUnit.MILLISECONDS, numOfExecutions);
    }

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

    private String notEnoughExecutions(int expected)
    {
        return String.format(
            "Number of flow executions was %d instead of %d", expected - semaphore.getQueueLength(), expected
        );
    }

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
