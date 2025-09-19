package com.example.oboedemo

object ZigLib {
    init {
        // This will load libzigdemo.so from app/src/main/jniLibs/<abi>/
        // Make sure to place compiled shared libraries there for the ABIs you target.
        System.loadLibrary("zigdemo")
    }

    external fun add(a: Int, b: Int): Int
}