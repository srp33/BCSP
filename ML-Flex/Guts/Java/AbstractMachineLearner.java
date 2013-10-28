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

/** This class provides a template for classes that perform general machine-learning tasks. It can be overridden with custom classes that implement these tasks for new third-party packages or for custom implementation.
 * @author Stephen Piccolo
 */
public abstract class AbstractMachineLearner
{
    /** This method is used by custom machine learner classes either 1) to select features that the algorithm determines to be most relevant to the dependent variable, or 2) to rank all features according to their relevance to the dependent variable. If feature selection is used, the features should still be ranked, if possible.
     *
     * @param algorithmParameters General parameter values that are used by the machine learner to execute. These are usually stored in the FeatureSelectionParameters.txt file in the Config directory.
     * @param trainData Training data instances
     * @param dependentVariableInstances Dependent variable data instances (one instance should exist for each training data instance)
     * @return A list of data point names, ranked according to their perceived relevance to the dependent variable
     * @throws Exception
     */
    protected abstract ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception;

    /** This method is used by custom machine learner classes to perform classification.
     *
     * @param classificationParameters General parameter values that are used by the machine learner to perform classification. These parameters are usually stored in the ClassificationParameters.txt file in the Config directory.
     * @param trainingData Training data instances
     * @param testData Test data instances
     * @param dependentVariableInstances Dependent variable data instances (one instance should exist for each training and test data instance)
     * @return Predictions for each test data instance
     * @throws Exception
     */
    protected abstract ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception;
}
