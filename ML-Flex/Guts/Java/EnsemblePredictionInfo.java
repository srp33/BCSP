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

/** This class stores prediction information about individual predictions that can then be used for making ensemble predictions.
 * @author Stephen Piccolo
 */
public class EnsemblePredictionInfo
{
    Prediction OuterPrediction;
    PredictionResults InnerPredictionResults;
    String Description;

    /** Constructor
     *
     * @param prediction Prediction information
     * @param innerPredictionResults PredictionResults object that suggests how well the outer prediction will perform based on how well the corresponding inner-fold predictions performed
     * @param description Description of the prediction information
     */
    EnsemblePredictionInfo(Prediction prediction, PredictionResults innerPredictionResults, String description)
    {
        OuterPrediction = prediction;
        InnerPredictionResults = innerPredictionResults;
        Description = description;
    }

    /** This simple weight value can be used by ensemble methods to weight each prediction. This value is the AUC attained in the inner cross-validation folds.
     *
     * @return Simple weight value
     * @throws Exception
     */
    double GetSimpleWeight() throws Exception
    {
        //return MathUtility.Round(Utilities.RandomNumberGenerator.nextGaussian(), 5);
        return InnerPredictionResults.GetAuc();
    }

    /** This weight value can be used by ensemble methods. The goal is to place a higher emphasis on predictions that are expected to perform well.
     *
     * @return Squared weight value
     * @throws Exception
     */
    double GetSquaredWeight() throws Exception
    {
        double simpleWeight = GetSimpleWeight();
        return simpleWeight * simpleWeight;
    }

    @Override
    public boolean equals(Object obj)
    {
        EnsemblePredictionInfo compareObj = (EnsemblePredictionInfo)obj;

        return this.Description.equals(compareObj.Description);
    }

    @Override
    public int hashCode()
    {
        return this.Description.hashCode();
    }
}
