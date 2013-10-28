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

/** This class is used to store raw data. It allows multiple values per data point and is essentially a wrapper around DataInstanceCollection.
 * @author Stephen Piccolo
 */
public class RawDataInstanceCollection
{
    private DataInstanceCollection _instanceCollection = new DataInstanceCollection();

    /** Adds a data valueToAdd for a given instance to this collection. It takes care of handling multiple values per data point.
     *
     * @param dataPointName Data point name
     * @param instanceID Data instance ID
     * @param valueToAdd Data value to add
     * @return This instance
     */
    public RawDataInstanceCollection Add(String dataPointName, String instanceID, String valueToAdd)
    {
        DataValues instance = new DataValues(instanceID);

        if (_instanceCollection.Contains(instanceID))
            instance = _instanceCollection.Get(instanceID);

        String dataPointValue = valueToAdd;

        if (instance.HasDataPoint(dataPointName))
            dataPointValue = instance.GetDataPointValue(dataPointName) + ";" + valueToAdd;

        instance.AddDataPoint(dataPointName, dataPointValue);

        _instanceCollection.UpdateInstance(instance);

        return this;
    }

    /** Transforms and summarizes all raw data values.
     *
     * @return Transformed, summarized data instance collection
     * @throws Exception
     */
    public DataInstanceCollection GetTransformedSummarizedCollection(AbstractDataProcessor processor) throws Exception
    {
        Utilities.Log.Info(_instanceCollection.Size() + " raw data instances");
        Utilities.Log.Info(_instanceCollection.GetNumDataPoints() + " raw data points");

        DataInstanceCollection newCollection = new DataInstanceCollection();

        for (String dataPointName : _instanceCollection.GetDataPointNames())
            for (String instanceID : _instanceCollection.GetIDs())
                newCollection.Add(dataPointName, instanceID, GetTransformedSummarizedValue(dataPointName, instanceID, processor.GetDataPointMeta(dataPointName)));

        Utilities.Log.Info(_instanceCollection.Size() + " transformed/summarized data instances");
        Utilities.Log.Info(_instanceCollection.GetNumDataPoints() + " transformed/summarized data points");

        return newCollection;
    }

    private String GetTransformedSummarizedValue(String dataPointName, String instanceID, DataValueMeta meta) throws Exception
    {
        if (!_instanceCollection.Contains(instanceID))
            return Settings.MISSING_VALUE_STRING;

        ArrayList<String> values = Lists.CreateStringList(_instanceCollection.Get(instanceID).GetDataPointValue(dataPointName).split(";"));

        ArrayList<String> transformedValues = new ArrayList<String>();
        for (String value : values)
            if (!meta.IsNullValue(value))
                transformedValues.add(meta.Transformer.TransformValue(value));

        if (transformedValues.size() == 0)
            return Settings.MISSING_VALUE_STRING;

        if (transformedValues.size() == 1)
            return transformedValues.get(0);

        return meta.Summarizer.Summarize(transformedValues);
    }
}
