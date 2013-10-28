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

/** ML-Flex supports ensemble-learning approaches for aggregating evidence from multiple classification predictions. This class is a generic handler of all ensemble approaches in ML-Flex.
 * @author Stephen Piccolo
 */
public abstract class AbstractEnsemblePredictor
{
    /** This method specifies the directory where the ensemble predictions will be saved.
     *
     * @param outerFold Number of outer cross-validation foldd
     * @return Absolute directory path
     */
    public String GetSaveDirectory(int outerFold)
    {
        return Settings.ENSEMBLE_PREDICTIONS_DIR + "Iteration" + Utilities.Iteration + "/OuterFold" + outerFold + "/";
    }

    /** This method specifies the file path where the ensemble predictions will be saved.
     *
     * @param outerFold Number of outer cross-validation foldd
     * @return Absolute file path
     */
    private String GetSaveFilePath(int outerFold)
    {
        return GetSaveDirectory(outerFold) + GetDescription() + "_Predictions.txt";
    }

    /** This method is the workhorse of this class. Having received information about individual predictions that were made for each data instance, it combines the individual predictions into a combined prediction, using the custom logic of the overriding class.
     *
     * @param outerFold Number of outer cross-validation fold
     * @param ensemblePredictionInfoMap Map of ensemble prediction info (one for each data instance)
     * @return Indicates whether this method was successful
     * @throws Exception
     */
    public Boolean MakeEnsemblePredictions(int outerFold, HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        Predictions ensemblePredictions = MakeInstancePredictions(ensemblePredictionInfoMap);
        ensemblePredictions.SaveToFile(GetSaveFilePath(outerFold));

        return ensemblePredictions.equals(GetEnsemblePredictions(outerFold));
    }

    /** This method reads from file the ensemble predictions that were previously made across all outer cross-validation folds.
     * @return Ensemble predictions that were made previously and stored on disk.
     * @throws Exception
     */
    public Predictions GetEnsemblePredictions() throws Exception
    {
        Predictions predictions = new Predictions();

        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            if (Files.FileExists(GetSaveFilePath(outerFold)))
                predictions.Add(Predictions.ReadFromFile(GetSaveFilePath(outerFold)));

        return predictions;
    }

    /** This method reads from file the ensemble predictions that were previously made.
     * @param outerFold Number of outer cross-validation fold
     * @return Combined predictions that were made previously and stored on disk.
     * @throws Exception
     */
    public Predictions GetEnsemblePredictions(int outerFold) throws Exception
    {
        return Predictions.ReadFromFile(GetSaveFilePath(outerFold));
    }

    /** This method makes ensemble predictions for a particulate data instance. This method is what needs to be overridden by most classes that inherit from this class.
     *
     * @param ensemblePredictionInfoMap Prediction info for the data instance
     * @return Combined prediction
     * @throws Exception
     */
    protected Predictions MakeInstancePredictions(HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap) throws Exception
    {
        Predictions predictions = new Predictions();

        for (String instanceID : ensemblePredictionInfoMap.keySet())
            predictions.Add(MakeInstancePrediction(instanceID, ensemblePredictionInfoMap.get(instanceID)).Prediction);

        return predictions;
    }

    /** This method makes an ensemble prediction for a particulate data instance. This method is what needs to be overridden by most classes that inherit from this class.
     *
     * @param instanceID Data instance ID
     * @param predictionInfos Prediction info for the data instance
     * @return Combined prediction
     * @throws Exception
     */
    protected ModelPrediction MakeInstancePrediction(String instanceID, EnsemblePredictionInfos predictionInfos) throws Exception
    {
        throw new Exception("Not implemented");
    }


    /** This method provides a simple description of the ensemble approach. By default this is the name of the class.
     *
     * @return Simple description of ensemble approach
     */
    protected String GetDescription()
    {
        return getClass().getSimpleName();
    }

    /** This method indicates which ensemble predictors should be used (if any).
     *
     * @return A list of ensemble predictor objects
     * @throws Exception
     */
    public static ArrayList<AbstractEnsemblePredictor> GetAllEnsemblePredictors() throws Exception
    {
        ArrayList<AbstractEnsemblePredictor> ensemblePredictors = new ArrayList<AbstractEnsemblePredictor>();

        if (Utilities.Config.GetFeatureSelectionAlgorithms().length == 1 && Utilities.Config.GetMainClassificationAlgorithms().length == 1 && Utilities.ProcessorVault.IndependentVariableDataProcessors.size() == 1)
            return ensemblePredictors; // No need to perform ensemble learning if there is only one learner

        ensemblePredictors.add(new MajorityVoteEnsemblePredictor());
        ensemblePredictors.add(new SimpleWeightedVoteEnsemblePredictor());
        ensemblePredictors.add(new SelectBestEnsemblePredictor());
        ensemblePredictors.add(new MaxProbabilityEnsemblePredictor());
        ensemblePredictors.add(new MeanProbabilityEnsemblePredictor());
        ensemblePredictors.add(new SimpleWeightedMeanProbabilityEnsemblePredictor());

        for (ClassificationAlgorithm algorithm : Utilities.Config.GetStackingClassificationAlgorithms())
        {
            ensemblePredictors.add(new StackedEnsemblePredictor(algorithm, true));
            ensemblePredictors.add(new StackedEnsemblePredictor(algorithm, false));
        }

        return ensemblePredictors;
    }

    /** This method retrieves information from predictions that were made previously, so that prediction information can be used for ensemble learning.
     *
     * @param outerFold Number of outer cross-validation fold
     * @param modelSelectors List of model selectors, which are used for retrieving the predictions
     * @return A map containing predictions for each data instance
     * @throws Exception
     */
    public static HashMap<String, EnsemblePredictionInfos> GetInstanceEnsemblePredictionInfos(int outerFold, ArrayList<ModelSelector> modelSelectors) throws Exception
    {
        HashMap<String, EnsemblePredictionInfos> instanceEnsemblePredictionInfoMap = new HashMap<String, EnsemblePredictionInfos>();

        for (final ModelSelector modelSelector : modelSelectors)
        {
            if (modelSelector.Processor instanceof AggregateDataProcessor)
                continue;

            int bestNumFeatures = modelSelector.GetBestNumFeatures(outerFold);
            Predictions innerPredictons = modelSelector.GetInnerPredictions(bestNumFeatures, outerFold);
            Predictions outerPredictons = modelSelector.GetOuterPredictions(bestNumFeatures, outerFold);

            if (innerPredictons.Size() == 0 || outerPredictons.Size() == 0)
                continue;

            PredictionResults innerPredictionResults = new PredictionResults(innerPredictons);

            for (String instanceID : outerPredictons.GetInstanceIDs())
            {
                EnsemblePredictionInfo instanceInfo = new EnsemblePredictionInfo(outerPredictons.GetSinglePrediction(instanceID), innerPredictionResults, modelSelector.GetSimpleDescription());

                if (instanceEnsemblePredictionInfoMap.containsKey(instanceID))
                    instanceEnsemblePredictionInfoMap.put(instanceID, instanceEnsemblePredictionInfoMap.get(instanceID).Add(instanceInfo));
                else
                    instanceEnsemblePredictionInfoMap.put(instanceID, new EnsemblePredictionInfos().Add(instanceInfo));

                //The code below will give you ensemble results for all number of features
                //for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(modelSelector.Processor))
                //    predictionInfos.Add(new EnsemblePredictionInfo(modelSelector.GetOuterPrediction(numFeatures, instanceID), modelSelector.GetInnerPredictions(numFeatures, instanceID), modelSelector.GetSimpleDescription()));
            }
        }

        return instanceEnsemblePredictionInfoMap;
    }
}
