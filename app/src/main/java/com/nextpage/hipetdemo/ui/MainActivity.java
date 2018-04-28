package com.nextpage.hipetdemo.ui;

import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nextpage.hipetdemo.Constant;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.beacon.BeaconData;
import com.nextpage.hipetdemo.beacon.BeaconService;
import com.nextpage.hipetdemo.databinding.ActivityMainBinding;
import com.nextpage.hipetdemo.fragment.Pet1Fragment;
import com.nextpage.hipetdemo.fragment.Pet2Fragment;
import com.nextpage.hipetdemo.fragment.SimpleDialog;
import com.nextpage.hipetdemo.util.AudioPlayer;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements Pet1Fragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener {

    private ActivityMainBinding mBinding;
    private BeaconService mService;
    private boolean mBound = false;
    private Pet1Fragment petFragment1;
    private Pet2Fragment petFragment2;
    private BluetoothAdapter bluetooth;
    private static final int PERMISSIONS_REQUEST_CODE_BLUETOOTH_ENABLE = 99;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
    private static final boolean IS_AT_LEAST_ANDROID_M = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isFindClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);
        mBinding.viewPager.addOnPageChangeListener(this);

        if (!isFineOrCoarseLocationPermissionGranted() && IS_AT_LEAST_ANDROID_M) {
            requestCoarseLocationPermission();
        }

        getSupportActionBar().setTitle("하이펫 - 토리");
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        turnOnBeacon();

        mBinding.includedLeftMenu.myPetButton.setSelected(true);
        onNewIntent(getIntent());

        isFindClicked = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("sound", false)) {
            if (intent.getBooleanExtra("isOut", false))
                new AudioPlayer().play(this, R.raw.pet);
            else
                new AudioPlayer().play(this, R.raw.doorbell);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BeaconService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (PreferenceManager.getDefaultSharedPreferences(this).contains(Constant.PREF_RECEIVE_MODE)) {
            menu.findItem(R.id.action_change_mode).setTitle(R.string.action_change_to_normal_mode);
        } else {
            menu.findItem(R.id.action_change_mode).setTitle(R.string.action_change_to_receive_mode);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mBinding.drawer.openDrawer(mBinding.includedLeftMenu.leftMenu);
                break;
            case R.id.action_change_mode:
                if (PreferenceManager.getDefaultSharedPreferences(this).contains(Constant.PREF_RECEIVE_MODE)) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().remove(Constant.PREF_RECEIVE_MODE).commit();
                    mService.setReceiveMode(false);
                } else {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constant.PREF_RECEIVE_MODE, true).commit();
                    mService.setReceiveMode(true);
                }
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                getSupportActionBar().setTitle("하이펫 - 토리");
                break;
            case 1:
                getSupportActionBar().setTitle("하이펫 - 모리");
                break;
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    petFragment1 = new Pet1Fragment();
                    return petFragment1;
                case 1:
                    petFragment2 = new Pet2Fragment();
                    return petFragment2;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BeaconService.LocalBinder binder = (BeaconService.LocalBinder) service;
            mService = binder.getService();
            if (mBound == false) {
                mBound = true;
//            if (mService.isLiveDataExist(Constant.MINOR)) {
//                mService.removeObservers(MainActivity.this, Constant.MINOR);
//            }
                MutableLiveData<Double> liveData = new MutableLiveData();
                BeaconData beaconData = new BeaconData(Constant.MINOR, 15);
                liveData.observe(MainActivity.this, accuracy -> petFragment1.setDistance(accuracy));
                mService.addLiveData(beaconData, liveData);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void turnOnBluetooth() {
        if (!bluetooth.isEnabled()) {
            startActivityForResult(new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE), PERMISSIONS_REQUEST_CODE_BLUETOOTH_ENABLE);
        }
    }

    public void turnOnBeacon() {
        if (!isFineOrCoarseLocationPermissionGranted() && IS_AT_LEAST_ANDROID_M) {
            requestCoarseLocationPermission();
            return;
        }
        if (bluetooth != null) {
            if (bluetooth.isEnabled() == false) {
                turnOnBluetooth();
            } else {
                startService(new Intent(this, BeaconService.class));
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @android.support.annotation.NonNull String[] permissions,
                                           @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final boolean isCoarseLocation = requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION;
        boolean permissionGranted = (grantResults.length != 0 && grantResults[0] == PERMISSION_GRANTED);

        if (isCoarseLocation && permissionGranted) {
            turnOnBeacon();
        }
    }

    private void requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }

    private boolean isFineOrCoarseLocationPermissionGranted() {
        boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
        boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

        return isAndroidMOrHigher && (isFineLocationPermissionGranted
                || isCoarseLocationPermissionGranted);
    }

    private boolean isGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
    }

    public void onReportClick(View view) {
        DialogFragment dialog = SimpleDialog.newInstance(getString(R.string.dialog_missing_report), new SimpleDialog.SimpleDialogListener() {
            @Override
            public void onDialogPositiveClick() {

            }

            @Override
            public void onDialogNegativeClick() {

            }
        });
        dialog.show(getSupportFragmentManager(), "SimpleDialog");
    }

    public void onMyPetClick(View view) {

    }

    public void onFindClick(View view) {
        isFindClicked = true;
        mBinding.drawer.closeDrawers();
        mBinding.drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (isFindClicked) {
                    Intent intent = new Intent(MainActivity.this, FindActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    isFindClicked = false;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.exit_msg, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
