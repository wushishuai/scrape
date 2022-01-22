package com.lcodecore.tkrefreshlayout.utils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.p000v4.view.ViewCompat;
import android.support.p003v7.widget.LinearLayoutManager;
import android.support.p003v7.widget.RecyclerView;
import android.support.p003v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import java.lang.reflect.Field;

/* loaded from: classes.dex */
public class ScrollingUtil {
    private ScrollingUtil() {
    }

    public static boolean canChildScrollUp(View view) {
        if (view == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(view, -1);
        }
        if (view instanceof AbsListView) {
            AbsListView absListView = (AbsListView) view;
            if (absListView.getChildCount() <= 0) {
                return false;
            }
            if (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop()) {
                return true;
            }
            return false;
        } else if (ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean canChildScrollDown(View view) {
        if (Build.VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(view, 1);
        }
        if (view instanceof AbsListView) {
            AbsListView absListView = (AbsListView) view;
            if (absListView.getChildCount() <= 0 || (absListView.getLastVisiblePosition() >= absListView.getChildCount() - 1 && absListView.getChildAt(absListView.getChildCount() - 1).getBottom() <= absListView.getPaddingBottom())) {
                return false;
            }
            return true;
        } else if (ViewCompat.canScrollVertically(view, 1) || view.getScrollY() < 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isScrollViewOrWebViewToTop(View view) {
        return view != null && view.getScrollY() == 0;
    }

    public static boolean isViewToTop(View view, int i) {
        if (view instanceof AbsListView) {
            return isAbsListViewToTop((AbsListView) view);
        }
        if (view instanceof RecyclerView) {
            return isRecyclerViewToTop((RecyclerView) view);
        }
        return view != null && Math.abs(view.getScrollY()) <= i * 2;
    }

    public static boolean isViewToBottom(View view, int i) {
        if (view instanceof AbsListView) {
            return isAbsListViewToBottom((AbsListView) view);
        }
        if (view instanceof RecyclerView) {
            return isRecyclerViewToBottom((RecyclerView) view);
        }
        if (view instanceof WebView) {
            return isWebViewToBottom((WebView) view, i);
        }
        if (view instanceof ViewGroup) {
            return isViewGroupToBottom((ViewGroup) view);
        }
        return false;
    }

    public static boolean isAbsListViewToTop(AbsListView absListView) {
        if (absListView != null) {
            int top = absListView.getChildCount() > 0 ? absListView.getChildAt(0).getTop() - absListView.getPaddingTop() : 0;
            if (absListView.getFirstVisiblePosition() == 0 && top == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRecyclerViewToTop(RecyclerView recyclerView) {
        int i;
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null || layoutManager.getItemCount() == 0) {
                return true;
            }
            if (recyclerView.getChildCount() > 0) {
                View childAt = recyclerView.getChildAt(0);
                if (childAt == null || childAt.getMeasuredHeight() < recyclerView.getMeasuredHeight()) {
                    View childAt2 = recyclerView.getChildAt(0);
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childAt2.getLayoutParams();
                    i = ((childAt2.getTop() - layoutParams.topMargin) - getRecyclerViewItemTopInset(layoutParams)) - recyclerView.getPaddingTop();
                } else if (Build.VERSION.SDK_INT >= 14) {
                    return !ViewCompat.canScrollVertically(recyclerView, -1);
                } else {
                    if (ViewCompat.canScrollVertically(recyclerView, -1) || recyclerView.getScrollY() > 0) {
                        return false;
                    }
                    return true;
                }
            } else {
                i = 0;
            }
            if (layoutManager instanceof LinearLayoutManager) {
                if (((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition() < 1 && i == 0) {
                    return true;
                }
            } else if ((layoutManager instanceof StaggeredGridLayoutManager) && ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(null)[0] < 1 && i == 0) {
                return true;
            }
        }
        return false;
    }

    private static int getRecyclerViewItemTopInset(RecyclerView.LayoutParams layoutParams) {
        try {
            Field declaredField = RecyclerView.LayoutParams.class.getDeclaredField("mDecorInsets");
            declaredField.setAccessible(true);
            return ((Rect) declaredField.get(layoutParams)).top;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isWebViewToBottom(WebView webView, int i) {
        return webView != null && (((float) webView.getContentHeight()) * webView.getScale()) - ((float) (webView.getHeight() + webView.getScrollY())) <= ((float) (i * 2));
    }

    public static boolean isViewGroupToBottom(ViewGroup viewGroup) {
        View childAt = viewGroup.getChildAt(0);
        if (childAt == null || childAt.getMeasuredHeight() > viewGroup.getScrollY() + viewGroup.getHeight()) {
            return false;
        }
        return true;
    }

    public static boolean isScrollViewToBottom(ScrollView scrollView) {
        if (scrollView == null || ((scrollView.getScrollY() + scrollView.getMeasuredHeight()) - scrollView.getPaddingTop()) - scrollView.getPaddingBottom() != scrollView.getChildAt(0).getMeasuredHeight()) {
            return false;
        }
        return true;
    }

    public static boolean isAbsListViewToBottom(AbsListView absListView) {
        if (absListView == null || absListView.getAdapter() == null || absListView.getChildCount() <= 0 || absListView.getLastVisiblePosition() != ((ListAdapter) absListView.getAdapter()).getCount() - 1 || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getMeasuredHeight()) {
            return false;
        }
        return true;
    }

    public static boolean isRecyclerViewToBottom(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager;
        if (recyclerView == null || (layoutManager = recyclerView.getLayoutManager()) == null || layoutManager.getItemCount() == 0) {
            return false;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            View childAt = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
            if (childAt == null || childAt.getMeasuredHeight() < recyclerView.getMeasuredHeight()) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == linearLayoutManager.getItemCount() - 1) {
                    return true;
                }
            } else if (Build.VERSION.SDK_INT >= 14) {
                return !ViewCompat.canScrollVertically(recyclerView, 1);
            } else {
                if (ViewCompat.canScrollVertically(recyclerView, 1) || recyclerView.getScrollY() < 0) {
                    return false;
                }
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] findLastCompletelyVisibleItemPositions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
            int itemCount = staggeredGridLayoutManager.getItemCount() - 1;
            for (int i : findLastCompletelyVisibleItemPositions) {
                if (i == itemCount) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void scrollAViewBy(View view, int i) {
        if (view instanceof RecyclerView) {
            ((RecyclerView) view).scrollBy(0, i);
        } else if (view instanceof ScrollView) {
            ((ScrollView) view).smoothScrollBy(0, i);
        } else if (view instanceof AbsListView) {
            ((AbsListView) view).smoothScrollBy(i, 0);
        } else {
            try {
                view.getClass().getDeclaredMethod("smoothScrollBy", Integer.class, Integer.class).invoke(view, 0, Integer.valueOf(i));
            } catch (Exception unused) {
                view.scrollBy(0, i);
            }
        }
    }

    public static void scrollToBottom(final ScrollView scrollView) {
        if (scrollView != null) {
            scrollView.post(new Runnable() { // from class: com.lcodecore.tkrefreshlayout.utils.ScrollingUtil.1
                @Override // java.lang.Runnable
                public void run() {
                    scrollView.fullScroll(130);
                }
            });
        }
    }

    public static void scrollToBottom(final AbsListView absListView) {
        if (absListView != null && absListView.getAdapter() != null && ((ListAdapter) absListView.getAdapter()).getCount() > 0) {
            absListView.post(new Runnable() { // from class: com.lcodecore.tkrefreshlayout.utils.ScrollingUtil.2
                @Override // java.lang.Runnable
                public void run() {
                    AbsListView absListView2 = absListView;
                    absListView2.setSelection(((ListAdapter) absListView2.getAdapter()).getCount() - 1);
                }
            });
        }
    }

    public static void scrollToBottom(final RecyclerView recyclerView) {
        if (recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
            recyclerView.post(new Runnable() { // from class: com.lcodecore.tkrefreshlayout.utils.ScrollingUtil.3
                @Override // java.lang.Runnable
                public void run() {
                    RecyclerView recyclerView2 = recyclerView;
                    recyclerView2.smoothScrollToPosition(recyclerView2.getAdapter().getItemCount() - 1);
                }
            });
        }
    }

    public static void scrollToBottom(View view) {
        if (view instanceof RecyclerView) {
            scrollToBottom((RecyclerView) view);
        }
        if (view instanceof AbsListView) {
            scrollToBottom((AbsListView) view);
        }
        if (view instanceof ScrollView) {
            scrollToBottom((ScrollView) view);
        }
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
