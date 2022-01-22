package p005io.reactivex.disposables;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.functions.Action;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.disposables.ActionDisposable */
/* loaded from: classes.dex */
final class ActionDisposable extends ReferenceDisposable<Action> {
    private static final long serialVersionUID = -8219729196779211169L;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionDisposable(Action action) {
        super(action);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDisposed(@NonNull Action action) {
        try {
            action.run();
        } catch (Throwable th) {
            throw ExceptionHelper.wrapOrThrow(th);
        }
    }
}
