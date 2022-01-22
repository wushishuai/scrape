package p006me.goldze.mvvmhabit.binding.viewadapter.spinner;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import java.util.ArrayList;
import java.util.List;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.spinner.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"itemDatas", "valueReply", "resource", "dropDownResource", "onItemSelectedCommand"})
    public static void onItemSelectedCommand(Spinner spinner, final List<IKeyAndValue> list, String str, int i, int i2, final BindingCommand<IKeyAndValue> bindingCommand) {
        if (list != null) {
            ArrayList arrayList = new ArrayList();
            for (IKeyAndValue iKeyAndValue : list) {
                arrayList.add(iKeyAndValue.getKey());
            }
            if (i == 0) {
                i = 17367048;
            }
            if (i2 == 0) {
                i2 = 17367049;
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(spinner.getContext(), i, arrayList);
            arrayAdapter.setDropDownViewResource(i2);
            spinner.setAdapter((SpinnerAdapter) arrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.spinner.ViewAdapter.1
                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onNothingSelected(AdapterView<?> adapterView) {
                }

                @Override // android.widget.AdapterView.OnItemSelectedListener
                public void onItemSelected(AdapterView<?> adapterView, View view, int i3, long j) {
                    bindingCommand.execute((IKeyAndValue) list.get(i3));
                }
            });
            if (!TextUtils.isEmpty(str)) {
                for (int i3 = 0; i3 < list.size(); i3++) {
                    if (str.equals(list.get(i3).getValue())) {
                        spinner.setSelection(i3);
                        return;
                    }
                }
                return;
            }
            return;
        }
        throw new NullPointerException("this itemDatas parameter is null");
    }
}
