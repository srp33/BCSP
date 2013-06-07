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

/** This class represents an ensemble/combiner approach that computes the combined prediction according to the class probabilities that were assigned for each individual prediction. This particular approach uses the maximum across the probabilities to compute the combined prediction.
 * @author Stephen Piccolo
 */
public class MaxProbabilityEnsemblePredictor extends AbstractEnsemblePredictor
{
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();

        double highestProbability = GetHighestProbability(combinedPredictionInfos.GetOuterPredictions());
        ArrayList<String> predictedClasses = GetPredictedClasses(combinedPredictionInfos.GetOuterPredictions(), highestProbability, classes);
        String predictedClass = Lists.PickRandomValue(predictedClasses);

        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        for (int i=0; i<classes.size(); i++)
            classProbabilities.add(MathUtility.Max(GetClassProbabilities(combinedPredictionInfos.GetOuterPredictions(), i)));

        double totalMaxProbabilities = MathUtility.Sum(classProbabilities);

        for (int i=0; i<classProbabilities.size(); i++)
            classProbabilities.set(i, classProbabilities.get(i) / totalMaxProbabilities);

        Prediction prediction = new Prediction(instanceID, Utilities.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);

        return new ModelPrediction(MajorityVoteEnsemblePredictor.GetDescription(predictedClass, classProbabilities), prediction);
    }

    private ArrayList<Double> GetClassProbabilities(Predictions predictions, int probabilityIndex)
    {
        ArrayList<Double> classProbabilities = new ArrayList<Double>();

        for (Prediction prediction : predictions)
            classProbabilities.add(prediction.ClassProbabilities.get(probabilityIndex));

        return classProbabilities;
    }

    private double GetHighestProbability(Predictions predictions) throws Exception
    {
        double highestProbability = -0.01;

        for (Prediction prediction : predictions)
            for (double classProbability : prediction.ClassProbabilities)
                if (classProbability > highestProbability)
                    highestProbability = classProbability;

        return highestProbability;
    }

    private ArrayList<String> GetPredictedClasses(Predictions predictions, double highestProbability, ArrayList<String> classes) throws Exception
    {
        ArrayList<String> predictedClasses = new ArrayList<String>();

        for (Prediction prediction : predictions)
            for (int i=0; i< prediction.ClassProbabilities.size(); i++)
            {
                double classProbability = prediction.ClassProbabilities.get(i);
                if (classProbability == highestProbability)
                    predictedClasses.add(classes.get(i));

            }

        return predictedClasses;
    }
}
