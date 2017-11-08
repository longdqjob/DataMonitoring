/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.ftp.process;

/**
 *
 * @author ThuyetLV
 */
public class ProcessThreadMX extends ProcessThread {

    public ProcessThreadMX() {
        super("ProcessThreadMX");
    }

    public ProcessThreadMX(String threadName) {
        super(threadName);
    }

    protected void process() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
