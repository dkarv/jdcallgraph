package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ConfigUtils {

  public static void replace(TemporaryFolder tmp, boolean addMissing, String... options) throws IOException {
    reset();
    new ConfigReader(write(tmp, addMissing, options)).read();
  }

  public static File write(TemporaryFolder tmp, boolean addMissing, String... options) throws IOException {
    if (addMissing) {
      options = Arrays.copyOf(options, options.length + 1);
      // add fake outDir
      options[options.length - 1] = "outDir: " + tmp.getRoot().getCanonicalPath();
    }
    return TestUtils.writeFile(tmp, options);
  }

  public static void reset() {
    Config.reset();
  }

  public static void inject(Config config) {
    Config.instance = config;
  }
}
