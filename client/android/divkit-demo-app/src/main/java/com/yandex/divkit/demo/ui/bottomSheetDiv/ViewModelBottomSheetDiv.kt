package com.yandex.divkit.demo.ui.bottomSheetDiv

import androidx.lifecycle.ViewModel
import com.yandex.divkit.demo.data.repository.PhPlusRepository

import javax.inject.Inject

class ViewModelBottomSheetDiv @Inject constructor(
    val repository: PhPlusRepository,
//    val basicInfoDao: BasicInfoDao
): ViewModel() {
//    fun getListItems(listId: String) = repository.getList(listId).asLiveData()
//    fun getParentTitle(parentId: Long) = repository.getParentTitle(parentId).asLiveData()
fun getValueByKey(key: String) = repository.getValueByKey(key)

}