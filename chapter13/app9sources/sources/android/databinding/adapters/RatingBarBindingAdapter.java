package android.databinding.adapters;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.support.annotation.RestrictTo;
import android.widget.RatingBar;

@InverseBindingMethods({@InverseBindingMethod(attribute = "android:rating", type = RatingBar.class)})
@RestrictTo({RestrictTo.Scope.LIBRARY})
/* loaded from: classes.dex */
public class RatingBarBindingAdapter {
    @BindingAdapter({"android:rating"})
    public static void setRating(RatingBar ratingBar, float f) {
        if (ratingBar.getRating() != f) {
            ratingBar.setRating(f);
        }
    }

    @BindingAdapter(requireAll = false, value = {"android:onRatingChanged", "android:ratingAttrChanged"})
    public static void setListeners(RatingBar ratingBar, final RatingBar.OnRatingBarChangeListener onRatingBarChangeListener, final InverseBindingListener inverseBindingListener) {
        if (inverseBindingListener == null) {
            ratingBar.setOnRatingBarChangeListener(onRatingBarChangeListener);
        } else {
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() { // from class: android.databinding.adapters.RatingBarBindingAdapter.1
                @Override // android.widget.RatingBar.OnRatingBarChangeListener
                public void onRatingChanged(RatingBar ratingBar2, float f, boolean z) {
                    RatingBar.OnRatingBarChangeListener onRatingBarChangeListener2 = onRatingBarChangeListener;
                    if (onRatingBarChangeListener2 != null) {
                        onRatingBarChangeListener2.onRatingChanged(ratingBar2, f, z);
                    }
                    inverseBindingListener.onChange();
                }
            });
        }
    }
}
