package hr.com.vgv.asyncunit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Mutex
{
    private final Sync sync = new Sync();

    public void lock()
    {
        sync.acquire(1);
    }

    public void unlock()
    {
        sync.release(0);
    }

    public void tryLock() throws InterruptedException
    {
        sync.acquireInterruptibly(1);
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
    {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    private static class Sync extends AbstractQueuedSynchronizer
    {
        private static final long serialVersionUID = -3708887661288419702L;

        @Override
        public final boolean tryAcquire(int acquires)
        {
            int state = getState();
            if (state == 0)
            {
                setState(1);
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public final boolean tryRelease(int releases)
        {
            setState(0);
            return true;
        }
    }
}
