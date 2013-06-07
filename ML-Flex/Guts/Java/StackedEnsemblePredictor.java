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

/** This class implements the "stacked combiner" approach ensemble learning. This method builds second-level classification models on the individual predictions that have been made for a given data instance. The classification algorithm used for the second-level predictions can be configured in the experiment files.
 * @author Stephen Piccolo
 */
public class StackedEnsemblePredictor extends AbstractEnsemblePredictor
{
    private ClassificationAlgorithm _level2ClassificationAlgorithm;
    private boolean _useProbabilities;

    /** Constructor
     *
     *
     * @param level2ClassificationAlgorithm Level 2 classification algorithm
     * @param useProbabilities Whether to use the class probabilities as variables in the level 2 classification; if false, the predicted classes are used
     * @throws Exception
     */
    public StackedEnsemblePredictor(ClassificationAlgorithm level2ClassificationAlgorithm, boolean useProbabilities) throws Exception
    {
        _level2ClassificationAlgorithm = level2ClassificationAlgorithm;
        _useProbabilities = useProbabilities;
    }

    @Override
    protected String GetDescription()
    {
        return Utilities.BuildDescription(super.GetDescription(), _level2ClassificationAlgorithm.Description, (_useProbabilities ? "Probabilities" : "Classes"));
    }

    @Override
    protected Predictions MakeInstancePredictions(HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        ArrayList<String> dependentVariableClasses = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();

        DataInstanceCollection trainData = new DataInstanceCollection();
        DataInstanceCollection testData = new DataInstanceCollection();

        HashSet<EnsemblePredictionInfo> trainInfos = new HashSet<EnsemblePredictionInfo>();

        for (String instanceID : ensemblePredictionInfoMap.keySet())
            trainInfos.addAll(ensemblePredictionInfoMap.get(instanceID).Infos);

        if (_useProbabilities)
            for (EnsemblePredictionInfo info : trainInfos)
                for (Prediction prediction : info.InnerPredictionResults.Predictions)
                    for (int i = 0; i<dependentVariableClasses.size(); i++)
                        trainData.Add(FormatName(info.Description + "_" + dependentVariableClasses.get(i)), prediction.InstanceID, String.valueOf((double) prediction.ClassProbabilities.get(i)));
        else
            for (EnsemblePredictionInfo info : trainInfos)
                for (Prediction prediction : info.InnerPredictionResults.Predictions)
                    trainData.Add(FormatName(info.Description), prediction.InstanceID, FormatName(prediction.Prediction));

        if (_useProbabilities)
        {
            for (String instanceID : ensemblePredictionInfoMap.keySet())
                for (EnsemblePredictionInfo info : ensemblePredictionInfoMap.get(instanceID).Infos)
                    for (int i = 0; i<dependentVariableClasses.size(); i++)
                        testData.Add(FormatName(info.Description + "_" + dependentVariableClasses.get(i)), instanceID, String.valueOf((double) info.OuterPrediction.ClassProbabilities.get(i)));
        }
        else
        {
            for (String instanceID : ensemblePredictionInfoMap.keySet())
                for (EnsemblePredictionInfo info : ensemblePredictionInfoMap.get(instanceID).Infos)
                    testData.Add(FormatName(info.Description), instanceID, FormatName(info.OuterPrediction.Prediction));
        }

        return _level2ClassificationAlgorithm.TrainTest(trainData, testData, Utilities.InstanceVault.GetTransformedDependentVariableInstances()).Predictions;
    }

    private String FormatName(String name)
    {
        return name.replace("-", "_").replace(".", "_");
    }
}