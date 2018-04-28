package com.nextpage.hipetdemo.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nextpage.hipetdemo.HiPetApplication;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.adapter.CustomInfoWindowAdapter;
import com.nextpage.hipetdemo.adapter.FindListAdapter;
import com.nextpage.hipetdemo.databinding.ActivityFindBinding;
import com.nextpage.hipetdemo.di.HiPetFactory;
import com.nextpage.hipetdemo.model.Report;
import com.nextpage.hipetdemo.model.SearchResult;
import com.nextpage.hipetdemo.viewmodel.FindViewModel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FindActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private ActivityFindBinding mBinding;
    private GoogleMap mMap;
    private boolean isList = true;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private FindViewModel findViewModel;
    private SearchResult mSearchResult;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Map<Marker, Report> markerReportMap;
    private LatLngBounds bounds;
    private boolean isMainClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_find);
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        findViewModel = ViewModelProviders.of(this,
                new HiPetFactory((HiPetApplication) getApplication())).get(FindViewModel.class);

        mBinding.fab.setOnClickListener(view -> toggle());
        mBinding.includedLeftMenu.findButton.setSelected(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mBinding.recyclerView.setLayoutManager(layoutManager);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mBinding.mapView.onCreate(mapViewBundle);

        mBinding.mapView.getMapAsync(this);
        mBinding.setIsLoading(true);
        search(1);
        isMainClicked = false;
    }

    private void search(int num) {
        findViewModel.search(num)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SearchResult>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull SearchResult searchResult) {
                        mBinding.setIsLoading(false);
                        if (searchResult != null) {
                            mSearchResult = searchResult;
                            showReports(searchResult);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mBinding.setIsLoading(false);
                        Toast.makeText(FindActivity.this, R.string.error_try_again, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onComplete() {

                    }

                });
    }

    private void showReports(SearchResult searchResult) {
        FindListAdapter adapter = new FindListAdapter(this, searchResult.getReports());
        mBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mBinding.recyclerView.setAdapter(adapter);
        markerReportMap = addMarkersToMap(mSearchResult.getReports());
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this, markerReportMap));
        mMap.setOnMapLoadedCallback(() -> mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50)));
    }

    private void toggle() {
        mBinding.viewSwitcher.showNext();
        isList = !isList;
        mBinding.fab.setImageResource(isList == true ? R.drawable.ic_map_black_24dp : R.drawable.ic_dashboard_black_24dp);
    }

    public void onMyPetClick(View view) {
        isMainClicked = true;
        mBinding.drawer.closeDrawers();
        mBinding.drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (isMainClicked) {
                    Intent intent = new Intent(FindActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    isMainClicked = false;
                    finish();
                }
            }
        });

    }

    public void onFindClick(View view) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mBinding.drawer.openDrawer(mBinding.includedLeftMenu.leftMenu);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mBinding.mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinding.mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBinding.mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapLoadedCallback(() -> mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37.520833, 127.116866))));
    }

    private Map<Marker, Report> addMarkersToMap(List<Report> reports) {
        Map<Marker, Report> markerReportMap = new HashMap<>();
        Report report;
        Marker marker;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < reports.size(); i++) {
            report = reports.get(i);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(report.getLatLng())
                    .title(report.getPetName())
                    .snippet(getString(R.string.report_date) + simpleDateFormat.format(report.getReportDate())));
            markerReportMap.put(marker, report);
            builder.include(report.getLatLng());
        }
        bounds = builder.build();
        return markerReportMap;
    }

    @Override
    protected void onPause() {
        mBinding.mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBinding.mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mBinding.mapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }
}
