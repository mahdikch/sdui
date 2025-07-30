
plugins {
    id("com.android.library")
//    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ir.nrdc.camera"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    //    kotlinOptions {
//        jvmTarget = "1.8"
//    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
//    implementation(libs.androidx.core)
//    implementation(libs.androidx.core.ktx)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //camera
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation( "androidx.camera:camera-camera2:1.4.0")
    implementation( "androidx.camera:camera-core:1.4.0")
    implementation ("com.github.yalantis:ucrop:2.2.10")
//    implementation ("com.github.dhaval2404:imagepicker:2.1")
//    implementation(files("libs/imagepicker-v2.0.aar"))


}