package com.google.gson;

/* loaded from: classes.dex */
public enum LongSerializationPolicy {
    DEFAULT {
        @Override // com.google.gson.LongSerializationPolicy
        public JsonElement serialize(Long value) {
            return new JsonPrimitive((Number) value);
        }
    },
    STRING {
        @Override // com.google.gson.LongSerializationPolicy
        public JsonElement serialize(Long value) {
            return new JsonPrimitive(String.valueOf(value));
        }
    };

    public abstract JsonElement serialize(Long l);
}
