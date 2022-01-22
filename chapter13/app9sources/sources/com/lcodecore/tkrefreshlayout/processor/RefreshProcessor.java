package com.lcodecore.tkrefreshlayout.processor;

import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.utils.ScrollingUtil;

/* loaded from: classes.dex */
public class RefreshProcessor implements IDecorator {

    /* renamed from: cp */
    protected TwinklingRefreshLayout.CoContext f86cp;
    private MotionEvent mLastMoveEvent;
    private float mTouchX;
    private float mTouchY;
    private boolean intercepted = false;
    private boolean willAnimHead = false;
    private boolean willAnimBottom = false;
    private boolean downEventSent = false;

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public boolean dealTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public boolean interceptTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public void onFingerDown(MotionEvent motionEvent) {
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public void onFingerFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
    }

    public RefreshProcessor(TwinklingRefreshLayout.CoContext coContext) {
        if (coContext != null) {
            this.f86cp = coContext;
            return;
        }
        throw new NullPointerException("The coprocessor can not be null.");
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.downEventSent = false;
                this.intercepted = false;
                this.mTouchX = motionEvent.getX();
                this.mTouchY = motionEvent.getY();
                if (this.f86cp.isEnableKeepIView()) {
                    if (!this.f86cp.isRefreshing()) {
                        this.f86cp.setPrepareFinishRefresh(false);
                    }
                    if (!this.f86cp.isLoadingMore()) {
                        this.f86cp.setPrepareFinishLoadMore(false);
                    }
                }
                this.f86cp.dispatchTouchEventSuper(motionEvent);
                return true;
            case 1:
            case 3:
                if (this.intercepted) {
                    if (this.f86cp.isStatePTD()) {
                        this.willAnimHead = true;
                    } else if (this.f86cp.isStatePBU()) {
                        this.willAnimBottom = true;
                    }
                    this.intercepted = false;
                    return true;
                }
                break;
            case 2:
                this.mLastMoveEvent = motionEvent;
                float x = motionEvent.getX() - this.mTouchX;
                float y = motionEvent.getY() - this.mTouchY;
                if (!this.intercepted && Math.abs(x) <= Math.abs(y) && Math.abs(y) > ((float) this.f86cp.getTouchSlop())) {
                    if (y > 0.0f && ScrollingUtil.isViewToTop(this.f86cp.getTargetView(), this.f86cp.getTouchSlop()) && this.f86cp.allowPullDown()) {
                        this.f86cp.setStatePTD();
                        this.mTouchX = motionEvent.getX();
                        this.mTouchY = motionEvent.getY();
                        sendCancelEvent();
                        this.intercepted = true;
                        return true;
                    } else if (y < 0.0f && ScrollingUtil.isViewToBottom(this.f86cp.getTargetView(), this.f86cp.getTouchSlop()) && this.f86cp.allowPullUp()) {
                        this.f86cp.setStatePBU();
                        this.mTouchX = motionEvent.getX();
                        this.mTouchY = motionEvent.getY();
                        this.intercepted = true;
                        sendCancelEvent();
                        return true;
                    }
                }
                if (this.intercepted) {
                    if (this.f86cp.isRefreshVisible() || this.f86cp.isLoadingVisible()) {
                        return this.f86cp.dispatchTouchEventSuper(motionEvent);
                    }
                    if (!this.f86cp.isPrepareFinishRefresh() && this.f86cp.isStatePTD()) {
                        if (y < ((float) (-this.f86cp.getTouchSlop())) || !ScrollingUtil.isViewToTop(this.f86cp.getTargetView(), this.f86cp.getTouchSlop())) {
                            this.f86cp.dispatchTouchEventSuper(motionEvent);
                        }
                        y = Math.max(0.0f, Math.min(this.f86cp.getMaxHeadHeight() * 2.0f, y));
                        this.f86cp.getAnimProcessor().scrollHeadByMove(y);
                    } else if (!this.f86cp.isPrepareFinishLoadMore() && this.f86cp.isStatePBU()) {
                        if (y > ((float) this.f86cp.getTouchSlop()) || !ScrollingUtil.isViewToBottom(this.f86cp.getTargetView(), this.f86cp.getTouchSlop())) {
                            this.f86cp.dispatchTouchEventSuper(motionEvent);
                        }
                        y = Math.min(0.0f, Math.max((float) ((-this.f86cp.getMaxBottomHeight()) * 2), y));
                        this.f86cp.getAnimProcessor().scrollBottomByMove(Math.abs(y));
                    }
                    if (y == 0.0f && !this.downEventSent) {
                        this.downEventSent = true;
                        sendDownEvent();
                    }
                    return true;
                }
                break;
        }
        return this.f86cp.dispatchTouchEventSuper(motionEvent);
    }

    private void sendCancelEvent() {
        MotionEvent motionEvent = this.mLastMoveEvent;
        if (motionEvent != null) {
            this.f86cp.dispatchTouchEventSuper(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getEventTime() + ((long) ViewConfiguration.getLongPressTimeout()), 3, motionEvent.getX(), motionEvent.getY(), motionEvent.getMetaState()));
        }
    }

    private void sendDownEvent() {
        MotionEvent motionEvent = this.mLastMoveEvent;
        this.f86cp.dispatchTouchEventSuper(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getEventTime(), 0, motionEvent.getX(), motionEvent.getY(), motionEvent.getMetaState()));
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public void onFingerUp(MotionEvent motionEvent, boolean z) {
        if (!z && this.willAnimHead) {
            this.f86cp.getAnimProcessor().dealPullDownRelease();
        }
        if (!z && this.willAnimBottom) {
            this.f86cp.getAnimProcessor().dealPullUpRelease();
        }
        this.willAnimHead = false;
        this.willAnimBottom = false;
    }

    @Override // com.lcodecore.tkrefreshlayout.processor.IDecorator
    public void onFingerScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2, float f3, float f4) {
        int touchSlop = this.f86cp.getTouchSlop();
        if (this.f86cp.isRefreshVisible() && f2 >= ((float) touchSlop) && !this.f86cp.isOpenFloatRefresh()) {
            this.f86cp.getAnimProcessor().animHeadHideByVy((int) f4);
        }
        if (this.f86cp.isLoadingVisible() && f2 <= ((float) (-touchSlop))) {
            this.f86cp.getAnimProcessor().animBottomHideByVy((int) f4);
        }
    }
}
