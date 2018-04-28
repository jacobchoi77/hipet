package com.nextpage.hipetdemo.ui;

import android.Manifest;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

import com.nextpage.hipetdemo.Constant;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.beacon.BeaconData;
import com.nextpage.hipetdemo.beacon.BeaconService;
import com.nextpage.hipetdemo.databinding.ActivityPopupBinding;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class PopupActivity extends AppCompatActivity {

    private ActivityPopupBinding mBinding;
    private BeaconService mService;
    private boolean mBound = false;
    private BluetoothAdapter bluetooth;
    private static final int PERMISSIONS_REQUEST_CODE_BLUETOOTH_ENABLE = 99;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
    private static final int PERMISSIONS_REQUEST_CODE_PHONE_CALL = 10000;
    private static final boolean IS_AT_LEAST_ANDROID_M = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_popup);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        getWindow().setLayout((int) (metrics.widthPixels * 0.90), (int) (metrics.widthPixels * 0.90));
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!isFineOrCoarseLocationPermissionGranted() && IS_AT_LEAST_ANDROID_M) {
            requestCoarseLocationPermission();
        }

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        turnOnBeacon();

        onNewIntent(getIntent());
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
                liveData.observe(PopupActivity.this, accuracy -> setDistance(accuracy));
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
        final boolean isPhoneCall = requestCode == PERMISSIONS_REQUEST_CODE_PHONE_CALL;
        boolean permissionGranted = (grantResults.length != 0 && grantResults[0] == PERMISSION_GRANTED);

        if (isCoarseLocation && permissionGranted) {
            turnOnBeacon();
        } else if (isPhoneCall && permissionGranted) {
            phoneCall();
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

    public void setDistance(double accuracy) {
        mBinding.distanceTextView.setText(String.format("%.1f", accuracy));
    }

    public void onCallClick(View view) {
        if (Build.VERSION.SDK_INT < 23) {
            phoneCall();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                phoneCall();
            } else {
                final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, PERMISSIONS_REQUEST_CODE_PHONE_CALL);
            }
        }
    }

    public void onEmailClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "jacobchoi77@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "당신의 펫이 발견되었어요.");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "여기로 전화 주세요. 010-5564-7709");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void phoneCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:01055647709"));
            startActivity(callIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}
