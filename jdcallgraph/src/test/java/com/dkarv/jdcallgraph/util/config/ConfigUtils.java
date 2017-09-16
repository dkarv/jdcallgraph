package com.dkarv.jdcallgraph.util.config;

import com.dkarv.jdcallgraph.helper.TestUtils;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Arrays;

public class ConfigUtils {

  public static void replace(TemporaryFolder tmp, boolean addMissing, String... options) throws IOException {
    Config.reset();
    if (addMissing) {
      options = Arrays.copyOf(options, options.length + 1);
      // add fake outDir
      options[options.length - 1] = "outDir: " + tmp.getRoot().getCanonicalPath();
    }

    new ConfigReader(TestUtils.writeFile(tmp, options)).read();
  }
}
