package hr.com.vgv.asyncunit;

public class Sleep
{
    public static void now() {
        now(100);
    }

    public static void now(long duration) {
        try
        {
            Thread.sleep(duration);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
