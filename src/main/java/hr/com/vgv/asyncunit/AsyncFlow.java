package hr.com.vgv.asyncunit;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncFlow
{
    public static Runnable prepare(Runnable runnable)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static <T> Consumer<T> prepare(Consumer<T> consumer)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static <T, U> BiConsumer<T, U> prepare(BiConsumer<T, U> consumer)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static <T> Supplier<T> prepare(Supplier<T> supplier)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static <T, R> Function<T, R> prepare(Function<T, R> function)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static <T, U, R> BiFunction<T, U, R> prepare(BiFunction<T, U, R> function)
    {
        throw new UnsupportedOperationException("#prepare");
    }

    public static void await() throws Throwable
    {
        throw new UnsupportedOperationException("#await");
    }

    public static void await(long period) throws Throwable
    {
        throw new UnsupportedOperationException("#await");
    }
}
