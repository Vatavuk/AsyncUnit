package hr.com.vgv.asyncunit;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsyncFlowSingleTest
{
    @Test
    public void throwsSupportedException() {

        AsyncFlow.Single flow = new AsyncFlow.Single(UncheckedIOException.class);

        new Thread(flow.prepare((Runnable) () -> {
            throw new MyIOException("");
        })).start();

        assertThrows(MyIOException.class, () -> flow.await(100));
    }

    @Test
    public void doesntThrowUnsupportedException() {

        AsyncFlow.Single flow = new AsyncFlow.Single(IOException.class);

        new Thread(flow.prepare((Runnable) () -> {
            throw new RuntimeException("");
        })).start();

        assertThrows(AssertionError.class, () -> flow.await(100));
    }



    private static class MyIOException extends UncheckedIOException
    {

        public MyIOException(String message)
        {
            super(message, new IOException());
        }
    }
}
