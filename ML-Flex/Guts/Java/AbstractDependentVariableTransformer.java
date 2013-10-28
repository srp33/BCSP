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

/** Dependent variables can be transformed before they are used in machine-learning analyses. For example, if the dependent variable is continuous, a transformer can be used to convert it to a discrete value. This class supports generic functionality for performing such transformations. Classes that inherit from this class can support custom transformations.
 * @author Stephen Piccolo
 */
public abstract class AbstractDependentVariableTransformer
{
    /** This method performs transformations of a single dependent variable value. In some cases, this transformation is different depending on the cross validation fold.
     * @param outerFold Cross validation fold number
     * @param rawValue Raw dependent variable value
     * @return Transformed value
     * @throws Exception
     */
    protected abstract String TransformDependentVariableValue(int outerFold, String rawValue) throws Exception;

    /** This method can be used to provide a custom description of the transformation approach that is used.
     * @return Description of transformation approach
     * @throws Exception
     */
    protected String GetDescription() throws Exception
    {
        return this.getClass().getSimpleName().replace("DependentVariableTransformer", "");
    }

    /** This method allows implementing classes to specify data instances that should be used for assigning data instances to cross validation folds. It may be desirable to do this if, for example, no cross-validation stratification is desired.
     * @return Data instances to be used
     * @throws Exception
     */
    protected DataInstanceCollection GetDependentVariableInstancesForCrossValidation() throws Exception
    {
        return Utilities.InstanceVault.GetTransformedDependentVariableInstances();
    }

    /** This method can be used to perform steps preliminary to transformation being performed. By default, no preprocessing is performed.
     * @throws Exception
     */
    public void Preprocess() throws Exception
    {
    }
}