package p006me.goldze.mvvmhabit.crash;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.p000v4.content.res.ResourcesCompat;
import android.support.p003v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import p006me.goldze.mvvmhabit.C0933R;

/* renamed from: me.goldze.mvvmhabit.crash.DefaultErrorActivity */
/* loaded from: classes.dex */
public final class DefaultErrorActivity extends AppCompatActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.support.p000v4.app.ComponentActivity, android.app.Activity
    @SuppressLint({"PrivateResource"})
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        TypedArray obtainStyledAttributes = obtainStyledAttributes(C0933R.styleable.AppCompatTheme);
        if (!obtainStyledAttributes.hasValue(C0933R.styleable.AppCompatTheme_windowActionBar)) {
            setTheme(C0933R.style.Theme_AppCompat_Light_DarkActionBar);
        }
        obtainStyledAttributes.recycle();
        setContentView(C0933R.layout.customactivityoncrash_default_error_activity);
        Button button = (Button) findViewById(C0933R.C0936id.customactivityoncrash_error_activity_restart_button);
        final CaocConfig configFromIntent = CustomActivityOnCrash.getConfigFromIntent(getIntent());
        if (!configFromIntent.isShowRestartButton() || configFromIntent.getRestartActivityClass() == null) {
            button.setOnClickListener(new View.OnClickListener() { // from class: me.goldze.mvvmhabit.crash.DefaultErrorActivity.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    CustomActivityOnCrash.closeApplication(DefaultErrorActivity.this, configFromIntent);
                }
            });
        } else {
            button.setText(C0933R.string.customactivityoncrash_error_activity_restart_app);
            button.setOnClickListener(new View.OnClickListener() { // from class: me.goldze.mvvmhabit.crash.DefaultErrorActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    CustomActivityOnCrash.restartApplication(DefaultErrorActivity.this, configFromIntent);
                }
            });
        }
        Button button2 = (Button) findViewById(C0933R.C0936id.customactivityoncrash_error_activity_more_info_button);
        if (configFromIntent.isShowErrorDetails()) {
            button2.setOnClickListener(new View.OnClickListener() { // from class: me.goldze.mvvmhabit.crash.DefaultErrorActivity.3
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    AlertDialog.Builder title = new AlertDialog.Builder(DefaultErrorActivity.this).setTitle(C0933R.string.customactivityoncrash_error_activity_error_details_title);
                    DefaultErrorActivity defaultErrorActivity = DefaultErrorActivity.this;
                    ((TextView) title.setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(defaultErrorActivity, defaultErrorActivity.getIntent())).setPositiveButton(C0933R.string.customactivityoncrash_error_activity_error_details_close, (DialogInterface.OnClickListener) null).setNeutralButton(C0933R.string.customactivityoncrash_error_activity_error_details_copy, new DialogInterface.OnClickListener() { // from class: me.goldze.mvvmhabit.crash.DefaultErrorActivity.3.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DefaultErrorActivity.this.copyErrorToClipboard();
                            Toast.makeText(DefaultErrorActivity.this, C0933R.string.customactivityoncrash_error_activity_error_details_copied, 0).show();
                        }
                    }).show().findViewById(16908299)).setTextSize(0, DefaultErrorActivity.this.getResources().getDimension(C0933R.dimen.customactivityoncrash_error_activity_error_details_text_size));
                }
            });
        } else {
            button2.setVisibility(8);
        }
        Integer errorDrawable = configFromIntent.getErrorDrawable();
        ImageView imageView = (ImageView) findViewById(C0933R.C0936id.customactivityoncrash_error_activity_image);
        if (errorDrawable != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), errorDrawable.intValue(), getTheme()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void copyErrorToClipboard() {
        ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(getString(C0933R.string.f204x949651f5), CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, getIntent())));
    }
}
