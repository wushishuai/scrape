package p006me.goldze.mvvmhabit.binding.viewadapter.image;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.image.ViewAdapter */
/* loaded from: classes.dex */
public final class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"url", "placeholderRes"})
    public static void setImageUri(ImageView imageView, String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            Glide.with(imageView.getContext()).load(str).apply(new RequestOptions().placeholder(i)).into(imageView);
        }
    }
}
