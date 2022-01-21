package p005io.reactivex.internal.util;

/* renamed from: io.reactivex.internal.util.Pow2 */
/* loaded from: classes.dex */
public final class Pow2 {
    private Pow2() {
        throw new IllegalStateException("No instances!");
    }

    public static int roundToPowerOfTwo(int value) {
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    public static boolean isPowerOfTwo(int value) {
        return ((value + -1) & value) == 0;
    }
}
