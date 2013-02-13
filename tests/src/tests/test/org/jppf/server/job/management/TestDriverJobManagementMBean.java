/*
 * JPPF.
 * Copyright (C) 2005-2012 JPPF Team.
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

package test.org.jppf.server.job.management;

import static org.junit.Assert.*;
import static org.jppf.utils.ReflectionUtils.getCurrentMethodName;

import java.util.List;

import org.jppf.client.*;
import org.jppf.server.job.management.DriverJobManagementMBean;
import org.jppf.server.protocol.JPPFTask;
import org.junit.Test;

import test.org.jppf.test.setup.*;
import test.org.jppf.test.setup.common.*;

/**
 * Unit tests for {@link JPPFTask}.
 * In this class, we test that the functionality of the DriverJobManagementMBean from the client point of view.
 * @author Laurent Cohen
 */
public class TestDriverJobManagementMBean extends Setup1D1N1C
{
  /**
   * A "short" duration for this test.
   */
  private static final long TIME_SHORT = 1000L;
  /**
   * A "long" duration for this test.
   */
  private static final long TIME_LONG = 3000L;

  /**
   * We test a job with 1 task, and attempt to cancel it after it has completed.
   * @throws Exception if any error occurs.
   */
  @Test(timeout=15000L)
  public void testCancelJob() throws Exception
  {
    int nbTasks = 10;
    JPPFJob job = BaseTestHelper.createJob(getCurrentMethodName(), false, false, nbTasks, LifeCycleTask.class, 5000L);
    JPPFResultCollector collector = (JPPFResultCollector) job.getResultListener();
    client.submit(job);
    Thread.sleep(TIME_SHORT);
    DriverJobManagementMBean proxy = BaseSetup.getJobManagementProxy(client);
    assertNotNull(proxy);
    proxy.cancelJob(job.getUuid());
    List<JPPFTask> results = collector.waitForResults();
    assertEquals(results.size(), nbTasks);
    assertNotNull(results.get(0));
    int count = 0;
    for (JPPFTask t: results)
    {
      if (t.getResult() == null) count++;
    }
    assertTrue(count > 0);
  }

  /**
   * We test a job with 1 task, and attempt to cancel it after it has completed.
   * @throws Exception if any error occurs.
   */
  @Test(timeout=10000L)
  public void testCancelJobAfterCompletion() throws Exception
  {
    JPPFJob job = BaseTestHelper.createJob(getCurrentMethodName(), true, false, 1, LifeCycleTask.class, TIME_SHORT);
    List<JPPFTask> results = client.submit(job);
    assertEquals(results.size(), 1);
    assertNotNull(results.get(0));
    assertNotNull(results.get(0).getResult());
    DriverJobManagementMBean proxy = BaseSetup.getJobManagementProxy(client);
    assertNotNull(proxy);
    proxy.cancelJob(job.getUuid());
  }

  /**
   * We test that no job remains in the server's queue after resuming, then cancelling an initially suspended job.
   * See <a href="http://www.jppf.org/tracker/tbg/jppf/issues/JPPF-126">JPPF-126 Job cancelled from the admin console may get stuck in the server queue</a>.
   * @throws Exception if any error occurs.
   */
  @Test(timeout=10000L)
  public void testResumeAndCancelSuspendedJob() throws Exception
  {
    int nbTasks = 2;
    DriverJobManagementMBean proxy = BaseSetup.getJobManagementProxy(client);
    assertNotNull(proxy);
    JPPFJob job = BaseTestHelper.createJob(getCurrentMethodName(), false, false, nbTasks, LifeCycleTask.class, 5000L);
    job.getSLA().setSuspended(true);
    client.submit(job);
    Thread.sleep(1500L);
    proxy.resumeJob(job.getUuid());
    Thread.sleep(500L);
    proxy.cancelJob(job.getUuid());
    JPPFResultCollector collector = (JPPFResultCollector) job.getResultListener();
    List<JPPFTask> results = collector.waitForResults();
    assertEquals(results.size(), nbTasks);
    Thread.sleep(1000L);
    String[] ids = proxy.getAllJobIds();
    assertNotNull(ids);
    assertEquals("the driver's job queue should be empty", 0, ids.length);
  }
}
