package com.bumptech.glide.request.transition;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.Transition;
/* loaded from: classes.dex */
public abstract class BitmapContainerTransitionFactory<R> implements TransitionFactory<R> {
    private final TransitionFactory<Drawable> realFactory;

    protected abstract Bitmap getBitmap(R r);

    public BitmapContainerTransitionFactory(TransitionFactory<Drawable> realFactory) {
        this.realFactory = realFactory;
    }

    @Override // com.bumptech.glide.request.transition.TransitionFactory
    public Transition<R> build(DataSource dataSource, boolean isFirstResource) {
        return new BitmapGlideAnimation(this.realFactory.build(dataSource, isFirstResource));
    }

    /* loaded from: classes.dex */
    private final class BitmapGlideAnimation implements Transition<R> {
        private final Transition<Drawable> transition;

        BitmapGlideAnimation(Transition<Drawable> transition) {
            this.transition = transition;
        }

        @Override // com.bumptech.glide.request.transition.Transition
        public boolean transition(R current, Transition.ViewAdapter adapter) {
            return this.transition.transition(new BitmapDrawable(adapter.getView().getResources(), BitmapContainerTransitionFactory.this.getBitmap(current)), adapter);
        }
    }
}
