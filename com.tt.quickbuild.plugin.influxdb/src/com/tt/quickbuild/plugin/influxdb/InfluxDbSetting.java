package com.tt.quickbuild.plugin.influxdb;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.quickbuild.annotation.Editable;

/**
 * Settings that can be modified per configuration.
 */
@Editable(name="InfluxDB settings", description="Specify plugin setting for InfluxDB.")
public class InfluxDbSetting {
    private String url;

    @Editable(description="URL of the InfluxDB instance (eg. http://192.168.1.1:8086)")
    @NotEmpty
    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        url = value;
    }
}
