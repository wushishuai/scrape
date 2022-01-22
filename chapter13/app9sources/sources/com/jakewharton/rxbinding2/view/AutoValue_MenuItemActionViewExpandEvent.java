package com.jakewharton.rxbinding2.view;

import android.support.annotation.NonNull;
import android.view.MenuItem;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_MenuItemActionViewExpandEvent extends MenuItemActionViewExpandEvent {
    private final MenuItem menuItem;

    public AutoValue_MenuItemActionViewExpandEvent(MenuItem menuItem) {
        if (menuItem != null) {
            this.menuItem = menuItem;
            return;
        }
        throw new NullPointerException("Null menuItem");
    }

    @Override // com.jakewharton.rxbinding2.view.MenuItemActionViewEvent
    @NonNull
    public MenuItem menuItem() {
        return this.menuItem;
    }

    public String toString() {
        return "MenuItemActionViewExpandEvent{menuItem=" + this.menuItem + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MenuItemActionViewExpandEvent) {
            return this.menuItem.equals(((MenuItemActionViewExpandEvent) obj).menuItem());
        }
        return false;
    }

    public int hashCode() {
        return this.menuItem.hashCode() ^ 1000003;
    }
}
