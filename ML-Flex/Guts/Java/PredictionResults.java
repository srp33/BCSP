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

import java.util.*;

/** This class provides support for evaluating a set of predictions that have been made and for summarizing the performance of the predictions using various metrics.
 * @author Stephen Piccolo
 */
public class PredictionResults
{
    public Predictions Predictions = new Predictions();

    /** Constructor
     *
     * @param predictions Predictions for which result metrics will be generated
     */
    public PredictionResults(Predictions predictions)
    {
        Predictions = predictions;
    }

    /** The accuracy represents the proportion of predictions that were correctly made (the actual class was the same as the predicted class).
     *
     * @return Accuracy value
     * @throws Exception
     */
    public double GetAccuracy() throws Exception
    {
        double numCorrect = 0.0;

        for (Prediction prediction : Predictions)
            if (prediction.Prediction.equals(prediction.DependentVariableValue))
                numCorrect++;

        return MathUtility.SmartDivide(numCorrect, (double)Predictions.Size());
    }

    /** The error rate represents the proportion of predictions that were incorrectly made (the actual class not the same as the predicted class).
     *
     * @return Error rate
     * @throws Exception
     */
    public double GetErrorRate() throws Exception
    {
        return 1 - GetAccuracy();
    }

    /** The baseline accuracy is the accuracy one would expect if the majority class were always selected by default.
     *
     * @return Baseline accuracy
     * @throws Exception
     */
    public double GetBaselineAccuracy() throws Exception
    {
        return MathUtility.SmartDivide((double)Lists.GetNumMatches(GetActuals(), Lists.GetMostFrequentValue(GetActuals())), (double)Predictions.Size());
    }

    /** The baseline error rate is the error rate one would expect if the majority class were always selected by default.
     *
     * @return Baseline error rate
     * @throws Exception
     */
    public double GetBaselineErrorRate() throws Exception
    {
        return 1 - GetBaselineAccuracy();
    }

    /** The baseline improvement represents the difference between the accuracy attained and the baseline accuracy. Positive values mean the predictions performed better than you would expect by chance.
     *
     * @return Improvement in accuracy over the baseline expectation
     * @throws Exception
     */
    public double GetBaselineImprovement() throws Exception
    {
        return GetAccuracy() - GetBaselineAccuracy();
    }

//    public double GetSquaredErrorPenalty(Predictions predictions, double threshold) throws Exception
//    {
//        double sqe = 0.0;
//
//        for (Prediction prediction : predictions)
//        {
//            double survival = Double.parseDouble(_rawDependentVariableInstances.Get(prediction.InstanceID).GetDataPointValue(0));
//
//            if (!prediction.WasCorrect())
//            {
//                double difference = (survival - threshold) * (survival - threshold);
//                sqe += difference;
//            }
//        }
//
//        return sqe;
//    }

    /** Indicates the number of actual instances that had a given dependent-variable value.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value
     * @throws Exception
     */
    public double GetNumActualsWithDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return Lists.GetNumMatches(GetActuals(), dependentVariableClass);
    }

    /** Indicates the number of actual instances that had a given dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value that were predicted correctly
     */
    public double GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(String dependentVariableClass)
    {
        double numCorrect = 0.0;

        for (Prediction prediction : Predictions)
            if (prediction.DependentVariableValue.equals(dependentVariableClass) && prediction.WasCorrect())
                numCorrect++;

        return numCorrect;
    }

    /** Indicates the number of actual instances that had a given dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Number of actual instances with the specified dependent-variable value that were predicted incorrectly
     */
    public double GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(String dependentVariableClass) throws Exception
    {
        double num = 0;

        for (Prediction prediction : Predictions)
            if (prediction.DependentVariableValue.equals(dependentVariableClass) && !prediction.WasCorrect())
                num++;

        return num;
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value
     * @throws Exception
     */
    public double GetProportionActualsWithDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClass(dependentVariableClass) / (double)Predictions.Size();
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value that were predicted correctly
     */
    public double GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClassThatWerePredictedCorrectly(dependentVariableClass) / GetNumActualsWithDependentVariableClass(dependentVariableClass);
    }

    /** Indicates the proportion of actual instances that had a given dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass The dependent-variable value in question
     * @return Proportion of actual instances with the specified dependent-variable value that were predicted incorrectly
     */
    public double GetProportionActualsWithDependentVariableClassThatWerePredictedIncorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumActualsWithDependentVariableClassThatWerePredictedIncorrectly(dependentVariableClass) / GetNumActualsWithDependentVariableClass(dependentVariableClass);
    }

    /** Indicates how many predictions were for a particular dependent-variable value.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumPredictionMatches(dependentVariableClass);
    }

    /** Indicates how many predictions were for a particular dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value that were predicted correctly
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClassCorrectly(String dependentVariableClass)
    {
        double numCorrect = 0.0;

        for (Prediction prediction : Predictions)
            if (prediction.Prediction.equals(dependentVariableClass) && prediction.WasCorrect())
                numCorrect++;

        return numCorrect;
    }

    /** Indicates how many predictions were for a particular dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Number of predictions for the specified dependent-variable value that were predicted incorrectly
     * @throws Exception
     */
    public double GetNumPredictedAsDependentVariableClassIncorrectly(String dependentVariableClass) throws Exception
    {
        double num = 0;

        for (Prediction prediction : Predictions)
            if (prediction.Prediction.equals(dependentVariableClass) && !prediction.WasCorrect())
                num++;

        return num;
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClass(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClass(dependentVariableClass) / (double)Predictions.Size();
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value and were predicted correctly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value that were predicted correctly
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClassCorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClassCorrectly(dependentVariableClass) / GetNumPredictedAsDependentVariableClass(dependentVariableClass);
    }

    /** Indicates the proportion of predictions that were for a particular dependent-variable value and were predicted incorrectly.
     *
     * @param dependentVariableClass Dependent-variable value in question
     * @return Proportion of predictions for the specified dependent-variable value that were predicted incorrectly
     * @throws Exception
     */
    public double GetProportionPredictedAsDependentVariableClassIncorrectly(String dependentVariableClass) throws Exception
    {
        return GetNumPredictedAsDependentVariableClassIncorrectly(dependentVariableClass) / GetNumPredictedAsDependentVariableClass(dependentVariableClass);
    }

    private Double _auc = Double.NaN;

    /** Convenience method for getting the area under the receiver operating characteristic curve (AUC) value for the predictions.
     *
     * @return AUC value (weighted by number of instances in each class)
     * @throws Exception
     */
    public double GetAuc() throws Exception
    {
        if (_auc.equals(Double.NaN))
            _auc = WekaInMemoryLearner.CalculateWeightedAreaUnderRoc(this.Predictions);

        return _auc;
    }

    /** Calculates the Youden index for the predictions. The Youden index is another metric that can be used to assess the performance of data sets with imbalanced class distributions. Youden index=1−(false positive rate+false negative rate) See "Ratio adjustment and calibration scheme for gene-wise normalization to enhance microarray inter-study prediction" by Cheng (Bioinformatics, 2009) "Index for rating diagnostic tests", Cancer, 3, 32–35. See also YOUDEN, W. J. (1950). Index for rating diagnostic tests. Cancer, 3(1), 32-5. http://www.ncbi.nlm.nih.gov/pubmed/15405679.
     *
     * @return Youden index value
     * @throws Exception
     */
    public double GetYoudenIndex() throws Exception
    {
        return GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues().get(0)) + GetProportionActualsWithDependentVariableClassThatWerePredictedCorrectly(Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues().get(1)) - 1;
    }

    /** Returns a list of actual classes associated with the predictions.
     *
     * @return Actual classes associated with the predictions
     * @throws Exception
     */
    public ArrayList<String> GetActuals() throws Exception
    {
        ArrayList<String> actuals = new ArrayList<String>();
        for (Prediction prediction : Predictions)
            actuals.add(prediction.DependentVariableValue);

        return actuals;
    }

    private double GetNumPredictionMatches(String classValue) throws Exception
    {
        double num = 0;

        for (Prediction prediction : Predictions)
            if (prediction.Prediction.equals(classValue))
                num++;

        return num;
    }
}
