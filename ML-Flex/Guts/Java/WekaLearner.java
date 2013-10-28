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

/** This class interfaces directly with the application programming interface of the Weka machine-learning software package. The Weka JAR file is packaged with ML-Flex, so this learner should work out of the box.
 * @author Stephen Piccolo
 */
public class WekaLearner extends AbstractMachineLearner
{
    @Override
    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        AnalysisFileCreator fileCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, Utilities.GetUniqueID(), trainData, null, dependentVariableInstances).CreateArffFile();
        String arffFilePath = fileCreator.GetArffFilePath();

        ArrayList<String> commandArgs = Lists.CreateStringList("java");
        ArrayList<String> parameters = Lists.CreateStringList("-classpath", ".:lib/weka.jar");
        parameters.addAll(algorithmParameters);
        parameters.add("-i");
        parameters.add(arffFilePath);

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters);
        Files.DeleteFile(arffFilePath);

        String output = CommandLineClient.GetCommandResult(results, CommandLineClient.OUTPUT_RESULTS_KEY);
        ArrayList<String> outputLines = Lists.CreateStringList(output.split("\n"));

        ArrayList<String> dataPointNames = Lists.Sort(trainData.GetDataPointNames());

        for (String line : outputLines)
        {
            if (line.startsWith("Selected attributes:"))
            {
                String rawSelectedAttributes = line.replace("Selected attributes: ", "");
                rawSelectedAttributes = rawSelectedAttributes.substring(0, rawSelectedAttributes.indexOf(":") - 1);
                ArrayList<Integer> selectedAttributeIndices = Lists.CreateIntegerList(Lists.CreateStringList(rawSelectedAttributes.split(",")));
                ArrayList<String> selectedAttributes = Lists.Get(dataPointNames, MathUtility.Add(selectedAttributeIndices, -1));

                return selectedAttributes;
             }
        }

        throw new Exception("Weka found no selected attributes. Output: " + output);
    }

    @Override
    protected ModelPredictions TrainTest(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        AnalysisFileCreator trainingFileCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, Utilities.GetUniqueID(), trainData, testData, dependentVariableInstances).CreateArffFile();
        String trainingArffFilePath = trainingFileCreator.GetArffFilePath();

        AnalysisFileCreator testFileCreator = new AnalysisFileCreator(Settings.TEMP_DATA_DIR, Utilities.GetUniqueID(), testData, trainData, dependentVariableInstances).CreateArffFile();
        String testArffFilePath = testFileCreator.GetArffFilePath();

        ArrayList<String> commandArgs = Lists.CreateStringList("java");
        ArrayList<String> parameters = Lists.CreateStringList("-classpath", ".:lib/weka.jar:lib/libsvm.jar");
        parameters.addAll(algorithmParameters);
        parameters.add("-t");
        parameters.add(trainingArffFilePath);
        parameters.add("-T");
        parameters.add(testArffFilePath);
        parameters.add("-p");
        parameters.add("0");
        parameters.add("-distribution");

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters);
        Files.DeleteFile(trainingArffFilePath);
        Files.DeleteFile(testArffFilePath);

        String output = CommandLineClient.GetCommandResult(results, CommandLineClient.OUTPUT_RESULTS_KEY);
        ArrayList<String> rawOutputLines = Lists.CreateStringList(output.split("\n"));

        ArrayList<String> outputLines = new ArrayList<String>();
        for (String line : rawOutputLines)
            if ((outputLines.size() > 0 || line.contains("inst#     actual  predicted error distribution")) && line.length() > 0)
                outputLines.add(line);
        outputLines.remove(0);

        ArrayList<String> dependentVariableValues = Lists.Sort(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues());

        Predictions predictions = new Predictions();

        for (int i=0; i<outputLines.size(); i++)
        {
            ArrayList<String> lineItems = Lists.CreateStringList(outputLines.get(i).trim().split("\\s+"));

            String rawProbabilities = lineItems.get(lineItems.size() - 1);
            ArrayList<String> rawProbabilitiesList = Lists.CreateStringList(rawProbabilities.split(","));

            int predictedClassIndex = -1;
            for (int j=0; j<rawProbabilitiesList.size(); j++)
                if (rawProbabilitiesList.get(j).startsWith("*"))
                    predictedClassIndex = j;

            rawProbabilitiesList.set(predictedClassIndex, rawProbabilitiesList.get(predictedClassIndex).substring(1));

            ArrayList<Double> probabilities = Lists.CreateDoubleList(rawProbabilitiesList);
            String predictedClass = dependentVariableValues.get(predictedClassIndex);

            String testInstanceID = testData.Get(i).GetID();
            predictions.Add(new Prediction(testInstanceID, dependentVariableInstances.Get(testInstanceID).GetDataPointValue(0), predictedClass, probabilities));
        }



        return new ModelPredictions("", predictions);
    }
}