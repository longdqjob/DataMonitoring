/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.main;

import com.egc.readfile.process.ProcessImportIfl;
import com.egc.readfile.process.ProcessImportReadFile;
import com.egc.readfile.process.ProcessTest;
import org.apache.log4j.Logger;

/**
 *
 * @author ThuyetLV
 */
public class Stop {

    protected static final Logger logger = Logger.getLogger(Start.class);

    public static void main(String[] args) throws Exception {
        try {
            ProcessImportReadFile.getInstance().stop();
            ProcessImportIfl.getInstance().stop();
            ProcessTest.getInstance().stop();
        } catch (Exception ex) {
            logger.error("ERROR stop: ", ex);
            System.exit(1);
        }
    }
}
