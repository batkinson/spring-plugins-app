package com.github.batkinson.app;

import com.github.batkinson.api.Service;
import com.github.batkinson.api.ServiceDep;
import com.github.batkinson.core.CoreService;
import com.github.batkinson.core.CoreServiceDep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Configuration
@RestController
@SpringBootApplication
public class Main extends SpringBootServletInitializer {

    @Bean
    public Service coreService() {
        return new CoreService();
    }

    @Bean
    ServiceDep coreServiceDep() {
        return new CoreServiceDep();
    }

    @Autowired
    GenericApplicationContext ctx;

    @RequestMapping(value = "/", produces = "text/plain")
    String home() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Service> entry : ctx.getBeansOfType(Service.class).entrySet()) {
            buf.append(entry.getKey());
            Service service = entry.getValue();
            buf.append(" ");
            buf.append(service.getClass().getName());
            buf.append(" [");
            buf.append(service.getClass().getClassLoader());
            buf.append("] ");
            buf.append(entry.getValue().getName());
            buf.append(String.format("%n"));
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
            .sources(Main.class)
            .initializers(new PluginLoader())
            .run(args);
    }

}
