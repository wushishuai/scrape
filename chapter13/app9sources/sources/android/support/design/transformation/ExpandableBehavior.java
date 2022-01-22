package android.support.design.transformation;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.expandable.ExpandableWidget;
import android.support.design.widget.CoordinatorLayout;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import java.util.List;

/* loaded from: classes.dex */
public abstract class ExpandableBehavior extends CoordinatorLayout.Behavior<View> {
    private static final int STATE_COLLAPSED = 2;
    private static final int STATE_EXPANDED = 1;
    private static final int STATE_UNINITIALIZED = 0;
    private int currentState = 0;

    @Override // android.support.design.widget.CoordinatorLayout.Behavior
    public abstract boolean layoutDependsOn(CoordinatorLayout coordinatorLayout, View view, View view2);

    protected abstract boolean onExpandedStateChange(View view, View view2, boolean z, boolean z2);

    public ExpandableBehavior() {
    }

    public ExpandableBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // android.support.design.widget.CoordinatorLayout.Behavior
    @CallSuper
    public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, final View view, int i) {
        final ExpandableWidget findExpandableWidget;
        if (ViewCompat.isLaidOut(view) || (findExpandableWidget = findExpandableWidget(coordinatorLayout, view)) == null || !didStateChange(findExpandableWidget.isExpanded())) {
            return false;
        }
        this.currentState = findExpandableWidget.isExpanded() ? 1 : 2;
        final int i2 = this.currentState;
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: android.support.design.transformation.ExpandableBehavior.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                if (ExpandableBehavior.this.currentState == i2) {
                    ExpandableBehavior expandableBehavior = ExpandableBehavior.this;
                    ExpandableWidget expandableWidget = findExpandableWidget;
                    expandableBehavior.onExpandedStateChange((View) expandableWidget, view, expandableWidget.isExpanded(), false);
                }
                return false;
            }
        });
        return false;
    }

    @Override // android.support.design.widget.CoordinatorLayout.Behavior
    @CallSuper
    public boolean onDependentViewChanged(CoordinatorLayout coordinatorLayout, View view, View view2) {
        ExpandableWidget expandableWidget = (ExpandableWidget) view2;
        if (!didStateChange(expandableWidget.isExpanded())) {
            return false;
        }
        this.currentState = expandableWidget.isExpanded() ? 1 : 2;
        return onExpandedStateChange((View) expandableWidget, view, expandableWidget.isExpanded(), true);
    }

    @Nullable
    protected ExpandableWidget findExpandableWidget(CoordinatorLayout coordinatorLayout, View view) {
        List<View> dependencies = coordinatorLayout.getDependencies(view);
        int size = dependencies.size();
        for (int i = 0; i < size; i++) {
            View view2 = dependencies.get(i);
            if (layoutDependsOn(coordinatorLayout, view, view2)) {
                return (ExpandableWidget) view2;
            }
        }
        return null;
    }

    private boolean didStateChange(boolean z) {
        if (z) {
            int i = this.currentState;
            return i == 0 || i == 2;
        } else if (this.currentState == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static <T extends ExpandableBehavior> T from(View view, Class<T> cls) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
            if (behavior instanceof ExpandableBehavior) {
                return cls.cast(behavior);
            }
            throw new IllegalArgumentException("The view is not associated with ExpandableBehavior");
        }
        throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
}
