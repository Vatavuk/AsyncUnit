package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Results
{
    private final Mutex mutex = new Mutex();

    private final AtomicInteger results = new AtomicInteger(0);

    private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean waiting = new AtomicBoolean(false);

    public Results()
    {
        mutex.lock();
    }

    public void addSuccess()
    {
        if (results.incrementAndGet() == 0)
        {
            mutex.unlock();
        }
    }

    public void addFailure(Throwable throwable)
    {
        errors.add(throwable);
        if (results.incrementAndGet() == 0)
        {
            mutex.unlock();
        }
    }

    public void await() throws InterruptedException
    {

    }

    public void await(long period, TimeUnit timeUnit, int repetitions) throws InterruptedException
    {
        if (waiting.get())
        {
            throw new IllegalStateException("Cannot wait for results, some other thread is already awaiting.");
        }
        try
        {
            waiting.set(true);
            int numberOfResults = 0;
            if (numberOfResults < repetitions)
            {
                if (period == 0)
                {
                    mutex.tryLock();
                }
                if (!mutex.tryLock(period, timeUnit))
                {
                    throw new AssertionError(notEnoughExecutions(repetitions, numberOfResults));
                }
            }
        }
        finally
        {
            mutex.lock();
            waiting.set(false);
            results.set(0);
            throwOnError();
        }
    }

    private String notEnoughExecutions(int repetitions, int numberOfResults)
    {
        return String.format(
            "Expected number of flow executions was %d instead of %d", numberOfResults, repetitions
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

    private <T extends Throwable> T sneakyThrow(T throwable)
    {
        return throwable;
    }
}
