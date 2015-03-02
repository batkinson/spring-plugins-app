package com.github.batkinson.app;


import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

public class PluginLoader implements ApplicationContextInitializer<GenericWebApplicationContext>, BeanFactoryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    @Override
    public void initialize(GenericWebApplicationContext ctx) {

        log.error("initializing plugins");

        File pluginDir = new File("/home/batkinson/plugins");
        File[] files = pluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        ClassWorld world = new ClassWorld();

        try {
            ClassRealm loaderRealm = world.newRealm("loader", PluginLoader.class.getClassLoader());

            for (File file : files) {

                log.info("loading {}...", file);

                ClassRealm pluginRealm = world.newRealm(file.getName());
                pluginRealm.setParent(loaderRealm);
                pluginRealm.addConstituent(file.toURL());

                ClassLoader classLoader = pluginRealm.getClassLoader();

                XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(ctx);
                beanReader.setBeanClassLoader(classLoader);

                ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
                beanReader.setResourceLoader(resourceLoader);

                beanReader.loadBeanDefinitions("classpath:META-INF/plugin-context.xml");
            }
        } catch (DuplicateRealmException | MalformedURLException e) {
            log.error("failure creating realm", e);
        }
    }
}
