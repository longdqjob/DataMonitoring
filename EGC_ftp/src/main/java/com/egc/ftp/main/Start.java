/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.ftp.main;

import com.egc.ftp.common.FTPUploadFile;
import com.egc.ftp.process.ProcessFtp;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author ThuyetLV
 */
public class Start {

    protected static final Logger logger = Logger.getLogger(Start.class);

    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("../etc/log4j.conf"));
            PropertyConfigurator.configure(props);

            FTPUploadFile.getInstance();

            Date date = new Date();

            System.out.println("date: " + date.getTime());
            System.out.println("date: " + System.currentTimeMillis());

//            exeCmd("ping -c 3 google.com");
//            exeCmd("influx -import -path=datarrr.txt");
            ProcessFtp.getInstance().start();
        } catch (IOException ex) {
            logger.error("ERROR Start: ", ex);
            System.exit(1);
        } catch (Exception ex) {
            logger.error("ERROR Start: ", ex);
            System.exit(1);
        }
    }
}
