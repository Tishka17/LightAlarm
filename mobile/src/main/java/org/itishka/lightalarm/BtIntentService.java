package org.itishka.lightalarm;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.io.IOException;
import java.util.UUID;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class BtIntentService extends IntentService {
    private static final String DEVICE = "";
    private static final String SERVICE = "";
    private static final String TAG = "MainActivity";
    private static final String DATA = "";

    private static final String ACTION_SEND = "org.itishka.lightalarm.action.SEND";

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private Subscription mBluetoothSubscription;
    private Subscription mSocketSubscription;
    private RxBluetooth mRxBluetooth;

    public BtIntentService() {
        super("BtIntentService");
    }

    public static void startSend(Context context) {
        Intent intent = new Intent(context, BtIntentService.class);
        intent.setAction(ACTION_SEND);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND.equals(action)) {
                handleSend();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothSubscription != null) {
            mBluetoothSubscription.unsubscribe();
        }
        if (mSocketSubscription != null) {
            mSocketSubscription.unsubscribe();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRxBluetooth = new RxBluetooth(this);
    }

    private void handleSend() {
        mBluetoothSubscription = mRxBluetooth
                .observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bluetoothDevice -> {
                    if (bluetoothDevice.getAddress().equals(DEVICE)) {
                        Log.d(TAG, "device");
                        subscribeSocket(bluetoothDevice);
                    }
                });
    }


    void subscribeSocket(BluetoothDevice bluetoothDevice) {
        mSocketSubscription = mRxBluetooth
                .observeConnectDevice(bluetoothDevice, UUID.fromString(SERVICE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bluetoothSocket -> {
                    Log.d(TAG, "Connect");
                    try {
                        bluetoothSocket.getOutputStream().write(
                                DATA.getBytes()
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

}
