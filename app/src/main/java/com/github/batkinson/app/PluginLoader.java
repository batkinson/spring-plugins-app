package com.github.batkinson.app;


import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

public class PluginLoader implements ApplicationContextInitializer<GenericWebApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    private File pluginDir;

    public PluginLoader(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    @Override
    public void initialize(GenericWebApplicationContext ctx) {

        log.info("initializing plugins");

        File[] files = pluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        ClassWorld world = new ClassWorld();
        ClassRealm loaderRealm = null;
        try {
            loaderRealm = world.newRealm("loader", PluginLoader.class.getClassLoader());
        } catch (DuplicateRealmException e) {
            log.error("failed to create realm for loader, plugin loading will be skipped", e);
            return;
        }

        /*
          We load plugins in three passes:
            * first pass : create realm for each file and add file to it
            * second pass: register import packages from other plugins
            * third pass: load bean definitions
         */

        Set<String> toLoad = new LinkedHashSet<String>();

        // Pass 1: locate and create realms for plugins
        for (File file : files) {
            try {
                String pluginName = file.getName();
                log.info("registering plugin from {}...", pluginName);
                URL pluginUrl = file.toURL();
                ClassRealm pluginRealm = world.newRealm(pluginName);
                pluginRealm.setParentRealm(loaderRealm);
                pluginRealm.addURL(pluginUrl);
                toLoad.add(pluginName);
            } catch (DuplicateRealmException | MalformedURLException e) {
                log.error("failed to register plugin", e);
            }
        }

        // Pass 2: register imports from other plugins
        for (File file : files) {
            String pluginName = file.getName();
            if (toLoad.contains(pluginName)) {
                try {
                    log.info("processing imports for plugin {}...", pluginName);
                    ClassRealm pluginRealm = world.getRealm(pluginName);
                    InputStream imports = pluginRealm.getResourceAsStream("META-INF/plugin-imports.txt");
                    if (imports != null) {
                        log.info("found plugin-imports.txt, importing...");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(imports));
                        String importLine;
                        while ((importLine = reader.readLine()) != null) {
                            String[] col = importLine.split(":");
                            if (col.length != 2) {
                                log.warn("invalid import line, should be plugin:package, was {}", importLine);
                                continue;
                            }
                            String plugin = col[0];
                            String packageToImport = col[1];
                            pluginRealm.importFrom(plugin, packageToImport);
                            log.info("imported {}:{}", plugin, packageToImport);
                        }
                    }
                } catch (IOException | NoSuchRealmException e) {
                    log.error("failed to process plugin", e);
                }
            }
        }

        // Pass 3: Load bean definitions for each plugin: order shouldn't matter - context instantiates later
        for (File file : files) {
            String pluginName = file.getName();
            if (toLoad.contains(pluginName)) {
                try {
                    log.info("processing imports for plugin {}...", pluginName);
                    ClassRealm pluginRealm = world.getRealm(pluginName);
                    XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(ctx);
                    beanReader.setBeanClassLoader(pluginRealm);
                    ResourceLoader resourceLoader = new DefaultResourceLoader(pluginRealm);
                    beanReader.setResourceLoader(resourceLoader);
                    beanReader.loadBeanDefinitions("classpath:META-INF/plugin-context.xml");
                } catch (NoSuchRealmException e) {
                    log.error("failed to load definitions from plugin");
                }
            }
        }
    }
}
