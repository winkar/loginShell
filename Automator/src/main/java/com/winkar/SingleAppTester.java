package com.winkar;

import org.apache.log4j.Logger;

/**
 * Created by WinKaR on 16/3/18.
 */
public class SingleAppTester {
    static Logger log = Logger.getLogger(Automator.class.getName());
    private final String apkPath;

    SingleAppTester(String apkPath) {
        this.apkPath = apkPath;
    }

    public void startTest() {

    }
}
