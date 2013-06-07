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

/** This class represents an ensemble/combiner approach that forms an aggregate prediction based on a majority vote on the individual predictions.
 * @author Stephen Piccolo
 */
public class MajorityVoteEnsemblePredictor extends AbstractEnsemblePredictor
{
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos predictionInfos) throws Exception
    {
        return Vote(instanceID, predictionInfos.GetOuterPredictions());
    }

    private static ModelPrediction Vote(String instanceID, Predictions predictions) throws Exception
    {
        if (predictions.Size() == 0)
            throw new Exception("No votes were cast for " + instanceID + ", so it's impossible to make a majority vote.");

        if (predictions.Size() == 1 || predictions.GetUniquePredictedClasses().size() == 1)
            return new ModelPrediction(predictions.Get(0).Prediction, predictions.Get(0));

        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
        ArrayList<Double> numClassPredictions = new ArrayList<Double>();

        for (String x : classes)
            numClassPredictions.add((double)predictions.GetNumMatchingPredictedClasses(x));

        String predictedClass = ChoosePredictedClass(numClassPredictions);

        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        for (String x : classes)
        {
            double numPredictions = (double)predictions.GetNumMatchingPredictedClasses(x);
            classProbabilities.add(numPredictions / (double)predictions.Size());
        }

        Prediction prediction = new Prediction(instanceID, Utilities.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);
        
        return new ModelPrediction(GetDescription(predictedClass, classProbabilities), prediction);
    }

    /** When majority voting is performed, this method provides a text description of how the majority voting was calculated.
     *
     * @param predictedClass The class that was predicted by majority vote
     * @param classProbabilities The probabilities of each class
     * @return Text description
     * @throws Exception
     */
    public static String GetDescription(String predictedClass, ArrayList<Double> classProbabilities) throws Exception
    {
        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
        String description = "";
        for (int i=0; i<classes.size(); i++)
            description += classes.get(i) + " probability = " + classProbabilities.get(i) + ", ";
        description += "Predicted class = " + predictedClass;
        return description;
    }

    /** When majority voting is used, it produces a summary value indicating the votes that are assigned to each class. This method determines which class has the most votes.
     *
     * @param dependentVariableSummaryValues Numeric values corresponding to number of votes per class
     * @return Predicted class
     * @throws Exception
     */
    public static String ChoosePredictedClass(ArrayList<Double> dependentVariableSummaryValues) throws Exception
    {
        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();

        ArrayList<Integer> indicesOfMaxValues = Lists.GetIndices(dependentVariableSummaryValues, MathUtility.Max(dependentVariableSummaryValues));

        String predictedClass;

        if (indicesOfMaxValues.size() == 1)
            predictedClass = classes.get(indicesOfMaxValues.get(0));
        else
            predictedClass = Lists.PickRandomValue(Lists.Subset(classes, indicesOfMaxValues));

        return predictedClass;
    }
}