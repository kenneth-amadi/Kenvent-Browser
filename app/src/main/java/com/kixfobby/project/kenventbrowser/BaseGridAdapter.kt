package com.kixfobby.project.kenventbrowser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
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
        if (holder is OriginalViewHolder) {

            val p = items[position]
            holder.tabTitle.text = p.title
            holder.tabUrl.setImageBitmap(byteToBitmap(p.preview!!))
            holder.tabIcon.setImageBitmap(byteToBitmap(p.favicon!!))

            //holder.tabUrl.renderWebPage(p.url)
            //TabFragment().replaceTab(ctx, p)

            holder.tab.setOnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, items[position], position)
                }
                //listener!!.loadTab(title, url, position)
            }
            holder.tabUrl.setOnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, items[position], position)
                }
            }
            holder.tabRemove.setOnClickListener { view ->
                TabFragment().removeTab(ctx, p)
                notifyItemChanged(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Tabs, position: Int)
    }

    fun byteToBitmap(bitmapBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var tabTitle: TextView = v.findViewById(R.id.title)
        var tabUrl: ImageView = v.findViewById(R.id.wv)
        var tabRemove: ImageView = v.findViewById(R.id.close)
        var tabIcon: ImageView = v.findViewById(R.id.icon)
        var tab: CardView = v.findViewById(R.id.tab)
    }

/*    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    fun WebView.renderWebPage(url: String?) {
        val myWebView: WebView = findViewById(R.id.wv)
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                getPreviewWebview(view!!)
            }
        }
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!TextUtils.isEmpty(title)) {
                    //requireActivity().title = title
                }
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {}
        }
        val webSetting: WebSettings = myWebView.settings
        webSetting.javaScriptEnabled = true
        myWebView.isLongClickable = false
        myWebView.isHapticFeedbackEnabled = false
        myWebView.isScrollContainer = false
        myWebView.webViewClient = WebViewClient()
        myWebView.canGoBack()
        myWebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP
                && myWebView.canGoBack()
            ) {
                myWebView.goBack()
                return@OnKeyListener true
            }
            false
        })

        myWebView.setOnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE }
        myWebView.loadUrl(url!!)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.allowContentAccess = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.useWideViewPort = true

    }*/

    private fun getPreviewWebview(view: WebView): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, 1500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }
}