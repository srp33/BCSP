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

/** This class can be used to randomly select features or to randomly assign instances to a given class. This can be used for validation purposes.

 @author Stephen Piccolo
 */
public class RandomMachineLearner extends AbstractMachineLearner
{
    @Override
    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        ArrayList<String> features = Lists.Shuffle(trainData.GetDataPointNames());

        if (algorithmParameters.size() > 0)
            features = Lists.Subset(features, 0, Integer.parseInt(algorithmParameters.get(0))); // You can specify how many features to select randomly
        
        return features;
    }

    @Override
    protected ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        DataInstanceCollection testDependentVariableInstances = dependentVariableInstances.Get(testData.GetIDs());
        String dependentVariableName = Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName();

        ArrayList<String> randomTestDataDependentVariableValues = Lists.Shuffle(testDependentVariableInstances.GetDataPointValues(dependentVariableName).GetAllValues());

        Predictions predictions = new Predictions();

        for (int i=0; i<testData.Size(); i++)
        {
            String instanceID = testData.Get(i).GetID();
            String predictedClass = randomTestDataDependentVariableValues.get(i);
            String actualClass = testDependentVariableInstances.Get(instanceID).GetDataPointValue(dependentVariableName);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (String dependentVariableValue : Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())
            {
                if (predictedClass.equals(dependentVariableValue))
                    classProbabilities.add(1.0);
                else
                    classProbabilities.add(0.0);
            }

            predictions.Add(new Prediction(instanceID, actualClass, predictedClass, classProbabilities));
        }

        return new ModelPredictions("", predictions);
    }
}
