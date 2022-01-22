package okio;

import java.io.IOException;
import java.util.zip.Inflater;

/* loaded from: classes.dex */
public final class InflaterSource implements Source {
    private int bufferBytesHeldByInflater;
    private boolean closed;
    private final Inflater inflater;
    private final BufferedSource source;

    public InflaterSource(Source source, Inflater inflater) {
        this(Okio.buffer(source), inflater);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public InflaterSource(BufferedSource bufferedSource, Inflater inflater) {
        if (bufferedSource == null) {
            throw new IllegalArgumentException("source == null");
        } else if (inflater != null) {
            this.source = bufferedSource;
            this.inflater = inflater;
        } else {
            throw new IllegalArgumentException("inflater == null");
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x0056, code lost:
        releaseInflatedBytes();
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x005d, code lost:
        if (r1.pos != r1.limit) goto L_?;
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x005f, code lost:
        r7.head = r1.pop();
        okio.SegmentPool.recycle(r1);
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x0068, code lost:
        return -1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:39:?, code lost:
        return -1;
     */
    @Override // okio.Source
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long read(okio.Buffer r7, long r8) throws java.io.IOException {
        /*
            r6 = this;
            r0 = 0
            int r2 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            if (r2 < 0) goto L_0x007a
            boolean r2 = r6.closed
            if (r2 != 0) goto L_0x0072
            int r2 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            if (r2 != 0) goto L_0x000f
            return r0
        L_0x000f:
            boolean r0 = r6.refill()
            r1 = 1
            okio.Segment r1 = r7.writableSegment(r1)     // Catch: DataFormatException -> 0x006b
            int r2 = r1.limit     // Catch: DataFormatException -> 0x006b
            int r2 = 8192 - r2
            long r2 = (long) r2     // Catch: DataFormatException -> 0x006b
            long r2 = java.lang.Math.min(r8, r2)     // Catch: DataFormatException -> 0x006b
            int r3 = (int) r2     // Catch: DataFormatException -> 0x006b
            java.util.zip.Inflater r2 = r6.inflater     // Catch: DataFormatException -> 0x006b
            byte[] r4 = r1.data     // Catch: DataFormatException -> 0x006b
            int r5 = r1.limit     // Catch: DataFormatException -> 0x006b
            int r2 = r2.inflate(r4, r5, r3)     // Catch: DataFormatException -> 0x006b
            if (r2 <= 0) goto L_0x003a
            int r8 = r1.limit     // Catch: DataFormatException -> 0x006b
            int r8 = r8 + r2
            r1.limit = r8     // Catch: DataFormatException -> 0x006b
            long r8 = r7.size     // Catch: DataFormatException -> 0x006b
            long r0 = (long) r2     // Catch: DataFormatException -> 0x006b
            long r8 = r8 + r0
            r7.size = r8     // Catch: DataFormatException -> 0x006b
            return r0
        L_0x003a:
            java.util.zip.Inflater r2 = r6.inflater     // Catch: DataFormatException -> 0x006b
            boolean r2 = r2.finished()     // Catch: DataFormatException -> 0x006b
            if (r2 != 0) goto L_0x0056
            java.util.zip.Inflater r2 = r6.inflater     // Catch: DataFormatException -> 0x006b
            boolean r2 = r2.needsDictionary()     // Catch: DataFormatException -> 0x006b
            if (r2 == 0) goto L_0x004b
            goto L_0x0056
        L_0x004b:
            if (r0 != 0) goto L_0x004e
            goto L_0x000f
        L_0x004e:
            java.io.EOFException r7 = new java.io.EOFException     // Catch: DataFormatException -> 0x006b
            java.lang.String r8 = "source exhausted prematurely"
            r7.<init>(r8)     // Catch: DataFormatException -> 0x006b
            throw r7     // Catch: DataFormatException -> 0x006b
        L_0x0056:
            r6.releaseInflatedBytes()     // Catch: DataFormatException -> 0x006b
            int r8 = r1.pos     // Catch: DataFormatException -> 0x006b
            int r9 = r1.limit     // Catch: DataFormatException -> 0x006b
            if (r8 != r9) goto L_0x0068
            okio.Segment r8 = r1.pop()     // Catch: DataFormatException -> 0x006b
            r7.head = r8     // Catch: DataFormatException -> 0x006b
            okio.SegmentPool.recycle(r1)     // Catch: DataFormatException -> 0x006b
        L_0x0068:
            r7 = -1
            return r7
        L_0x006b:
            r7 = move-exception
            java.io.IOException r8 = new java.io.IOException
            r8.<init>(r7)
            throw r8
        L_0x0072:
            java.lang.IllegalStateException r7 = new java.lang.IllegalStateException
            java.lang.String r8 = "closed"
            r7.<init>(r8)
            throw r7
        L_0x007a:
            java.lang.IllegalArgumentException r7 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "byteCount < 0: "
            r0.append(r1)
            r0.append(r8)
            java.lang.String r8 = r0.toString()
            r7.<init>(r8)
            throw r7
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.InflaterSource.read(okio.Buffer, long):long");
    }

    public boolean refill() throws IOException {
        if (!this.inflater.needsInput()) {
            return false;
        }
        releaseInflatedBytes();
        if (this.inflater.getRemaining() != 0) {
            throw new IllegalStateException("?");
        } else if (this.source.exhausted()) {
            return true;
        } else {
            Segment segment = this.source.buffer().head;
            this.bufferBytesHeldByInflater = segment.limit - segment.pos;
            this.inflater.setInput(segment.data, segment.pos, this.bufferBytesHeldByInflater);
            return false;
        }
    }

    private void releaseInflatedBytes() throws IOException {
        int i = this.bufferBytesHeldByInflater;
        if (i != 0) {
            int remaining = i - this.inflater.getRemaining();
            this.bufferBytesHeldByInflater -= remaining;
            this.source.skip((long) remaining);
        }
    }

    @Override // okio.Source
    public Timeout timeout() {
        return this.source.timeout();
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.closed) {
            this.inflater.end();
            this.closed = true;
            this.source.close();
        }
    }
}
