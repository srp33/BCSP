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

/** This class is designed to store all data for a set of data instances. It provides methods that make it easier to create, retrieve, update, and delete data values for these instances.
 * @author Stephen Piccolo
 */
public class DataInstanceCollection implements Iterable<DataValues>
{
    public static String END_OF_FILE_MARKER = "[EOF]";
    private ArrayList<DataValues> _instances;

    /** Default constructor */
    public DataInstanceCollection()
    {
        this(new ArrayList<DataValues>());
    }

    /** Constructor
     *
     * @param instance Data instance that will be the first data instance in this collection
     */
    public DataInstanceCollection(DataValues instance)
    {
        _instances = new ArrayList<DataValues>();
        _instances.add(instance);
    }

    /** Constructor
     *
     * @param instances List of data instances that will be included initially in this collection
     */
    public DataInstanceCollection(ArrayList<DataValues> instances)
    {
        _instances = instances;
    }

    /** Constructor
     *
     * @param instances Collection of data instances that will be included initially in this collection
     */
    public DataInstanceCollection(DataInstanceCollection instances)
    {
        this();
        for (DataValues instance : instances)
            _instances.add(instance);
    }

    /** Adds a data instance to this collection
     *
     * @param instance Data instance to add
     */
    public void Add(DataValues instance)
    {
        if (!Contains(instance))
            _instances.add(instance.CopyStructure());
        
        for (String dataPointName : instance)
            Add(dataPointName, instance.GetID(), instance.GetDataPointValue(dataPointName));
    }

    /** Adds a collection of data instances to this collection.
     *
     * @param instances Collection of data instances to add
     * @return This instance after the new instances have been added
     */
    public DataInstanceCollection Add(DataInstanceCollection instances)
    {
        for (DataValues instance : instances)
            Add(instance);

        return this;
    }

    /** Adds a data value for a given instance to this collection.
     *
     * @param dataPointName Data point name
     * @param instanceID Data instance ID
     * @param value Data value
     */
    public void Add(String dataPointName, String instanceID, String value)
    {
        DataValues instance = new DataValues(instanceID);

        if (this.Contains(instanceID))
            instance = Get(instanceID);

        instance.AddDataPoint(dataPointName, value);
        UpdateInstance(instance);
    }

    /** For a given data point, this method converts values to zero or one, depending on whether they coincide with the specified value.
     *
     * @param dataPointName Data point name
     * @param oneOption Value that corresponds to a "1" binary value (other values will be converted to "0"
     * @return Binarized value
     */
    public DataInstanceCollection BinarizeDataPoint(String dataPointName, String oneOption)
    {
        for (DataValues instance : this)
            instance.AddBinaryDataPoint(dataPointName, instance.GetDataPointValue(dataPointName), oneOption);

        return this;
    }

    /** Removes all data values from this collection.
     *
     * @return This instance
     */
    public DataInstanceCollection ClearDataPoints()
    {
        for (DataValues instance : _instances)
            UpdateInstance(instance.ClearDataPoints());

        return this;
    }

    /** Creates a deep copy of this collection
     *
     * @return Deep copy of this instance
     * @throws Exception
     */
    public DataInstanceCollection Clone() throws Exception
    {
        return Clone(GetIDs(), GetDataPointNames());
    }

    /** Creates a deep copy of this collection for the data instance IDs and data points specified
     *
     * @param ids Data instance IDs to include in copy
     * @param dataPoints Data points to include in copy
     * @return Deep copy of this instance
     * @throws Exception
     */
    public DataInstanceCollection Clone(ArrayList<String> ids, ArrayList<String> dataPoints) throws Exception
    {
        DataInstanceCollection newInstances = new DataInstanceCollection();

        for (DataValues instance : _instances)
        {
            if (ids.contains(instance.GetID()))
            {
                DataValues newInstance = instance.CopyStructure();

                for (String dataPoint : dataPoints)
                    newInstance.AddDataPoint(dataPoint, instance.GetDataPointValue(dataPoint));

                newInstances.Add(newInstance);
            }
        }

        return newInstances;
    }

    /** Indicates whether this collection contains the specified data instance.
     *
     * @param instance Query instance
     * @return Whether the collection contains the instance
     */
    public boolean Contains(DataValues instance)
    {
        return _instances.contains(instance);
    }

    /** Indicates whether this collection contains the specified data instance.
     *
     * @param instanceID Query instance ID
     * @return Whether the collection contains the instance
     */
    public boolean Contains(String instanceID)
    {
        return Contains(new DataValues(instanceID));
    }

//    public DataInstanceCollection FilterAnd(HashMap<String, String> namesAndValues)
//    {
//        DataInstanceCollection matches = new DataInstanceCollection();
//
//        for (DataValues instance : this)
//        {
//            int numMatches = 0;
//
//            for (String dataPointName : namesAndValues.keySet())
//            {
//                if (instance.GetDataPointValue(dataPointName).equals(namesAndValues.get(dataPointName)))
//                    numMatches++;
//            }
//
//            if (numMatches == namesAndValues.size())
//                matches.Add(instance.Clone());
//        }
//
//        return matches;
//    }
//
//    public DataInstanceCollection FilterByDataPointValue(ArrayList<String> dataPointNames, String dataPointValue)
//    {
//        DataInstanceCollection matches = new DataInstanceCollection();
//
//        for (DataValues instance : this)
//            for (String dataPointName : dataPointNames)
//                if (instance.GetDataPointValue(dataPointName).equals(dataPointValue))
//                {
//                    matches.Add(instance.Clone());
//                    break;
//                }
//
//        return matches;
//    }

    /** Creates a new collection that contains only data instances with the specified value for the specified data point.
     *
     * @param dataPointName Query data point name
     * @param dataPointValue Query data point value
     * @return New, filtered collection
     */
    public DataInstanceCollection FilterByDataPointValue(String dataPointName, String dataPointValue)
    {
        DataInstanceCollection matches = new DataInstanceCollection();

        for (DataValues instance : this)
        {
            String instanceValue = instance.GetDataPointValue(dataPointName);

            if (instanceValue.equals(dataPointValue))
                matches.Add(instance.Clone());
        }

        return matches;
    }

    /** Formats all data point names.
     *
     */
    public void FormatDataPointNames()
    {
        for (DataValues patient : this)
            patient.FormatDataPointNames();
    }

    /** Gets the data instance at the specified index.
     *
     * @param index Query index
     * @return Data instance at the specified index
     */
    public DataValues Get(int index)
    {
        return _instances.get(index);
    }

    /** Gets a collection of data instances that match the specified data instance IDs.
     *
     * @param ids Query data instance IDs
     * @return Collection of data instances for specified data instance IDs
     */
    public DataInstanceCollection Get(ArrayList<String> ids)
    {
        DataInstanceCollection result = new DataInstanceCollection();

        for (String id : ids)
            if (Contains(id))
                result.Add(Get(id));

        return result;
    }

    /** Gets the data instance for the specified data instance ID.
     *
     * @param id Query data instance ID
     * @return Data instance for the specified data instance ID
     */
    public DataValues Get(String id)
    {
        int index = _instances.indexOf(new DataValues(id));

        if (index > -1)
            return _instances.get(index);
        return null;
    }

    /** Gets a list of all data point names across all data instances in the collection.
     *
     * @return List of all data point names
     */
    public ArrayList<String> GetDataPointNames()
    {
        HashSet<String> names = new HashSet<String>();

        for (DataValues instance : _instances)
            names.addAll(instance.GetDataPointNames());

        return new ArrayList<String>(names);
    }

    /** Gets a list of data point names that match the specified pattern, across all data instances in the collection.
     *
     * @param pattern Pattern to match
     * @return List of matching data point names
     */
    public ArrayList<String> GetDataPointNamesMatching(String pattern)
    {
        ArrayList<String> matching = new ArrayList<String>();

        for (String dataPointName : GetDataPointNames())
            if (dataPointName.contains(pattern))
                matching.add(dataPointName);

        return matching;
    }

    /** Gets a list of all unique data point names across all data instances in the collection.
     *
     * @return List of unique data point names
     */
    public HashMap<String, ArrayList<String>> GetDataPointUniqueValues()
    {
        HashMap<String, ArrayList<String>> dataPointUniqueValues = new HashMap<String, ArrayList<String>>();

        for (String dataPointName : GetDataPointNames())
            dataPointUniqueValues.put(dataPointName, GetUniqueValues(dataPointName));

        return dataPointUniqueValues;
    }

    /** Gets the data point values across all data instances for the specified data point.
     *
     * @param dataPointName Query data point name
     * @return Data values across all data instances
     */
    public DataValues GetDataPointValues(String dataPointName)
    {
        DataValues values = new DataValues(dataPointName);

        for (DataValues instance : _instances)
            values.AddDataPoint(instance.GetID(), instance.GetDataPointValue(dataPointName));

        return values;
    }

    /** Gets a list of data instance IDs for the instances in this collection.
     *
     * @return List of all data instance IDs in this collection
     */
    public ArrayList<String> GetIDs()
    {
        ArrayList<String> ids = new ArrayList<String>();

        for (DataValues instance : _instances)
            ids.add(instance.GetID());

        return ids;
    }

    /** Indicates the number of data point names across all data instances in this collection.
     *
     * @return Number of data point names across all data instances in this collection
     */
    public int GetNumDataPoints()
    {
        return GetDataPointNames().size();
    }

    /** Indicates the proportion of missing values across all data instances in this collection.
     *
     * @return Proportion of missing values.
     */
    public double GetProportionMissingValues()
    {
        double numNotMissing = 0.0;

        for (DataValues instance : _instances)
            numNotMissing += instance.GetNumNotMissingValues();

        double proportionMissing = 1 - (numNotMissing / ((double) Size() * (double) GetNumDataPoints()));
        return MathUtility.Round(proportionMissing, 3);
    }

    /** Indicates which data points have all the same value across all data instances in the collection.
     *
     * @return List of data points that have all the same value across all data instances
     * @throws Exception
     */
    public ArrayList<String> GetSingleValueDataPoints() throws Exception
    {
        ArrayList<String> singleValued = new ArrayList<String>();

        for (String dataPointName : GetDataPointNames())
        {
            HashSet<String> uniqueValues = new HashSet<String>();

            for (DataValues instance : _instances)
            {
                String value = instance.GetDataPointValue(dataPointName);
                if (value != null && !value.equals(Settings.MISSING_VALUE_STRING))
                    uniqueValues.add(value);

                if (uniqueValues.size() > 1)
                    break;
            }

            if (uniqueValues.size() <= 0)
                singleValued.add(dataPointName);
        }

        return singleValued;
    }

    /** Identifies all unique values for the specified data point, across all data instances in the collection. Null and missing values are ignored.
     *
     * @param dataPointName Query data point name
     * @return List of all unique values for the specified data point
     */
    public ArrayList<String> GetUniqueValues(String dataPointName)
    {
        HashSet<String> values = new HashSet<String>();

        for (DataValues instance : _instances)
        {
            String value = instance.GetDataPointValue(dataPointName);
            if (value != null && !value.equals(Settings.MISSING_VALUE_STRING))
                values.add(value);
        }

        return new ArrayList<String>(values);
    }

    /** For the specified data points, this method retains only the data points that were specified (others are removed).
     *
     * @param toKeep List of data points to keep
     * @return This collection after filtering
     */
    public DataInstanceCollection KeepDataPoints(ArrayList<String> toKeep)
    {
        ArrayList<String> toRemove = Lists.GetDifference(GetDataPointNames(), toKeep);
        RemoveDataPointNames(toRemove);

        return this;
    }

    /** Retains only the data instances with IDs in the specified list. All others are removed.
     *
     * @param idsToKeep List of IDs that should be retained
     * @return This collection after filtering
     */
    public DataInstanceCollection KeepInstances(ArrayList<String> idsToKeep)
    {
        ArrayList<String> toRemove = Lists.GetDifference(GetIDs(), idsToKeep);
        RemoveInstances(toRemove);

        return this;
    }

//    public static DataInstanceCollection MergeLeft(DataInstanceCollection instances1, DataInstanceCollection instances2) throws Exception
//    {
//        DataInstanceCollection mergedInstances = new DataInstanceCollection();
//
//        for (String instanceID : instances1.GetIDs())
//        {
//            DataValues instance1 = instances1.Get(instanceID);
//
//            if (instances2.Contains(instanceID))
//            {
//                DataValues instance2 = instances2.Get(instanceID);
//
//                ArrayList<String> overlappingDataPoints = Lists.Intersect(instance1.GetDataPointNames(), instance2.GetDataPointNames());
//                if (overlappingDataPoints.size() > 0)
//                    throw new Exception("No overlapping data points allowed between instances in MergeInner. Overlapping: " + Lists.Join(overlappingDataPoints, " "));
//
//                DataValues mergedInstance = new DataValues(instanceID);
//                mergedInstance.AddDataPoints(instance1);
//                mergedInstance.AddDataPoints(instance2);
//                mergedInstances.Add(mergedInstance);
//            }
//            else
//                mergedInstances.Add(instance1);
//        }
//
//        return mergedInstances;
//    }

//    public static DataInstanceCollection MergeInner(DataInstanceCollection instances1, DataInstanceCollection instances2) throws Exception
//    {
//        DataInstanceCollection mergedInstances = new DataInstanceCollection();
//
//        ArrayList<String> uniqueInstanceIDs = Lists.Intersect(instances1.GetIDs(), instances2.GetIDs());
//
//        for (String instanceID : uniqueInstanceIDs)
//        {
//            DataValues instance1 = instances1.Get(instanceID);
//            DataValues instance2 = instances2.Get(instanceID);
//
//            ArrayList<String> overlappingDataPoints = Lists.Intersect(instance1.GetDataPointNames(), instance2.GetDataPointNames());
//            if (overlappingDataPoints.size() > 0)
//                throw new Exception("No overlapping data points allowed between instances in MergeInner. Overlapping: " + Lists.Join(overlappingDataPoints, " "));
//
//            DataValues mergedInstance = new DataValues(instanceID);
//            mergedInstance.AddDataPoints(instance1);
//            mergedInstance.AddDataPoints(instance2);
//
//            mergedInstances.Add(mergedInstance);
//        }
//
//        return mergedInstances;
//    }

    /** This method can be used to permute the data instances IDs in a collection. This should have a similar effect to permuting class labels. It can be used for validation experiments.
     *
     * @param instances Data instances to be permuted
     * @return Permuted data instances
     */
    public static DataInstanceCollection PermuteIDs(DataInstanceCollection instances)
    {
        ArrayList<String> ids = Lists.Shuffle(instances.GetIDs());
        DataInstanceCollection permutedInstances = new DataInstanceCollection();

        for (DataValues instance : instances)
        {
            DataValues permutedInstance = instance.Clone();
            permutedInstance.SetID(ids.remove(0));
            permutedInstances.Add(permutedInstance);
        }

        return permutedInstances;
    }

//    public static DataInstanceCollection PermuteDataValues(DataInstanceCollection instances) throws Exception
//    {
//        final DataInstanceCollection permutedInstances = new DataInstanceCollection();
//        final DataInstanceCollection instancesToCopy = instances.Clone();
//
//        //Collection<_callable<Object>> callables = new LinkedList<_callable<Object>>();
//
//        for (final String dataPointName : instancesToCopy.GetDataPointNames())
//        {
//        //    callables.add(new _callable<Object>()
//        //    {
//        //        public Object call() throws Exception
//        //        {
//                    ArrayList<String> dataValues = Lists.Shuffle(instancesToCopy.GetDataPointValues(dataPointName).GetAllValues());
//                    ArrayList<String> instanceIDs = Lists.Shuffle(instancesToCopy.GetIDs());
//
//                    for (String instanceID : instanceIDs)
//                        permutedInstances.Add(dataPointName, instanceID, dataValues.remove(0));
//
//        //            return null;
//        //        }
//        //    });
//        }
//
//        //Utilities.MultiThread(callables)
//
//        return permutedInstances;
//    }

    /** Prepends the specified text to the beginning of each data point name.
     *
     * @param prefix Prefix to be prepended
     */
    public void PrefixDataPointNames(String prefix)
    {
        for (String dataPointName : GetDataPointNames())
            UpdateDataPointName(dataPointName, prefix + "_" + dataPointName);
    }

    /** Removes the specified data points across all instances in the collection.
     *
     * @param dataPointNames List of data points to be removed
     * @return This collection after removal has occurred
     */
    public DataInstanceCollection RemoveDataPointNames(ArrayList<String> dataPointNames)
    {
        for (String dataPointName : dataPointNames)
            RemoveDataPointName(dataPointName);

        return this;
    }

    /** Removes the specified data point from all instances in the collection.
     *
     * @param dataPointName Data point to be removed
     */
    public void RemoveDataPointName(String dataPointName)
    {
        for (DataValues instance : _instances)
        {
            int index = _instances.indexOf(instance);

            if (index > -1)
            {
                instance.RemoveDataPoint(dataPointName);
                _instances.set(index, instance);
            }
        }
    }

    /** Removes any data point matching the specified pattern.
     *
     * @param pattern Query pattern
     */
    public void RemoveDataPointNamesMatching(String pattern)
    {
        RemoveDataPointNames(GetDataPointNamesMatching(pattern));
    }

    /** Removes data instances that are in the specified list.
     *
     * @param ids List of data instance IDs to be removed
     */
    public void RemoveInstances(ArrayList<String> ids)
    {
        for (String id : ids)
            RemoveInstance(id);
    }

    /** Removes the specified data instance.
     *
     * @param id Data instance ID
     */
    public void RemoveInstance(String id)
    {
        if (Contains(id))
            _instances.remove(_instances.indexOf(new DataValues(id)));
        else
            Utilities.Log.Info("A data instance with ID " + id + " cannot be removed because it does not exist in the collection.");
    }

    /** Replaces any missing values with the specified replacement value.
     *
     * @param newValue Replacement value
     * @return This instance after the replacement has occurred
     */
    public DataInstanceCollection ReplaceMissingValues(String newValue)
    {
        for (DataValues instance : this)
            UpdateInstance(instance.ReplaceMissingValues(newValue));

        return this;
    }

    /** Serializes this collection to a text file in a tab-delimited format.
     *
     * @param outputDirectory Absolute path to the directory where the file will be saved
     * @param fileNamePrefix File name prefix
     * @return Absolute path to the saved file
     * @throws Exception
     */
    public String SaveToFile(String outputDirectory, String fileNamePrefix) throws Exception
    {
        AnalysisFileCreator creator = new AnalysisFileCreator(outputDirectory, fileNamePrefix, this, null, Utilities.InstanceVault.GetRawDependentVariableInstances());
        return creator.CreateTabDelimitedFile(true).GetTabDelimitedFilePath();
    }

    /** Indicates the number of data instances in this collection.
     *
     * @return Number of data instances in this collection
     */
    public int Size()
    {
        return _instances.size();
    }

//    public void SortByID()
//    {
//        Collections.sort(_instances);
//    }

    /** Updates a given data point with the specified values.
     *
     * @param dataPointName Data point to be updated
     * @param values The values for each instance that will be updated
     */
    public void UpdateDataPoints(String dataPointName, DataValues values)
    {
        for (String instanceID : values)
            UpdateDataPoint(dataPointName, instanceID, values.GetDataPointValue(instanceID));
    }

    /** Updates a given data point for a given data instance with the specified value.
     *
     * @param dataPointName Data point to be updated
     * @param instanceID Data instance ID
     * @param value Update value
     */
    public void UpdateDataPoint(String dataPointName, String instanceID, String value)
    {
        if (value != null)
        {
            int index = _instances.indexOf(new DataValues(instanceID));
            DataValues instance = _instances.get(index);
            instance.AddDataPoint(dataPointName, value);
            _instances.set(index, instance);
        }
    }

    /** Changes an existing data point name to the specified value.
     *
     * @param fromDataPointName Current data point name
     * @param toDataPointName New data point name
     */
    public void UpdateDataPointName(String fromDataPointName, String toDataPointName)
    {
        for (DataValues instance : this)
        {
            instance.UpdateDataPointName(fromDataPointName, toDataPointName);
            UpdateInstance(instance);
        }
    }

    /** Replaces an existing data instance with the specified data instance. If the data instance does not exist in this collection, the specified instance is added.
     *
     * @param instance Replacement data instance
     */
    public void UpdateInstance(DataValues instance)
    {
        int index = _instances.indexOf(instance);

        if (index == -1)
            _instances.add(instance);
        else
            _instances.set(index, instance);
    }

    public Iterator<DataValues> iterator()
    {
        return _instances.iterator();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (DataValues instance : _instances)
            builder.append(instance.toString() + "\n");

        return builder.toString();
    }

    /** Serializes this collection to a text file in the ML-Flex format.
     *
     * @param filePath Absolute file path where the file will be saved
     * @throws Exception
     */
    public void SerializeToFile(String filePath) throws Exception
    {
        Files.WriteTextToFile(filePath, "");

        for (DataValues instance : _instances)
            Files.AppendTextToFile(filePath, instance.toString() + "\n");

        Files.AppendTextToFile(filePath, END_OF_FILE_MARKER);
    }

    /** Deserializes a collection that has been saved in a text file in the ML-Flex format.
     *
     * @param filePath Absolute file path where the file is located
     * @return A data collection
     * @throws Exception
     */
    public static DataInstanceCollection DeserializeFromFile(String filePath) throws Exception
    {
        Files.CheckFileExists(filePath);

        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        BigFileReader fileReader = new BigFileReader(filePath);

        for (final String line : fileReader)
        {
            taskHandler.Add(new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return DataValues.FromString(line);
                }
            });
        }

        boolean reachedEndOfFile = false;

        DataInstanceCollection collection = new DataInstanceCollection();
        for (Object x : taskHandler.Execute())
        {
            DataValues dv = (DataValues)x;
            if (dv.GetID().equals(END_OF_FILE_MARKER))
                reachedEndOfFile = true;
            else
                collection.Add(dv);
        }

        if (reachedEndOfFile)
            return collection;
        else
            throw new Exception("Never reached the end of file marker in " + filePath + ".");
    }

    /** Creates a String representation of this object in a format that can be used for debugging purposes.
     *
     * @return Short String representation of this object
     */
    public String toShortString()
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i<5 && i< Size(); i++)
        {
            DataValues instance = Get(i);
            builder.append(instance.toShortString() + "\n");
        }

        return builder.toString();
    }
}
