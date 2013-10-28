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

import mlflex.DataInstanceCollection;
import mlflex.DataValues;
import mlflex.Files;
import mlflex.Lists;

import java.util.ArrayList;

/** This file is used to parse the raw data files from the UCI Machine Learning Repository. These files follow a fairly standard format, but this class allows custom handling of the files.
 * @author Stephen Piccolo
 */
public class UciDelimitedFileParser
{
    private String _filePath;
    private int _idIndex;
    private int _classIndex;
    private String _delimiter;

    /** Constructor
     *
     * @param filePath Absolute path to file containing data
     * @param idIndex Index of the column in the file that contains the data instance IDs
     * @param classIndex Index of the column in the file that contains the class values
     * @param delimiter Delimiter separating values in the file
     */
    public UciDelimitedFileParser(String filePath, int idIndex, int classIndex, String delimiter)
    {
        _filePath = filePath;
        _idIndex = idIndex;
        _classIndex = classIndex;
        _delimiter = delimiter;
    }

    /** This method parses the data from the raw files and saves the data into a collection of data instances.
     *
     * @return Collection of data instances
     * @throws Exception
     */
    public DataInstanceCollection ParseDataValues() throws Exception
    {
        DataInstanceCollection instances = new DataInstanceCollection();

        ArrayList<ArrayList<String>> data = Files.ParseDelimitedFile(_filePath);
        for (int i=0; i<data.size(); i++)
        {
            ArrayList<String> row = GetRow(data, i);
            DataValues instance = new DataValues(GetID(i, row));

            for (int j=0; j<row.size(); j++)
                if (j != _idIndex && j != _classIndex)
                    instance.AddDataPoint("DataPoint" + j, row.get(j));

            instances.Add(instance);
        }

        return instances;
    }

    /** This method parses the dependent-variable data from the raw files and saves the data into a collection of data instances.
     *
     * @param dependentVariableDataPointName
     * @return Collection of dependent-variable instances
     * @throws Exception
     */
    public DataInstanceCollection ParseDependentVariableValues(String dependentVariableDataPointName) throws Exception
    {
        DataInstanceCollection instances = new DataInstanceCollection();

        ArrayList<ArrayList<String>> data = Files.ParseDelimitedFile(_filePath);
        for (int i=0; i<data.size(); i++)
        {
            ArrayList<String> row = GetRow(data, i);
            DataValues instance = new DataValues(GetID(i, row));

            String dependentVariableClass = "Class_" + row.get(_classIndex);
            instance.AddDataPoint(dependentVariableDataPointName, dependentVariableClass);
            instances.Add(instance);
        }

        return instances;
    }

    private ArrayList<String> GetRow(ArrayList<ArrayList<String>> data, int i)
    {
        return Lists.CreateStringList(data.get(i).get(0).split(_delimiter));
    }

    private String GetID(int i, ArrayList<String> rowValues)
    {
        return _idIndex == -1 ? "ID" + i : rowValues.get(_idIndex);
    }
}