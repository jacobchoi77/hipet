package com.nextpage.hipetdemo.beacon;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.github.pwittchen.reactivebeacons.library.rx2.ReactiveBeacons;
import com.nextpage.hipetdemo.Constant;
import com.nextpage.hipetdemo.R;
import com.nextpage.hipetdemo.ui.MainActivity;
import com.nextpage.hipetdemo.ui.PopupActivity;
import com.nextpage.hipetdemo.util.AudioPlayer;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

import static com.nextpage.hipetdemo.Constant.NOTI_CHANNEL_ID;
import static com.nextpage.hipetdemo.Constant.NOTI_CHANNEL_NAME;

public class BeaconService extends Service {

    private ReactiveBeacons reactiveBeacons;
    private Disposable subscription;
    public static final int NOTI_ID_STOP = 333;
    public static final int NOTI_ID_MAIN = NOTI_ID_STOP + 1;
    public static final int NOTI_ID_POPUP = NOTI_ID_STOP + 2;

    private Map<Integer, BeaconData> beaconDatas = new HashMap<>();
    private Map<Integer, MutableLiveData<Double>> mutableLiveDataMap = new HashMap<>();
    private final IBinder mBinder = new LocalBinder();
    private boolean isOut = false;
    private boolean isReceiveMode = false;

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reactiveBeacons = new ReactiveBeacons(this);
        isReceiveMode = PreferenceManager.getDefaultSharedPreferences(this).contains(Constant.PREF_RECEIVE_MODE);
        startSubscription();
        showStopNotification();
    }

    public class LocalBinder extends Binder {
        public BeaconService getService() {
            return BeaconService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showStopNotification() {
        Intent stopIntent = new Intent(this, StopServiceReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                this, (int) System.currentTimeMillis(), stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getBroadcast(
                this, (int) System.currentTimeMillis(), mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_service_title))
                    .setContentText(getString(R.string.noti_service_content))
                    .setSmallIcon(R.drawable.ic_info_outline_black_24dp)
                    .setAutoCancel(true)
                    .setChannelId(NOTI_CHANNEL_ID)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), stopPendingIntent).build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_service_title))
                    .setContentText(getString(R.string.noti_service_content))
                    .setSmallIcon(R.drawable.ic_info_outline_black_24dp)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), stopPendingIntent).build();
        }

        startForeground(NOTI_ID_STOP, notification);
    }

    private void showMainNotification(boolean isOut, int minor) {
        Intent mainIntent = new Intent(BeaconService.this, MainActivity.class);
        mainIntent.putExtra("minor", minor);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_title))
                    .setSmallIcon(R.drawable.ic_pets_black_24dp)
                    .setContentText(isOut == true ? getString(R.string.noti_out_title) : getString(R.string.noti_in_title))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setChannelId(NOTI_CHANNEL_ID)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_title))
                    .setSmallIcon(R.drawable.ic_pets_black_24dp)
                    .setContentText(isOut == true ? getString(R.string.noti_out_title) : getString(R.string.noti_in_title))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }
        new Thread(() -> {
            if (isOut)
                new AudioPlayer().play(this, R.raw.pet);
            else
                new AudioPlayer().play(this, R.raw.doorbell);
        }).start();

//        if (isOut)
//            notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pet);
//        else
//            notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.doorbell);
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
        notificationManager.notify(NOTI_ID_MAIN, notification);
    }

    private void showPopupNotification(int minor) {
        Intent popupIntent = new Intent(BeaconService.this, PopupActivity.class);
        popupIntent.putExtra("minor", minor);
        popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), popupIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTI_CHANNEL_ID, NOTI_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_title))
                    .setSmallIcon(R.drawable.ic_pets_black_24dp)
                    .setContentText(getString(R.string.noti_in_title_popup))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setChannelId(NOTI_CHANNEL_ID)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.noti_title))
                    .setSmallIcon(R.drawable.ic_pets_black_24dp)
                    .setContentText(getString(R.string.noti_in_title_popup))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
        notificationManager.notify(NOTI_ID_POPUP, notification);
    }

    @SuppressLint("MissingPermission")
    private void startSubscription() {
        subscription = reactiveBeacons.observe()
                .subscribeOn(Schedulers.computation())
                .switchMap(beacon -> Observable.just(new BluetoothLeDevice(beacon.device, beacon.rssi, beacon.scanRecord, System.currentTimeMillis())), 10)
                .filter(bluetoothLeDevice -> BeaconUtils.getBeaconType(bluetoothLeDevice) == BeaconType.IBEACON)
                .switchMap(bluetoothLeDevice -> Observable.just(new IBeaconDevice(bluetoothLeDevice)))
                .filter(iBeaconDevice -> Constant.UUID.equals(iBeaconDevice.getUUID()) && Constant.MAJOR == iBeaconDevice.getMajor() &&
                        Constant.MINOR == iBeaconDevice.getMinor())
                .doOnNext(iBeaconDevice -> handleAccuracy(iBeaconDevice))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(iBeaconDevice -> {
                    final int minor = iBeaconDevice.getMinor();
//                    if (beaconDatas.containsKey(minor) && iBeaconDevice.getAccuracy() > beaconDatas.get(minor).getLimit()) {
//                        if (isScreenOn() == false)
//                            showMain(isOut, iBeaconDevice.getMinor());
//                        else
//                            showMainNotification(isOut, iBeaconDevice.getMinor());
//                    }
                    MutableLiveData<Double> liveData = mutableLiveDataMap.get(minor);
                    if (liveData != null) {
                        liveData.setValue(iBeaconDevice.getAccuracy());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTI_ID_STOP);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTI_ID_MAIN);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTI_ID_POPUP);
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    private void showMain(boolean isOut, int minor) {
        Intent intent = new Intent(BeaconService.this, MainActivity.class);
        intent.putExtra("minor", minor);
        intent.putExtra("sound", true);
        intent.putExtra("isOut", isOut);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showPopup(int minor) {
        Intent intent = new Intent(BeaconService.this, PopupActivity.class);
        intent.putExtra("minor", minor);
        intent.putExtra("sound", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void addLiveData(BeaconData beaconData, MutableLiveData<Double> mutableLiveData) {
        beaconDatas.put(beaconData.getMinor(), beaconData);
        mutableLiveDataMap.put(beaconData.getMinor(), mutableLiveData);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner, int minor) {
        mutableLiveDataMap.get(minor).removeObservers(lifecycleOwner);
        mutableLiveDataMap.remove(minor);
    }

    public boolean isLiveDataExist(int minor) {
        return mutableLiveDataMap.containsKey(minor);
    }

    public void changeBeaconData(BeaconData beaconData) {
        beaconDatas.put(beaconData.getMinor(), beaconData);
    }

    private void handleAccuracy(IBeaconDevice iBeaconDevice) {
        final int minor = iBeaconDevice.getMinor();
        if (beaconDatas.containsKey(minor) == false)
            return;
        if (isReceiveMode) {
            if (isScreenOn() == false)
                showPopup(iBeaconDevice.getMinor());
            else
                showPopupNotification(iBeaconDevice.getMinor());
        } else {
            if (iBeaconDevice.getAccuracy() < beaconDatas.get(minor).getLimit()) {
                if (isOut == true) {
                    isOut = false;
                    if (isScreenOn() == false)
                        showMain(isOut, iBeaconDevice.getMinor());
                    else
                        showMainNotification(isOut, iBeaconDevice.getMinor());
                }
            } else {
                if (isOut == false) {
                    isOut = true;
                    if (isScreenOn() == false)
                        showMain(isOut, iBeaconDevice.getMinor());
                    else
                        showMainNotification(isOut, iBeaconDevice.getMinor());
                }
            }
        }

    }

    public void setReceiveMode(boolean receiveMode) {
        isReceiveMode = receiveMode;
    }
}
