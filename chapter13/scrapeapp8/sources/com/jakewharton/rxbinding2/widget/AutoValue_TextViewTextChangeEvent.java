package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.widget.TextView;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_TextViewTextChangeEvent extends TextViewTextChangeEvent {
    private final int before;
    private final int count;
    private final int start;
    private final CharSequence text;
    private final TextView view;

    public AutoValue_TextViewTextChangeEvent(TextView view, CharSequence text, int start, int before, int count) {
        if (view != null) {
            this.view = view;
            if (text != null) {
                this.text = text;
                this.start = start;
                this.before = before;
                this.count = count;
                return;
            }
            throw new NullPointerException("Null text");
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
    @NonNull
    public TextView view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
    @NonNull
    public CharSequence text() {
        return this.text;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
    public int start() {
        return this.start;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
    public int before() {
        return this.before;
    }

    @Override // com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
    public int count() {
        return this.count;
    }

    public String toString() {
        return "TextViewTextChangeEvent{view=" + this.view + ", text=" + ((Object) this.text) + ", start=" + this.start + ", before=" + this.before + ", count=" + this.count + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TextViewTextChangeEvent)) {
            return false;
        }
        TextViewTextChangeEvent that = (TextViewTextChangeEvent) o;
        if (this.view.equals(that.view()) && this.text.equals(that.text()) && this.start == that.start() && this.before == that.before() && this.count == that.count()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((((((1 * 1000003) ^ this.view.hashCode()) * 1000003) ^ this.text.hashCode()) * 1000003) ^ this.start) * 1000003) ^ this.before) * 1000003) ^ this.count;
    }
}
