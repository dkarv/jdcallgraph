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
package com.dkarv.jdcallgraph.instr.bytebuddy;

import com.dkarv.jdcallgraph.util.log.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class TracerLogger implements AgentBuilder.Listener {
  private static final Logger LOG = new Logger(TracerLogger.class);

  @Override
  public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    // LOG.debug("onDiscovery: {}, {}, {}", typeName, module, loaded);
  }

  @Override
  public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
    // LOG.debug("onTransformation: {}, {}, {}, {}", typeDescription, module, loaded, dynamicType);
  }

  @Override
  public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
    // LOG.debug("onIgnored: {}, {}, {}", typeDescription, module, loaded);
  }

  @Override
  public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
    LOG.error("onError: {}, {}, {}", typeName, module, loaded, throwable);
  }

  @Override
  public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    // LOG.debug("onComplete: {}, {}, {}", typeName, module, loaded);
  }
}
