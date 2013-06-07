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
import java.util.HashMap;
import java.util.Random;

/** This is the class that gets invoked when ML-Flex begins to execute.
 * @author Stephen Piccolo
 */
public class Main
{
    /** This method is the first one invoked when ML-Flex is run from the command line.
     *
     * @param args Array of arguments that are passed to this application from the Java runtime
     */
    public static void main(String[] args)
    {
        InitializeLogging(args);

        try
        {
            ParseSettings(args);

            ArrayList<String> experiments = Lists.CreateStringList(GetArgValue(args, "EXPERIMENTS", null).split(","));
            experiments = Lists.Replace(experiments, ".txt", "");
            ProcessExperiments(experiments, GetActions(args));

            //Test();

            Utilities.Log.Info("Successfully completed!");
            System.exit(0);
        }
        catch (Exception ex)
        {
            Utilities.Log.Exception(ex);
            System.exit(0);
        }
    }

    private static void InitializeLogging(String[] args)
    {
        try
        {
            String logFilePath = GetArgValue(args, "LOG_FILE", "Log.txt");
            String debugFilePath = GetArgValue(args, "DEBUG_FILE", "Debug.txt");
            boolean debug = Boolean.parseBoolean(GetArgValue(args, "DEBUG", "false"));

            Utilities.Log = new Log(logFilePath, debugFilePath, debug, GetArgValue(args, "ACTION", null));
        }
        catch (Exception ex)
        {
            System.out.println("ML-Flex logging could not be configured.");

            System.out.println("Args:");
            for (String arg : args)
                System.out.println(arg);
            ex.printStackTrace();

            System.exit(0);
        }
    }

    private static void ParseSettings(String[] args) throws Exception
    {
        Settings.MAIN_DIR = System.getProperty("user.dir") + "/";
        Settings.CONFIG_DIR = Files.CreateDirectoryIfNotExists(Settings.MAIN_DIR + "Config/");
        Settings.EXPERIMENTS_DIR = Files.CreateDirectoryIfNotExists(Settings.MAIN_DIR + "Experiments/");
        Settings.GUTS_DIR = Files.CreateDirectoryIfNotExists(Settings.MAIN_DIR + "Guts/");
        Settings.RAW_DATA_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "RawData/");
        Settings.DATA_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "Data/");

        String numAvailableProcessors = String.valueOf(Runtime.getRuntime().availableProcessors());
        Settings.NUM_THREADS = Integer.parseInt(GetArgValue(args, "NUM_THREADS", numAvailableProcessors));
        Settings.THREAD_TIMEOUT_MINUTES = Long.parseLong(GetArgValue(args, "THREAD_TIMEOUT_MINUTES", "60"));
        Settings.PAUSE_SECONDS = Long.parseLong(GetArgValue(args, "PAUSE_SECONDS", "60"));

        String classificationAlgorithmsFilePath = GetArgValue(args, "CLASSIFICATION_ALGORITHMS_FILE", Settings.CONFIG_DIR + "ClassificationAlgorithmParameters.txt");
        String featureSelectionAlgorithmsFilePath = GetArgValue(args, "FEATURE_SELECTION_ALGORITHMS_FILE", Settings.CONFIG_DIR + "FeatureSelectionAlgorithmParameters.txt");
        Settings.ParseAlgorithms(classificationAlgorithmsFilePath, featureSelectionAlgorithmsFilePath);
    }

    private static void Test() throws Exception
    {
        Tests.Test();

        String testFilePath = "/Users/stevep/Temp/testabcdefg.txt";
        if (Files.FileExists(testFilePath))
            Files.DeleteFile(testFilePath);
        Settings.THREAD_TIMEOUT_MINUTES = 1;
        Files.CreateEmptyFile("/Users/stevep/Temp/testabcdefg.txt");
        Thread.currentThread().sleep(30000);
        System.out.println(Files.FileExists(testFilePath));
        Thread.currentThread().sleep(29000);
        System.out.println(Files.FileExists(testFilePath));
        Thread.currentThread().sleep(2000);
        Utilities.CheckLockFileTimeout(testFilePath);
        System.out.println(Files.FileExists(testFilePath));
    }

    private static void ProcessExperiments(ArrayList<String> experiments, ArrayList<Action> actions) throws Exception
    {
        for (String experiment : experiments)
        {
            Utilities.Log.Info("Beginning experiment " + experiment);

            // Initialize configuration settings specific to this experiment
            Utilities.Config = new Config(Settings.EXPERIMENTS_DIR + experiment + ".txt");

            // Initialize singleton objects
            Utilities.Experiment = new Experiment(experiment);
            Utilities.InstanceVault = new InstanceVault();
            Utilities.ProcessorVault = new ProcessorVault();
            Utilities.ProcessorVault.Load();

            // Initialize (and create if needed) the experiment-specific directories
            Settings.FEATURE_SELECTION_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "SelectedFeatures/" + Utilities.Experiment.toString() + "/");
            Settings.PREDICTIONS_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "Predictions/" + Utilities.Experiment.toString() + "/");
            Settings.ENSEMBLE_PREDICTIONS_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "EnsemblePredictions/" + Utilities.Experiment.toString() + "/");
            Settings.OUTPUT_DIR = Files.CreateDirectoryIfNotExists(Settings.MAIN_DIR + "Output/" + Utilities.Experiment.toString() + "/");
            Settings.LOCKS_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "Locks/" + Utilities.Experiment.toString() + "/");
            Settings.STATUS_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "Status/" + Utilities.Experiment.toString() + "/");
            Settings.TEMP_DATA_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "TempData/" + Utilities.Experiment.toString() + "/");
            Settings.TEMP_RESULTS_DIR = Files.CreateDirectoryIfNotExists(Settings.GUTS_DIR + "TempResults/" + Utilities.Experiment.toString() + "/");

            for (int i=1; i<(Utilities.Config.GetNumIterations() +1); i++)
            {
                Utilities.Iteration = i;
                Utilities.RandomNumberGenerator = Utilities.Config.GetRandomSeed() == 0 ? new Random() : new Random(Utilities.Config.GetRandomSeed());
                Utilities.Log.NumExceptionsCaught = 0;
                Utilities.Experiment.Orchestrate(actions);
            }
        }
    }

    private static ArrayList<Action> GetActions(String[] args) throws Exception
    {
        String actionText = GetArgValue(args, "ACTION", null);
        ArrayList<Action> actions = new ArrayList<Action>();

        for (String action : Lists.CreateStringList(actionText.split(","))) // This is an undocumented feature that allows you to specify multiple actions. It's undocumented because this is discouraged if you are running ML-Flex across multiple nodes.
        {
            try
            {
                actions.add(Action.valueOf(action));
            }
            catch (Exception ex)
            {
                throw new Exception("Invalid action specified: " + action);
            }
        }

        if (actions.size() == 0)
            throw new Exception("No valid action value has been specified.");

        return actions;
    }

    private static String GetArgValue(String[] args, String name, String defaultValue) throws Exception
    {
        HashMap<String, String> keyValueMap = new HashMap<String, String>();

        for (String arg : args)
            if (arg.contains("="))
            {
                String[] parts = arg.split("=");
                if (parts.length == 2 && parts[0].length() > 0 && parts[1].length() > 0)
                    keyValueMap.put(parts[0], parts[1]);
            }

        if (keyValueMap.containsKey(name))
            return keyValueMap.get(name);
        else
        {
            if (defaultValue == null)
                throw new Exception("A value for " + name + " must be set at the command line.");
            else
                return defaultValue;
        }
    }
}