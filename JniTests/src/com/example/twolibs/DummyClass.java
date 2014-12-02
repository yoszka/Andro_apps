package com.example.twolibs;

import java.util.List;

import android.content.pm.PackageInfo;

public abstract class DummyClass<T>{
    public DummyClass() {
        // simple empty
    }

    public T get(int index) {
        return null;
    }
    
    public abstract List<PackageInfo> getInstalledPackages(int flags);
}
