package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleMap */
/* loaded from: classes.dex */
public final class SingleMap<T, R> extends Single<R> {
    final Function<? super T, ? extends R> mapper;
    final SingleSource<? extends T> source;

    public SingleMap(SingleSource<? extends T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super R> t) {
        this.source.subscribe(new MapSingleObserver(t, this.mapper));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleMap$MapSingleObserver */
    /* loaded from: classes.dex */
    static final class MapSingleObserver<T, R> implements SingleObserver<T> {
        final Function<? super T, ? extends R> mapper;

        /* renamed from: t */
        final SingleObserver<? super R> f175t;

        /* JADX INFO: Access modifiers changed from: package-private */
        public MapSingleObserver(SingleObserver<? super R> t, Function<? super T, ? extends R> mapper) {
            this.f175t = t;
            this.mapper = mapper;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.f175t.onSubscribe(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            try {
                this.f175t.onSuccess(ObjectHelper.requireNonNull(this.mapper.apply(value), "The mapper function returned a null value."));
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                onError(e);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.f175t.onError(e);
        }
    }
}
