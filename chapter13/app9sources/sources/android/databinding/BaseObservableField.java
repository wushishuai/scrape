package android.databinding;

import android.databinding.Observable;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class BaseObservableField extends BaseObservable {
    public BaseObservableField() {
    }

    public BaseObservableField(Observable... observableArr) {
        if (!(observableArr == null || observableArr.length == 0)) {
            DependencyCallback dependencyCallback = new DependencyCallback();
            for (Observable observable : observableArr) {
                observable.addOnPropertyChangedCallback(dependencyCallback);
            }
        }
    }

    /* loaded from: classes.dex */
    class DependencyCallback extends Observable.OnPropertyChangedCallback {
        DependencyCallback() {
        }

        @Override // android.databinding.Observable.OnPropertyChangedCallback
        public void onPropertyChanged(Observable observable, int i) {
            BaseObservableField.this.notifyChange();
        }
    }
}
