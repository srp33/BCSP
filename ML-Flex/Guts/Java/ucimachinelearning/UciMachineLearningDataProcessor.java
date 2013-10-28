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

package mlflex.ucimachinelearning;

import mlflex.AbstractDataProcessor;
import mlflex.DataInstanceCollection;
import mlflex.DataValues;

public class UciMachineLearningDataProcessor extends AbstractDataProcessor
{
    private String _description;
    private String _fileName;
    private int _idIndex;
    private int _classIndex;
    private String _delimiter;

    /** Constructor
     * @param description Description of the data set
     * @param fileName Name of file containing data (this file should reside within the RawData/UCI directory)
     * @param idIndex Index of column containing identifiers that identify each data instance (when none exists, this should be -1)
     * @param classIndex Index of column containing class information
     */
    public UciMachineLearningDataProcessor(String description, String fileName, Integer idIndex, Integer classIndex)
    {
        this(description, fileName, idIndex, classIndex, ",");
    }

    /** Constructor
     * @param description Description of the data set
     * @param fileName Name of file containing data (this file should reside within the RawData/UCI directory)
     * @param idIndex Index of column containing identifiers that identify each data instance (when none exists, this should be -1)
     * @param classIndex Index of column containing class information
     * @param delimiter Character/string that is used in the file to delimit entries
     */
    public UciMachineLearningDataProcessor(String description, String fileName, Integer idIndex, Integer classIndex, String delimiter)
    {
        _description = description;
        _fileName = fileName;
        _idIndex = idIndex;
        _classIndex = classIndex;
        _delimiter = delimiter.equals("") ? " " : delimiter;
    }

    @Override
    public String GetDescription()
    {
        return _description;
    }

    @Override
    protected String GetRawDataDirName()
    {
        return "UCI";
    }

    @Override
    protected double GetProportionMissingPerInstanceOK()
    {
        return 0.99;
    }

    @Override
    protected double GetProportionMissingPerDataPointOK()
    {
        return 0.99;
    }

    @Override
    protected void ParseRawData() throws Exception
    {
        DataInstanceCollection rawInstances = new UciDelimitedFileParser(GetRawDataDir() + _fileName, _idIndex, _classIndex, _delimiter).ParseDataValues();

        DataInstanceCollection parsedInstances = new DataInstanceCollection();

        for (DataValues instance : rawInstances)
            for (String dataPointName : instance)
                parsedInstances.Add(dataPointName, instance.GetID(), instance.GetDataPointValue(dataPointName));

        for (DataValues instance : ConvertMultiValuedDataPointsToBinary(parsedInstances))
            for (String dataPointName : instance.GetDataPointNames())
                SaveRawDataPoint(dataPointName, instance.GetID(), instance.GetDataPointValue(dataPointName));
    }
}