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
import java.util.Random;

/** This data processor generates data randomly. It is used for negative testing (ensuring that no positive result is attained when none is expected. This class generates random values for the dependent variable.
 * @author Stephen Piccolo
 */
public class RandomDependentVariableDataProcessor extends AbstractDependentVariableDataProcessor
{
    private Random _random = new Random(Utilities.Iteration);
    private int _numInstances;

    /**
     * This constructor allows the user to specify how many random instances should be generated.
     * @param numInstances Number of data instances for which dependent variable values should be generated.
     */
    public RandomDependentVariableDataProcessor(Integer numInstances)
    {
        _numInstances = numInstances;
    }

    @Override
    public String GetDescription()
    {
        return super.GetDescription() + "_" + _numInstances + "I";
    }

    @Override
    protected String GetRawDataDirName()
    {
        return null;
    }

    @Override
    protected DataInstanceCollection GetTransformedInstances() throws Exception
    {
        DataInstanceCollection instances = new DataInstanceCollection();
        
        for (int i=0; i<_numInstances; i++)
            instances.Add(GetDependentVariableDataPointName(), "ID" + i, GenerateRandomDependentVariableValue());

        return instances;
    }

    @Override
    public String GetDependentVariableDataPointName() throws Exception
    {
        return "RandomDependentVariable";
    }

    @Override
    protected void ParseRawData() throws Exception
    {
    }

    private String GenerateRandomDependentVariableValue()
    {
        ArrayList<String> values = new ArrayList<String>();
        values.add("Value1");
        values.add("Value2");

        return Lists.PickRandomValue(values);
    }
}
