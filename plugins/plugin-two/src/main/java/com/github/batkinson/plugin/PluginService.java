package com.github.batkinson.plugin;

import com.github.batkinson.api.Service;
import com.github.batkinson.api.ServiceDep;
import com.github.batkinson.plugin.crossplugin.NewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.stereotype.Service
@RestController
public class PluginService implements Service, NewService {

    private static Logger log = LoggerFactory.getLogger(PluginService.class);

    private ServiceDep munger;

    @RequestMapping(name = "/pluginEndpoint", produces = "text/plain")
    public String extendIt() {
        return "Hey!";
    }

    @Autowired
    public void setMunger(ServiceDep munger) {
        this.munger = munger;
    }

    @Override
    public String getName() {
        return munger.munge("Plugin Two");
    }

    @Override
    public void someNewMethod() {
        log.info("called the new method, cross-plugin dependencies are functional");
    }
}
