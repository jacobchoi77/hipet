package com.nextpage.hipetdemo.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.nextpage.hipetdemo.HiPetApplication;
import com.nextpage.hipetdemo.di.components.HiPetComponent;

/**
 * @author rebeccafranks
 * @since 2017/05/10.
 */

public class HiPetFactory extends ViewModelProvider.NewInstanceFactory {

    private HiPetApplication application;

    public HiPetFactory(HiPetApplication application) {
        this.application = application;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        T t = super.create(modelClass);
        if (t instanceof HiPetComponent.Injectable) {
            ((HiPetComponent.Injectable) t).inject(application.getHiPetComponent());
        }
        return t;
    }
}
