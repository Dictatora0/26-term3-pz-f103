package com.iteaj.framework.spring.boot;

import com.iteaj.framework.ProfilesInclude;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.HashSet;
import java.util.Set;

public class FrameworkEnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private static String packagePrefix = "com.iteaj.iboot.plugin";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Set<String> includes = new HashSet<>(16);
        event.getEnvironment().addActiveProfile("framework");
        ClassLoader classLoader = event.getSpringApplication().getClassLoader();
        ImportCandidates load = ImportCandidates.load(AutoConfiguration.class, classLoader);
        load.forEach(beanName -> {
            if(beanName.contains(packagePrefix)) {
                try {
                    Class<?> aClass = Class.forName(beanName, false, classLoader);
                    ProfilesInclude annotation = aClass.getAnnotation(ProfilesInclude.class);
                    if(annotation != null) {
                        String[] value = annotation.value();
                        if(value != null && value.length > 0) {
                            for (int i = 0; i < value.length; i++) {
                                includes.add(value[i]);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    //
                }
            }
        });

        if(includes.size() > 0) {
            includes.forEach(profile -> event.getEnvironment().addActiveProfile(profile));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
