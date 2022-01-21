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
    public static void onClickCommand(View view, final BindingCommand clickCommand, boolean isThrottleFirst) {
        if (isThrottleFirst) {
            RxView.clicks(view).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.1
                @Override // p005io.reactivex.functions.Consumer
                public void accept(Object object) throws Exception {
                    BindingCommand bindingCommand = clickCommand;
                    if (bindingCommand != null) {
                        bindingCommand.execute();
                    }
                }
            });
        } else {
            RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.2
                @Override // p005io.reactivex.functions.Consumer
                public void accept(Object object) throws Exception {
                    BindingCommand bindingCommand = clickCommand;
                    if (bindingCommand != null) {
                        bindingCommand.execute();
                    }
                }
            });
        }
    }

    @BindingAdapter(requireAll = false, value = {"onLongClickCommand"})
    public static void onLongClickCommand(View view, final BindingCommand clickCommand) {
        RxView.longClicks(view).subscribe(new Consumer<Object>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.3
            @Override // p005io.reactivex.functions.Consumer
            public void accept(Object object) throws Exception {
                BindingCommand bindingCommand = clickCommand;
                if (bindingCommand != null) {
                    bindingCommand.execute();
                }
            }
        });
    }

    @BindingAdapter(requireAll = false, value = {"currentView"})
    public static void replyCurrentView(View currentView, BindingCommand bindingCommand) {
        if (bindingCommand != null) {
            bindingCommand.execute(currentView);
        }
    }

    @BindingAdapter({"requestFocus"})
    public static void requestFocusCommand(View view, Boolean needRequestFocus) {
        if (needRequestFocus.booleanValue()) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            return;
        }
        view.clearFocus();
    }

    @BindingAdapter({"onFocusChangeCommand"})
    public static void onFocusChangeCommand(View view, final BindingCommand<Boolean> onFocusChangeCommand) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter.4
            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View v, boolean hasFocus) {
                BindingCommand bindingCommand = onFocusChangeCommand;
                if (bindingCommand != null) {
                    bindingCommand.execute(Boolean.valueOf(hasFocus));
                }
            }
        });
    }

    @BindingAdapter(requireAll = false, value = {"isVisible"})
    public static void isVisible(View view, Boolean visibility) {
        if (visibility.booleanValue()) {
            view.setVisibility(0);
        } else {
            view.setVisibility(8);
        }
    }
}
