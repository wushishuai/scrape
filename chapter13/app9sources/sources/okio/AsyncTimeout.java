package okio;

import android.support.p000v4.media.session.PlaybackStateCompat;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/* loaded from: classes.dex */
public class AsyncTimeout extends Timeout {
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);
    private static final int TIMEOUT_WRITE_SIZE = 65536;
    @Nullable
    static AsyncTimeout head;
    private boolean inQueue;
    @Nullable
    private AsyncTimeout next;
    private long timeoutAt;

    protected void timedOut() {
    }

    public final void enter() {
        if (!this.inQueue) {
            long timeoutNanos = timeoutNanos();
            boolean hasDeadline = hasDeadline();
            if (timeoutNanos != 0 || hasDeadline) {
                this.inQueue = true;
                scheduleTimeout(this, timeoutNanos, hasDeadline);
                return;
            }
            return;
        }
        throw new IllegalStateException("Unbalanced enter/exit");
    }

    private static synchronized void scheduleTimeout(AsyncTimeout asyncTimeout, long j, boolean z) {
        synchronized (AsyncTimeout.class) {
            if (head == null) {
                head = new AsyncTimeout();
                new Watchdog().start();
            }
            long nanoTime = System.nanoTime();
            if (j != 0 && z) {
                asyncTimeout.timeoutAt = Math.min(j, asyncTimeout.deadlineNanoTime() - nanoTime) + nanoTime;
            } else if (j != 0) {
                asyncTimeout.timeoutAt = j + nanoTime;
            } else if (z) {
                asyncTimeout.timeoutAt = asyncTimeout.deadlineNanoTime();
            } else {
                throw new AssertionError();
            }
            long remainingNanos = asyncTimeout.remainingNanos(nanoTime);
            AsyncTimeout asyncTimeout2 = head;
            while (asyncTimeout2.next != null && remainingNanos >= asyncTimeout2.next.remainingNanos(nanoTime)) {
                asyncTimeout2 = asyncTimeout2.next;
            }
            asyncTimeout.next = asyncTimeout2.next;
            asyncTimeout2.next = asyncTimeout;
            if (asyncTimeout2 == head) {
                AsyncTimeout.class.notify();
            }
        }
    }

    public final boolean exit() {
        if (!this.inQueue) {
            return false;
        }
        this.inQueue = false;
        return cancelScheduledTimeout(this);
    }

    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout asyncTimeout) {
        synchronized (AsyncTimeout.class) {
            for (AsyncTimeout asyncTimeout2 = head; asyncTimeout2 != null; asyncTimeout2 = asyncTimeout2.next) {
                if (asyncTimeout2.next == asyncTimeout) {
                    asyncTimeout2.next = asyncTimeout.next;
                    asyncTimeout.next = null;
                    return false;
                }
            }
            return true;
        }
    }

    private long remainingNanos(long j) {
        return this.timeoutAt - j;
    }

    public final Sink sink(final Sink sink) {
        return new Sink() { // from class: okio.AsyncTimeout.1
            @Override // okio.Sink
            public void write(Buffer buffer, long j) throws IOException {
                Util.checkOffsetAndCount(buffer.size, 0, j);
                while (true) {
                    long j2 = 0;
                    if (j > 0) {
                        Segment segment = buffer.head;
                        while (true) {
                            if (j2 >= PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
                                break;
                            }
                            j2 += (long) (segment.limit - segment.pos);
                            if (j2 >= j) {
                                j2 = j;
                                break;
                            }
                            segment = segment.next;
                        }
                        boolean z = false;
                        AsyncTimeout.this.enter();
                        try {
                            try {
                                sink.write(buffer, j2);
                                j -= j2;
                                z = true;
                            } catch (IOException e) {
                                throw AsyncTimeout.this.exit(e);
                            }
                        } finally {
                            AsyncTimeout.this.exit(z);
                        }
                    } else {
                        return;
                    }
                }
            }

            @Override // okio.Sink, java.io.Flushable
            public void flush() throws IOException {
                try {
                    AsyncTimeout.this.enter();
                    try {
                        sink.flush();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Sink, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                try {
                    AsyncTimeout.this.enter();
                    try {
                        sink.close();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Sink
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            @Override // java.lang.Object
            public String toString() {
                return "AsyncTimeout.sink(" + sink + ")";
            }
        };
    }

    public final Source source(final Source source) {
        return new Source() { // from class: okio.AsyncTimeout.2
            @Override // okio.Source
            public long read(Buffer buffer, long j) throws IOException {
                try {
                    AsyncTimeout.this.enter();
                    try {
                        long read = source.read(buffer, j);
                        AsyncTimeout.this.exit(true);
                        return read;
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                try {
                    try {
                        source.close();
                        AsyncTimeout.this.exit(true);
                    } catch (IOException e) {
                        throw AsyncTimeout.this.exit(e);
                    }
                } catch (Throwable th) {
                    AsyncTimeout.this.exit(false);
                    throw th;
                }
            }

            @Override // okio.Source
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            @Override // java.lang.Object
            public String toString() {
                return "AsyncTimeout.source(" + source + ")";
            }
        };
    }

    final void exit(boolean z) throws IOException {
        if (exit() && z) {
            throw newTimeoutException(null);
        }
    }

    final IOException exit(IOException iOException) throws IOException {
        if (!exit()) {
            return iOException;
        }
        return newTimeoutException(iOException);
    }

    protected IOException newTimeoutException(@Nullable IOException iOException) {
        InterruptedIOException interruptedIOException = new InterruptedIOException("timeout");
        if (iOException != null) {
            interruptedIOException.initCause(iOException);
        }
        return interruptedIOException;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class Watchdog extends Thread {
        Watchdog() {
            super("Okio Watchdog");
            setDaemon(true);
        }

        /* JADX WARN: Code restructure failed: missing block: B:14:0x0015, code lost:
            r1.timedOut();
         */
        @Override // java.lang.Thread, java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r3 = this;
            L_0x0000:
                java.lang.Class<okio.AsyncTimeout> r0 = okio.AsyncTimeout.class
                monitor-enter(r0)     // Catch: InterruptedException -> 0x0000
                okio.AsyncTimeout r1 = okio.AsyncTimeout.awaitTimeout()     // Catch: all -> 0x0019
                if (r1 != 0) goto L_0x000b
                monitor-exit(r0)     // Catch: all -> 0x0019
                goto L_0x0000
            L_0x000b:
                okio.AsyncTimeout r2 = okio.AsyncTimeout.head     // Catch: all -> 0x0019
                if (r1 != r2) goto L_0x0014
                r1 = 0
                okio.AsyncTimeout.head = r1     // Catch: all -> 0x0019
                monitor-exit(r0)     // Catch: all -> 0x0019
                return
            L_0x0014:
                monitor-exit(r0)     // Catch: all -> 0x0019
                r1.timedOut()     // Catch: InterruptedException -> 0x0000
                goto L_0x0000
            L_0x0019:
                r1 = move-exception
                monitor-exit(r0)     // Catch: all -> 0x0019
                throw r1     // Catch: InterruptedException -> 0x0000
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: okio.AsyncTimeout.Watchdog.run():void");
        }
    }

    @Nullable
    static AsyncTimeout awaitTimeout() throws InterruptedException {
        AsyncTimeout asyncTimeout = head.next;
        if (asyncTimeout == null) {
            long nanoTime = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            if (head.next != null || System.nanoTime() - nanoTime < IDLE_TIMEOUT_NANOS) {
                return null;
            }
            return head;
        }
        long remainingNanos = asyncTimeout.remainingNanos(System.nanoTime());
        if (remainingNanos > 0) {
            long j = remainingNanos / 1000000;
            AsyncTimeout.class.wait(j, (int) (remainingNanos - (1000000 * j)));
            return null;
        }
        head.next = asyncTimeout.next;
        asyncTimeout.next = null;
        return asyncTimeout;
    }
}
