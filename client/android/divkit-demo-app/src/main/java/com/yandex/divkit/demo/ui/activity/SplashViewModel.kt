package com.yandex.divkit.demo.ui.activity

import androidx.lifecycle.*
import com.yandex.divkit.demo.data.repository.PhPlusRepository

//import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: PhPlusRepository
) : ViewModel() {
}