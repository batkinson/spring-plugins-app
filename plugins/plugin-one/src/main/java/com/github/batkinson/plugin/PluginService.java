package com.github.batkinson.plugin;

import com.github.batkinson.api.Service;
import com.github.batkinson.api.ServiceDep;
import com.github.batkinson.plugin.crossplugin.NewService;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class PluginService implements Service {

    private ServiceDep munger;

    private NewService crossPluginService;

    @Autowired
    public void setCrossPluginService(NewService newService) {
        this.crossPluginService = newService;
    }

    @Autowired
    public void setMunger(ServiceDep munger) {
        this.munger = munger;
    }

    public String getName() {
        crossPluginService.someNewMethod();
        return munger.munge("Plugin One");
    }

}
