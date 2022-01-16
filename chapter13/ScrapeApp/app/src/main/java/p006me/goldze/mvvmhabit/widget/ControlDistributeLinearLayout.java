package p006me.goldze.mvvmhabit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import p006me.goldze.mvvmhabit.C0934R;

/* renamed from: me.goldze.mvvmhabit.widget.ControlDistributeLinearLayout */
/* loaded from: classes.dex */
public class ControlDistributeLinearLayout extends LinearLayout {
    private boolean isDistributeEvent;

    public ControlDistributeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.isDistributeEvent = false;
        this.isDistributeEvent = getContext().obtainStyledAttributes(attrs, C0934R.styleable.ControlDistributeLinearLayout).getBoolean(C0934R.styleable.ControlDistributeLinearLayout_distribute_event, false);
    }

    public ControlDistributeLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlDistributeLinearLayout(Context context) {
        this(context, null);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isDistributeEvent();
    }

    public boolean isDistributeEvent() {
        return this.isDistributeEvent;
    }

    public void setDistributeEvent(boolean distributeEvent) {
        this.isDistributeEvent = distributeEvent;
    }
}
