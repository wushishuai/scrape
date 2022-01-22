package com.jakewharton.rxbinding2.view;

import android.support.annotation.NonNull;
import android.view.View;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_ViewAttachDetachedEvent extends ViewAttachDetachedEvent {
    private final View view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AutoValue_ViewAttachDetachedEvent(View view) {
        if (view != null) {
            this.view = view;
            return;
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.view.ViewAttachEvent
    @NonNull
    public View view() {
        return this.view;
    }

    public String toString() {
        return "ViewAttachDetachedEvent{view=" + this.view + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ViewAttachDetachedEvent) {
            return this.view.equals(((ViewAttachDetachedEvent) obj).view());
        }
        return false;
    }

    public int hashCode() {
        return this.view.hashCode() ^ 1000003;
    }
}
