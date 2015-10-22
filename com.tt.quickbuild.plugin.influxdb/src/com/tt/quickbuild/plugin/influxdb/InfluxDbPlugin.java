package com.tt.quickbuild.plugin.influxdb;

import com.pmease.quickbuild.extensionpoint.StepProvider;
import com.pmease.quickbuild.pluginsupport.AbstractPlugin;
import com.pmease.quickbuild.stepsupport.Step;

/**
 * Main entrypoint when Quickbuild is loading plugins.
 */
public class InfluxDbPlugin extends AbstractPlugin {

    @Override
    public Object[] getExtensions() {
        return new Object[] {
            new StepProvider() {
                @Override
                public Class<? extends Step> getStepClass() {
                    return InfluxDbStep.class;
                }
            }
        };
    }

    /**
     * Have the possibility to change the InfluxDb settings per configuration.
     */
    @Override
    public Class<?> getConfigurationSettingClass() {
        return InfluxDbSetting.class;
    }
}
