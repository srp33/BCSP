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

/** When the dependent variable is continuous, it can be transformed to a discrete value. This transformer enables it to be transformed depending on whether the continuous value is greater than or less than a specific value.
 * @author Stephen Piccolo
 */
public class SpecificValueDependentValueTransformer extends AbstractDependentVariableTransformer
{
    private double _specificTime;
    private String _lowDescriptor;
    private String _highDescriptor;

    /** Constructor
     *
     * @param specificValue The value threshold that separates the "low" instances from the "high" instances
     * @param lowDescriptor The text descriptor for the "low" instances
     * @param highDescriptor The text descriptor for the "high" instances
     */
    public SpecificValueDependentValueTransformer(double specificValue, String lowDescriptor, String highDescriptor)
    {
        _specificTime = specificValue;
        _lowDescriptor = lowDescriptor;
        _highDescriptor = highDescriptor;
    }

    @Override
    protected String TransformDependentVariableValue(int outerFold, String rawValue) throws Exception
    {
        return Double.parseDouble(rawValue) > _specificTime ? _lowDescriptor : _highDescriptor;
    }
}
