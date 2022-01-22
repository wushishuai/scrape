package com.bumptech.glide.load.engine;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.p000v4.util.Pools;
import android.util.Log;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.DataRewinder;
import com.bumptech.glide.load.engine.DataFetcherGenerator;
import com.bumptech.glide.load.engine.DecodePath;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.util.LogTime;
import com.bumptech.glide.util.pool.FactoryPools;
import com.bumptech.glide.util.pool.GlideTrace;
import com.bumptech.glide.util.pool.StateVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class DecodeJob<R> implements DataFetcherGenerator.FetcherReadyCallback, Runnable, Comparable<DecodeJob<?>>, FactoryPools.Poolable {
    private static final String TAG = "DecodeJob";
    private Callback<R> callback;
    private Key currentAttemptingKey;
    private Object currentData;
    private DataSource currentDataSource;
    private DataFetcher<?> currentFetcher;
    private volatile DataFetcherGenerator currentGenerator;
    private Key currentSourceKey;
    private Thread currentThread;
    private final DiskCacheProvider diskCacheProvider;
    private DiskCacheStrategy diskCacheStrategy;
    private GlideContext glideContext;
    private int height;
    private volatile boolean isCallbackNotified;
    private volatile boolean isCancelled;
    private EngineKey loadKey;
    private Object model;
    private boolean onlyRetrieveFromCache;
    private Options options;
    private int order;
    private final Pools.Pool<DecodeJob<?>> pool;
    private Priority priority;
    private RunReason runReason;
    private Key signature;
    private Stage stage;
    private long startFetchTime;
    private int width;
    private final DecodeHelper<R> decodeHelper = new DecodeHelper<>();
    private final List<Throwable> throwables = new ArrayList();
    private final StateVerifier stateVerifier = StateVerifier.newInstance();
    private final DeferredEncodeManager<?> deferredEncodeManager = new DeferredEncodeManager<>();
    private final ReleaseManager releaseManager = new ReleaseManager();

    /* loaded from: classes.dex */
    public interface Callback<R> {
        void onLoadFailed(GlideException glideException);

        void onResourceReady(Resource<R> resource, DataSource dataSource);

        void reschedule(DecodeJob<?> decodeJob);
    }

    /* loaded from: classes.dex */
    public interface DiskCacheProvider {
        DiskCache getDiskCache();
    }

    /* loaded from: classes.dex */
    public enum RunReason {
        INITIALIZE,
        SWITCH_TO_SOURCE_SERVICE,
        DECODE_DATA
    }

    /* loaded from: classes.dex */
    public enum Stage {
        INITIALIZE,
        RESOURCE_CACHE,
        DATA_CACHE,
        SOURCE,
        ENCODE,
        FINISHED
    }

    public DecodeJob(DiskCacheProvider diskCacheProvider, Pools.Pool<DecodeJob<?>> pool) {
        this.diskCacheProvider = diskCacheProvider;
        this.pool = pool;
    }

    public DecodeJob<R> init(GlideContext glideContext, Object obj, EngineKey engineKey, Key key, int i, int i2, Class<?> cls, Class<R> cls2, Priority priority, DiskCacheStrategy diskCacheStrategy, Map<Class<?>, Transformation<?>> map, boolean z, boolean z2, boolean z3, Options options, Callback<R> callback, int i3) {
        this.decodeHelper.init(glideContext, obj, key, i, i2, diskCacheStrategy, cls, cls2, priority, options, map, z, z2, this.diskCacheProvider);
        this.glideContext = glideContext;
        this.signature = key;
        this.priority = priority;
        this.loadKey = engineKey;
        this.width = i;
        this.height = i2;
        this.diskCacheStrategy = diskCacheStrategy;
        this.onlyRetrieveFromCache = z3;
        this.options = options;
        this.callback = callback;
        this.order = i3;
        this.runReason = RunReason.INITIALIZE;
        this.model = obj;
        return this;
    }

    public boolean willDecodeFromCache() {
        Stage nextStage = getNextStage(Stage.INITIALIZE);
        return nextStage == Stage.RESOURCE_CACHE || nextStage == Stage.DATA_CACHE;
    }

    public void release(boolean z) {
        if (this.releaseManager.release(z)) {
            releaseInternal();
        }
    }

    private void onEncodeComplete() {
        if (this.releaseManager.onEncodeComplete()) {
            releaseInternal();
        }
    }

    private void onLoadFailed() {
        if (this.releaseManager.onFailed()) {
            releaseInternal();
        }
    }

    private void releaseInternal() {
        this.releaseManager.reset();
        this.deferredEncodeManager.clear();
        this.decodeHelper.clear();
        this.isCallbackNotified = false;
        this.glideContext = null;
        this.signature = null;
        this.options = null;
        this.priority = null;
        this.loadKey = null;
        this.callback = null;
        this.stage = null;
        this.currentGenerator = null;
        this.currentThread = null;
        this.currentSourceKey = null;
        this.currentData = null;
        this.currentDataSource = null;
        this.currentFetcher = null;
        this.startFetchTime = 0;
        this.isCancelled = false;
        this.model = null;
        this.throwables.clear();
        this.pool.release(this);
    }

    public int compareTo(@NonNull DecodeJob<?> decodeJob) {
        int priority = getPriority() - decodeJob.getPriority();
        return priority == 0 ? this.order - decodeJob.order : priority;
    }

    private int getPriority() {
        return this.priority.ordinal();
    }

    public void cancel() {
        this.isCancelled = true;
        DataFetcherGenerator dataFetcherGenerator = this.currentGenerator;
        if (dataFetcherGenerator != null) {
            dataFetcherGenerator.cancel();
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        GlideTrace.beginSectionFormat("DecodeJob#run(model=%s)", this.model);
        DataFetcher<?> dataFetcher = this.currentFetcher;
        try {
            if (this.isCancelled) {
                notifyFailed();
                return;
            }
            runWrapped();
            if (dataFetcher != null) {
                dataFetcher.cleanup();
            }
            GlideTrace.endSection();
        } finally {
            if (dataFetcher != null) {
                dataFetcher.cleanup();
            }
            GlideTrace.endSection();
        }
    }

    private void runWrapped() {
        switch (this.runReason) {
            case INITIALIZE:
                this.stage = getNextStage(Stage.INITIALIZE);
                this.currentGenerator = getNextGenerator();
                runGenerators();
                return;
            case SWITCH_TO_SOURCE_SERVICE:
                runGenerators();
                return;
            case DECODE_DATA:
                decodeFromRetrievedData();
                return;
            default:
                throw new IllegalStateException("Unrecognized run reason: " + this.runReason);
        }
    }

    private DataFetcherGenerator getNextGenerator() {
        switch (this.stage) {
            case RESOURCE_CACHE:
                return new ResourceCacheGenerator(this.decodeHelper, this);
            case DATA_CACHE:
                return new DataCacheGenerator(this.decodeHelper, this);
            case SOURCE:
                return new SourceGenerator(this.decodeHelper, this);
            case FINISHED:
                return null;
            default:
                throw new IllegalStateException("Unrecognized stage: " + this.stage);
        }
    }

    private void runGenerators() {
        this.currentThread = Thread.currentThread();
        this.startFetchTime = LogTime.getLogTime();
        boolean z = false;
        while (!this.isCancelled && this.currentGenerator != null && !(z = this.currentGenerator.startNext())) {
            this.stage = getNextStage(this.stage);
            this.currentGenerator = getNextGenerator();
            if (this.stage == Stage.SOURCE) {
                reschedule();
                return;
            }
        }
        if ((this.stage == Stage.FINISHED || this.isCancelled) && !z) {
            notifyFailed();
        }
    }

    private void notifyFailed() {
        setNotifiedOrThrow();
        this.callback.onLoadFailed(new GlideException("Failed to load resource", new ArrayList(this.throwables)));
        onLoadFailed();
    }

    private void notifyComplete(Resource<R> resource, DataSource dataSource) {
        setNotifiedOrThrow();
        this.callback.onResourceReady(resource, dataSource);
    }

    private void setNotifiedOrThrow() {
        this.stateVerifier.throwIfRecycled();
        if (!this.isCallbackNotified) {
            this.isCallbackNotified = true;
            return;
        }
        throw new IllegalStateException("Already notified");
    }

    private Stage getNextStage(Stage stage) {
        switch (stage) {
            case RESOURCE_CACHE:
                return this.diskCacheStrategy.decodeCachedData() ? Stage.DATA_CACHE : getNextStage(Stage.DATA_CACHE);
            case DATA_CACHE:
                return this.onlyRetrieveFromCache ? Stage.FINISHED : Stage.SOURCE;
            case SOURCE:
            case FINISHED:
                return Stage.FINISHED;
            case INITIALIZE:
                return this.diskCacheStrategy.decodeCachedResource() ? Stage.RESOURCE_CACHE : getNextStage(Stage.RESOURCE_CACHE);
            default:
                throw new IllegalArgumentException("Unrecognized stage: " + stage);
        }
    }

    @Override // com.bumptech.glide.load.engine.DataFetcherGenerator.FetcherReadyCallback
    public void reschedule() {
        this.runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
        this.callback.reschedule(this);
    }

    @Override // com.bumptech.glide.load.engine.DataFetcherGenerator.FetcherReadyCallback
    public void onDataFetcherReady(Key key, Object obj, DataFetcher<?> dataFetcher, DataSource dataSource, Key key2) {
        this.currentSourceKey = key;
        this.currentData = obj;
        this.currentFetcher = dataFetcher;
        this.currentDataSource = dataSource;
        this.currentAttemptingKey = key2;
        if (Thread.currentThread() != this.currentThread) {
            this.runReason = RunReason.DECODE_DATA;
            this.callback.reschedule(this);
            return;
        }
        GlideTrace.beginSection("DecodeJob.decodeFromRetrievedData");
        try {
            decodeFromRetrievedData();
        } finally {
            GlideTrace.endSection();
        }
    }

    @Override // com.bumptech.glide.load.engine.DataFetcherGenerator.FetcherReadyCallback
    public void onDataFetcherFailed(Key key, Exception exc, DataFetcher<?> dataFetcher, DataSource dataSource) {
        dataFetcher.cleanup();
        GlideException glideException = new GlideException("Fetching data failed", exc);
        glideException.setLoggingDetails(key, dataSource, dataFetcher.getDataClass());
        this.throwables.add(glideException);
        if (Thread.currentThread() != this.currentThread) {
            this.runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
            this.callback.reschedule(this);
            return;
        }
        runGenerators();
    }

    private void decodeFromRetrievedData() {
        if (Log.isLoggable(TAG, 2)) {
            long j = this.startFetchTime;
            logWithTimeAndKey("Retrieved data", j, "data: " + this.currentData + ", cache key: " + this.currentSourceKey + ", fetcher: " + this.currentFetcher);
        }
        Resource<R> resource = null;
        try {
            resource = decodeFromData(this.currentFetcher, this.currentData, this.currentDataSource);
        } catch (GlideException e) {
            e.setLoggingDetails(this.currentAttemptingKey, this.currentDataSource);
            this.throwables.add(e);
        }
        if (resource != null) {
            notifyEncodeAndRelease(resource, this.currentDataSource);
        } else {
            runGenerators();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void notifyEncodeAndRelease(Resource<R> resource, DataSource dataSource) {
        if (resource instanceof Initializable) {
            ((Initializable) resource).initialize();
        }
        LockedResource lockedResource = 0;
        if (this.deferredEncodeManager.hasResourceToEncode()) {
            resource = LockedResource.obtain(resource);
            lockedResource = resource;
        }
        notifyComplete(resource, dataSource);
        this.stage = Stage.ENCODE;
        try {
            if (this.deferredEncodeManager.hasResourceToEncode()) {
                this.deferredEncodeManager.encode(this.diskCacheProvider, this.options);
            }
            onEncodeComplete();
        } finally {
            if (lockedResource != 0) {
                lockedResource.unlock();
            }
        }
    }

    private <Data> Resource<R> decodeFromData(DataFetcher<?> dataFetcher, Data data, DataSource dataSource) throws GlideException {
        if (data == null) {
            return null;
        }
        try {
            long logTime = LogTime.getLogTime();
            Resource<R> decodeFromFetcher = decodeFromFetcher(data, dataSource);
            if (Log.isLoggable(TAG, 2)) {
                logWithTimeAndKey("Decoded result " + decodeFromFetcher, logTime);
            }
            return decodeFromFetcher;
        } finally {
            dataFetcher.cleanup();
        }
    }

    private <Data> Resource<R> decodeFromFetcher(Data data, DataSource dataSource) throws GlideException {
        return runLoadPath(data, dataSource, (LoadPath<Data, ?, R>) this.decodeHelper.getLoadPath(data.getClass()));
    }

    @NonNull
    private Options getOptionsWithHardwareConfig(DataSource dataSource) {
        Options options = this.options;
        if (Build.VERSION.SDK_INT < 26) {
            return options;
        }
        boolean z = dataSource == DataSource.RESOURCE_DISK_CACHE || this.decodeHelper.isScaleOnlyOrNoTransform();
        Boolean bool = (Boolean) options.get(Downsampler.ALLOW_HARDWARE_CONFIG);
        if (bool != null && (!bool.booleanValue() || z)) {
            return options;
        }
        Options options2 = new Options();
        options2.putAll(this.options);
        options2.set(Downsampler.ALLOW_HARDWARE_CONFIG, Boolean.valueOf(z));
        return options2;
    }

    private <Data, ResourceType> Resource<R> runLoadPath(Data data, DataSource dataSource, LoadPath<Data, ResourceType, R> loadPath) throws GlideException {
        Options optionsWithHardwareConfig = getOptionsWithHardwareConfig(dataSource);
        DataRewinder<Data> rewinder = this.glideContext.getRegistry().getRewinder(data);
        try {
            return loadPath.load(rewinder, optionsWithHardwareConfig, this.width, this.height, new DecodeCallback(dataSource));
        } finally {
            rewinder.cleanup();
        }
    }

    private void logWithTimeAndKey(String str, long j) {
        logWithTimeAndKey(str, j, null);
    }

    private void logWithTimeAndKey(String str, long j, String str2) {
        String str3;
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(" in ");
        sb.append(LogTime.getElapsedMillis(j));
        sb.append(", load key: ");
        sb.append(this.loadKey);
        if (str2 != null) {
            str3 = ", " + str2;
        } else {
            str3 = "";
        }
        sb.append(str3);
        sb.append(", thread: ");
        sb.append(Thread.currentThread().getName());
        Log.v(TAG, sb.toString());
    }

    @Override // com.bumptech.glide.util.pool.FactoryPools.Poolable
    @NonNull
    public StateVerifier getVerifier() {
        return this.stateVerifier;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @NonNull
    <Z> Resource<Z> onResourceDecoded(DataSource dataSource, @NonNull Resource<Z> resource) {
        Transformation<Z> transformation;
        Resource<Z> resource2;
        EncodeStrategy encodeStrategy;
        ResourceEncoder resourceEncoder;
        Key key;
        Class<?> cls = resource.get().getClass();
        if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
            Transformation<Z> transformation2 = this.decodeHelper.getTransformation(cls);
            resource2 = transformation2.transform(this.glideContext, resource, this.width, this.height);
            transformation = transformation2;
        } else {
            resource2 = resource;
            transformation = null;
        }
        if (!resource.equals(resource2)) {
            resource.recycle();
        }
        if (this.decodeHelper.isResourceEncoderAvailable(resource2)) {
            ResourceEncoder resultEncoder = this.decodeHelper.getResultEncoder(resource2);
            encodeStrategy = resultEncoder.getEncodeStrategy(this.options);
            resourceEncoder = resultEncoder;
        } else {
            encodeStrategy = EncodeStrategy.NONE;
            resourceEncoder = null;
        }
        if (!this.diskCacheStrategy.isResourceCacheable(!this.decodeHelper.isSourceKey(this.currentSourceKey), dataSource, encodeStrategy)) {
            return resource2;
        }
        if (resourceEncoder != null) {
            switch (encodeStrategy) {
                case SOURCE:
                    key = new DataCacheKey(this.currentSourceKey, this.signature);
                    break;
                case TRANSFORMED:
                    key = new ResourceCacheKey(this.decodeHelper.getArrayPool(), this.currentSourceKey, this.signature, this.width, this.height, transformation, cls, this.options);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown strategy: " + encodeStrategy);
            }
            LockedResource obtain = LockedResource.obtain(resource2);
            this.deferredEncodeManager.init(key, resourceEncoder, obtain);
            return obtain;
        }
        throw new Registry.NoResultEncoderAvailableException(resource2.get().getClass());
    }

    /* loaded from: classes.dex */
    public final class DecodeCallback<Z> implements DecodePath.DecodeCallback<Z> {
        private final DataSource dataSource;

        DecodeCallback(DataSource dataSource) {
            DecodeJob.this = r1;
            this.dataSource = dataSource;
        }

        @Override // com.bumptech.glide.load.engine.DecodePath.DecodeCallback
        @NonNull
        public Resource<Z> onResourceDecoded(@NonNull Resource<Z> resource) {
            return DecodeJob.this.onResourceDecoded(this.dataSource, resource);
        }
    }

    /* loaded from: classes.dex */
    public static class ReleaseManager {
        private boolean isEncodeComplete;
        private boolean isFailed;
        private boolean isReleased;

        ReleaseManager() {
        }

        synchronized boolean release(boolean z) {
            this.isReleased = true;
            return isComplete(z);
        }

        synchronized boolean onEncodeComplete() {
            this.isEncodeComplete = true;
            return isComplete(false);
        }

        synchronized boolean onFailed() {
            this.isFailed = true;
            return isComplete(false);
        }

        synchronized void reset() {
            this.isEncodeComplete = false;
            this.isReleased = false;
            this.isFailed = false;
        }

        private boolean isComplete(boolean z) {
            return (this.isFailed || z || this.isEncodeComplete) && this.isReleased;
        }
    }

    /* loaded from: classes.dex */
    public static class DeferredEncodeManager<Z> {
        private ResourceEncoder<Z> encoder;
        private Key key;
        private LockedResource<Z> toEncode;

        DeferredEncodeManager() {
        }

        /* JADX WARN: Multi-variable type inference failed */
        <X> void init(Key key, ResourceEncoder<X> resourceEncoder, LockedResource<X> lockedResource) {
            this.key = key;
            this.encoder = resourceEncoder;
            this.toEncode = lockedResource;
        }

        void encode(DiskCacheProvider diskCacheProvider, Options options) {
            GlideTrace.beginSection("DecodeJob.encode");
            try {
                diskCacheProvider.getDiskCache().put(this.key, new DataCacheWriter(this.encoder, this.toEncode, options));
            } finally {
                this.toEncode.unlock();
                GlideTrace.endSection();
            }
        }

        boolean hasResourceToEncode() {
            return this.toEncode != null;
        }

        void clear() {
            this.key = null;
            this.encoder = null;
            this.toEncode = null;
        }
    }
}
