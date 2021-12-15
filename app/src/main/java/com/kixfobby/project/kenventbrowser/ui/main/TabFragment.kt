package com.kixfobby.project.kenventbrowser.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.kixfobby.project.kenventbrowser.BaseGridAdapter
import com.kixfobby.project.kenventbrowser.OnBackPressed
import com.kixfobby.project.kenventbrowser.R
import com.kixfobby.project.kenventbrowser.databinding.TabFragmentBinding
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class TabFragment : Fragment(), OnBackPressed {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: TabFragmentBinding
    private lateinit var onTabClick: OnTabClick
    private lateinit var mAdapter: BaseGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.tab_fragment, container, false)
        onTabClick = activity as OnTabClick
        viewModel = ViewModelProvider(this)[MainViewModel::class.java] // TODO: Use the ViewModel
        binding = TabFragmentBinding.bind(root)
        binding.presenter = viewModel
        this.lifecycle.addObserver(viewModel)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionBar: ActionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayShowCustomEnabled(false)

        binding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(false)
            setItemViewCacheSize(50)
            isNestedScrollingEnabled = false

            mAdapter = BaseGridAdapter(requireContext(), retrieveTabs(requireContext()))
            adapter = mAdapter

            mAdapter.setOnItemClickListener(object : BaseGridAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, obj: Tabs, position: Int) {
                    onTabClick.loadTab(obj.url!!, position)
                }
            })
        }

        binding.executePendingBindings()
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

    fun saveTab(c: Context, p: Tabs) {
        val tabs = retrieveTabs(c)
        tabs.add(p)
        Log.i("TAB MANAGER", "Added: " + p.url)
        setSavedTabs(c, tabs)
    }

    fun replaceTab(c: Context, p: Tabs) {
        //val tabs = retrieveTabs(c)
        //tabs.set(p.position!!, Tabs(p.title, p.url, p.preview, p.favicon, p.position))
        //setSavedTabs(c, tabs)
        Toast.makeText(requireContext(), "Replace!", Toast.LENGTH_SHORT).show()
    }

    fun removeTab(c: Context, p: Tabs) {
        val tabs = retrieveTabs(c)
        for (i in tabs.indices) {
            if (i < tabs.lastIndex + 1) {
                if (tabs[i] == p) {
                    tabs.removeAt(i)
                    Log.i("TAB MANAGER", "Removed: " + p.title)
                    //Toast.makeText(c, "$i....${loadTab.size}.....$loadTab[i].....$p", Toast.LENGTH_LONG).show()
                }
            }
        }
        setSavedTabs(c, tabs)
    }

    fun setSavedTabs(c: Context, tabs: ArrayList<Tabs>) {
        try {
            val fos = c.openFileOutput("TABS", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(tabs)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun retrieveTabs(c: Context): ArrayList<Tabs> {
        var ret = ArrayList<Tabs>()
        try {
            val fis = c.openFileInput("TABS")
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<Tabs>
        } catch (e: FileNotFoundException) {
            setSavedTabs(c, ArrayList())
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        /*ret.sortWith { o2, o1 ->
            o2.url.compareTo(o1.url)
        }*/
        return ret
    }
}
