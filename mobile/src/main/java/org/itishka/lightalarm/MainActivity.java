package org.itishka.lightalarm;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String DEVICE = "";
    private static final String SERVICE = "";
    private static final String TAG = "MainActivity";
    private static final String DATA = "";

    @BindView(R.id.button_send)
    Button mButtonSend;

    RxBluetooth mRxBluetooth;
    private Subscription mBluetoothSubscription;
    private Subscription mSocketSubscription;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRxBluetooth != null) {
            mRxBluetooth.cancelDiscovery();
        }
        if (mBluetoothSubscription != null) {
            mBluetoothSubscription.unsubscribe();
        }
        if (mSocketSubscription != null) {
            mSocketSubscription.unsubscribe();
        }
    }

    void send() {
        if (!mRxBluetooth.isBluetoothEnabled()) {
            mRxBluetooth.enableBluetooth(this, REQUEST_ENABLE_BT);
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRxBluetooth = new RxBluetooth(this);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe(granted -> {
                    if (granted) {
                        //ok
                    } else {
                        finish();
                    }
                });
        RxView.clicks(mButtonSend)
                .subscribe(aVoid -> {
                    send();
                });
    }
}
