package com.winkar;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;
import java.util.Properties;


public class Automator {
    final static Logger log = Logger.getLogger(Automator.class.getName());
    final private static String apkPath = "/Users/WinKaR/Documents/lab/loginShell/apkCrawler/download/full";


    public static void main(String args[]) throws IOException {
//        BasicConfigurator.configure();
        DOMConfigurator.configure("config/log4j.xml");
        new MainTester(apkPath).startTest();
    }
}
