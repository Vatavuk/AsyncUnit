package hr.com.vgv.asyncunit;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Asynchronous part of a code under test.
 *
 * Example of usage:
 *
 *  new Thread(AsyncFlow.prepare(
 *       sleep(200);
 *       assertTrue(true);
 *   )).start();
 *
 *   AsyncFlow.await();
 *
 * @author Vedran Vatavuk
 */
public class AsyncFlow
{
    /**
     * Prepares Runnable for testing in main class.
     * @param runnable Runnable under test
     * @return Runnable
     */
    public static Runnable prepare(Runnable runnable)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Prepares Consumer for testing in main class.
     * @param consumer Consumer under test
     * @param <T> T
     * @return Consumer
     */
    public static <T> Consumer<T> prepare(Consumer<T> consumer)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Prepares BiConsumer for testing in main class.
     * @param consumer BiConsumer under test
     * @param <T> T
     * @param <U> U
     * @return BiConsumer
     */
    public static <T, U> BiConsumer<T, U> prepare(BiConsumer<T, U> consumer)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Prepares Supplier for testing in main class.
     * @param supplier Supplier under test
     * @param <T> T
     * @return Supplier
     */
    public static <T> Supplier<T> prepare(Supplier<T> supplier)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Prepares Function for testing in main class.
     * @param function Function under test
     * @param <T> T
     * @param <R> R
     * @return Function
     */
    public static <T, R> Function<T, R> prepareFn(Function<T, R> function)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Prepares BiFunction for testing in main class.
     * @param function BiFunction under test
     * @param <T> T
     * @param <U> U
     * @param <R> R
     * @return BiFunction
     */
    public static <T, U, R> BiFunction<T, U, R> prepareFn(BiFunction<T, U, R> function)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    /**
     * Waits for a prepared async flow to finishes.
     *
     * <b>NOTE:</b> Use this method only in MAIN class!!
     *
     * @throws InterruptedException If interrupted
     */
    public static void await() throws InterruptedException
    {
        //TODO: don't forget to clear cache in finally block
        throw new UnsupportedOperationException("#await");
    }

    /**
     * Waits for a prepared async flow to finishes. It raises AssertionError after timeout expires.
     *
     * <b>NOTE:</b> Use this method only in MAIN class!!
     *
     * @throws InterruptedException If interrupted
     */
    public static void await(long timeout) throws InterruptedException
    {
        //TODO: don't forget to clear cache in finally block
        throw new UnsupportedOperationException("#await");
    }
}
