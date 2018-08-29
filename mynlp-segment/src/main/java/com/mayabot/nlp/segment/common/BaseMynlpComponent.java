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
        this.enabled = enabled;
    }
}
