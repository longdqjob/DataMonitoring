package com.egc.ftp.common;

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
            pr = new PropertiesConfiguration("../etc/ftp.properties");
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
    public static String getHost() {
        return getString("FTP_HOST", "");
    }

    public static Integer getPort() {
        return getInteger("FTP_PORT", 21);
    }

    public static String getUser() {
        return getString("FTP_USER", "");
    }

    public static String getPass() {
        return getString("FTP_PASS", "");
    }

    public static String getFixHost() {
        return getString("FTP_FIX_HOST", "");
    }
    
    public static String getFtpMode() {
        return getString("FTP_MODE", "PassiveMode");
    }

    public static String getFtpDir() {
        return getString("FTP_DIR", "");
    }

    public static Integer getBufferSize() {
        return getInteger("FTP_BUFF", 100000);
    }
    //Read
    public static String getUploadDir() {
        return getString("READ_DIR", "");
    }
    public static String getUploadExt() {
        return getString("READ_EXT", ".csv");
    }
    public static String getBackUpFolder() {
        return getString("BACKUP_DIR", "");
    }
}
