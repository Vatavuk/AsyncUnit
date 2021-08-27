package hr.com.vgv.asyncunit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Asynchronous part of a code under test.
 * <p>
 * Example of usage:
 * <p>
 * new Thread(AsyncFlow.prepare(
 * sleep(200);
 * assertTrue(true);
 * )).start();
 * <p>
 * AsyncFlow.await();
 *
 * <b>Note</b>: Prepare and await calls must be done on the same thread, usually this will be
 * the main test thread.
 *
 * @author Vedran Vatavuk
 */
public class AsyncFlow
{
    private static final Map<Long, Results> flow = new ConcurrentHashMap<>();

    /**
     * Prepares Runnable for testing in main class.
     *
     * @param runnable Runnable under test
     * @return Runnable
     */
    public static Runnable prepare(Runnable runnable)
    {
        Consumer<Object> prepared = prepare(e -> runnable.run());

        return () -> prepared.accept(null);
    }

    /**
     * Prepares Consumer for testing in main class.
     *
     * @param consumer Consumer under test
     * @param <T>      T
     * @return Consumer
     */
    public static <T, U> Consumer<T> prepare(Consumer<T> consumer)
    {
        BiConsumer<T, U> prepared = prepare((T t, U u) -> consumer.accept(t));

        return t -> prepared.accept(t, null);
    }

    /**
     * Prepares BiConsumer for testing in main class.
     *
     * @param consumer BiConsumer under test
     * @param <T>      T
     * @param <U>      U
     * @return BiConsumer
     */
    public static <T, U> BiConsumer<T, U> prepare(BiConsumer<T, U> consumer)
    {
        Results results = initResults();
        return (t, u) -> {
            try
            {
                consumer.accept(t, u);
                results.addSuccess();
            }
            catch (Throwable throwable)
            {
                results.addFailure(throwable);
            }
        };
    }

    /**
     * Prepares Supplier for testing in main class.
     *
     * @param supplier Supplier under test
     * @param <T>      T
     * @return Supplier
     */
    public static <T> Supplier<T> prepare(Supplier<T> supplier)
    {
        /**/

        Function<Object, T> perpared = prepareFn((t) -> supplier.get());

        return () -> perpared.apply(null);
    }

    /**
     * Prepares Function for testing in main class.
     *
     * @param function Function under test
     * @param <T>      T
     * @param <R>      R
     * @return Function
     */
    public static <T, U, R> Function<T, R> prepareFn(Function<T, R> function)
    {
        BiFunction<T, U, R> prepared = prepareFn((T t, U u) -> function.apply(t));

        return t -> prepared.apply(t, null);
    }

    /**
     * Prepares BiFunction for testing in main class.
     *
     * @param function BiFunction under test
     * @param <T>      T
     * @param <U>      U
     * @param <R>      R
     * @return BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> prepareFn(BiFunction<T, U, R> function)
    {
        Results results = initResults();
        return (T t, U u) -> {
            try
            {
                R result = function.apply(t, u);
                results.addSuccess();
                return result;
            }
            catch (Throwable throwable)
            {
                results.addFailure(throwable);
                throw throwable;
            }
        };
    }

    /**
     * Waits for a prepared async flow to finishes.
     *
     * @throws InterruptedException If interrupted
     */
    public static void await() throws InterruptedException
    {
        await(0);
    }

    /**
     * Waits for a prepared async flow to finishes. It raises AssertionError after timeout expires.
     *
     * @throws InterruptedException If interrupted
     */
    public static void await(long timeout) throws InterruptedException
    {
        await(0, TimeUnit.MILLISECONDS);
    }

    public static void await(long timeout, TimeUnit timeUnit) throws InterruptedException
    {
        await(timeout, timeUnit, 1);
    }

    public static void await(long timeout, int times) throws InterruptedException
    {
        await(timeout, TimeUnit.MILLISECONDS, times);
    }

    public static void await(long timeout, TimeUnit timeUnit, int times) throws InterruptedException
    {
        long thread = currentThread();
        if (!flow.containsKey(thread))
        {
            throw new IllegalStateException(
                "No async flow prepared int this thread. Use AsyncFlow.prepare method before calling await");
        }
        try
        {
            flow.get(thread).await(timeout, timeUnit, times);
        }
        finally
        {
            flow.remove(thread);
        }
    }

    private static long currentThread()
    {
        return Thread.currentThread().getId();
    }

    private static Results initResults()
    {
        long thread = currentThread();
        flow.putIfAbsent(thread, new Results());
        return flow.get(thread);
    }
}
