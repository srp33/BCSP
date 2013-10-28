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

/** This class enables the user to import data directly from delimited (for example, by tabs or commas) text files. This class ignores the final column in the file (which it assumes contains the dependent variable values). The default delimiter is a tab.
 * @author Stephen Piccolo
 */
public class DelimitedDataProcessor extends AbstractDataProcessor
{
    private String _description;
    private ArrayList<String> _filePaths = new ArrayList<String>();
    private String _delimiter;
    private String _commentChar;
    private String _missingValueCharacter;

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative paths (separated by commas) where the delimited files are stored (path is relative to the raw data directory)
     */
    public DelimitedDataProcessor(String description, String relativeFilePaths)
    {
        this(description, relativeFilePaths, "\t");
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative paths (separated by commas) where the delimited files are stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     */
    public DelimitedDataProcessor(String description, String relativeFilePaths, String delimiter)
    {
        this(description, relativeFilePaths, delimiter, "#");
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative paths (separated by commas) where the delimited files are stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     * @param commentChar Comment character (file lines starting with this character will be ignored)
     */
    public DelimitedDataProcessor(String description, String relativeFilePaths, String delimiter, String commentChar)
    {
        this(description, relativeFilePaths, delimiter, commentChar, Settings.MISSING_VALUE_STRING);
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative paths (separated by commas) where the delimited files are stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     * @param commentChar Comment character (file lines starting with this character will be ignored)
     * @param missingValueCharacter Missing value character (if this character is encountered in the file, it will be considered missing)
     */
    public DelimitedDataProcessor(String description, String relativeFilePaths, String delimiter, String commentChar, String missingValueCharacter)
    {
        _description = description;

        for (String relativeFilePath : relativeFilePaths.split("\\|"))
            _filePaths.add(Settings.RAW_DATA_DIR + relativeFilePath);

        _delimiter = delimiter;
        _commentChar = commentChar;
        _missingValueCharacter = missingValueCharacter;
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
        for (String filePath : _filePaths)
        {
            ArrayList<ArrayList<String>> fileLines = Files.ParseDelimitedFile(filePath, _delimiter, _commentChar);
            ValidateDelimitedFile(filePath, fileLines);

            ArrayList<String> instanceIDs = fileLines.remove(0);

            // Sometimes files don't include a tab or a column description for the row names
            if (instanceIDs.size() == fileLines.get(0).size())
                instanceIDs.remove(0);

            for (int i=0; i<fileLines.size()-1; i++)
            {
                ArrayList<String> rowValues = fileLines.get(i);
                String dataPointName = rowValues.remove(0);

                for (int j=0; j<rowValues.size(); j++)
                {
                    String value = rowValues.get(j);

                    if (!value.equals(_missingValueCharacter))
                        SaveRawDataPoint(dataPointName, instanceIDs.get(j), rowValues.get(j));
                }
            }
        }
    }

    /** Checks to make sure a delimited file has a valid structure before it is parsed.
     *
     * @param filePath Absolute path to file
     * @param fileLines Values in each file line
     * @throws Exception
     */
    public static void ValidateDelimitedFile(String filePath, ArrayList<ArrayList<String>> fileLines) throws Exception
    {
        if (fileLines.size() <= 1)
            throw new Exception("The file located at " + filePath + " has no data.");

        for (int i=1; i<fileLines.size(); i++)
            if (fileLines.get(i).size() != fileLines.get(0).size() && fileLines.get(i).size() != fileLines.get(0).size() + 1)
                throw new Exception("Line " + i + " (after any comment characters were removed) in " + filePath + " does not have the same number of values as the number of instances.");
    }
}