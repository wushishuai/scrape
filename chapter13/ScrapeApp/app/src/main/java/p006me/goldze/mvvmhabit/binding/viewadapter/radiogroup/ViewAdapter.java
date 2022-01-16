package p006me.goldze.mvvmhabit.binding.viewadapter.radiogroup;

import android.databinding.BindingAdapter;
import android.support.annotation.IdRes;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.radiogroup.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"onCheckedChangedCommand"})
    public static void onCheckedChangedCommand(RadioGroup radioGroup, final BindingCommand<String> bindingCommand) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.radiogroup.ViewAdapter.1
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                BindingCommand.this.execute(((RadioButton) group.findViewById(checkedId)).getText().toString());
            }
        });
    }
}
