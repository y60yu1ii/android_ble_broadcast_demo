package de.fishare.bioapp

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.cell_device.view.*

class ViewModel {
   fun setUpView(v:CustomItem, avl: DemoAvail, adapter:EasyListAdapter, indexPath:EasyListAdapter.IndexPath){
      v.lblName.text = avl.name
      v.lblMac.text  = avl.mac
      v.btnConnect.setOnClickListener { adapter.listener?.onItemClick(it, indexPath) }
      v.btnAction.setOnClickListener { adapter.listener?.onItemClick(it, indexPath) }
   }

   fun setUpView(v:CustomItem, peri: DemoPeri, adapter:EasyListAdapter, indexPath: EasyListAdapter.IndexPath){
      v.lblName.text = peri.name
      v.lblMac.text  = peri.mac
      v.btnConnect.setOnClickListener { adapter.listener?.onItemClick(it, indexPath) }
      v.btnAction.setOnClickListener { adapter.listener?.onItemClick(it, indexPath) }
   }

   fun update(v:CustomItem, peri:DemoPeri, adapter:EasyListAdapter) {
//      v.lblData.text  = avl.rawData.hex4Human()
      v.lblData.text  = peri.lumenData.toString()
      v.lblRSSI.text  = peri.rssi.toString()
   }

   fun update(v:CustomItem, avl:DemoAvail, adapter:EasyListAdapter) {
//      v.lblData.text  = avl.rawData.hex4Human()
      v.lblData.text  = avl.lumenData.toString()
      v.lblRSSI.text  = avl.rssi.toString()
   }

}

class CustomItem(itemView: View): RecyclerView.ViewHolder(itemView){
   val lblName = itemView.lblName
   val lblMac  = itemView.lblMac
   val lblData = itemView.lblData
   val lblRSSI = itemView.lblRSSI
   val btnConnect = itemView.btnConnect
   val btnAction = itemView.btnAction
}

