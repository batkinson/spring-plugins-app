package com.github.batkinson.plugin;

import com.github.batkinson.api.Service;
import com.github.batkinson.api.ServiceDep;

public class PluginService implements Service {

    private ServiceDep munger;

    public void setMunger(ServiceDep munger) {
        this.munger = munger;
    }

    public String getName() {
        return munger.munge("Plugin One");
    }

}
