package p005io.reactivex.exceptions;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.exceptions.OnErrorNotImplementedException */
/* loaded from: classes.dex */
public final class OnErrorNotImplementedException extends RuntimeException {
    private static final long serialVersionUID = -6298857009889503852L;

    public OnErrorNotImplementedException(String str, @NonNull Throwable th) {
        super(str, th == null ? new NullPointerException() : th);
    }

    public OnErrorNotImplementedException(@NonNull Throwable th) {
        this("The exception was not handled due to missing onError handler in the subscribe() method call. Further reading: https://github.com/ReactiveX/RxJava/wiki/Error-Handling | " + th, th);
    }
}
