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
import java.util.Collections;
import java.util.concurrent.Callable;

/** This class contains the logic necessary to empirically determine the "optimal" cutoff for converting a continuous dependent variable to a discrete dependent variable. This method uses the internal cross-validation folds to determine this value.
 * @author Stephen Piccolo
 */
public class ContinuousDependentVariableThresholdFinder
{
    public static final String SAVE_DIR = Settings.GUTS_DIR + "ThresholdSelection";

    private AbstractDataProcessor _processor;
    private FeatureSelectionAlgorithm _selectionAlgorithm = new FeatureSelectionAlgorithm("None", "", new ArrayList<String>());
    private ClassificationAlgorithm _classificationAlgorithm;
    private Metric _metric;
    private boolean _performHillClimbing;
    private String _survivalTransformedDataPointName;
    private String _lowDescriptor;
    private String _highDescriptor;

    /** Constructor
     *
     * @param processor Data processor containing the data that will be used for classification
     * @param classificationAlgorithm Classification algorithm
     * @param metric Metric that will be used to assess performance at each threshold
     * @param performHillClimbing Whether to perform hill climbining
     * @param survivalTransformedDataPointName Data point name for the transformed survival values
     * @param lowDescriptor Descriptor used to indicate values that were below a given threshold
     * @param highDescriptor Descriptor used to indicate values that were above a given threshold
     * @throws Exception
     */
    public ContinuousDependentVariableThresholdFinder(AbstractDataProcessor processor, ClassificationAlgorithm classificationAlgorithm, Metric metric, boolean performHillClimbing,
                                                      String survivalTransformedDataPointName, String lowDescriptor, String highDescriptor) throws Exception
    {
        _processor = processor;
        _classificationAlgorithm = classificationAlgorithm;
        _metric = metric;
        _performHillClimbing = performHillClimbing;
        _survivalTransformedDataPointName = survivalTransformedDataPointName;
        _lowDescriptor = lowDescriptor;
        _highDescriptor = highDescriptor;
    }

    /** This method orchestrates the processor of selecting the "optimal" threshold for each outer cross-validation fold.
     *
     * @throws Exception
     */
    public void SelectThresholds() throws Exception
    {
        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(_processor))
            if (GetBestThresholdScalar(outerFold) == null)
                Save(outerFold);
    }

    /** This method indicates the "optimal" threshold for a given outer cross-validation fold.
     *
     * @param outerFold Number of outer cross-validation fold
     * @return Selected threshold
     * @throws Exception
     */
    public double GetBestThreshold(int outerFold) throws Exception
    {
        return Double.parseDouble(GetBestThresholdScalar(outerFold));
    }

    /** This method saves the threshold results for a given cross-validation fold. It contains logic to share the work across multiple computing nodes.
     *
     * @param outerFold Number of outer cross-validation fold.
     * @throws Exception
     */
    public void Save(int outerFold) throws Exception
    {
        //TODO: Use LockedCallable class for this?
        if (!Files.FileExists(GetLockFilePath(outerFold)) && Files.CreateEmptyFile(GetLockFilePath(outerFold)))
        {
            try
            {
                ThresholdPredictions best = EvaluateThresholds(outerFold, GetCandidateThresholds(_survivalTransformedDataPointName));
                SaveThresholdResults(outerFold, best);
            }
            finally
            {
                Files.DeleteFile(GetLockFilePath(outerFold));
            }
        }
    }

    private ThresholdPredictions EvaluateThresholds(int outerFold, ArrayList<Double> thresholds) throws Exception
    {
        for (double threshold : thresholds)
        {
            final ThresholdPredictionDataProcessor processor = new ThresholdPredictionDataProcessor(outerFold, threshold);
            Utilities.Log.Debug("Evaluating survival threshold (" + threshold + ") for " + Utilities.BuildDescription(processor.GetDescription()));

            processor.GetModelEvaluator().SavePredictions(false);

            Utilities.InstanceVault.RemoveAnalysisInstancesFromCache(processor);
            Utilities.Log.Debug("Done evaluating survival threshold (" + threshold + ") for " + Utilities.BuildDescription(processor.GetDescription()));
        }

        return GetBestThresholdPredictions(outerFold, thresholds);
    }

    private String GetLockFilePath(int outerFold)
    {
        return GetDescription(outerFold) + ".txt";
    }

    private void SaveThresholdResults(int outerFold, ThresholdPredictions best) throws Exception
    {
        if (GetBestThresholdScalar(outerFold) == null)
        {
            ResultsSaver.SaveScalarResults(Utilities.GetScalarResultsFilePath(GetDescription(outerFold)), new PredictionResults(best.Predictions));

            AnalysisFileCreator.WriteAucInputFile(best.Predictions, ContinuousDependentVariableThresholdFinder.SAVE_DIR + GetDescription(outerFold) + ".AUC.txt");

            SurvivalHelper survivalHelper = new SurvivalHelper(best.Predictions);
            survivalHelper.SaveAnalysisFile(ContinuousDependentVariableThresholdFinder.SAVE_DIR, GetDescription(outerFold), false);

            SaveResultsToFile(outerFold);

            Utilities.SaveScalarValue(Utilities.GetStatisticsFilePath(GetDescription(outerFold)), "Best Threshold", best.Threshold);
        }
    }

    private void SaveResultsToFile(int outerFold) throws Exception
    {
        String filePath = SAVE_DIR + GetDescription(outerFold) + "_Results.txt";

        if (!Files.FileExists(filePath))
        {
            Files.WriteTextToFile(filePath, "Threshold\t");
            Files.AppendLineToFile(filePath, Lists.Join(Lists.CreateStringList(GetCandidateThresholds(_survivalTransformedDataPointName)), "\t"));
        }

        ArrayList<Double> sortedResults = new ArrayList<Double>();
        for (ThresholdPredictions x : GetThresholdPredictions(outerFold, GetCandidateThresholds(_survivalTransformedDataPointName)))
            sortedResults.add(x.ResultMeasurer.GetResult());

        Files.AppendTextToFile(filePath, Utilities.GetUniqueID() + "\t");
        Files.AppendLineToFile(filePath, Lists.Join(Lists.CreateStringListFromDoubleList(sortedResults), "\t"));
    }

    private ThresholdPredictions GetBestThresholdPredictions(int outerFold, ArrayList<Double> thresholds) throws Exception
    {
        ArrayList<ThresholdPredictions> best = new ArrayList<ThresholdPredictions>();

        for (ThresholdPredictions x : GetThresholdPredictions(outerFold, thresholds))
        {
            if (best.size() == 0)
            {
                best.add(x);
                continue;
            }

            if (x.ResultMeasurer.Beats(best.get(0).ResultMeasurer))
            {
                best.clear();
                best.add(x);
            }
            else
            {
                if (x.ResultMeasurer.Ties(best.get(0).ResultMeasurer))
                    best.add(x);
            }
        }

        if (best.size() <= 1)
            return best.get(0);

        double medianIndex = MathUtility.Median(Lists.CreateDoubleList(0.0, (double) best.size()));
        Collections.sort(best);

        if (MathUtility.IsOdd(best.size()))
            return best.get((int)medianIndex);

        return best.get((int)Math.ceil(medianIndex));
    }

    private ArrayList<ThresholdPredictions> GetThresholdPredictions(final int outerFold, ArrayList<Double> thresholds) throws Exception
    {
        Utilities.Log.Info("Getting threshold predictions and calculating results for outer fold " + outerFold);
        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler(1);

        for (final double threshold : thresholds)
        {
            taskHandler.Add(new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    ThresholdPredictionDataProcessor processor = new ThresholdPredictionDataProcessor(outerFold, threshold);
                    Predictions predictions = processor.GetModelEvaluator().GetInnerPredictions();

                    ThresholdPredictions thresholdPredictions = new ThresholdPredictions(threshold, predictions);
                    thresholdPredictions.ResultMeasurer.GetResult();

                    Utilities.InstanceVault.RemoveAnalysisInstancesFromCache(processor);

                    return thresholdPredictions;
                }
            });
        }

        ArrayList<ThresholdPredictions> allThresholdPredictions = new ArrayList<ThresholdPredictions>();
        for (Object x : taskHandler.Execute())
            allThresholdPredictions.add((ThresholdPredictions)x);

        return allThresholdPredictions;
    }

    private String GetBestThresholdScalar(int outerFold) throws Exception
    {
        return Utilities.GetScalarValue(Utilities.GetStatisticsFilePath(GetDescription(outerFold)), "Best Threshold");
    }

    private String GetDescription(int outerFold)
    {
        return Utilities.BuildDescription(_processor.GetDescription(), _classificationAlgorithm, _metric, (_performHillClimbing ? "HillClimbing" : "NoHillClimbing"), "OuterFold" + outerFold);
    }

    private static ArrayList<Double> GetCandidateThresholds(String survivalTransformedDataPointName) throws Exception
    {
        ArrayList<Double> candidates = new ArrayList<Double>();

//        for (double i=90.0; i<=900.0; i+=90.0)
//            candidates.add(i);

        ArrayList<String> survivalStrings = Utilities.InstanceVault.GetRawDependentVariableInstances().GetUniqueValues(survivalTransformedDataPointName);
        ArrayList<Double> survivals = Lists.CreateDoubleList(survivalStrings);
        Collections.sort(survivals);

        for (int i = 0; i < survivals.size() - 1; i++)
        {
            double candidate = (survivals.get(i) + survivals.get(i + 1)) / 2;
            candidates.add(candidate);
        }

        if (candidates.size() == 0)
            throw new Exception("No candidate thresholds were available.");

        return candidates;
    }

    private static DataInstanceCollection DiscretizeDependentVariableInstances(DataInstanceCollection rawDependentVariableInstances, double threshold, String lowDescriptor, String highDescriptor) throws Exception
    {
        DataInstanceCollection instances = rawDependentVariableInstances.Clone();

        for (DataValues instance : instances)
        {
            String dataPointName = Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName();
            String rawSurvival = instance.GetDataPointValue(dataPointName);

            instance.UpdateDataPoint(dataPointName, DiscretizeSurvival(rawSurvival, threshold, lowDescriptor, highDescriptor));
        }

        return instances;
    }

    /** This pass-through method performs the work of discretizing a raw dependent-variable value based on the selected threshold associated with the given outer cross-validation fold.
     *
     * @param outerFold Number of outer cross-validation fold
     * @param rawSurvival Raw survival value
     * @return Discretized survival value
     * @throws Exception
     */
    public String DiscretizeSurvival(int outerFold, String rawSurvival) throws Exception
    {
        return DiscretizeSurvival(rawSurvival, GetBestThreshold(outerFold), _lowDescriptor, _highDescriptor);
    }

    private static String DiscretizeSurvival(String rawSurvival, double threshold, String lowDescriptor, String highDescriptor)
    {
        if (Double.parseDouble(rawSurvival) < threshold)
            return highDescriptor;

        return lowDescriptor;
    }

    private class ThresholdPredictionDataProcessor extends AbstractDataProcessor
    {
        private int _outerFold;
        private double _threshold;

        public ThresholdPredictionDataProcessor(int outerFold, double threshold)
        {
            _outerFold = outerFold;
            _threshold = threshold;
        }

        @Override
        public String GetDescription()
        {
            return _processor.GetDescription() + "_" + _metric + "_" + _selectionAlgorithm + "_" + _classificationAlgorithm + "_Threshold" + _threshold + "_" + (_performHillClimbing ? "HillClimbing" : "NoHillClimbing");
        }

        @Override
        protected double GetProportionMissingPerInstanceOK()
        {
            return 1.0;
        }

        @Override
        protected double GetProportionMissingPerDataPointOK()
        {
            return 1.0;
        }

        private DataInstanceCollection _transformedInstances = null;
        @Override
        protected DataInstanceCollection GetTransformedInstances() throws Exception
        {
            if (_transformedInstances == null)
                _transformedInstances = Utilities.InstanceVault.GetAnalysisInstances(_processor, null, null);

            return _transformedInstances;
        }

        DataInstanceCollection GetDependentVariableInstances() throws Exception
        {
            return DiscretizeDependentVariableInstances(Utilities.InstanceVault.GetRawDependentVariableInstances(), _threshold, _lowDescriptor, _highDescriptor);
        }

        PredictionEvaluator GetModelEvaluator() throws Exception
        {
            return new PredictionEvaluator(this, _selectionAlgorithm, _classificationAlgorithm, GetTransformedInstances().GetNumDataPoints(), _outerFold, GetDependentVariableInstances());
        }
    }

    private class ThresholdPredictions implements Comparable<ThresholdPredictions>
    {
        double Threshold;
        Predictions Predictions;
        AbstractResultMeasurer ResultMeasurer;

        ThresholdPredictions(double threshold, Predictions predictions) throws Exception
        {
            Threshold = threshold;
            Predictions = predictions;
            ResultMeasurer = GetResultMeasurer();
        }

        private AbstractResultMeasurer GetResultMeasurer() throws Exception
        {
            if (_metric.equals(Metric.Kappa))
                return new KappaResultMeasurer(Predictions);
            if (_metric.equals(Metric.Auc))
                return new AucResultMeasurer(Predictions);

            return new LogRankStatisticResultMeasurer(Predictions);
        }

        public int compareTo(ThresholdPredictions thresholdPredictions)
        {
            return ((Double)Threshold).compareTo(((Double)thresholdPredictions.Threshold));
        }
    }

    /** This enum supports specification of any of a number of metrics to be used in determining the optimal threshold. */
    public enum Metric
    {
        Kappa,
        Auc,
        LogRankStatistic
    }
}