package com.winkar;

import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
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
    private int screenShotCounter;
    AppTraversal(String appPath) {
        this.appPath = appPath;
    }

    private void log(Object e) {
        System.out.println(e);
    }

    private String getLogDir() {
        return LOG_DIR;
    }

    private boolean createLogDir() {
        File file;
        try {
            LOG_DIR = "log" + File.separator + appPackage + File.separator + new Date().toString().replace(' ', '_');
            file = new File(LOG_DIR);
            return file.mkdirs();
        } finally {
            file = null;
        }
    }


    public void traversal(String currentActivity, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        log("\n\nat " + currentActivity);

        appiumAgent.takeScreenShot(getLogDir());

        List clickableElements = appiumAgent.findElements(By.xpath("//*[@clickable='true']"));
        for (Object obj :
                clickableElements) {
            AndroidElement element = ((AndroidElement) obj);
            if (!elementBlackList.contains(element.getText())) {
                element.click();
                if (!appiumAgent.currentActivity().equals(currentActivity)) {
                    log(appiumAgent.currentActivity() + "\t" + currentActivity);
                    traversal(appiumAgent.currentActivity(), depth + 1);
                }
            }
        }
        while (appiumAgent.currentActivity().equals(currentActivity)) {
            appiumAgent.pressKeyCode(AndroidKeyCode.BACK);
        }
    }


    public void start() throws IOException {
        appiumAgent = new AppiumAgent(appPath);
        try {
            appPackage = AndroidUtils.getPackageName(appPath);
//            appMainActivity = AndroidUtils.getMainActivity(appPath);

            if (!createLogDir()) {
                throw new IOException("Directory not created");
            }

//            appiumAgent.installApp(appPath);

            currentActivity = appiumAgent.currentActivity();

//            appiumAgent.startActivity(appPackage, appMainActivity);

            log("start traversal");
            traversal(currentActivity, 0);
        } catch (org.openqa.selenium.WebDriverException e) {
            e.printStackTrace();
        } finally {
            appiumAgent.removeApp(appPackage);
            appiumAgent.quit();
        }

    }
}
