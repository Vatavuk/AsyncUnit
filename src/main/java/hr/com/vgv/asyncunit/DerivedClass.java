package hr.com.vgv.asyncunit;

/**
 * Class derived from a base class.
 */
public class DerivedClass
{
    /**
     * Derived class.
     */
    private final Class<?> derived;

    /**
     * Ctor.
     * @param cderived Derived class
     */
    public DerivedClass(final Class<?> cderived) {
        this.derived = cderived;
    }

    /**
     * Check if this class is related to a given class.
     * @param cls Class
     * @return Boolean Boolean
     */
    public boolean isRelatedTo(final Class<?> cls) {
        final int level;
        if (cls.equals(this.derived)) {
            level = 0;
        } else {
            level = this.calculateLevel(cls);
        }
        return level >= 0;
    }

    /**
     * Calculates inheritance level. The number of superclasses between base and derived class.
     *
     * <p>This class is thread safe.
     *
     * <p>Result interpretation:
     * <ul>
     *     <li>{@link Integer#MIN_VALUE} -&gt; classes are not related.
     *     (ex. matching FileNotFoundException with RuntimeException);
     *     <li>0 -&gt; classes are identical. (ex. matching IOException with
     *     IOException);
     *     <li>1 -&gt; single level inheritance. (ex. matching
     *     FileNotFoundException with IOException);
     *     <li>2 -&gt; two inheritance levels. (ex. matching
     *     FileNotFoundException with Exception).
     * </ul>
     *
     * @return Integer Level
     */
    private int calculateLevel(Class<?> base) {
        int level = Integer.MIN_VALUE;
        Class<?> sclass = this.derived.getSuperclass();
        int idx = 0;
        while (!sclass.equals(Object.class)) {
            idx += 1;
            if (sclass.equals(base)) {
                level = idx;
                break;
            }
            sclass = sclass.getSuperclass();
        }
        return level;
    }
}
