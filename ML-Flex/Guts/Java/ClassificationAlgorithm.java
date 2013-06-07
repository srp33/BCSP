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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** This class acts as a wrapper for performing classification tasks. It interprets parameters for executing these tasks, based on what has been configured in ML-Flex's configuration files.
 * @author Stephen Piccolo
 */
public class ClassificationAlgorithm
{
    public String Description;
    private AbstractMachineLearner _learner;
    private ArrayList<String> _algorithmParameters;

    /** Constructor
     *
     * @param description Description of the classification algorithm
     * @param learnerClassName Full name of the ML-Flex Java class that extends AbstractMachineLearner that contains the algorithm's logic
     * @param algorithmParameters List of parameters that are passed to the algorithm
     * @throws Exception
     */
    public ClassificationAlgorithm(String description, String learnerClassName, ArrayList<String> algorithmParameters) throws Exception
    {
        Description = description;
        _learner = (AbstractMachineLearner) ((Constructor) Class.forName(learnerClassName).getConstructor()).newInstance();
        _algorithmParameters = algorithmParameters;
    }

    /** This is a pass-through method to perform training and testing. It throws a detailed exception if it cannot be performed.
     *
     * @param trainData Training data instances
     * @param testData Testing data instances
     * @param dependentVariableInstances Dependent-variable instances
     * @return Predictions and model information
     * @throws Exception
     */
    public ModelPredictions TrainTest(DataInstanceCollection trainData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        if (!Lists.AreEqual(trainData.GetDataPointNames(), testData.GetDataPointNames()))
        {
            Utilities.Log.Debug("Data points in training but not in testing data:");
            Utilities.Log.Debug(Lists.GetDifference(trainData.GetDataPointNames(), testData.GetDataPointNames()));
            Utilities.Log.Debug("Data points in testing but not in training data:");
            Utilities.Log.Debug(Lists.GetDifference(testData.GetDataPointNames(), trainData.GetDataPointNames()));

            throw new Exception("The data points in the training data don't match those in the test data.");
        }

        if (trainData.Size() == 0 || testData.Size() == 0)
        {
            Utilities.Log.Info("No predictions will be made because the training and/or test set have no data");
            return new ModelPredictions("", new Predictions());
        }

        if (Lists.Intersect(trainData.GetIDs(), testData.GetIDs()).size() > 0)
        {
            Utilities.Log.Info("Algorithm: " + Description);
            Utilities.Log.Info("Train Data IDs");
            Utilities.Log.Info(trainData.GetIDs());
            Utilities.Log.Info("Test Data IDs");
            Utilities.Log.Info(testData.GetIDs());

            throw new Exception("The training and test sets overlap.");
        }

        if (trainData.GetNumDataPoints() == 0)
            throw new Exception("The training data had no data points");

        try
        {
            return _learner.TrainTest(_algorithmParameters, trainData, testData, dependentVariableInstances);
        }
        catch (Exception ex)
        {
            Utilities.Log.Info("Algorithm: " + Description);
            Utilities.Log.Info("Training data (first five instances):");
            Utilities.Log.Info(trainData.toShortString());
            Utilities.Log.Info("Test data (first five instances):");
            Utilities.Log.Info(testData.toShortString());
            Utilities.Log.Info("Dependent variable data (first five instances):");
            Utilities.Log.Info(dependentVariableInstances.toShortString());
            throw ex;
        }
    }

    @Override
    public String toString()
    {
        return Description;
    }
}