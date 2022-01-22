package p006me.goldze.mvvmhabit.http.download;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import p006me.goldze.mvvmhabit.bus.RxBus;

/* renamed from: me.goldze.mvvmhabit.http.download.ProgressResponseBody */
/* loaded from: classes.dex */
public class ProgressResponseBody extends ResponseBody {
    private BufferedSource bufferedSource;
    private ResponseBody responseBody;
    private String tag;

    public ProgressResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public ProgressResponseBody(ResponseBody responseBody, String str) {
        this.responseBody = responseBody;
        this.tag = str;
    }

    @Override // okhttp3.ResponseBody
    public MediaType contentType() {
        return this.responseBody.contentType();
    }

    @Override // okhttp3.ResponseBody
    public long contentLength() {
        return this.responseBody.contentLength();
    }

    @Override // okhttp3.ResponseBody
    public BufferedSource source() {
        if (this.bufferedSource == null) {
            this.bufferedSource = Okio.buffer(source(this.responseBody.source()));
        }
        return this.bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) { // from class: me.goldze.mvvmhabit.http.download.ProgressResponseBody.1
            long bytesReaded = 0;

            @Override // okio.ForwardingSource, okio.Source
            public long read(Buffer buffer, long j) throws IOException {
                long read = super.read(buffer, j);
                this.bytesReaded += read == -1 ? 0 : read;
                RxBus.getDefault().post(new DownLoadStateBean(ProgressResponseBody.this.contentLength(), this.bytesReaded, ProgressResponseBody.this.tag));
                return read;
            }
        };
    }
}
