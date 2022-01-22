package p006me.majiajie.pagerbottomtabstrip.item;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import p006me.majiajie.pagerbottomtabstrip.C1028R;
import p006me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;

/* renamed from: me.majiajie.pagerbottomtabstrip.item.NormalItemView */
/* loaded from: classes.dex */
public class NormalItemView extends BaseTabItem {
    private int mCheckedDrawable;
    private int mCheckedTextColor;
    private int mDefaultDrawable;
    private int mDefaultTextColor;
    private ImageView mIcon;
    private final RoundMessageView mMessages;
    private final TextView mTitle;

    public NormalItemView(Context context) {
        this(context, null);
    }

    public NormalItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NormalItemView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDefaultTextColor = 1442840576;
        this.mCheckedTextColor = 1442840576;
        LayoutInflater.from(context).inflate(C1028R.layout.item_normal, (ViewGroup) this, true);
        this.mIcon = (ImageView) findViewById(C1028R.C1030id.icon);
        this.mTitle = (TextView) findViewById(C1028R.C1030id.title);
        this.mMessages = (RoundMessageView) findViewById(C1028R.C1030id.messages);
    }

    public void initialize(@DrawableRes int i, @DrawableRes int i2, String str) {
        this.mDefaultDrawable = i;
        this.mCheckedDrawable = i2;
        this.mTitle.setText(str);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setChecked(boolean z) {
        if (z) {
            this.mIcon.setImageResource(this.mCheckedDrawable);
            this.mTitle.setTextColor(this.mCheckedTextColor);
            return;
        }
        this.mIcon.setImageResource(this.mDefaultDrawable);
        this.mTitle.setTextColor(this.mDefaultTextColor);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setMessageNumber(int i) {
        this.mMessages.setMessageNumber(i);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setHasMessage(boolean z) {
        this.mMessages.setHasMessage(z);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public String getTitle() {
        return this.mTitle.getText().toString();
    }

    public void setTextDefaultColor(@ColorInt int i) {
        this.mDefaultTextColor = i;
    }

    public void setTextCheckedColor(@ColorInt int i) {
        this.mCheckedTextColor = i;
    }
}
