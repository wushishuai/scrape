package p006me.goldze.mvvmhabit.binding.viewadapter.view;

import android.databinding.BindingAdapter;
import android.view.View;
import com.jakewharton.rxbinding2.view.RxView;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.functions.Consumer;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    public static final int CLICK_INTERVAL = 1;

    @BindingAdapter(requireAll = false, value = {"onClickCommand", "isThrottleFirst"})
    public static void onClickCommand(View view, final BindingCommand bindingCommand, boolean z) {
        if (z) {
            RxView.clicks(view).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.1
                @Override // p005io.reactivex.functions.Consumer
                public void accept(Object obj) throws Exception {
                    BindingCommand bindingCommand2 = bindingCommand;
                    if (bindingCommand2 != null) {
                        bindingCommand2.execute();
                    }
                }
            });
        } else {
            RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.2
                @Override // p005io.reactivex.functions.Consumer
                public void accept(Object obj) throws Exception {
                    BindingCommand bindingCommand2 = bindingCommand;
                    if (bindingCommand2 != null) {
                        bindingCommand2.execute();
                    }
                }
            });
        }
    }

    @BindingAdapter(requireAll = false, value = {"onLongClickCommand"})
    public static void onLongClickCommand(View view, final BindingCommand bindingCommand) {
        RxView.longClicks(view).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.3
            @Override // p005io.reactivex.functions.Consumer
            public void accept(Object obj) throws Exception {
                BindingCommand bindingCommand2 = bindingCommand;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute();
                }
            }
        });
    }

    @BindingAdapter(requireAll = false, value = {"currentView"})
    public static void replyCurrentView(View view, BindingCommand bindingCommand) {
        if (bindingCommand != null) {
            bindingCommand.execute(view);
        }
    }

    @BindingAdapter({"requestFocus"})
    public static void requestFocusCommand(View view, Boolean bool) {
        if (bool.booleanValue()) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            return;
        }
        view.clearFocus();
    }

    @BindingAdapter({"onFocusChangeCommand"})
    public static void onFocusChangeCommand(View view, final BindingCommand<Boolean> bindingCommand) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.4
            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view2, boolean z) {
                BindingCommand bindingCommand2 = bindingCommand;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute(Boolean.valueOf(z));
                }
            }
        });
    }

    @BindingAdapter(requireAll = false, value = {"isVisible"})
    public static void isVisible(View view, Boolean bool) {
        if (bool.booleanValue()) {
            view.setVisibility(0);
        } else {
            view.setVisibility(8);
        }
    }
}
