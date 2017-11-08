package com.egc.readfile.common;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * doc thong tin tu file properties
 *
 * @author ThuyetLV
 */
public class Properties {

    protected static final Logger logger = Logger.getLogger(Properties.class);

    private static Configuration pr;

    static {
        try {
            pr = new PropertiesConfiguration("../etc/app.properties");
        } catch (ConfigurationException ex) {
            logger.error("ERROR Can not load configuration file", ex);
        }
    }

    public static int getInteger(String propertyKey, int defaultValue) {
        try {
            int prop = pr.getInt(propertyKey, defaultValue);
            return prop;
        } catch (ConversionException ce) {
            logger.warn("The value of " + propertyKey + " was not a int, instead using default value  " + defaultValue);
            return defaultValue;
        }
    }

    public static long getLong(String propertyKey, long defaultValue) {
        try {
            long prop = pr.getLong(propertyKey, defaultValue);
            return prop;
        } catch (ConversionException ce) {
            logger.warn("The value of " + propertyKey + " was not a long, instead using default value  " + defaultValue);
            return defaultValue;
        }
    }

    public static String getString(String propertyKey, String defaultValue) {
        try {
            String prop = pr.getString(propertyKey, defaultValue);
            return prop;
        } catch (ConversionException ce) {
            logger.warn("The value of " + propertyKey + " was not a string, instead using default value  " + defaultValue);
            return defaultValue;
        }
    }

    public static List getList(String propertyKey, List defaultValue) {
        try {
            List prop = pr.getList(propertyKey, defaultValue);
            return prop;
        } catch (ConversionException ce) {
            logger.warn("The value of " + propertyKey + " was not a list, instead using default value  " + defaultValue);
            return defaultValue;
        }
    }

    //Config
    public static String getImportDir() {
        return getString("READ_DIR", "");
    }

    public static String getImportExt() {
        return getString("READ_EXT.url", ".csv");
    }

    //INFLUXDB
    public static String getInfluxdbUrl() {
        return getString("influxdb.url", "http://127.0.0.1:8086");
    }

    public static String getInfluxdbUser() {
        return getString("influxdb.user", "");
    }

    public static String getInfluxdbPass() {
        return getString("influxdb.pass", "");
    }

    public static String getInfluxdbDb() {
        return getString("influxdb.database", "collectd");
    }

    public static String getInfluxdbMeasurement() {
        return getString("influxdb.measurement", "logs");
    }

    public static Integer getInfluxdbBatchMin() {
        return getInteger("influxdb.batch.min", 100);
    }

    public static Integer getInfluxdbBatchMax() {
        return getInteger("influxdb.batch.max", 2000);
    }
}
