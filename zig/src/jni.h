#ifndef ZIG_STUB_JNI_H
#define ZIG_STUB_JNI_H

// Minimal JNI type stubs for editor/intellisense only.
// This header is intentionally tiny so zls/@cImport can resolve `jni.h`
// without requiring the Android NDK include path. The actual build should
// still use the real NDK jni.h via an -I sysroot include path as documented
// in zig/README.md.

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// Primitive Java types
typedef uint8_t  jboolean;
typedef int8_t   jbyte;
typedef uint16_t jchar;
typedef int16_t  jshort;
typedef int32_t  jint;
typedef int64_t  jlong;
typedef float    jfloat;
typedef double   jdouble;

// Opaque reference types
typedef void*    jobject;
typedef jobject  jclass;

// Opaque JNIEnv forward decl so code can use `JNIEnv*`
typedef struct JNIEnv JNIEnv;

#ifdef __cplusplus
}
#endif

#endif // ZIG_STUB_JNI_H
