package com.nextpage.hipetdemo.di.modules;

import android.app.Application;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.nextpage.hipetdemo.di.ConnectivityInterceptor;
import com.nextpage.hipetdemo.di.LogJsonInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ir.mirrajabi.okhttpjsonmock.interceptors.OkHttpMockInterceptor;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.nextpage.hipetdemo.Constant.MOCK;

@Module
public class NetModule {

    String mBaseUrl;

    public NetModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache, Application application) {
        if (MOCK) {
            return new OkHttpClient.Builder()
                    .addInterceptor(new OkHttpMockInterceptor(application, 0))
                    .build();
        } else {
            return new OkHttpClient().newBuilder()
                    .addInterceptor(new ConnectivityInterceptor(application))
                    .addInterceptor(new LogJsonInterceptor()).cache(cache).build();
        }
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit;
    }


}
