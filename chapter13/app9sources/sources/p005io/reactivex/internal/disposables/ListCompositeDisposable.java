package p005io.reactivex.internal.disposables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.internal.disposables.ListCompositeDisposable */
/* loaded from: classes.dex */
public final class ListCompositeDisposable implements Disposable, DisposableContainer {
    volatile boolean disposed;
    List<Disposable> resources;

    public ListCompositeDisposable() {
    }

    public ListCompositeDisposable(Disposable... disposableArr) {
        ObjectHelper.requireNonNull(disposableArr, "resources is null");
        this.resources = new LinkedList();
        for (Disposable disposable : disposableArr) {
            ObjectHelper.requireNonNull(disposable, "Disposable item is null");
            this.resources.add(disposable);
        }
    }

    public ListCompositeDisposable(Iterable<? extends Disposable> iterable) {
        ObjectHelper.requireNonNull(iterable, "resources is null");
        this.resources = new LinkedList();
        for (Disposable disposable : iterable) {
            ObjectHelper.requireNonNull(disposable, "Disposable item is null");
            this.resources.add(disposable);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        if (!this.disposed) {
            synchronized (this) {
                if (!this.disposed) {
                    this.disposed = true;
                    List<Disposable> list = this.resources;
                    this.resources = null;
                    dispose(list);
                }
            }
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override // p005io.reactivex.internal.disposables.DisposableContainer
    public boolean add(Disposable disposable) {
        ObjectHelper.requireNonNull(disposable, "d is null");
        if (!this.disposed) {
            synchronized (this) {
                if (!this.disposed) {
                    List list = this.resources;
                    if (list == null) {
                        list = new LinkedList();
                        this.resources = list;
                    }
                    list.add(disposable);
                    return true;
                }
            }
        }
        disposable.dispose();
        return false;
    }

    public boolean addAll(Disposable... disposableArr) {
        ObjectHelper.requireNonNull(disposableArr, "ds is null");
        if (!this.disposed) {
            synchronized (this) {
                if (!this.disposed) {
                    List list = this.resources;
                    if (list == null) {
                        list = new LinkedList();
                        this.resources = list;
                    }
                    for (Disposable disposable : disposableArr) {
                        ObjectHelper.requireNonNull(disposable, "d is null");
                        list.add(disposable);
                    }
                    return true;
                }
            }
        }
        for (Disposable disposable2 : disposableArr) {
            disposable2.dispose();
        }
        return false;
    }

    @Override // p005io.reactivex.internal.disposables.DisposableContainer
    public boolean remove(Disposable disposable) {
        if (!delete(disposable)) {
            return false;
        }
        disposable.dispose();
        return true;
    }

    @Override // p005io.reactivex.internal.disposables.DisposableContainer
    public boolean delete(Disposable disposable) {
        ObjectHelper.requireNonNull(disposable, "Disposable item is null");
        if (this.disposed) {
            return false;
        }
        synchronized (this) {
            if (this.disposed) {
                return false;
            }
            List<Disposable> list = this.resources;
            if (list != null && list.remove(disposable)) {
                return true;
            }
            return false;
        }
    }

    public void clear() {
        if (!this.disposed) {
            synchronized (this) {
                if (!this.disposed) {
                    List<Disposable> list = this.resources;
                    this.resources = null;
                    dispose(list);
                }
            }
        }
    }

    void dispose(List<Disposable> list) {
        if (list != null) {
            ArrayList arrayList = null;
            for (Disposable disposable : list) {
                try {
                    disposable.dispose();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(th);
                }
            }
            if (arrayList == null) {
                return;
            }
            if (arrayList.size() == 1) {
                throw ExceptionHelper.wrapOrThrow((Throwable) arrayList.get(0));
            }
            throw new CompositeException(arrayList);
        }
    }
}
