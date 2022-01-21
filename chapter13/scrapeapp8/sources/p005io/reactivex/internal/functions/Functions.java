package p005io.reactivex.internal.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import p005io.reactivex.Notification;
import p005io.reactivex.Scheduler;
import p005io.reactivex.exceptions.OnErrorNotImplementedException;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.BiConsumer;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.BooleanSupplier;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.functions.Function3;
import p005io.reactivex.functions.Function4;
import p005io.reactivex.functions.Function5;
import p005io.reactivex.functions.Function6;
import p005io.reactivex.functions.Function7;
import p005io.reactivex.functions.Function8;
import p005io.reactivex.functions.Function9;
import p005io.reactivex.functions.LongConsumer;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.Timed;

/* renamed from: io.reactivex.internal.functions.Functions */
/* loaded from: classes.dex */
public final class Functions {
    static final Function<Object, Object> IDENTITY = new Identity();
    public static final Runnable EMPTY_RUNNABLE = new EmptyRunnable();
    public static final Action EMPTY_ACTION = new EmptyAction();
    static final Consumer<Object> EMPTY_CONSUMER = new EmptyConsumer();
    public static final Consumer<Throwable> ERROR_CONSUMER = new ErrorConsumer();
    public static final Consumer<Throwable> ON_ERROR_MISSING = new OnErrorMissingConsumer();
    public static final LongConsumer EMPTY_LONG_CONSUMER = new EmptyLongConsumer();
    static final Predicate<Object> ALWAYS_TRUE = new TruePredicate();
    static final Predicate<Object> ALWAYS_FALSE = new FalsePredicate();
    static final Callable<Object> NULL_SUPPLIER = new NullCallable();
    static final Comparator<Object> NATURAL_COMPARATOR = new NaturalObjectComparator();
    public static final Consumer<Subscription> REQUEST_MAX = new MaxRequestSubscription();

    private Functions() {
        throw new IllegalStateException("No instances!");
    }

    public static <T1, T2, R> Function<Object[], R> toFunction(BiFunction<? super T1, ? super T2, ? extends R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array2Func(f);
    }

    public static <T1, T2, T3, R> Function<Object[], R> toFunction(Function3<T1, T2, T3, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array3Func(f);
    }

    public static <T1, T2, T3, T4, R> Function<Object[], R> toFunction(Function4<T1, T2, T3, T4, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array4Func(f);
    }

    public static <T1, T2, T3, T4, T5, R> Function<Object[], R> toFunction(Function5<T1, T2, T3, T4, T5, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array5Func(f);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function<Object[], R> toFunction(Function6<T1, T2, T3, T4, T5, T6, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array6Func(f);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function<Object[], R> toFunction(Function7<T1, T2, T3, T4, T5, T6, T7, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array7Func(f);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function<Object[], R> toFunction(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array8Func(f);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function<Object[], R> toFunction(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> f) {
        ObjectHelper.requireNonNull(f, "f is null");
        return new Array9Func(f);
    }

    public static <T> Function<T, T> identity() {
        return (Function<T, T>) IDENTITY;
    }

    public static <T> Consumer<T> emptyConsumer() {
        return (Consumer<T>) EMPTY_CONSUMER;
    }

    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) ALWAYS_TRUE;
    }

    public static <T> Predicate<T> alwaysFalse() {
        return (Predicate<T>) ALWAYS_FALSE;
    }

    public static <T> Callable<T> nullSupplier() {
        return (Callable<T>) NULL_SUPPLIER;
    }

    public static <T> Comparator<T> naturalOrder() {
        return (Comparator<T>) NATURAL_COMPARATOR;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$FutureAction */
    /* loaded from: classes.dex */
    public static final class FutureAction implements Action {
        final Future<?> future;

        FutureAction(Future<?> future) {
            this.future = future;
        }

        @Override // p005io.reactivex.functions.Action
        public void run() throws Exception {
            this.future.get();
        }
    }

    public static Action futureAction(Future<?> future) {
        return new FutureAction(future);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$JustValue */
    /* loaded from: classes.dex */
    public static final class JustValue<T, U> implements Callable<U>, Function<T, U> {
        final U value;

        JustValue(U value) {
            this.value = value;
        }

        @Override // java.util.concurrent.Callable
        public U call() throws Exception {
            return this.value;
        }

        @Override // p005io.reactivex.functions.Function
        public U apply(T t) throws Exception {
            return this.value;
        }
    }

    public static <T> Callable<T> justCallable(T value) {
        return new JustValue(value);
    }

    public static <T, U> Function<T, U> justFunction(U value) {
        return new JustValue(value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$CastToClass */
    /* loaded from: classes.dex */
    public static final class CastToClass<T, U> implements Function<T, U> {
        final Class<U> clazz;

        CastToClass(Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override // p005io.reactivex.functions.Function
        public U apply(T t) throws Exception {
            return this.clazz.cast(t);
        }
    }

    public static <T, U> Function<T, U> castFunction(Class<U> target) {
        return new CastToClass(target);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ArrayListCapacityCallable */
    /* loaded from: classes.dex */
    public static final class ArrayListCapacityCallable<T> implements Callable<List<T>> {
        final int capacity;

        ArrayListCapacityCallable(int capacity) {
            this.capacity = capacity;
        }

        @Override // java.util.concurrent.Callable
        public List<T> call() throws Exception {
            return new ArrayList(this.capacity);
        }
    }

    public static <T> Callable<List<T>> createArrayList(int capacity) {
        return new ArrayListCapacityCallable(capacity);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$EqualsPredicate */
    /* loaded from: classes.dex */
    public static final class EqualsPredicate<T> implements Predicate<T> {
        final T value;

        EqualsPredicate(T value) {
            this.value = value;
        }

        @Override // p005io.reactivex.functions.Predicate
        public boolean test(T t) throws Exception {
            return ObjectHelper.equals(t, this.value);
        }
    }

    public static <T> Predicate<T> equalsWith(T value) {
        return new EqualsPredicate(value);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$HashSetCallable */
    /* loaded from: classes.dex */
    public enum HashSetCallable implements Callable<Set<Object>> {
        INSTANCE;

        @Override // java.util.concurrent.Callable
        public Set<Object> call() throws Exception {
            return new HashSet();
        }
    }

    public static <T> Callable<Set<T>> createHashSet() {
        return HashSetCallable.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$NotificationOnNext */
    /* loaded from: classes.dex */
    public static final class NotificationOnNext<T> implements Consumer<T> {
        final Consumer<? super Notification<T>> onNotification;

        NotificationOnNext(Consumer<? super Notification<T>> onNotification) {
            this.onNotification = onNotification;
        }

        @Override // p005io.reactivex.functions.Consumer
        public void accept(T v) throws Exception {
            this.onNotification.accept(Notification.createOnNext(v));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$NotificationOnError */
    /* loaded from: classes.dex */
    public static final class NotificationOnError<T> implements Consumer<Throwable> {
        final Consumer<? super Notification<T>> onNotification;

        NotificationOnError(Consumer<? super Notification<T>> onNotification) {
            this.onNotification = onNotification;
        }

        public void accept(Throwable v) throws Exception {
            this.onNotification.accept(Notification.createOnError(v));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$NotificationOnComplete */
    /* loaded from: classes.dex */
    public static final class NotificationOnComplete<T> implements Action {
        final Consumer<? super Notification<T>> onNotification;

        NotificationOnComplete(Consumer<? super Notification<T>> onNotification) {
            this.onNotification = onNotification;
        }

        @Override // p005io.reactivex.functions.Action
        public void run() throws Exception {
            this.onNotification.accept(Notification.createOnComplete());
        }
    }

    public static <T> Consumer<T> notificationOnNext(Consumer<? super Notification<T>> onNotification) {
        return new NotificationOnNext(onNotification);
    }

    public static <T> Consumer<Throwable> notificationOnError(Consumer<? super Notification<T>> onNotification) {
        return new NotificationOnError(onNotification);
    }

    public static <T> Action notificationOnComplete(Consumer<? super Notification<T>> onNotification) {
        return new NotificationOnComplete(onNotification);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ActionConsumer */
    /* loaded from: classes.dex */
    public static final class ActionConsumer<T> implements Consumer<T> {
        final Action action;

        ActionConsumer(Action action) {
            this.action = action;
        }

        @Override // p005io.reactivex.functions.Consumer
        public void accept(T t) throws Exception {
            this.action.run();
        }
    }

    public static <T> Consumer<T> actionConsumer(Action action) {
        return new ActionConsumer(action);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ClassFilter */
    /* loaded from: classes.dex */
    public static final class ClassFilter<T, U> implements Predicate<T> {
        final Class<U> clazz;

        ClassFilter(Class<U> clazz) {
            this.clazz = clazz;
        }

        @Override // p005io.reactivex.functions.Predicate
        public boolean test(T t) throws Exception {
            return this.clazz.isInstance(t);
        }
    }

    public static <T, U> Predicate<T> isInstanceOf(Class<U> clazz) {
        return new ClassFilter(clazz);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$BooleanSupplierPredicateReverse */
    /* loaded from: classes.dex */
    public static final class BooleanSupplierPredicateReverse<T> implements Predicate<T> {
        final BooleanSupplier supplier;

        BooleanSupplierPredicateReverse(BooleanSupplier supplier) {
            this.supplier = supplier;
        }

        @Override // p005io.reactivex.functions.Predicate
        public boolean test(T t) throws Exception {
            return !this.supplier.getAsBoolean();
        }
    }

    public static <T> Predicate<T> predicateReverseFor(BooleanSupplier supplier) {
        return new BooleanSupplierPredicateReverse(supplier);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$TimestampFunction */
    /* loaded from: classes.dex */
    public static final class TimestampFunction<T> implements Function<T, Timed<T>> {
        final Scheduler scheduler;
        final TimeUnit unit;

        TimestampFunction(TimeUnit unit, Scheduler scheduler) {
            this.unit = unit;
            this.scheduler = scheduler;
        }

        @Override // p005io.reactivex.functions.Function
        public Timed<T> apply(T t) throws Exception {
            return new Timed<>(t, this.scheduler.now(this.unit), this.unit);
        }
    }

    public static <T> Function<T, Timed<T>> timestampWith(TimeUnit unit, Scheduler scheduler) {
        return new TimestampFunction(unit, scheduler);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ToMapKeySelector */
    /* loaded from: classes.dex */
    public static final class ToMapKeySelector<K, T> implements BiConsumer<Map<K, T>, T> {
        private final Function<? super T, ? extends K> keySelector;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.functions.BiConsumer
        public /* bridge */ /* synthetic */ void accept(Object obj, Object obj2) throws Exception {
            accept((Map<K, Map<K, T>>) ((Map) obj), (Map<K, T>) obj2);
        }

        ToMapKeySelector(Function<? super T, ? extends K> keySelector) {
            this.keySelector = keySelector;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public void accept(Map<K, T> m, T t) throws Exception {
            m.put(this.keySelector.apply(t), t);
        }
    }

    public static <T, K> BiConsumer<Map<K, T>, T> toMapKeySelector(Function<? super T, ? extends K> keySelector) {
        return new ToMapKeySelector(keySelector);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ToMapKeyValueSelector */
    /* loaded from: classes.dex */
    public static final class ToMapKeyValueSelector<K, V, T> implements BiConsumer<Map<K, V>, T> {
        private final Function<? super T, ? extends K> keySelector;
        private final Function<? super T, ? extends V> valueSelector;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.functions.BiConsumer
        public /* bridge */ /* synthetic */ void accept(Object obj, Object obj2) throws Exception {
            accept((Map) ((Map) obj), (Map<K, V>) obj2);
        }

        ToMapKeyValueSelector(Function<? super T, ? extends V> valueSelector, Function<? super T, ? extends K> keySelector) {
            this.valueSelector = valueSelector;
            this.keySelector = keySelector;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public void accept(Map<K, V> m, T t) throws Exception {
            m.put(this.keySelector.apply(t), this.valueSelector.apply(t));
        }
    }

    public static <T, K, V> BiConsumer<Map<K, V>, T> toMapKeyValueSelector(Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector) {
        return new ToMapKeyValueSelector(valueSelector, keySelector);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ToMultimapKeyValueSelector */
    /* loaded from: classes.dex */
    public static final class ToMultimapKeyValueSelector<K, V, T> implements BiConsumer<Map<K, Collection<V>>, T> {
        private final Function<? super K, ? extends Collection<? super V>> collectionFactory;
        private final Function<? super T, ? extends K> keySelector;
        private final Function<? super T, ? extends V> valueSelector;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.functions.BiConsumer
        public /* bridge */ /* synthetic */ void accept(Object obj, Object obj2) throws Exception {
            accept((Map) ((Map) obj), (Map<K, Collection<V>>) obj2);
        }

        ToMultimapKeyValueSelector(Function<? super K, ? extends Collection<? super V>> collectionFactory, Function<? super T, ? extends V> valueSelector, Function<? super T, ? extends K> keySelector) {
            this.collectionFactory = collectionFactory;
            this.valueSelector = valueSelector;
            this.keySelector = keySelector;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public void accept(Map<K, Collection<V>> m, T t) throws Exception {
            Object apply = this.keySelector.apply(t);
            Collection collection = (Collection) m.get(apply);
            if (collection == null) {
                collection = (Collection) this.collectionFactory.apply(apply);
                m.put(apply, collection);
            }
            collection.add(this.valueSelector.apply(t));
        }
    }

    public static <T, K, V> BiConsumer<Map<K, Collection<V>>, T> toMultimapKeyValueSelector(Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector, Function<? super K, ? extends Collection<? super V>> collectionFactory) {
        return new ToMultimapKeyValueSelector(collectionFactory, valueSelector, keySelector);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$NaturalComparator */
    /* loaded from: classes.dex */
    public enum NaturalComparator implements Comparator<Object> {
        INSTANCE;

        @Override // java.util.Comparator
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    }

    public static <T> Comparator<T> naturalComparator() {
        return NaturalComparator.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$ListSorter */
    /* loaded from: classes.dex */
    public static final class ListSorter<T> implements Function<List<T>, List<T>> {
        final Comparator<? super T> comparator;

        @Override // p005io.reactivex.functions.Function
        public /* bridge */ /* synthetic */ Object apply(Object obj) throws Exception {
            return apply((List) ((List) obj));
        }

        ListSorter(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }

        public List<T> apply(List<T> v) {
            Collections.sort(v, this.comparator);
            return v;
        }
    }

    public static <T> Function<List<T>, List<T>> listSorter(Comparator<? super T> comparator) {
        return new ListSorter(comparator);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array2Func */
    /* loaded from: classes.dex */
    public static final class Array2Func<T1, T2, R> implements Function<Object[], R> {

        /* renamed from: f */
        final BiFunction<? super T1, ? super T2, ? extends R> f91f;

        Array2Func(BiFunction<? super T1, ? super T2, ? extends R> f) {
            this.f91f = f;
        }

        public R apply(Object[] a) throws Exception {
            if (a.length == 2) {
                return (R) this.f91f.apply(a[0], a[1]);
            }
            throw new IllegalArgumentException("Array of size 2 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array3Func */
    /* loaded from: classes.dex */
    public static final class Array3Func<T1, T2, T3, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function3<T1, T2, T3, R> f92f;

        Array3Func(Function3<T1, T2, T3, R> f) {
            this.f92f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 3) {
                return (R) this.f92f.apply(a[0], a[1], a[2]);
            }
            throw new IllegalArgumentException("Array of size 3 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array4Func */
    /* loaded from: classes.dex */
    public static final class Array4Func<T1, T2, T3, T4, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function4<T1, T2, T3, T4, R> f93f;

        Array4Func(Function4<T1, T2, T3, T4, R> f) {
            this.f93f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 4) {
                return (R) this.f93f.apply(a[0], a[1], a[2], a[3]);
            }
            throw new IllegalArgumentException("Array of size 4 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array5Func */
    /* loaded from: classes.dex */
    public static final class Array5Func<T1, T2, T3, T4, T5, R> implements Function<Object[], R> {

        /* renamed from: f */
        private final Function5<T1, T2, T3, T4, T5, R> f94f;

        Array5Func(Function5<T1, T2, T3, T4, T5, R> f) {
            this.f94f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 5) {
                return (R) this.f94f.apply(a[0], a[1], a[2], a[3], a[4]);
            }
            throw new IllegalArgumentException("Array of size 5 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array6Func */
    /* loaded from: classes.dex */
    public static final class Array6Func<T1, T2, T3, T4, T5, T6, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function6<T1, T2, T3, T4, T5, T6, R> f95f;

        Array6Func(Function6<T1, T2, T3, T4, T5, T6, R> f) {
            this.f95f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 6) {
                return (R) this.f95f.apply(a[0], a[1], a[2], a[3], a[4], a[5]);
            }
            throw new IllegalArgumentException("Array of size 6 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array7Func */
    /* loaded from: classes.dex */
    public static final class Array7Func<T1, T2, T3, T4, T5, T6, T7, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function7<T1, T2, T3, T4, T5, T6, T7, R> f96f;

        Array7Func(Function7<T1, T2, T3, T4, T5, T6, T7, R> f) {
            this.f96f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 7) {
                return (R) this.f96f.apply(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            }
            throw new IllegalArgumentException("Array of size 7 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array8Func */
    /* loaded from: classes.dex */
    public static final class Array8Func<T1, T2, T3, T4, T5, T6, T7, T8, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f97f;

        Array8Func(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
            this.f97f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 8) {
                return (R) this.f97f.apply(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]);
            }
            throw new IllegalArgumentException("Array of size 8 expected but got " + a.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.functions.Functions$Array9Func */
    /* loaded from: classes.dex */
    public static final class Array9Func<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> implements Function<Object[], R> {

        /* renamed from: f */
        final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> f98f;

        Array9Func(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> f) {
            this.f98f = f;
        }

        /* JADX WARN: Multi-variable type inference failed */
        public R apply(Object[] a) throws Exception {
            if (a.length == 9) {
                return (R) this.f98f.apply(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8]);
            }
            throw new IllegalArgumentException("Array of size 9 expected but got " + a.length);
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$Identity */
    /* loaded from: classes.dex */
    static final class Identity implements Function<Object, Object> {
        Identity() {
        }

        @Override // p005io.reactivex.functions.Function
        public Object apply(Object v) {
            return v;
        }

        public String toString() {
            return "IdentityFunction";
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$EmptyRunnable */
    /* loaded from: classes.dex */
    static final class EmptyRunnable implements Runnable {
        EmptyRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
        }

        @Override // java.lang.Object
        public String toString() {
            return "EmptyRunnable";
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$EmptyAction */
    /* loaded from: classes.dex */
    static final class EmptyAction implements Action {
        EmptyAction() {
        }

        @Override // p005io.reactivex.functions.Action
        public void run() {
        }

        public String toString() {
            return "EmptyAction";
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$EmptyConsumer */
    /* loaded from: classes.dex */
    static final class EmptyConsumer implements Consumer<Object> {
        EmptyConsumer() {
        }

        @Override // p005io.reactivex.functions.Consumer
        public void accept(Object v) {
        }

        public String toString() {
            return "EmptyConsumer";
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$ErrorConsumer */
    /* loaded from: classes.dex */
    static final class ErrorConsumer implements Consumer<Throwable> {
        ErrorConsumer() {
        }

        public void accept(Throwable error) {
            RxJavaPlugins.onError(error);
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$OnErrorMissingConsumer */
    /* loaded from: classes.dex */
    static final class OnErrorMissingConsumer implements Consumer<Throwable> {
        OnErrorMissingConsumer() {
        }

        public void accept(Throwable error) {
            RxJavaPlugins.onError(new OnErrorNotImplementedException(error));
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$EmptyLongConsumer */
    /* loaded from: classes.dex */
    static final class EmptyLongConsumer implements LongConsumer {
        EmptyLongConsumer() {
        }

        @Override // p005io.reactivex.functions.LongConsumer
        public void accept(long v) {
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$TruePredicate */
    /* loaded from: classes.dex */
    static final class TruePredicate implements Predicate<Object> {
        TruePredicate() {
        }

        @Override // p005io.reactivex.functions.Predicate
        public boolean test(Object o) {
            return true;
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$FalsePredicate */
    /* loaded from: classes.dex */
    static final class FalsePredicate implements Predicate<Object> {
        FalsePredicate() {
        }

        @Override // p005io.reactivex.functions.Predicate
        public boolean test(Object o) {
            return false;
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$NullCallable */
    /* loaded from: classes.dex */
    static final class NullCallable implements Callable<Object> {
        NullCallable() {
        }

        @Override // java.util.concurrent.Callable
        public Object call() {
            return null;
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$NaturalObjectComparator */
    /* loaded from: classes.dex */
    static final class NaturalObjectComparator implements Comparator<Object> {
        NaturalObjectComparator() {
        }

        @Override // java.util.Comparator
        public int compare(Object a, Object b) {
            return ((Comparable) a).compareTo(b);
        }
    }

    /* renamed from: io.reactivex.internal.functions.Functions$MaxRequestSubscription */
    /* loaded from: classes.dex */
    static final class MaxRequestSubscription implements Consumer<Subscription> {
        MaxRequestSubscription() {
        }

        public void accept(Subscription t) throws Exception {
            t.request(Long.MAX_VALUE);
        }
    }

    public static <T> Consumer<T> boundedConsumer(int bufferSize) {
        return new BoundedConsumer(bufferSize);
    }

    /* renamed from: io.reactivex.internal.functions.Functions$BoundedConsumer */
    /* loaded from: classes.dex */
    public static class BoundedConsumer implements Consumer<Subscription> {
        final int bufferSize;

        BoundedConsumer(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public void accept(Subscription s) throws Exception {
            s.request((long) this.bufferSize);
        }
    }
}
