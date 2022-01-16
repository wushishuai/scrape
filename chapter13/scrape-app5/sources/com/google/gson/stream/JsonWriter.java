package com.google.gson.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
/* loaded from: classes.dex */
public class JsonWriter implements Closeable, Flushable {
    private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
    private static final String[] REPLACEMENT_CHARS = new String[128];
    private String deferredName;
    private boolean htmlSafe;
    private String indent;
    private boolean lenient;
    private final Writer out;
    private int[] stack = new int[32];
    private int stackSize = 0;
    private String separator = ":";
    private boolean serializeNulls = true;

    static {
        for (int i = 0; i <= 31; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", Integer.valueOf(i));
        }
        String[] strArr = REPLACEMENT_CHARS;
        strArr[34] = "\\\"";
        strArr[92] = "\\\\";
        strArr[9] = "\\t";
        strArr[8] = "\\b";
        strArr[10] = "\\n";
        strArr[13] = "\\r";
        strArr[12] = "\\f";
        HTML_SAFE_REPLACEMENT_CHARS = (String[]) strArr.clone();
        String[] strArr2 = HTML_SAFE_REPLACEMENT_CHARS;
        strArr2[60] = "\\u003c";
        strArr2[62] = "\\u003e";
        strArr2[38] = "\\u0026";
        strArr2[61] = "\\u003d";
        strArr2[39] = "\\u0027";
    }

    public JsonWriter(Writer out) {
        push(6);
        if (out != null) {
            this.out = out;
            return;
        }
        throw new NullPointerException("out == null");
    }

    public final void setIndent(String indent) {
        if (indent.length() == 0) {
            this.indent = null;
            this.separator = ":";
            return;
        }
        this.indent = indent;
        this.separator = ": ";
    }

    public final void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public final void setHtmlSafe(boolean htmlSafe) {
        this.htmlSafe = htmlSafe;
    }

    public final boolean isHtmlSafe() {
        return this.htmlSafe;
    }

    public final void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    public final boolean getSerializeNulls() {
        return this.serializeNulls;
    }

    public JsonWriter beginArray() throws IOException {
        writeDeferredName();
        return open(1, "[");
    }

    public JsonWriter endArray() throws IOException {
        return close(1, 2, "]");
    }

    public JsonWriter beginObject() throws IOException {
        writeDeferredName();
        return open(3, "{");
    }

    public JsonWriter endObject() throws IOException {
        return close(3, 5, "}");
    }

    private JsonWriter open(int empty, String openBracket) throws IOException {
        beforeValue();
        push(empty);
        this.out.write(openBracket);
        return this;
    }

    private JsonWriter close(int empty, int nonempty, String closeBracket) throws IOException {
        int context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalStateException("Nesting problem.");
        } else if (this.deferredName == null) {
            this.stackSize--;
            if (context == nonempty) {
                newline();
            }
            this.out.write(closeBracket);
            return this;
        } else {
            throw new IllegalStateException("Dangling name: " + this.deferredName);
        }
    }

    private void push(int newTop) {
        int i = this.stackSize;
        int[] iArr = this.stack;
        if (i == iArr.length) {
            int[] newStack = new int[i * 2];
            System.arraycopy(iArr, 0, newStack, 0, i);
            this.stack = newStack;
        }
        int[] iArr2 = this.stack;
        int i2 = this.stackSize;
        this.stackSize = i2 + 1;
        iArr2[i2] = newTop;
    }

    private int peek() {
        int i = this.stackSize;
        if (i != 0) {
            return this.stack[i - 1];
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    private void replaceTop(int topOfStack) {
        this.stack[this.stackSize - 1] = topOfStack;
    }

    public JsonWriter name(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        } else if (this.deferredName != null) {
            throw new IllegalStateException();
        } else if (this.stackSize != 0) {
            this.deferredName = name;
            return this;
        } else {
            throw new IllegalStateException("JsonWriter is closed.");
        }
    }

    private void writeDeferredName() throws IOException {
        if (this.deferredName != null) {
            beforeName();
            string(this.deferredName);
            this.deferredName = null;
        }
    }

    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        string(value);
        return this;
    }

    public JsonWriter jsonValue(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        this.out.append((CharSequence) value);
        return this;
    }

    public JsonWriter nullValue() throws IOException {
        if (this.deferredName != null) {
            if (this.serializeNulls) {
                writeDeferredName();
            } else {
                this.deferredName = null;
                return this;
            }
        }
        beforeValue();
        this.out.write("null");
        return this;
    }

    public JsonWriter value(boolean value) throws IOException {
        writeDeferredName();
        beforeValue();
        this.out.write(value ? "true" : "false");
        return this;
    }

    public JsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        this.out.write(value.booleanValue() ? "true" : "false");
        return this;
    }

    public JsonWriter value(double value) throws IOException {
        writeDeferredName();
        if (this.lenient || (!Double.isNaN(value) && !Double.isInfinite(value))) {
            beforeValue();
            this.out.append((CharSequence) Double.toString(value));
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public JsonWriter value(long value) throws IOException {
        writeDeferredName();
        beforeValue();
        this.out.write(Long.toString(value));
        return this;
    }

    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        String string = value.toString();
        if (this.lenient || (!string.equals("-Infinity") && !string.equals("Infinity") && !string.equals("NaN"))) {
            beforeValue();
            this.out.append((CharSequence) string);
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    @Override // java.io.Flushable
    public void flush() throws IOException {
        if (this.stackSize != 0) {
            this.out.flush();
            return;
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.out.close();
        int size = this.stackSize;
        if (size > 1 || (size == 1 && this.stack[size - 1] != 7)) {
            throw new IOException("Incomplete document");
        }
        this.stackSize = 0;
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x0034  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void string(java.lang.String r9) throws java.io.IOException {
        /*
            r8 = this;
            boolean r0 = r8.htmlSafe
            if (r0 == 0) goto L_0x0007
            java.lang.String[] r0 = com.google.gson.stream.JsonWriter.HTML_SAFE_REPLACEMENT_CHARS
            goto L_0x0009
        L_0x0007:
            java.lang.String[] r0 = com.google.gson.stream.JsonWriter.REPLACEMENT_CHARS
        L_0x0009:
            java.io.Writer r1 = r8.out
            java.lang.String r2 = "\""
            r1.write(r2)
            r1 = 0
            int r2 = r9.length()
            r3 = 0
        L_0x0016:
            if (r3 >= r2) goto L_0x0045
            char r4 = r9.charAt(r3)
            r5 = 128(0x80, float:1.794E-43)
            if (r4 >= r5) goto L_0x0025
            r5 = r0[r4]
            if (r5 != 0) goto L_0x0032
            goto L_0x0042
        L_0x0025:
            r5 = 8232(0x2028, float:1.1535E-41)
            if (r4 != r5) goto L_0x002c
            java.lang.String r5 = "\\u2028"
            goto L_0x0032
        L_0x002c:
            r5 = 8233(0x2029, float:1.1537E-41)
            if (r4 != r5) goto L_0x0042
            java.lang.String r5 = "\\u2029"
        L_0x0032:
            if (r1 >= r3) goto L_0x003b
            java.io.Writer r6 = r8.out
            int r7 = r3 - r1
            r6.write(r9, r1, r7)
        L_0x003b:
            java.io.Writer r6 = r8.out
            r6.write(r5)
            int r1 = r3 + 1
        L_0x0042:
            int r3 = r3 + 1
            goto L_0x0016
        L_0x0045:
            if (r1 >= r2) goto L_0x004e
            java.io.Writer r3 = r8.out
            int r4 = r2 - r1
            r3.write(r9, r1, r4)
        L_0x004e:
            java.io.Writer r3 = r8.out
            java.lang.String r4 = "\""
            r3.write(r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.gson.stream.JsonWriter.string(java.lang.String):void");
    }

    private void newline() throws IOException {
        if (this.indent != null) {
            this.out.write("\n");
            int size = this.stackSize;
            for (int i = 1; i < size; i++) {
                this.out.write(this.indent);
            }
        }
    }

    private void beforeName() throws IOException {
        int context = peek();
        if (context == 5) {
            this.out.write(44);
        } else if (context != 3) {
            throw new IllegalStateException("Nesting problem.");
        }
        newline();
        replaceTop(4);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private void beforeValue() throws IOException {
        switch (peek()) {
            case 1:
                replaceTop(2);
                newline();
                return;
            case 2:
                this.out.append(',');
                newline();
                return;
            case 3:
            case 5:
            default:
                throw new IllegalStateException("Nesting problem.");
            case 4:
                this.out.append((CharSequence) this.separator);
                replaceTop(5);
                return;
            case 6:
                break;
            case 7:
                if (!this.lenient) {
                    throw new IllegalStateException("JSON must have only one top-level value.");
                }
                break;
        }
        replaceTop(7);
    }
}
