package com.nextpage.hipetdemo.interfaces;

import com.nextpage.hipetdemo.model.SearchResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HiPetApiInterface {

    @GET("search/{num}")
    Observable<SearchResult> search(@Path("num") int number);


}