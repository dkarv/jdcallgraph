package com.dkarv.jdcallgraph.util;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
}
