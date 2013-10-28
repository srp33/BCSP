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

/** This transformer class is a wrapper around the ContinuousDependentVariableThresholdFinder class to transform a continuous dependent variable to a discrete one.
 * @author Stephen Piccolo
 */
public class ContinuousDependentVariableDiscretizationTransformer extends AbstractDependentVariableTransformer
{
    private ContinuousDependentVariableThresholdFinder.Metric _metric = ContinuousDependentVariableThresholdFinder.Metric.Auc;
    private ClassificationAlgorithm _classificationAlgorithm = Settings.ClassificationAlgorithms.get(Utilities.Config.GetStringValue("THRESHOLD_SELECTION_CLASSIFICATION_ALGORITHM", "c5"));

    private String _dependentVariableDataPointName;
    private String _continuousDependentVariableLowDescriptor;
    private String _continuousDependentVariableHighDescriptor;

    /** Constructor. */
    public ContinuousDependentVariableDiscretizationTransformer() throws Exception
    {
        _dependentVariableDataPointName = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_DATA_POINT_NAME", "ContinuousDependentVariable");
        _continuousDependentVariableLowDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_LOW_DESCRIPTOR", "LOW");
        _continuousDependentVariableHighDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_HIGH_DESCRIPTOR", "HIGH");
    }

    @Override
    public void Preprocess() throws Exception
    {
        GetThresholdFinder().SelectThresholds();
    }

    @Override
    protected DataInstanceCollection GetDependentVariableInstancesForCrossValidation() throws Exception
    {
        return Utilities.InstanceVault.GetRawDependentVariableInstances();
    }

    @Override
    protected String TransformDependentVariableValue(int outerFold, String rawValue) throws Exception
    {
        return GetThresholdFinder().DiscretizeSurvival(outerFold, rawValue);
    }

    private ContinuousDependentVariableThresholdFinder GetThresholdFinder() throws Exception
    {
        return new ContinuousDependentVariableThresholdFinder(Utilities.ProcessorVault.ThresholdSelectionDataProcessor, _classificationAlgorithm, _metric, false, _dependentVariableDataPointName, _continuousDependentVariableLowDescriptor, _continuousDependentVariableLowDescriptor);
    }
}