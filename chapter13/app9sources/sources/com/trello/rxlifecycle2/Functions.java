package com.trello.rxlifecycle2;

import java.util.concurrent.CancellationException;
import p005io.reactivex.Completable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class Functions {
    static final Function<Throwable, Boolean> RESUME_FUNCTION = new Function<Throwable, Boolean>() { // from class: com.trello.rxlifecycle2.Functions.1
        public Boolean apply(Throwable th) throws Exception {
            if (th instanceof OutsideLifecycleException) {
                return true;
            }
            Exceptions.propagate(th);
            return false;
        }
    };
    static final Predicate<Boolean> SHOULD_COMPLETE = new Predicate<Boolean>() { // from class: com.trello.rxlifecycle2.Functions.2
        public boolean test(Boolean bool) throws Exception {
            return bool.booleanValue();
        }
    };
    static final Function<Object, Completable> CANCEL_COMPLETABLE = new Function<Object, Completable>() { // from class: com.trello.rxlifecycle2.Functions.3
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // p005io.reactivex.functions.Function
        public Completable apply(Object obj) throws Exception {
            return Completable.error(new CancellationException());
        }
    };

    private Functions() {
        throw new AssertionError("No instances!");
    }
}
