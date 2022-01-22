package p006me.goldze.mvvmhabit.binding.viewadapter.mswitch;

import android.databinding.BindingAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.mswitch.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter({"switchState"})
    public static void setSwitchState(Switch r0, boolean z) {
        r0.setChecked(z);
    }

    @BindingAdapter({"onCheckedChangeCommand"})
    public static void onCheckedChangeCommand(Switch r1, final BindingCommand<Boolean> bindingCommand) {
        if (bindingCommand != null) {
            r1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.mswitch.ViewAdapter.1
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    BindingCommand.this.execute(Boolean.valueOf(z));
                }
            });
        }
    }
}
