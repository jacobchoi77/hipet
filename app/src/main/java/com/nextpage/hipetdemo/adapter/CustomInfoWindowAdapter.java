package com.nextpage.hipetdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.model.Report;

import java.util.Map;

/**
 * Created by jacobsFactory on 2017-12-01.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;
    private Map<Marker, Report> markerReportMap;
    private final View mContents;

    public CustomInfoWindowAdapter(Context mContext, Map<Marker, Report> markerReportMap) {
        this.mContext = mContext;
        this.markerReportMap = markerReportMap;
        mContents = LayoutInflater.from(mContext).inflate(R.layout.custom_info_contents, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        render(marker, mContents);
        return mContents;
    }

    private void render(Marker marker, View view) {
        final Report report = markerReportMap.get(marker);
        Glide.with(mContext).load(report.getPetImageUrl()).asBitmap().override(150, 150).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                e.printStackTrace();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (!isFromMemoryCache) marker.showInfoWindow();
                return false;
            }
        }).into((ImageView) view.findViewById(R.id.badge));
        ((TextView) view.findViewById(R.id.title)).setText(marker.getTitle());
        ((TextView) view.findViewById(R.id.snippet)).setText(marker.getSnippet());
    }
}
