package android.support.p003v7.view.menu;

/* renamed from: android.support.v7.view.menu.BaseWrapper */
/* loaded from: classes.dex */
class BaseWrapper<T> {
    final T mWrappedObject;

    /* JADX INFO: Access modifiers changed from: package-private */
    public BaseWrapper(T t) {
        if (t != null) {
            this.mWrappedObject = t;
            return;
        }
        throw new IllegalArgumentException("Wrapped Object can not be null.");
    }

    public T getWrappedObject() {
        return this.mWrappedObject;
    }
}
