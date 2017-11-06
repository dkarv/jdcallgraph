package com.dkarv.jdcallgraph.util;

public class OsUtils {
  public static boolean isWindows = System.getProperty("os.name").startsWith("Windows");

  public static String escapeFilename(String fileName) {
    if (isWindows) {
      return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
    return fileName;
  }
}
