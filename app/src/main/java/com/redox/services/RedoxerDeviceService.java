package com.redox.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.redox.utils.GattAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedoxerDeviceService
{

    private BluetoothLeService mBluetoothLeService;
    public BluetoothGattCharacteristic mDeviceCommunicationCharacteristic , mDeviceREadCharacterstics;
    private BluetoothGattService mDeviceCommunicationService;
    private BluetoothGatt mBluetoothGatt;
    private final static String TAG = RedoxerDeviceService.class.getSimpleName();
    public List<BluetoothGattCharacteristic> chars = new ArrayList<>();

    public RedoxerDeviceService(BluetoothLeService bluetoothLeService, BluetoothGatt bluetoothGatt)
    {
        mBluetoothLeService= bluetoothLeService;
        mBluetoothGatt= bluetoothGatt;

//        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        String uuid = tManager.getDeviceId();

       mDeviceCommunicationService = mBluetoothGatt.getService(UUID.fromString(GattAttributes.SERVICE_MICROCHIP_ACCESS));


       //mDeviceCommunicationService = mBluetoothGatt.getService(UUID.fromString(GattAttributes.SERVICE_GENERIC_ACCESS));

        mBluetoothGatt.getServices();

        if (mDeviceCommunicationService != null)
        {
            mDeviceCommunicationCharacteristic = mDeviceCommunicationService.getCharacteristic(UUID.fromString(GattAttributes.CHARACTERISTIC_MICROCHIP_ACCESS_WRITE));
            //mDeviceCommunicationCharacteristic = mDeviceCommunicationService.getCharacteristic(UUID.fromString(GattAttributes.CHARACTERISTIC_GENERIC_ACCESS_WRITE));
           //FOR READING CHARACTERSTICS
            mDeviceREadCharacterstics = mDeviceCommunicationService.getCharacteristic(UUID.fromString(GattAttributes.CHARACTERISTIC_MICROCHIP_ACCESS_READ));

            if (mDeviceCommunicationCharacteristic == null || mDeviceREadCharacterstics == null)
            {
                Log.e(TAG, "Communication read and write characteristic not discovered in the device!");
            }
            else
             {
                 Log.e(TAG, "Communication read and write characteristic discovered in the device!");

             }
        }
        else
            {
            Log.e(TAG, "Communication service not discovered in the device!");
        }
    }

    /**
     * Start Device
     * @return If start command send successfully
     */
    public boolean startDevice() throws Exception
    {
        byte[] command= new byte[]{(byte)0xBE, (byte)0xB0, 0x01, (byte)0xc0, 0x36};

        String c = "start";
        mBluetoothLeService.setCharacteristicNotification(mDeviceCommunicationCharacteristic, true);
        return mBluetoothLeService.writeCharacteristic(mDeviceCommunicationCharacteristic, c);
    }
    public boolean readdevice() throws  Exception
    {

        BluetoothGattDescriptor descriptor = mDeviceREadCharacterstics.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            Log.i(TAG, descriptor.toString());
        mBluetoothLeService.setCharacteristicNotification(mDeviceREadCharacterstics,true);
        return mBluetoothLeService.readCharacteristic(mDeviceREadCharacterstics);
    }

    public void requestCharacteristics(BluetoothGatt gatt) {
        mBluetoothGatt.readCharacteristic(chars.get(chars.size() - 1));
    }

    //request characterstic to read
    public void readchar(BluetoothGatt mBluetoothGatt)
    {
        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        for(BluetoothGattService service:services)
        {
            chars.addAll(service.getCharacteristics());

        }
        requestCharacteristics(mBluetoothGatt);

    }
    //Convert the byte-String using this function

}
