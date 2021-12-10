package com.kixfobby.project.kenventbrowser.ui.main

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(), LifecycleObserver {
    // TODO: Implement the ViewModel

    private val mAction: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun getVM(): LiveData<Boolean> {
        return mAction
    }

}