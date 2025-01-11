package com.yandex.divkit.demo.data

/**
 * Created by vahid on 12/07/2015.
 */
object EncryptionConstant {
    @JvmField
//    var ENCRYPTION_KEY = "66e8c645-c2e5-47f5-b3be-fc4a7a60d3b6"
//    var ENCRYPTION_KEY = ""
    var ENCRYPTION_KEY = "d3d35354-48f2-48cc-9b69-6473fd4b1f84"
    @JvmField
//    var TOKEN = ""
    var TOKEN = ""
//    var TOKEN = "7f42a18a-70d9-4de8-8994-6ea235fd250c"
    @JvmField
    var ENCRYPTION_VALUE = "JVAaVhAiddKAaghraikhmaini"
    @JvmField
    var ENCRYPTION_BYTE_NUMBER = 16
    var ENCRYPTION_KEY_PH = ""
    var ENCRYPTION_KEY_FIXED = "Android"
    @JvmField
    var TOKEN_PH = ""
    var TOKEN_PH_MOBILE = ""
    var TOKEN_PH_ID: Long = 0
    var operationId: Long = 0
    var chkListId = ""
    var inspectedSubYeganId = ""
    val BASE_URL="http://svr-app.bazresi.naja.net:7001/phbazresi/"
//    val BASE_URL = "http://172.19.83.16:7002/phbazresi/"
//    val BASE_URL = "http://172.19.83.20:7001/phbazresi/"
//    val BASE_URL = "http://172.19.25.90:7002/phbazresi/"
    val LOG_SERVICE_TAG = "LOG_SERVICE_TAG"

    object ConstantDataBase{
        val DATABASE_NAME = "PhPlus"
    }



    object ConstantSharedPreferences{
        val SHARED_PREFS_NAME = "phplus"
        val TOKEN_PH = "token"
        val KEY_PH = "key"
        val FIRST_NAME = "firstName"
        val LAST_NAME = "lastName"
        val NATIONAL_ID = "nationalId"
        val PHONE_NUMBER = "phoneNumber"
        val POLICE_CODE = "policeCode"
        val FKPROVINCE_ID = "fkProvinceId"
        val USER_ID = "userId"
        val USER_NAME = "username"
        val PERSON_ID = "personId"
    }



    object ConstantParentId{
        val PLAN_TYPE :Long = 5010
        val EXECUTION_TYPE :Long = 5011
        val ENCOURAGE_TYPE :Long = 5013
        val ENCOURAGE_GRADE :Long = 5014
        val ROOL_TYPE :Long = 5015
        val PUNISHMENT_TYPE :Long = 5016
        val PUNISHMENT_GRADE :Long = 5017
        val FOLLOW_UP_METHOD :Long = 113
    }

}