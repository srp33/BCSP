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

/** This class stores a list of CombinedPredictionInfo objects that can be used for making ensemble/combined predictions.
 * @author Stephen Piccolo
 */
public class EnsemblePredictionInfos
{
    public ArrayList<EnsemblePredictionInfo> Infos = new ArrayList<EnsemblePredictionInfo>();

    /** Adds a combined prediction information object to this collection.
     *
     * @param info Object to add
     * @return The current instance for convenience
     */
    public EnsemblePredictionInfos Add(EnsemblePredictionInfo info)
    {
        Infos.add(info);
        return this;
    }

    /** Gets all outer predictions.
     *
     * @return Outer predictions
     * @throws Exception
     */
    public Predictions GetOuterPredictions() throws Exception
    {
        Predictions predictions = new Predictions();

        for (EnsemblePredictionInfo x : Infos)
            predictions.Add(x.OuterPrediction);

        return predictions;
    }

    /** Indicates which data instance IDs were used for the inner cross-validation fold predictions.
     *
     * @return List of data instance IDs
     */
    public ArrayList<String> GetInnerInstanceIDs()
    {
        HashSet<String> ids = new HashSet<String>();

        for (EnsemblePredictionInfo info : Infos)
            ids.addAll(info.InnerPredictionResults.Predictions.GetInstanceIDs());

        return new ArrayList<String>(ids);
    }

//    public String GetOuterInstanceID() throws Exception
//    {
//        HashSet<String> ids = new HashSet<String>();
//
//        for (CombinedPredictionInfo info : Infos)
//            ids.add(info.OuterPrediction.InstanceID);
//
//        if (ids.size() == 0)
//            throw new Exception("No outer instance IDs were found.");
//        if (ids.size() > 1)
//            throw new Exception("Multiple outer instance IDs were found.");
//
//        return new ArrayList<String>(ids).get(0);
//    }

    private double GetMeanInnerSimpleWeights() throws Exception
    {
        ArrayList<Double> accuracies = new ArrayList<Double>();

        for (EnsemblePredictionInfo predictionInfo : Infos)
            accuracies.add(predictionInfo.GetSimpleWeight());

        return MathUtility.Mean(accuracies);
    }

//    public static CombinedPredictionInfos ThinUsingKappa(CombinedPredictionInfos predictionInfos, boolean useAbsoluteValue) throws Exception
//    {
//        if (predictionInfos.GetOuterPredictions().Size() <= 1)
//            return predictionInfos;
//
//        ArrayList<CombinedPredictionInfo> predictionInfosToKeep = new ArrayList<CombinedPredictionInfo>(predictionInfos.Infos);
//
//        double stopNum = (double) predictionInfos.Infos.size() * 0.5;
//
//        while ((double)predictionInfosToKeep.size() > stopNum)
//        {
//            //Log.Info("Thinning using Kappa. " + ((double)predictionInfosToKeep.size()) + " prediction infos remain.");
//
//            HashMap<CombinedPredictionInfo, Double> map = new HashMap<CombinedPredictionInfo, Double>();
//            for (CombinedPredictionInfo predictionInfo : predictionInfosToKeep)
//            {
//                ArrayList<CombinedPredictionInfo> toEvaluate = new ArrayList<CombinedPredictionInfo>(predictionInfosToKeep);
//                toEvaluate.remove(predictionInfo);
//
//                Double kappa = CalculateKappaStatistic(new CombinedPredictionInfos(toEvaluate));
//
//                //Log.Info("The overall kappa without " + predictionInfo.Description + " is " + kappa);
//                map.put(predictionInfo, kappa);
//            }
//
//            CombinedPredictionInfo lowest = GetLowestKappa(map, useAbsoluteValue);
//
//            //Log.Info("Number before removing: " + predictionInfosToKeep.size());
//            predictionInfosToKeep.remove(lowest);
//            //Log.Info("Number after removing: " + predictionInfosToKeep.size());
//        }
//
//        return new CombinedPredictionInfos(predictionInfosToKeep);
//    }
//
//    private static double CalculateKappaStatistic(CombinedPredictionInfos predictionInfos) throws Exception
//    {
//        ArrayList<String> innerInstanceIDs = predictionInfos.GetInnerInstanceIDs();
//
//        double L = (double)predictionInfos.Infos.size();
//        double N = (double)innerInstanceIDs.size();
//        double p = predictionInfos.GetMeanInnerSimpleWeights();
//
//        double sum = 0.0;
//
//        for (String instanceID : innerInstanceIDs)
//        {
//            double lzj = 0.0;
//
//            for (CombinedPredictionInfo predictionInfo : predictionInfos.Infos)
//                //if (predictionInfo.InnerPredictionResults.Predictions.GetSinglePrediction(instanceID).WasCorrect())
//                if (predictionInfo.InnerPredictionResults.Predictions.HasPrediction(instanceID) && predictionInfo.InnerPredictionResults.Predictions.GetSinglePrediction(instanceID).WasCorrect())
//                    lzj += 1.0;
//
//            sum += lzj * (L - lzj);
//        }
//
//        if (sum == 0.0)
//            return 1.0;
//
//        return 1 - (sum / L) / (N * (L-1) * p * (1-p));
//     }
//
//    private static CombinedPredictionInfo GetLowestKappa(HashMap<CombinedPredictionInfo, Double> map, boolean useAbsoluteValue) throws Exception
//    {
//        CombinedPredictionInfo lowestPredictionInfo = null;
//        Double lowestKappa = Double.MAX_VALUE;
//
//        for (Map.Entry<CombinedPredictionInfo, Double> entry : map.entrySet())
//        {
//            if (useAbsoluteValue)
//            {
//                if (Math.abs(entry.getValue()) < Math.abs(lowestKappa))
//                {
//                    lowestPredictionInfo = entry.getKey();
//                    lowestKappa = entry.getValue();
//                }
//            }
//            else
//            {
//                if (entry.getValue() < lowestKappa)
//                {
//                    lowestPredictionInfo = entry.getKey();
//                    lowestKappa = entry.getValue();
//                }
//            }
//        }
//
//        return lowestPredictionInfo;
//    }
}
