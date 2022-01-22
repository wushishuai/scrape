package p006me.goldze.mvvmhabit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import p006me.goldze.mvvmhabit.C0933R;

/* renamed from: me.goldze.mvvmhabit.widget.ControlDistributeLinearLayout */
/* loaded from: classes.dex */
public class ControlDistributeLinearLayout extends LinearLayout {
    private boolean isDistributeEvent;

    public ControlDistributeLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.isDistributeEvent = false;
        this.isDistributeEvent = getContext().obtainStyledAttributes(attributeSet, C0933R.styleable.ControlDistributeLinearLayout).getBoolean(C0933R.styleable.ControlDistributeLinearLayout_distribute_event, false);
    }

    public ControlDistributeLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ControlDistributeLinearLayout(Context context) {
        this(context, null);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return isDistributeEvent();
    }

    public boolean isDistributeEvent() {
        return this.isDistributeEvent;
    }

    public void setDistributeEvent(boolean z) {
        this.isDistributeEvent = z;
    }
}
