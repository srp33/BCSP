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
import java.util.concurrent.Callable;

//  TODO:
//    Add predictive value (class specific) weighting option to stacked combiner?
//    Resolve problems with SaveAgreementResults
//      Calculate QStatistic by calculating agrement between algorithms within each data category (only for patients with data) and then averaging across the data categories
//      Calculate QStatistic by calculating agreement between data processors only for patients that have data for all categories.
//      Similar concept for Kappa statistic
//      Update kappa statistic so it can handle where data categories are missing data
//    Come up with permutation code to get empirical p-value that compares two algorithms. Alternatively (and more simply), use the McNemar statistic as described here: http://www.ailab.si/orange/doc/modules/orngStat.htm
//    Put in infrastructure to handle survival as a continuous variable for prediction. The following paper explains how to calculate AUC from continuous variables: AUC: A Better Measure than Accuracy in Comparing Learning Algorithms
//  Create threshold selection example?
//  Other metric(s)? Relative Classifier Information (see EvaluationMetrics class). F measure? Matthews Correlation Coefficient (see paper by Oberthuer = "Comparison of one-color and two-color microarray data"). Root mean squared error (see paper by Oberthuer; good for binary or continuous output). These latter two may not be best because they are not suitable for multiclass?
//  In WekaLearner and OrangeLearner, figure out how to get the output and add to ModelPredictions.


/** This class contains high-level commands for executing tasks within a given experiment. It makes sure steps are performed in the correct order and verifies that all prerequisite steps are performed before moving to the next step.
 *
 * @author Stephen Piccolo
 */
public class Experiment
{
    public String Description;

    /** Constructor used to initialize an experiment.
     *
     * @param description Short description of the experiment
     */
    public Experiment(String description)
    {
        Description = description;
    }

    /** This method orchestrates the computational tasks that will be performed for an experiment, depending on the "actions" requested by the user. It is also intended to provide a high-level view of the workflow that is followed in an experiment.
     *
     * @param actions List of tasks to orchestrate
     * @throws Exception
     */
    public void Orchestrate(ArrayList<Action> actions) throws Exception
    {
        if (actions.contains(Action.Reset))
            Reset();

        if (actions.contains(Action.Process))
        {
            if (Utilities.IsFirstIteration())
            {
                // The following tasks prepare the data that will be used for a given experiment. Because the same data will be used in all iterations, these steps need only be performed on the first iteration.
                ProcessMetadata();
                ProcessRawData();
                PostProcessRawData();

                // This loads data into memory before performing the main experiment tasks
                Preload();

                // This saves information about the data for the user to examine
                SaveStatistics();
            }

            // Perform feature selection
            SelectFeatures();

            // Classify (for individual learners and for ensemble learners)
            ArrayList<ModelSelector> modelSelectors = ModelSelector.GetAllModelSelectors();
            MakePredictions(modelSelectors);
            MakeEnsemblePredictions(modelSelectors);

            // Calculate and save results
            SavePredictionResults(modelSelectors);
            SaveEnsembleResults();
            SaveResultsSummary();

            // Save description files that help with interpretation
            SaveDescriptionFiles();
        }

        if (actions.contains(Action.Export) && Utilities.IsFirstIteration())
        {
            // When the user desires to export data, they can do this without also invoking the Process action. So the steps of preparing the data must be completed if they haven't already
            if (!actions.contains(Action.Process))
            {
                ProcessMetadata();
                ProcessRawData();
                PostProcessRawData();
            }

            // This exports the data for each data processor in various formats
            ExportDataFiles();
        }

        // This removes temporary files that may have been left on the file system inadvertently
        Clean();
    }

    /** This method deletes any files that might exist from previous runs of this experiment so that the current experiment can start from scratch.
     *
     * @throws Exception
     */
    public void Reset() throws Exception
    {
        for (String directoryPath : Lists.CreateStringList(Settings.FEATURE_SELECTION_DIR, Settings.PREDICTIONS_DIR, Settings.OUTPUT_DIR, Settings.STATUS_DIR, Settings.LOCKS_DIR))
            Files.DeleteAllFilesAndDirectoriesRecursively(directoryPath + this.toString());
    }

    private void ProcessMetadata() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractMetadataProcessor processor : Utilities.ProcessorVault.MetaDataProcessors)
        {
            taskHandler.Add(new LockedCallable<Object>("Metadata/" + processor.GetDescription(), "Metadata_" + processor.GetDescription(), "Save metadata for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.Save();
                }
            }));
        }

        taskHandler.ExecuteWithRetries("save metadata");
    }

    private void ProcessRawData() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractDataProcessor processor : Utilities.ProcessorVault.AllDataProcessors)
        {
            taskHandler.Add(new LockedCallable<Object>("RawData/" + processor.GetDescription(), "RawData_" + processor.GetDescription(), "Process raw data for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.ProcessRawData();
                }
            }));
        }

        taskHandler.ExecuteWithRetries("process raw data");
    }

    private void PostProcessRawData() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractDataProcessor processor : Utilities.ProcessorVault.AllDataProcessors)
        {
            taskHandler.Add(new LockedCallable<Object>("RawDataPostProcess/" + processor.GetDescription(), "RawDataPostProcess_" + processor.GetDescription(), "Post-process raw data for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.PostProcessRawData();
                }
            }));
        }

        taskHandler.ExecuteWithRetries("post-process raw data");
    }

    private void Preload() throws Exception
    {
        //This is an attempt to prevent any issues caused by competing threads. The data get cached after they
        //are retrieved from the file system.
        for (AbstractDataProcessor processor : Utilities.ProcessorVault.AllDataProcessors)
        {
            Utilities.Log.Debug("Preloading instances for " + processor.GetDescription());
            DataInstanceCollection instances = Utilities.InstanceVault.GetAnalysisInstances(processor, null, null);

            if (instances.Size() == 0)
                throw new Exception("There were no data instances to be analyzed for " + processor.GetDescription());
        }

        Utilities.InstanceVault.GetCrossValidationAssignments();
    }

    private void SaveStatistics() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractDataProcessor processor : Utilities.ProcessorVault.AllDataProcessors)
        {
            taskHandler.Add(new LockedCallable<Object>("Statistics/" + processor.GetDescription(), "Statistics_" + processor.GetDescription(), "Save statistics for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return processor.SaveStatistics();
                }
            }));
        }

        taskHandler.Add(new LockedCallable<Object>("Statistics/All", "Statistics_All", "Save statistics for all" , new Callable<Object>()
        {
            public Object call() throws Exception
            {
                return AbstractDataProcessor.SaveStatisticsAcrossAllIndependentVariableProcessors();
            }
        }));

        taskHandler.ExecuteWithRetries("save statistics");
    }

    private void SelectFeatures() throws Exception
    {
        for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
            for (FeatureSelectionAlgorithm fsAlgorithm : Utilities.Config.GetFeatureSelectionAlgorithms())
                if (!fsAlgorithm.IsNone() && !fsAlgorithm.IsPriorKnowledge())
                    for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
                        new FeatureEvaluator(processor, fsAlgorithm, outerFold).SelectFeatures();
    }

    private void MakePredictions(ArrayList<ModelSelector> modelSelectors) throws Exception
    {
        for (ModelSelector selector : modelSelectors)
            selector.SavePredictions();
    }

    private void MakeEnsemblePredictions(final ArrayList<ModelSelector> modelSelectors) throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final Integer outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
        {
            taskHandler.Add(new LockedCallable<Object>("EnsemblePredictions/OuterFold" + outerFold, "EnsemblePredictions_OuterFold" + outerFold, "Save ensemble predictions for outer fold " + outerFold, new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    // This step pulls the predictions that have already been made so they can be aggregated for the ensemble learnerrs
                    HashMap<String, EnsemblePredictionInfos> ensemblePredictionInfoMap = AbstractEnsemblePredictor.GetInstanceEnsemblePredictionInfos(outerFold, modelSelectors);

                    for (final AbstractEnsemblePredictor ensemblePredictor : AbstractEnsemblePredictor.GetAllEnsemblePredictors())
                    {
                        Files.CreateDirectoryNoFatalError(ensemblePredictor.GetSaveDirectory(outerFold));
                        if (!ensemblePredictor.MakeEnsemblePredictions(outerFold, ensemblePredictionInfoMap))
                            return Boolean.FALSE;
                    }

                    return Boolean.TRUE;
                }
            }));
        }

        taskHandler.ExecuteWithRetries("save ensemble predictions");
    }

    private void SavePredictionResults(ArrayList<ModelSelector> modelSelectors) throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final ModelSelector modelSelector : modelSelectors)
        {
            taskHandler.Add(new LockedCallable<Object>("Results/" + modelSelector.GetDescription(), "Results_" + modelSelector.GetDescription(), "Save results " + modelSelector.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return new ResultsSaver().SavePredictionResults(modelSelector);
                }
            }));
        }

        taskHandler.ExecuteWithRetries("save results");
    }

    private void SaveEnsembleResults() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractEnsemblePredictor ensemblePredictor : AbstractEnsemblePredictor.GetAllEnsemblePredictors())
        {
            taskHandler.Add(new LockedCallable<Object>("EnsembleResults/" + ensemblePredictor.GetDescription(), "EnsembleResults_" + ensemblePredictor.GetDescription(), "Save ensemble results " + ensemblePredictor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return new ResultsSaver().SaveEnsembleResults(ensemblePredictor);
                }
            }));
        }

        taskHandler.ExecuteWithRetries("save ensemble results");
    }

    private void SaveResultsSummary() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        if (Utilities.IsLastIteration())
        {
            taskHandler.Clear();
            taskHandler.Add(new LockedCallable<Object>("Results/Summary", "Results/Summary", "Save results summary", new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return ResultsSaver.SaveScalarResultsSummary();
                }
            }));
        }

        taskHandler.ExecuteWithRetries("save results summary");
    }

    private void SaveDescriptionFiles() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();
        taskHandler.Add(new DescriptionFileSaver().SaveMiscellaneousDescriptionFiles());
        taskHandler.Add(new ArrayList<Callable<Object>>(new DescriptionFileSaver().SaveMeanFeatureRanksFiles()));
        taskHandler.ExecuteWithRetries("save description files");
    }

    private void ExportDataFiles() throws Exception
    {
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
        {
            taskHandler.Add(new LockedCallable<Object>("ExportData/" + processor.GetDescription(), "ExportData_" + processor.GetDescription(), "Export data for " + processor.GetDescription(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    DataInstanceCollection instances = Utilities.InstanceVault.GetAnalysisInstances(processor, null, null);

                    if (instances.Size() > 0)
                    {
                        instances.SaveToFile(Settings.GetExperimentOutputDir(false), processor.GetDescription());
                        new AnalysisFileCreator(Settings.GetExperimentOutputDir(false), processor.GetDescription(), instances, null, Utilities.InstanceVault.GetTransformedDependentVariableInstances()).CreateArffFile();
                    }

                    return Boolean.TRUE;
                }
            }));

            taskHandler.ExecuteWithRetries("export data files");
        }
    }

//    private ArrayList<ModelSelector> _modelSelectors = null;
//    private ArrayList<ModelSelector> GetModelSelectors() throws Exception
//    {
//        if (_modelSelectors == null)
//        {
//            ArrayList<PredictionEvaluator> allModelEvaluators = new ArrayList<PredictionEvaluator>();
//
//            Utilities.Log.Debug(("Getting all prediction evaluators"));
//            for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
//                for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
//                {
//                    DataInstanceCollection dependentVariableInstances = Utilities.InstanceVault.GetTransformedDependentVariableInstances(outerFold);
//
//                    for (FeatureSelectionAlgorithm fsAlgorithm : Utilities.Config.GetFeatureSelectionAlgorithms())
//                        for (ClassificationAlgorithm cAlgorithm : Utilities.Config.GetMainClassificationAlgorithms())
//                            for (int numFeatures : Utilities.Config.GetNumFeaturesOptions(processor, fsAlgorithm))
//                                allModelEvaluators.add(new PredictionEvaluator(processor, fsAlgorithm, cAlgorithm, numFeatures, outerFold, dependentVariableInstances));
//                }
//
//            Utilities.Log.Debug("Getting all model selectors");
//            _modelSelectors = new ArrayList<ModelSelector>();
//
//            for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
//                for (FeatureSelectionAlgorithm fsAlgorithm : Utilities.Config.GetFeatureSelectionAlgorithms())
//                    for (ClassificationAlgorithm cAlgorithm : Utilities.Config.GetMainClassificationAlgorithms())
//                        if (Utilities.InstanceVault.GetCrossValidationAssignments().HasAnyTestData(processor))
//                        {
//                            ArrayList<PredictionEvaluator> modelEvaluators = new ArrayList<PredictionEvaluator>();
//                            for (PredictionEvaluator evaluator : allModelEvaluators)
//                                if (evaluator.Processor.equals(processor) && evaluator.FeatureSelectionAlgorithm.equals(fsAlgorithm) && evaluator.ClassificationAlgorithm.equals(cAlgorithm))
//                                    modelEvaluators.add(evaluator);
//
//                            _modelSelectors.add(new ModelSelector(processor, fsAlgorithm, cAlgorithm, modelEvaluators));
//                        }
//        }
//
//        return _modelSelectors;
//    }


    /** This method deletes any temporary files or log files that might exist from previous runs of this experiment.
     *
     * @throws Exception
     */
    public void Clean() throws Exception
    {
        for (String directoryPath : Lists.CreateStringList(Settings.TEMP_DATA_DIR, Settings.TEMP_RESULTS_DIR))
        {
            Files.DeleteAllFilesAndDirectoriesRecursively(directoryPath + this.toString());
            Files.DeleteDirectory(directoryPath);
        }
    }

    @Override
    public String toString()
    {
        return Description + "Experiment";
    }
}