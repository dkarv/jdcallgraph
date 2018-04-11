package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;

import java.io.IOException;
import java.io.InputStream;

public class ConfigUtils {

  public static void replace(boolean addDefaults, String... options) throws IOException {
    if (addDefaults) {
      ConfigReader.read(
          ConfigUtils.class.getResourceAsStream("/com/dkarv/jdcallgraph/defaults.ini"),
          write(options));
    } else {
      ConfigReader.read(
          write(options));
    }
  }

  public static InputStream write(String... options) throws IOException {
    StringBuilder str = new StringBuilder();
    for (String opt : options) {
      str.append(opt);
      str.append('\n');
    }
    return TestUtils.writeInputStream(str.toString());
  }

  public static void inject(Config config) {
    Config.instance = config;
  }
}
