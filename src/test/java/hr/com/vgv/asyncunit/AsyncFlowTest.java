package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsyncFlowTest
{
    @Test
    public void supportsAssertionsInSingleThread() throws Exception
    {
        AtomicBoolean flag = new AtomicBoolean(false);

        new Thread(
            AsyncFlow.prepare(() -> {
                Sleep.now();
                Assertions.assertTrue(true);
                flag.set(true);
            })
        ).start();

        AsyncFlow.await();
        Assertions.assertTrue(flag.get());
    }

    @Test
    public void supportsAssertionsInMultipleThreads() throws Exception
    {
        Queue<Long> queue = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < 4; i++)
        {
            new Thread(
                AsyncFlow.prepare(() -> {
                    Assertions.assertTrue(true);
                    Sleep.now();
                    queue.add(Thread.currentThread().getId());
                })
            ).start();
        }

        AsyncFlow.await();
        Assertions.assertEquals(4, queue.size());
    }

    @Test
    public void failsOnAssertionsionErrorInThread() throws Exception
    {
        new Thread(
            AsyncFlow.prepare((Runnable) Assertions::fail)
        ).start();

        Assertions.assertThrows(AssertionError.class, AsyncFlow::await);
    }

    @Test
    public void failsOnExceptionThrownInThread() throws Exception
    {
        new Thread(
            (Runnable) AsyncFlow.prepare(() -> {
                throw new IllegalStateException("");
            })
        ).start();

        Assertions.assertThrows(IllegalStateException.class, AsyncFlow::await);
    }

    @Test
    public void failsOnThreadInterruption() throws Exception
    {
        final Thread main = Thread.currentThread();
        new Thread(main::interrupt).start();

        Assertions.assertThrows(InterruptedException.class, AsyncFlow::await);
    }
}
