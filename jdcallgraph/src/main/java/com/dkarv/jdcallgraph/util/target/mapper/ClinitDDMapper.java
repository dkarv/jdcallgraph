/*
 * MIT License
 * <p>
 * Copyright (c) 2017 David Krebs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dkarv.jdcallgraph.util.target.mapper;

/**
 * Duplicate dd calls.
 */
public class ClinitDDMapper {

  /**
   * Clinit methods are only called once. This is an issue because we don't add transitive data dependence nodes. Assume this scenario:
   * static1 {
   * x = 1;
   * }
   * static2 {
   * y = x * 2;
   * }
   * <p>
   * Only static2 is listed as dependency. The target is to also have static1 in there. That means:
   * - For all reads in static1 add an edge to clinitDD.
   * - For all reads to a variable that was written in static1, check if static1 depends on other staticX
   * <p>
   * TODO make configurable
   */
  //private static final boolean transitiveDDClinit = true;

  //private static final Map<StackItem, Set<StackItem>> clinitDD = new HashMap<>();


   /*
        // TODO move this to a processor
        if (transitiveDDClinit && lastWrite.isClinit()) {
          if (location.isClinit()) {
            Set<StackItem> set = clinitDD.get(location);
            if (set == null) {
              set = new HashSet<>();
              clinitDD.put(location, set);
            }
            set.add(lastWrite);
          }

          Set<StackItem> dependencies = clinitDD.get(lastWrite);
          if (dependencies != null) {
            for (StackItem dependency : dependencies) {
              for (GraphWriter writer : writers) {
                writer.edge(dependency, lastWrite, "clinit dependency");
              }
            }
          }
        }*/
}
