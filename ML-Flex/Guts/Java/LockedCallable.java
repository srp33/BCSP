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

import java.util.concurrent.Callable;

/** This class encapsulates objects necessary to run tasks in parallel across multiple compute nodes. It contains logic for saving/deleting lock files and for handling errors that may occur. It also contains logic to check whether a status file has been created, which would indicate that this task has already been completed.
 */
public class LockedCallable<V> implements Callable<Object>
{
    /** This is the path to the status file that indicates whether this task has been completed previously. */
    private String _statusFilePath;
    /** This is the path to the lock file. */
    private String _lockFilePath;
    /** This is what will be output to log files indicating status. */
    private String _logDescription;
    /** This is the callable that will actually be executed after locking has occurred. */
    private Callable<Object> _callable;

    /** Constructor that accepts the objects that are necessary to support the function of this class. Thisi particular constructor is intended to be used when the task being executed is a simple one that doesn't need a complex status file or lock file. The same value is used for the status file, lock file, and lock description.
     * @param simpleDescription Simple description of the task that will be executed
     * @param callable Object that will be executed when locking is successful
     */
    public LockedCallable(String simpleDescription, Callable<Object> callable)
    {
        this(simpleDescription, simpleDescription, simpleDescription, callable);
    }

    /** Constructor that accepts the objects that are necessary to support the function of this class.
     * @param statusRelativeFilePath Relative path to the status file that indicates whether this task has been completed previously
     * @param lockRelativeFilePath Relative path to the lock file that will be attempted to be created
     * @param logDescription Description fo the task being executed that will be output to the log files
     * @param callable Object that will be executed when locking is successful
     */
    public LockedCallable(String statusRelativeFilePath, String lockRelativeFilePath, String logDescription, Callable<Object> callable)
    {
        _statusFilePath = Settings.STATUS_DIR + statusRelativeFilePath;
        _lockFilePath = Settings.LOCKS_DIR + lockRelativeFilePath;
        _logDescription = logDescription;
        _callable = callable;
    }

    /** This method attempts to create a lock file that will indicate to other threads or compute nodes that the _callable task is being executed. If the file cannot be created (most likely because the task is already being executed by another thread/node, then nothing will happen.
     *
     * @return Result of callable
     * @throws Exception
     */
    public Object call() throws Exception
    {
        // Checking to see if a stale lock file exists
        Utilities.CheckLockFileTimeout(_lockFilePath);

        // Checking to see if this task has already been completed
        if (Files.FileExists(_statusFilePath))
            return Boolean.TRUE;

        Boolean result = Boolean.FALSE;

        // Attempting to create a lock file if it doesn't already exist
        if (!Files.FileExists(_lockFilePath) && Files.CreateEmptyFile(_lockFilePath))
        {
            try
            {
                // Checking a second time whether the task has already been completed, due to a possible race condition on the previous check
                if (!Files.FileExists(_statusFilePath))
                {
                    Utilities.Log.Debug("Attempt: " + _logDescription);

                    // Try to invoke the command
                    if (_callable.call().equals(Boolean.TRUE))
                    {
                        // Try to create a status file indicating the command was successful
                        if (Files.CreateEmptyFile(_statusFilePath))
                        {
                            Utilities.Log.Info("Success: " + _logDescription);
                            result = Boolean.TRUE;
                        }
                        else
                            Utilities.Log.Debug("Status file could not be saved at " + _statusFilePath + ". " + _logDescription);
                    }
                    else
                        // Some error occurred, so need to retry
                        Utilities.Log.Debug("Retry required: " + _logDescription);
                }

                Files.DeleteFile(_lockFilePath);
            }
            catch (Exception ex)
            {
                Files.DeleteFile(_lockFilePath);
                throw ex;
            }
        }

        return result;
    }
}
