package com.kixfobby.project.kenventbrowser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.project.kenventbrowser.ui.main.OnTabClick
import com.kixfobby.project.kenventbrowser.ui.main.TabFragment
import com.kixfobby.project.kenventbrowser.ui.main.Tabs


open class BaseGridAdapter(private val ctx: Context, private val items: ArrayList<Tabs>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null
    private var listener: OnTabClick? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    fun setOnTabClick(onTabClick: OnTabClick?) {
        listener = onTabClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
        vh = OriginalViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder) {
            if (this is OriginalViewHolder) {
                val p = items[position]
                tabTitle.text = p.title
                tabUrl.setImageBitmap(p.preview?.let { byteToBitmap(it) })
                tabIcon.setImageBitmap(p.favicon?.let { byteToBitmap(it) })

                tab.setOnClickListener { view ->
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener!!.onItemClick(view, items[position], position)
                    }
                    //listener!!.loadTab(title, url, position)
                }
                tabUrl.setOnClickListener { view ->
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener!!.onItemClick(view, items[position], position)
                    }
                }
                tabRemove.setOnClickListener { view ->
                    TabFragment().removeTab(ctx, p)
                    items.removeAt(position)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Tabs, position: Int)
    }

    private fun byteToBitmap(bitmapBytes: ByteArray?): Bitmap? {
        return bitmapBytes?.size?.let { BitmapFactory.decodeByteArray(bitmapBytes, 0, it) }
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var tabTitle: TextView = v.findViewById(R.id.title)
        var tabUrl: ImageView = v.findViewById(R.id.wv)
        var tabRemove: ImageView = v.findViewById(R.id.close)
        var tabIcon: ImageView = v.findViewById(R.id.icon)
        var tab: CardView = v.findViewById(R.id.tab)
    }

}