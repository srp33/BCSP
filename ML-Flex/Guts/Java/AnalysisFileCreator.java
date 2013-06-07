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

/** This class is used to transform data from the ML-Flex data format to the format that is required for external software components.
 * @author Stephen Piccolo
 */
public class AnalysisFileCreator
{
    /** These represent file extensions */
    public enum Extension
    {
        ARFF(".arff"),
        ORANGE(".tab"),
        //MLPY(".dat"),
        GCT(".gct"),
        CLS(".cls"),
        SURVIVAL("_survival.txt"),
        TAB(".tab"),
        C5NAMES(".names"),
        C5TRAINDATA(".data"),
        C5TESTDATA(".cases");

        private final String _extension;

        Extension(String extension)
        {
            _extension = extension;
        }

        @Override
        public String toString()
        {
            return _extension;
        }
    }

    private String _outputDir;
    private String _fileNamePrefix;
    private DataInstanceCollection _dataInstances;
    private DataInstanceCollection _otherInstances;
    private DataInstanceCollection _dependentVariableInstances;

    /** Constructor
     *
     * @param outputDirectory Absolute path of directory where files will be saved
     * @param fileNamePrefix Text that will be prepended to each file name that is saved
     * @param dataInstances Collection of data dataInstances
     * @param otherInstances Collection of other data instances that may be necessary for determining all options for a given data point (may be left null)
     * @param dependentVariableInstances Collection of dependent-variable dataInstances
     */
    public AnalysisFileCreator(String outputDirectory, String fileNamePrefix, DataInstanceCollection dataInstances, DataInstanceCollection otherInstances, DataInstanceCollection dependentVariableInstances)
    {
        _outputDir = outputDirectory;
        _fileNamePrefix = fileNamePrefix;
        _dataInstances = dataInstances;
        _otherInstances = otherInstances;
        _dependentVariableInstances = dependentVariableInstances;
    }

    private String GetDependentVariableValue(String instanceID) throws Exception
    {
        return _dependentVariableInstances.Get(instanceID).GetDataPointValue(0);
    }

    /** Generates files in the ARFF format.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateArffFile() throws Exception
    {
        String outFilePath = GetFilePath(Extension.ARFF);

        StringBuilder output = new StringBuilder();
        output.append("@relation thedata\n\n");

        ArrayList<String> dataPointNames = Lists.Sort(_dataInstances.GetDataPointNames());

        for (String dataPointName : dataPointNames)
        {
            HashSet<String> uniqueValues = new HashSet<String>(_dataInstances.GetUniqueValues(dataPointName));

            if (_otherInstances != null)
                uniqueValues.addAll(_otherInstances.GetUniqueValues(dataPointName));

            AppendArffAttribute(new ArrayList<String>(uniqueValues), dataPointName, output);
        }

        if (_dependentVariableInstances != null)
            AppendArffAttribute(Lists.Sort(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues()), Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName(), output);

        output.append("\n@data");

        for (DataValues instance : _dataInstances)
        {
            output.append("\n" + Lists.Join(FormatOutputValues(instance.GetDataPointValues(dataPointNames)), ","));

            if (_dependentVariableInstances != null)
                output.append("," + FormatOutputValue(GetDependentVariableValue(instance.GetID())));
        }

        Files.WriteTextToFile(outFilePath, output.toString());

        return this;
    }

    private void AppendArffAttribute(ArrayList<String> values, String dataPointName, StringBuilder output) throws Exception
    {
        output.append("@attribute " + dataPointName + " ");

        if (DataTypes.HasOnlyBinary(values))
            output.append("{" + Lists.Join(Lists.Sort(values), ",") + "}");
        else
        {
            if (DataTypes.HasOnlyNumeric(values))
                output.append("real");
            else
            {
                FormatOutputValues(values);
                output.append("{" + Lists.Join(Lists.Sort(values), ",") + "}");
            }
        }
        output.append("\n");
    }

    /** This method generates a basic tab-delimited file with variables as columns and instances as rows.
     * @param includeInstanceIDs Whether to include the ID of each instance in the file
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateTabDelimitedFile(boolean includeInstanceIDs) throws Exception
    {
        ArrayList<String> dataPoints = _dataInstances.GetDataPointNames();

        ArrayList<String> headerDataPoints = Utilities.UnformatNames(new ArrayList<String>(dataPoints));

        if (includeInstanceIDs)
            headerDataPoints.add(0, "ID");

        String filePath = GetTabDelimitedFilePath();
        Files.WriteLineToFile(filePath, Lists.Join(headerDataPoints, "\t") + (_dependentVariableInstances == null ? "" : "\t" + Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName()));

        for (DataValues instance : _dataInstances)
        {
            ArrayList<String> values = instance.GetDataPointValues(dataPoints);

            if (includeInstanceIDs)
                values.add(0, instance.GetID());

            if (_dependentVariableInstances != null)
                values.add(GetDependentVariableValue(instance.GetID()));

            values = Lists.ReplaceAllExactMatches(values, Settings.MISSING_VALUE_STRING, "NA");

            FormatOutputValues(values);

            Files.AppendTextToFile(filePath, Lists.Join(values, "\t") + "\n");
        }

        return this;
    }

//    public AnalysisFileCreator CreateGenePatternFiles() throws Exception
//    {
//        Files.WriteTextToFile(GetFilePath(Extension.GCT), "#1.2\n");
//        Files.AppendTextToFile(GetFilePath(Extension.GCT), String.valueOf(_dataInstances.GetDataPointNames().size()) + "\t" + _dataInstances.Size() + "\n");
//        Files.AppendTextToFile(GetFilePath(Extension.GCT), "Name\tDescription\t" + Strings.Join(_dataInstances.GetIDs(), "\t") + "\n");
//
//        for (String dataPointName : _dataInstances.GetDataPointNames())
//        {
//            String line = dataPointName + "\t" + dataPointName + "\t";
//            line += Strings.Join(FormatOutputValues(_dataInstances.GetValues(dataPointName).GetAllValues()), "\t");
//            Files.AppendTextToFile(GetFilePath(Extension.GCT), line + "\n");
//        }
//
//        if (_dataInstances.HasClass())
//        {
//            ArrayList<String> classes = Lists.GetUniqueValues(_dataInstances.GetClassValues());
//
//            String output = _dataInstances.Size() + " ";
//            output += classes.size() + " 1\n";
//            output += "# " + Strings.Join(classes, " ") + "\n";
//
//            for (DataValues patient : _dataInstances)
//                output += classes.indexOf(patient.GetClassValue()) + " ";
//
//            Files.AppendTextToFile(GetFilePath(Extension.CLS), output.trim() + "\n");
//        }
//
//        return this;
//    }

    /** This method generates a text file in the format required by the Orange machine-learning framework.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateOrangeFile() throws Exception
    {
        DataInstanceCollection instances = _dataInstances.Clone();

        String outFilePath = GetFilePath(Extension.ORANGE);
        ArrayList<String> dataPointNames = instances.GetDataPointNames();

        String header = Lists.Join(dataPointNames, "\t");
        header += _dependentVariableInstances != null ? "\t" + Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName() : "";
        header += "\n" + Lists.Join(GetOrangeAttributeHeader(instances, dataPointNames), "\t");
        header += _dependentVariableInstances != null ? "\td" : "";
        header += "\n" + Lists.Join(Lists.CreateStringList("", dataPointNames.size() + 1), "\t");
        header += _dependentVariableInstances != null ? "class" : "";
        header += "\n";

        Files.WriteTextToFile(outFilePath, header);

        for (DataValues instance : instances)
        {
            String line = Lists.Join(FormatOutputValues(instance.GetDataPointValues(dataPointNames)), "\t");

            if (_dependentVariableInstances != null)
                line += "\t" + FormatOutputValue(GetDependentVariableValue(instance.GetID()));

            Files.AppendTextToFile(outFilePath, line + "\n");
        }

        return this;
    }

    private ArrayList<String> GetOrangeAttributeHeader(DataInstanceCollection instances, ArrayList<String> dataPointNames)
    {
        ArrayList<String> results = new ArrayList<String>();

        for (String dataPointName : dataPointNames)
        {
            ArrayList<String> uniqueValues = instances.GetUniqueValues(dataPointName);
            results.add((DataTypes.HasOnlyNumeric(uniqueValues) && !DataTypes.HasOnlyBinary(uniqueValues)) ? "c" : "d");
        }

        return results;
    }

    /** This method creates text files in the format required by the C5.0 software. Specifically, it generates .names files.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateC5NamesFile() throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append(Lists.Join(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues(), ", ") + ".\n\n");

        for (String dataPointName : _dataInstances.GetDataPointNames())
        {
            output.append(dataPointName + ":\t");
            ArrayList<String> uniqueDataValues = _dataInstances.GetUniqueValues(dataPointName);

            if (DataTypes.HasOnlyNumeric(uniqueDataValues) && !DataTypes.HasOnlyBinary(uniqueDataValues))
                output.append("continuous");
            else
            {
                AnalysisFileCreator.FormatOutputValues(uniqueDataValues);
                output.append(Lists.Join(uniqueDataValues, ", "));
            }

            output.append(".\n");
        }

        Files.WriteTextToFile(GetC5NamesFilePath(), output.toString());

        return this;
    }

    /** This method creates text files in the format required by the C5.0 software. Specifically, it generates a .data file to be used for training a model.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateC5TrainDataFile() throws Exception
    {
        return CreateC5DataFile(GetC5TrainDataFilePath(), false);
    }

    /** This method creates text files in the format required by the C5.0 software. Specifically, it generates a .data file to be used for testing.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateC5TestDataFile() throws Exception
    {
        return CreateC5DataFile(GetC5TestDataFilePath(), true);
    }

    private AnalysisFileCreator CreateC5DataFile(String filePath, boolean areTestInstances) throws Exception
    {
        StringBuilder output = new StringBuilder();

        for (DataValues instance : _dataInstances)
        {
            ArrayList<String> values = instance.GetDataPointValues(_dataInstances.GetDataPointNames());
            values.add(areTestInstances ? "?" : _dependentVariableInstances.Get(instance.GetID()).GetDataPointValue(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName()));
            output.append(Lists.Join(values, ",") + "\n");
        }

        Files.WriteTextToFile(filePath, output.toString());

        return this;
    }

    /** This method creates a text file in the format required by the survival package in the R project software.
     * @return This instance
     * @throws Exception
     */
    public AnalysisFileCreator CreateSurvivalFile() throws Exception
    {
        ArrayList<String> predictors = _dataInstances.GetDataPointNames();

        Files.WriteTextToFile(GetFilePath(Extension.SURVIVAL), "case id\t" + Lists.Join(predictors, "\t") + "\tstatus\ttime\n");

        for (DataValues instance : _dataInstances)
        {
            ArrayList<String> predictorValues = instance.GetDataPointValues(predictors);
            predictorValues = Lists.ReplaceAllExactMatches(predictorValues, Settings.MISSING_VALUE_STRING, "NA");
            FormatOutputValues(predictorValues);

            Files.AppendTextToFile(GetFilePath(Extension.SURVIVAL), instance.GetID() + "\t" + Lists.Join(predictorValues, "\t") + "\t1\t" + GetDependentVariableValue(instance.GetID()) + "\n");
        }
        
        return this;
    }

    /** Deletes an analysis file that has already been created.
     * @param extension File extension
     * @throws Exception
     */
    public void DeleteFile(Extension extension) throws Exception
    {
        Files.DeleteFile(GetFilePath(extension));
    }

    /** Deletes an ARFF file that has already been created.
     * @throws Exception
     */
    public void DeleteArffFile() throws Exception
    {
        DeleteFile(Extension.ARFF);
    }

    /** Deletes an Orange file that has already been created.
     * @throws Exception
     */
    public void DeleteOrangeFile() throws Exception
    {
        DeleteFile(Extension.ORANGE);
    }

//    public void DeleteMlpyFile() throws Exception
//    {
//        DeleteFile(Extension.MLPY);
//    }

    /** Deletes a survival file that has already been created.
     * @throws Exception
     */
    public void DeleteSurvivalFile() throws Exception
    {
        DeleteFile(Extension.SURVIVAL);
    }

    /** Deletes a tab-delimited file that has already been created.
     *
     * @throws Exception
     */
    public void DeleteTabDelimitedFile() throws Exception
    {
        DeleteFile(Extension.TAB);
    }

    private String GetFilePath(Extension extension)
    {
        return _outputDir + _fileNamePrefix + extension.toString();
    }

    public String GetArffFilePath()
    {
        return GetFilePath(Extension.ARFF);
    }

    public String GetTabDelimitedFilePath()
    {
        return GetFilePath(Extension.TAB);
    }

    public String GetC5NamesFilePath()
    {
        return GetFilePath(Extension.C5NAMES);
    }

    public String GetC5TrainDataFilePath()
    {
        return GetFilePath(Extension.C5TRAINDATA);
    }

    public String GetC5TestDataFilePath()
    {
        return GetFilePath(Extension.C5TESTDATA);
    }

    public String GetOrangeFilePath()
    {
        return GetFilePath(Extension.ORANGE);
    }

//    public String GetMlpyFilePath()
//    {
//        return GetFilePath(Extension.MLPY);
//    }

    public String GetSurvivalFilePath()
    {
        return GetFilePath(Extension.SURVIVAL);
    }

    public static ArrayList<String> FormatOutputValues(ArrayList<String> values)
    {
        for (int i = 0; i < values.size(); i++)
            values.set(i, FormatOutputValue(values.get(i)));

        return values;
    }

    public static String FormatOutputValue(String value)
    {
        return value.replace(" ", "_");
    }

    /** Generates a file in the format required by the ROCR package in the R Project software.
     * @param predictions Predictions
     * @param filePath Absolute file path
     * @throws Exception
     */
    public static void WriteAucInputFile(Predictions predictions, String filePath) throws Exception
    {
        String output = "";

        for (Prediction prediction : predictions)
        {
            output += Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues().indexOf(prediction.DependentVariableValue) + "\t";
            output += Lists.Join(Lists.CreateStringList(prediction.ClassProbabilities), "\t");
            output += "\n";
        }

        Files.WriteTextToFile(filePath, output);
    }
}
