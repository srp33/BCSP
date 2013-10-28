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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/** This class contains methods for logging information about ML-Flex execution. Values are output to the screen and to output files.
 * @author Stephen Piccolo
 */
public class Log
{
    public String LogFilePath;
    public String DebugFilePath;
    private boolean _debug;
    private String _action;
    public int NumExceptionsCaught;
    private String _machineAddress;

    /** Constructor
     *
     * @param logFilePath Absolute path to where the log file will be stored
     * @param debugFilePath Absolute path to where the debug file will be stored
     * @param debug Whether or not to save debugging information to the debug file
     * @param action Action to be performed
     * @throws Exception
     */
    public Log(String logFilePath, String debugFilePath, boolean debug, String action) throws Exception
    {
        LogFilePath = logFilePath;
        DebugFilePath = debugFilePath;
        _debug = debug;
        _action = action;

        Files.CheckFileDirectoryExists(logFilePath);

        if (debug)
            Files.CheckFileDirectoryExists(debugFilePath);

        NumExceptionsCaught = 0;
        _machineAddress = Utilities.GetMachineAddress();
    }

    /** Saves debug information
     *
     * @param text Debug text
     */
    public void Debug(Object text)
    {
        if (_debug)
            Print(FormatText(text), DebugFilePath, true);
    }

    /** Saves debug information
     *
     * @param list List of items to be logged
     */
    public void Debug(ArrayList list)
    {
        if (!_debug)
            return;

        if (list == null)
            Debug("<null ArrayList>");
        else
            Debug("\n" + Lists.Join(Lists.CreateStringList(list), "\n"));
    }

    /** Saves debugging information for a non-fatal error.
     *
     * @param ex Exception that occurred.
     */
    public void Debug(Throwable ex)
    {
        if (ex == null)
        {
            Info("<null Exception>");
            return;
        }

        Debug("A non-fatal error occurred. It will be logged but may not affect processing of this program.");
        Debug(GetStackTrace(ex));
    }

    /** Saves logging information
     *
     * @param text Logging text
     */
    public void Info(Object text)
    {
        Print(FormatText(text), LogFilePath, true);

        if (_debug)
            Print(FormatText(text), DebugFilePath, false);
    }

    /** Saves logging information
     *
     * @param list List of items to be logged
     */
    public void Info(ArrayList list)
    {
        if (list == null)
        {
            Info("<null ArrayList>");
            return;
        }

        Info("\n" + Lists.Join(Lists.CreateStringList(list), "\n"));
    }

    /** Saves logging information
     *
     * @param map Map of items to be logged
     */
    public void Info(HashMap map)
    {
        if (map == null)
        {
            Info("<null HashMap>");
            return;
        }

        for (Object key : map.keySet())
        {
            Object value = map.get(key);
            String output = key.toString() + "=" + value.toString();
            Info(output);
        }
    }

    /** Saves logging information for a non-fatal error.
     *
     * @param ex Exception that occurred.
     */
    public void Info(Throwable ex)
    {
        if (ex == null)
        {
            Info("<null Exception>");
            return;
        }

        Info("A non-fatal error occurred. It will be logged but may not affect processing of this program.");
        Debug(GetStackTrace(ex));
    }

    /** Saves exception information
     *
     * @param message Exception message
     */
    public void Exception(String message)
    {
        Exception(new Exception(message));
    }

    /** Saves exception information
     *
     * @param ex Exception object
     */
    public void Exception(Throwable ex)
    {
        Info(GetStackTrace(ex));

        NumExceptionsCaught++;

        if (NumExceptionsCaught > 25)
        {
            Info("More than " + NumExceptionsCaught + " nonfatal exceptions have occurred, so aborting!");
            System.exit(0);
        }
     }

    /** Saves exception information when the exception is severe enough that execution of the program should be halted.
     *
     * @param message Exception message
     */
    public void ExceptionFatal(String message)
    {
        ExceptionFatal(new Exception(message));
    }

    /** Saves exception information when the exception is severe enough that execution of the program should be halted.
     *
     * @param ex Exception object
     */
    public void ExceptionFatal(Throwable ex)
    {
        Exception(ex);
        System.exit(0);
     }

    /** Obtains stack-trace information when an exception has occurred.
     *
     * @param throwable Exception object
     * @return Stack-trace information
     */
    public String GetStackTrace(Throwable throwable)
    {
        if (throwable == null)
            return "<null exception>";

        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    private String FormatText(Object text)
    {
        String outText = text == null ? "<null>" : String.valueOf(text);

        String experiment = Utilities.Experiment == null ? "" : Utilities.Experiment.Description;

        if (Utilities.Iteration > 1)
            experiment += " (Iteration " + Utilities.Iteration + ")";

        return Dates.GetTimeStamp() + " | " + _machineAddress + " | " + experiment + " | " + _action + " | " + outText;
    }

    private static void Print(Object x, String filePath, boolean printToOut)
    {
        try
        {
            String out = x == null ? "<null>" : String.valueOf(x);

            if (out.equals(""))
                return;

            Files.AppendLineToFile(filePath, out);

            if (printToOut)
                System.out.println(out);
        }
        catch (Exception ex)
        {
            System.out.println("Could not print error.");
            ex.printStackTrace();
            System.exit(0);
        }
    }

//    public static String GetStackTrace()
//    {
//        StringBuilder trace = new StringBuilder();
//
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for (int i = 1; i < elements.length; i++)
//        {
//            StackTraceElement s = elements[i];
//            trace.append("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
//        }
//
//        return trace.toString();
//    }
}
