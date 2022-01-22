package com.jakewharton.rxbinding2.view;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_ViewGroupHierarchyChildViewRemoveEvent extends ViewGroupHierarchyChildViewRemoveEvent {
    private final View child;
    private final ViewGroup view;

    public AutoValue_ViewGroupHierarchyChildViewRemoveEvent(ViewGroup viewGroup, View view) {
        if (viewGroup != null) {
            this.view = viewGroup;
            if (view != null) {
                this.child = view;
                return;
            }
            throw new NullPointerException("Null child");
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.view.ViewGroupHierarchyChangeEvent
    @NonNull
    public ViewGroup view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.view.ViewGroupHierarchyChangeEvent
    @NonNull
    public View child() {
        return this.child;
    }

    public String toString() {
        return "ViewGroupHierarchyChildViewRemoveEvent{view=" + this.view + ", child=" + this.child + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ViewGroupHierarchyChildViewRemoveEvent)) {
            return false;
        }
        ViewGroupHierarchyChildViewRemoveEvent viewGroupHierarchyChildViewRemoveEvent = (ViewGroupHierarchyChildViewRemoveEvent) obj;
        if (!this.view.equals(viewGroupHierarchyChildViewRemoveEvent.view()) || !this.child.equals(viewGroupHierarchyChildViewRemoveEvent.child())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return ((this.view.hashCode() ^ 1000003) * 1000003) ^ this.child.hashCode();
    }
}
