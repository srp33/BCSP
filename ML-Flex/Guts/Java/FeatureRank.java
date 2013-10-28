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

/** This class contains information about how highly a feature (data point) was ranked by a given feature selection/ranking approach.
 * @author Stephen Piccolo
 */
public class FeatureRank implements Comparable
{
    public String Feature;
    public double Rank;

    /** Constructor
     *
     * @param feature Feature/variable name
     * @param rank Rank
     */
    FeatureRank(String feature, double rank)
    {
        Feature = feature;
        Rank = rank;
    }

    public int compareTo(Object obj)
    {
        FeatureRank compareObj = (FeatureRank) obj;
        return new Double(this.Rank).compareTo(compareObj.Rank);
    }

    @Override
    public boolean equals(Object obj)
    {
        FeatureRank compareObj = (FeatureRank) obj;
        return this.Feature.equals(compareObj.Feature);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.Feature != null ? this.Feature.hashCode() : 0);
        return hash;
    }
}