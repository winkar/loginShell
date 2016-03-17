package com.winkar;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by WinKaR on 16/3/14.
 */
public class AppiumAgent {
    private AndroidDriver driver;
    private int screenShotCounter;


    public AppiumAgent(String appPath) {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();

            capabilities.setCapability("deviceName", "Galaxy Note4");
            capabilities.setCapability("platformVersion", "4.4");
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("app", appPath);
            driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), capabilities);

        } catch (MalformedURLException e) {
        }
    }

    public String formatAndroidElement(AndroidElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append(element.getTagName())
                .append("\n")
                .append("\tId:" + element.getId())
                .append("\n")
                .append("\tText:" + element.getText())
                .append("\n\tClickable:" + element.getAttribute("clickable"))
                .append("\n");

        return sb.toString();
    }

    public void takeScreenShot(String logDir) {
        OutputStream os = null;
        try {
            byte[] screenShot = driver.getScreenshotAs(OutputType.BYTES);

            os = new FileOutputStream(
                    String.format("%s/%d.png", logDir, screenShotCounter)
            );
//            os = new FileOutputStream(logDir + File.separator + "screenshot" + Integer.toString(screenShotCounter) + ".png");
            screenShotCounter += 1;

            for (int i = 0; i < screenShot.length; i++) {
                os.write(screenShot[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public String currentActivity() {
        return driver.currentActivity();
    }

    public void pressKeyCode(int key) {
        driver.pressKeyCode(key);
    }

    public List findElements(By by) {
        return driver.findElements(by);
    }

    public void quit() {
        driver.quit();
    }

    public void installApp(String apkPath) {
        driver.installApp(apkPath);
    }

    public void removeApp(String bundleId) {
        driver.removeApp(bundleId);
    }

    public void startActivity(String appPackage, String activity) {
        driver.startActivity(appPackage, activity);
    }
}
