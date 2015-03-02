package com.github.batkinson.core;

import com.github.batkinson.api.ServiceDep;

public class CoreServiceDep implements ServiceDep {
    @Override
    public String munge(String name) {
        return name.toUpperCase();
    }
}
