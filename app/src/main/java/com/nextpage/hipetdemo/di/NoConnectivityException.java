package com.nextpage.hipetdemo.di;

import java.io.IOException;

/**
 * Created by Jacob on 2017-09-12.
 */

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return "No connectivity exception";
    }

}