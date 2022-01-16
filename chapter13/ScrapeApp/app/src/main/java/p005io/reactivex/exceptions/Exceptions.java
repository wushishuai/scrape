package p005io.reactivex.exceptions;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.exceptions.Exceptions */
/* loaded from: classes.dex */
public final class Exceptions {
    private Exceptions() {
        throw new IllegalStateException("No instances!");
    }

    @NonNull
    public static RuntimeException propagate(@NonNull Throwable t) {
        throw ExceptionHelper.wrapOrThrow(t);
    }

    public static void throwIfFatal(@NonNull Throwable t) {
        if (t instanceof VirtualMachineError) {
            throw ((VirtualMachineError) t);
        } else if (t instanceof ThreadDeath) {
            throw ((ThreadDeath) t);
        } else if (t instanceof LinkageError) {
            throw ((LinkageError) t);
        }
    }
}
