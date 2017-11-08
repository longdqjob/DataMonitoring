/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.main;

import com.egc.readfile.process.ProcessImportIfl;
import com.egc.readfile.process.ProcessImportReadFile;
import com.egc.readfile.process.ProcessTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author ThuyetLV
 */
public class Start {

    protected static final Logger logger = Logger.getLogger(Start.class);

    private static final String FILE_IMPORT_EXT = ".csv";
    private static final String fileDirectory = "D:\\tmp\\EGC\\template";
    private static final String fileName = "D:\\tmp\\EGC\\template\\HD_AXIAL_KEY_20160215.CSV";

    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("../etc/log4j.conf"));
            PropertyConfigurator.configure(props);
            
            Date date = new Date();
            
            System.out.println("date: " + date.getTime());
            System.out.println("date: " + System.currentTimeMillis());
            

//            exeCmd("ping -c 3 google.com");
//            exeCmd("influx -import -path=datarrr.txt");
            ProcessTest.getInstance().start();
            ProcessImportIfl.getInstance().start();
            ProcessImportReadFile.getInstance().start();
        } catch (IOException ex) {
            logger.error("ERROR Start: ", ex);
            System.exit(1);
        } catch (Exception ex) {
            logger.error("ERROR Start: ", ex);
            System.exit(1);
        }
    }

    public static void processReadFile() {
        File folder = new File(fileDirectory);
        File[] listOfFiles = folder.listFiles();

        String fileName;
        for (int i = 0; i < listOfFiles.length; i++) {
            fileName = listOfFiles[i].getName();
            if (listOfFiles[i].isFile()) {
                logger.info("File " + fileName);
                if (StringUtils.endsWith(fileName.toLowerCase(), FILE_IMPORT_EXT)) {
                    try {
                        CSVUtils.readUseCSVReader(fileDirectory + File.separator + fileName);
                    } catch (ParseException ex) {
                        logger.error("ERROR ParseException readUseCSVReader: ", ex);
                    }
                }
            } else if (listOfFiles[i].isDirectory()) {
                logger.warn("Directory " + fileName);
            }
        }
    }

    public static void exeCmd(String command) {
        logger.info("----exeCmd: " + command);
        String output = executeCommand(command);
        logger.info(output);
    }

    private static String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception ex) {
            logger.error("ERROR executeCommand: ", ex);
        }

        return output.toString();

    }
}
