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

/** This class provides an interface into the Orange machine-learning software package. Using this interface, Orange can be used for feature selection and classification. Please see the README files for information about how to install and configure Orange on the computer where ML-Flex is being executed.
 * @author Stephen Piccolo
 */
public class OrangeLearner extends AbstractMachineLearner
{
    @Override
    public ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        AnalysisFileCreator creator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeDataForRanking_" + Utilities.GetUniqueID(), trainData, null, dependentVariableInstances);

        String dataFilePath = creator.CreateOrangeFile().GetOrangeFilePath();
        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + Utilities.GetUniqueID() + "/";
        String outputFileName = "RankedFeatures_" + Utilities.GetUniqueID() + ".txt";

        Files.CreateDirectoryIfNotExists(outputDirectoryPath);

        ArrayList<String> commandArgs = Lists.CreateStringList(algorithmParameters.get(0), algorithmParameters.get(1).replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR).replace("{Settings.GUTS_DIR}", Settings.GUTS_DIR));

        ArrayList<String> parameters = Lists.Subset(algorithmParameters, 2, algorithmParameters.size());
        parameters.add(dataFilePath);
        parameters.add(outputDirectoryPath + outputFileName);

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters, outputDirectoryPath);

        ArrayList<String> features = Lists.CreateStringList(CommandLineClient.GetCommandResult(results, outputFileName).split("\n"));

        Files.DeleteFile(dataFilePath);
        Files.DeleteFile(outputDirectoryPath + outputFileName);

        return features;
    }

    @Override
    public ModelPredictions TrainTest(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        AnalysisFileCreator trainingCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeTrain_" + Utilities.GetUniqueID(), trainData, testData, dependentVariableInstances);
        trainingCreator.CreateOrangeFile();
        AnalysisFileCreator testCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, "OrangeTest_" + Utilities.GetUniqueID(), testData, trainData, null);
        testCreator.CreateOrangeFile();

        String outputDirectoryPath = Settings.TEMP_RESULTS_DIR + Utilities.GetUniqueID() + "/";
        String predictionsFileName = "Predictions_" + Utilities.GetUniqueID();
        String probabilitiesFileName = "Probabilities_" + Utilities.GetUniqueID();

        Files.CreateDirectoryIfNotExists(outputDirectoryPath);

        ArrayList<String> commandArgs = Lists.CreateStringList(algorithmParameters.get(0), algorithmParameters.get(1).replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR).replace("{Settings.GUTS_DIR}", Settings.GUTS_DIR));

        ArrayList<String> parameters = Lists.Subset(algorithmParameters, 2, algorithmParameters.size());
        parameters.add(trainingCreator.GetOrangeFilePath());
        parameters.add(testCreator.GetOrangeFilePath());
        parameters.add(outputDirectoryPath + predictionsFileName);
        parameters.add(outputDirectoryPath + probabilitiesFileName);

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters, outputDirectoryPath);

        String predictionText = CommandLineClient.GetCommandResult(results, predictionsFileName);
        String probabilityText = CommandLineClient.GetCommandResult(results, probabilitiesFileName);

        ArrayList<String> predictionLines = Lists.CreateStringList(predictionText.trim().split("\n"));

        ArrayList<String> probabilityLines = Lists.CreateStringList(probabilityText.trim().split("\n"));
        ArrayList<String> probabilityClasses = Lists.CreateStringList(probabilityLines.remove(0).split("\t"));

        Predictions predictions = new Predictions();

        for (DataValues testInstance : testData)
        {
            String actual = dependentVariableInstances.Get(testInstance.GetID()).GetDataPointValue(0);
            String prediction = predictionLines.remove(0);
            ArrayList<String> probabilities = Lists.CreateStringList(probabilityLines.remove(0).trim().split("\t"));

            ArrayList<Double> classProbabilities = new ArrayList<Double>();

            for (String x : Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())
                classProbabilities.add(ParseProbability(probabilityClasses, probabilities, x, prediction));

            predictions.Add(new Prediction(testInstance.GetID(), actual, prediction, classProbabilities));
        }

        trainingCreator.DeleteOrangeFile();
        testCreator.DeleteOrangeFile();

        return new ModelPredictions(CommandLineClient.GetCommandResult(results, CommandLineClient.OUTPUT_RESULTS_KEY), predictions);
    }

    private double ParseProbability(ArrayList<String> probabilityClasses, ArrayList<String> probabilities, String classDescriptor, String predictedClass)
    {
        if (!probabilityClasses.contains(classDescriptor))
            return 0.0;

        String rawProbability = probabilities.get(probabilityClasses.indexOf(classDescriptor));

        if (rawProbability.equals("nan"))
        {
            if (classDescriptor.equals(predictedClass))
                return 1.0;
            return 0.0;
        }

        return Double.parseDouble(rawProbability);
    }
}
