package com.dkarv.jdcallgraph.helper;

import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
  public static File writeFile(final TemporaryFolder tmp, String... content) throws IOException {
    File configFile = tmp.newFile();
    PrintWriter out = new PrintWriter(configFile, "UTF-8");
    for (String line : content) {
      out.println(line);
    }
    out.close();
    return configFile;
  }

  public static String readFile(final TemporaryFolder tmp, String fileName) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(tmp.getRoot().getCanonicalPath(), fileName));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  /**
   * Clear/Empty a file.
   */
  public static void clear(TemporaryFolder tmp, String fileName) throws FileNotFoundException {
    (new PrintWriter(new File(tmp.getRoot(), fileName))).close();
  }

  public static InputStream writeInputStream(String content) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name()));
  }
}
