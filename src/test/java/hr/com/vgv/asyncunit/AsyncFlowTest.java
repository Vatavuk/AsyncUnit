package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

        AsyncFlow.await(1000, 4);
        Assertions.assertEquals(4, queue.size());
    }

    @Test
    public void supportsSupplierFlowAssertions() throws InterruptedException
    {
        final AtomicBoolean flag = new AtomicBoolean(false);

        final Consumer<AtomicBoolean> flow = AsyncFlow.prepare(e -> {
            Sleep.now();
            Assertions.assertTrue(true);
            e.set(true);
        });
        new Thread(
            () -> {
                Sleep.now();
                flow.accept(flag);
            }
        ).start();

        AsyncFlow.await();
        Assertions.assertTrue(flag.get());
    }

    @Test
    public void failsOnMissingThreadExecutions()
    {
        for (int i = 0; i < 2; i++)
        {
            new Thread(AsyncFlow.prepare((Runnable) Sleep::now)).start();
        }

        Assertions.assertThrows(
            AssertionError.class,
            () -> AsyncFlow.await(1000, 5),
            "Number of flow executions was 2 instead of 5"
        );
    }

    @Test
    public void failsOnAssertionsErrorInThread()
    {
        new Thread(
            AsyncFlow.prepare((Runnable) Assertions::fail)
        ).start();

        Assertions.assertThrows(AssertionError.class, AsyncFlow::await);
    }

    @Test
    public void failsOnExceptionThrownInThread()
    {
        new Thread(
            AsyncFlow.prepare(this::sneakyThrow)
        ).start();

        Assertions.assertThrows(IllegalStateException.class, AsyncFlow::await);
    }

    @Test
    public void failsOnThreadInterruption()
    {
        final Thread main = Thread.currentThread();
        new Thread(AsyncFlow.prepare(main::interrupt)).start();

        Assertions.assertThrows(InterruptedException.class, AsyncFlow::await);
    }

    private void sneakyThrow()
    {
        throw new IllegalStateException("");
    }
}
