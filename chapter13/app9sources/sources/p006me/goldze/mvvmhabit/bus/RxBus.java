package p006me.goldze.mvvmhabit.bus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableEmitter;
import p005io.reactivex.ObservableOnSubscribe;
import p005io.reactivex.subjects.PublishSubject;
import p005io.reactivex.subjects.Subject;

/* renamed from: me.goldze.mvvmhabit.bus.RxBus */
/* loaded from: classes.dex */
public class RxBus {
    private static volatile RxBus mDefaultInstance;
    private final Subject<Object> mBus = PublishSubject.create().toSerialized();
    private final Map<Class<?>, Object> mStickyEventMap = new ConcurrentHashMap();

    public static RxBus getDefault() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    public void post(Object obj) {
        this.mBus.onNext(obj);
    }

    public <T> Observable<T> toObservable(Class<T> cls) {
        return (Observable<T>) this.mBus.ofType(cls);
    }

    public boolean hasObservers() {
        return this.mBus.hasObservers();
    }

    public void reset() {
        mDefaultInstance = null;
    }

    public void postSticky(Object obj) {
        synchronized (this.mStickyEventMap) {
            this.mStickyEventMap.put(obj.getClass(), obj);
        }
        post(obj);
    }

    public <T> Observable<T> toObservableSticky(final Class<T> cls) {
        synchronized (this.mStickyEventMap) {
            Observable<T> observable = (Observable<T>) this.mBus.ofType(cls);
            final Object obj = this.mStickyEventMap.get(cls);
            if (obj == null) {
                return observable;
            }
            return Observable.merge(observable, Observable.create(new ObservableOnSubscribe<T>() { // from class: me.goldze.mvvmhabit.bus.RxBus.1
                @Override // p005io.reactivex.ObservableOnSubscribe
                public void subscribe(ObservableEmitter<T> observableEmitter) throws Exception {
                    observableEmitter.onNext(cls.cast(obj));
                }
            }));
        }
    }

    public <T> T getStickyEvent(Class<T> cls) {
        T cast;
        synchronized (this.mStickyEventMap) {
            cast = cls.cast(this.mStickyEventMap.get(cls));
        }
        return cast;
    }

    public <T> T removeStickyEvent(Class<T> cls) {
        T cast;
        synchronized (this.mStickyEventMap) {
            cast = cls.cast(this.mStickyEventMap.remove(cls));
        }
        return cast;
    }

    public void removeAllStickyEvents() {
        synchronized (this.mStickyEventMap) {
            this.mStickyEventMap.clear();
        }
    }
}
