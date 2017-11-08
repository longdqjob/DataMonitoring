/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.ftp.main;

import com.egc.ftp.process.ProcessFtp;
import org.apache.log4j.Logger;

/**
 *
 * @author ThuyetLV
 */
public class Stop {

    protected static final Logger logger = Logger.getLogger(Start.class);

    public static void main(String[] args) throws Exception {
        try {
            ProcessFtp.getInstance().stop();
        } catch (Exception ex) {
            logger.error("ERROR stop: ", ex);
            System.exit(1);
        }
    }
}
