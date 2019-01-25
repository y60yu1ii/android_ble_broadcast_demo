package de.fishare.bioapp

import android.support.v7.widget.RecyclerView
import android.view.View
import de.fishare.lumosble.hex4Human
import kotlinx.android.synthetic.main.cell_device.view.*

class ViewModel {
   fun setUpView(v:CustomItem, avl: DemoAvail){
      v.lblName.text = avl.name
      v.lblMac.text  = avl.mac
   }

   fun update(v:CustomItem, avl:DemoAvail) {
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

}

