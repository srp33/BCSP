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

/** This class represents an ensemble/combiner approach that computes the combined prediction according to the class probabilities that were assigned for each individual prediction. This particular approach uses the mean across the probabilities to compute the combined prediction.
 * @author Stephen Piccolo
 */
public class MeanProbabilityEnsemblePredictor extends AbstractEnsemblePredictor
{
    private boolean _assignWeights;

    /** Constuctor
     */
    public MeanProbabilityEnsemblePredictor()
    {
        this(false);
    }

    /** Pass-through constructor
     *
     * @param assignWeights Indicates whether weights should be assigned to the individual probabilities, based on inner cross-validation performance.
     */
    protected MeanProbabilityEnsemblePredictor(boolean assignWeights)
    {
        _assignWeights = assignWeights;
    }

    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();

        for (String x : classes)
            classProbabilities.add(0.0);

        for (String x : classes)
            for (EnsemblePredictionInfo info : combinedPredictionInfos.Infos)
                for (int i=0; i<info.OuterPrediction.ClassProbabilities.size(); i++)
                {
                    Double probability = info.OuterPrediction.ClassProbabilities.get(i);

                    if (!_assignWeights)
                        probability *= info.GetSimpleWeight();

                    classProbabilities.set(i, classProbabilities.get(i) + probability); // Sum has same effect as mean
                }

        String predictedClass = MajorityVoteEnsemblePredictor.ChoosePredictedClass(classProbabilities);

        double totalProbability = MathUtility.Sum(classProbabilities);

        for (int i=0; i<classProbabilities.size(); i++)
            classProbabilities.set(i, classProbabilities.get(i) / totalProbability);

        Prediction prediction = new Prediction(instanceID, Utilities.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);

        return new ModelPrediction(MajorityVoteEnsemblePredictor.GetDescription(predictedClass, classProbabilities), prediction);
    }
}