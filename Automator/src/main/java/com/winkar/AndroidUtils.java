package com.winkar;


import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by WinKaR on 16/3/15.
 */
public class AndroidUtils {

    private static String checkOutput(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd);


            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String output = "";
            String linestr;
            while ((linestr = reader.readLine()) != null) {
                output += linestr;
            }


            if (process.waitFor() != 0) {
                if (process.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束
                    System.err.println("execute Failed!");
            }


            in.close();
            reader.close();
            return output;

        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPackageName(String apkPath) {

        String filename = Iterables.getLast(Arrays.asList(apkPath.split("/")));

        String[] splited_filename = filename.split("\\.");

        return StringUtils.join(
                Arrays.copyOfRange(splited_filename, 0, splited_filename.length - 1),
                '.');

//        return checkOutput("aapt dump badging " + apkPath +
//                " | grep  package | awk '{print $2}' | sed s/name=//g | sed s/\\'//g");
    }


    public static String getMainActivity(String apkPath) {
        return checkOutput("aapt dump badging " + apkPath +
                "  |awk -F\" \" '/launchable-activity/ {print $2}'" +
                "  |awk -F\"'\" '/name=/ {print $2}'");
    }


}
