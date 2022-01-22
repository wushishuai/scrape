package com.bumptech.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.widget.ImageView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.ErrorRequestCoordinator;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestCoordinator;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.SingleRequest;
import com.bumptech.glide.request.ThumbnailRequestCoordinator;
import com.bumptech.glide.request.target.PreloadTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.ApplicationVersionSignature;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class RequestBuilder<TranscodeType> implements Cloneable, ModelTypes<RequestBuilder<TranscodeType>> {
    protected static final RequestOptions DOWNLOAD_ONLY_OPTIONS = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).priority(Priority.LOW).skipMemoryCache(true);
    private final Context context;
    private final RequestOptions defaultRequestOptions;
    @Nullable
    private RequestBuilder<TranscodeType> errorBuilder;
    private final Glide glide;
    private final GlideContext glideContext;
    private boolean isDefaultTransitionOptionsSet;
    private boolean isModelSet;
    private boolean isThumbnailBuilt;
    @Nullable
    private Object model;
    @Nullable
    private List<RequestListener<TranscodeType>> requestListeners;
    private final RequestManager requestManager;
    @NonNull
    protected RequestOptions requestOptions;
    @Nullable
    private Float thumbSizeMultiplier;
    @Nullable
    private RequestBuilder<TranscodeType> thumbnailBuilder;
    private final Class<TranscodeType> transcodeClass;
    @NonNull
    private TransitionOptions<?, ? super TranscodeType> transitionOptions;

    /* JADX INFO: Access modifiers changed from: protected */
    public RequestBuilder(Glide glide, RequestManager requestManager, Class<TranscodeType> cls, Context context) {
        this.isDefaultTransitionOptionsSet = true;
        this.glide = glide;
        this.requestManager = requestManager;
        this.transcodeClass = cls;
        this.defaultRequestOptions = requestManager.getDefaultRequestOptions();
        this.context = context;
        this.transitionOptions = requestManager.getDefaultTransitionOptions(cls);
        this.requestOptions = this.defaultRequestOptions;
        this.glideContext = glide.getGlideContext();
    }

    protected RequestBuilder(Class<TranscodeType> cls, RequestBuilder<?> requestBuilder) {
        this(requestBuilder.glide, requestBuilder.requestManager, cls, requestBuilder.context);
        this.model = requestBuilder.model;
        this.isModelSet = requestBuilder.isModelSet;
        this.requestOptions = requestBuilder.requestOptions;
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> apply(@NonNull RequestOptions requestOptions) {
        Preconditions.checkNotNull(requestOptions);
        this.requestOptions = getMutableOptions().apply(requestOptions);
        return this;
    }

    @NonNull
    protected RequestOptions getMutableOptions() {
        RequestOptions requestOptions = this.defaultRequestOptions;
        RequestOptions requestOptions2 = this.requestOptions;
        return requestOptions == requestOptions2 ? requestOptions2.clone() : requestOptions2;
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> transition(@NonNull TransitionOptions<?, ? super TranscodeType> transitionOptions) {
        this.transitionOptions = (TransitionOptions) Preconditions.checkNotNull(transitionOptions);
        this.isDefaultTransitionOptionsSet = false;
        return this;
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> listener(@Nullable RequestListener<TranscodeType> requestListener) {
        this.requestListeners = null;
        return addListener(requestListener);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> addListener(@Nullable RequestListener<TranscodeType> requestListener) {
        if (requestListener != null) {
            if (this.requestListeners == null) {
                this.requestListeners = new ArrayList();
            }
            this.requestListeners.add(requestListener);
        }
        return this;
    }

    @NonNull
    public RequestBuilder<TranscodeType> error(@Nullable RequestBuilder<TranscodeType> requestBuilder) {
        this.errorBuilder = requestBuilder;
        return this;
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> thumbnail(@Nullable RequestBuilder<TranscodeType> requestBuilder) {
        this.thumbnailBuilder = requestBuilder;
        return this;
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> thumbnail(@Nullable RequestBuilder<TranscodeType>... requestBuilderArr) {
        RequestBuilder<TranscodeType> requestBuilder = null;
        if (requestBuilderArr == null || requestBuilderArr.length == 0) {
            return thumbnail((RequestBuilder) null);
        }
        for (int length = requestBuilderArr.length - 1; length >= 0; length--) {
            RequestBuilder<TranscodeType> requestBuilder2 = requestBuilderArr[length];
            if (requestBuilder2 != null) {
                requestBuilder = requestBuilder == null ? requestBuilder2 : requestBuilder2.thumbnail(requestBuilder);
            }
        }
        return thumbnail(requestBuilder);
    }

    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> thumbnail(float f) {
        if (f < 0.0f || f > 1.0f) {
            throw new IllegalArgumentException("sizeMultiplier must be between 0 and 1");
        }
        this.thumbSizeMultiplier = Float.valueOf(f);
        return this;
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable Object obj) {
        return loadGeneric(obj);
    }

    @NonNull
    private RequestBuilder<TranscodeType> loadGeneric(@Nullable Object obj) {
        this.model = obj;
        this.isModelSet = true;
        return this;
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable Bitmap bitmap) {
        return loadGeneric(bitmap).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable Drawable drawable) {
        return loadGeneric(drawable).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable String str) {
        return loadGeneric(str);
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable Uri uri) {
        return loadGeneric(uri);
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable File file) {
        return loadGeneric(file);
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@RawRes @DrawableRes @Nullable Integer num) {
        return loadGeneric(num).apply(RequestOptions.signatureOf(ApplicationVersionSignature.obtain(this.context)));
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @Deprecated
    public RequestBuilder<TranscodeType> load(@Nullable URL url) {
        return loadGeneric(url);
    }

    @Override // com.bumptech.glide.ModelTypes
    @CheckResult
    @NonNull
    public RequestBuilder<TranscodeType> load(@Nullable byte[] bArr) {
        RequestBuilder<TranscodeType> loadGeneric = loadGeneric(bArr);
        if (!loadGeneric.requestOptions.isDiskCacheStrategySet()) {
            loadGeneric = loadGeneric.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
        }
        return !loadGeneric.requestOptions.isSkipMemoryCacheSet() ? loadGeneric.apply(RequestOptions.skipMemoryCacheOf(true)) : loadGeneric;
    }

    @Override // java.lang.Object
    @CheckResult
    public RequestBuilder<TranscodeType> clone() {
        try {
            RequestBuilder<TranscodeType> requestBuilder = (RequestBuilder) super.clone();
            requestBuilder.requestOptions = requestBuilder.requestOptions.clone();
            requestBuilder.transitionOptions = requestBuilder.transitionOptions.clone();
            return requestBuilder;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public <Y extends Target<TranscodeType>> Y into(@NonNull Y y) {
        return (Y) into((RequestBuilder<TranscodeType>) y, (RequestListener) null);
    }

    @NonNull
    <Y extends Target<TranscodeType>> Y into(@NonNull Y y, @Nullable RequestListener<TranscodeType> requestListener) {
        return (Y) into(y, requestListener, getMutableOptions());
    }

    private <Y extends Target<TranscodeType>> Y into(@NonNull Y y, @Nullable RequestListener<TranscodeType> requestListener, @NonNull RequestOptions requestOptions) {
        Util.assertMainThread();
        Preconditions.checkNotNull(y);
        if (this.isModelSet) {
            RequestOptions autoClone = requestOptions.autoClone();
            Request buildRequest = buildRequest(y, requestListener, autoClone);
            Request request = y.getRequest();
            if (!buildRequest.isEquivalentTo(request) || isSkipMemoryCacheWithCompletePreviousRequest(autoClone, request)) {
                this.requestManager.clear((Target<?>) y);
                y.setRequest(buildRequest);
                this.requestManager.track(y, buildRequest);
                return y;
            }
            buildRequest.recycle();
            if (!((Request) Preconditions.checkNotNull(request)).isRunning()) {
                request.begin();
            }
            return y;
        }
        throw new IllegalArgumentException("You must call #load() before calling #into()");
    }

    private boolean isSkipMemoryCacheWithCompletePreviousRequest(RequestOptions requestOptions, Request request) {
        return !requestOptions.isMemoryCacheable() && request.isComplete();
    }

    @NonNull
    public ViewTarget<ImageView, TranscodeType> into(@NonNull ImageView imageView) {
        Util.assertMainThread();
        Preconditions.checkNotNull(imageView);
        RequestOptions requestOptions = this.requestOptions;
        if (!requestOptions.isTransformationSet() && requestOptions.isTransformationAllowed() && imageView.getScaleType() != null) {
            switch (C06362.$SwitchMap$android$widget$ImageView$ScaleType[imageView.getScaleType().ordinal()]) {
                case 1:
                    requestOptions = requestOptions.clone().optionalCenterCrop();
                    break;
                case 2:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
                case 3:
                case 4:
                case 5:
                    requestOptions = requestOptions.clone().optionalFitCenter();
                    break;
                case 6:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
            }
        }
        return (ViewTarget) into(this.glideContext.buildImageViewTarget(imageView, this.transcodeClass), null, requestOptions);
    }

    @Deprecated
    public FutureTarget<TranscodeType> into(int i, int i2) {
        return submit(i, i2);
    }

    @NonNull
    public FutureTarget<TranscodeType> submit() {
        return submit(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @NonNull
    public FutureTarget<TranscodeType> submit(int i, int i2) {
        final RequestFutureTarget requestFutureTarget = new RequestFutureTarget(this.glideContext.getMainHandler(), i, i2);
        if (Util.isOnBackgroundThread()) {
            this.glideContext.getMainHandler().post(new Runnable() { // from class: com.bumptech.glide.RequestBuilder.1
                @Override // java.lang.Runnable
                public void run() {
                    if (!requestFutureTarget.isCancelled()) {
                        RequestBuilder requestBuilder = RequestBuilder.this;
                        RequestFutureTarget requestFutureTarget2 = requestFutureTarget;
                        requestBuilder.into((RequestBuilder) requestFutureTarget2, (RequestListener) requestFutureTarget2);
                    }
                }
            });
        } else {
            into((RequestBuilder<TranscodeType>) requestFutureTarget, requestFutureTarget);
        }
        return requestFutureTarget;
    }

    @NonNull
    public Target<TranscodeType> preload(int i, int i2) {
        return into((RequestBuilder<TranscodeType>) PreloadTarget.obtain(this.requestManager, i, i2));
    }

    @NonNull
    public Target<TranscodeType> preload() {
        return preload(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @CheckResult
    @Deprecated
    public <Y extends Target<File>> Y downloadOnly(@NonNull Y y) {
        return (Y) getDownloadOnlyRequest().into((RequestBuilder<File>) y);
    }

    @CheckResult
    @Deprecated
    public FutureTarget<File> downloadOnly(int i, int i2) {
        return getDownloadOnlyRequest().submit(i, i2);
    }

    @CheckResult
    @NonNull
    protected RequestBuilder<File> getDownloadOnlyRequest() {
        return new RequestBuilder(File.class, this).apply(DOWNLOAD_ONLY_OPTIONS);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.bumptech.glide.RequestBuilder$2 */
    /* loaded from: classes.dex */
    public static /* synthetic */ class C06362 {
        static final /* synthetic */ int[] $SwitchMap$android$widget$ImageView$ScaleType;

        static {
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.LOW.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.NORMAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.HIGH.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.IMMEDIATE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $SwitchMap$android$widget$ImageView$ScaleType = new int[ImageView.ScaleType.values().length];
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER_CROP.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER_INSIDE.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_START.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_END.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_XY.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER.ordinal()] = 7;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.MATRIX.ordinal()] = 8;
            } catch (NoSuchFieldError unused12) {
            }
        }
    }

    @NonNull
    private Priority getThumbnailPriority(@NonNull Priority priority) {
        switch (priority) {
            case LOW:
                return Priority.NORMAL;
            case NORMAL:
                return Priority.HIGH;
            case HIGH:
            case IMMEDIATE:
                return Priority.IMMEDIATE;
            default:
                throw new IllegalArgumentException("unknown priority: " + this.requestOptions.getPriority());
        }
    }

    private Request buildRequest(Target<TranscodeType> target, @Nullable RequestListener<TranscodeType> requestListener, RequestOptions requestOptions) {
        return buildRequestRecursive(target, requestListener, null, this.transitionOptions, requestOptions.getPriority(), requestOptions.getOverrideWidth(), requestOptions.getOverrideHeight(), requestOptions);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private Request buildRequestRecursive(Target<TranscodeType> target, @Nullable RequestListener<TranscodeType> requestListener, @Nullable RequestCoordinator requestCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int i, int i2, RequestOptions requestOptions) {
        ErrorRequestCoordinator errorRequestCoordinator;
        RequestCoordinator requestCoordinator2;
        int i3;
        int i4;
        if (this.errorBuilder != null) {
            requestCoordinator2 = new ErrorRequestCoordinator(requestCoordinator);
            errorRequestCoordinator = requestCoordinator2;
        } else {
            errorRequestCoordinator = 0;
            requestCoordinator2 = requestCoordinator;
        }
        Request buildThumbnailRequestRecursive = buildThumbnailRequestRecursive(target, requestListener, requestCoordinator2, transitionOptions, priority, i, i2, requestOptions);
        if (errorRequestCoordinator == 0) {
            return buildThumbnailRequestRecursive;
        }
        int overrideWidth = this.errorBuilder.requestOptions.getOverrideWidth();
        int overrideHeight = this.errorBuilder.requestOptions.getOverrideHeight();
        if (!Util.isValidDimensions(i, i2) || this.errorBuilder.requestOptions.isValidOverride()) {
            i4 = overrideWidth;
            i3 = overrideHeight;
        } else {
            int overrideWidth2 = requestOptions.getOverrideWidth();
            i3 = requestOptions.getOverrideHeight();
            i4 = overrideWidth2;
        }
        RequestBuilder<TranscodeType> requestBuilder = this.errorBuilder;
        errorRequestCoordinator.setRequests(buildThumbnailRequestRecursive, requestBuilder.buildRequestRecursive(target, requestListener, errorRequestCoordinator, requestBuilder.transitionOptions, requestBuilder.requestOptions.getPriority(), i4, i3, this.errorBuilder.requestOptions));
        return errorRequestCoordinator;
    }

    private Request buildThumbnailRequestRecursive(Target<TranscodeType> target, RequestListener<TranscodeType> requestListener, @Nullable RequestCoordinator requestCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int i, int i2, RequestOptions requestOptions) {
        int i3;
        int i4;
        RequestBuilder<TranscodeType> requestBuilder = this.thumbnailBuilder;
        if (requestBuilder != null) {
            if (!this.isThumbnailBuilt) {
                TransitionOptions<?, ? super TranscodeType> transitionOptions2 = requestBuilder.isDefaultTransitionOptionsSet ? transitionOptions : requestBuilder.transitionOptions;
                Priority priority2 = this.thumbnailBuilder.requestOptions.isPrioritySet() ? this.thumbnailBuilder.requestOptions.getPriority() : getThumbnailPriority(priority);
                int overrideWidth = this.thumbnailBuilder.requestOptions.getOverrideWidth();
                int overrideHeight = this.thumbnailBuilder.requestOptions.getOverrideHeight();
                if (!Util.isValidDimensions(i, i2) || this.thumbnailBuilder.requestOptions.isValidOverride()) {
                    i4 = overrideWidth;
                    i3 = overrideHeight;
                } else {
                    int overrideWidth2 = requestOptions.getOverrideWidth();
                    i3 = requestOptions.getOverrideHeight();
                    i4 = overrideWidth2;
                }
                ThumbnailRequestCoordinator thumbnailRequestCoordinator = new ThumbnailRequestCoordinator(requestCoordinator);
                Request obtainRequest = obtainRequest(target, requestListener, requestOptions, thumbnailRequestCoordinator, transitionOptions, priority, i, i2);
                this.isThumbnailBuilt = true;
                RequestBuilder<TranscodeType> requestBuilder2 = this.thumbnailBuilder;
                Request buildRequestRecursive = requestBuilder2.buildRequestRecursive(target, requestListener, thumbnailRequestCoordinator, transitionOptions2, priority2, i4, i3, requestBuilder2.requestOptions);
                this.isThumbnailBuilt = false;
                thumbnailRequestCoordinator.setRequests(obtainRequest, buildRequestRecursive);
                return thumbnailRequestCoordinator;
            }
            throw new IllegalStateException("You cannot use a request as both the main request and a thumbnail, consider using clone() on the request(s) passed to thumbnail()");
        } else if (this.thumbSizeMultiplier == null) {
            return obtainRequest(target, requestListener, requestOptions, requestCoordinator, transitionOptions, priority, i, i2);
        } else {
            ThumbnailRequestCoordinator thumbnailRequestCoordinator2 = new ThumbnailRequestCoordinator(requestCoordinator);
            thumbnailRequestCoordinator2.setRequests(obtainRequest(target, requestListener, requestOptions, thumbnailRequestCoordinator2, transitionOptions, priority, i, i2), obtainRequest(target, requestListener, requestOptions.clone().sizeMultiplier(this.thumbSizeMultiplier.floatValue()), thumbnailRequestCoordinator2, transitionOptions, getThumbnailPriority(priority), i, i2));
            return thumbnailRequestCoordinator2;
        }
    }

    private Request obtainRequest(Target<TranscodeType> target, RequestListener<TranscodeType> requestListener, RequestOptions requestOptions, RequestCoordinator requestCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions, Priority priority, int i, int i2) {
        Context context = this.context;
        GlideContext glideContext = this.glideContext;
        return SingleRequest.obtain(context, glideContext, this.model, this.transcodeClass, requestOptions, i, i2, priority, target, requestListener, this.requestListeners, requestCoordinator, glideContext.getEngine(), transitionOptions.getTransitionFactory());
    }
}
