package okhttp3.internal.p008ws;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;

/* renamed from: okhttp3.internal.ws.WebSocketReader */
/* loaded from: classes.dex */
final class WebSocketReader {
    boolean closed;
    final FrameCallback frameCallback;
    long frameLength;
    final boolean isClient;
    boolean isControlFrame;
    boolean isFinalFrame;
    private final Buffer.UnsafeCursor maskCursor;
    private final byte[] maskKey;
    int opcode;
    final BufferedSource source;
    private final Buffer controlFrameBuffer = new Buffer();
    private final Buffer messageFrameBuffer = new Buffer();

    /* renamed from: okhttp3.internal.ws.WebSocketReader$FrameCallback */
    /* loaded from: classes.dex */
    public interface FrameCallback {
        void onReadClose(int i, String str);

        void onReadMessage(String str) throws IOException;

        void onReadMessage(ByteString byteString) throws IOException;

        void onReadPing(ByteString byteString);

        void onReadPong(ByteString byteString);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebSocketReader(boolean z, BufferedSource bufferedSource, FrameCallback frameCallback) {
        byte[] bArr;
        if (bufferedSource == null) {
            throw new NullPointerException("source == null");
        } else if (frameCallback != null) {
            this.isClient = z;
            this.source = bufferedSource;
            this.frameCallback = frameCallback;
            Buffer.UnsafeCursor unsafeCursor = null;
            if (z) {
                bArr = null;
            } else {
                bArr = new byte[4];
            }
            this.maskKey = bArr;
            this.maskCursor = !z ? new Buffer.UnsafeCursor() : unsafeCursor;
        } else {
            throw new NullPointerException("frameCallback == null");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void processNextFrame() throws IOException {
        readHeader();
        if (this.isControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    /* JADX WARN: Finally extract failed */
    private void readHeader() throws IOException {
        if (!this.closed) {
            long timeoutNanos = this.source.timeout().timeoutNanos();
            this.source.timeout().clearTimeout();
            try {
                int readByte = this.source.readByte() & 255;
                this.source.timeout().timeout(timeoutNanos, TimeUnit.NANOSECONDS);
                this.opcode = readByte & 15;
                boolean z = true;
                this.isFinalFrame = (readByte & 128) != 0;
                this.isControlFrame = (readByte & 8) != 0;
                if (!this.isControlFrame || this.isFinalFrame) {
                    boolean z2 = (readByte & 64) != 0;
                    boolean z3 = (readByte & 32) != 0;
                    boolean z4 = (readByte & 16) != 0;
                    if (z2 || z3 || z4) {
                        throw new ProtocolException("Reserved flags are unsupported.");
                    }
                    int readByte2 = this.source.readByte() & 255;
                    if ((readByte2 & 128) == 0) {
                        z = false;
                    }
                    boolean z5 = this.isClient;
                    if (z == z5) {
                        throw new ProtocolException(z5 ? "Server-sent frames must not be masked." : "Client-sent frames must be masked.");
                    }
                    this.frameLength = (long) (readByte2 & 127);
                    long j = this.frameLength;
                    if (j == 126) {
                        this.frameLength = ((long) this.source.readShort()) & 65535;
                    } else if (j == 127) {
                        this.frameLength = this.source.readLong();
                        if (this.frameLength < 0) {
                            throw new ProtocolException("Frame length 0x" + Long.toHexString(this.frameLength) + " > 0x7FFFFFFFFFFFFFFF");
                        }
                    }
                    if (this.isControlFrame && this.frameLength > 125) {
                        throw new ProtocolException("Control frame must be less than 125B.");
                    } else if (z) {
                        this.source.readFully(this.maskKey);
                    }
                } else {
                    throw new ProtocolException("Control frames must be final.");
                }
            } catch (Throwable th) {
                this.source.timeout().timeout(timeoutNanos, TimeUnit.NANOSECONDS);
                throw th;
            }
        } else {
            throw new IOException("closed");
        }
    }

    private void readControlFrame() throws IOException {
        long j = this.frameLength;
        if (j > 0) {
            this.source.readFully(this.controlFrameBuffer, j);
            if (!this.isClient) {
                this.controlFrameBuffer.readAndWriteUnsafe(this.maskCursor);
                this.maskCursor.seek(0);
                WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
                this.maskCursor.close();
            }
        }
        switch (this.opcode) {
            case 8:
                short s = 1005;
                String str = "";
                long size = this.controlFrameBuffer.size();
                if (size != 1) {
                    if (size != 0) {
                        s = this.controlFrameBuffer.readShort();
                        str = this.controlFrameBuffer.readUtf8();
                        String closeCodeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(s);
                        if (closeCodeExceptionMessage != null) {
                            throw new ProtocolException(closeCodeExceptionMessage);
                        }
                    }
                    this.frameCallback.onReadClose(s, str);
                    this.closed = true;
                    return;
                }
                throw new ProtocolException("Malformed close payload length of 1.");
            case 9:
                this.frameCallback.onReadPing(this.controlFrameBuffer.readByteString());
                return;
            case 10:
                this.frameCallback.onReadPong(this.controlFrameBuffer.readByteString());
                return;
            default:
                throw new ProtocolException("Unknown control opcode: " + Integer.toHexString(this.opcode));
        }
    }

    private void readMessageFrame() throws IOException {
        int i = this.opcode;
        if (i == 1 || i == 2) {
            readMessage();
            if (i == 1) {
                this.frameCallback.onReadMessage(this.messageFrameBuffer.readUtf8());
            } else {
                this.frameCallback.onReadMessage(this.messageFrameBuffer.readByteString());
            }
        } else {
            throw new ProtocolException("Unknown opcode: " + Integer.toHexString(i));
        }
    }

    private void readUntilNonControlFrame() throws IOException {
        while (!this.closed) {
            readHeader();
            if (this.isControlFrame) {
                readControlFrame();
            } else {
                return;
            }
        }
    }

    private void readMessage() throws IOException {
        while (!this.closed) {
            long j = this.frameLength;
            if (j > 0) {
                this.source.readFully(this.messageFrameBuffer, j);
                if (!this.isClient) {
                    this.messageFrameBuffer.readAndWriteUnsafe(this.maskCursor);
                    this.maskCursor.seek(this.messageFrameBuffer.size() - this.frameLength);
                    WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
                    this.maskCursor.close();
                }
            }
            if (!this.isFinalFrame) {
                readUntilNonControlFrame();
                if (this.opcode != 0) {
                    throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(this.opcode));
                }
            } else {
                return;
            }
        }
        throw new IOException("closed");
    }
}