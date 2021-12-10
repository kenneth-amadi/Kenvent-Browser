package com.kixfobby.project.kenventbrowser.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var preview: ByteArray
    private lateinit var favicon: ByteArray
    private var url: String? = null
    private var position: Int? = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.web_fragment, container, false)
        onTabClick = activity as OnTabClick

        url = arguments?.getString("url").toString()
        position = arguments?.getInt("position")!!

        viewModel = ViewModelProvider(this)[MainViewModel::class.java] // TODO: Use the ViewModel
        binding = WebFragmentBinding.bind(root)
        binding.presenter = viewModel
        this.lifecycle.addObserver(viewModel)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionBar: ActionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayShowCustomEnabled(true)

        val inflator =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = inflator.inflate(R.layout.search, null)
        actionBar.customView = v

        searchEditBar = v.findViewById(R.id.search_edit_bar)
        searchEditBar.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchEditBar.clearFocus();
                var inp: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inp.hideSoftInputFromWindow(searchEditBar.windowToken, 0)
                val lastPosition = TabFragment().retrieveTabs(requireContext()).lastIndex
                val url = searchEditBar.text.toString()
                onTabClick.loadTab(url, Pref(requireContext()).get("position", position!!))
                return@OnEditorActionListener true
            }
            false
        })

        renderPrivacyWebPage(url)
        // Makes Progress bar Visible
        /*requireActivity().window.setFeatureInt(
            Window.FEATURE_PROGRESS,
            Window.PROGRESS_VISIBILITY_ON
        );*/
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected fun renderPrivacyWebPage(url: String?) {
        val myWebView = binding.webView
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Toast.makeText(requireContext(), "Started!", Toast.LENGTH_SHORT).show()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //this@WebFragment.preview = getPreviewWebview(view!!)
                Toast.makeText(requireContext(), "Finished!", Toast.LENGTH_SHORT).show()
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
                Pref(requireContext()).put("position", position!!)
                //Pref(requireContext()).put("title", title)

                val title = Pref(requireContext()).get("title", url)

                this@WebFragment.preview = getPreview(view!!)
                this@WebFragment.favicon = getFavicon(icon)
                val tabs = TabFragment().retrieveTabs(requireContext())
                tabs.set(position!!, Tabs(title, view.url, preview, favicon, position))
                TabFragment().setSavedTabs(requireContext(), tabs)

            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {}
        }
        val webSetting: WebSettings = myWebView.settings
        webSetting.javaScriptEnabled = true
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

        myWebView.loadUrl(url!!)
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.allowContentAccess = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.useWideViewPort = true

    }

    private fun getPreview(view: WebView): ByteArray {
        val bitmap = Bitmap.createBitmap(view.width, 1500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteStream)
        return byteStream.toByteArray()
    }

    fun getFavicon(icon: Bitmap?): ByteArray {
        val byteStream = ByteArrayOutputStream()
        icon?.compress(Bitmap.CompressFormat.PNG, 0, byteStream)
        return byteStream.toByteArray()
    }

    override fun onBackPressed(): Boolean {
        return run {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.container, MainFragment())
            transaction?.disallowAddToBackStack()
            transaction?.commit()
            true
        }
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
