package retrofit2;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;
import javax.annotation.Nullable;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class ParameterHandler<T> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException;

    ParameterHandler() {
    }

    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() { // from class: retrofit2.ParameterHandler.1
            @Override // retrofit2.ParameterHandler
            /* bridge */ /* synthetic */ void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
                apply(requestBuilder, (Iterable) ((Iterable) obj));
            }

            void apply(RequestBuilder requestBuilder, @Nullable Iterable<T> iterable) throws IOException {
                if (iterable != null) {
                    for (T t : iterable) {
                        ParameterHandler.this.apply(requestBuilder, t);
                    }
                }
            }
        };
    }

    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() { // from class: retrofit2.ParameterHandler.2
            /* JADX WARN: Multi-variable type inference failed */
            @Override // retrofit2.ParameterHandler
            void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
                if (obj != null) {
                    int length = Array.getLength(obj);
                    for (int i = 0; i < length; i++) {
                        ParameterHandler.this.apply(requestBuilder, Array.get(obj, i));
                    }
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class RelativeUrl extends ParameterHandler<Object> {
        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable Object obj) {
            Utils.checkNotNull(obj, "@Url parameter is null.");
            requestBuilder.setRelativeUrl(obj);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Header(String str, Converter<T, String> converter) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            String convert;
            if (t != null && (convert = this.valueConverter.convert(t)) != null) {
                requestBuilder.addHeader(this.name, convert);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Path<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Path(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                requestBuilder.addPathParam(this.name, this.valueConverter.convert(t), this.encoded);
                return;
            }
            throw new IllegalArgumentException("Path parameter \"" + this.name + "\" value must not be null.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Query<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Query(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            String convert;
            if (t != null && (convert = this.valueConverter.convert(t)) != null) {
                requestBuilder.addQueryParam(this.name, convert, this.encoded);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class QueryName<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final Converter<T, String> nameConverter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public QueryName(Converter<T, String> converter, boolean z) {
            this.nameConverter = converter;
            this.encoded = z;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                requestBuilder.addQueryParam(this.nameConverter.convert(t), null, this.encoded);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final boolean encoded;
        private final Converter<T, String> valueConverter;

        @Override // retrofit2.ParameterHandler
        /* bridge */ /* synthetic */ void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
            apply(requestBuilder, (Map) ((Map) obj));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public QueryMap(Converter<T, String> converter, boolean z) {
            this.valueConverter = converter;
            this.encoded = z;
        }

        void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    String key = entry.getKey();
                    if (key != null) {
                        T value = entry.getValue();
                        if (value != null) {
                            String convert = this.valueConverter.convert(value);
                            if (convert != null) {
                                requestBuilder.addQueryParam(key, convert, this.encoded);
                            } else {
                                throw new IllegalArgumentException("Query map value '" + value + "' converted to null by " + this.valueConverter.getClass().getName() + " for key '" + key + "'.");
                            }
                        } else {
                            throw new IllegalArgumentException("Query map contained null value for key '" + key + "'.");
                        }
                    } else {
                        throw new IllegalArgumentException("Query map contained null key.");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Query map was null.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;

        @Override // retrofit2.ParameterHandler
        /* bridge */ /* synthetic */ void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
            apply(requestBuilder, (Map) ((Map) obj));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public HeaderMap(Converter<T, String> converter) {
            this.valueConverter = converter;
        }

        void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    String key = entry.getKey();
                    if (key != null) {
                        T value = entry.getValue();
                        if (value != null) {
                            requestBuilder.addHeader(key, this.valueConverter.convert(value));
                        } else {
                            throw new IllegalArgumentException("Header map contained null value for key '" + key + "'.");
                        }
                    } else {
                        throw new IllegalArgumentException("Header map contained null key.");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Header map was null.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Field<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Field(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            String convert;
            if (t != null && (convert = this.valueConverter.convert(t)) != null) {
                requestBuilder.addFormField(this.name, convert, this.encoded);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final boolean encoded;
        private final Converter<T, String> valueConverter;

        @Override // retrofit2.ParameterHandler
        /* bridge */ /* synthetic */ void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
            apply(requestBuilder, (Map) ((Map) obj));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public FieldMap(Converter<T, String> converter, boolean z) {
            this.valueConverter = converter;
            this.encoded = z;
        }

        void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    String key = entry.getKey();
                    if (key != null) {
                        T value = entry.getValue();
                        if (value != null) {
                            String convert = this.valueConverter.convert(value);
                            if (convert != null) {
                                requestBuilder.addFormField(key, convert, this.encoded);
                            } else {
                                throw new IllegalArgumentException("Field map value '" + value + "' converted to null by " + this.valueConverter.getClass().getName() + " for key '" + key + "'.");
                            }
                        } else {
                            throw new IllegalArgumentException("Field map contained null value for key '" + key + "'.");
                        }
                    } else {
                        throw new IllegalArgumentException("Field map contained null key.");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Field map was null.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Part<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;
        private final Headers headers;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Part(Headers headers, Converter<T, RequestBody> converter) {
            this.headers = headers;
            this.converter = converter;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) {
            if (t != null) {
                try {
                    requestBuilder.addPart(this.headers, this.converter.convert(t));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to convert " + t + " to RequestBody", e);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class RawPart extends ParameterHandler<MultipartBody.Part> {
        static final RawPart INSTANCE = new RawPart();

        private RawPart() {
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void apply(RequestBuilder requestBuilder, @Nullable MultipartBody.Part part) {
            if (part != null) {
                requestBuilder.addPart(part);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        private final String transferEncoding;
        private final Converter<T, RequestBody> valueConverter;

        @Override // retrofit2.ParameterHandler
        /* bridge */ /* synthetic */ void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
            apply(requestBuilder, (Map) ((Map) obj));
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public PartMap(Converter<T, RequestBody> converter, String str) {
            this.valueConverter = converter;
            this.transferEncoding = str;
        }

        void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    String key = entry.getKey();
                    if (key != null) {
                        T value = entry.getValue();
                        if (value != null) {
                            requestBuilder.addPart(Headers.m4of("Content-Disposition", "form-data; name=\"" + key + "\"", "Content-Transfer-Encoding", this.transferEncoding), this.valueConverter.convert(value));
                        } else {
                            throw new IllegalArgumentException("Part map contained null value for key '" + key + "'.");
                        }
                    } else {
                        throw new IllegalArgumentException("Part map contained null key.");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Part map was null.");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class Body<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;

        /* JADX INFO: Access modifiers changed from: package-private */
        public Body(Converter<T, RequestBody> converter) {
            this.converter = converter;
        }

        @Override // retrofit2.ParameterHandler
        void apply(RequestBuilder requestBuilder, @Nullable T t) {
            if (t != null) {
                try {
                    requestBuilder.setBody(this.converter.convert(t));
                } catch (IOException e) {
                    throw new RuntimeException("Unable to convert " + t + " to RequestBody", e);
                }
            } else {
                throw new IllegalArgumentException("Body parameter value must not be null.");
            }
        }
    }
}
