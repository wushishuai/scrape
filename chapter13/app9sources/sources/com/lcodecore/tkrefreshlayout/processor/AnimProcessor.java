package com.lcodecore.tkrefreshlayout.processor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.p003v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.utils.LogUtil;
import com.lcodecore.tkrefreshlayout.utils.ScrollingUtil;
import java.io.PrintStream;
import java.util.LinkedList;

/* loaded from: classes.dex */
public class AnimProcessor implements IAnimRefresh, IAnimOverScroll {
    private static final float animFraction = 1.0f;
    private LinkedList<Animator> animQueue;

    /* renamed from: cp */
    private TwinklingRefreshLayout.CoContext f84cp;
    private boolean scrollHeadLocked = false;
    private boolean scrollBottomLocked = false;
    private boolean isAnimHeadToRefresh = false;
    private boolean isAnimHeadBack = false;
    private boolean isAnimBottomToLoad = false;
    private boolean isAnimBottomBack = false;
    private boolean isAnimHeadHide = false;
    private boolean isAnimBottomHide = false;
    private boolean isAnimOsTop = false;
    private boolean isOverScrollTopLocked = false;
    private boolean isAnimOsBottom = false;
    private boolean isOverScrollBottomLocked = false;
    private ValueAnimator.AnimatorUpdateListener animHeadUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.10
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.f84cp.isEnableKeepIView()) {
                AnimProcessor.this.f84cp.getHeader().getLayoutParams().height = intValue;
                AnimProcessor.this.f84cp.getHeader().requestLayout();
                AnimProcessor.this.f84cp.getHeader().setTranslationY(0.0f);
                AnimProcessor.this.f84cp.onPullDownReleasing((float) intValue);
            } else {
                AnimProcessor.this.transHeader((float) intValue);
            }
            if (!AnimProcessor.this.f84cp.isOpenFloatRefresh()) {
                AnimProcessor.this.f84cp.getTargetView().setTranslationY((float) intValue);
                AnimProcessor.this.translateExHead(intValue);
            }
        }
    };
    private ValueAnimator.AnimatorUpdateListener animBottomUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.11
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.f84cp.isEnableKeepIView()) {
                AnimProcessor.this.f84cp.getFooter().getLayoutParams().height = intValue;
                AnimProcessor.this.f84cp.getFooter().requestLayout();
                AnimProcessor.this.f84cp.getFooter().setTranslationY(0.0f);
                AnimProcessor.this.f84cp.onPullUpReleasing((float) intValue);
            } else {
                AnimProcessor.this.transFooter((float) intValue);
            }
            AnimProcessor.this.f84cp.getTargetView().setTranslationY((float) (-intValue));
        }
    };
    private ValueAnimator.AnimatorUpdateListener overScrollTopUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.12
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            if (AnimProcessor.this.f84cp.isOverScrollTopShow()) {
                if (AnimProcessor.this.f84cp.getHeader().getVisibility() != 0) {
                    AnimProcessor.this.f84cp.getHeader().setVisibility(0);
                }
            } else if (AnimProcessor.this.f84cp.getHeader().getVisibility() != 8) {
                AnimProcessor.this.f84cp.getHeader().setVisibility(8);
            }
            if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.f84cp.isEnableKeepIView()) {
                AnimProcessor.this.f84cp.getHeader().setTranslationY(0.0f);
                AnimProcessor.this.f84cp.getHeader().getLayoutParams().height = intValue;
                AnimProcessor.this.f84cp.getHeader().requestLayout();
                AnimProcessor.this.f84cp.onPullDownReleasing((float) intValue);
            } else {
                AnimProcessor.this.transHeader((float) intValue);
            }
            AnimProcessor.this.f84cp.getTargetView().setTranslationY((float) intValue);
            AnimProcessor.this.translateExHead(intValue);
        }
    };
    private ValueAnimator.AnimatorUpdateListener overScrollBottomUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.13
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            if (AnimProcessor.this.f84cp.isOverScrollBottomShow()) {
                if (AnimProcessor.this.f84cp.getFooter().getVisibility() != 0) {
                    AnimProcessor.this.f84cp.getFooter().setVisibility(0);
                }
            } else if (AnimProcessor.this.f84cp.getFooter().getVisibility() != 8) {
                AnimProcessor.this.f84cp.getFooter().setVisibility(8);
            }
            if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.f84cp.isEnableKeepIView()) {
                AnimProcessor.this.f84cp.getFooter().getLayoutParams().height = intValue;
                AnimProcessor.this.f84cp.getFooter().requestLayout();
                AnimProcessor.this.f84cp.getFooter().setTranslationY(0.0f);
                AnimProcessor.this.f84cp.onPullUpReleasing((float) intValue);
            } else {
                AnimProcessor.this.transFooter((float) intValue);
            }
            AnimProcessor.this.f84cp.getTargetView().setTranslationY((float) (-intValue));
        }
    };
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(8.0f);

    public AnimProcessor(TwinklingRefreshLayout.CoContext coContext) {
        this.f84cp = coContext;
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void scrollHeadByMove(float f) {
        float interpolation = (this.decelerateInterpolator.getInterpolation((f / this.f84cp.getMaxHeadHeight()) / 2.0f) * f) / 2.0f;
        if (this.f84cp.isPureScrollModeOn() || (!this.f84cp.enableRefresh() && !this.f84cp.isOverScrollTopShow())) {
            if (this.f84cp.getHeader().getVisibility() != 8) {
                this.f84cp.getHeader().setVisibility(8);
            }
        } else if (this.f84cp.getHeader().getVisibility() != 0) {
            this.f84cp.getHeader().setVisibility(0);
        }
        if (!this.scrollHeadLocked || !this.f84cp.isEnableKeepIView()) {
            this.f84cp.getHeader().setTranslationY(0.0f);
            this.f84cp.getHeader().getLayoutParams().height = (int) Math.abs(interpolation);
            this.f84cp.getHeader().requestLayout();
            this.f84cp.onPullingDown(interpolation);
        } else {
            this.f84cp.getHeader().setTranslationY(interpolation - ((float) this.f84cp.getHeader().getLayoutParams().height));
        }
        if (!this.f84cp.isOpenFloatRefresh()) {
            this.f84cp.getTargetView().setTranslationY(interpolation);
            translateExHead((int) interpolation);
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void scrollBottomByMove(float f) {
        float interpolation = (this.decelerateInterpolator.getInterpolation((f / ((float) this.f84cp.getMaxBottomHeight())) / 2.0f) * f) / 2.0f;
        if (this.f84cp.isPureScrollModeOn() || (!this.f84cp.enableLoadmore() && !this.f84cp.isOverScrollBottomShow())) {
            if (this.f84cp.getFooter().getVisibility() != 8) {
                this.f84cp.getFooter().setVisibility(8);
            }
        } else if (this.f84cp.getFooter().getVisibility() != 0) {
            this.f84cp.getFooter().setVisibility(0);
        }
        if (!this.scrollBottomLocked || !this.f84cp.isEnableKeepIView()) {
            this.f84cp.getFooter().setTranslationY(0.0f);
            this.f84cp.getFooter().getLayoutParams().height = (int) Math.abs(interpolation);
            this.f84cp.getFooter().requestLayout();
            this.f84cp.onPullingUp(-interpolation);
        } else {
            this.f84cp.getFooter().setTranslationY(((float) this.f84cp.getFooter().getLayoutParams().height) - interpolation);
        }
        this.f84cp.getTargetView().setTranslationY(-interpolation);
    }

    public void dealPullDownRelease() {
        if (this.f84cp.isPureScrollModeOn() || !this.f84cp.enableRefresh() || getVisibleHeadHeight() < this.f84cp.getHeadHeight() - this.f84cp.getTouchSlop()) {
            animHeadBack(false);
        } else {
            animHeadToRefresh();
        }
    }

    public void dealPullUpRelease() {
        if (this.f84cp.isPureScrollModeOn() || !this.f84cp.enableLoadmore() || getVisibleFootHeight() < this.f84cp.getBottomHeight() - this.f84cp.getTouchSlop()) {
            animBottomBack(false);
        } else {
            animBottomToLoad();
        }
    }

    private int getVisibleHeadHeight() {
        LogUtil.m40i("header translationY:" + this.f84cp.getHeader().getTranslationY() + ",Visible head height:" + (((float) this.f84cp.getHeader().getLayoutParams().height) + this.f84cp.getHeader().getTranslationY()));
        return (int) (((float) this.f84cp.getHeader().getLayoutParams().height) + this.f84cp.getHeader().getTranslationY());
    }

    public int getVisibleFootHeight() {
        LogUtil.m40i("footer translationY:" + this.f84cp.getFooter().getTranslationY() + "");
        return (int) (((float) this.f84cp.getFooter().getLayoutParams().height) - this.f84cp.getFooter().getTranslationY());
    }

    public void transHeader(float f) {
        this.f84cp.getHeader().setTranslationY(f - ((float) this.f84cp.getHeader().getLayoutParams().height));
    }

    public void transFooter(float f) {
        this.f84cp.getFooter().setTranslationY(((float) this.f84cp.getFooter().getLayoutParams().height) - f);
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadToRefresh() {
        LogUtil.m40i("animHeadToRefresh:");
        this.isAnimHeadToRefresh = true;
        animLayoutByTime(getVisibleHeadHeight(), this.f84cp.getHeadHeight(), this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AnimProcessor.this.isAnimHeadToRefresh = false;
                if (AnimProcessor.this.f84cp.getHeader().getVisibility() != 0) {
                    AnimProcessor.this.f84cp.getHeader().setVisibility(0);
                }
                AnimProcessor.this.f84cp.setRefreshVisible(true);
                if (!AnimProcessor.this.f84cp.isEnableKeepIView()) {
                    AnimProcessor.this.f84cp.setRefreshing(true);
                    AnimProcessor.this.f84cp.onRefresh();
                } else if (!AnimProcessor.this.scrollHeadLocked) {
                    AnimProcessor.this.f84cp.setRefreshing(true);
                    AnimProcessor.this.f84cp.onRefresh();
                    AnimProcessor.this.scrollHeadLocked = true;
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadBack(final boolean z) {
        LogUtil.m40i("animHeadBack：finishRefresh?->" + z);
        this.isAnimHeadBack = true;
        if (z && this.scrollHeadLocked && this.f84cp.isEnableKeepIView()) {
            this.f84cp.setPrepareFinishRefresh(true);
        }
        animLayoutByTime(getVisibleHeadHeight(), 0, this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AnimProcessor.this.isAnimHeadBack = false;
                AnimProcessor.this.f84cp.setRefreshVisible(false);
                if (z && AnimProcessor.this.scrollHeadLocked && AnimProcessor.this.f84cp.isEnableKeepIView()) {
                    AnimProcessor.this.f84cp.getHeader().getLayoutParams().height = 0;
                    AnimProcessor.this.f84cp.getHeader().requestLayout();
                    AnimProcessor.this.f84cp.getHeader().setTranslationY(0.0f);
                    AnimProcessor.this.scrollHeadLocked = false;
                    AnimProcessor.this.f84cp.setRefreshing(false);
                    AnimProcessor.this.f84cp.resetHeaderView();
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomToLoad() {
        LogUtil.m40i("animBottomToLoad");
        this.isAnimBottomToLoad = true;
        animLayoutByTime(getVisibleFootHeight(), this.f84cp.getBottomHeight(), this.animBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AnimProcessor.this.isAnimBottomToLoad = false;
                if (AnimProcessor.this.f84cp.getFooter().getVisibility() != 0) {
                    AnimProcessor.this.f84cp.getFooter().setVisibility(0);
                }
                AnimProcessor.this.f84cp.setLoadVisible(true);
                if (!AnimProcessor.this.f84cp.isEnableKeepIView()) {
                    AnimProcessor.this.f84cp.setLoadingMore(true);
                    AnimProcessor.this.f84cp.onLoadMore();
                } else if (!AnimProcessor.this.scrollBottomLocked) {
                    AnimProcessor.this.f84cp.setLoadingMore(true);
                    AnimProcessor.this.f84cp.onLoadMore();
                    AnimProcessor.this.scrollBottomLocked = true;
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomBack(final boolean z) {
        LogUtil.m40i("animBottomBack：finishLoading?->" + z);
        this.isAnimBottomBack = true;
        if (z && this.scrollBottomLocked && this.f84cp.isEnableKeepIView()) {
            this.f84cp.setPrepareFinishLoadMore(true);
        }
        animLayoutByTime(getVisibleFootHeight(), 0, new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int visibleFootHeight;
                int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                if (!ScrollingUtil.isViewToBottom(AnimProcessor.this.f84cp.getTargetView(), AnimProcessor.this.f84cp.getTouchSlop()) && (visibleFootHeight = AnimProcessor.this.getVisibleFootHeight() - intValue) > 0) {
                    if (AnimProcessor.this.f84cp.getTargetView() instanceof RecyclerView) {
                        ScrollingUtil.scrollAViewBy(AnimProcessor.this.f84cp.getTargetView(), visibleFootHeight);
                    } else {
                        ScrollingUtil.scrollAViewBy(AnimProcessor.this.f84cp.getTargetView(), visibleFootHeight / 2);
                    }
                }
                AnimProcessor.this.animBottomUpListener.onAnimationUpdate(valueAnimator);
            }
        }, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AnimProcessor.this.isAnimBottomBack = false;
                AnimProcessor.this.f84cp.setLoadVisible(false);
                if (z && AnimProcessor.this.scrollBottomLocked && AnimProcessor.this.f84cp.isEnableKeepIView()) {
                    AnimProcessor.this.f84cp.getFooter().getLayoutParams().height = 0;
                    AnimProcessor.this.f84cp.getFooter().requestLayout();
                    AnimProcessor.this.f84cp.getFooter().setTranslationY(0.0f);
                    AnimProcessor.this.scrollBottomLocked = false;
                    AnimProcessor.this.f84cp.resetBottomView();
                    AnimProcessor.this.f84cp.setLoadingMore(false);
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadHideByVy(int i) {
        if (!this.isAnimHeadHide) {
            this.isAnimHeadHide = true;
            LogUtil.m40i("animHeadHideByVy：vy->" + i);
            int abs = Math.abs(i);
            if (abs < 5000) {
                abs = 8000;
            }
            animLayoutByTime(getVisibleHeadHeight(), 0, (long) (Math.abs((getVisibleHeadHeight() * 1000) / abs) * 5), this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.6
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    AnimProcessor.this.isAnimHeadHide = false;
                    AnimProcessor.this.f84cp.setRefreshVisible(false);
                    if (!AnimProcessor.this.f84cp.isEnableKeepIView()) {
                        AnimProcessor.this.f84cp.setRefreshing(false);
                        AnimProcessor.this.f84cp.onRefreshCanceled();
                        AnimProcessor.this.f84cp.resetHeaderView();
                    }
                }
            });
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomHideByVy(int i) {
        LogUtil.m40i("animBottomHideByVy：vy->" + i);
        if (!this.isAnimBottomHide) {
            this.isAnimBottomHide = true;
            int abs = Math.abs(i);
            if (abs < 5000) {
                abs = 8000;
            }
            animLayoutByTime(getVisibleFootHeight(), 0, (long) (((getVisibleFootHeight() * 5) * 1000) / abs), this.animBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.7
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    AnimProcessor.this.isAnimBottomHide = false;
                    AnimProcessor.this.f84cp.setLoadVisible(false);
                    if (!AnimProcessor.this.f84cp.isEnableKeepIView()) {
                        AnimProcessor.this.f84cp.setLoadingMore(false);
                        AnimProcessor.this.f84cp.onLoadmoreCanceled();
                        AnimProcessor.this.f84cp.resetBottomView();
                    }
                }
            });
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimOverScroll
    public void animOverScrollTop(float f, int i) {
        final int i2;
        LogUtil.m40i("animOverScrollTop：vy->" + f + ",computeTimes->" + i);
        if (!this.isOverScrollTopLocked) {
            this.isOverScrollTopLocked = true;
            this.isAnimOsTop = true;
            this.f84cp.setStatePTD();
            final int abs = (int) Math.abs((f / ((float) i)) / 2.0f);
            if (abs > this.f84cp.getOsHeight()) {
                abs = this.f84cp.getOsHeight();
            }
            if (abs <= 50) {
                i2 = 115;
            } else {
                double d = (double) abs;
                Double.isNaN(d);
                i2 = (int) ((d * 0.3d) + 100.0d);
            }
            animLayoutByTime(getVisibleHeadHeight(), abs, (long) i2, this.overScrollTopUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.8
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.f84cp.isEnableKeepIView() || !AnimProcessor.this.f84cp.showRefreshingWhenOverScroll()) {
                        AnimProcessor animProcessor = AnimProcessor.this;
                        animProcessor.animLayoutByTime(abs, 0, (long) (i2 * 2), animProcessor.overScrollTopUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.8.1
                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator2) {
                                AnimProcessor.this.isAnimOsTop = false;
                                AnimProcessor.this.isOverScrollTopLocked = false;
                            }
                        });
                        return;
                    }
                    AnimProcessor.this.animHeadToRefresh();
                    AnimProcessor.this.isAnimOsTop = false;
                    AnimProcessor.this.isOverScrollTopLocked = false;
                }
            });
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimOverScroll
    public void animOverScrollBottom(float f, int i) {
        final int i2;
        LogUtil.m40i("animOverScrollBottom：vy->" + f + ",computeTimes->" + i);
        if (!this.isOverScrollBottomLocked) {
            this.f84cp.setStatePBU();
            final int abs = (int) Math.abs((f / ((float) i)) / 2.0f);
            if (abs > this.f84cp.getOsHeight()) {
                abs = this.f84cp.getOsHeight();
            }
            if (abs <= 50) {
                i2 = 115;
            } else {
                double d = (double) abs;
                Double.isNaN(d);
                i2 = (int) ((d * 0.3d) + 100.0d);
            }
            if (this.scrollBottomLocked || !this.f84cp.autoLoadMore()) {
                this.isOverScrollBottomLocked = true;
                this.isAnimOsBottom = true;
                animLayoutByTime(0, abs, (long) i2, this.overScrollBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.9
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.f84cp.isEnableKeepIView() || !AnimProcessor.this.f84cp.showLoadingWhenOverScroll()) {
                            AnimProcessor animProcessor = AnimProcessor.this;
                            animProcessor.animLayoutByTime(abs, 0, (long) (i2 * 2), animProcessor.overScrollBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.9.1
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator animator2) {
                                    AnimProcessor.this.isAnimOsBottom = false;
                                    AnimProcessor.this.isOverScrollBottomLocked = false;
                                }
                            });
                            return;
                        }
                        AnimProcessor.this.animBottomToLoad();
                        AnimProcessor.this.isAnimOsBottom = false;
                        AnimProcessor.this.isOverScrollBottomLocked = false;
                    }
                });
                return;
            }
            this.f84cp.startLoadMore();
        }
    }

    public void translateExHead(int i) {
        if (!this.f84cp.isExHeadLocked()) {
            this.f84cp.getExHead().setTranslationY((float) i);
        }
    }

    public void animLayoutByTime(int i, int i2, long j, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        ValueAnimator ofInt = ValueAnimator.ofInt(i, i2);
        ofInt.setInterpolator(new DecelerateInterpolator());
        ofInt.addUpdateListener(animatorUpdateListener);
        ofInt.addListener(animatorListener);
        ofInt.setDuration(j);
        ofInt.start();
    }

    public void animLayoutByTime(int i, int i2, long j, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        ValueAnimator ofInt = ValueAnimator.ofInt(i, i2);
        ofInt.setInterpolator(new DecelerateInterpolator());
        ofInt.addUpdateListener(animatorUpdateListener);
        ofInt.setDuration(j);
        ofInt.start();
    }

    public void animLayoutByTime(int i, int i2, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        ValueAnimator ofInt = ValueAnimator.ofInt(i, i2);
        ofInt.setInterpolator(new DecelerateInterpolator());
        ofInt.addUpdateListener(animatorUpdateListener);
        ofInt.addListener(animatorListener);
        ofInt.setDuration((long) ((int) (((float) Math.abs(i - i2)) * animFraction)));
        ofInt.start();
    }

    private void offerToQueue(Animator animator) {
        if (animator != null) {
            if (this.animQueue == null) {
                this.animQueue = new LinkedList<>();
            }
            this.animQueue.offer(animator);
            PrintStream printStream = System.out;
            printStream.println("Current Animators：" + this.animQueue.size());
            animator.addListener(new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.14
                long startTime = 0;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator2) {
                    this.startTime = System.currentTimeMillis();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    AnimProcessor.this.animQueue.poll();
                    if (AnimProcessor.this.animQueue.size() > 0) {
                        ((Animator) AnimProcessor.this.animQueue.getFirst()).start();
                    }
                    PrintStream printStream2 = System.out;
                    printStream2.println("Anim end：start time->" + this.startTime + ",elapsed time->" + (System.currentTimeMillis() - this.startTime));
                }
            });
            if (this.animQueue.size() == 1) {
                animator.start();
            }
        }
    }
}
