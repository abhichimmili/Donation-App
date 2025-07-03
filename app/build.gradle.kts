plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.donation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.donation"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.appcompat)                      // AndroidX AppCompat
    implementation(libs.activity)                       // Activity lifecycle support
    implementation(libs.constraintlayout)               // ConstraintLayout for complex UIs
    implementation(libs.material)                       // Google Material Components
    implementation(libs.androidx.preference)            // Preferences screen support

    // UI Components & Image Handling
    implementation(libs.cardview)                       // CardView for cards UI
    implementation(libs.circleimageview)                // Circular ImageView (e.g., profile pics)
    implementation(libs.glide)                          // Glide for image loading
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")  // Glide annotation processor

    // Firebase Services
    implementation(libs.firebase.auth)                  // Firebase Authentication
    implementation(libs.firebase.database)              // Firebase Realtime Database
    implementation(libs.firebase.messaging)             // Firebase Cloud Messaging (push notifications)

    // Location & Maps
    implementation(libs.play.services.location)         // Google Play Services Location API
    implementation(libs.osmdroid.android)               // OpenStreetMap (OSMDroid)

    // Testing Libraries
    testImplementation(libs.junit)                      // Unit testing
    androidTestImplementation(libs.ext.junit)           // AndroidX JUnit extensions
    androidTestImplementation(libs.espresso.core)       // Espresso for UI testing
}
