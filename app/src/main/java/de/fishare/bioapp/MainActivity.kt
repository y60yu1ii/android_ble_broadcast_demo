package de.fishare.bioapp

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.fishare.lumosble.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AvailObj.Listener {
    val TAG = "MainActivity"
    lateinit var recyclerView : RecyclerView
    val centralMgr by lazy { CentralManagerBuilder(listOf()).build(this)}
    val notificationMgr by lazy { NotificationManager.getInstance(this) }
    var avails = listOf<DemoAvail>()
    private lateinit var adapter: EasyListAdapter
    private var viewModel = ViewModel()

    private var isRegistered = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpBLECentralManager()
        addBroadcastReceiver()
        initViews()

        btnScan.tag = true
        btnScan.setOnClickListener {
            it.tag = (it.tag == true).not()
            if(it.tag == true){
                btnScan.post { btnScan.text = "stop" }
                centralMgr.scan()
            }else{
                btnScan.post { btnScan.text = "scan" }
                centralMgr.stopScan()
            }
        }
    }

    private fun setUpBLECentralManager(){
        centralMgr.event = object : CentralManager.EventListener {
            override fun didDiscover(availObj: AvailObj) {
//                print(TAG, "list size ${avails.count()} Found ${availObj.mac} data is ${availObj.rawData.hex4Human()}")
                refreshAvail()
                runOnUiThread { adapter.reload() }
            }
        }
        centralMgr.setting = object :CentralManager.Setting{
            override fun getNameRule(): String {
                return "(LBLE)-[a-zA-Z0-9]{0,5}"
            }

            override fun getCustomAvl(device: BluetoothDevice): AvailObj {
                return DemoAvail(device)
            }

        }
        centralMgr.loadHistory()
        centralMgr.checkPermit(this)
    }

    private fun refreshAvail(){
        avails = centralMgr.avails.map { it as DemoAvail }
        avails.forEach { it.listener = this@MainActivity }
    }

/**
 * Broadcast handler
 **/
    //on RSSI changed of avail
    override fun onRSSIChanged(rssi: Int, availObj: AvailObj) {
        val vh = getCustomItemOfAvail(availObj.mac)
        if(vh != null){ viewModel.update(vh, availObj as DemoAvail) }
    }

    //on Data Update changed of avail
    override fun onUpdated(label: String, value: Any, availObj: AvailObj) {
//        print(TAG, "${availObj.name} update $label with ${value}")
        val vh = getCustomItemOfAvail(availObj.mac)
        if(vh != null){ viewModel.update(vh, availObj as DemoAvail) }
        if(label == "lumenData" && (value as Int) < 50 ){
            val payload = mapOf(
               "title" to "Alert",
               "body"  to "Light is dimmed!"
            )
            notificationMgr.send(payload)
//            notificationMgr.beep()
//            notificationMgr.sendMail()
        }
    }


    /**
     *  Broadcast relative
     *
     **/
    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                CONNECTION_EVENT->{
                    val mac = intent.getStringExtra("mac") ?: ""
                    val isConnected = intent.getBooleanExtra("connected", false)
                    print(TAG, "$mac is ${if(isConnected) "CONNECT" else "DISCONNECT" }")
                    refreshAvail()
                }
                REFRESH_EVENT->{
                    refreshAvail()
                    centralMgr.clearOutdateAvl()
                }
            }
        }
    }

    private fun addBroadcastReceiver(){
        val filter = IntentFilter().apply {
            addAction(CONNECTION_EVENT)
            addAction(REFRESH_EVENT)
        }
        registerReceiver(receiver, filter)
        isRegistered = true
    }

    override fun onDestroy() {
        super.onDestroy()
        print(TAG, "unregistered")
        if(isRegistered){unregisterReceiver(receiver)}
    }

    /**
     *  View relative
     **/
    private fun initViews(){
        adapter = EasyListAdapter()
        adapter.dataSource = object : EasyListAdapter.AdapterDataSource {
            override fun numberOfRowIn(section: Int): Int {
                return when(section){
                    0 -> avails.count()
                    else -> 0
                }
            }

            override fun numberOfSection(): Int {
                return 1
            }

            override fun onBindOfRow(vh: RecyclerView.ViewHolder, indexPath: EasyListAdapter.IndexPath) {
                if(indexPath.section == 0 && indexPath.row < avails.size){
                    val avl = avails[indexPath.row]
                    runOnUiThread {
                        viewModel.setUpView(vh as CustomItem, avl)
                    }
                }
            }

            override fun cellForRow(parent: ViewGroup, section: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.cell_device, parent, false)
                return CustomItem(view)
            }
        }//data source

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }//init views

    private fun getCustomItemOfAvail(mac: String):CustomItem?{
        val idx = getAvailIdx(mac)
        return if(idx != null) getCustomItem(EasyListAdapter.IndexPath(0, idx)) else null
    }

    private fun getAvailIdx(mac:String):Int?{
        val idx = avails.indexOfFirst { it.mac == mac }
        return if(idx < avails.size && idx >= 0) idx else null
    }

    private fun getCustomItem(indexPath: EasyListAdapter.IndexPath):CustomItem?{
        val position = adapter.getPositionOf(indexPath)
        val view = recyclerView.findViewHolderForLayoutPosition(position)?.itemView
        return if(view != null){ CustomItem(view) } else null
    }
}
