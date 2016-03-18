package com.winkar;

import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by WinKaR on 16/3/14.
 */
public class AppTraversal {
    private static int MAX_DEPTH;
    private static List<String> elementBlackList;
    final static Logger log = Logger.getLogger(Automator.class.getName());

    static {
        MAX_DEPTH = 4;
        elementBlackList = new ArrayList<String>(Arrays.asList("否"));

    }


    private String appPath;
    private String LOG_DIR;
    private String currentActivity;
    private String appPackage;
    private String appMainActivity;
    private AppiumAgent appiumAgent;

    AppTraversal(String appPath) {
        this.appPath = appPath;
    }

    private String getLogDir() {
        return LOG_DIR;
    }

    private boolean createLogDir() {
        File file;
        LOG_DIR = "log" + File.separator + appPackage + File.separator + new Date().toString().replace(' ', '_');
        file = new File(LOG_DIR);
        return file.mkdirs();
    }



    public void traversal(String currentActivity, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        log.info("Current at "+ currentActivity);
        log.info("Current traversal depth is "+ depth);

        appiumAgent.takeScreenShot(getLogDir());

        List clickableElements = appiumAgent.findElements(By.xpath("//*[@clickable='true']"));
        for (Object obj :
                clickableElements) {
            AndroidElement element = ((AndroidElement) obj);
            if (!elementBlackList.contains(element.getText())) {
                element.click();
                log.info("Clicked " + appiumAgent.formatAndroidElement(element));
                if (!appiumAgent.currentActivity().equals(currentActivity)) {
                    log.info("Jumped to activity " + appiumAgent.currentActivity());
                    traversal(appiumAgent.currentActivity(), depth + 1);
                }
            }
        }

        if (depth==0) {
            appiumAgent.closeApp();
            return ;
        }

        while (appiumAgent.currentActivity().equals(currentActivity)) {
            appiumAgent.pressKeyCode(AndroidKeyCode.BACK);
            log.info("Back pressed");
            log.info("Jump to activity "+ appiumAgent.currentActivity());
        }
    }


    public void start() throws IOException {
        appiumAgent = new AppiumAgent(appPath);
        try {
            appPackage = AndroidUtils.getPackageName(appPath);
            log.info("Get package Name: " + appPackage);
            if (!createLogDir()) {
                throw new IOException("Directory not created");
            }
            currentActivity = appiumAgent.currentActivity();
            log.info("Traversal started");
            traversal(currentActivity, 0);
        } catch (org.openqa.selenium.WebDriverException e) {
            e.printStackTrace();
        } finally {
            appiumAgent.removeApp(appPackage);
            appiumAgent.quit();
        }

    }
}
