package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.widget.RatingBar;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_RatingBarChangeEvent extends RatingBarChangeEvent {
    private final boolean fromUser;
    private final float rating;
    private final RatingBar view;

    public AutoValue_RatingBarChangeEvent(RatingBar view, float rating, boolean fromUser) {
        if (view != null) {
            this.view = view;
            this.rating = rating;
            this.fromUser = fromUser;
            return;
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.RatingBarChangeEvent
    @NonNull
    public RatingBar view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.RatingBarChangeEvent
    public float rating() {
        return this.rating;
    }

    @Override // com.jakewharton.rxbinding2.widget.RatingBarChangeEvent
    public boolean fromUser() {
        return this.fromUser;
    }

    public String toString() {
        return "RatingBarChangeEvent{view=" + this.view + ", rating=" + this.rating + ", fromUser=" + this.fromUser + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RatingBarChangeEvent)) {
            return false;
        }
        RatingBarChangeEvent that = (RatingBarChangeEvent) o;
        if (this.view.equals(that.view()) && Float.floatToIntBits(this.rating) == Float.floatToIntBits(that.rating()) && this.fromUser == that.fromUser()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((1 * 1000003) ^ this.view.hashCode()) * 1000003) ^ Float.floatToIntBits(this.rating)) * 1000003) ^ (this.fromUser ? 1231 : 1237);
    }
}
