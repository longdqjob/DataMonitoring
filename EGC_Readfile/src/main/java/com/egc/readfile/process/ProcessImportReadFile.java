/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.process;

import com.egc.readfile.common.Properties;
import com.egc.readfile.main.CSVUtils;
import java.io.File;
import java.text.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author ThuyetLV
 */
public class ProcessImportReadFile extends ProcessThreadMX {

    private static final Logger logger = Logger.getLogger(ProcessImportReadFile.class.getSimpleName());
    long sleepTime = 500;

    static ProcessImportReadFile _instance;

    public static synchronized ProcessImportReadFile getInstance() throws Exception {
        if (_instance == null) {
            _instance = new ProcessImportReadFile(ProcessImportReadFile.class.getSimpleName());
        }
        return _instance;
    }

    private String importDir;
    private String importExt;

    ProcessImportReadFile(String threadName) throws Exception {
        super(threadName);
        importDir = Properties.getImportDir();
        importExt = Properties.getImportExt();
    }

    public void processReadFile() {
        long startTime = System.currentTimeMillis();
        boolean hasData = false;
        File folder = new File(importDir);
        File[] listOfFiles = folder.listFiles();

        String fileName;
        for (int i = 0; i < listOfFiles.length; i++) {
            fileName = listOfFiles[i].getName();
            if (listOfFiles[i].isFile()) {
                logger.info("File " + fileName);
                if (StringUtils.endsWith(fileName.toLowerCase(), importExt)) {
                    try {
                        hasData = true;
                        CSVUtils.readUseCSVReader(importDir + File.separator + fileName);
                    } catch (ParseException ex) {
                        logger.error("ERROR ParseException readUseCSVReader: ", ex);
                    }
                }
            } else if (listOfFiles[i].isDirectory()) {
                logger.warn("Directory " + fileName);
            }
        }
        if (hasData) {
            logger.info("Time to processReadFile: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    @Override
    protected void process() {
        try {
            Thread.sleep(500);
            processReadFile();
        } catch (Exception ex) {
            logger.error("ERROR process: ", ex);
        }
    }
}
