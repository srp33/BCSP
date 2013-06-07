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

/** This class stores survival information that can be used for calculation of survival statistics. When these calculations are made, the distributions of survival values are compared across multiple groups.
 * @author Stephen Piccolo
 */
public class SurvivalGroups
{
    private ArrayList<Survivals> All = new ArrayList<Survivals>();

    /** Constructor
     *
     * @param survivalGroups Groups of survival values
     */
    public SurvivalGroups(Survivals... survivalGroups)
    {
        for (Survivals survivalGroup : survivalGroups)
            All.add(survivalGroup);
    }

    /** Adds a new survival group
     *
     * @param survivalGroup Survival group to be added
     * @return This object
     */
    public SurvivalGroups Add(Survivals survivalGroup)
    {
        All.add(survivalGroup);
        return this;
    }

    /** Retrieves a list of all observed survival times across the groups
     *
     * @return List of observed survival times
     */
    public ArrayList<Double> GetObservedTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survivals survivals : All)
            for (Survival survival : survivals.Values)
                if (!survival.Censored)
                    times.add(survival.Time);

        Collections.sort(times);

        return times;
    }

    /** Retrieves a list of all censored survival times across the groups
     *
     * @return List of censored survival times
     */
    public ArrayList<Double> GetCensoredTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survivals survivals : All)
            for (Survival survival : survivals.Values)
                if (survival.Censored)
                    times.add(survival.Time);

        Collections.sort(times);

        return times;
    }

    /** Retrieves a list of all survival times across the groups, whether or not the times were censored
     *
     * @return List of survival times
     */
    public ArrayList<Double> GetAllTimes()
    {
        ArrayList<Double> times = new ArrayList<Double>();

        for (Survivals survivals : All)
            times.addAll(survivals.GetAllTimes());

        return times;
    }

    /** Indicates the survival group that a given survival instance is assigned to
     *
     * @param index Index of the survival value
     * @return Survival group for that index
     */
    public Survivals GetGroup(int index)
    {
        return All.get(index);
    }

    /** Indicates number of survival objects across all groups
     *
     * @return Number of survival objects across all groups
     */
    public int Size()
    {
        return All.size();
    }

    @Override
    public String toString()
    {
        String out = "";

        for (int i=0; i<All.size(); i++)
        {
            Survivals s = All.get(i);
            out += "Survival Group " + (i+1) + ": " + s.toString() + "\n";
        }

        return out;
    }
}