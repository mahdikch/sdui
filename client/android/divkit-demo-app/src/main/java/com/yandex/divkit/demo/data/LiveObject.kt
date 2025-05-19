package com.yandex.divkit.demo.data

import androidx.lifecycle.MutableLiveData
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.remote.Resource


//object PhPlusObject {
//    val value = MutableLiveData<ListItemDto>()
//}
//
//object YeganBottomSheetSpinnerClickListener {
//    val value = MutableLiveData<OrgUnit>()
//}
//object FacilitiesBottomSheetSpinnerClickListener {
//    val value = MutableLiveData<OrgUnit>()
//}
//object AgreementYeganBottomSheetSpinnerClickListener {
//    val value = MutableLiveData<OrgUnit>()
//}

object RemoteData {
    var value = MutableLiveData<HashMap<String,String>>()
}

object VariableToSetMap {
    var value = MutableLiveData<MutableList<PhPlusDB>>()
}

object ShowLoadingService {
    val value = MutableLiveData<Resource.Status>()
}

object ScreenToLoad {
    val value = MutableLiveData<String>()
}


object VariableToGet {
    val value = MutableLiveData<String>()
}
object VariableToSet {
    val value = MutableLiveData<String>()
}