package com.kixfobby.project.kenventbrowser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.kixfobby.project.kenventbrowser.ui.main.MainFragment
import com.kixfobby.project.kenventbrowser.ui.main.OnTabClick
import com.kixfobby.project.kenventbrowser.ui.main.WebFragment

class MainActivity : AppCompatActivity(), OnTabClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }

    }

    override fun loadTab(url: String, position: Int) {
        val bundle = Bundle()
        bundle.putString("url", url)
        bundle.putInt("position", position)
        val transaction = this.supportFragmentManager.beginTransaction()
        val webFragment = WebFragment()
        webFragment.arguments = bundle
        transaction.replace(R.id.container, webFragment)
        transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()

    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.container)
        (fragment as? OnBackPressed)?.onBackPressed()?.not()?.let {
            //super.onBackPressed()
        }
    }


}