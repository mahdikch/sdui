package ir.nrdc.arbaeeintraficcontrol.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.yandex.divkit.demo.data.EncryptionConstant
import com.yandex.divkit.demo.data.remote.Resource
import kotlinx.coroutines.Dispatchers

@SuppressLint("LogNotTimber")
fun <T> myPerformGetOperation(
    tag: String,
    isShowLoading: Boolean,
    networkCall: suspend () -> Resource<T>,
): LiveData<Resource<T>> =

    liveData(Dispatchers.IO) {
//        if (isShowLoading)
//            ShowLoadingService.value.postValue(Resource.Status.LOADING)
        emit(Resource.loading(null))
//        val source = databaseQuery.invoke().map { Resource.success(it) }
//        emitSource(source)

        val responseStatus = networkCall.invoke()
        if (responseStatus.status == Resource.Status.SUCCESS) {
//            if (isShowLoading)
//                ShowLoadingService.value.postValue(Resource.Status.SUCCESS)
//            Log.i("${EncryptionConstant.LOG_SERVICE_TAG} $tag", responseStatus.data!!.toString())
            if (responseStatus.data==null){
                emit(Resource.error("پاسخ دریافتی از سرور نادرست است"))
            }else
            emit(Resource.success(responseStatus.data))
        }
        else if (responseStatus.status == Resource.Status.ERROR) {
//            if (isShowLoading)
//                ShowLoadingService.value.postValue(Resource.Status.ERROR)
//            Log.i("${EncryptionConstant.LOG_SERVICE_TAG} $tag", responseStatus.message!!.toString())
            if (responseStatus.message==null){
                emit(Resource.error("پاسخ دریافتی از سرور نادرست است"))
            }else
            emit(Resource.error(responseStatus.message, null ))
//            emitSource(source)
        }

    }