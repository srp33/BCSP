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

/** Several of the ensemble/combiner approaches in ML-Flex have a common characteristic: they assign weights to individual predictions before making the combined prediction. This class abstracts some of that common functionality.
 * @author Stephen Piccolo
 */
public abstract class AbstractWeightedVoteCombiner extends AbstractEnsemblePredictor
{
    /** This method provides a way for inheriting classes to indicate what weight should be assigned to a given prediction for a given data instance.
     * @param info Prediction info
     * @return Weight assigned to the prediction
     * @throws Exception
     */
    protected abstract double GetWeight(EnsemblePredictionInfo info) throws Exception;

    /** This method makes a ensemble/combined prediction for all weight-based combiner classes
     * @param instanceID Data instance ID
     * @param combinedPredictionInfos Prediction info
     * @return Combined prediction
     * @throws Exception
     */
    @Override
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos combinedPredictionInfos) throws Exception
    {
        ArrayList<String> classes = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
        ArrayList<Double> classWeights = new ArrayList<Double>();

        for (String x : classes)
            classWeights.add(0.0);

        for (EnsemblePredictionInfo info : combinedPredictionInfos.Infos)
            classWeights.set(classes.indexOf(info.OuterPrediction.Prediction), GetWeight(info));

        String predictedClass = MajorityVoteEnsemblePredictor.ChoosePredictedClass(classWeights);

        double totalWeight = MathUtility.Sum(classWeights);

        ArrayList<Double> classProbabilities = new ArrayList<Double>();
        for (int i=0; i<classWeights.size(); i++)
            classProbabilities.add(classWeights.get(i) / totalWeight);

        Prediction prediction = new Prediction(instanceID, Utilities.InstanceVault.GetTransformedDependentVariableValue(instanceID), predictedClass, classProbabilities);

        return new ModelPrediction(MajorityVoteEnsemblePredictor.GetDescription(predictedClass, classProbabilities), prediction);
    }
}
