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

/** This class stores survival values that can then be used to calculate survival statistics.
 * @author Stephen Piccolo
 */
public class Survivals
{
    public ArrayList<Survival> Values = new ArrayList<Survival>();

    /** Constructor
     *
     * @param survivals A survival object for each data instance in this survival group
     */
    public Survivals(Survival... survivals)
    {
        for (Survival survival : survivals)
            Values.add(survival);
    }

    /** Indicates all survival times, whether observed or not
     *
     * @return List of survival times
     */
    public ArrayList<Double> GetAllTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survival survival : Values)
            times.add(survival.Time);

        return times;
    }

    /** Indicates all observed survival times
     *
     * @return List of observed survival times
     */
    public ArrayList<Double> GetObservedTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survival survival : Values)
            if (!survival.Censored)
                times.add(survival.Time);

        return times;
    }

    /** Indicates all censored survival times
     *
     * @return List of censored survival times
     */
    public ArrayList<Double> GetCensoredTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survival survival : Values)
            if (survival.Censored)
                times.add(survival.Time);

        return times;
    }

    @Override
    public String toString()
    {
        ArrayList<String> outValues = new ArrayList<String>();

        for (Survival s : Values)
            outValues.add(s.toString());

        return Lists.Join(outValues, ";");
    }
}