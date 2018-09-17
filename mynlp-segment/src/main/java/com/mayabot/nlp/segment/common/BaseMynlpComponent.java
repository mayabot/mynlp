package com.mayabot.nlp.segment.common;

import com.mayabot.nlp.segment.MynlpComponent;

/**
 * @author jimichan
 */
public abstract class BaseMynlpComponent implements MynlpComponent {

    private boolean enabled = true;

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("XProcess", "");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    private int order = 0;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }
}
