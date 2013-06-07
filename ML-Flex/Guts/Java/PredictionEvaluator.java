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
import java.util.concurrent.Callable;

/** This class contains logic for evaluating classification models. It orchestrates the process of performing classification for a particular data processor, feature-selection algorith, classification algorithm, number of features, and cross-validation fold. It also contains logic for distributing tasks across multiple computing nodes and threads.
 * @author Stephen Piccolo
 */
public class PredictionEvaluator
{
    public final AbstractDataProcessor Processor;
    public final FeatureSelectionAlgorithm FeatureSelectionAlgorithm;
    public final ClassificationAlgorithm ClassificationAlgorithm;
    public final int NumFeatures;
    public final int OuterFold;
    private final DataInstanceCollection _dependentVariableInstances;

    /** Constructor
     *
     * @param processor Data processor that handles data that will be used for constructing classification models
     * @param selectionAlgorithm Feature-selection algorithm that will be applied prior to classification
     * @param classificationAlgorithm Classification algorithm that will be used
     * @param numFeatures The number of features that will be used for the classification models
     * @param outerFold Outer cross-validation fold number
     * @param dependentVariableInstances Dependent-variable data instances
     * @throws Exception
     */
    public PredictionEvaluator(AbstractDataProcessor processor, FeatureSelectionAlgorithm selectionAlgorithm, ClassificationAlgorithm classificationAlgorithm, int numFeatures, int outerFold, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        Processor = processor;
        FeatureSelectionAlgorithm = selectionAlgorithm;
        ClassificationAlgorithm = classificationAlgorithm;
        NumFeatures = numFeatures;
        OuterFold = outerFold;
        _dependentVariableInstances = dependentVariableInstances;
    }

    private String GetSaveDirectory()
    {
        return Settings.PREDICTIONS_DIR + Processor.GetDescription() + "Data/" + FeatureSelectionAlgorithm + "/" + ClassificationAlgorithm + "/" + NumFeatures + "Features/Iteration" + Utilities.Iteration + "/OuterFold" + OuterFold + "/";
    }

    private String GetOuterSaveFilePath()
    {
        return GetSaveDirectory() + GetOuterPredictionsFileName();
    }

    private String GetInnerSaveFilePath(int innerFold)
    {
        return GetSaveDirectory() + GetModelPredictionsFileName(innerFold);
    }

    private String GetOuterModelFilePath()
    {
        return GetSaveDirectory() + "OuterFold_Model.txt";
    }

    private String GetLockFilePrefix()
    {
        return Processor.GetDescription() + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm + "_" + NumFeatures + "_" + Utilities.Iteration + "_" + OuterFold + "_";
    }

    private String GetOuterLockFilePath()
    {
        return GetLockFilePrefix() + GetOuterPredictionsFileName();
    }

    private String GetInnerLockFilePath(int innerFold)
    {
        return GetLockFilePrefix() + GetModelPredictionsFileName(innerFold);
    }

    private String GetStatusFilePrefix()
    {
        return GetSaveDirectory().replace(Settings.PREDICTIONS_DIR, "Predictions/");
    }

    private String GetOuterStatusFilePath()
    {
        return GetStatusFilePrefix() + GetOuterPredictionsFileName();
    }

    private String GetInnerStatusFilePath(int innerFold)
    {
        return GetStatusFilePrefix() + GetModelPredictionsFileName(innerFold);
    }

    /** Indicates the file name where the outer-fold prediction information should be stored
     *
     * @return File name where prediction information is stored
     */
    public String GetOuterPredictionsFileName()
    {
        return "OuterFold_Predictions.txt";
    }

    /** Indicates the file name where the inner-fold prediction information should be stored
     *
     * @param innerFold Number of inner cross-validation fold
     * @return File name where prediction information is stored
     */
    public String GetModelPredictionsFileName(int innerFold)
    {
        return "InnerFold" + innerFold + "_Predictions.txt";
    }

    /** This method contains logic for performing classification across all cross-validation folds.
     *
     * @throws Exception
     */
    public void SavePredictions(boolean includeOuter) throws Exception
    {
        Files.CreateDirectoryNoFatalError(GetSaveDirectory());

        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final int innerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
        {
            taskHandler.Add(new LockedCallable<Object>(GetInnerStatusFilePath(innerFold), GetInnerLockFilePath(innerFold), "Make predictions for " + GetInnerDescription(innerFold), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ArrayList<String> features = GetInnerFeatures(innerFold);
                    DataInstanceCollection trainData = Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTrainInstances(Processor, innerFold, features);
                    DataInstanceCollection testData = Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTestInstances(Processor, innerFold, features);

                    return MakeAndSavePredictions(features, trainData, testData, GetInnerSaveFilePath(innerFold), null, GetInnerDescription(innerFold));
                }
            }));
        }

        if (includeOuter)
        {
            taskHandler.Add(new LockedCallable<Object>(GetOuterStatusFilePath(), GetOuterLockFilePath(), "Make predictions for " + GetOuterDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ArrayList<String> features = GetOuterFeatures();
                    DataInstanceCollection trainData = Utilities.InstanceVault.GetCrossValidationAssignments().GetTrainInstances(Processor, OuterFold, features);
                    DataInstanceCollection testData = Utilities.InstanceVault.GetCrossValidationAssignments().GetTestInstances(Processor, OuterFold, features);

                    return MakeAndSavePredictions(features, trainData, testData, GetOuterSaveFilePath(), GetOuterModelFilePath(), GetOuterDescription());
                }
            }));
        }

        taskHandler.ExecuteWithRetries("make predictions for " + toString());
    }

    private Boolean MakeAndSavePredictions(ArrayList<String> features, DataInstanceCollection trainData, DataInstanceCollection testData, String saveFilePath, String modelFilePath, String description) throws Exception
    {
        if (!NeedToMakePredictions(features, trainData, testData, description))
            return Boolean.TRUE;

        ModelPredictions modelPredictions = ClassificationAlgorithm.TrainTest(trainData, testData, _dependentVariableInstances.Clone());

        if (!PredictionsAreValid(testData, modelPredictions, description))
            return Boolean.FALSE;

        modelPredictions.Predictions.SaveToFile(saveFilePath);

        if (modelFilePath != null && modelPredictions.Model.length() > 0)
            Files.WriteTextToFile(modelFilePath, modelPredictions.Model);

        return modelPredictions.Predictions.equals(Predictions.ReadFromFile(saveFilePath));
    }

    private boolean NeedToMakePredictions(ArrayList<String> features, DataInstanceCollection trainData, DataInstanceCollection testData, String description)
    {
        if (features.size() == 0)
        {
            Utilities.Log.Debug("No predictions were saved for " + description + " because no features were selected.");
            return false;
        }

        if (trainData.Size() == 0)
        {
            Utilities.Log.Debug("No predictions were saved for " + description + " because there were no training data instances.");
            return false;
        }

        if (testData.Size() == 0)
        {
            Utilities.Log.Debug("No predictions were saved for " + description + " because there were no test data instances.");
            return false;
        }

        return true;
    }

    private boolean PredictionsAreValid(DataInstanceCollection testData, ModelPredictions modelPredictions, String description) throws Exception
    {
        for (Prediction prediction : modelPredictions.Predictions)
            if (!testData.Contains(prediction.InstanceID))
            {
                Utilities.Log.Info("Test IDs:");
                Utilities.Log.Info(testData.GetIDs());
                Utilities.Log.Info("Prediction IDs:");
                Utilities.Log.Info(modelPredictions);
                Utilities.Log.Exception("In " + description + ", a prediction was made for instance " + prediction.InstanceID + " even though it wasn't a test instance.");
                return false;
            }

        for (String instanceID : testData.GetIDs())
            if (!modelPredictions.Predictions.HasPrediction(instanceID))
            {
                Utilities.Log.Exception("In " + description + ", no prediction was made for instance " + instanceID + ".");
                return false;
            }

        if (modelPredictions.Predictions.Size() != testData.Size())
        {
            Utilities.Log.Exception("The number of predictions made for " + description + "(" + modelPredictions.Predictions.Size() + ") was not equal to the number of test instances (" + testData.Size() + ".");
            return false;
        }

        return true;
    }

    private ArrayList<String> GetOuterFeatures() throws Exception
    {
        return new FeatureEvaluator(Processor, FeatureSelectionAlgorithm, OuterFold).GetOuterSelectedFeatures(NumFeatures);
    }

    private ArrayList<String> GetInnerFeatures(int innerFold) throws Exception
    {
        return new FeatureEvaluator(Processor, FeatureSelectionAlgorithm, OuterFold).GetInnerSelectedFeatures(innerFold, NumFeatures);
    }

    private Predictions ReadInnerPredictions(int innerFold) throws Exception
    {
        return Predictions.ReadFromFile(GetInnerSaveFilePath(innerFold));
    }

    private Predictions ReadOuterPredictions() throws Exception
    {
        return Predictions.ReadFromFile(GetOuterSaveFilePath());
    }

    private Predictions _innerPredictions = null;
    /** This method retrieves predictions that have been made for patients in the inner cross-validation folds.
     *
     * @return Predictions for data instances in inner cross-validation folds
     * @throws Exception
     */
    public Predictions GetInnerPredictions() throws Exception
    {
        if (_innerPredictions == null)
        {
            MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

            for (final int innerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
            {
                taskHandler.Add(new Callable<Object>()
                {
                    public Predictions call() throws Exception
                    {
                        if (GetInnerFeatures(innerFold).size() == 0)
                            return new Predictions();

                        return ReadInnerPredictions(innerFold);
                    }
                });
            }

            _innerPredictions = new Predictions();
            for (Object x : taskHandler.Execute())
                _innerPredictions.Add((Predictions)x);
        }

        return _innerPredictions;
    }

    private Predictions _outerPredictions = null;
    /** This method retrieves predictions that have been made for patients in the outer cross-validation folds.
     *
     * @return Predictions for data instances in outer cross-validation folds
     * @throws Exception
     */
    public Predictions GetOuterPredictions() throws Exception
    {
        if (_outerPredictions == null)
        {
            if (GetOuterFeatures().size() == 0)
                return new Predictions();

            _outerPredictions = ReadOuterPredictions();
        }

        return _outerPredictions;
    }

    /** Provides a description of this object, specific to processing of inner cross-validation folds.
     *
     * @return Description
     */
    public String GetInnerDescription(int innerFold)
    {
        return GetOuterDescription() + "_InnerFold" + innerFold;
    }

    /** Provides a description of this object, specific to processing of outer cross-validation fold.
     * @return Description
     */
    public String GetOuterDescription()
    {
        return Processor + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm + "_" + NumFeatures + "Features_OuterFold" + OuterFold;
    }

    @Override
    public boolean equals(Object obj)
    {
        PredictionEvaluator compareObj = (PredictionEvaluator)obj;

        return this.Processor.equals(compareObj.Processor) &&
               this.FeatureSelectionAlgorithm.equals(compareObj.FeatureSelectionAlgorithm) &&
               this.ClassificationAlgorithm.equals(compareObj.ClassificationAlgorithm) &&
               this.NumFeatures == compareObj.NumFeatures &&
               this.OuterFold == compareObj.OuterFold;
    }

    @Override
    public String toString()
    {
        return Processor.GetDescription() + "_" + FeatureSelectionAlgorithm + "_" + ClassificationAlgorithm + "_" + NumFeatures + "_" + "OuterFold" + OuterFold;
    }
}
