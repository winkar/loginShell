package com.winkar;


import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by WinKaR on 16/3/15.
 */
public class MultiAppTester {

    static Logger log = Logger.getLogger(Automator.class.getName());
    private static List<String> appBlackList;

    static {
        appBlackList = new ArrayList<String>(Arrays.asList(
                "aimoxiu.theme.mx49c81e403f35f52d4cdc6ad2020da3d8.apk",
                "aimoxiu.theme.mx62fbed7a2d8bc5f11a4a35ae0289a3b3.apk"
        ));
    }

    private String apkDirectoryRoot;

    public MultiAppTester(String apkDirectoryRoot) {
        this.apkDirectoryRoot = apkDirectoryRoot;
    }

    public void startTest() throws IOException {

        File apkRoot = new File(apkDirectoryRoot);

        for (String path : apkRoot.list()) {
            if (!appBlackList.contains(path)) {
                log.info(String.format("Testing apk %s", path));
                AppTraversal appTraversal = new AppTraversal(apkDirectoryRoot + File.separator + path);
                appTraversal.start();
                log.info(String.format("Stop testing apk %s", path));
            }

        }
    }
}
