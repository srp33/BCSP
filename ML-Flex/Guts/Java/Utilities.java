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

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/** This class contains general-purpose utility methods that are used in various places throughout the code. It also contains Singleton objects (those that are instantiated only once and stored as static variables).
 * @author Stephen
 */
public class Utilities
{
    // Singletons
    public static Log Log;
    public static Config Config;
    public static int Iteration;
    public static Random RandomNumberGenerator;
    public static Experiment Experiment;
    public static ProcessorVault ProcessorVault;
    public static InstanceVault InstanceVault;

    /** Indicates whether ML-Flex is executing its first (or only) iteration.
     *
     * @return Whether or not it is the first iteration
     * @throws Exception
     */
    public static boolean IsFirstIteration() throws Exception
    {
        return Iteration == 1;
    }

    /** Indicates whether ML-Flex is executing its final (or only) iteration.
     *
     * @return Whether or not it is the last iteration
     * @throws Exception
     */
    public static boolean IsLastIteration() throws Exception
    {
        return Iteration == Config.GetNumIterations();
    }

    /** Some external libraries do not work well with special characters, so this method changes those special characters temporarily to other characters.
     *
     * @param names List of names to be formatted
     * @return Formatted names
     */
    public static ArrayList<String> FormatNames(ArrayList<String> names)
    {
        ArrayList<String> formatted = new ArrayList<String>();

        for (String name : names)
            formatted.add(FormatName(name));

        return formatted;
    }

    /** Some external libraries do not work well with special characters, so this method changes those special characters temporarily to other characters.
     *
     * @param name Name to be formatted
     * @return Formatted name
     */
    public static String FormatName(String name)
    {
        return name.replace("/", "_forward_").replace(" ", "_space_").replace("*", "_star_").replace("-", "_hyphen_");
    }

    /** Some external libraries do not work well with special characters. After a name has been formatted, this method changes the characters back to the original characters.
     *
     * @param names List of names to be unformatted
     * @return Unformatted names
     */
    public static ArrayList<String> UnformatNames(ArrayList<String> names)
    {
        ArrayList<String> unformatted = new ArrayList<String>();

        for (String name : names)
            unformatted.add(UnformatName(name));

        return unformatted;
    }

    /** Some external libraries do not work well with special characters. After a name has been formatted, this method changes the characters back to the original characters.
     *
     * @param name Name to be unformatted
     * @return Unformatted name
     */
    public static String UnformatName(String name)
    {
        return name.replace("_forward_", "/").replace("_space_", " ").replace("_star_", "*").replace("_hyphen_", "-");
    }

    /** This method checks how long a lock file has been stored on the file system. If the file has been on the file system longer than the length of time specified in the configuration file, the file will be a candidate for deletion.
     *
     * @param lockFilePath Absolute path to lock file
     * @throws Exception
     */
    public static void CheckLockFileTimeout(String lockFilePath) throws Exception
    {
        if (Files.FileExists(lockFilePath) && Files.GetFileAgeMinutes(lockFilePath) > (double)Settings.THREAD_TIMEOUT_MINUTES)
            Files.DeleteFile(lockFilePath);
    }

    /** Saves a scalar result
     *
     * @param filePath File path where the result will be saved
     * @param key Specific description of the result
     * @param value Scalar result value
     * @return An object that summarizes the saved result
     * @throws Exception
     */
    public static ScalarValue SaveScalarValue(String filePath, String key, Object value) throws Exception
    {
        String existingScalarValue = GetScalarValue(filePath, key);
        String description = ParseDescriptionFromResultsFilePath(filePath);

        if (existingScalarValue == null) // TODO: Still need to check for existing?
        {
            Log.Info(description + " - " + key + ": " + value.toString());

            Files.CreateDirectoryIfNotExists(new File(filePath).getParent());
            Files.AppendTextToFile(filePath, key + "\t" + value.toString() + "\n");
        }

        return new ScalarValue(description, key, value);
    }

    /** Retrieves a given scalar result from a result file.
     *
     * @param filePath File path where the result was saved
     * @param key Specific description of the result
     * @return Scalar result value
     * @throws Exception
     */
    public static String GetScalarValue(String filePath, String key) throws Exception
    {
        if (Files.FileExists(filePath))
            for (ArrayList<String> row : Files.ParseDelimitedFile(filePath))
                if (row.get(0).equals(key))
                    return row.get(1);

        return null;
    }

    /** Retrieves all scalar results for the current experiment
     *
     * @return List of scalar results
     * @throws Exception
     */
    public static ArrayList<ScalarValue> GetAllScalarResultsValues() throws Exception
    {
        ArrayList<File> files = Files.GetFilesInDirectoryRecursively(Settings.GetExperimentOutputDir(false), "*_Results.txt");

        ArrayList<ScalarValue> allResults = new ArrayList<ScalarValue>();

        for (File file : files)
        {
            String description = ParseDescriptionFromResultsFilePath(file.getAbsolutePath());

            for (ArrayList<String> row : Files.ParseDelimitedFile(file.getAbsolutePath()))
                allResults.add(new ScalarValue(description, row.get(0), row.get(1)));
        }

        return allResults;
    }

    /** This formats a file name indicating where scalar results will be stored for a given experiment.
     *
     * @param descriptors Items that describe the scalar results
     * @return Formatted file name
     * @throws Exception
     */
    public static String GetScalarResultsFilePath(String ... descriptors) throws Exception
    {
        return Settings.GetExperimentOutputDir(true) + Lists.Join(descriptors, "_") + "_Results.txt";
    }

    /** This formats a file name indicating where statistics values will be stored for a given experiment.
     *
     * @param descriptors Items that describe the statistics
     * @return Formatted file name
     * @throws Exception
     */
    public static String GetStatisticsFilePath(String ... descriptors) throws Exception
    {
        return Settings.GetExperimentOutputDir(true) + Lists.Join(descriptors, "_") + "_Statistics.txt";
    }

    private static String ParseDescriptionFromResultsFilePath(String filePath) throws Exception
    {
        return new File(filePath).getName().replace("_Results.txt", "");
    }

    /** Indicates the IP address of the machine where this software is being run
     *
     * @return IP address
     * @throws Exception
     */
    public static String GetMachineAddress() throws Exception
    {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /** Generates a unique identifier randomly
     *
     * @return Random unique identifier
     */
    public static String GetUniqueID()
    {
        return "id." + UUID.randomUUID();
    }

    /** Builds a string description that is prefixed by the current experiment name. Objects that are passed to this method are appended to the description. Multiple values are separated by underscores
     *
     * @param descriptors Descriptors to be included in the description
     * @return Formatted description
     */
    public static String BuildDescription(Object... descriptors)
    {
        return Lists.Join(Lists.CreateStringList(descriptors), "_");
    }

    /** Indicates whether a string value is either null or equal to the missing character
     *
     * @param value Value to be tested
     * @return Whether or not it is considered missing
     */
    public static boolean IsMissing(String value)
    {
        return value == null || value.equals(Settings.MISSING_VALUE_STRING);
    }
}