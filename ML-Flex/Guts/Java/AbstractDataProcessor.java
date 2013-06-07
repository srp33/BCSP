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

import java.util.*;
import java.util.concurrent.Callable;

/** This abstract class coordinates all tasks required to process raw data, store data after it has been processed, and describe the data. This class takes care of the generic functionality to accomplish these tasks yet allows the user to develop custom classes that inherit from this class.
 *
 * @author Stephen Piccolo
 */

public abstract class AbstractDataProcessor
{
    private RawDataInstanceCollection _rawDataInstances = new RawDataInstanceCollection();

    public Boolean ProcessRawData() throws Exception
    {
        Boolean success;

        try
        {
            Utilities.Log.Debug("Parsing raw data for " + GetDescription());
            ParseRawData();

            Utilities.Log.Debug("Getting raw instances for " + GetDescription());
            RawDataInstanceCollection rawInstances = GetRawInstances();

            Utilities.Log.Debug("Transforming/summarizing raw instances for " + GetDescription());
            DataInstanceCollection instances = rawInstances.GetTransformedSummarizedCollection(this);
            rawInstances = null; // Saves memory?
            ClearRawIinstances(); // Saves memory?

            Utilities.Log.Debug("Transforming instances for " + GetDescription());
            DataInstanceCollection transformedInstances = TransformInstances(instances);
            transformedInstances.FormatDataPointNames();

            Utilities.Log.Debug("Saving transformed instances for " + GetDescription());
            transformedInstances.SerializeToFile(GetDataFilePath());
            DataInstanceCollection.DeserializeFromFile(GetDataFilePath()); // This verifies that you can read the file after writing it

            success = Boolean.TRUE;
        }
        catch (Exception ex)
        {
            Utilities.Log.Debug(ex);
            success = Boolean.FALSE;
        }

        return success;
    }

    /** This is the name of the directory, under the raw data directory, that contains the raw data to be processed.
     * 
     * @return Directory name (relative path)
     * @throws Exception
     */
    protected String GetRawDataDirName() throws Exception
    {
        throw new Exception("Not implemented");
    }

    /** This is the absolute path to the raw data directory. Raw data files for this data processor should be stored in this location.
     *
     * @return Absolute directory path
     * @throws Exception
     */
    public String GetRawDataDir() throws Exception
    {
        return Settings.RAW_DATA_DIR + GetRawDataDirName() + "/";
    }

    /** When this method is executed, raw data is parsed from the raw data directory and saved so it can be processed further. Classes that inherit from this class must implement this method if new data are being added to ML-Flex.
     *
     * @throws Exception
     */
    protected void ParseRawData() throws Exception
    {
        throw new Exception("Not implemented");
    }

    /** This method provides a description of the data provided by this processor. By default this description is the beginning of the class name.
     *
     * @return Description of the data
     */
    public String GetDescription()
    {
        return this.getClass().getSimpleName().replace("DataProcessor", "");
    }

    /** This method supports custom specification of how much sparsity is allowed per data instance. If a given data instance is missing more data than the specified proportion (0.00 - 1.00), it will be filtered out and not used in machine-learning analysis steps.
     *
     * @return Proportion of missing values allowed for a given data instance
     */
    protected double GetProportionMissingPerInstanceOK()
    {
        return 0.5;
    }

    /** This method supports custom specification of how much sparsity is allowed per data point. If a given data point is missing more data than the specified proportion (0.00 - 1.00) across all instances, it will be filtered out and not used in machine-learning analysis steps.
     *
     * @return Proportion of missing values allowed for a given data point
     */
    protected double GetProportionMissingPerDataPointOK()
    {
        return 0.5;
    }

    /** If the "PriorKnowledge" feature-selection approach is used, a hand-selected list of features for each data processor must be specified. In this case, this method should be overridden, and the values should be returned.
     *
     * @return A list of features that are believed (perhaps based on prior studies or a literature search) to be most relevant for classification.
     * @throws Exception
     */
    protected ArrayList<String> GetPriorKnowledgeSelectedFeatures() throws Exception
    {
        return new ArrayList<String>();
    }

    /** This value provides DataValueMeta about a given data point. This helps ML-Flex to interpret the raw data as it is processed. If this is not override, generic metadata will be used.
     *
     * @param dataPointName Data point name
     * @return Metadata describing the data point.
     */
    protected DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new GetFirstSummarizer(), new NullTransformer());
    }

    /** After raw data have been processed, they are packages into a DataInstanceCollection and can be processed further by calling this method.
     *
     * @return A collection of data instances
     * @throws Exception
     */
    protected DataInstanceCollection GetTransformedInstances() throws Exception
    {
        return GetInstancesFromFile();
    }

    /** Retrieves data instances from a processed file.
     *
     * @return Data instances that are stored in an ML-Flex formatted file
     * @throws Exception
     */
    protected DataInstanceCollection GetInstancesFromFile() throws Exception
    {
        String filePath = GetDataFilePath();

        if (!Files.FileExists(filePath))
        {
            Utilities.Log.Info("No data file exists at " + filePath + " so returning an empty set of data instances. Make sure the data have been preprocessed.");
            return new DataInstanceCollection();
        }

        return DataInstanceCollection.DeserializeFromFile(filePath);
    }

    /** After data instances have been processed and stored, it is still possible to modify them before each time they are used in a machine-learning analysis. This method supports that functionality.
     *
     * @param instances Data instances to be updated
     * @throws Exception
     */
    protected void UpdateInstancesForAnalysis(DataInstanceCollection instances) throws Exception
    {
    }

    /** Returns the absolute file path where ML-Flex stores data for this processor.
     *
     * @return Absolute file path where ML-Flex stores data file
     */
    protected String GetDataFilePath()
    {
        return Settings.DATA_DIR + GetDescription() + ".data";
    }

    /** This method is used by most custom data processors that inherits from AbstractDataProcessor. It stores a given raw data point for further processing.
     *
     * @param dataPointName The name that should be used by ML-Flex to describe the data value
     * @param instanceID The instance ID associated witih the data value
     * @param value The data value
     * @throws Exception
     */
    public void SaveRawDataPoint(String dataPointName, String instanceID, String value) throws Exception
    {
        ////if (!Utilities.IsMissing(value))
        ////    Files.AppendTextToFile(GetRawDataPointFilePath(Utilities.FormatName(dataPointName)), instanceID + "\t" + value + "\n");

        _rawDataInstances.Add(dataPointName, instanceID, value);
    }

    /** This method retrieves raw data instances after the initial processing has occured.
     *
     * @return Collection of raw data instances
     * @throws Exception
     */
    protected RawDataInstanceCollection GetRawInstances() throws Exception
    {
        return _rawDataInstances;
//        ArrayList<String> dataPointNames = GetSavedRawDataPointNames();
//
//        DataInstanceCollection rawInstances = new DataInstanceCollection();
//
//        for (String dataPointName : dataPointNames)
//        {
//            DataValues dataPointValues = GetRawDataPointValues(dataPointName);
//
//            for (String instanceID : dataPointValues)
//                rawInstances.Add(Utilities.UnformatName(dataPointName), instanceID, dataPointValues.GetDataPointValue(instanceID));
//        }
//
//        Utilities.Log.Info("# raw instances: " + rawInstances.Size());
//        Utilities.Log.Info("# raw data points: " + rawInstances.GetNumDataPoints());
//
//        return rawInstances;
    }

    /** Removes raw data instances from memory.
     *
     * @throws Exception
     */
    public void ClearRawIinstances() throws Exception
    {
        _rawDataInstances = new RawDataInstanceCollection();
    }

//    private DataValues GetRawDataPointValues(String dataPointName) throws Exception
//    {
//        DataValueMeta meta = GetDataPointMeta(dataPointName);
//
//        HashMap<String, ArrayList<String>> instanceValues = new HashMap<String, ArrayList<String>>();
//
//        for (ArrayList<String> item : GetSavedRawData(dataPointName))
//        {
//            String instanceID = item.get(0);
//            String value = item.get(1);
//
//            if (value == null || value.equals(Settings.MISSING_VALUE_STRING) || meta.IsNullValue(value))
//                continue;
//
//            ArrayList<String> values = new ArrayList<String>();
//
//            if (instanceValues.containsKey(instanceID))
//                values = instanceValues.get(instanceID);
//
//            values.add(value);
//            instanceValues.put(instanceID, values);
//        }
//
//        DataValues dataValues = new DataValues(dataPointName);
//        for (Map.Entry<String, ArrayList<String>> entry : instanceValues.entrySet())
//        {
//            String instanceID = entry.getKey();
//            ArrayList<String> values = entry.getValue();
//
//            for (int i=0; i<values.size(); i++)
//                values.set(i, meta.Transformer.TransformValue(values.get(i)));
//
//            String summarizedValue = Settings.MISSING_VALUE_STRING;
//            if (values.size() == 1)
//                summarizedValue = values.get(0);
//            else
//            {
//                if (values.size() > 1)
//                    summarizedValue = meta.Summarizer.Summarize(values);
//            }
//
//            dataValues.AddDataPoint(instanceID, summarizedValue);
//        }
//
//        return dataValues;
//    }

    /** After raw data have been processed by ML-Flex, they can be transformed before they are stored in the ML-Flex final format. This method supports that functionality. It invokes various other methods that can be overriden to transform individual parts of the data, or this method can be overridden to perform a wholesale transformation.
     *
     * @param rawInstances Raw data instances to be transformed
     * @return Transformed data instances
     * @throws Exception
     */
    protected DataInstanceCollection TransformInstances(DataInstanceCollection rawInstances) throws Exception
    {
        DataInstanceCollection transformedInstances = new DataInstanceCollection();

        for (DataValues instance : rawInstances)
            if (KeepRawInstance(instance))
                transformedInstances.Add(TransformRawInstance(instance));

        Utilities.Log.Info("Transforming data points for " + GetDescription());
        ArrayList<String> dataPointNames = transformedInstances.GetDataPointNames();
        for (String dataPointName : dataPointNames)
        {
            DataValues transformed = TransformDataPoint(transformedInstances.GetDataPointValues(dataPointName));
            transformedInstances.UpdateDataPoints(dataPointName, transformed);
        }

        Utilities.Log.Info("Updating data point names for " + GetDescription());
        for (String dataPointName : dataPointNames)
            transformedInstances.UpdateDataPointName(dataPointName, TransformDataPointName(dataPointName));

        Utilities.Log.Info("# transformed instances: " + transformedInstances.Size());
        Utilities.Log.Info("# transformed data points: " + transformedInstances.GetNumDataPoints());

        return transformedInstances;
    }

    /** This method indicates whether a given raw data instance should be retained for further processing. A custom data processor might use certain filter criteria to remove some data instances.
     *
     * @param instance Data instance to be tested
     * @return Whether or not the instance should be retained
     * @throws Exception
     */
    protected boolean KeepRawInstance(DataValues instance) throws Exception
    {
        return true;
    }

    /** Individual data instances can be transformed before they are stored in the ML-Flex final format. This method supports that functionality for individual data instances. By default, no transformation is performed.
     *
     * @param instance Data instance to be transformed.
     * @return Transformed data instance.
     * @throws Exception
     */
    protected DataValues TransformRawInstance(DataValues instance) throws Exception
    {
        return instance;
    }

    /** Individual data points can be transformed before data are stored in the ML-Flex final format. This method supports that functionality. By default, no transformation is performed.
     *
     * @param data
     * @return Transformed data values
     * @throws Exception
     */
    protected DataValues TransformDataPoint(DataValues data) throws Exception
    {
        return data;
    }

    /** The name of a data point can be changed using this method. By default, no transformation is performed.
     *
     * @param dataPointName Raw data point name
     * @return Transformed data point name
     * @throws Exception
     */
    protected String TransformDataPointName(String dataPointName) throws Exception
    {
        return dataPointName;
    }

    /** This method indicates whether a given transformed data instance should be retained for further processing. A custom data processor might use certain filter criteria to remove some data instances.
     *
     * @param instance Data instance to be tested
     * @return Whether or not the instance should be retained
     * @throws Exception
     */
    protected boolean KeepTransformedInstance(DataValues instance) throws Exception
    {
        return true;
    }

    /** This method supports any processing steps that should be performed after all previous steps are performed. By default, no post-processing occurs.
     *
     * @throws Exception
     */
    protected Boolean PostProcessRawData() throws Exception
    {
        return Boolean.TRUE;
    }

    /** This method removes any sparse instances, so they will not be used in machine-learning analyses.
     *
     * @param instances Data instances to be tested for sparsity
     * @throws Exception
     */
    public void RemoveSparseInstances(DataInstanceCollection instances) throws Exception
    {
        if (GetProportionMissingPerInstanceOK() == 1.0)
            return;
        
        ArrayList<String> toRemove = new ArrayList<String>();

        for (DataValues instance : instances)
            if (InstanceIsSparse(instance))
                toRemove.add(instance.GetID());

        instances.RemoveInstances(toRemove);
    }

    private boolean InstanceIsSparse(DataValues instance) throws Exception
    {
        double numMissing = 0.0;

        for (String dataPointName : instance.GetDataPointNames())
            if (instance.GetDataPointValue(dataPointName).equals(Settings.MISSING_VALUE_STRING))
                numMissing += 1.0;

        double proportionMissing = (numMissing / (double) instance.GetDataPointNames().size());
        return proportionMissing > GetProportionMissingPerInstanceOK();
    }

    /** This method removes any sparse data points, so they will not be used in machine-learning analyses.
     *
     * @param instances Data instances to be tested for sparsity
     * @throws Exception
     */
    protected void RemoveSparseDataPoints(DataInstanceCollection instances) throws Exception
    {
        if (GetProportionMissingPerDataPointOK() == 1.0)
            return;

        final DataInstanceCollection instances2 = instances.Clone();
        final double numInstances = (double)instances.Size();

        Collection<Callable<Object>> callables = new LinkedList<Callable<Object>>();

        for (final String dataPointName : instances.GetDataPointNames())
        {
            callables.add(new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    if (DataPointIsSparse(instances2, dataPointName, numInstances))
                        return dataPointName;

                    return null;
                }
            });
        }

        //instances.RemoveDataPoints(Utilities.MultiThread(callables));

        ArrayList<String> sparse = new ArrayList<String>();
        for (Callable callable : callables)
        {
            String result = (String)callable.call();
            if (result != null)
                sparse.add(result);
        }

        instances.RemoveDataPointNames(sparse);
    }

    private boolean DataPointIsSparse(DataInstanceCollection instances, String dataPointName, double numInstances) throws Exception
    {
        ArrayList<String> values = instances.GetDataPointValues(dataPointName).GetAllValues();
        HashMap<String, Integer> frequencyMap = Maps.GetFrequencyMap(values);

        if (!frequencyMap.containsKey(Settings.MISSING_VALUE_STRING))
            return false;

        double numMissing = (double)frequencyMap.get(Settings.MISSING_VALUE_STRING);
        return ((numMissing / numInstances) > GetProportionMissingPerDataPointOK());
    }

    /** This method can be used to convert data points that have more than two possible values into a series of binary data points.
     *
     * @param instances Data instances to be converted
     * @return Converted data instances
     * @throws Exception
     */
    protected DataInstanceCollection ConvertMultiValuedDataPointsToBinary(DataInstanceCollection instances) throws Exception
    {
        DataInstanceCollection modInstances = new DataInstanceCollection();

        for (String dataPointName : instances.GetDataPointNames())
        {
            ArrayList<String> uniqueDataPointValues = instances.GetUniqueValues(dataPointName);

            for (DataValues instance : instances)
            {
                DataValues newInstance = instance.CopyStructure();

                if (uniqueDataPointValues.size() <= 2)
                    newInstance.AddDataPoint(dataPointName, instance.GetDataPointValue(dataPointName));
                else
                {
                    if (DataTypes.HasOnlyNumeric(uniqueDataPointValues))
                        newInstance.AddDataPoint(dataPointName, instance.GetDataPointValue(dataPointName));
                    else
                        for (String dataPointValueOption : uniqueDataPointValues)
                            newInstance.AddBinaryDataPoint(dataPointName + "_" + dataPointValueOption, instance.GetDataPointValue(dataPointName), dataPointValueOption);
                }

                modInstances.Add(newInstance);
            }
        }

        return modInstances;
    }

    /** This method saves basic statistical information about the transformed data used by this processor.
     *
     * @return Whether values were saved to the file system successfully
     * @throws Exception
     */
    public Boolean SaveStatistics() throws Exception
    {
        DataInstanceCollection dataInstances = Utilities.InstanceVault.GetAnalysisInstances(this, null, null);

        String statisticsFilePath = Utilities.GetStatisticsFilePath(GetDescription());
        Utilities.SaveScalarValue(statisticsFilePath, "Num Instances", dataInstances.Size());
        Utilities.SaveScalarValue(statisticsFilePath, "Num Features", dataInstances.GetNumDataPoints());
        Utilities.SaveScalarValue(statisticsFilePath, "Proportion Missing Values", dataInstances.GetProportionMissingValues());

        return Boolean.TRUE;
    }

    /** This method saves basic statistical information that describes all independent-variable processors.
     *
     * @return Whether the save was successful
     * @throws Exception
     */
    public static Boolean SaveStatisticsAcrossAllIndependentVariableProcessors() throws Exception
    {
        ArrayList<String> allIDs = new ArrayList<String>();

        for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
            allIDs.addAll(Utilities.InstanceVault.GetAnalysisInstanceIDs(processor));

        HashMap<String, Integer> idFrequencyMap = Maps.GetFrequencyMap(allIDs);
        ArrayList<String> allFrequencies = Lists.CreateStringList(idFrequencyMap.values());
        HashMap<String, Integer> frequencyFrequencyMap = Maps.GetFrequencyMap(allFrequencies);

        for (Map.Entry<String, Integer> entry : frequencyFrequencyMap.entrySet())
            Utilities.SaveScalarValue(Utilities.GetStatisticsFilePath("All"), "Subjects with at least " + entry.getKey() + " categories", Maps.GetNumKeysGreaterThanOrEqualTo(frequencyFrequencyMap, Integer.parseInt(entry.getKey())));

        return Boolean.TRUE;
    }

    /** This method indicates whether this instance is equal to another instance, based on the descriptions.
     *
     * @param obj Object to be tested
     * @return Whether this instance is equal to another instance
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof AbstractDataProcessor)) return false;

        AbstractDataProcessor compareObj = (AbstractDataProcessor)obj;
        return compareObj.GetDescription().equals(this.GetDescription());
    }

    @Override
    public int hashCode()
    {
        return this.GetDescription().hashCode();
    }

    /** String representation of this class.
     *
     * @return Description
     */
    @Override
    public String toString()
    {
        return GetDescription();
    }
}