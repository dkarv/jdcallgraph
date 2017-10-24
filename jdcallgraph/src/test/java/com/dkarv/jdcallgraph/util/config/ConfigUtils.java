package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;

import java.io.IOException;
import java.io.InputStream;

public class ConfigUtils {

  public static void replace(boolean addDefaults, String... options) throws IOException {
    if (addDefaults) {
      new ConfigReader(
          ConfigUtils.class.getResourceAsStream("/com/dkarv/jdcallgraph/defaults.ini"),
          write(options)).read();
    } else {
      new ConfigReader(
          write(options)).read();
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
