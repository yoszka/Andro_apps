package com.example.btsmartserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final int REQUEST_ENABLE_BT = 1;
    private boolean mIsBound;
    
    private EditText mValueToSendEditText;

    private BluetoothAdapter mBluetoothAdapter;
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case AdvertiserService.MESSAGE_SET_INT_VALUE:
                break;
            case AdvertiserService.MSGESSAGE_SET_STRING_VALUE:
                String str1 = msg.getData().getString("str1");
                break;
            default:
                super.handleMessage(msg);
            }
        };
    });

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, AdvertiserService.MESSAGE_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mValueToSendEditText = (EditText) findViewById(R.id.valueToSendEditText);
        
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (mBluetoothAdapter.isEnabled()) {
            doBindService();
        } else {
            Toast.makeText(this, "BT not enabled", Toast.LENGTH_LONG).show();
            promptForEnableBT();
        }
    }

    public void onClickSend(View v) {
        String text = mValueToSendEditText.getText().toString();
        Integer intValue = Integer.valueOf(text);
        sendIntegerMessageToService(intValue);
        Toast.makeText(this, "Sent " + intValue, Toast.LENGTH_LONG).show();
    }


    private void doBindService() {
        bindService(getServiceIntent(), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, AdvertiserService.MESSAGE_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void sendIntegerMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, AdvertiserService.MESSAGE_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
    
    private void promptForEnableBT() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Intent getServiceIntent() {
        return new Intent(this, AdvertiserService.class);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            doBindService();
        }
    }
}
