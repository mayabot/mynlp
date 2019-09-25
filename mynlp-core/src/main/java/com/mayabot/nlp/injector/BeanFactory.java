package com.mayabot.nlp.injector;

import org.jetbrains.annotations.NotNull;

public interface BeanFactory {
    public Object create(@NotNull Injector injector) ;
}
