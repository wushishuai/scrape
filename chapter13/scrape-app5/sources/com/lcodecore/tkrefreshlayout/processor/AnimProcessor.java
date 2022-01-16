package com.lcodecore.tkrefreshlayout.processor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
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
    private TwinklingRefreshLayout.CoContext cp;
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
        public void onAnimationUpdate(ValueAnimator animation) {
            int height = ((Integer) animation.getAnimatedValue()).intValue();
            if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.cp.isEnableKeepIView()) {
                AnimProcessor.this.cp.getHeader().getLayoutParams().height = height;
                AnimProcessor.this.cp.getHeader().requestLayout();
                AnimProcessor.this.cp.getHeader().setTranslationY(0.0f);
                AnimProcessor.this.cp.onPullDownReleasing((float) height);
            } else {
                AnimProcessor.this.transHeader((float) height);
            }
            if (!AnimProcessor.this.cp.isOpenFloatRefresh()) {
                AnimProcessor.this.cp.getTargetView().setTranslationY((float) height);
                AnimProcessor.this.translateExHead(height);
            }
        }
    };
    private ValueAnimator.AnimatorUpdateListener animBottomUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.11
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            int height = ((Integer) animation.getAnimatedValue()).intValue();
            if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.cp.isEnableKeepIView()) {
                AnimProcessor.this.cp.getFooter().getLayoutParams().height = height;
                AnimProcessor.this.cp.getFooter().requestLayout();
                AnimProcessor.this.cp.getFooter().setTranslationY(0.0f);
                AnimProcessor.this.cp.onPullUpReleasing((float) height);
            } else {
                AnimProcessor.this.transFooter((float) height);
            }
            AnimProcessor.this.cp.getTargetView().setTranslationY((float) (-height));
        }
    };
    private ValueAnimator.AnimatorUpdateListener overScrollTopUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.12
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            int height = ((Integer) animation.getAnimatedValue()).intValue();
            if (AnimProcessor.this.cp.isOverScrollTopShow()) {
                if (AnimProcessor.this.cp.getHeader().getVisibility() != 0) {
                    AnimProcessor.this.cp.getHeader().setVisibility(0);
                }
            } else if (AnimProcessor.this.cp.getHeader().getVisibility() != 8) {
                AnimProcessor.this.cp.getHeader().setVisibility(8);
            }
            if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.cp.isEnableKeepIView()) {
                AnimProcessor.this.cp.getHeader().setTranslationY(0.0f);
                AnimProcessor.this.cp.getHeader().getLayoutParams().height = height;
                AnimProcessor.this.cp.getHeader().requestLayout();
                AnimProcessor.this.cp.onPullDownReleasing((float) height);
            } else {
                AnimProcessor.this.transHeader((float) height);
            }
            AnimProcessor.this.cp.getTargetView().setTranslationY((float) height);
            AnimProcessor.this.translateExHead(height);
        }
    };
    private ValueAnimator.AnimatorUpdateListener overScrollBottomUpListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.13
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            int height = ((Integer) animation.getAnimatedValue()).intValue();
            if (AnimProcessor.this.cp.isOverScrollBottomShow()) {
                if (AnimProcessor.this.cp.getFooter().getVisibility() != 0) {
                    AnimProcessor.this.cp.getFooter().setVisibility(0);
                }
            } else if (AnimProcessor.this.cp.getFooter().getVisibility() != 8) {
                AnimProcessor.this.cp.getFooter().setVisibility(8);
            }
            if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.cp.isEnableKeepIView()) {
                AnimProcessor.this.cp.getFooter().getLayoutParams().height = height;
                AnimProcessor.this.cp.getFooter().requestLayout();
                AnimProcessor.this.cp.getFooter().setTranslationY(0.0f);
                AnimProcessor.this.cp.onPullUpReleasing((float) height);
            } else {
                AnimProcessor.this.transFooter((float) height);
            }
            AnimProcessor.this.cp.getTargetView().setTranslationY((float) (-height));
        }
    };
    private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(8.0f);

    public AnimProcessor(TwinklingRefreshLayout.CoContext coProcessor) {
        this.cp = coProcessor;
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void scrollHeadByMove(float moveY) {
        float offsetY = (this.decelerateInterpolator.getInterpolation((moveY / this.cp.getMaxHeadHeight()) / 2.0f) * moveY) / 2.0f;
        if (this.cp.isPureScrollModeOn() || (!this.cp.enableRefresh() && !this.cp.isOverScrollTopShow())) {
            if (this.cp.getHeader().getVisibility() != 8) {
                this.cp.getHeader().setVisibility(8);
            }
        } else if (this.cp.getHeader().getVisibility() != 0) {
            this.cp.getHeader().setVisibility(0);
        }
        if (!this.scrollHeadLocked || !this.cp.isEnableKeepIView()) {
            this.cp.getHeader().setTranslationY(0.0f);
            this.cp.getHeader().getLayoutParams().height = (int) Math.abs(offsetY);
            this.cp.getHeader().requestLayout();
            this.cp.onPullingDown(offsetY);
        } else {
            this.cp.getHeader().setTranslationY(offsetY - ((float) this.cp.getHeader().getLayoutParams().height));
        }
        if (!this.cp.isOpenFloatRefresh()) {
            this.cp.getTargetView().setTranslationY(offsetY);
            translateExHead((int) offsetY);
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void scrollBottomByMove(float moveY) {
        float offsetY = (this.decelerateInterpolator.getInterpolation((moveY / ((float) this.cp.getMaxBottomHeight())) / 2.0f) * moveY) / 2.0f;
        if (this.cp.isPureScrollModeOn() || (!this.cp.enableLoadmore() && !this.cp.isOverScrollBottomShow())) {
            if (this.cp.getFooter().getVisibility() != 8) {
                this.cp.getFooter().setVisibility(8);
            }
        } else if (this.cp.getFooter().getVisibility() != 0) {
            this.cp.getFooter().setVisibility(0);
        }
        if (!this.scrollBottomLocked || !this.cp.isEnableKeepIView()) {
            this.cp.getFooter().setTranslationY(0.0f);
            this.cp.getFooter().getLayoutParams().height = (int) Math.abs(offsetY);
            this.cp.getFooter().requestLayout();
            this.cp.onPullingUp(-offsetY);
        } else {
            this.cp.getFooter().setTranslationY(((float) this.cp.getFooter().getLayoutParams().height) - offsetY);
        }
        this.cp.getTargetView().setTranslationY(-offsetY);
    }

    public void dealPullDownRelease() {
        if (this.cp.isPureScrollModeOn() || !this.cp.enableRefresh() || getVisibleHeadHeight() < this.cp.getHeadHeight() - this.cp.getTouchSlop()) {
            animHeadBack(false);
        } else {
            animHeadToRefresh();
        }
    }

    public void dealPullUpRelease() {
        if (this.cp.isPureScrollModeOn() || !this.cp.enableLoadmore() || getVisibleFootHeight() < this.cp.getBottomHeight() - this.cp.getTouchSlop()) {
            animBottomBack(false);
        } else {
            animBottomToLoad();
        }
    }

    private int getVisibleHeadHeight() {
        LogUtil.i("header translationY:" + this.cp.getHeader().getTranslationY() + ",Visible head height:" + (((float) this.cp.getHeader().getLayoutParams().height) + this.cp.getHeader().getTranslationY()));
        return (int) (((float) this.cp.getHeader().getLayoutParams().height) + this.cp.getHeader().getTranslationY());
    }

    public int getVisibleFootHeight() {
        LogUtil.i("footer translationY:" + this.cp.getFooter().getTranslationY() + "");
        return (int) (((float) this.cp.getFooter().getLayoutParams().height) - this.cp.getFooter().getTranslationY());
    }

    public void transHeader(float offsetY) {
        this.cp.getHeader().setTranslationY(offsetY - ((float) this.cp.getHeader().getLayoutParams().height));
    }

    public void transFooter(float offsetY) {
        this.cp.getFooter().setTranslationY(((float) this.cp.getFooter().getLayoutParams().height) - offsetY);
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadToRefresh() {
        LogUtil.i("animHeadToRefresh:");
        this.isAnimHeadToRefresh = true;
        animLayoutByTime(getVisibleHeadHeight(), this.cp.getHeadHeight(), this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                AnimProcessor.this.isAnimHeadToRefresh = false;
                if (AnimProcessor.this.cp.getHeader().getVisibility() != 0) {
                    AnimProcessor.this.cp.getHeader().setVisibility(0);
                }
                AnimProcessor.this.cp.setRefreshVisible(true);
                if (!AnimProcessor.this.cp.isEnableKeepIView()) {
                    AnimProcessor.this.cp.setRefreshing(true);
                    AnimProcessor.this.cp.onRefresh();
                } else if (!AnimProcessor.this.scrollHeadLocked) {
                    AnimProcessor.this.cp.setRefreshing(true);
                    AnimProcessor.this.cp.onRefresh();
                    AnimProcessor.this.scrollHeadLocked = true;
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadBack(final boolean isFinishRefresh) {
        LogUtil.i("animHeadBack：finishRefresh?->" + isFinishRefresh);
        this.isAnimHeadBack = true;
        if (isFinishRefresh && this.scrollHeadLocked && this.cp.isEnableKeepIView()) {
            this.cp.setPrepareFinishRefresh(true);
        }
        animLayoutByTime(getVisibleHeadHeight(), 0, this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                AnimProcessor.this.isAnimHeadBack = false;
                AnimProcessor.this.cp.setRefreshVisible(false);
                if (isFinishRefresh && AnimProcessor.this.scrollHeadLocked && AnimProcessor.this.cp.isEnableKeepIView()) {
                    AnimProcessor.this.cp.getHeader().getLayoutParams().height = 0;
                    AnimProcessor.this.cp.getHeader().requestLayout();
                    AnimProcessor.this.cp.getHeader().setTranslationY(0.0f);
                    AnimProcessor.this.scrollHeadLocked = false;
                    AnimProcessor.this.cp.setRefreshing(false);
                    AnimProcessor.this.cp.resetHeaderView();
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomToLoad() {
        LogUtil.i("animBottomToLoad");
        this.isAnimBottomToLoad = true;
        animLayoutByTime(getVisibleFootHeight(), this.cp.getBottomHeight(), this.animBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                AnimProcessor.this.isAnimBottomToLoad = false;
                if (AnimProcessor.this.cp.getFooter().getVisibility() != 0) {
                    AnimProcessor.this.cp.getFooter().setVisibility(0);
                }
                AnimProcessor.this.cp.setLoadVisible(true);
                if (!AnimProcessor.this.cp.isEnableKeepIView()) {
                    AnimProcessor.this.cp.setLoadingMore(true);
                    AnimProcessor.this.cp.onLoadMore();
                } else if (!AnimProcessor.this.scrollBottomLocked) {
                    AnimProcessor.this.cp.setLoadingMore(true);
                    AnimProcessor.this.cp.onLoadMore();
                    AnimProcessor.this.scrollBottomLocked = true;
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomBack(final boolean isFinishLoading) {
        LogUtil.i("animBottomBack：finishLoading?->" + isFinishLoading);
        this.isAnimBottomBack = true;
        if (isFinishLoading && this.scrollBottomLocked && this.cp.isEnableKeepIView()) {
            this.cp.setPrepareFinishLoadMore(true);
        }
        animLayoutByTime(getVisibleFootHeight(), 0, new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                int dy;
                int height = ((Integer) animation.getAnimatedValue()).intValue();
                if (!ScrollingUtil.isViewToBottom(AnimProcessor.this.cp.getTargetView(), AnimProcessor.this.cp.getTouchSlop()) && (dy = AnimProcessor.this.getVisibleFootHeight() - height) > 0) {
                    if (AnimProcessor.this.cp.getTargetView() instanceof RecyclerView) {
                        ScrollingUtil.scrollAViewBy(AnimProcessor.this.cp.getTargetView(), dy);
                    } else {
                        ScrollingUtil.scrollAViewBy(AnimProcessor.this.cp.getTargetView(), dy / 2);
                    }
                }
                AnimProcessor.this.animBottomUpListener.onAnimationUpdate(animation);
            }
        }, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                AnimProcessor.this.isAnimBottomBack = false;
                AnimProcessor.this.cp.setLoadVisible(false);
                if (isFinishLoading && AnimProcessor.this.scrollBottomLocked && AnimProcessor.this.cp.isEnableKeepIView()) {
                    AnimProcessor.this.cp.getFooter().getLayoutParams().height = 0;
                    AnimProcessor.this.cp.getFooter().requestLayout();
                    AnimProcessor.this.cp.getFooter().setTranslationY(0.0f);
                    AnimProcessor.this.scrollBottomLocked = false;
                    AnimProcessor.this.cp.resetBottomView();
                    AnimProcessor.this.cp.setLoadingMore(false);
                }
            }
        });
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animHeadHideByVy(int vy) {
        if (!this.isAnimHeadHide) {
            this.isAnimHeadHide = true;
            LogUtil.i("animHeadHideByVy：vy->" + vy);
            int vy2 = Math.abs(vy);
            if (vy2 < 5000) {
                vy2 = 8000;
            }
            animLayoutByTime(getVisibleHeadHeight(), 0, (long) (Math.abs((getVisibleHeadHeight() * 1000) / vy2) * 5), this.animHeadUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.6
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    AnimProcessor.this.isAnimHeadHide = false;
                    AnimProcessor.this.cp.setRefreshVisible(false);
                    if (!AnimProcessor.this.cp.isEnableKeepIView()) {
                        AnimProcessor.this.cp.setRefreshing(false);
                        AnimProcessor.this.cp.onRefreshCanceled();
                        AnimProcessor.this.cp.resetHeaderView();
                    }
                }
            });
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimRefresh
    public void animBottomHideByVy(int vy) {
        LogUtil.i("animBottomHideByVy：vy->" + vy);
        if (!this.isAnimBottomHide) {
            this.isAnimBottomHide = true;
            int vy2 = Math.abs(vy);
            if (vy2 < 5000) {
                vy2 = 8000;
            }
            animLayoutByTime(getVisibleFootHeight(), 0, (long) (((getVisibleFootHeight() * 5) * 1000) / vy2), this.animBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.7
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    AnimProcessor.this.isAnimBottomHide = false;
                    AnimProcessor.this.cp.setLoadVisible(false);
                    if (!AnimProcessor.this.cp.isEnableKeepIView()) {
                        AnimProcessor.this.cp.setLoadingMore(false);
                        AnimProcessor.this.cp.onLoadmoreCanceled();
                        AnimProcessor.this.cp.resetBottomView();
                    }
                }
            });
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IAnimOverScroll
    public void animOverScrollTop(float vy, int computeTimes) {
        final int time;
        LogUtil.i("animOverScrollTop：vy->" + vy + ",computeTimes->" + computeTimes);
        if (!this.isOverScrollTopLocked) {
            this.isOverScrollTopLocked = true;
            this.isAnimOsTop = true;
            this.cp.setStatePTD();
            int oh = (int) Math.abs((vy / ((float) computeTimes)) / 2.0f);
            final int overHeight = oh > this.cp.getOsHeight() ? this.cp.getOsHeight() : oh;
            if (overHeight <= 50) {
                time = 115;
            } else {
                double d = (double) overHeight;
                Double.isNaN(d);
                time = (int) ((d * 0.3d) + 100.0d);
            }
            animLayoutByTime(getVisibleHeadHeight(), overHeight, (long) time, this.overScrollTopUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.8
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    if (!AnimProcessor.this.scrollHeadLocked || !AnimProcessor.this.cp.isEnableKeepIView() || !AnimProcessor.this.cp.showRefreshingWhenOverScroll()) {
                        AnimProcessor animProcessor = AnimProcessor.this;
                        animProcessor.animLayoutByTime(overHeight, 0, (long) (time * 2), animProcessor.overScrollTopUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.8.1
                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animation2) {
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
    public void animOverScrollBottom(float vy, int computeTimes) {
        final int time;
        LogUtil.i("animOverScrollBottom：vy->" + vy + ",computeTimes->" + computeTimes);
        if (!this.isOverScrollBottomLocked) {
            this.cp.setStatePBU();
            int oh = (int) Math.abs((vy / ((float) computeTimes)) / 2.0f);
            final int overHeight = oh > this.cp.getOsHeight() ? this.cp.getOsHeight() : oh;
            if (overHeight <= 50) {
                time = 115;
            } else {
                double d = (double) overHeight;
                Double.isNaN(d);
                time = (int) ((d * 0.3d) + 100.0d);
            }
            if (this.scrollBottomLocked || !this.cp.autoLoadMore()) {
                this.isOverScrollBottomLocked = true;
                this.isAnimOsBottom = true;
                animLayoutByTime(0, overHeight, (long) time, this.overScrollBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.9
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animation) {
                        if (!AnimProcessor.this.scrollBottomLocked || !AnimProcessor.this.cp.isEnableKeepIView() || !AnimProcessor.this.cp.showLoadingWhenOverScroll()) {
                            AnimProcessor animProcessor = AnimProcessor.this;
                            animProcessor.animLayoutByTime(overHeight, 0, (long) (time * 2), animProcessor.overScrollBottomUpListener, new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.processor.AnimProcessor.9.1
                                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                public void onAnimationEnd(Animator animation2) {
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
            this.cp.startLoadMore();
        }
    }

    public void translateExHead(int offsetY) {
        if (!this.cp.isExHeadLocked()) {
            this.cp.getExHead().setTranslationY((float) offsetY);
        }
    }

    public void animLayoutByTime(int start, int end, long time, ValueAnimator.AnimatorUpdateListener listener, Animator.AnimatorListener animatorListener) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(listener);
        va.addListener(animatorListener);
        va.setDuration(time);
        va.start();
    }

    public void animLayoutByTime(int start, int end, long time, ValueAnimator.AnimatorUpdateListener listener) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(listener);
        va.setDuration(time);
        va.start();
    }

    public void animLayoutByTime(int start, int end, ValueAnimator.AnimatorUpdateListener listener, Animator.AnimatorListener animatorListener) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(listener);
        va.addListener(animatorListener);
        va.setDuration((long) ((int) (((float) Math.abs(start - end)) * animFraction)));
        va.start();
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
                public void onAnimationStart(Animator animation) {
                    this.startTime = System.currentTimeMillis();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
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
