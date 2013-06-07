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

/** This class provides functionality for interfacing with the C5.0 Decision Trees software package.
 * @author Stephen Piccolo
 */
public class C5Learner extends AbstractMachineLearner
{
    @Override
    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        throw new Exception("Not yet implemented");
    }

    @Override
    protected ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        String uniqueID = Utilities.GetUniqueID();

        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, dependentVariableInstances).CreateC5NamesFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, trainingData, trainingData, dependentVariableInstances).CreateC5TrainDataFile();
        new AnalysisFileCreator(Settings.TEMP_DATA_DIR, uniqueID, testData, trainingData, dependentVariableInstances).CreateC5TestDataFile();

        String tempFileDescription = Settings.TEMP_DATA_DIR + uniqueID;

        ArrayList<String> commandArgs = Lists.CreateStringList(classificationParameters.get(0).replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR).replace("{Settings.GUTS_DIR}", Settings.GUTS_DIR));
        ArrayList<String> parameters = Lists.CreateStringList(classificationParameters.get(1), classificationParameters.get(2).replace("{fileDescription}", tempFileDescription));

        CommandLineClient.RunAnalysis(commandArgs, parameters);

        commandArgs = Lists.CreateStringList(classificationParameters.get(3).replace("{Settings.MAIN_DIR}", Settings.MAIN_DIR).replace("{Settings.GUTS_DIR}", Settings.GUTS_DIR));
        parameters = Lists.CreateStringList(classificationParameters.get(4), classificationParameters.get(5).replace("{fileDescription}", tempFileDescription));

        HashMap<String, String> results = CommandLineClient.RunAnalysis(commandArgs, parameters);
        String output = CommandLineClient.GetCommandResult(results, CommandLineClient.OUTPUT_RESULTS_KEY);
        
        Files.DeleteFilesInDirectory(Settings.TEMP_DATA_DIR, uniqueID + ".*");

        ArrayList<String> outputLines = Lists.CreateStringList(CommandLineClient.GetCommandResult(results, CommandLineClient.OUTPUT_RESULTS_KEY).split("\n"));
        outputLines.remove(0);
        outputLines.remove(0);
        outputLines.remove(0);

        Predictions predictions = new Predictions();

        for (int i = 0; i < outputLines.size(); i++)
        {
            String prediction = outputLines.get(i).split("\\s+")[3];
            double confidence = Double.parseDouble(outputLines.get(i).split("\\s+")[4].replace("[", "").replace("]", ""));

            DataValues instance = testData.Get(i);

            ArrayList<String> dependentVariableOptions = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
            ArrayList<Double> classProbabilities = new ArrayList<Double>();

            //It uses the confidence value assigned by C5.0 for the predicted class and splits the remaining confidence equally across the other classes (for lack of a better solution).
            for (String dependentVariableValue : dependentVariableOptions)
                classProbabilities.add(prediction.equals(dependentVariableValue) ? confidence : ((1 - confidence) / ((double)(dependentVariableOptions.size()-1))));

            predictions.Add(new Prediction(instance.GetID(), dependentVariableInstances.Get(instance.GetID()).GetDataPointValue(0), prediction, classProbabilities));
        }

        return new ModelPredictions(output, predictions);
    }
}