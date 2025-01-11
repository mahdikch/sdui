package com.yandex.divkit.demo.data

import android.content.SharedPreferences
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.FIRST_NAME
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.FKPROVINCE_ID
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.KEY_PH
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.LAST_NAME
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.NATIONAL_ID
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.PERSON_ID
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.PHONE_NUMBER
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.POLICE_CODE
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.USER_ID
import com.yandex.divkit.demo.data.EncryptionConstant.ConstantSharedPreferences.USER_NAME
import com.yandex.divkit.demo.data.EncryptionConstant.TOKEN

import javax.inject.Inject

class SharePref @Inject constructor(private val sharePref: SharedPreferences) {

    var tokenPh: String?
        get() = sharePref.getString(TOKEN, "")
        set(value) {
            sharePref.apply {
                edit().putString(TOKEN, value).apply()
            }
        }

    var keyPh: String?
        get() = sharePref.getString(KEY_PH, "")
        set(value) {
            sharePref.apply {
                edit().putString(KEY_PH, value).apply()
            }
        }

    var firstName: String?
        get() = sharePref.getString(FIRST_NAME, "")
        set(value) {
            sharePref.apply {
                edit().putString(FIRST_NAME, value).apply()
            }
        }

    var lastName: String?
        get() = sharePref.getString(LAST_NAME, "")
        set(value) {
            sharePref.apply {
                edit().putString(LAST_NAME, value).apply()
            }
        }

    var nationalId: String?
        get() = sharePref.getString(NATIONAL_ID, "")
        set(value) {
            sharePref.apply {
                edit().putString(NATIONAL_ID, value).apply()
            }
        }

    var phoneNumber: String?
        get() = sharePref.getString(PHONE_NUMBER, "")
        set(value) {
            sharePref.apply {
                edit().putString(PHONE_NUMBER, value).apply()
            }
        }

    var policeCode: String?
        get() = sharePref.getString(POLICE_CODE, "")
        set(value) {
            sharePref.apply {
                edit().putString(POLICE_CODE, value).apply()
            }
        }

    var fkProvinceId: String?
        get() = sharePref.getString(FKPROVINCE_ID, "")
        set(value) {
            sharePref.apply {
                edit().putString(FKPROVINCE_ID, value).apply()
            }
        }

    var userId: String?
        get() = sharePref.getString(USER_ID, "")
        set(value) {
            sharePref.apply {
                edit().putString(USER_ID, value).apply()
            }
        }

    var username: String?
        get() = sharePref.getString(USER_NAME, "")
        set(value) {
            sharePref.apply {
                edit().putString(USER_NAME, value).apply()
            }
        }

    var personId: String?
        get() = sharePref.getString(PERSON_ID, "")
        set(value) {
            sharePref.apply {
                edit().putString(PERSON_ID, value).apply()
            }
        }

}