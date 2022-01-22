package p006me.goldze.mvvmhabit.crash;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.PathInterpolatorCompat;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import p006me.goldze.mvvmhabit.crash.CustomActivityOnCrash;

/* renamed from: me.goldze.mvvmhabit.crash.CaocConfig */
/* loaded from: classes.dex */
public class CaocConfig implements Serializable {
    public static final int BACKGROUND_MODE_CRASH = 2;
    public static final int BACKGROUND_MODE_SHOW_CUSTOM = 1;
    public static final int BACKGROUND_MODE_SILENT = 0;
    private int backgroundMode = 1;
    private boolean enabled = true;
    private boolean showErrorDetails = true;
    private boolean showRestartButton = true;
    private boolean trackActivities = false;
    private int minTimeBetweenCrashesMs = PathInterpolatorCompat.MAX_NUM_POINTS;
    private Integer errorDrawable = null;
    private Class<? extends Activity> errorActivityClass = null;
    private Class<? extends Activity> restartActivityClass = null;
    private CustomActivityOnCrash.EventListener eventListener = null;

    public int getBackgroundMode() {
        return this.backgroundMode;
    }

    public void setBackgroundMode(int i) {
        this.backgroundMode = i;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    public boolean isShowErrorDetails() {
        return this.showErrorDetails;
    }

    public void setShowErrorDetails(boolean z) {
        this.showErrorDetails = z;
    }

    public boolean isShowRestartButton() {
        return this.showRestartButton;
    }

    public void setShowRestartButton(boolean z) {
        this.showRestartButton = z;
    }

    public boolean isTrackActivities() {
        return this.trackActivities;
    }

    public void setTrackActivities(boolean z) {
        this.trackActivities = z;
    }

    public int getMinTimeBetweenCrashesMs() {
        return this.minTimeBetweenCrashesMs;
    }

    public void setMinTimeBetweenCrashesMs(int i) {
        this.minTimeBetweenCrashesMs = i;
    }

    @DrawableRes
    @Nullable
    public Integer getErrorDrawable() {
        return this.errorDrawable;
    }

    public void setErrorDrawable(@DrawableRes @Nullable Integer num) {
        this.errorDrawable = num;
    }

    @Nullable
    public Class<? extends Activity> getErrorActivityClass() {
        return this.errorActivityClass;
    }

    public void setErrorActivityClass(@Nullable Class<? extends Activity> cls) {
        this.errorActivityClass = cls;
    }

    @Nullable
    public Class<? extends Activity> getRestartActivityClass() {
        return this.restartActivityClass;
    }

    public void setRestartActivityClass(@Nullable Class<? extends Activity> cls) {
        this.restartActivityClass = cls;
    }

    @Nullable
    public CustomActivityOnCrash.EventListener getEventListener() {
        return this.eventListener;
    }

    public void setEventListener(@Nullable CustomActivityOnCrash.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    /* renamed from: me.goldze.mvvmhabit.crash.CaocConfig$Builder */
    /* loaded from: classes.dex */
    public static class Builder {
        private CaocConfig config;

        @NonNull
        public static Builder create() {
            Builder builder = new Builder();
            CaocConfig config = CustomActivityOnCrash.getConfig();
            CaocConfig caocConfig = new CaocConfig();
            caocConfig.backgroundMode = config.backgroundMode;
            caocConfig.enabled = config.enabled;
            caocConfig.showErrorDetails = config.showErrorDetails;
            caocConfig.showRestartButton = config.showRestartButton;
            caocConfig.trackActivities = config.trackActivities;
            caocConfig.minTimeBetweenCrashesMs = config.minTimeBetweenCrashesMs;
            caocConfig.errorDrawable = config.errorDrawable;
            caocConfig.errorActivityClass = config.errorActivityClass;
            caocConfig.restartActivityClass = config.restartActivityClass;
            caocConfig.eventListener = config.eventListener;
            builder.config = caocConfig;
            return builder;
        }

        @NonNull
        public Builder backgroundMode(int i) {
            this.config.backgroundMode = i;
            return this;
        }

        @NonNull
        public Builder enabled(boolean z) {
            this.config.enabled = z;
            return this;
        }

        @NonNull
        public Builder showErrorDetails(boolean z) {
            this.config.showErrorDetails = z;
            return this;
        }

        @NonNull
        public Builder showRestartButton(boolean z) {
            this.config.showRestartButton = z;
            return this;
        }

        @NonNull
        public Builder trackActivities(boolean z) {
            this.config.trackActivities = z;
            return this;
        }

        @NonNull
        public Builder minTimeBetweenCrashesMs(int i) {
            this.config.minTimeBetweenCrashesMs = i;
            return this;
        }

        @NonNull
        public Builder errorDrawable(@DrawableRes @Nullable Integer num) {
            this.config.errorDrawable = num;
            return this;
        }

        @NonNull
        public Builder errorActivity(@Nullable Class<? extends Activity> cls) {
            this.config.errorActivityClass = cls;
            return this;
        }

        @NonNull
        public Builder restartActivity(@Nullable Class<? extends Activity> cls) {
            this.config.restartActivityClass = cls;
            return this;
        }

        @NonNull
        public Builder eventListener(@Nullable CustomActivityOnCrash.EventListener eventListener) {
            if (eventListener == null || eventListener.getClass().getEnclosingClass() == null || Modifier.isStatic(eventListener.getClass().getModifiers())) {
                this.config.eventListener = eventListener;
                return this;
            }
            throw new IllegalArgumentException("The event listener cannot be an inner or anonymous class, because it will need to be serialized. Change it to a class of its own, or make it a static inner class.");
        }

        @NonNull
        public CaocConfig get() {
            return this.config;
        }

        public void apply() {
            CustomActivityOnCrash.setConfig(this.config);
        }
    }
}
