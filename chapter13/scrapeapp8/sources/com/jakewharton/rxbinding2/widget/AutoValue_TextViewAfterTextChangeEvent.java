package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.widget.TextView;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_TextViewAfterTextChangeEvent extends TextViewAfterTextChangeEvent {
    private final Editable editable;
    private final TextView view;

    public AutoValue_TextViewAfterTextChangeEvent(TextView view, @Nullable Editable editable) {
        if (view != null) {
            this.view = view;
            this.editable = editable;
            return;
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
    @NonNull
    public TextView view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
    @Nullable
    public Editable editable() {
        return this.editable;
    }

    public String toString() {
        return "TextViewAfterTextChangeEvent{view=" + this.view + ", editable=" + ((Object) this.editable) + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TextViewAfterTextChangeEvent)) {
            return false;
        }
        TextViewAfterTextChangeEvent that = (TextViewAfterTextChangeEvent) o;
        if (this.view.equals(that.view())) {
            Editable editable = this.editable;
            if (editable == null) {
                if (that.editable() == null) {
                    return true;
                }
            } else if (editable.equals(that.editable())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int h = ((1 * 1000003) ^ this.view.hashCode()) * 1000003;
        Editable editable = this.editable;
        return h ^ (editable == null ? 0 : editable.hashCode());
    }
}
