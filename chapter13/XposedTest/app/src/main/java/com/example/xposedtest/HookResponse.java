package com.example.xposedtest;

import java.io.IOException;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HookResponse implements IXposedHookLoadPackage {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.goldze.mvvmhabit")){
            XposedBridge.log("Hooked com.goldze.mvvmhabit package");
            final Class clazz = lpparam.classLoader.loadClass("com.goldze.mvvmhabit.data.source.HttpResponse");
            XposedHelpers.findAndHookMethod(clazz, "getResults", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Called beforedHookedMethod");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Called afterHookedMethod");
                    List result = (List) param.getResult();
                    for (Object o:result){
                        XposedBridge.log(o.toString());
                        String entity = o.toString();
                        XposedBridge.log("MovieEntity"+entity);
                        sendDataToServer(entity);
                    }
                }
            });
        }
    }
    public void sendDataToServer(String data) throws IOException{
        String server = "http://<SERVER_HOST>/data";
        RequestBody formBody = new FormBody.Builder()
                .add("data",data)
                .add("from","Xposed")
                .add("crawled_at",String.valueOf(System.currentTimeMillis()))
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(server)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                XposedBridge.log("Save failed:"+e.getMessage());
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                XposedBridge.log("Save successfully:"+response.body().string());


            }
        });
    }

}
