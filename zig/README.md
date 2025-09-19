# Zig + Android (JNI) Demo for this project

This repository now contains a minimal example of calling a Zig function from Kotlin via JNI.

What’s included:
- Kotlin class `ZigLib` that loads `libzigdemo.so` and declares an `external` function `add(a, b)`.
- Compose UI updated to call `ZigLib.add(2, 3)` and display the result (or a helpful message if the library is missing).
- A Zig source file that exports the JNI entry point `Java_com_example_oboedemo_ZigLib_add`.
- `jniLibs` directories created for common Android ABIs, where you should place the compiled `.so` files.

## Requirements
- Zig 0.11+ (or newer)
- Android NDK r23+ (tested paths assume r26+/r27 layout). Ensure you know your NDK path.

## Zig source
File: `zig/src/zigdemo.zig`

```zig
const c = @cImport({
    @cInclude("jni.h");
});

// JNI entry point: int ZigLib.add(int a, int b)
// Fully-qualified JNI name: Java_com_example_oboedemo_ZigLib_add
export fn Java_com_example_oboedemo_ZigLib_add(env: ?*c.JNIEnv, clazz: c.jclass, a: c.jint, b: c.jint) c.jint {
    _ = env;
    _ = clazz;
    return a + b;
}
```

This keeps things simple by avoiding calls back into JNI (no strings/objects), just adds two integers.

## Build commands
These commands compile Zig into a shared library for Android ABIs and copy them into `app/src/main/jniLibs/<abi>/libzigdemo.so` so the app can load them.

Set variables first (adjust paths for your setup):

```sh
# macOS example; adjust for Linux/Windows accordingly
export ANDROID_NDK="$HOME/Library/Android/sdk/ndk/27.0.12077973"

# Common include dir that provides jni.h
export ANDROID_SYSROOT_INCLUDE="$ANDROID_NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include"

# Project root (this repo)
export PROJ_ROOT="$(pwd)"
```

Build each ABI and copy to jniLibs:

```sh
# arm64-v8a
zig build-lib \
  -target aarch64-android \
  -dynamic \
  -O ReleaseSmall \
  -fPIC \
  -I "$ANDROID_SYSROOT_INCLUDE" \
  -lc \
  -fsoname=libzigdemo.so \
  -o "$PROJ_ROOT/app/src/main/jniLibs/arm64-v8a/libzigdemo.so" \
  "$PROJ_ROOT/zig/src/zigdemo.zig"

# armeabi-v7a
zig build-lib \
  -target arm-android \
  -mcpu=generic+v7a \
  -dynamic \
  -O ReleaseSmall \
  -fPIC \
  -I "$ANDROID_SYSROOT_INCLUDE" \
  -lc \
  -fsoname=libzigdemo.so \
  -o "$PROJ_ROOT/app/src/main/jniLibs/armeabi-v7a/libzigdemo.so" \
  "$PROJ_ROOT/zig/src/zigdemo.zig"

# x86_64 (emulators)
zig build-lib \
  -target x86_64-android \
  -dynamic \
  -O ReleaseSmall \
  -fPIC \
  -I "$ANDROID_SYSROOT_INCLUDE" \
  -lc \
  -fsoname=libzigdemo.so \
  -o "$PROJ_ROOT/app/src/main/jniLibs/x86_64/libzigdemo.so" \
  "$PROJ_ROOT/zig/src/zigdemo.zig"
```

Notes:
- The `-I` flag points Zig to the NDK sysroot headers that contain `jni.h`.
- We link against the C runtime with `-lc`.
- If you need logging, also link `-landroid` and `-llog` and include the appropriate headers.

## Kotlin usage
- Loader and JNI declaration are in `app/src/main/java/com/example/oboedemo/ZigLib.kt`:

```kotlin
object ZigLib {
    init { System.loadLibrary("zigdemo") }
    external fun add(a: Int, b: Int): Int
}
```

- The UI already calls it and shows the result or a helpful message if the library is missing at runtime.

## Running
1. Build the `.so` files using the commands above for the ABIs you target.
2. Ensure the outputs are placed under:
   - `app/src/main/jniLibs/arm64-v8a/libzigdemo.so`
   - `app/src/main/jniLibs/armeabi-v7a/libzigdemo.so`
   - `app/src/main/jniLibs/x86_64/libzigdemo.so`
3. Build and run the app on a device/emulator with a matching ABI. The greeting text will display: `Zig add(2,3) = 5`.

## Optional: Gradle integration
You can automate the Zig build via a Gradle task that invokes the `zig build-lib` commands before `:app:assemble`. For a simple demo, the manual commands above are sufficient.
