package android.databinding.adapters;

import android.databinding.BindingAdapter;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.support.annotation.RestrictTo;
import android.widget.CompoundButton;

@BindingMethods({@BindingMethod(attribute = "android:buttonTint", method = "setButtonTintList", type = CompoundButton.class), @BindingMethod(attribute = "android:onCheckedChanged", method = "setOnCheckedChangeListener", type = CompoundButton.class)})
@InverseBindingMethods({@InverseBindingMethod(attribute = "android:checked", type = CompoundButton.class)})
@RestrictTo({RestrictTo.Scope.LIBRARY})
/* loaded from: classes.dex */
public class CompoundButtonBindingAdapter {
    @BindingAdapter({"android:checked"})
    public static void setChecked(CompoundButton compoundButton, boolean z) {
        if (compoundButton.isChecked() != z) {
            compoundButton.setChecked(z);
        }
    }

    @BindingAdapter(requireAll = false, value = {"android:onCheckedChanged", "android:checkedAttrChanged"})
    public static void setListeners(CompoundButton compoundButton, final CompoundButton.OnCheckedChangeListener onCheckedChangeListener, final InverseBindingListener inverseBindingListener) {
        if (inverseBindingListener == null) {
            compoundButton.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            compoundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: android.databinding.adapters.CompoundButtonBindingAdapter.1
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public void onCheckedChanged(CompoundButton compoundButton2, boolean z) {
                    CompoundButton.OnCheckedChangeListener onCheckedChangeListener2 = onCheckedChangeListener;
                    if (onCheckedChangeListener2 != null) {
                        onCheckedChangeListener2.onCheckedChanged(compoundButton2, z);
                    }
                    inverseBindingListener.onChange();
                }
            });
        }
    }
}
