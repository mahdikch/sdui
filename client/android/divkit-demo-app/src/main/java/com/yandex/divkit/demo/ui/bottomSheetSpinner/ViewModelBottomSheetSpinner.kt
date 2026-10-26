package com.yandex.divkit.demo.ui.bottomSheetSpinner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.divkit.demo.data.remote.Resource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject

class ViewModelBottomSheetSpinner @Inject constructor(
    val repository: PhPlusRepository,
) : ViewModel() {

    fun getValueByKey(key: String) = repository.getValueByKey(key)

    fun fetchService(
        phId: String,
        request: HashMap<String, String>,
        onResult: (Resource<Map<String, String>>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(Resource.loading(null))
            val result = withContext(Dispatchers.IO) {
                repository.phPlusDirect(phId, request)
            }
            onResult(result)
        }
    }
}
