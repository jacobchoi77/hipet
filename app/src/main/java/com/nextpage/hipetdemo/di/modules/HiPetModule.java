package com.nextpage.hipetdemo.di.modules;

import android.content.Context;

import com.nextpage.hipetdemo.HiPetApplication;
import com.nextpage.hipetdemo.di.scopes.UserScope;
import com.nextpage.hipetdemo.interfaces.HiPetApiInterface;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class HiPetModule {

    private HiPetApplication hiPetApplication;

    public HiPetModule(HiPetApplication hiPetApplication) {
        this.hiPetApplication = hiPetApplication;
    }

    @Provides
    Context applicationContext() {
        return hiPetApplication;
    }

    @Provides
    @UserScope
    public HiPetApiInterface providesHiPetInterface(Retrofit retrofit) {
        return retrofit.create(HiPetApiInterface.class);
    }
}
