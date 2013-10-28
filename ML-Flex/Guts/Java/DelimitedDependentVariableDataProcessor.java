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

/** This class enables the user to import data directly from delimited (for example, by tabs or commas) text files. This class retrieves the dependent variable values from the final column in the file. The default delimiter is a tab.
 * @author Stephen Piccolo
 */
public class DelimitedDependentVariableDataProcessor extends AbstractDependentVariableDataProcessor
{
    private String _description;
    private ArrayList<String> _filePaths = new ArrayList<String>();
    private String _delimiter;
    private String _commentChar;
    private String _missingValueCharacter;

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative path (separated by commas) where the delimited file is stored (path is relative to the raw data directory)
     */
    public DelimitedDependentVariableDataProcessor(String description, String relativeFilePaths)
    {
        this(description, relativeFilePaths, "\t");
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative path (separated by commas) where the delimited file is stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     */
    public DelimitedDependentVariableDataProcessor(String description, String relativeFilePaths, String delimiter)
    {
        this(description, relativeFilePaths, delimiter, "#");
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative path (separated by commas) where the delimited file is stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     * @param commentChar Comment character (file lines starting with this character will be ignored)
     */
    public DelimitedDependentVariableDataProcessor(String description, String relativeFilePaths, String delimiter, String commentChar)
    {
        this(description, relativeFilePaths, delimiter, commentChar, Settings.MISSING_VALUE_STRING);
    }

    /** This constructor requires the user to specify a description that will be used and a relative file path indicating where the data file is stored.
     * @param description Description of the data stored in the delimited file (must be unique to this experiment)
     * @param relativeFilePaths Relative path (separated by commas) where the delimited file is stored (path is relative to the raw data directory)
     * @param delimiter Delimiter (use "\t" for tabs or "," for commas, etc.)
     * @param commentChar Comment character (file lines starting with this character will be ignored)
     * @param missingValueCharacter Missing value character (if this character is encountered in the file, it will be considered missing)
     */
    public DelimitedDependentVariableDataProcessor(String description, String relativeFilePaths, String delimiter, String commentChar, String missingValueCharacter)
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
    protected void ParseRawData() throws Exception
    {
        for (String filePath : _filePaths)
        {
            ArrayList<ArrayList<String>> fileLines = Files.ParseDelimitedFile(filePath, _delimiter, _commentChar);
            DelimitedDataProcessor.ValidateDelimitedFile(filePath, fileLines);

            ArrayList<String> instanceIDs = fileLines.remove(0);

            // Sometimes files don't include a tab or a column description for the row names
            if (instanceIDs.size() == fileLines.get(0).size())
                instanceIDs.remove(0);

            ArrayList<String> classes = fileLines.get(fileLines.size() - 1);
            String dataPointName = classes.remove(0);

            for (int i=0; i<instanceIDs.size(); i++)
            {
                String x = classes.get(i);
                if (!x.equals(_missingValueCharacter))
                    SaveRawDataPoint(dataPointName, instanceIDs.get(i), x);
            }
        }
    }

    @Override
    public String GetDependentVariableDataPointName() throws Exception
    {
        ArrayList<ArrayList<String>> fileLines = Files.ParseDelimitedFile(_filePaths.get(0));
        DelimitedDataProcessor.ValidateDelimitedFile(_filePaths.get(0), fileLines);

        return fileLines.get(fileLines.size() - 1).get(0);
    }
}