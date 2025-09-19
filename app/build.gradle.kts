plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.oboedemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.oboedemo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

// Task to build Zig libraries for Android
tasks.register("buildZigLibs") {
    description = "Build Zig shared libraries for Android ABIs"
    group = "build"
    
    doLast {
        // Try to find Zig executable
        val zigPath = findZigPath()
            ?: throw GradleException("Zig executable not found. Please install Zig or set ZIG_PATH environment variable to the zig executable path.")

        println("Using Zig at: $zigPath")
        
        // Try to find Android NDK path
        val androidNdkPath = findAndroidNdkPath()
            ?: throw GradleException("Android NDK not found. Please set ANDROID_NDK environment variable or ensure NDK is installed via Android Studio.")

        println("Using Android NDK at: $androidNdkPath")
        
        // Determine the NDK prebuilt directory based on OS
        val osName = System.getProperty("os.name").lowercase()
        val prebuiltDir = when {
            osName.contains("mac") -> "darwin-x86_64"
            osName.contains("linux") -> "linux-x86_64"
            osName.contains("windows") -> "windows-x86_64"
            else -> "linux-x86_64" // fallback
        }
        
        val androidSysrootInclude = "$androidNdkPath/toolchains/llvm/prebuilt/$prebuiltDir/sysroot/usr/include"
        val projectRoot = project.projectDir.parent
        val zigSourceFile = "$projectRoot/zig/src/zigdemo.zig"
        
        // Define target ABIs and their corresponding Zig targets (updated for Zig 0.15.1)
        val targets = mapOf(
            "arm64-v8a" to "aarch64-linux-android",
            "armeabi-v7a" to "arm-linux-androideabi", 
            "x86_64" to "x86_64-linux-android"
        )
        
        targets.forEach { (abi, zigTarget) ->
            val outputPath = "$projectDir/src/main/jniLibs/$abi/libzigdemo.so"
            
            // Create directory if it doesn't exist
            file("$projectDir/src/main/jniLibs/$abi").mkdirs()
            
            // Build command arguments (updated for Zig 0.15.1 syntax)
            val buildArgs = mutableListOf(
                zigPath, "build-lib",
                "-target", zigTarget,
                "-dynamic",
                "-O", "ReleaseSmall",
                "-fPIC",
                "-I", androidSysrootInclude,
                "-fsoname=libzigdemo.so",
                "-femit-bin=$outputPath",
                zigSourceFile
            )
            
            // Add specific CPU for armeabi-v7a
            if (abi == "armeabi-v7a") {
                buildArgs.add(7, "-mcpu=generic+v7a") // Insert after -fPIC
            }
            
            println("Building $abi: ${buildArgs.joinToString(" ")}")
            
            // Execute the build command
            val result = exec {
                commandLine = buildArgs
                isIgnoreExitValue = true
            }
            
            if (result.exitValue != 0) {
                throw GradleException("Failed to build Zig library for $abi")
            }
            
            println("Successfully built $abi library at $outputPath")
        }
        
        println("All Zig libraries built successfully!")
    }
}

// Function to find Android NDK path
fun findAndroidNdkPath(): String? {
    // Try environment variable first
    System.getenv("ANDROID_NDK")?.let { return it }
    
    // Try ANDROID_SDK_ROOT or ANDROID_HOME
    val sdkRoot = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (sdkRoot != null) {
        val ndkDir = file("$sdkRoot/ndk")
        if (ndkDir.exists()) {
            // Find the latest NDK version
            val ndkVersions = ndkDir.listFiles()?.filter { it.isDirectory }?.sortedByDescending { it.name }
            if (ndkVersions?.isNotEmpty() == true) {
                return ndkVersions.first().absolutePath
            }
        }
    }
    
    // Try common locations
    val commonPaths = listOf(
        "${System.getProperty("user.home")}/Library/Android/sdk/ndk", // macOS
        "${System.getProperty("user.home")}/Android/Sdk/ndk", // Linux
        "C:/Users/${System.getProperty("user.name")}/AppData/Local/Android/Sdk/ndk" // Windows
    )
    
    for (path in commonPaths) {
        val ndkDir = file(path)
        if (ndkDir.exists()) {
            val ndkVersions = ndkDir.listFiles()?.filter { it.isDirectory }?.sortedByDescending { it.name }
            if (ndkVersions?.isNotEmpty() == true) {
                return ndkVersions.first().absolutePath
            }
        }
    }
    
    return null
}

// Function to find Zig executable path
fun findZigPath(): String? {
    // Try environment variable first
    System.getenv("ZIG_PATH")?.let { zigPath ->
        val zigFile = file(zigPath)
        if (zigFile.exists() && zigFile.canExecute()) {
            return zigPath
        }
    }
    
    // Try to find zig in PATH by checking common locations
    val commonPaths = listOf(
        "/opt/homebrew/bin/zig", // Homebrew on Apple Silicon
        "/usr/local/bin/zig", // Homebrew on Intel Mac / manual install
        "/usr/bin/zig", // System package manager
        "${System.getProperty("user.home")}/zig/zig", // User local install
        "C:/zig/zig.exe", // Windows common location
        "C:/Program Files/zig/zig.exe" // Windows Program Files
    )
    
    for (path in commonPaths) {
        val zigFile = file(path)
        if (zigFile.exists() && zigFile.canExecute()) {
            return zigFile.absolutePath
        }
    }
    
    // Try to execute 'which zig' or 'where zig' command
    try {
        val whichCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "where" else "which"
        val process = ProcessBuilder(whichCommand, "zig").start()
        process.waitFor()
        if (process.exitValue() == 0) {
            val path = process.inputStream.bufferedReader().readText().trim()
            if (path.isNotEmpty()) {
                return path
            }
        }
    } catch (e: Exception) {
        // Ignore exceptions from which/where command
    }
    
    return null
}

// Make sure Zig libs are built before Android compilation
tasks.named("preBuild") {
    dependsOn("buildZigLibs")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}