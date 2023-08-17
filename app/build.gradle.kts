import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.8.10"
}

android {
    namespace = "moe.fuqiuluo.shamrock"
    compileSdk = 33

    defaultConfig {
        applicationId = "moe.fuqiuluo.shamrock"
        minSdk = 24
        targetSdk = 33
        versionCode = 4
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        externalNativeBuild {
            cmake {
                abiFilters += "arm64-v8a"
                cppFlags += ""
            }
        }
        packagingOptions {
            exclude("lib/armeabi-v7a/*")
            exclude("lib/x86/*")
            exclude("lib/x86_64/*")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes +=  "/META-INF/{AL2.0,LGPL2.1}"
            excludes +=  "/META-INF/*"
            excludes +=  "/META-INF/NOTICE.txt"
            excludes +=  "/META-INF/DEPENDENCIES.txt"
            excludes +=  "/META-INF/NOTICE"
            excludes +=  "/META-INF/LICENSE"
            excludes +=  "/META-INF/DEPENDENCIES"
            excludes +=  "/META-INF/notice.txt"
            excludes +=  "/META-INF/dependencies.txt"
            excludes +=  "/META-INF/LGPL2.1"
            excludes +=  "/META-INF/ASL2.0"
            excludes +=  "/META-INF/INDEX.LIST"
            excludes +=  "/META-INF/io.netty.versions.properties"
            excludes +=  "/META-INF/INDEX.LIST"
            excludes +=  "/META-INF/LICENSE.txt"
            excludes +=  "/META-INF/license.txt"
            excludes +=  "/META-INF/*.kotlin_module"
            excludes +=  "/META-INF/services/reactor.blockhound.integration.BlockHoundIntegration"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    configureAppSigningConfigsForRelease(project)
}

fun configureAppSigningConfigsForRelease(project: Project) {
    val keystorePath: String? = System.getenv("KEYSTORE_PATH")
    if (keystorePath.isNullOrBlank()) {
        println("ERROR: KEYSTORE_PATH is not set or is blank.")
        return
    }
    project.configure<ApplicationExtension> {
        signingConfigs {
            create("release") {
                storeFile = file(System.getenv("KEYSTORE_PATH"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
        buildTypes {
            release {
                signingConfig = signingConfigs.findByName("release")
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    //noinspection GradleDynamicVersion
    implementation("com.google.accompanist:accompanist-pager:0.31.5+")
    //noinspection GradleDynamicVersion
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.5+")
    //noinspection GradleDynamicVersion useless
    // implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0+")
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-host-common:2.3.3")
    implementation("io.ktor:ktor-server-status-pages:2.3.3")
    implementation("io.ktor:ktor-server-netty:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    // useless
    //implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.2.0")
    //implementation ("com.maxkeppeler.sheets-compose-dialogs:info:1.2.0")
    //implementation ("com.maxkeppeler.sheets-compose-dialogs:input:1.2.0")
    //implementation ("com.maxkeppeler.sheets-compose-dialogs:list:1.2.0")
    //implementation ("com.maxkeppeler.sheets-compose-dialogs:state:1.2.0")

    implementation(project(":xposed"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}