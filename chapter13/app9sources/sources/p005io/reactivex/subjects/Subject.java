package p005io.reactivex.subjects;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.subjects.Subject */
/* loaded from: classes.dex */
public abstract class Subject<T> extends Observable<T> implements Observer<T> {
    @Nullable
    public abstract Throwable getThrowable();

    public abstract boolean hasComplete();

    public abstract boolean hasObservers();

    public abstract boolean hasThrowable();

    @NonNull
    public final Subject<T> toSerialized() {
        if (this instanceof SerializedSubject) {
            return this;
        }
        return new SerializedSubject(this);
    }
}
