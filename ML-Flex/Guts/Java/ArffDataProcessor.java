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

/** This data processor class is designed to parse text files in the ARFF format.
 * @author Stephen Piccolo
 */
public class ArffDataProcessor extends AbstractDataProcessor
{
    private String _description;
    private ArrayList<String> _filePaths = new ArrayList<String>();

    /** This constructor accepts a description of the data in the ARFF file as well as the relative path to the file.
     * @param description Description of the data in the ARFF file
     * @param relativeFilePaths Relative paths (separated by a vertical bar) where the files are located (under the raw data directory)
     */
    public ArffDataProcessor(String description, String relativeFilePaths)
    {
        _description = description;

        for (String relativeFilePath : relativeFilePaths.split("\\|"))
            _filePaths.add(Settings.RAW_DATA_DIR + relativeFilePath);
    }

    @Override
    public String GetDescription()
    {
        return _description;
    }

    @Override
    protected double GetProportionMissingPerInstanceOK()
    {
        return 1.00;
    }

    @Override
    protected double GetProportionMissingPerDataPointOK()
    {
        return 1.00;
    }

    @Override
    protected void ParseRawData() throws Exception
    {
        int overallInstanceCount = 0;

        for (String filePath : _filePaths)
        {
            ArrayList<String> fileLines = Files.ReadLinesFromFile(filePath, "%");
            ArrayList<String> metaRows = Lists.GetValuesStartingWith(fileLines, "@");
            ArrayList<String> dataRows = Lists.RemoveAll(fileLines, metaRows);
            metaRows = Lists.Replace(metaRows, "\t", " ");

            if (dataRows.size() == 0)
                throw new Exception("No data rows could be identified in " + filePath + ".");

            ArrayList<String> attributeNames = ParseAttributeNames(metaRows, filePath);

            int idIndex = Lists.ToLowerCase(attributeNames).indexOf("id");

            for (int i=0; i<dataRows.size(); i++)
            {
                overallInstanceCount++;

                ArrayList<String> dataRowItems = Lists.CreateStringList(dataRows.get(i).trim().split(","));
                String instanceID = idIndex == -1 ? "Instance" + overallInstanceCount : dataRowItems.get(idIndex);

                for (int j=0; j<dataRowItems.size() - 1; j++)
                    if (j != idIndex)
                        SaveRawDataPoint(attributeNames.get(j), instanceID, dataRowItems.get(j));
            }
        }
    }

    /** Parses attribute names from the metadata rows in an ARFF file.
     *
     * @param metaRows Metadata rows (those that start with @)
     * @param filePath Path where the ARFF file was stored
     * @return Attribute names
     * @throws Exception
     */
    public static ArrayList<String> ParseAttributeNames(ArrayList<String> metaRows, String filePath) throws Exception
    {
        ArrayList<String> attributeNames = new ArrayList<String>();

        for (String metaRow : metaRows)
        {
            ArrayList<String> metaRowItems = Lists.CreateStringList(metaRow.split(" "));
            metaRowItems = Lists.RemoveAll(metaRowItems, " ");

            String descriptor = metaRowItems.get(0).toLowerCase();

            if (!descriptor.equals("@attribute"))
                continue;

            String attributeName = metaRowItems.get(1).trim();
            if (!attributeName.equals("class"))
                attributeNames.add(attributeName);
        }

        if (attributeNames.size() == 0 || (attributeNames.size() == 1 && attributeNames.get(0).toLowerCase().equals("id")))
            throw new Exception("No attributes could be identified in " + filePath + ".");

        return attributeNames;
    }
}