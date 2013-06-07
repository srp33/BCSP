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

/** When the dependent variable contains continuous values, this class transforms those values to discrete values, depending on whether each value is higher or lower than the median value.
 * @author Stephen Piccolo
 */
public class MedianContinuousDependentVariableTransformer extends AbstractDependentVariableTransformer
{
    private String _dependentVariableDataPointName;
    private String _continuousDependentVariableLowDescriptor;
    private String _continuousDependentVariableHighDescriptor;

    /** Constructor
     *
     * @throws Exception
     */
    public MedianContinuousDependentVariableTransformer() throws Exception
    {
        _dependentVariableDataPointName = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_DATA_POINT_NAME", "ContinuousDependentVariable");
        _continuousDependentVariableLowDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_LOW_DESCRIPTOR", "LTS");
        _continuousDependentVariableHighDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_HIGH_DESCRIPTOR", "STS");
    }

    @Override
    protected String TransformDependentVariableValue(int outerFold, String rawValue) throws Exception
    {
        double survival = Double.parseDouble(rawValue);
        return (survival > GetMedianSurvival()) ? _continuousDependentVariableLowDescriptor : _continuousDependentVariableHighDescriptor;
    }

    private Double _medianSurvival = Double.NaN;
    private double GetMedianSurvival() throws Exception
    {
        if (_medianSurvival.equals(Double.NaN))
        {
            DataInstanceCollection dependentVariableInstances = Utilities.InstanceVault.GetRawDependentVariableInstances();
            dependentVariableInstances.Get(Utilities.InstanceVault.GetAnalysisInstanceIDs());

            ArrayList<Double> values = Lists.CreateDoubleList(dependentVariableInstances.GetDataPointValues(_dependentVariableDataPointName).GetAllValues());

            _medianSurvival = MathUtility.Median(values);
        }

        return _medianSurvival;
    }
}
