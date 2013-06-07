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

/** This class is designed to provide generic functionality for all data processors that store dependent variables. Data processors can only be used for dependent variables if they inherit from this class.
 * @author Stephen Piccolo
 */
public abstract class AbstractDependentVariableDataProcessor extends AbstractDataProcessor
{
    /** This method indicates which data point (assuming there may be multiple) should be used as the dependent variable.
     * @return Data point name to be used as the dependent variable
     * @throws Exception
     */
    public abstract String GetDependentVariableDataPointName() throws Exception;

    /** Dependent variables can be transformed before they are used in machine-learning analyses. For example, if the dependent variable is continuous, a transformer can be used to convert it to a discrete value. This method supports such transformations. By default, no transformation will occur.
     * @return Transformer object
     */
    protected AbstractDependentVariableTransformer GetDependentVariableTransformer()
    {
        return new NullDependentVariableTransformer();
    }

    /** This method indicates whether the raw dependent variable has continuous values. ML-Flex needs to know this for processing.
     * @return Value indicateing whether dependent variable has continuous values
     */
    protected boolean HasContinuousRawValues()
    {
        return false;
    }

    /** This method performs the work of transforming dependent variable instances for a given cross validation fold.
     *
     * @param dependentVariableInstances Raw dependent variable instances
     * @param outerFold Cross validation fold (outer)
     * @return Transformed data instances
     * @throws Exception
     */
    public DataInstanceCollection TransformDependentVariableInstances(DataInstanceCollection dependentVariableInstances, int outerFold) throws Exception
    {
        DataInstanceCollection transformed = dependentVariableInstances.Clone();

        for (DataValues dependentVariableInstance : transformed)
            transformed.UpdateDataPoint(GetDependentVariableDataPointName(), dependentVariableInstance.GetID(), GetDependentVariableTransformer().TransformDependentVariableValue(outerFold, dependentVariableInstance.GetDataPointValue(GetDependentVariableDataPointName())));

        return transformed;
    }

    private ArrayList<String> _uniqueDependentVariableValues = null;
    /** This method indicates all unique values for the dependent variable.
     * @return All unique values for the dependent variable.
     * @throws Exception
     */
    protected ArrayList<String> GetUniqueDependentVariableValues() throws Exception
    {
        if (_uniqueDependentVariableValues == null)
        {
            _uniqueDependentVariableValues = Utilities.InstanceVault.GetTransformedDependentVariableInstances().GetUniqueValues(GetDependentVariableDataPointName());
            Collections.sort(_uniqueDependentVariableValues);
        }

        return _uniqueDependentVariableValues;
    }

    /** This method indicates whether result metrics should be calculated independently for each cross validation fold. This approach provides a conservative (maybe too conservative?) estimate of classification performance. By default, this value is false.
     * @return Value indicating whether result metrics should be calculated independently for each cross validation fold.
     * @throws Exception
     */
    public boolean CalculateResultsSeparatelyForEachFold() throws Exception
    {
        return false;
    }
}
