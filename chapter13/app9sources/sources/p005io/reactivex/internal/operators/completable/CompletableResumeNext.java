package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.completable.CompletableResumeNext */
/* loaded from: classes.dex */
public final class CompletableResumeNext extends Completable {
    final Function<? super Throwable, ? extends CompletableSource> errorMapper;
    final CompletableSource source;

    public CompletableResumeNext(CompletableSource completableSource, Function<? super Throwable, ? extends CompletableSource> function) {
        this.source = completableSource;
        this.errorMapper = function;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        ResumeNextObserver resumeNextObserver = new ResumeNextObserver(completableObserver, this.errorMapper);
        completableObserver.onSubscribe(resumeNextObserver);
        this.source.subscribe(resumeNextObserver);
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableResumeNext$ResumeNextObserver */
    /* loaded from: classes.dex */
    static final class ResumeNextObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
        private static final long serialVersionUID = 5018523762564524046L;
        final CompletableObserver downstream;
        final Function<? super Throwable, ? extends CompletableSource> errorMapper;
        boolean once;

        ResumeNextObserver(CompletableObserver completableObserver, Function<? super Throwable, ? extends CompletableSource> function) {
            this.downstream = completableObserver;
            this.errorMapper = function;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.replace(this, disposable);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            if (this.once) {
                this.downstream.onError(th);
                return;
            }
            this.once = true;
            try {
                ((CompletableSource) ObjectHelper.requireNonNull(this.errorMapper.apply(th), "The errorMapper returned a null CompletableSource")).subscribe(this);
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                this.downstream.onError(new CompositeException(th, th2));
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }
    }
}
