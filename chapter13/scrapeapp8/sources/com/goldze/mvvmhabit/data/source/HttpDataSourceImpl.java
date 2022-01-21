package com.goldze.mvvmhabit.data.source;

import com.goldze.mvvmhabit.data.source.http.service.MovieApiService;
import com.goldze.mvvmhabit.entity.MovieEntity;
import com.goldze.mvvmhabit.utils.Encrypt;
import java.util.ArrayList;
import java.util.List;
import p005io.reactivex.Observable;

/* loaded from: classes.dex */
public class HttpDataSourceImpl implements HttpDataSource {
    private static volatile HttpDataSourceImpl INSTANCE = null;
    private MovieApiService apiService;

    public static HttpDataSourceImpl getInstance(MovieApiService apiService) {
        if (INSTANCE == null) {
            synchronized (HttpDataSourceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpDataSourceImpl(apiService);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private HttpDataSourceImpl(MovieApiService apiService) {
        this.apiService = apiService;
    }

    @Override // com.goldze.mvvmhabit.data.source.HttpDataSource
    public Observable<HttpResponse<MovieEntity>> index(int page, int limit) {
        int offset = (page - 1) * limit;
        List<String> strings = new ArrayList<>();
        strings.add(MovieApiService.indexPath);
        return this.apiService.index(offset, limit, Encrypt.encrypt(strings, offset));
    }
}
