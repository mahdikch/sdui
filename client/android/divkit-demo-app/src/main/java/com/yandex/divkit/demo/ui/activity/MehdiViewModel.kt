package com.yandex.divkit.demo.ui.activity

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.entities.res.ResponsePhPlus
import com.yandex.divkit.demo.data.remote.Resource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
class MehdiViewModel @Inject constructor(
    private val repository: PhPlusRepository
) : ViewModel() {
    private val _phPlusRequest = MutableLiveData<MutableMap<String, String>>()

    fun setphPlusRequest(request: MutableMap<String, String>) {
        _phPlusRequest.postValue(request)
    }

    private val _phPlus = _phPlusRequest.switchMap { request ->
        repository.phPlus(request)
    }

    val phPlus: LiveData<Resource<MutableMap<String, String>>> = _phPlus


//    fun insertListToDb(phPlusDBs: List<PhPlusDB>) = viewModelScope.launch(Dispatchers.IO) {
//        repository.insertList(phPlusDBs)
//    }

//    fun getPage(key: String) = viewModelScope.launch(Dispatchers.IO) {
//        repository.getByKey(key)
//    }
    fun getPage(key: String) = repository.getByKey(key).asLiveData()
    fun getJson(key: String) = repository.getByKey(key).asLiveData()
    fun getVariable(key: String) = repository.getByKey(key).asLiveData()
    fun getRequestParam(key: String) = repository.getByKey(key).asLiveData()
    fun getValueByKey(key: String) = repository.getValueByKey(key)
    fun getpatch(key: String) = repository.getByKey(key).asLiveData()
    fun getall() = repository.getall().asLiveData()


    fun insertItemToDb(phPlusDB: PhPlusDB) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertItem(phPlusDB)
    }

    suspend fun insertBasicInfo(basicInfoList: List<PhPlusDB>) {
        this.repository.insertList(basicInfoList)
    }
}