package com.bumptech.glide.request.transition;

import android.view.View;
import com.bumptech.glide.request.transition.Transition;

/* loaded from: classes.dex */
public class ViewPropertyTransition<R> implements Transition<R> {
    private final Animator animator;

    /* loaded from: classes.dex */
    public interface Animator {
        void animate(View view);
    }

    public ViewPropertyTransition(Animator animator) {
        this.animator = animator;
    }

    @Override // com.bumptech.glide.request.transition.Transition
    public boolean transition(R current, Transition.ViewAdapter adapter) {
        if (adapter.getView() == null) {
            return false;
        }
        this.animator.animate(adapter.getView());
        return false;
    }
}
