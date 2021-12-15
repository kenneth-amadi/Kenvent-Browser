package com.kixfobby.project.kenventbrowser.ui.main

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kixfobby.project.kenventbrowser.OnBackPressed
import com.kixfobby.project.kenventbrowser.R
import com.kixfobby.project.kenventbrowser.databinding.MainFragmentBinding


class MainFragment : Fragment(), OnBackPressed {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding
    private lateinit var onTabClick: OnTabClick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.main_fragment, container, false)
        onTabClick = activity as OnTabClick
        viewModel = ViewModelProvider(this)[MainViewModel::class.java] // TODO: Use the ViewModel
        binding = MainFragmentBinding.bind(root)
        binding.presenter = viewModel
        this.lifecycle.addObserver(viewModel)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val actionBar: ActionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayShowCustomEnabled(false)

        binding.searchGoBtn.setOnClickListener {
            val lastPosition = TabFragment().retrieveTabs(requireContext()).lastIndex
            val url = binding.searchEdit.text.toString()
            TabFragment().saveTab(requireContext(), Tabs(null, url, null, null, lastPosition + 1))
            onTabClick.loadTab(url, lastPosition + 1)
        }

        binding.searchEdit.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val lastPosition = TabFragment().retrieveTabs(requireContext()).lastIndex
                val url = binding.searchEdit.text.toString()
                TabFragment().saveTab(
                    requireContext(),
                    Tabs(null, url, null, null, lastPosition + 1)
                )
                onTabClick.loadTab(url, lastPosition + 1)
                return@OnEditorActionListener true
            }
            false
        })

        val onSearchClick = Observer { bool: Boolean? ->
            if (bool != null) {
                Toast.makeText(context, "Hello!", Toast.LENGTH_SHORT).show()

            }
        }

        viewModel.getVM().observe(viewLifecycleOwner, onSearchClick)
        binding.executePendingBindings()
    }

    override fun onBackPressed(): Boolean {
        return run {
            requireActivity().finish()
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
                requireContext(), R.drawable.ic_baseline_help_outline_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                return run {
                    requireActivity().supportFragmentManager.beginTransaction().let {
                        it.replace(R.id.container, TabFragment())
                        it.disallowAddToBackStack()
                        it.commit()
                        true
                    }
                }
            }
            1 -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}