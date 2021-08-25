package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

public class AsyncFlowTest
{

    @Test
    public void supportsAssertionsInSingleThread() throws Throwable
    {
        AtomicBoolean flag = new AtomicBoolean(false);

        new Thread(
            AsyncFlow.prepare(() -> {
                sleepn();
                Assert.assertTrue(true);
                flag.set(true);
            })
        ).start();

        AsyncFlow.await();
        Assert.assertTrue(flag.get());
    }

    @Test
    public void supportsAssertionsInMultipleThreads() throws Throwable
    {
        Queue<Long> queue = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < 4; i++)
        {
            new Thread(
                AsyncFlow.prepare(() -> {
                    Assert.assertTrue(true);
                    sleepn();
                    queue.add(Thread.currentThread().getId());
                })
            ).start();
        }

        AsyncFlow.await();
        Assert.assertEquals(4, queue.size());
    }

    @Test(expected = AssertionError.class)
    public void failsOnAssertionErrorInThread() throws Throwable
    {
        new Thread(
            AsyncFlow.prepare((Runnable) Assert::fail)
        ).start();

        AsyncFlow.await();
    }

    @Test(expected = IllegalStateException.class)
    public void failsOnExceptionThrownInThread() throws Throwable
    {
        new Thread(
            (Runnable) AsyncFlow.prepare(() -> {
                throw new IllegalStateException("");
            })
        ).start();

        AsyncFlow.await();
    }

    @Test(expected = InterruptedException.class)
    public void failsOnThreadInterruption() throws Throwable
    {
        final Thread main = Thread.currentThread();
        new Thread(main::interrupt).start();

        AsyncFlow.await();
    }

    private void sleepn()
    {
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
