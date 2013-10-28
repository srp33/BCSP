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
import java.util.HashMap;
import java.util.HashSet;

/** This class contains utility methods for interacting with the R Project software via command-line calls.
 * @author Stephen Piccolo
 */
public class RLearner extends AbstractMachineLearner
{
    @Override
    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        throw new Exception("Not implemented");
    }

    @Override
    protected ModelPredictions TrainTest(ArrayList<String> algorithmParameters, DataInstanceCollection trainingData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        CheckDataTypes(trainingData, testData);

        AnalysisFileCreator trainingCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "RTrain_" + Utilities.GetUniqueID(), trainingData, testData, dependentVariableInstances);
        trainingCreator.CreateTabDelimitedFile(false);
        AnalysisFileCreator testCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "RTest_" + Utilities.GetUniqueID(), testData, trainingData, null);
        testCreator.CreateTabDelimitedFile(false);

        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + Utilities.GetUniqueID() + "/";
        Files.CreateDirectoryIfNotExists(outputDirectoryPath);

        String outputFileName = "Output_" + Utilities.GetUniqueID() + ".txt";

        ArrayList<String> commandArgs = Lists.CreateStringList(algorithmParameters.get(0), "--vanilla", algorithmParameters.get(1).replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR).replace("{Settings.GUTS_DIR}", Settings.GUTS_DIR));

        ArrayList<String> parameters = Lists.Subset(algorithmParameters, 2, algorithmParameters.size());
        parameters.add(trainingCreator.GetTabDelimitedFilePath());
        parameters.add(testCreator.GetTabDelimitedFilePath());
        parameters.add(outputDirectoryPath + outputFileName);

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters, outputDirectoryPath);

        trainingCreator.DeleteTabDelimitedFile();
        testCreator.DeleteTabDelimitedFile();

        String outputText = CommandLineClient.GetCommandResult(results, outputFileName);

        return new ModelPredictions("", ParsePredictions(outputText, testData, dependentVariableInstances));
    }

    private Predictions ParsePredictions(String outputText, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        ArrayList<String> outputLines = Lists.CreateStringList(outputText.split("\n"));
        ArrayList<String> headerItems = Lists.CreateStringList(outputLines.remove(0).split("\t"));

        Predictions predictions = new Predictions();

        for (int i=0; i<testData.Size(); i++)
        {
            DataValues testInstance = testData.Get(i);
            ArrayList<String> outputItems = Lists.CreateStringList(outputLines.get(i).split("\t"));
            String predictedClass = outputItems.get(0);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (String dependentVariableValue : Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())
                classProbabilities.add(Double.parseDouble(outputItems.get(headerItems.indexOf(dependentVariableValue))));

            predictions.Add(new Prediction(testInstance.GetID(), dependentVariableInstances.Get(testInstance.GetID()).GetDataPointValue(0), predictedClass, classProbabilities));
        }
        return predictions;
    }

    private void CheckDataTypes(DataInstanceCollection trainingData, DataInstanceCollection testData) throws Exception
    {
        for (String dataPointName : trainingData.GetDataPointNames())
        {
            HashSet<String> uniqueValues = new HashSet<String>(trainingData.GetUniqueValues(dataPointName));
            uniqueValues.addAll(testData.GetUniqueValues(dataPointName));

            if (DataTypes.GetGeneralDataType(new ArrayList<String>(uniqueValues)).equals(GeneralDataType.Nominal))
            {
                if (uniqueValues.size() == 2)
                {
                    String oneOption = Lists.SortStringList(new ArrayList<String>(uniqueValues)).get(0);
                    trainingData.BinarizeDataPoint(dataPointName, oneOption);
                    testData.BinarizeDataPoint(dataPointName, oneOption);
                    continue;
                }

                if (uniqueValues.size() > 2)
                    throw new Exception("The " + this.getClass().getName() + " class is not equipped to handle discrete variables with more than two value options.");
            }
        }
    }
}