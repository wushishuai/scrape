package p006me.goldze.mvvmhabit.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import java.lang.reflect.InvocationTargetException;

/* renamed from: me.goldze.mvvmhabit.base.ViewModelFactory */
/* loaded from: classes.dex */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    @SuppressLint({"StaticFieldLeak"})
    private static volatile ViewModelFactory INSTANCE;
    private final Application mApplication;

    public static ViewModelFactory getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ViewModelFactory(application);
                }
            }
        }
        return INSTANCE;
    }

    private ViewModelFactory(Application application) {
        this.mApplication = application;
    }

    @Override // android.arch.lifecycle.ViewModelProvider.NewInstanceFactory, android.arch.lifecycle.ViewModelProvider.Factory
    public <T extends ViewModel> T create(Class<T> cls) {
        if (cls.isAssignableFrom(BaseViewModel.class)) {
            return new BaseViewModel(this.mApplication);
        }
        try {
            return (T) ((ViewModel) Class.forName(cls.getCanonicalName()).getConstructor(Application.class).newInstance(this.mApplication));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + cls.getName());
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + cls.getName());
        } catch (InstantiationException e3) {
            e3.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + cls.getName());
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + cls.getName());
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + cls.getName());
        }
    }
}
