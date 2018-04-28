package com.nextpage.hipetdemo.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.nextpage.hipetdemo.di.components.HiPetComponent;
import com.nextpage.hipetdemo.interfaces.HiPetApiInterface;
import com.nextpage.hipetdemo.model.SearchResult;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Jacob on 2017-09-12.
 */

public class FindViewModel extends ViewModel implements HiPetComponent.Injectable {
    @Inject
    HiPetApiInterface hiPetApiInterface;

    public Observable<SearchResult> search(int number) {
        return hiPetApiInterface.search(number);
    }

    @Override
    public void inject(HiPetComponent hiPetComponent) {
        hiPetComponent.inject(this);
    }

}
