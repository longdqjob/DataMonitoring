/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.process;

import com.egc.readfile.common.Properties;
import com.egc.readfile.obj.Device;
import static com.egc.readfile.process.ProcessTest.defaultRetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

/**
 *
 * @author ThuyetLV
 */
public class ProcessImportIfl extends ProcessThreadMX {

    private static final Logger logger = Logger.getLogger(ProcessImportIfl.class.getSimpleName());
    long sleepTime = 500;

    static ProcessImportIfl _instance;

    public static synchronized ProcessImportIfl getInstance() throws Exception {
        if (_instance == null) {
            _instance = new ProcessImportIfl(ProcessImportIfl.class.getSimpleName());
        }
        return _instance;
    }

    BlockingQueue<Device> queue;

    InfluxDB influxDB;
    String measurement;
    String dbName;

    ProcessImportIfl(String threadName) throws Exception {
        super(threadName);
        queue = new LinkedBlockingQueue<Device>();
        //Init InfluxDB
        String url = Properties.getInfluxdbUrl();
        String user = Properties.getInfluxdbUser();
        String pass = Properties.getInfluxdbPass();
        measurement = Properties.getInfluxdbMeasurement();
        if (StringUtils.isBlank(user) || StringUtils.isBlank(pass)) {
            influxDB = InfluxDBFactory.connect(url);
        } else {
            influxDB = InfluxDBFactory.connect(url, user, pass);
        }
        dbName = Properties.getInfluxdbDb();
        influxDB.setDatabase(Properties.getInfluxdbDb());

    }

    @Override
    protected void process() {
        try {
            Thread.sleep(500);
            if (queue != null && queue.size() > 0) {
                List<Device> listRecord = new ArrayList<Device>(Properties.getInfluxdbBatchMax() - 20);
                queue.drainTo(listRecord, Properties.getInfluxdbBatchMax() - 20);
                processLstRecord(listRecord);
            }
        } catch (Exception ex) {
            logger.error("ERROR process: ", ex);
        }
    }

    private void processLstRecord(List<Device> listRecord) {
        long startTime = System.currentTimeMillis();
        try {
            this.influxDB.enableBatch(Properties.getInfluxdbBatchMax(), Properties.getInfluxdbBatchMin(), TimeUnit.MILLISECONDS);
            String rp = defaultRetentionPolicy(this.influxDB.version());
            for (Device device : listRecord) {
                logger.info("----device.getTime(): " + device.getTimeImport());
                logger.info("----device.getTime new Date(): " + new Date(device.getTimeImport()));
                Point point = Point.measurement(measurement)
                        .tag("device", device.getDevice())
                        .addField("value", (float) device.getValue())
                        .time(device.getTimeImport(), TimeUnit.MILLISECONDS)
                        .build();
                this.influxDB.write(dbName, rp, point);
            }
            this.influxDB.disableBatch();
        } catch (Exception ex) {
            logger.error("ERROR processLstRecord: ", ex);
        } finally {
            logger.info("Time to write influxDB " + listRecord.size() + " record takes: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    public void addAll(List listRecord) {
        queue.addAll(listRecord);
    }

    public void addAll(Device device) {
        queue.add(device);
    }

    public void addAll(long time, String deviceName, String value) {
        Device device = new Device();
        device.setTimeImport(time);
        device.setDevice(deviceName);
        device.setValue(Float.parseFloat(value));
        queue.add(device);
    }
}
