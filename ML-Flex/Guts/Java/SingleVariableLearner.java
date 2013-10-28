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

/** This learner can be used for validation purposes. It selects a single variable. To do this, it sorts the data points alphabetically and then picks the variable at the index of the current iteration. So this learner makes most sense when ML-Flex is run for multiple iterations. The point of this is to show what kind of performance you get if you repeatedly construct models on single variables. If you do, then the data may contain many redundant predictors, or there may be some kind of systematic bias in the data.
 *
 */
public class SingleVariableLearner extends AbstractMachineLearner
{
    @Override
    protected ArrayList<String> SelectOrRankFeatures(ArrayList<String> algorithmParameters, DataInstanceCollection trainData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        ArrayList<String> features = Lists.CreateStringList(trainData.GetDataPointNames().get(Utilities.Iteration - 1));

        Collections.sort(features);

        return features;
    }

    @Override
    protected ModelPredictions TrainTest(ArrayList<String> classificationParameters, DataInstanceCollection trainingData, DataInstanceCollection testData, DataInstanceCollection dependentVariableInstances) throws Exception
    {
        throw new Exception("Method not implemented.");
    }
}
