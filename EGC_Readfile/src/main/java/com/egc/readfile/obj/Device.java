/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.obj;

import java.time.Instant;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 *
 * @author thuyetlv
 */
@Measurement(name = "logs")
public class Device {

    @Column(name = "time")
    private Instant time;
    @Column(name = "device", tag = true)
    private String device;
    @Column(name = "value")
    private Float value;
    
    private long timeImport;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public long getTimeImport() {
        return timeImport;
    }

    public void setTimeImport(long timeImport) {
        this.timeImport = timeImport;
    }

}
