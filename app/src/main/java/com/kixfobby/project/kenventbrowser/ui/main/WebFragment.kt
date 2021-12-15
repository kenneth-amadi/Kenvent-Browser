package com.kixfobby.project.kenventbrowser.ui.main

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.webkit.WebView.HitTestResult
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kixfobby.project.kenventbrowser.OnBackPressed
import com.kixfobby.project.kenventbrowser.Pref
import com.kixfobby.project.kenventbrowser.R
import com.kixfobby.project.kenventbrowser.databinding.WebFragmentBinding
import java.io.ByteArrayOutputStream


open class WebFragment : Fragment(), OnBackPressed {
    private lateinit var searchEditBar: EditText
    private lateinit var onTabClick: OnTabClick
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: WebFragmentBinding
    private lateinit var myWebView: WebView
    private lateinit var webSetting: WebSettings
    private lateinit var mySwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var preview: ByteArray
    private lateinit var favicon: ByteArray
    private var doubleBackToExitPressedOnce = false
    private lateinit var url: String
    private var position: Int = -999
    private var progress: Progress? = null
    private var isLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        if (isOnline()/* && !isLoaded*/) renderWebPage(url)
        else showToast("No internet")
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.web_fragment, container, false).let {
            onTabClick = activity as OnTabClick

            url = requireArguments().getString("url").toString()
            position = requireArguments().getInt("position")

            viewModel =
                ViewModelProvider(this)[MainViewModel::class.java] // TODO: Use the ViewModel
            binding = WebFragmentBinding.bind(it)
            binding.presenter = viewModel
            this.lifecycle.addObserver(viewModel)

            return it
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        myWebView = binding.webView
        webSetting = myWebView.settings
        mySwipeRefreshLayout = binding.swipeRefresh

        getString(R.string.app_name).let(::println)

        with(requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) {
            inflate(R.layout.search, null)?.let {
                ((activity as AppCompatActivity?)?.supportActionBar)?.apply {
                    setDisplayShowCustomEnabled(true)
                    customView = it
                }
                searchEditBar = it.findViewById(R.id.search_edit_bar)
                with(searchEditBar) {
                    setText(url)
                    setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            clearFocus()
                            val inp: InputMethodManager =
                                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inp.hideSoftInputFromWindow(windowToken, 0)

                            val url = text.toString()
                            onTabClick.loadTab(
                                url,
                                Pref(requireContext()).get("position", position)
                            )
                            return@OnEditorActionListener true
                        }
                        false
                    })
                }

            }
        }

        renderWebPage(url)

        mySwipeRefreshLayout.setOnRefreshListener {
            Log.i("on refresh", "onRefresh called from SwipeRefreshLayout")
            myWebView.reload()
            webSetting.cacheMode = WebSettings.LOAD_DEFAULT
            setProgressDialogVisibility(false)
        }

        if (!isOnline()) {
            showToast("No internet")
            //infoTV.text = getString(R.string.no_internet)
            //showNoNetSnackBar()
            return
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected fun renderWebPage(url: String?) {
        //infoTV.text = ""
        registerForContextMenu(myWebView)

        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                setProgressDialogVisibility(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoaded = true
                setProgressDialogVisibility(false)
                mySwipeRefreshLayout.isRefreshing = false
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                isLoaded = false
                //showToast(error.description as String)
                setProgressDialogVisibility(false)

            }
        }

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!TextUtils.isEmpty(title)) {
                    Pref(requireContext()).put("title", title)
                    searchEditBar.setText(view!!.url)
                }
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                position.let {
                    Pref(requireContext()).put("position", it)
                    //Pref(requireContext()).put("title", title)
                    val title = Pref(requireContext()).get("title", url)
                    try {
                        this@WebFragment.preview = getPreview(view!!)
                        this@WebFragment.favicon = getFavicon(icon)
                        val tabs = TabFragment().retrieveTabs(requireContext())
                        tabs.set(it, Tabs(title, view.url, preview, favicon, it))
                        TabFragment().setSavedTabs(requireContext(), tabs)
                    } catch (e: Exception) {
                        val tabs = TabFragment().retrieveTabs(requireContext())
                        tabs.set(it, Tabs(title, view?.url, null, null, it))
                        TabFragment().setSavedTabs(requireContext(), tabs)
                    }
                }
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {}
        }
        myWebView.canGoBack()
        myWebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP
                && myWebView.canGoBack()
            ) {
                myWebView.goBack()
                return@OnKeyListener true
            } else {
                showToastToExit()
            }
            false
        })
        myWebView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            DownloadManager.Request(Uri.parse(url)).run {
                CookieManager.getInstance().getCookie(url).let { addRequestHeader("cookie", it) }
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading file...")
                setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                allowScanningByMediaScanner()
                setMimeType(mimeType)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )
                val dm = requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
                dm!!.enqueue(this)
                showToast("Downloading File")
            }
        }
        with(webSetting) {
            javaScriptEnabled = true
            allowContentAccess = true
            domStorageEnabled = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }
        myWebView.loadUrl(url!!)
    }

    private fun getPreview(view: WebView): ByteArray {
        Bitmap.createBitmap(view.width, 1500, Bitmap.Config.ARGB_8888).let {
            val canvas = Canvas(it)
            view.draw(canvas)

            ByteArrayOutputStream().run {
                it.compress(Bitmap.CompressFormat.PNG, 0, this)
                return toByteArray()
            }
        }
    }

    fun getFavicon(icon: Bitmap?): ByteArray {
        ByteArrayOutputStream().run {
            icon?.compress(Bitmap.CompressFormat.PNG, 0, this)
            return this.toByteArray()
        }
    }

    override fun onBackPressed(): Boolean {
        return run {
            requireActivity().supportFragmentManager.beginTransaction().let {
                it.replace(R.id.container, MainFragment())
                it.disallowAddToBackStack()
                it.commit()
                true
            }
        }
    }

    private fun showToastToExit() {
        when {
            doubleBackToExitPressedOnce -> {
                onBackPressed()
            }
            else -> {
                doubleBackToExitPressedOnce = true
                showToast("Press back again to exit")
                Handler(Looper.myLooper()!!).postDelayed(
                    { doubleBackToExitPressedOnce = false },
                    2000
                )
            }
        }
    }

    private fun setProgressDialogVisibility(visible: Boolean) {
        if (visible) progress = Progress(requireContext(), "Please wait", cancelable = true)
        progress?.apply { if (visible) show() else dismiss() }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /*private fun showNoNetSnackBar() {
        val snack = Snackbar.make(requireView(), "No internet", Snackbar.LENGTH_INDEFINITE)
        snack.setAction("Settings") {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        snack.show()
    }*/


    override fun onCreateContextMenu(
        contextMenu: ContextMenu,
        view: View,
        contextMenuInfo: ContextMenuInfo?
    ) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo)
        val webViewHitTestResult: HitTestResult = myWebView.hitTestResult
        if (webViewHitTestResult.type == HitTestResult.IMAGE_TYPE ||
            webViewHitTestResult.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            contextMenu.setHeaderTitle("Download Image")
            contextMenu.add(0, 1, 0, "Save Image")

        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        val webViewHitTestResult: HitTestResult = myWebView.hitTestResult
        item.setOnMenuItemClickListener {
            when (item.itemId) {
                1 -> {
                    val downloadImageURL = webViewHitTestResult.extra
                    if (URLUtil.isValidUrl(downloadImageURL)) {
                        val request = DownloadManager.Request(Uri.parse(downloadImageURL))
                        request.allowScanningByMediaScanner()
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, null, null)
                        )
                        request.setAllowedOverMetered(true)
                        val downloadManager =
                            requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
                        downloadManager!!.enqueue(request)
                        showToast("Image Downloading...")
                    } else {
                        showToast("Sorry.. Something Went Wrong.")
                    }
                    false
                }
                else -> {
                    false
                }
            }
        }
        return false
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflater.inflate(R.menu.menu_web, menu);
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "TAB").setIcon(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_baseline_tab_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 1, Menu.NONE, "Settings").setIcon(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_baseline_home_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 2, Menu.NONE, "Settings").setIcon(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_baseline_help_outline_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.replace(R.id.container, TabFragment())
                //transaction?.disallowAddToBackStack()
                transaction?.commit()
                return true
            }
            1 -> {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.replace(R.id.container, MainFragment())
                //transaction?.disallowAddToBackStack()
                transaction?.commit()
                return true
            }
            2 -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
