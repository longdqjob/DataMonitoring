/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.egc.readfile.main;

import com.egc.readfile.process.ProcessImportIfl;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author thuyetlv
 */
public class CSVUtils {

    protected static final Logger logger = Logger.getLogger(CSVUtils.class);

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    private static final String fileName = "D:\\tmp\\EGC\\template\\HD_AXIAL_KEY_20160215.CSV";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("../etc/log4j.conf"));
        PropertyConfigurator.configure(props);

        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyyh:mm:ss");
        String input = "2/15/20160:01:00";
        System.out.println("input: " + input);
        System.out.println("sdf: " + sdf.parse(input));
//        readUseReader(fileName);
        readUseCSVReader(fileName);
    }

    public static void readUseReader(String csvFile) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        int numRecord = 0;
        Scanner scanner = new Scanner(new File(fileName));
        List<String> line;
        while (scanner.hasNext()) {
            line = parseLine(scanner.nextLine());
            processRecord(line);
//            System.out.println("Country [id= " + line.get(0) + ", code= " + line.get(1) + " , name=" + line.get(2) + "]");
            numRecord++;
        }
        scanner.close();

        System.out.println("Time to read " + numRecord + " record: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void processRecord(List<String> line) {
        System.out.println("---: " + line.size());
    }

    public static void processRecord(String[] line) {
        System.out.println("---: " + line.length);
    }

    //Bester
    public static void readUseCSVReader(String csvFile) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyh:mm:ss");
        long startTime = System.currentTimeMillis();
        int numRecord = 0;
        CSVReader reader = null;
        try {
            String date = "";
            String time = "";
            List<String> lstDevice = new ArrayList<>();
            int idx = 0;
            int skipCol = 2;
            long timeData = 0;
            Date dDate = new Date();

            reader = new CSVReader(new FileReader(csvFile));
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < skipCol) {
                    logger.warn("ERROR DATA FROM file: " + csvFile + " idx: " + numRecord + " data: " + StringUtils.join(line, ","));
                    continue;
                }
                idx = 0;
                if (numRecord == 0) {
                    //Doc header
                    for (String device : line) {
                        idx++;
                        if (idx > skipCol) {
                            //Doc du lieu tbi
                            lstDevice.add(device);
                        }
                    }
                } else {
                    //Data
                    date = line[0];
                    time = line[1];
                    dDate = sdf.parse(date + time);

                    logger.info("---timeData: " + (date + time));
                    logger.info("---dDate: " + dDate);

                    timeData = dDate.getTime();

                    for (String value : line) {
                        idx++;
                        if (idx > skipCol) {
                            if (!StringUtils.isBlank(value)) {
                                try {
                                    ProcessImportIfl.getInstance().addAll(timeData, lstDevice.get(idx - skipCol - 1), value);
                                } catch (Exception ex) {
                                    logger.error("ERROR ProcessImportIfl getInstance: ", ex);
                                }
                            }
                        }
                    }
                }
                numRecord++;
            }
        } catch (IOException e) {
            logger.error("ERROR readUseCSVReader: ", e);
        } finally {
            if (numRecord > 1) {
                deleteFile(csvFile);
            }
            logger.info("Time to readUseCSVReader " + numRecord + " record: " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    public static void deleteFile(String filePath) {
        try {

            File file = new File(filePath);

            if (file.delete()) {
                logger.debug(filePath + " is deleted!");
            } else {
                logger.warn(filePath + " Delete operation is failed.");
            }
        } catch (Exception ex) {
            logger.error("ERROR deleteFile " + filePath + " ", ex);
        }
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }
}
