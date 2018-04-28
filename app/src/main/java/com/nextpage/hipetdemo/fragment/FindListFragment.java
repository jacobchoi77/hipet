package com.nextpage.hipetdemo.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nextpage.hipetdemo.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FindListFragment extends Fragment {

    public FindListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_list, container, false);
    }
}
