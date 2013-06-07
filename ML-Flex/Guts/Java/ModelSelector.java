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
import java.util.concurrent.ConcurrentHashMap;

/** This class is used to help determine which "model" is best for a given data processor, feature-selection algorith, and classification algorithm. It uses the internal cross-validation folds to determine the "best" model.
 * @author Stephen Piccolo
 */
public class ModelSelector
{
    private ArrayList<PredictionEvaluator> _modelEvaluators;

    public AbstractDataProcessor Processor;
    public FeatureSelectionAlgorithm FeatureSelectionAlgorithm;
    public ClassificationAlgorithm ClassificationAlgorithm;

    /** Constructor
     *
     * @param processor Data processor to be tested
     * @param featureSelectionAlgorithm Feature-selection algorithm that will be used to determine which features to use for classification
     * @param classificationAlgorithm Classification algorithm to be applied
     * @param modelEvaluators List of model evaluators
     * @throws Exception
     */
    public ModelSelector(AbstractDataProcessor processor, FeatureSelectionAlgorithm featureSelectionAlgorithm, ClassificationAlgorithm classificationAlgorithm, ArrayList<PredictionEvaluator> modelEvaluators) throws Exception
    {
        Processor = processor;
        FeatureSelectionAlgorithm = featureSelectionAlgorithm;
        ClassificationAlgorithm = classificationAlgorithm;
        _modelEvaluators = modelEvaluators;
    }

    private ArrayList<PredictionEvaluator> GetModelEvaluators(int numFeatures) throws Exception
    {
        ArrayList<PredictionEvaluator> matches = new ArrayList<PredictionEvaluator>();

        for (PredictionEvaluator evaluator : _modelEvaluators)
            if (evaluator.NumFeatures == numFeatures)
                matches.add(evaluator);

        return matches;
    }

    private PredictionEvaluator GetModelEvaluator(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = new PredictionEvaluator(Processor, FeatureSelectionAlgorithm, ClassificationAlgorithm, numFeatures, outerFold, Utilities.InstanceVault.GetTransformedDependentVariableInstances(outerFold));
        int index = _modelEvaluators.indexOf(evaluator);

        if (index == -1)
            return null;

        return _modelEvaluators.get(index);
    }

    /** This method retrieves predictions that have been made previously for data instances within a given inner cross-validation fold and for a given number of features.
     *
     * @param numFeatures Number of features
     * @param outerFold Outer cross-validation fold number
     * @return Predictions that were made previously
     * @throws Exception
     */
    public Predictions GetInnerPredictions(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = GetModelEvaluator(numFeatures, outerFold);

        if (evaluator == null)
            return new Predictions();

        return evaluator.GetInnerPredictions();
    }

    /** This method retrieves the predictions that were made for all inner cross-validation instances
     *
     * @param numFeatures Number of features that were used for the predictions
     * @return Inner cross-validation fold predictions
     * @throws Exception
     */
    public Predictions GetInnerPredictionsAllFolds(int numFeatures) throws Exception
    {
        Predictions predictions = new Predictions();

        for (PredictionEvaluator evaluator : GetModelEvaluators(numFeatures))
            predictions.Add(evaluator.GetInnerPredictions());

        return predictions;
    }

    /** This method retrieves the predictions that were made for all outer cross-validation instances
     *
     * @param numFeatures Number of features that were used for the predictions
     * @return Outer cross-validation fold predictions
     * @throws Exception
     */
    public Predictions GetOuterPredictionsAllFolds(int numFeatures) throws Exception
    {
        Predictions predictions = new Predictions();

        for (PredictionEvaluator evaluator : GetModelEvaluators(numFeatures))
            predictions.Add(evaluator.GetOuterPredictions());

        return predictions;
    }

    /** Identifies the predictions that were made for the inner cross-validation folds and that performed the best across all options for number of features.
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestInnerPredictions(int outerFold) throws Exception
    {
        return GetInnerPredictions(GetBestNumFeatures(outerFold), outerFold);
    }

    /** Identifies the predictions that were made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestOuterPredictions(int outerFold) throws Exception
    {
        return GetOuterPredictions(GetBestNumFeatures(outerFold), outerFold);
    }

    /** This method retrieves predictions that have been made previously for data instances in a given outer cross-validation fold and for a given number of features.
     *
     * @param numFeatures Number of features
     * @param outerFold Outer cross-validation fold number
     * @return Predictions that were made previously
     * @throws Exception
     */
    public Predictions GetOuterPredictions(int numFeatures, int outerFold) throws Exception
    {
        PredictionEvaluator evaluator = GetModelEvaluator(numFeatures, outerFold);

        if (evaluator == null)
            return new Predictions();

        return evaluator.GetOuterPredictions();
    }

    /** Identifies the predictions that were made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @return Predictions
     * @throws Exception
     */
    public Predictions GetBestOuterPredictionsAllFolds() throws Exception
    {
        Predictions predictions = new Predictions();

        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            predictions.Add(GetBestOuterPredictions(outerFold));
        
        return predictions;
    }

    /** Identifies the prediction that was made for the outer cross-validation folds and that performed the best across all options for number of features (tested within inner cross-validation folds).
     *
     * @param instanceID Data instance ID
     * @return Prediction
     * @throws Exception
     */
    public Prediction GetBestOuterPrediction(String instanceID) throws Exception
    {
        int outerFold = Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldNumber(instanceID);

        Predictions outerPredictions = GetBestOuterPredictions(outerFold);

        if (outerPredictions.HasPrediction(instanceID))
            return outerPredictions.GetSinglePrediction(instanceID);

        return null;
    }

    private double GetMeanOuterAuc(int numFeatures) throws Exception
    {
        ArrayList<Double> aucs = new ArrayList<Double>();

        for (PredictionEvaluator evaluator : GetModelEvaluators(numFeatures))
            aucs.add(new PredictionResults(evaluator.GetOuterPredictions()).GetAuc());

        return MathUtility.Mean(aucs);
    }

    private int _bestNumFeaturesAllFolds = 0;

    /** Identifies the option for the number of features that performed best across all outer cross-validation folds. The "best" performance is determined according to performance within outer cross-validation folds.
     *
     * @return Best number of features
     * @throws Exception
     */
    public int GetBestNumFeaturesAllFolds() throws Exception
    {
        if (_bestNumFeaturesAllFolds == 0)
        {
            double bestResult = Double.MIN_VALUE;

            for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(Processor, FeatureSelectionAlgorithm))
            {
                double auc = GetMeanOuterAuc(numFeatures);

                if (auc > bestResult)
                {
                    bestResult = auc;
                    _bestNumFeaturesAllFolds = numFeatures;
                }
            }
        }

        return _bestNumFeaturesAllFolds;
    }

    private ConcurrentHashMap<Integer, Integer> _bestNumFeaturesMap = new ConcurrentHashMap<Integer, Integer>();

    /** Identifies the option for the number of fatures that performed best for a given outer cross-validation fold. The "best" performance is determined according to performance within inner cross-validation folds.
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Best number of features
     * @throws Exception
     */
    public int GetBestNumFeatures(int outerFold) throws Exception
    {
        if (!_bestNumFeaturesMap.containsKey(outerFold))
        {
            Utilities.Log.Debug("Getting best num features for outer fold " + outerFold + " and " + GetDescription());
            int bestNumFeatures = 0;
            double bestResult = Double.MIN_VALUE;

            for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(Processor, FeatureSelectionAlgorithm))
            {
                PredictionEvaluator modelEvaluator = GetModelEvaluator(numFeatures, outerFold);

                if (modelEvaluator == null)
                    continue;

                Predictions innerPredictions = modelEvaluator.GetInnerPredictions();
                double auc = new PredictionResults(innerPredictions).GetAuc();

                if (auc > bestResult)
                {
                    bestResult = auc;
                    bestNumFeatures = numFeatures;
                }
            }

            _bestNumFeaturesMap.put(outerFold, bestNumFeatures);
        }

        return _bestNumFeaturesMap.get(outerFold);
    }

    /** Provides a detailed description of this object.
     *
     * @return Description
     */
    public String GetDescription()
    {
        return Utilities.BuildDescription(Processor.GetDescription(), FeatureSelectionAlgorithm, ClassificationAlgorithm);
    }

    /** Provides a simple description of this object.
     *
     * @return Description
     */
    public String GetSimpleDescription()
    {
        return Lists.Join(Lists.CreateStringList(Processor.GetDescription(), FeatureSelectionAlgorithm.toString(), ClassificationAlgorithm.toString()), "_");
    }

    /** Saves predictions for all data instances across all cross-validation folds.
     *
     * @throws Exception
     */
    public void SavePredictions() throws Exception
    {
        for (PredictionEvaluator evaluator : _modelEvaluators)
            evaluator.SavePredictions(true);
    }

    @Override
    public boolean equals(Object obj)
    {
        ModelSelector compareObj = (ModelSelector)obj;

        return this.Processor.equals(compareObj.Processor) &&
               this.FeatureSelectionAlgorithm.equals(compareObj.FeatureSelectionAlgorithm) &&
               this.ClassificationAlgorithm.equals(compareObj.ClassificationAlgorithm);
    }

    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        return Processor.GetDescription() + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm;
    }

    /** This method constructs a list of ModelSelector objects that are used in an experiment. It creates one for every combination of data processor, feature-selection algorithm, and classification algorithm.
     *
     * @return List of ModelSelector objects
     * @throws Exception
     */
    public static ArrayList<ModelSelector> GetAllModelSelectors() throws Exception
    {
        DataInstanceCollection dependentVariableInstances = Utilities.InstanceVault.GetTransformedDependentVariableInstances();
        ArrayList<ModelSelector> modelSelectors = new ArrayList<ModelSelector>();

        for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
            if (Utilities.InstanceVault.GetCrossValidationAssignments().HasAnyTestData(processor))
                for (FeatureSelectionAlgorithm fsAlgorithm : Utilities.Config.GetFeatureSelectionAlgorithms())
                    for (ClassificationAlgorithm cAlgorithm : Utilities.Config.GetMainClassificationAlgorithms())
                    {
                        ArrayList<PredictionEvaluator> predictionEvaluators = new ArrayList<PredictionEvaluator>();

                        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
                            for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(processor, fsAlgorithm))
                                predictionEvaluators.add(new PredictionEvaluator(processor, fsAlgorithm, cAlgorithm, numFeatures, outerFold, dependentVariableInstances));

                        modelSelectors.add(new ModelSelector(processor, fsAlgorithm, cAlgorithm, predictionEvaluators));
                    }

        return modelSelectors;
    }
}