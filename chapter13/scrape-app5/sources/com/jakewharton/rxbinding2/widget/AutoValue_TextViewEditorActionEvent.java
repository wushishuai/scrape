package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.TextView;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_TextViewEditorActionEvent extends TextViewEditorActionEvent {
    private final int actionId;
    private final KeyEvent keyEvent;
    private final TextView view;

    public AutoValue_TextViewEditorActionEvent(TextView view, int actionId, @Nullable KeyEvent keyEvent) {
        if (view != null) {
            this.view = view;
            this.actionId = actionId;
            this.keyEvent = keyEvent;
            return;
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewEditorActionEvent
    @NonNull
    public TextView view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewEditorActionEvent
    public int actionId() {
        return this.actionId;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewEditorActionEvent
    @Nullable
    public KeyEvent keyEvent() {
        return this.keyEvent;
    }

    public String toString() {
        return "TextViewEditorActionEvent{view=" + this.view + ", actionId=" + this.actionId + ", keyEvent=" + this.keyEvent + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TextViewEditorActionEvent)) {
            return false;
        }
        TextViewEditorActionEvent that = (TextViewEditorActionEvent) o;
        if (this.view.equals(that.view()) && this.actionId == that.actionId()) {
            KeyEvent keyEvent = this.keyEvent;
            if (keyEvent == null) {
                if (that.keyEvent() == null) {
                    return true;
                }
            } else if (keyEvent.equals(that.keyEvent())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int h = ((((1 * 1000003) ^ this.view.hashCode()) * 1000003) ^ this.actionId) * 1000003;
        KeyEvent keyEvent = this.keyEvent;
        return h ^ (keyEvent == null ? 0 : keyEvent.hashCode());
    }
}
