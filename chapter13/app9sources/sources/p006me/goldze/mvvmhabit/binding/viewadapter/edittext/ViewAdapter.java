package p006me.goldze.mvvmhabit.binding.viewadapter.edittext;

import android.databinding.BindingAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.edittext.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"requestFocus"})
    public static void requestFocusCommand(EditText editText, Boolean bool) {
        if (bool.booleanValue()) {
            editText.setSelection(editText.getText().length());
            editText.requestFocus();
            ((InputMethodManager) editText.getContext().getSystemService("input_method")).showSoftInput(editText, 1);
        }
        editText.setFocusableInTouchMode(bool.booleanValue());
    }

    @BindingAdapter(requireAll = false, value = {"textChanged"})
    public static void addTextChangedListener(EditText editText, final BindingCommand<String> bindingCommand) {
        editText.addTextChangedListener(new TextWatcher() { // from class: me.goldze.mvvmhabit.binding.viewadapter.edittext.ViewAdapter.1
            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                BindingCommand bindingCommand2 = BindingCommand.this;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute(charSequence.toString());
                }
            }
        });
    }
}
