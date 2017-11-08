/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.ftp.process;

import com.egc.ftp.common.FTPUploadFile;
import com.egc.ftp.common.Properties;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author ThuyetLV
 */
public class ProcessFtp extends ProcessThreadMX {

    private static final Logger logger = Logger.getLogger(ProcessFtp.class.getSimpleName());
    long sleepTime = 500;

    static ProcessFtp _instance;

    public static synchronized ProcessFtp getInstance() throws Exception {
        if (_instance == null) {
            _instance = new ProcessFtp(ProcessFtp.class.getSimpleName());
        }
        return _instance;
    }

    private String dir;
    private String ext;
    private int date = -1;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    ProcessFtp(String threadName) throws Exception {
        super(threadName);
        dir = Properties.getUploadDir();
        ext = Properties.getUploadExt();
    }

    private void beforeProcess() {
        Date now = new Date();
        if (now.getDate() != date) {
            FTPUploadFile.getInstance().createFolderBackupDaily(sdf.format(now));
            date = now.getDate();
        }
    }

    @Override
    protected void process() {
        try {
            beforeProcess();
            Thread.sleep(1000);
            processReadFile();
        } catch (Exception ex) {
            logger.error("ERROR process: ", ex);
        }
    }

    public void processReadFile() {
        long startTime = System.currentTimeMillis();
        boolean hasData = false;
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();

        String fileName;
        for (int i = 0; i < listOfFiles.length; i++) {
            fileName = listOfFiles[i].getName();
            if (listOfFiles[i].isFile()) {
                logger.info("File " + fileName);
                if (StringUtils.endsWith(fileName.toLowerCase(), ext)) {
                    try {
                        hasData = true;
                        FTPUploadFile.getInstance().uploadFileToRemoteServer(fileName, dir + File.separator + fileName, fileName);
                    } catch (Exception ex) {
                        logger.error("ERROR Exception uploadFileToRemoteServer: ", ex);
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
}
