package com.nextpage.hipetdemo.di.components;


import android.content.SharedPreferences;

import com.nextpage.hipetdemo.di.modules.AppModule;
import com.nextpage.hipetdemo.di.modules.DataModule;
import com.nextpage.hipetdemo.di.modules.NetModule;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Singleton
@Component(modules = {AppModule.class, NetModule.class, DataModule.class})
public interface NetComponent {
    Retrofit retrofit();

    OkHttpClient okHttpClient();

    SharedPreferences sharedPreferences();
}