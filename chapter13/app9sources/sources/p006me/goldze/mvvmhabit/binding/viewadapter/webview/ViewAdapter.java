package p006me.goldze.mvvmhabit.binding.viewadapter.webview;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.webkit.WebView;
import com.bumptech.glide.load.Key;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.webview.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter({"render"})
    public static void loadHtml(WebView webView, String str) {
        if (!TextUtils.isEmpty(str)) {
            webView.loadDataWithBaseURL(null, str, "text/html", Key.STRING_CHARSET_NAME, null);
        }
    }
}
