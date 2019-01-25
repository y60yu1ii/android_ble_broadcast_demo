package de.fishare.bioapp

import android.bluetooth.BluetoothDevice
import de.fishare.lumosble.*

class DemoAvail (device: BluetoothDevice):AvailObj(device){
    var lumenData = 0
    override fun onRawUpdate(data: ByteArray) {
        super.onRawUpdate(data)
        val dataMap = parseScanRecord(rawData)
        val rData:ByteArray? = dataMap[-1]?.slice(2..3)?.toByteArray()
//        print(TAG, "rData is ${rData?.to2unsignedInt()}")
        lumenData = rData?.to2unsignedInt() ?: lumenData
        listener?.onUpdated("lumenData", lumenData, this)
    }
}