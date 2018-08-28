package com.mayabot.nlp.segment.support;

import com.mayabot.nlp.segment.MynlpComponent;

/**
 * @author jimichan
 */
public abstract class BaseNlpComponent implements MynlpComponent {


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
        this.enabled = enabled;
    }
}
