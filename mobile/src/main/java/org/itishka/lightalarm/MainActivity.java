package org.itishka.lightalarm;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.button_send)
    Button mButtonSend;

    RxBluetooth mRxBluetooth;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRxBluetooth != null) {
            mRxBluetooth.cancelDiscovery();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
                    BtIntentService.startSend(this);
                });
    }
}
