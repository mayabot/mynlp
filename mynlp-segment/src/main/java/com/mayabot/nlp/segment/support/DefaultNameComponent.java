package com.mayabot.nlp.segment.support;

import com.google.common.base.Preconditions;
import com.mayabot.nlp.segment.NamedComponent;

public abstract class DefaultNameComponent implements NamedComponent {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        Preconditions.checkState(name==null,"Name 只能被设置一次");
        this.name = name;
    }
}
