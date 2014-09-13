/*
 * JPPF.
 * Copyright (C) 2005-2014 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jppf.management.diagnostics.spi;

import org.jppf.management.diagnostics.*;
import org.jppf.management.spi.JPPFNodeMBeanProvider;
import org.jppf.node.Node;
import org.jppf.utils.CloseableHandler;

/**
 * 
 * @author Laurent Cohen
 */
public class NodeDiagnosticsMBeanProvider extends AbstractDiagnosticsMBeanProvider implements JPPFNodeMBeanProvider
{
  @Override
  public Object createMBean(final Node node)
  {
    return new Diagnostics(CloseableHandler.NODE);
  }

  @Override
  public String getMBeanName()
  {
    return DiagnosticsMBean.MBEAN_NAME_NODE;
  }
}
