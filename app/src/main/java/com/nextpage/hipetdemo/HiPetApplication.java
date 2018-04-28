package com.nextpage.hipetdemo;

import android.app.Application;

import com.nextpage.hipetdemo.di.components.DaggerHiPetComponent;
import com.nextpage.hipetdemo.di.components.DaggerNetComponent;
import com.nextpage.hipetdemo.di.components.HiPetComponent;
import com.nextpage.hipetdemo.di.components.NetComponent;
import com.nextpage.hipetdemo.di.modules.AppModule;
import com.nextpage.hipetdemo.di.modules.HiPetModule;
import com.nextpage.hipetdemo.di.modules.NetModule;


/**
 * Created by Jacob on 2017-09-11.
 */

public class HiPetApplication extends Application {
    private NetComponent mNetComponent;
    private HiPetComponent mHiPetComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule("http://next-page.co.kr/"))
                .build();
        mHiPetComponent = DaggerHiPetComponent.builder()
                .netComponent(mNetComponent)
                .hiPetModule(new HiPetModule(this))
                .build();

    }

    public NetComponent getNetComponent() {
        return mNetComponent;
    }

    public HiPetComponent getHiPetComponent() {
        return mHiPetComponent;
    }

}
