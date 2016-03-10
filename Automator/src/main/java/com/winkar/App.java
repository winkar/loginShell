package com.winkar;



import com.thoughtworks.selenium.webdriven.commands.WaitForPageToLoad;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.apache.xalan.templates.FuncKey;
import org.apache.xalan.xsltc.compiler.util.InternalError;
import org.apache.xpath.operations.And;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import io.appium.java_client.android.AndroidElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import sun.jvm.hotspot.debugger.cdbg.Sym;
import sun.jvm.hotspot.runtime.Bytes;

public class App 
{
    final private static String APP_PATH = "/Users/WinKaR/Documents/lab/loginShell/apk/weixin.apk";
    final private static int MAX_DEPTH = 4;

    private static int windowWidth;
    private static int windowHeight;
    private static List<String> blackList;
    private static String LOG_DIR;
    private static String currentActivity;

    public static AndroidDriver driver;

    private static int screenShotCounter;

    private static String androidElementFormat(AndroidElement element) {
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

    private static void log(Object e){
        System.out.println(e);
    }

    public static void takeScreenShot() {
        OutputStream os = null;
        try {
            byte[] screenShot = driver.getScreenshotAs(OutputType.BYTES);

            os = new FileOutputStream(LOG_DIR + File.separator + "screenshot" + Integer.toString(screenShotCounter) + ".png");
            screenShotCounter += 1;

            for (int i = 0; i < screenShot.length; i++) {
                os.write(screenShot[i]);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException e) {

                }
            }
        }

    }

    private static boolean createLogDir() {
        File file;
        try {
            LOG_DIR = new Date().toString().replace(' ', '_');
            file = new File(LOG_DIR);
            return file.mkdir();
        }
        finally {
            file = null;
        }
    }


    public static void traversal(String currentActivity, int depth)  {
        if (depth > MAX_DEPTH) {
            return;
        }
        log("\n\nat "+ currentActivity);
//        driver.wait(1);
//        WaitForPageToLoad wait = new WaitForPageToLoad();

        takeScreenShot();
        List clickableElements = driver.findElements(By.xpath("//*[@clickable='true']"));
        for (Object obj:
             clickableElements) {
            AndroidElement element = ((AndroidElement) obj);
            if (!blackList.contains(element.getText())) {
                element.click();
                if (!driver.currentActivity().equals(currentActivity)) {
                    log(driver.currentActivity() + "\t" + currentActivity);
                    traversal(driver.currentActivity(), depth+1);
                }
            }
        }
        while (driver.currentActivity().equals(currentActivity)) {
            driver.pressKeyCode(AndroidKeyCode.BACK);
        }
    }

    public static void initBlackList(){
        blackList = new ArrayList<String>(Arrays.asList("否"));
    }

    public static void main( String[] args ) throws MalformedURLException
    {
        try {
            createLogDir();
            initBlackList();

            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("deviceName", "Android Emulator");
            capabilities.setCapability("platformVersion", "6.0");
            capabilities.setCapability("platformName", "Android");
            capabilities.setCapability("app", APP_PATH);

            driver = new AndroidDriver(new URL("http://localhost:4723/wd/hub"), capabilities);

            Dimension dimension = driver.manage().window().getSize();
            windowWidth = dimension.getWidth();
            windowHeight = dimension.getHeight();

            currentActivity = driver.currentActivity();
            log("start traversal");
            traversal(currentActivity, 0);
        }
        catch (Exception e) {

        }
        finally {
            if (driver!=null) {
                driver.quit();
            }
        }
    }
}
