package p006me.goldze.mvvmhabit.http.download;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

/* renamed from: me.goldze.mvvmhabit.http.download.DownLoadStateBean */
/* loaded from: classes.dex */
public class DownLoadStateBean implements Serializable, Parcelable {
    public static final Parcelable.Creator<DownLoadStateBean> CREATOR = new Parcelable.Creator<DownLoadStateBean>() { // from class: me.goldze.mvvmhabit.http.download.DownLoadStateBean.1
        @Override // android.os.Parcelable.Creator
        public DownLoadStateBean createFromParcel(Parcel parcel) {
            return new DownLoadStateBean(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DownLoadStateBean[] newArray(int i) {
            return new DownLoadStateBean[i];
        }
    };
    long bytesLoaded;
    String tag;
    long total;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DownLoadStateBean(long j, long j2) {
        this.total = j;
        this.bytesLoaded = j2;
    }

    public DownLoadStateBean(long j, long j2, String str) {
        this.total = j;
        this.bytesLoaded = j2;
        this.tag = str;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long j) {
        this.total = j;
    }

    public long getBytesLoaded() {
        return this.bytesLoaded;
    }

    public void setBytesLoaded(long j) {
        this.bytesLoaded = j;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String str) {
        this.tag = str;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.total);
        parcel.writeLong(this.bytesLoaded);
        parcel.writeString(this.tag);
    }

    protected DownLoadStateBean(Parcel parcel) {
        this.total = parcel.readLong();
        this.bytesLoaded = parcel.readLong();
        this.tag = parcel.readString();
    }
}
