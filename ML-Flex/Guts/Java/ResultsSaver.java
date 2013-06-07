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
import java.util.HashSet;

/** This class has functionality to save results from machine-learning analyses. These results are saved in a variety of text files in the Output directory.
 * @author Stephen Piccolo
 */
public class ResultsSaver
{
    /** Saves results to output files, depending on the type of analysis it was
     *
     * @return Indicates whether the save was successful
     * @throws Exception
     */
    public Boolean SavePredictionResults(ModelSelector modelSelector) throws Exception
    {
        if (Utilities.ProcessorVault.DependentVariableDataProcessor.CalculateResultsSeparatelyForEachFold())
            SavePredictionResultsForEachFold(modelSelector);
        else
            SavePredictionResultsForAllFolds(modelSelector);

        return Boolean.TRUE;
    }

    /** Saves results to output files, depending on the type of analysis it was
     *
     * @return Indicates whether the save was successful
     * @throws Exception
     */
    public Boolean SaveEnsembleResults(AbstractEnsemblePredictor ensemblePredictor) throws Exception
    {
        if (Utilities.ProcessorVault.DependentVariableDataProcessor.CalculateResultsSeparatelyForEachFold())
            SaveEnsembleResultsForEachFold(ensemblePredictor);
        else
            SaveEnsembleResultsForAllFolds(ensemblePredictor);

        return Boolean.TRUE;
    }

    private void SavePredictionResultsForEachFold(ModelSelector modelSelector) throws Exception
    {
        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
        {
            Predictions predictions = modelSelector.GetBestOuterPredictions(outerFold);
            int bestNumFeatures = modelSelector.GetBestNumFeatures(outerFold);
            String description = Utilities.BuildDescription(modelSelector.Processor.GetDescription(), modelSelector.FeatureSelectionAlgorithm, modelSelector.ClassificationAlgorithm, "OuterFold" + outerFold);

            SavePredictionResults(modelSelector, predictions, bestNumFeatures, description);
        }
    }

    private void SavePredictionResultsForAllFolds(ModelSelector modelSelector) throws Exception
    {
        Predictions predictions = modelSelector.GetBestOuterPredictionsAllFolds();
        int bestNumFeatures = modelSelector.GetBestNumFeaturesAllFolds();
        String description = Utilities.BuildDescription(modelSelector.Processor.GetDescription(), modelSelector.FeatureSelectionAlgorithm, modelSelector.ClassificationAlgorithm);

        SavePredictionResults(modelSelector, predictions, bestNumFeatures, description);
    }

    private void SavePredictionResults(ModelSelector modelSelector, Predictions predictions, int bestNumFeatures, String description) throws Exception
    {
        Utilities.SaveScalarValue(Utilities.GetScalarResultsFilePath(description), "Best Num Features", bestNumFeatures);
        SaveScalarResults(Utilities.GetScalarResultsFilePath(description), new PredictionResults(predictions));

        //AnalysisFileCreator.WriteAucInputFile(predictions, Settings.GetOutputDir() + outFileDescription + "_AUC.txt");

        if (Utilities.ProcessorVault.DependentVariableDataProcessor.HasContinuousRawValues())
            new SurvivalHelper(predictions).SaveAnalysisFile(Settings.GetExperimentOutputDir(true), description + "_SurvivalInfo", false);

        String innerResultsIterationFilePath = Settings.GetExperimentOutputDir(true) + modelSelector.GetSimpleDescription() + "_InnerNumFeatures_AUC.txt";
        String outerResultsIterationFilePath = Settings.GetExperimentOutputDir(true) + modelSelector.GetSimpleDescription() + "_OuterNumFeatures_AUC.txt";
        String innerResultsOverallFilePath = Settings.GetExperimentOutputDir(false) + modelSelector.GetSimpleDescription() + "_InnerNumFeatures_AUC.txt";
        String outerResultsOverallFilePath = Settings.GetExperimentOutputDir(false) + modelSelector.GetSimpleDescription() + "_OuterNumFeatures_AUC.txt";

        Files.DeleteFile(innerResultsIterationFilePath);
        Files.DeleteFile(outerResultsIterationFilePath);

        if (Utilities.Config.GetNumIterations() > 1 && Utilities.IsFirstIteration())
        {
            Files.DeleteFile(innerResultsOverallFilePath);
            Files.DeleteFile(outerResultsOverallFilePath);
        }

        ArrayList<Integer> numFeaturesOptions = Utilities.Config.GetNumFeaturesOptions(modelSelector.Processor, modelSelector.FeatureSelectionAlgorithm);

        ArrayList<String> numFeaturesHeaderItems = Lists.CreateStringListFromIntegerList(numFeaturesOptions);
        numFeaturesHeaderItems.add(0, "NumFeatures");

        Files.AppendLineToFile(innerResultsIterationFilePath, Lists.Join(numFeaturesHeaderItems, "\t"));
        Files.AppendLineToFile(outerResultsIterationFilePath, Lists.Join(numFeaturesHeaderItems, "\t"));

        if (Utilities.Config.GetNumIterations() > 1 && Utilities.IsFirstIteration())
        {
            Files.AppendLineToFile(innerResultsOverallFilePath, Lists.Join(numFeaturesHeaderItems, "\t"));
            Files.AppendLineToFile(outerResultsOverallFilePath, Lists.Join(numFeaturesHeaderItems, "\t"));
        }

        ArrayList<String> innerNumFeaturesLineItems = new ArrayList<String>();
        ArrayList<String> outerNumFeaturesLineItems = new ArrayList<String>();

        for (int numFeatures : numFeaturesOptions)
        {
            innerNumFeaturesLineItems.add(String.valueOf(new PredictionResults(modelSelector.GetInnerPredictionsAllFolds(numFeatures)).GetAuc()));
            outerNumFeaturesLineItems.add(String.valueOf(new PredictionResults(modelSelector.GetOuterPredictionsAllFolds(numFeatures)).GetAuc()));
        }

        innerNumFeaturesLineItems.add(0, "Result");
        outerNumFeaturesLineItems.add(0, "Result");

        Files.AppendLineToFile(innerResultsIterationFilePath, Lists.Join(innerNumFeaturesLineItems, "\t"));
        Files.AppendLineToFile(outerResultsIterationFilePath, Lists.Join(outerNumFeaturesLineItems, "\t"));

        if (Utilities.Config.GetNumIterations() > 1)
        {
            innerNumFeaturesLineItems.set(0, "Iteration" + Utilities.Iteration);
            outerNumFeaturesLineItems.set(0, "Iteration" + Utilities.Iteration);

            Files.AppendLineToFile(innerResultsOverallFilePath, Lists.Join(innerNumFeaturesLineItems, "\t"));
            Files.AppendLineToFile(outerResultsOverallFilePath, Lists.Join(outerNumFeaturesLineItems, "\t"));
        }

        SavePredictionInfoFile(predictions, Settings.GetExperimentOutputDir(true) + Utilities.BuildDescription(description));
    }

    private void SaveEnsembleResultsForEachFold(AbstractEnsemblePredictor ensemblePredictor) throws Exception
    {
        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
        {
            Predictions ensemblePredictions = ensemblePredictor.GetEnsemblePredictions(outerFold);
            Predictions foldPredictions = ensemblePredictions.GetInstancePredictions(Utilities.InstanceVault.GetCrossValidationAssignments().GetTestIDs(outerFold));
            ResultsSaver.SaveScalarResults(Utilities.GetScalarResultsFilePath(ensemblePredictor.GetDescription()), new PredictionResults(foldPredictions));
            ResultsSaver.SavePredictionInfoFile(foldPredictions, Settings.GetExperimentOutputDir(true) + ensemblePredictor.GetDescription() + "_OuterFold" + outerFold);
        }
    }

    private void SaveEnsembleResultsForAllFolds(AbstractEnsemblePredictor ensemblePredictor) throws Exception
    {
        Predictions ensemblePredictions = ensemblePredictor.GetEnsemblePredictions();
        ResultsSaver.SaveScalarResults(Utilities.GetScalarResultsFilePath(ensemblePredictor.GetDescription()), new PredictionResults(ensemblePredictions));
        ResultsSaver.SavePredictionInfoFile(ensemblePredictions, Settings.GetExperimentOutputDir(true) + ensemblePredictor.GetDescription());
    }

    /** Saves results to output files for a variety of performance metrics.
     *
     * @param filePath File path where the scalar results will be saved
     * @param results Prediction results
     * @return List of scalar results
     * @throws Exception
     */
    public static void SaveScalarResults(String filePath, PredictionResults results) throws Exception
    {
        if (results.Predictions.Size() == 0)
            return;

        Utilities.SaveScalarValue(filePath, "Error rate", results.GetErrorRate());
        Utilities.SaveScalarValue(filePath, "Baseline error rate", results.GetBaselineErrorRate());
        Utilities.SaveScalarValue(filePath, "Baseline improvement", results.GetBaselineImprovement());
        Utilities.SaveScalarValue(filePath, "Accuracy", results.GetAccuracy());

        for (String dependentVariableClass : Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())
        {
            Utilities.SaveScalarValue(filePath, "Number instances with dependent variable class " + dependentVariableClass, results.GetNumActualsWithDependentVariableClass(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Number instances with dependent variable class " + dependentVariableClass + " predicted correctly", results.GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Number instances with dependent variable class " + dependentVariableClass + " predicted incorrectly", results.GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Number instances predicted as dependent variable class " + dependentVariableClass, results.GetNumPredictedAsDependentVariableClass(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Number instances predicted as dependent variable class " + dependentVariableClass + " correctly", results.GetNumPredictedAsDependentVariableClassCorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Number instances predicted as dependent variable class " + dependentVariableClass + " incorrectly", results.GetNumPredictedAsDependentVariableClassIncorrectly(dependentVariableClass));

            Utilities.SaveScalarValue(filePath, "Proportion instances with dependent variable class " + dependentVariableClass, results.GetProportionActualsWithDependentVariableClass(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Proportion instances with dependent variable class " + dependentVariableClass + " predicted correctly", results.GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Proportion instances with dependent variable class " + dependentVariableClass + " predicted incorrectly", results.GetProportionActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Proportion instances predicted as dependent variable class " + dependentVariableClass, results.GetProportionPredictedAsDependentVariableClass(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Proportion instances predicted as dependent variable class " + dependentVariableClass + " correctly", results.GetProportionPredictedAsDependentVariableClassCorrectly(dependentVariableClass));
            Utilities.SaveScalarValue(filePath, "Proportion instances predicted as dependent variable class " + dependentVariableClass + " incorrectly", results.GetProportionPredictedAsDependentVariableClassIncorrectly(dependentVariableClass));
        }

        Utilities.SaveScalarValue(filePath, "AUC", results.GetAuc());

        if (Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues().size() == 2)
            Utilities.SaveScalarValue(filePath, "Youden Index", results.GetYoudenIndex());

        if (Utilities.ProcessorVault.DependentVariableDataProcessor.HasContinuousRawValues())
            new SurvivalHelper(results.Predictions).SaveScalarValue(filePath);
    }

    /** Saves an output file that summarizes predictions that were made. This output file can be used by external applications to analyze the predictions.
     *
     * @param predictions Predictions object
     * @param outFilePathPrefix Text that will prepended to the file names that are saved
     * @throws Exception
     */
    public static void SavePredictionInfoFile(Predictions predictions, String outFilePathPrefix) throws Exception
    {
        ArrayList<String> dependentVariableClasses = Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues();
        ArrayList<String> output = new ArrayList<String>();

        ArrayList<String> headerVals = Lists.CreateStringList("InstanceID", "DependentVariableValue", "Prediction");
        for (String x : dependentVariableClasses)
            headerVals.add(x + "_Probability");
        output.add(Lists.Join(headerVals, "\t"));

        for (Prediction prediction : predictions)
        {
            ArrayList<String> predictionVals = Lists.CreateStringList(prediction.InstanceID, prediction.DependentVariableValue, prediction.Prediction);

            for (int i=0; i<dependentVariableClasses.size(); i++)
                predictionVals.add(String.valueOf(prediction.ClassProbabilities.get(i)));

            output.add(Lists.Join(predictionVals, "\t"));
        }

        Files.WriteLinesToFile(outFilePathPrefix + "_PredictionInfo.txt", output);
    }

    /** Saves a text file that summarizes all the scalar results for this experiment into a single tab-delimited text file. This file gets saved into the Output directory.
     *
     * @throws Exception
     */
    public static Boolean SaveScalarResultsSummary() throws Exception
    {
        ArrayList<ScalarValue> allResults = Utilities.GetAllScalarResultsValues();

        ArrayList<String> outFileLines = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> metrics = new ArrayList<String>();

        for (ScalarValue result : allResults)
        {
            descriptions.add(result.Description);
            metrics.add(result.Metric);
        }

        descriptions = Lists.Sort(new ArrayList<String>(new HashSet<String>(descriptions)));
        metrics = Lists.Sort(new ArrayList<String>(new HashSet<String>(metrics)));

        outFileLines.add(Lists.Join(Lists.InsertIntoStringList(metrics, "", 0), "\t"));

        for (String description : descriptions)
        {
            ArrayList<String> outLineItems = Lists.CreateStringList(description);

            for (String metric : metrics)
                outLineItems.add(GetScalarResultValue(allResults, description, metric));

            outFileLines.add(Lists.Join(outLineItems, "\t"));
        }

        Files.WriteLinesToFile(Settings.GetExperimentOutputDir(false) + "OverallResultsSummary.txt", outFileLines);

        return Boolean.TRUE;
    }

    private static String GetScalarResultValue(ArrayList<ScalarValue> results, String description, String metric) throws Exception
    {
        ArrayList<String> values = new ArrayList<String>();

        for (ScalarValue result : results)
            if (result.Description.equals(description) && result.Metric.equals(metric))
                values.add(String.valueOf(result.Value));

        if (values.size() == 0)
            return "NA";
        if (values.size() == 1)
            return values.get(0);

        return String.valueOf(MathUtility.Mean(Lists.CreateDoubleList(values)));
    }
}