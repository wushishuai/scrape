package p006me.goldze.mvvmhabit.http;

/* renamed from: me.goldze.mvvmhabit.http.ResponseThrowable */
/* loaded from: classes.dex */
public class ResponseThrowable extends Exception {
    public int code;
    public String message;

    public ResponseThrowable(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }
}
