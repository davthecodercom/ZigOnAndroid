const c = @cImport({
    @cInclude("jni.h");
});

// JNI entry point: int ZigLib.add(int a, int b)
// Fully-qualified JNI name: Java_com_example_oboedemo_ZigLib_add
// This avoids calling back into JNI methods; it only sums two integers.
export fn Java_com_example_oboedemo_ZigLib_add(env: ?*c.JNIEnv, clazz: c.jclass, a: c.jint, b: c.jint) c.jint {
    _ = env;
    _ = clazz;
    return a + b;
}
