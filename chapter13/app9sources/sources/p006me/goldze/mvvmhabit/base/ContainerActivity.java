package p006me.goldze.mvvmhabit.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentTransaction;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import java.lang.ref.WeakReference;
import p006me.goldze.mvvmhabit.C0933R;

/* renamed from: me.goldze.mvvmhabit.base.ContainerActivity */
/* loaded from: classes.dex */
public class ContainerActivity extends RxAppCompatActivity {
    public static final String BUNDLE = "bundle";
    public static final String FRAGMENT = "fragment";
    private static final String FRAGMENT_TAG = "content_fragment_tag";
    protected WeakReference<Fragment> mFragment;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.trello.rxlifecycle2.components.support.RxAppCompatActivity, android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.support.p000v4.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        getWindow().setSoftInputMode(32);
        super.onCreate(bundle);
        setContentView(C0933R.layout.activity_container);
        Fragment fragment = bundle != null ? getSupportFragmentManager().getFragment(bundle, FRAGMENT_TAG) : null;
        if (fragment == null) {
            fragment = initFromIntent(getIntent());
        }
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        beginTransaction.replace(C0933R.C0936id.content, fragment);
        beginTransaction.commitAllowingStateLoss();
        this.mFragment = new WeakReference<>(fragment);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.support.p000v4.app.ComponentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        getSupportFragmentManager().putFragment(bundle, FRAGMENT_TAG, this.mFragment.get());
    }

    protected Fragment initFromIntent(Intent intent) {
        if (intent != null) {
            try {
                String stringExtra = intent.getStringExtra(FRAGMENT);
                if (stringExtra == null || "".equals(stringExtra)) {
                    throw new IllegalArgumentException("can not find page fragmentName");
                }
                Fragment fragment = (Fragment) Class.forName(stringExtra).newInstance();
                Bundle bundleExtra = intent.getBundleExtra(BUNDLE);
                if (bundleExtra != null) {
                    fragment.setArguments(bundleExtra);
                }
                return fragment;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("fragment initialization failed!");
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                throw new RuntimeException("fragment initialization failed!");
            } catch (InstantiationException e3) {
                e3.printStackTrace();
                throw new RuntimeException("fragment initialization failed!");
            }
        } else {
            throw new RuntimeException("you must provide a page info to display");
        }
    }

    @Override // android.support.p000v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        Fragment findFragmentById = getSupportFragmentManager().findFragmentById(C0933R.C0936id.content);
        if (!(findFragmentById instanceof BaseFragment)) {
            super.onBackPressed();
        } else if (!((BaseFragment) findFragmentById).isBackPressed()) {
            super.onBackPressed();
        }
    }
}
