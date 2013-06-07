// Copyright 2011 Stephen Piccolo
// 
// This file is part of ML-Flex.
// 
// ML-Flex is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
// 
// ML-Flex is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with ML-Flex. If not, see <http://www.gnu.org/licenses/>.

package mlflex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.*;

/** This class encapsulates logic for executing computational tasks across one or more threads.
 */
public class MultiThreadedTaskHandler
{
    private LinkedList<Callable<Object>> _callables;
    private int _numThreads;

    /** This default constructor initializes the class. It uses the number of threads that has been specified at the command line.
    * @throws Exception
    */
    public MultiThreadedTaskHandler()
    {
        this(Settings.NUM_THREADS);
    }

    /** This constructor accepts the arguments to the class that are necessary to execute tasks in parallel and initializes private variables.
    * @param numThreads Number of threads on which to execute the callable objects
    * @throws Exception
    */
    public MultiThreadedTaskHandler(int numThreads)
    {
        Clear();
        _numThreads = numThreads;
    }

    /** This method can be used to add a task that needs to be executed.
     * @param callable Callable object to be executed
     * @return The current instance of this object for convenience
     */
    public MultiThreadedTaskHandler Add(Callable<Object> callable)
    {
        _callables.add(callable);
        return this;
    }

    /** This method can be used to add a task that needs to be executed.
     * @param callables List of callable objects to be executed
     * @return The current instance of this object for convenience
     */
    public MultiThreadedTaskHandler Add(ArrayList<Callable<Object>> callables)
    {
        _callables.addAll(callables);
        return this;
    }

    /** Executes multiple callable objects in a multithreaded fashion
     *
     * @return Objects that are returned from each _callable object
     * @throws Exception
     */
    public ArrayList Execute() throws Exception
    {
        if (_callables == null || _callables.size() == 0)
            return new ArrayList();

        Utilities.Log.Debug("Attempting to share execution across " + _numThreads + " threads.");

        ArrayList results = new ArrayList();

        // If only one thread is desired, then there is no need to involve the more complicated thread pool logic
        if (_numThreads == 1)
        {
            for (Callable callable : _callables)
            {
                Object result = callable.call();

                if (result != null)
                    results.add(result);
            }

            return results;
        }

        // The logic below enables multi-threaded execution
        ExecutorService service = Executors.newFixedThreadPool(_numThreads);

        ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();

        for (Callable<Object> callable : _callables)
            futures.add(service.submit(callable));

        for (Future<Object> future : futures)
        {
            Object result = future.get(Settings.THREAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (result != null)
                results.add(result);
        }

        // Very important to shut down the service
        service.shutdown();

        return results;
    }

    // Execute all callable objects and pause if any of them has not completed
    public void ExecuteWithRetries(String description) throws Exception
    {
        ArrayList results = Lists.CreateBooleanList(false);

        do
        {
            try
            {
                // Try to execute the tasks
                results = Execute();

                // If any of the tasks have not yet been executed, pause
                if (Lists.AnyFalse(results))
                    Pause(description);
            }
            catch (Exception ex)
            {
                // Pause after an exception has occurred
                Utilities.Log.Exception(ex);
                Pause(description);
            }
        }
        while (Lists.AnyFalse(results));
    }

    private void Pause(String description) throws Exception
    {
        Utilities.Log.Debug("Pausing for " + Settings.PAUSE_SECONDS + " seconds: " + description + ". Other threads may be processing these tasks.");
        Thread.currentThread().sleep(Settings.PAUSE_SECONDS * 1000);
        Utilities.Log.Debug("Retrying after pause: " + description + ".");
    }

    public void Clear()
    {
        _callables = new LinkedList<Callable<Object>>();
    }
}
