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

import mlflex.AbstractDependentVariableDataProcessor;
import mlflex.DataValues;

public class UciMachineLearningDependentVariableDataProcessor extends AbstractDependentVariableDataProcessor
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
    public UciMachineLearningDependentVariableDataProcessor(String description, String fileName, Integer idIndex, Integer classIndex)
    {
        this(description, fileName, idIndex, classIndex, ",");
    }

    /** Constructor
     * @param description Description of the data set
     * @param fileName Absolute path to file containing dependent-variable data
     * @param idIndex Index of column containing identifiers that identify each data instance (when none exists, this should be -1)
     * @param classIndex Index of column containing class information
     * @param delimiter Character that delimits data columns
     */
    public UciMachineLearningDependentVariableDataProcessor(String description, String fileName, Integer idIndex, Integer classIndex, String delimiter)
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
    protected void ParseRawData() throws Exception
    {
        for (DataValues instance : new UciDelimitedFileParser(GetRawDataDir() + _fileName, _idIndex, _classIndex, _delimiter).ParseDependentVariableValues(GetDependentVariableDataPointName()))
            SaveRawDataPoint(GetDependentVariableDataPointName(), instance.GetID(), instance.GetDataPointValue(GetDependentVariableDataPointName()));
    }

    @Override
    public String GetDependentVariableDataPointName()
    {
        return "DependentVariable";
    }
}