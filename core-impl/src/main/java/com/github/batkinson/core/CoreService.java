package com.github.batkinson.core;

import com.github.batkinson.api.Service;

public class CoreService implements Service {

    @Override
    public String getName() {
        return CoreService.class.getName();
    }
}
