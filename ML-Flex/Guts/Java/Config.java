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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/** This class is used to parse configuration values from configuration files.
 * @author Stephen Piccolo
 */
public class Config
{
    private String _filePath;

    /** Constructor
     *
     * @param filePath Absolute file path to the configuration file this object will represent
     */
    public Config(String filePath)
    {
        _filePath = filePath;
    }

    /** Gets the configuration value indicating how many times the experiment should be performed iteratively.
     *
     * @return Number of iterations to perform
     * @throws Exception
     */
    public int GetNumIterations() throws Exception
    {
        return GetIntValue("NUM_ITERATIONS", 1);
    }

    /** Gets an array of feature-selection algorithms that should be used for the current experiment.
     *
     * @return Algorithms to use
     * @throws Exception
     */
    public FeatureSelectionAlgorithm[] GetFeatureSelectionAlgorithms() throws Exception
    {
        ArrayList<String> configValues = GetMandatoryStringListConfigValue("FEATURE_SELECTION_ALGORITHMS");

        FeatureSelectionAlgorithm[] algorithms = new FeatureSelectionAlgorithm[configValues.size()];

        for (int i=0; i<configValues.size(); i++)
            algorithms[i] = Settings.FeatureSelectionAlgorithms.get(configValues.get(i));

        return algorithms;
    }

    private ClassificationAlgorithm[] GetClassificationAlgorithms(String key, String[] defaultValues) throws Exception
    {
        ArrayList<String> configValues = new ArrayList<String>();

        if (defaultValues == null)
            configValues = GetMandatoryStringListConfigValue(key);
        else
            configValues = GetStringListConfigValue(key, defaultValues);

        ClassificationAlgorithm[] algorithms = new ClassificationAlgorithm[configValues.size()];

        for (int i=0; i<configValues.size(); i++)
        {
            if (!Settings.ClassificationAlgorithms.containsKey(configValues.get(i)))
                Utilities.Log.ExceptionFatal(new Exception("No classification algorithm has been configured with a " + configValues.get(i) + " key."));

            algorithms[i] = Settings.ClassificationAlgorithms.get(configValues.get(i));
        }

        return algorithms;
    }

    /** Gets an array of classification algorithms that should be used for the current experiment.
     *
     * @return Algorithms to use
     * @throws Exception
     */
    public ClassificationAlgorithm[] GetMainClassificationAlgorithms() throws Exception
    {
        return GetClassificationAlgorithms("CLASSIFICATION_ALGORITHMS", null);
    }

    /** Gets an array of classification algorithms that should be used for the current experiment as the second level classification algorithms in the "stacked" combiner.
     *
     * @return Algorithms to use
     * @throws Exception
     */
    public ClassificationAlgorithm[] GetStackingClassificationAlgorithms() throws Exception
    {
        return GetClassificationAlgorithms("STACKING_CLASSIFICATION_ALGORITHMS", new String[] {"weka_svm_rbf"});
    }

    /** Gets the configuration value for the number of outer cross-validation folds.
     *
     * @param numInstances Total number of data instances in this experiment
     * @return Number of outer cross-validation folds
     * @throws Exception
     */
    public int GetNumOuterCrossValidationFolds(int numInstances) throws Exception
    {
        int configNumFolds = GetIntValue("NUM_OUTER_CROSS_VALIDATION_FOLDS", 10);

        if (configNumFolds < 1)
            return numInstances;

        return configNumFolds;
    }

    /** Gets the configuration value for the number of inner cross-validation folds.
     *
\    * @return Number of inner cross-validation folds
     * @throws Exception
     */
    public int GetNumInnerCrossValidationFolds() throws Exception
    {
        return GetIntValue("NUM_INNER_CROSS_VALIDATION_FOLDS", 10);
    }

    /** Gets the configuration value for the random seed that should be used for assigning cross-validation folds.
     *
     * @return Random seed
     * @throws Exception
     */
    public long GetRandomSeed() throws Exception
    {
        return GetLongValue("RANDOM_SEED", Utilities.Iteration);
    }

    /** In cases where multiple data processors are used, this configuration value indicates whether only those data instances with data for all processors should be used in machine-learning analyses.
     *
     * @return Whether only data instances with data for all processors should be used
     * @throws Exception
     */
    public boolean OnlyInstancesWithAllData() throws Exception
    {
        return GetBooleanValue("ONLY_INSTANCES_WITH_ALL_DATA", false);
    }

    /** Gets the configuration value indicating any data instances that should be excluded.
     *
     * @return List of instances to exclude
     * @throws Exception
     */
    public ArrayList<String> GetInstanceIDsToExclude() throws Exception
    {
        return Lists.CreateStringList(GetStringListConfigValue("INSTANCE_IDS_TO_EXCLUDE", new String[]{}));
    }

    /** Indicates how many training instances should be excluded randomly from the analyses. This strategy can be used for evaluating the effects of outliers, etc.
     *
     * @return Number of training instances to exclude randomly
     * @throws Exception
     */
    public int GetNumTrainingInstancesToExcludeRandomly() throws Exception
    {
        return GetIntValue("NUM_TRAINING_INSTANCES_TO_EXCLUDE_RANDOMLY", 0);
    }

    /** Gets the configuration value indicating any data instances that should be used specifically for training.  It is also possible to specify a path to a text file containing a list of data instance IDs (one on each line).
     *
     * @return Data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetTrainingInstanceIDs() throws Exception
    {
        ArrayList<String> instanceIDs = Lists.GetUniqueValues(Lists.CreateStringList(GetStringListConfigValue("TRAIN_INSTANCE_IDS", new String[]{})));

        if (instanceIDs.size() == 1)
        {
            String filePath = Settings.EXPERIMENTS_DIR + instanceIDs.get(0);

            if (Files.FileExists(filePath))
                instanceIDs = Files.ReadLinesFromFile(filePath);
            else
                throw new Exception("A single value cannot be specified for TRAIN_INSTANCE_IDS unless it is a file name containing a list of training instance IDs. A file with the path " + Settings.EXPERIMENTS_DIR + instanceIDs.get(0) + " does not exist.");
        }

        return instanceIDs;
    }

    /** Gets the configuration value indicating any data instances that should be used specifically for testing. If this is not specified, they will be assigned randomly. It is also possible to specify a path to a text file containing a list of data instance IDs (one on each line).
     *
     * @return Data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetTestInstanceIDs() throws Exception
    {
        ArrayList<String> instanceIDs = Lists.GetUniqueValues(Lists.CreateStringList(GetStringListConfigValue("TEST_INSTANCE_IDS", new String[]{})));

        if (instanceIDs.size() == 1)
        {
            String filePath = Settings.EXPERIMENTS_DIR + instanceIDs.get(0);

            if (Files.FileExists(filePath))
                instanceIDs = Files.ReadLinesFromFile(filePath);
            else
                throw new Exception("A single value cannot be specified for TEST_INSTANCE_IDS unless it is a file name containing a list of test instance IDs. A file with the path " + Settings.EXPERIMENTS_DIR + instanceIDs.get(0) + " does not exist.");
        }

        return instanceIDs;
    }

    /** Gets the number of feature options that should be used.
     *
     * @param processor Data processor
     * @param algorithm Feature selection algorithm
     * @return List of options
     * @throws Exception
     */
    public ArrayList<Integer> GetNumFeaturesOptions(AbstractDataProcessor processor, FeatureSelectionAlgorithm algorithm) throws Exception
    {
        if (algorithm.IsNone() || algorithm.IsPriorKnowledge())
            return Lists.CreateIntegerList(Utilities.InstanceVault.GetAnalysisDataPoints(processor, null).size());

        int numDataPoints = Utilities.InstanceVault.GetAnalysisInstancesNumDataPoints(processor);

        HashSet<Integer> options = new HashSet<Integer>();

        if (algorithm.IsNone())
        {
            options.add(numDataPoints);
        }
        else
        {
            for (int numFeatures : GetIntListConfigValue("NUM_FEATURES_OPTIONS", new Integer[]{numDataPoints}))
            {
                if (numDataPoints >= numFeatures && numFeatures > 0)
                    options.add(numFeatures);
                if (numFeatures <= 0)
                    options.add(numDataPoints);
            }

            if (options.size() == 0)
                options.add(numDataPoints);
        }

        ArrayList<Integer> numFeaturesOptions = new ArrayList<Integer>(options);
        Collections.sort(numFeaturesOptions);

        return numFeaturesOptions;
    }

    HashMap<String, String> _configValues = new HashMap<String, String>();
    private String GetConfigValue(String key) throws Exception
    {
        if (!_configValues.containsKey(key))
        {
            String value = ParseConfigValue(key);
            _configValues.put(key, value);
        }

        return _configValues.get(key);
    }

    private String ParseConfigValue(String key) throws Exception
    {
        String fileText = Files.ReadTextFile(_filePath);

        HashMap<String, String> items = new HashMap<String, String>();

        for (String line : fileText.split("\n"))
        {
            line = line.trim();
            if (!line.startsWith("#"))
            {
                String[] lineValues = line.split("=");
                String rowKey = lineValues[0].trim();

                String rowValue = lineValues.length == 2 ? lineValues[1].trim() : "";
                items.put(rowKey,  rowValue);
            }
        }

        String value = items.get(key);

        return value;
    }

    /** Indicates whether a configuration value with the specified key has been specified.
     *
     * @param key Configuration key
     * @return Whether a value has been specified
     * @throws Exception
     */
    public boolean HasConfigValue(String key) throws Exception
    {
        String value = GetConfigValue(key);

        if (value == null || value.equals(""))
            return false;
        return true;
    }

    /** Convenience method to get a configuration value that is a String.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public String GetStringValue(String key, String defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return defaultValue;

        return GetConfigValue(key);
    }

    /** Convenience method to get a configuration value that is a String. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public String GetMandatoryStringValue(String key) throws Exception
    {
        if (!HasConfigValue(key))
            ThrowMissingKeyException(key);

        return GetConfigValue(key);
    }

    /** Convenience method to get a configuration value that is an integer.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public int GetIntValue(String key, int defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return defaultValue;

        return Integer.parseInt(GetConfigValue(key));
    }

    /** Convenience method to get a configuration value that is an integer. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public int GetMandatoryIntValue(String key) throws Exception
    {
        if (!HasConfigValue(key))
            ThrowMissingKeyException(key);

        return Integer.parseInt(GetConfigValue(key));
    }

    /** Convenience method to get a configuration value that is a long object.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public long GetLongValue(String key, long defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return defaultValue;

        return Long.parseLong(GetConfigValue(key));
    }

    /** Convenience method to get a configuration value that is a long object. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public long GetMandatoryLongValue(String key) throws Exception
    {
        if (!HasConfigValue(key))
            ThrowMissingKeyException(key);

        return Long.parseLong(key);
    }

    /** Convenience method to get a configuration value that is a boolean object.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public boolean GetBooleanValue(String key, boolean defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return defaultValue;

        return Boolean.parseBoolean(GetConfigValue(key));
    }

    /** Convenience method to get a configuration value that is a delimited list of String values.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<String> GetStringListConfigValue(String key, String[] defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return Lists.CreateStringList(defaultValue);

        return Lists.GetUniqueValues(Lists.CreateStringList(GetConfigValue(key).split(";")));
    }

    /** Convenience method to get a configuration value that is a list of String values. If no value is found, a fatal exception is thrown.
     *
     * @param key Configuration key
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<String> GetMandatoryStringListConfigValue(String key) throws Exception
    {
        if (!HasConfigValue(key))
            ThrowMissingKeyException(key);

        return GetStringListConfigValue(key, new String[0]);
    }

    /** Convenience method to get a configuration value that is a delimited list of integer values.
     *
     * @param key Configuration key
     * @param defaultValue Default value if no configuration value is found
     * @return Configuration value
     * @throws Exception
     */
    public ArrayList<Integer> GetIntListConfigValue(String key, Integer[] defaultValue) throws Exception
    {
        if (!HasConfigValue(key))
            return Lists.CreateIntegerList(defaultValue);

        return new ArrayList<Integer>(new HashSet<Integer>(Lists.CreateIntegerList(GetStringListConfigValue(key, new String[0]))));
    }

    private void ThrowMissingKeyException(String key) throws Exception
    {
        throw new Exception("No config value with key of " + key + " in " + _filePath);
    }
}