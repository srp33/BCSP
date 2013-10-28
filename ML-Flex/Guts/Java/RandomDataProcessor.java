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

/** This data processor generates data randomly. It is used for negative testing (ensuring that no positive result is attained when none is expected.
 * @author Stephen Piccolo
 */
public class RandomDataProcessor extends AbstractDataProcessor
{
    private int _numInstances;
    private int _numDiscreteDataPoints;
    private int _numContinuousDataPoints;

    /** This constructor allows the user to specify how many random instances should be generated and how many data points should be generated per data instance.
     * @param numInstances Number of data instances for which random data should be generated.
     * @param numDiscreteDataPoints Number of discrete data points for which data should be generated (the discrete data points are either "Value1" or "Value2")
     * @param numContinuousDataPoints Number of continuous data points for which data should be generated (the data points come from a standard normal distrubution)
     */
    public RandomDataProcessor(Integer numInstances, Integer numDiscreteDataPoints, Integer numContinuousDataPoints)
    {
        _numInstances = numInstances;
        _numDiscreteDataPoints = numDiscreteDataPoints;
        _numContinuousDataPoints = numContinuousDataPoints;
    }

    @Override
    public String GetDescription()
    {
        return super.GetDescription() + "_" + _numInstances + "I_" + _numDiscreteDataPoints + "D_" + _numContinuousDataPoints + "C";
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
        {
            String instanceID = "ID" + i;

            for (String dataPointName : GenerateDataPointNames(_numDiscreteDataPoints, "D"))
                instances.Add(dataPointName, instanceID, GenerateRandomDiscreteValue());

            for (String dataPointName : GenerateDataPointNames(_numContinuousDataPoints, "C"))
                instances.Add(dataPointName, instanceID, String.valueOf(GenerateRandomContinuousValue()));
        }

        return instances;
    }

    @Override
    protected void ParseRawData() throws Exception
    {
    }

    private static ArrayList<String> GenerateDataPointNames(int number, String suffix)
    {
        ArrayList<String> alphabet = Lists.ALPHABET;
        ArrayList<String> dataPoints = new ArrayList<String>();

        for (int i = 0; i < alphabet.size() && dataPoints.size() < number; i++)
            for (int j = 0; j < alphabet.size() && dataPoints.size() < number; j++)
                for (int k = 0; k < alphabet.size() && dataPoints.size() < number; k++)
                    for (int l = 0; l < alphabet.size() && dataPoints.size() < number; l++)
                        for (int m = 0; m < alphabet.size() && dataPoints.size() < number; m++)
                        {
                            String dataPoint = alphabet.get(i) + alphabet.get(j) + alphabet.get(k) + alphabet.get(l) + alphabet.get(m);
                            dataPoint += (suffix.equals("") ? "" : "_" + suffix);
                            dataPoints.add(dataPoint);
                        }

        return dataPoints;
    }

    private double GenerateRandomContinuousValue()
    {
        return MathUtility.Round(Utilities.RandomNumberGenerator.nextGaussian(), 8);
    }

    private String GenerateRandomDiscreteValue()
    {
        ArrayList<String> values = new ArrayList<String>();
        values.add("Value1");
        values.add("Value2");

        return Lists.PickRandomValue(values);
    }    
}
