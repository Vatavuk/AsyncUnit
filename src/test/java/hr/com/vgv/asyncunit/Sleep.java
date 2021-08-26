package hr.com.vgv.asyncunit;

/**
 * Wrapper around Thread.sleep()
 */
public class Sleep
{
    /**
     * Sleep for 100 milliseconds.
     */
    public static void now() {
        now(100);
    }

    /**
     * Sleep for duration milliseconds. Handles thread interrupt exception.
     * @param duration Duration of sleep in milliseconds
     */
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
