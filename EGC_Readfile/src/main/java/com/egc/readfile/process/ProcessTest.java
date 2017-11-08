/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.process;

import com.egc.readfile.common.Properties;
import com.egc.readfile.obj.Device;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;

/**
 *
 * @author ThuyetLV
 */
public class ProcessTest extends ProcessThreadMX {

    private static final Logger logger = Logger.getLogger(ProcessTest.class.getSimpleName());
    long sleepTime = 500;

    static ProcessTest _instance;

    public static synchronized ProcessTest getInstance() throws Exception {
        if (_instance == null) {
            _instance = new ProcessTest(ProcessTest.class.getSimpleName());
        }
        return _instance;
    }

    InfluxDB influxDB;
    String measurement;
    String dbName;
    List<String> lstDevice;

    ProcessTest(String threadName) throws Exception {
        super(threadName);
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

        getData();
    }

    private void getData() {
        lstDevice = new ArrayList<String>();
        Query query = new Query("SHOW TAG VALUES FROM logs WITH KEY = \"device\"", dbName);
        influxDB.query(query, 20, queryResult -> {
            List<QueryResult.Result> list = queryResult.getResults();
            if (list != null) {
                logger.info("----list: " + list.size());
                for (QueryResult.Result result : list) {
                    List<QueryResult.Series> listSeries = result.getSeries();
                    logger.info("----listSeries: " + listSeries.size());
                    for (QueryResult.Series seri : listSeries) {
                        List<List<Object>> lstObjects = seri.getValues();
                        for (List<Object> objects : lstObjects) {
                            logger.info("device: " + String.valueOf(objects.get(1)));
                            lstDevice.add(String.valueOf(objects.get(1)));
                        }
                    }
                }
            }
        });

//        QueryResult queryResult = influxDB.query(new Query("SHOW TAG VALUES FROM logs WITH KEY = \"device\"", dbName));
//        
//        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper(); // thread-safe - can be reused
//        List<Device> cpuList = resultMapper.toPOJO(queryResult, Device.class);
//        
//        for (Device device : cpuList) {
//            logger.info("------getData: " + device.getDevice());
//            lstDevice.add(device.getDevice());
//        }
    }

    @Override
    protected void process() {
        try {
            Thread.sleep(1000);
            test2();
        } catch (Exception ex) {
            logger.error("ERROR process: ", ex);
        }
    }

    private final static int SINGLE_POINT_COUNT = 10000;

    public static String defaultRetentionPolicy(String version) {
        if (version.startsWith("0.")) {
            return "default";
        } else {
            return "autogen";
        }
    }

    private void test2() {
        Random rand = new Random();
        this.influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS);
        String rp = defaultRetentionPolicy(this.influxDB.version());
        for (String device : lstDevice) {
            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("device", device)
                    .addField("value", (float) rand.nextInt(100) + 1)
                    .build();
            this.influxDB.write(dbName, rp, point);
        }
        this.influxDB.disableBatch();
    }

    private void test() {
        this.influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS);
        String rp = defaultRetentionPolicy(this.influxDB.version());
        for (int j = 0; j < SINGLE_POINT_COUNT; j++) {
            Point point = Point.measurement(dbName)
                    .addField("idle", (double) j)
                    .addField("user", 2.0 * j)
                    .addField("system", 3.0 * j).build();
            this.influxDB.write(dbName, rp, point);
        }
        this.influxDB.disableBatch();
    }
}
