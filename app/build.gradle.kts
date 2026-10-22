plugins { id("com.android.application") }

android {
    namespace = "com.example.practicalfive"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.example.practicalfive"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.example.practicalfive.MunicipalityInstrumentation"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
