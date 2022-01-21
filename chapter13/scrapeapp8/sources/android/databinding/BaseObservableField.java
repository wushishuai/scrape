package android.databinding;

import android.databinding.Observable;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class BaseObservableField extends BaseObservable {
    public BaseObservableField() {
    }

    public BaseObservableField(Observable... dependencies) {
        if (!(dependencies == null || dependencies.length == 0)) {
            DependencyCallback callback = new DependencyCallback();
            for (Observable observable : dependencies) {
                observable.addOnPropertyChangedCallback(callback);
            }
        }
    }

    /* loaded from: classes.dex */
    class DependencyCallback extends Observable.OnPropertyChangedCallback {
        DependencyCallback() {
        }

        @Override // android.databinding.Observable.OnPropertyChangedCallback
        public void onPropertyChanged(Observable sender, int propertyId) {
            BaseObservableField.this.notifyChange();
        }
    }
}
