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

/** This class contains utility methods to support survival analyses.
 * @author Stephen Piccolo
 */
public class SurvivalHelper
{
    public static final String SURVIVAL_PREDICTION_DATA_POINT_NAME = "Prediction";

    private Predictions _predictions;

    /** Constructor
     *
     * @param predictions Predictions that have been made for a set of data instances
     */
    public SurvivalHelper(Predictions predictions)
    {
        _predictions = predictions;
    }

    /** Indicates whether a survival analysis should be performed for a given set of predictions
     *
     * @return Whether a survival analysis should be performed
     */
    public boolean ShouldCompute()
    {
        return _predictions.GetUniquePredictedClasses().size() > 1;
    }

    /** Calculates the log-rank statistic for the set of predictions that have been specified
     *
     * @return Log-rank statistic value
     * @throws Exception
     */
    public double GetLogRankStatistic() throws Exception
    {
        if (!ShouldCompute())
            return 1.0;

        HashMap<String, Survivals> classSurvivals = new HashMap<String, Survivals>();
        
        for (String predictedClass : _predictions.GetUniquePredictedClasses())
            classSurvivals.put(predictedClass, new Survivals());

        for (Prediction prediction : _predictions)
            classSurvivals.get(prediction.Prediction).Values.add(new Survival(Double.parseDouble(prediction.GetRawDependentVariableValue())));

        SurvivalGroups survivalGroups = new SurvivalGroups();
        for (Survivals survivals : classSurvivals.values())
            survivalGroups.Add(survivals);

        return new EvaluationMetrics().CalculateLogRankStatistic(survivalGroups);
    }

    /** Saves a text file with survival values, which enables downstream applications to produce graphs and interpret the survival results.
     *
     * @param directory Absolute path of the directory where the file will be saved
     * @param description Description used within file name
     * @param uniquifyFileName Whether a unique identifier should be added to the file name
     * @return Absolute path where the file was saved
     * @throws Exception
     */
    public String SaveAnalysisFile(String directory, String description, boolean uniquifyFileName) throws Exception
    {
        DataInstanceCollection predictorInstances = new DataInstanceCollection();

        for (String survivalInstanceID : _predictions.GetInstanceIDs())
        {
            if (!_predictions.HasPrediction(survivalInstanceID))
                continue;

            DataValues predictorInstance = new DataValues(survivalInstanceID);
            predictorInstance.AddDataPoint(SURVIVAL_PREDICTION_DATA_POINT_NAME, _predictions.GetSinglePrediction(survivalInstanceID).Prediction);

            predictorInstances.Add(predictorInstance);
        }

        description += uniquifyFileName ? "_" + Utilities.GetUniqueID() : "";

        AnalysisFileCreator fileCreator = new AnalysisFileCreator(directory, description, predictorInstances, null, Utilities.InstanceVault.GetRawDependentVariableInstances());
        fileCreator.CreateSurvivalFile();

        return fileCreator.GetSurvivalFilePath();
    }

    /** Wrapper method for saving a scalar survival result
     *
     * @param filePath File path where the scalar result file will be saved
     * @return Scalar result object
     * @throws Exception
     */
    public ScalarValue SaveScalarValue(String filePath) throws Exception
    {
        return Utilities.SaveScalarValue(filePath, "Log-Rank Statistic", GetLogRankStatistic());
    }
}
