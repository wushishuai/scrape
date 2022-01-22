package android.databinding;

import android.databinding.Observable;
import android.support.annotation.NonNull;

/* loaded from: classes.dex */
public class BaseObservable implements Observable {
    private transient PropertyChangeRegistry mCallbacks;

    @Override // android.databinding.Observable
    public void addOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback onPropertyChangedCallback) {
        synchronized (this) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new PropertyChangeRegistry();
            }
        }
        this.mCallbacks.add(onPropertyChangedCallback);
    }

    @Override // android.databinding.Observable
    public void removeOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback onPropertyChangedCallback) {
        synchronized (this) {
            if (this.mCallbacks != null) {
                this.mCallbacks.remove(onPropertyChangedCallback);
            }
        }
    }

    public void notifyChange() {
        synchronized (this) {
            if (this.mCallbacks != null) {
                this.mCallbacks.notifyCallbacks(this, 0, null);
            }
        }
    }

    public void notifyPropertyChanged(int i) {
        synchronized (this) {
            if (this.mCallbacks != null) {
                this.mCallbacks.notifyCallbacks(this, i, null);
            }
        }
    }
}
