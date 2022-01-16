package com.trello.rxlifecycle2;

import io.reactivex.Completable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.CancellationException;
/* loaded from: classes.dex */
final class Functions {
    static final Function<Throwable, Boolean> RESUME_FUNCTION = new Function<Throwable, Boolean>() { // from class: com.trello.rxlifecycle2.Functions.1
        public Boolean apply(Throwable throwable) throws Exception {
            if (throwable instanceof OutsideLifecycleException) {
                return true;
            }
            Exceptions.propagate(throwable);
            return false;
        }
    };
    static final Predicate<Boolean> SHOULD_COMPLETE = new Predicate<Boolean>() { // from class: com.trello.rxlifecycle2.Functions.2
        public boolean test(Boolean shouldComplete) throws Exception {
            return shouldComplete.booleanValue();
        }
    };
    static final Function<Object, Completable> CANCEL_COMPLETABLE = new Function<Object, Completable>() { // from class: com.trello.rxlifecycle2.Functions.3
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // io.reactivex.functions.Function
        public Completable apply(Object ignore) throws Exception {
            return Completable.error(new CancellationException());
        }
    };

    private Functions() {
        throw new AssertionError("No instances!");
    }
}
