package p006me.goldze.mvvmhabit.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentManager;
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
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(32);
        super.onCreate(savedInstanceState);
        setContentView(C0933R.layout.activity_container);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = null;
        if (savedInstanceState != null) {
            fragment = fm.getFragment(savedInstanceState, FRAGMENT_TAG);
        }
        if (fragment == null) {
            fragment = initFromIntent(getIntent());
        }
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(C0933R.C0936id.content, fragment);
        trans.commitAllowingStateLoss();
        this.mFragment = new WeakReference<>(fragment);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.support.p000v4.app.ComponentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, this.mFragment.get());
    }

    protected Fragment initFromIntent(Intent data) {
        if (data != null) {
            try {
                String fragmentName = data.getStringExtra(FRAGMENT);
                if (fragmentName == null || "".equals(fragmentName)) {
                    throw new IllegalArgumentException("can not find page fragmentName");
                }
                Fragment fragment = (Fragment) Class.forName(fragmentName).newInstance();
                Bundle args = data.getBundleExtra(BUNDLE);
                if (args != null) {
                    fragment.setArguments(args);
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(C0933R.C0936id.content);
        if (!(fragment instanceof BaseFragment)) {
            super.onBackPressed();
        } else if (!((BaseFragment) fragment).isBackPressed()) {
            super.onBackPressed();
        }
    }
}