package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;
import com.dkarv.jdcallgraph.util.Target;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Arrays;

public class ConfigUtils {

  public static void replace(TemporaryFolder tmp, boolean addMissing, String... options) throws IOException {
    reset();
    if (addMissing) {
      options = Arrays.copyOf(options, options.length + 1);
      // add fake outDir
      options[options.length - 1] = "outDir: " + tmp.getRoot().getCanonicalPath();
    }

    new ConfigReader(TestUtils.writeFile(tmp, options)).read();
  }

  public static void reset() {
    Config.reset();
  }

  public static void inject(Config config) {
    Config.instance = config;
  }
}
