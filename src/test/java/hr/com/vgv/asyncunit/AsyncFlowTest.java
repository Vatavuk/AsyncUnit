package hr.com.vgv.asyncunit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsyncFlowTest
{
    @Test
    public void supportsAssertionsInSingleThread() throws Exception
    {
        AtomicBoolean flag = new AtomicBoolean(false);

        new Thread(
            AsyncFlow.prepare(() -> {
                Sleep.now();
                assertTrue(true);
                flag.set(true);
            })
        ).start();

        AsyncFlow.await();
        assertTrue(flag.get());
    }

    @Test
    public void supportsAssertionsInMultipleThreads() throws Exception
    {
        Queue<Long> queue = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < 4; i++)
        {
            new Thread(
                AsyncFlow.prepare(() -> {
                    assertTrue(true);
                    Sleep.now();
                    queue.add(Thread.currentThread().getId());
                })
            ).start();
        }

        AsyncFlow.await(1000, 4);
        Assertions.assertEquals(4, queue.size());
    }

    @Test
    public void supportsConsumerFlowAssertions() throws InterruptedException
    {
        final AtomicBoolean flag = new AtomicBoolean(false);

        final Consumer<AtomicBoolean> flow = AsyncFlow.prepare(e -> {
            Sleep.now();
            e.set(true);
        });
        new Thread(
            () -> {
                Sleep.now();
                flow.accept(flag);
            }
        ).start();

        AsyncFlow.await();
        assertTrue(flag.get());
    }

    @Test
    public void supportsSupplierFlowAssertions() throws InterruptedException
    {
        final AtomicBoolean flag = new AtomicBoolean(false);

        final Supplier<AtomicBoolean> flow = AsyncFlow.prepare(() -> {
            Sleep.now();
            assertTrue(true);
            return new AtomicBoolean(true);
        });
        new Thread(
            () -> {
                Sleep.now();
                flag.set(flow.get().get());
            }
        ).start();

        AsyncFlow.await();
        assertTrue(flag.get());
    }

    @Test
    public void failsOnMissingThreadExecutions()
    {
        for (int i = 0; i < 2; i++)
        {
            new Thread(AsyncFlow.prepare((Runnable) Sleep::now)).start();
        }
        assertThatThrownBy(() -> AsyncFlow.await(1000, 5))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Number of flow executions was 2 instead of 5");
    }

    @Test
    public void failsOnTimeout()
    {
        for (int i = 0; i < 4; i++)
        {
            new Thread(AsyncFlow.prepare(() -> Sleep.now(2000))).start();
        }
        assertThatThrownBy(() -> AsyncFlow.await(1000, 5))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Number of flow executions was 0 instead of 5");
    }

    @Test
    public void failsOnAssertionsErrorInThread()
    {
        new Thread(
            AsyncFlow.prepare((Runnable) Assertions::fail)
        ).start();

        assertThrows(AssertionError.class, AsyncFlow::await);
    }

    @Test
    public void failsOnExceptionThrownInThread()
    {
        new Thread(
            AsyncFlow.prepare(this::sneakyThrow)
        ).start();

        assertThrows(IllegalStateException.class, AsyncFlow::await);
    }

    @Test
    public void failsOnThreadInterruption()
    {
        final Thread main = Thread.currentThread();
        new Thread(AsyncFlow.prepare(main::interrupt)).start();

        assertThrows(InterruptedException.class, AsyncFlow::await);
    }

    @Test
    public void failsOnNoFlowPrepared()
    {
        assertThrows(AssertionError.class, () -> AsyncFlow.await(100));
    }

    @Test
    public void failsOnPreparingFlowLazyUsingStaticMethods()
    {
        new Thread(
            () -> AsyncFlow.prepare(() -> assertTrue(true)).run()
        ).start();

        assertThrows(AssertionError.class, () -> AsyncFlow.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void supportsLazyPreparationInSingleFlow() throws InterruptedException
    {
        AsyncFlow.Single flow = new AsyncFlow.Single();
        final AtomicBoolean flag = new AtomicBoolean(false);

        new Thread(() ->
            flow.prepare(() -> {
                Sleep.now();
                flag.set(true);
            }).run()
        ).start();

        flow.await(400, TimeUnit.MILLISECONDS);

        assertTrue(flag.get());
    }

    private void sneakyThrow()
    {
        throw new IllegalStateException("");
    }
}
