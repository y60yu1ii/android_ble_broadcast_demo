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
import android.view.View
import android.view.ViewGroup
import de.fishare.lumosble.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AvailObj.Listener, PeriObj.Listener {
    val TAG = "MainActivity"
    lateinit var recyclerView : RecyclerView
    val centralMgr by lazy { CentralManagerBuilder(listOf()).build(this)}
    val notificationMgr by lazy { NotificationManager.getInstance(this) }
    var avails = listOf<DemoAvail>()
    var peris = listOf<DemoPeri>()
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
                refreshAll()
            }
        }
        centralMgr.setting = object :CentralManager.Setting{

            override fun getCustomAvl(device: BluetoothDevice): AvailObj {
                return DemoAvail(device)
            }

            override fun getCustomObj(mac: String, name: String): PeriObj {
                return DemoPeri(mac)
            }

        }
        centralMgr.loadHistory()
        centralMgr.checkPermit(this)
    }

    private fun refreshAll(){
        avails = centralMgr.avails.map { it as DemoAvail }
        avails.forEach { it.listener = this@MainActivity }

        peris = centralMgr.peris.map { it as DemoPeri }
        peris.forEach { it.listener = this@MainActivity }

        runOnUiThread { adapter.reload() }
    }

/**
 * Broadcast handler
 **/
    //on RSSI changed of avail
    override fun onRSSIChanged(rssi: Int, availObj: AvailObj) {
        val vh = getCustomItemOfAvail(availObj.mac)
        if(vh != null){ viewModel.update(vh, availObj as DemoAvail, adapter) }
    }

    //on Data Update changed of avail
    override fun onUpdated(label: String, value: Any, availObj: AvailObj) {
//        print(TAG, "${availObj.name} update $label with ${value}")
        val vh = getCustomItemOfAvail(availObj.mac)
        if(vh != null){ viewModel.update(vh, availObj as DemoAvail, adapter) }
        if(label == "lumenData" && (value as Int) < 50 ){
            val payload = mapOf(
               "title" to "Alert",
               "body"  to "Light is dimmed!"
            )
//            notificationMgr.send(payload)
//            notificationMgr.beep()
//            notificationMgr.sendMail()
        }
    }

    /**
     * notify handler
     **/
    override fun onRSSIChanged(rssi: Int, periObj: PeriObj) {
        val vh = getCustomItemOfPeri(periObj.mac)
        if(vh != null){ viewModel.update(vh, periObj as DemoPeri, adapter) }
    }

    //on Data Update changed of avail
    override fun onUpdated(label: String, value: Any, periObj: PeriObj) {
//        print(TAG, "${availObj.name} update $label with ${value}")
        val vh = getCustomItemOfPeri(periObj.mac)
        if(vh != null){ viewModel.update(vh, periObj as DemoPeri, adapter) }
        if(label == "lumenData" && (value as Int) < 50 ){
//            val payload = mapOf(
//                "title" to "Alert",
//                "body"  to "Light is dimmed!"
//            )
//            notificationMgr.send(payload)
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
                    refreshAll()

                    if(isConnected.not()){
                        val payload = mapOf(
                            "title" to "Disconnect",
                            "body"  to "$mac is disconnected."
                        )
                        notificationMgr.send(payload)
                    }
                }
                REFRESH_EVENT->{
                    refreshAll()
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
                    1 -> peris.count()
                    else -> 0
                }
            }

            override fun numberOfSection(): Int {
                return 2
            }

            override fun onBindOfRow(vh: RecyclerView.ViewHolder, indexPath: EasyListAdapter.IndexPath) {
                if(indexPath.section == 0 && indexPath.row < avails.size){
                    runOnUiThread {
                        viewModel.setUpView(vh as CustomItem, avails[indexPath.row], adapter, indexPath)
                    }
                }else if(indexPath.section == 1 && indexPath.row < peris.size){
                    runOnUiThread {
                        viewModel.setUpView(vh as CustomItem, peris[indexPath.row], adapter, indexPath)
                    }
                }
            }

            override fun cellForRow(parent: ViewGroup, section: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.cell_device, parent, false)
                return CustomItem(view)
            }
        }//data source

        adapter.listener = object : EasyListAdapter.ItemEvent{
            override fun onItemClick(view: View, indexPath: EasyListAdapter.IndexPath) {
                when(view.id){
                    R.id.btnAction -> {
                        print(TAG, "[EVENT] action button is click ")
                        when(indexPath.section){
                            1 -> {
                                if(indexPath.row < peris.size){
                                    val peri = peris[indexPath.row]
                                    peri?.write("%1")
                                }
                            }
                        }

                    }
                    R.id.btnConnect -> {
                        print(TAG, "[EVENT] connect button is click ")
                        val vh = getCustomItem(indexPath)
                        when(indexPath.section){
                            0 ->{
                                if(indexPath.row < avails.size){
                                    vh?.btnConnect?.post { vh.btnConnect.text = "connecting" }
                                    centralMgr.connect(avails[indexPath.row].mac)
                                }
                            }

                            1 ->{
                                if(indexPath.row < peris.size){
                                    vh?.btnConnect?.post { vh.btnConnect.text = "removing" }
                                    centralMgr.remove(peris[indexPath.row].mac)
                                }
                            }
                        }
                    }
                }
            }
        }

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

    private fun getCustomItemOfPeri(mac: String):CustomItem?{
        val idx = getPeriIdx(mac)
        return if(idx != null) getCustomItem(EasyListAdapter.IndexPath(0, idx)) else null
    }

    private fun getPeriIdx(mac:String):Int?{
        val idx = peris.indexOfFirst { it.mac == mac }
        return if(idx < peris.size && idx >= 0) idx else null
    }

    private fun getCustomItem(indexPath: EasyListAdapter.IndexPath):CustomItem?{
        val position = adapter.getPositionOf(indexPath)
        val view = recyclerView.findViewHolderForLayoutPosition(position)?.itemView
        return if(view != null){ CustomItem(view) } else null
    }
}
