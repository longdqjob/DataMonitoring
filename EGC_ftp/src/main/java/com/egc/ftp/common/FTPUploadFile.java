/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.ftp.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

/**
 *
 * @author THUYETLV
 */
public class FTPUploadFile {

    public static final Logger log = Logger.getLogger(FTPUploadFile.class.getName());

    private static FTPUploadFile fTPUploadFile;

    public static synchronized FTPUploadFile getInstance() {
        if (fTPUploadFile == null) {
            fTPUploadFile = new FTPUploadFile();
        }
        return fTPUploadFile;
    }

    private String server;
    private int port;
    private String user;
    private String pass;
    private String fixHost;
    private int bufferSize;
    private String dir;
    private String mode;
    private String backupFolder;
    private String backupFolderDaily;

    private FTPUploadFile() {
        init();
    }

    private void init() {
        server = Properties.getHost();
        port = Properties.getPort();
        user = Properties.getUser();
        pass = Properties.getPass();
        fixHost = Properties.getFixHost();
        bufferSize = Properties.getBufferSize();
        dir = Properties.getFtpDir();
        mode = Properties.getFtpMode();
        backupFolder = Properties.getBackUpFolder();
        backupFolderDaily = backupFolder;
        createFolderBackup();
        log.info("-----dir: " + dir);
        log.info("-----backupFolder: " + backupFolder);
    }

    private void createFolderBackup() {
        if (!StringUtils.isBlank(backupFolder)) {
            File theDir = new File(backupFolder);

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                log.info("creating directory: " + theDir.getName());
                boolean result = false;

                try {
                    theDir.mkdir();
                    result = true;
                } catch (SecurityException se) {
                    //handle it
                }
                if (result) {
                    log.info("DIR created");
                }
            }
        }
    }

    public void createFolderBackupDaily(String date) {
        if (!StringUtils.isBlank(backupFolder)) {
            createFolderBackup();
            backupFolderDaily = backupFolder + File.separator + date;
            File theDir = new File(backupFolderDaily);

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                log.info("creating directory: " + backupFolderDaily);
                boolean result = false;

                try {
                    theDir.mkdir();
                    result = true;
                } catch (SecurityException se) {
                    //handle it
                }
                if (result) {
                    log.info("DIR created");
                }
            }
        }
    }

    /**
     *
     * @param absolutePath
     * @param remoteFile
     * @return
     */
    public boolean uploadFileToRemoteServer(String localFileName, String absolutePath, String remoteFile) {
        FTPClient ftpClient = new FTPClient();
        boolean result = false;
        File firstLocalFile = null;
        boolean backupSuccess = false;
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            if (mode.equalsIgnoreCase("PassiveMode")) {
                ftpClient.enterLocalPassiveMode();
            } else if (mode.equalsIgnoreCase("ActiveMode")) {
                ftpClient.enterLocalActiveMode();
            }

            ftpClient.enterLocalPassiveMode();
            if (fixHost.equalsIgnoreCase("true")) {
                ftpClient.setUseEPSVwithIPv4(true);
                ftpClient.setRemoteVerificationEnabled(false);
            }
            //End Add

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //Set BufferSize
            if (bufferSize > ftpClient.getBufferSize()) {
                log.warn("BufferSize too small: " + ftpClient.getBufferSize());
                ftpClient.setBufferSize(bufferSize);
            }

            //Set WorkingDirectory
            log.info("printWorkingDirectory: " + ftpClient.printWorkingDirectory());
            if (!ftpClient.printWorkingDirectory().equals(dir)) {
                ftpClient.changeWorkingDirectory(dir);
                log.info("changeWorkingDirectory: " + ftpClient.printWorkingDirectory());
                showServerReply(ftpClient);
            }

            firstLocalFile = new File(absolutePath);
            
            //Backup file
            backupSuccess = backupFile(firstLocalFile, localFileName);

            FileInputStream inputStream = new FileInputStream(firstLocalFile);

            String remoteFileName = remoteFile;
            if (remoteFile.contains(File.separator)) {
                String remotePath = remoteFile.substring(0, remoteFile.lastIndexOf(File.separator));
                remoteFileName = remoteFile.substring(remoteFile.lastIndexOf(File.separator) + 1);

                if (!remotePath.endsWith(File.separator)) {
                    remotePath += File.separator;
                }
                if (!ftpClient.changeWorkingDirectory(remotePath)) {
                    if (!makeDirectories(ftpClient, remotePath)) {
                        log.error("ERROR uploadFileToRemoteServer: makeDirectories " + remotePath + " failure.");
                        return false;
                    }
                }
            }

            boolean done = ftpClient.storeFile(remoteFileName, inputStream);
            inputStream.close();
            if (done) {
                String rtn_tmp = ftpClient.printWorkingDirectory().replaceAll("\"", "");
                rtn_tmp = rtn_tmp + "/" + remoteFileName;
                ftpClient.sendSiteCommand("chmod " + "755 " + rtn_tmp);
                result = true;
            } else {
                showServerReply(ftpClient);
                log.warn("storeFile: " + remoteFileName + " fail..");
                result = false;
            }
        } catch (IOException ex) {
            log.error("IOException uploadFileToRemoteServer: ", ex);
            result = false;
        } finally {
            if (backupSuccess && firstLocalFile != null) {
                firstLocalFile.delete();
            }
            try {
                if (ftpClient != null && ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                log.error("IOException uploadFileToRemoteServer: ", ex);
            }
        }
        return result;
    }

    private boolean backupFile(File source, String fileName) {
        if (!StringUtils.isBlank(backupFolderDaily)) {
            try {
                File dest = new File(backupFolderDaily + File.separator + fileName);
                FileUtils.copyFile(source, dest);
            } catch (Exception ex) {
                log.error("ERROR backupFile: " + fileName, ex);
                return false;
            }
        }
        return true;
    }

    public boolean makeDirectories(FTPClient ftpClient, String dirPath)
            throws IOException {
        try {
            String[] pathElements = dirPath.split("\\" + File.separator);
            if (pathElements != null && pathElements.length > 0) {
                for (String singleDir : pathElements) {
                    if (singleDir == null || singleDir.length() <= 0) {
                        continue;
                    }
                    boolean existed = ftpClient.changeWorkingDirectory(singleDir);
                    if (!existed) {
                        boolean created = ftpClient.makeDirectory(singleDir);
                        if (created) {
                            ftpClient.changeWorkingDirectory(singleDir);
                        } else {
                            log.error("makeDirectories false: " + singleDir + " + " + ftpClient.getReplyCode() + " - " + ftpClient.getReplyString());
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception makeDirectories: ", ex);
            return false;
        }
    }

    public boolean deleteFile(String filePath) {
        FTPClient client = new FTPClient();
        try {
            client.connect(server, port);
            client.login(user, pass);

            if (!client.printWorkingDirectory().equals(dir)) {
                log.info("changeWorkingDirectory " + dir + ": " + client.changeWorkingDirectory(dir));
                showServerReply(client);
            }

            client.sendSiteCommand("chmod " + "755" + filePath);

            if (client.deleteFile(filePath)) {
                log.info("File deleted " + filePath);
                return true;
            }
            log.info("File deleted false " + filePath);
        } catch (IOException ex) {
            log.error("IOException uploadFileToRemoteServer: ", ex);
            return false;
        } catch (Exception ex) {
            log.error("Exception uploadFileToRemoteServer: ", ex);
            return false;
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout();
                    client.disconnect();
                }

            } catch (Exception ex) {
                log.error("Exception disconnect deleteFile: ", ex);
                return false;
            }
        }
        return false;
    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                log.warn("SERVER: " + aReply);
            }
        }
    }
}
