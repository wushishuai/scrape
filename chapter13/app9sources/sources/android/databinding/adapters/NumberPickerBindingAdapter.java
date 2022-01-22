package android.databinding.adapters;

import android.databinding.BindingAdapter;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.support.annotation.RestrictTo;
import android.widget.NumberPicker;

@BindingMethods({@BindingMethod(attribute = "android:format", method = "setFormatter", type = NumberPicker.class), @BindingMethod(attribute = "android:onScrollStateChange", method = "setOnScrollListener", type = NumberPicker.class)})
@InverseBindingMethods({@InverseBindingMethod(attribute = "android:value", type = NumberPicker.class)})
@RestrictTo({RestrictTo.Scope.LIBRARY})
/* loaded from: classes.dex */
public class NumberPickerBindingAdapter {
    @BindingAdapter({"android:value"})
    public static void setValue(NumberPicker numberPicker, int i) {
        if (numberPicker.getValue() != i) {
            numberPicker.setValue(i);
        }
    }

    @BindingAdapter(requireAll = false, value = {"android:onValueChange", "android:valueAttrChanged"})
    public static void setListeners(NumberPicker numberPicker, final NumberPicker.OnValueChangeListener onValueChangeListener, final InverseBindingListener inverseBindingListener) {
        if (inverseBindingListener == null) {
            numberPicker.setOnValueChangedListener(onValueChangeListener);
        } else {
            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() { // from class: android.databinding.adapters.NumberPickerBindingAdapter.1
                @Override // android.widget.NumberPicker.OnValueChangeListener
                public void onValueChange(NumberPicker numberPicker2, int i, int i2) {
                    NumberPicker.OnValueChangeListener onValueChangeListener2 = onValueChangeListener;
                    if (onValueChangeListener2 != null) {
                        onValueChangeListener2.onValueChange(numberPicker2, i, i2);
                    }
                    inverseBindingListener.onChange();
                }
            });
        }
    }
}
