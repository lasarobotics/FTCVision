package com.arthurpachachura.jni;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Build {
    public static void main(String args[])
    {
        Logger log = Logger.getLogger("my.logger");
        log.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);

        String propertiesFile = args[0];
        final String ndkpath = getNDKLocation(propertiesFile, log, handler);

        if (ndkpath == null) { System.exit(1); return; }

        try {
            //let's try the Windows version first
            String ndkpath_win = ndkpath;
            String s = "\\:";
            Pattern p = Pattern.compile(s);
            Matcher m = p.matcher(ndkpath_win);
            m.replaceAll(":");
            StringBuffer tail = new StringBuffer();
            m.appendTail(tail);
            ndkpath_win = ndkpath.substring(0, 1) + ":" + tail.toString();

            if (!exec(ndkpath_win + "\\ndk-build.cmd", log, handler))
            {
                throw new Exception("Found Linux!  :D");
            }
        }
        catch (Exception e) {
            //good job, you're running Linux! (or another Unix....)
            if (exec(ndkpath + "/ndk-build", log, handler)) {
                System.exit(0);
                return;
            }
            //and now we have no idea what to do with you, so we're exiting with a negative report
            log.setLevel(Level.ALL);
            handler.setLevel(Level.ALL);
            log.fine("[NDK-BUILD] FAILURE: Could not find 'ndk-build'");
            System.exit(1);
        }
    }

    private static String getNDKLocation(String propertiesFile, Logger log, ConsoleHandler handler)
    {
        Scanner s=new Scanner(System.in);
        String search="ndk.dir";
        Scanner readFile;
        try {
            readFile = new Scanner(new File(propertiesFile));
        } catch (FileNotFoundException e) {
            log.setLevel(Level.ALL);
            handler.setLevel(Level.ALL);
            log.fine("[NDK-BUILD] FAILURE: Could not find local.properties!");
            log.fine(e.getMessage());
            return null;
        }
        while(readFile.hasNextLine())
        {
            String line = readFile.nextLine();
            if (line.contains(search)) {
                line = line.substring(line.indexOf("=") + 1).trim();
                return line;
            }
        }
        log.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
        log.fine("[NDK-BUILD] FAILURE: Could not find " + search + " in local.properties");
        return null;
    }

    private static boolean exec(String path, Logger log, ConsoleHandler handler)
    {
        try {
            Runtime rt = Runtime.getRuntime();
            String cmd = path + " " +
                    "NDK_PROJECT_PATH=build/intermediates/ndk " +
                    "NDK_LIBS_OUT=src/main/jniLibs " +
                    "APP_BUILD_SCRIPT=src/main/jni/Android.mk " +
                    "NDK_APPLICATION_MK=src/main/jni/Application.mk";
            Process pr = rt.exec(cmd);
            pr.waitFor();
            InputStreamReader r = new InputStreamReader(pr.getInputStream());
            String s = "";
            while(r.ready())
            {
                s = s + (char)r.read();
            }
            InputStreamReader e = new InputStreamReader(pr.getErrorStream());
            String k = "";
            while(e.ready())
            {
                k = k + (char)e.read();
            }
            log.fine("[NDK-BUILD] " + s);
            log.severe("[NDK-BUILD] " + k);
            log.fine("[NDK-BUILD] SUCCESS!");
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
