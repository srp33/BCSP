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

/** This class stores application wide values. These values are typically machine-specific and are set via command-line parameters.
 * @author Stephen Piccolo
 */
public class Settings
{
    //These variables are machine-specific and are set via command-line parameters
    public static String MAIN_DIR;
    public static String GUTS_DIR;
    public static String RAW_DATA_DIR;
    public static String DATA_DIR;
    public static String TEMP_DATA_DIR;
    public static String TEMP_RESULTS_DIR;
    public static String CONFIG_DIR;
    public static String EXPERIMENTS_DIR;
    public static String FEATURE_SELECTION_DIR;
    public static String PREDICTIONS_DIR;
    public static String ENSEMBLE_PREDICTIONS_DIR;
    public static String LOCKS_DIR;
    public static String OUTPUT_DIR;
    public static String STATUS_DIR;
    public static int NUM_THREADS;
    public static long THREAD_TIMEOUT_MINUTES;
    public static long PAUSE_SECONDS;
    public static boolean SKIP_PREVIOUSLY_COMPLETED_ITERATIONS;
    public static String MISSING_VALUE_STRING = "?";
    public static HashMap<String, ClassificationAlgorithm> ClassificationAlgorithms = new HashMap<String, ClassificationAlgorithm>();
    public static HashMap<String, FeatureSelectionAlgorithm> FeatureSelectionAlgorithms = new HashMap<String, FeatureSelectionAlgorithm>();

    /** The purpose of this method is to parse the algorithms that have been defined in the algorithm configuration files.
     *
     * @param classificationAlgorithmsFilePath Absolute path to the configuration file specifying classification algorithms.
     * @param featureSelectionAlgorithmsFilePath Absolute path to the configuration file specifying feature-selection algorithms.
     * @throws Exception
     */
    public static void ParseAlgorithms(String classificationAlgorithmsFilePath, String featureSelectionAlgorithmsFilePath) throws Exception
    {
        for (String line : Files.ReadLinesFromFile(classificationAlgorithmsFilePath))
        {
            if (line.startsWith("#"))
                continue;

            ArrayList<String> lineItems = Lists.CreateStringList(line.split(";"));
            String description = lineItems.remove(0);
            String learnerClassName = lineItems.remove(0);
            ArrayList<String> parameters = lineItems;
            ClassificationAlgorithms.put(description, new ClassificationAlgorithm(description, learnerClassName, parameters));
        }

        for (String line : Files.ReadLinesFromFile(featureSelectionAlgorithmsFilePath))
        {
            if (line.startsWith("#"))
                continue;

            ArrayList<String> lineItems = Lists.CreateStringList(line.split(";"));

            String description = lineItems.remove(0);
            String learnerClassName = lineItems.remove(0);
            ArrayList<String> parameters = lineItems;

            FeatureSelectionAlgorithms.put(description, new FeatureSelectionAlgorithm(description, learnerClassName, parameters));
        }

        FeatureSelectionAlgorithms.put("None", new FeatureSelectionAlgorithm("None"));
        FeatureSelectionAlgorithms.put("PriorKnowledge", new FeatureSelectionAlgorithm("PriorKnowledge"));
    }

    public static String GetExperimentOutputDir(boolean addIterationIfMoreThanOne) throws Exception
    {
        if (addIterationIfMoreThanOne && Utilities.Config.GetNumIterations() > 1)
            return Settings.OUTPUT_DIR + "Iteration" + Utilities.Iteration + "/";
        
        return Settings.OUTPUT_DIR;
    }
}
