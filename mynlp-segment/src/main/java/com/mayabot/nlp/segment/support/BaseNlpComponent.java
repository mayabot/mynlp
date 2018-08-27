package com.mayabot.nlp.segment.support;

import com.mayabot.nlp.segment.NlpSegmentComponent;

/**
 * @author jimichan
 */
public abstract class BaseNlpComponent implements NlpSegmentComponent {


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
