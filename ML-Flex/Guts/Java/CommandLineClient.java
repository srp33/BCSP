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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** This class is used to execute commands at the command. It contains functionality to invoke commands and retrieve results using a text-file based approach.
 * @author Stephen Piccolo
 */
public class CommandLineClient
{
    public static final String OUTPUT_RESULTS_KEY = "output";

    /** Executes the specified command at the command line
     *
     * @param commandArgs These values specify the files that are part of the application that is being invoked
     * @param parameters These values specify parameters that are passed to the application that is being invoked
     * @return A map of keys and values that were returned by the command
     * @throws Exception
     */
    public static HashMap<String, String> RunAnalysis(ArrayList<String> commandArgs, ArrayList<String> parameters) throws Exception
    {
        return RunAnalysis(commandArgs, parameters, null);
    }

    /** Executes the specified command at the command line
     *
     * @param commandArgs These values specify the files that are part of the application that is being invoked
     * @param parameters These values specify parameters that are passed to the application that is being invoked
     * @param outputDirectoryPath Absolute directory path where the output files will be stored temporarily
     * @return A map of keys and values that were returned by the command
     * @throws Exception
     */
    public static HashMap<String, String> RunAnalysis(ArrayList<String> commandArgs, ArrayList<String> parameters, String outputDirectoryPath) throws Exception
    {
        Utilities.Log.Debug("Command args:");
        Utilities.Log.Debug(commandArgs);
        Utilities.Log.Debug("Parameters:");
        Utilities.Log.Debug(parameters);

        String[] strings = Lists.ConvertToStringArray(Lists.CreateStringList(commandArgs, parameters));

        ProcessBuilder processBuilder = new ProcessBuilder(strings);
        Process p = processBuilder.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        StringBuffer output = new StringBuffer();
        StringBuffer error = new StringBuffer();

        String s;
        while ((s = stdInput.readLine()) != null)
            output.append(s + "\n");

        while ((s = stdError.readLine()) != null)
            error.append(s + "\n");

        stdInput.close();
        stdError.close();
        p.destroy();

        java.io.File[] outputFiles = new java.io.File[0];

        if (outputDirectoryPath != null)
            outputFiles = Files.GetFilesInDirectory(outputDirectoryPath);

        if (output.length() > 0)
            Utilities.Log.Debug("Command output: " + output.toString());

        if (error.length() > 0)
        {
            Utilities.Log.Debug("Command error: " + error.toString());

            Utilities.Log.Debug("Parameters:");
            for (String parameter : parameters)
            {
                Utilities.Log.Debug(parameter);
                if (Files.FileExists(parameter))
                {
                    Utilities.Log.Debug("Parameter file text:");
                    Utilities.Log.Debug(Files.ReadTextFile(parameter));
                }
            }

            Utilities.Log.Debug("Output files:");
            for (java.io.File file : outputFiles)
            {
                Utilities.Log.Debug(file.getName());
                Utilities.Log.Debug(Files.ReadTextFile(file));
            }
        }

        HashMap<String, String> results = new HashMap<String, String>();
        for (java.io.File file : outputFiles)
            results.put(file.getName(), Files.ReadTextFile(file).trim());
        results.put(OUTPUT_RESULTS_KEY, output.toString());

        if (outputDirectoryPath != null)
            Files.RemoveDirectory(outputDirectoryPath);

        return results;
    }

    /** Convenience method for accessing an individual result of a command.
     *
     * @param results Command results
     * @param name Key of result to retrieve
     * @return Result
     * @throws Exception
     */
    public static String GetCommandResult(HashMap<String, String> results, String name) throws Exception
    {
        if (results.get(name) == null)
        {
            String error = "An error occurred in executing at the command line. The result named " + name + " could not be found.";

            if (results.size() > 0)
                error += "\nResults that could be found:\n";

            for (Map.Entry<String, String> entry : results.entrySet())
                error += entry.getKey() + ": " + entry.getValue() + "\n";
            
            throw new Exception(error);
        }

        return results.get(name).trim();
    }
}
