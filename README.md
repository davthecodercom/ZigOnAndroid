# Zig on Android Integration Demo

A demonstration project showcasing how to integrate Zig programming language with Android development using JNI (Java Native Interface). This project features a modern Android app built with Jetpack Compose that calls native Zig functions.
Blogpost: https://www.davthecoder.com/blog/supercharging-android-apps-with-zig-a-complete-jni-integration-guide
## 🌟 Features

- **Modern Android Development**: Built with Jetpack Compose and Material 3 design
- **Zig Native Integration**: Uses Zig programming language for native code instead of C/C++
- **Cross-Platform Native Libraries**: Builds Zig code for multiple Android ABIs (arm64-v8a, armeabi-v7a, x86_64)
- **Automated Build Process**: Custom Gradle tasks handle Zig compilation and library placement
- **Error Handling**: Graceful fallback when native libraries are not available

## 🏗️ Project Structure

```
OboeDemo/
├── app/                          # Android app module
│   ├── src/main/
│   │   ├── java/com/example/oboedemo/
│   │   │   ├── MainActivity.kt   # Main activity with Compose UI
│   │   │   └── ZigLib.kt        # JNI wrapper for Zig functions
│   │   ├── jniLibs/             # Native libraries directory
│   │   │   ├── arm64-v8a/       # ARM 64-bit libraries
│   │   │   ├── armeabi-v7a/     # ARM 32-bit libraries
│   │   │   └── x86_64/          # x86 64-bit libraries
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts         # App-level build configuration with Zig integration
├── zig/                         # Zig source code
│   ├── src/
│   │   └── zigdemo.zig         # Native Zig functions with JNI exports
│   └── README.md               # Zig-specific documentation
└── README.md                   # This file
```

## 🚀 Getting Started

### Prerequisites

1. **Android Studio**: Latest version with Android SDK
2. **Android NDK**: Version r23+ (tested with r26+/r27)
3. **Zig**: Version 0.11+ (compatible with 0.15.1)
4. **Java**: Version 11+

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd OboeDemo
   ```

2. **Install Zig** (if not already installed):
   
   **macOS (Homebrew)**:
   ```bash
   brew install zig
   ```
   
   **Other platforms**: Download from [ziglang.org](https://ziglang.org/download/)

3. **Set up Android NDK**:
   - Install via Android Studio SDK Manager, or
   - Set `ANDROID_NDK` environment variable to your NDK path

4. **Build and run**:
   ```bash
   ./gradlew app:buildZigLibs  # Build Zig libraries
   ./gradlew assembleDebug     # Build Android app
   ```

### Environment Variables (Optional)

For custom installations, you can set:
- `ZIG_PATH`: Path to Zig executable
- `ANDROID_NDK`: Path to Android NDK installation

## 🔧 How It Works

### 1. Zig Native Code
The project includes a simple Zig function that adds two integers:

```zig
export fn Java_com_example_oboedemo_ZigLib_add(
    env: ?*c.JNIEnv, 
    clazz: c.jclass, 
    a: c.jint, 
    b: c.jint
) c.jint {
    return a + b;
}
```

### 2. Kotlin JNI Wrapper
The `ZigLib` class provides a Kotlin interface to the native Zig function:

```kotlin
object ZigLib {
    init {
        System.loadLibrary("zigdemo")
    }
    external fun add(a: Int, b: Int): Int
}
```

### 3. UI Integration
The Compose UI calls the native function and displays the result:

```kotlin
val sum = ZigLib.add(2, 3)
Text("Zig add(2,3) = $sum")
```

## 🏭 Build Process

The project includes a custom Gradle task that:

1. **Detects Zig Installation**: Automatically finds Zig executable
2. **Locates Android NDK**: Discovers NDK installation path
3. **Cross-Compiles**: Builds Zig code for multiple Android ABIs
4. **Places Libraries**: Copies compiled `.so` files to correct directories
5. **Integrates with Android Build**: Runs before Android compilation

### Supported ABIs
- `arm64-v8a` (64-bit ARM)
- `armeabi-v7a` (32-bit ARM)
- `x86_64` (64-bit x86)

## 🛠️ Development

### Adding New Zig Functions

1. **Add function to `zig/src/zigdemo.zig`**:
   ```zig
   export fn Java_com_example_oboedemo_ZigLib_newFunction(
       env: ?*c.JNIEnv, 
       clazz: c.jclass,
       param: c.jint
   ) c.jint {
       // Your Zig code here
       return param * 2;
   }
   ```

2. **Declare in `ZigLib.kt`**:
   ```kotlin
   external fun newFunction(param: Int): Int
   ```

3. **Rebuild native libraries**:
   ```bash
   ./gradlew app:buildZigLibs
   ```

### Testing

Run the test suite:
```bash
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumented tests
```

## 📱 Tech Stack

- **Language**: Kotlin, Zig
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Build System**: Gradle with Kotlin DSL
- **Native Interface**: JNI
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36

## 🎯 Why Zig?

This project demonstrates Zig as an alternative to C/C++ for Android native development:

- **Memory Safety**: Compile-time checks prevent common bugs
- **Cross-Compilation**: Excellent support for Android targets
- **Simple Interop**: Clean C ABI for JNI integration
- **Performance**: Zero-cost abstractions and optimizations
- **Developer Experience**: Clear error messages and tooling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🔗 Resources

- [Zig Programming Language](https://ziglang.org/)
- [Android NDK Documentation](https://developer.android.com/ndk)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](../../issues) page
2. Review the Zig-specific documentation in `zig/README.md`
3. Ensure your Zig and Android NDK installations are correct
4. Verify environment variables are set properly

---

**Happy coding with Zig and Android! 🚀**
