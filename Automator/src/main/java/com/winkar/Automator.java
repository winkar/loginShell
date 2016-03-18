package com.winkar;


import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;


public class Automator {
    final static Logger log = Logger.getLogger(Automator.class.getName());
    final private static String apkPath = "/Users/WinKaR/Documents/lab/loginShell/apkCrawler/download/full";


    public static void main(String args[]) throws IOException {
        DOMConfigurator.configureAndWatch("config/log4j.xml");
        log.info("Automator started");
        new MultiAppTester(apkPath).startTest();
    }
}
