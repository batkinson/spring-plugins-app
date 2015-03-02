package com.github.batkinson.plugin;

import com.github.batkinson.api.Service;
import com.github.batkinson.api.ServiceDep;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class PluginService implements Service {

    private ServiceDep munger;

    @Autowired
    public void setMunger(ServiceDep munger) {
        this.munger = munger;
    }

    public String getName() {
        return munger.munge("Plugin One");
    }

}
