plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun Project.resolveSecret(name: String, fallback: String): String {
    val fromGradleProp = providers.gradleProperty(name).orNull
    val fromRootProp = rootProject.providers.gradleProperty(name).orNull
    val fromEnv = providers.environmentVariable(name).orNull
    return (fromGradleProp ?: fromRootProp ?: fromEnv)?.trim()?.takeIf { it.isNotBlank() } ?: fallback
}

android {
    namespace = "com.pierregasly.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pierregasly.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase (set via gradle.properties or environment variables)
        val supabaseUrl = project.resolveSecret("SUPABASE_URL", "https://YOUR_PROJECT_ID.supabase.co")
        val supabaseAnonKey = project.resolveSecret("SUPABASE_ANON_KEY", "YOUR_SUPABASE_ANON_KEY")


        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "SUPABASE_REST_URL", "\"${supabaseUrl.trimEnd('/')}/rest/v1/\"")
        buildConfigField("String", "SUPABASE_AUTH_URL", "\"${supabaseUrl.trimEnd('/')}/auth/v1\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
