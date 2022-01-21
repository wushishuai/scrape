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

    public void post(Object event) {
        this.mBus.onNext(event);
    }

    public <T> Observable<T> toObservable(Class<T> eventType) {
        return (Observable<T>) this.mBus.ofType(eventType);
    }

    public boolean hasObservers() {
        return this.mBus.hasObservers();
    }

    public void reset() {
        mDefaultInstance = null;
    }

    public void postSticky(Object event) {
        synchronized (this.mStickyEventMap) {
            this.mStickyEventMap.put(event.getClass(), event);
        }
        post(event);
    }

    public <T> Observable<T> toObservableSticky(final Class<T> eventType) {
        synchronized (this.mStickyEventMap) {
            Observable<T> observable = (Observable<T>) this.mBus.ofType(eventType);
            final Object event = this.mStickyEventMap.get(eventType);
            if (event == null) {
                return observable;
            }
            return Observable.merge(observable, Observable.create(new ObservableOnSubscribe<T>() { // from class: me.goldze.mvvmhabit.bus.RxBus.1
                @Override // p005io.reactivex.ObservableOnSubscribe
                public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                    emitter.onNext(eventType.cast(event));
                }
            }));
        }
    }

    public <T> T getStickyEvent(Class<T> eventType) {
        T cast;
        synchronized (this.mStickyEventMap) {
            cast = eventType.cast(this.mStickyEventMap.get(eventType));
        }
        return cast;
    }

    public <T> T removeStickyEvent(Class<T> eventType) {
        T cast;
        synchronized (this.mStickyEventMap) {
            cast = eventType.cast(this.mStickyEventMap.remove(eventType));
        }
        return cast;
    }

    public void removeAllStickyEvents() {
        synchronized (this.mStickyEventMap) {
            this.mStickyEventMap.clear();
        }
    }
}
