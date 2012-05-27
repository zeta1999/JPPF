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

package org.jppf.example.fj;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;

import java.util.concurrent.ForkJoinTask;

/**
 * Sample class for fork join support demonstration.
 * @author Martin JANDA
 */
public class FibonacciFJ
{
  /**
   * Number of tasks to execute.
   */
  public static final int COUNT = 10;
  /**
   *
   */
  public static final int N     = 5;

  /**
   * Main method for demonstration of fork join support
   * @param args
   */
  public static void main(final String[] args)
  {
    JPPFClient client = null;
    try
    {
      client = new JPPFClient();
      JPPFJob job = new JPPFJob();
      job.setBlocking(true);

      System.out.printf("Creating %d tasks: fib(%d)%n", COUNT, N);
      for(int index = 0; index < COUNT; index++)
      {
        job.addTask(new JPPFTaskFibonacci(N));
      }

      System.out.println("Submitting job...");
      long dur = System.nanoTime();
      client.submit(job);
      dur = System.nanoTime() - dur;
      System.out.printf("Job done in %sms%n", dur / 1000000.0);

      for (JPPFTask task : job.getResults().getAll())
      {
        if(task.getResult() instanceof FibonacciResult)
        {
          FibonacciResult result = (FibonacciResult) task.getResult();

          System.out.printf("  %2d. ForkJoin: %s, Result: %d%n", task.getPosition(), result.isForkJoinUsed(), result.getResult());
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if(client != null) client.close();
    }
  }

  /**
   * Implementation of JPPF Fibonacci task.
   */
  public static class JPPFTaskFibonacci extends JPPFTask
  {
    /**
     * Explicit serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Order of Fibonacci number to compute.
     */
    private final int n;

    /**
     * Initializes Fibonacci task with given order.
     * @param n order of Fibonacci number to compute. Must be greater or equal to zero.
     */
    public JPPFTaskFibonacci(final int n)
    {
      this.n = n;
    }

    @Override
    public void run()
    {
      FibonacciResult result;
      if(ForkJoinTask.inForkJoinPool())
      {
        result = new FibonacciResult(true, new FibonacciTaskFJ(n).compute());
      }
      else
      {
        result = new FibonacciResult(false, fib(n));
      }
      setResult(result);
    }

    /**
     * Compute Fibonacci number.
     * @param n
     * @return the Fibonacci number.
     */
    private static long fib(final int n)
    {
      if(n <= 1) return n;

      return fib(n - 1) + fib(n - 2);
    }
  }
}
