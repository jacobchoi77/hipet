package com.nextpage.hipetdemo.di.components;


import com.nextpage.hipetdemo.di.modules.HiPetModule;
import com.nextpage.hipetdemo.di.scopes.UserScope;
import com.nextpage.hipetdemo.ui.FindActivity;
import com.nextpage.hipetdemo.viewmodel.FindViewModel;

import dagger.Component;

@UserScope
@Component(dependencies = NetComponent.class, modules = HiPetModule.class)
public interface HiPetComponent {
    void inject(FindViewModel findViewModel);

    interface Injectable {
        void inject(HiPetComponent hiPetComponent);
    }
}
