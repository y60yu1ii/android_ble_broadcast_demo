package de.fishare.bioapp

import android.os.Handler
import de.fishare.lumosble.*

class DemoPeri (mac: String): PeriObj(mac){
    var lumenData = 0
    var tx = "2a05"
    var rx = "2a04"
    override var TAG = "DemoPEri"
    private var handler = Handler()

    override fun authAndSubscribe(){
        super.authAndSubscribe()
        print(TAG, "auth and start")
    }

    fun write(cmd:String){
       controller?.writeTo(rx, cmd.toByteArray(), true)
    }

    override fun getUpdated(uuidStr: String, value: ByteArray, kind: GattController.UpdateKind) {
        if(kind == GattController.UpdateKind.Notify){
            print(TAG, "[Notify] is $uuidStr has ${value.hex4Human()} int is ${value.to2Int()}")
//            listener?.onUpdated(uuidStr, value.to2Int(), this@DemoPeri)
//        val dataMap = parseScanRecord(rawData)
//        val rData:ByteArray? = dataMap[-1]?.slice(2..3)?.toByteArray()
////        print(TAG, "rData is ${rData?.to2unsignedInt()}")
//        lumenData = rData?.to2unsignedInt() ?: lumenData
//        listener?.onUpdated("lumenData", lumenData, this)
        }
    }
}