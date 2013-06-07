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

/** This class stores survival information for a particular data instances.
 * @author Stephen Piccolo
 */
public class Survival
{
    double Time;
    boolean Censored;

    /** Constructor. By default, no censoring is assumed.
     *
     * @param time Length of survival (can be in any units desired)
     */
    public Survival(double time)
    {
        this(time, false);
    }

    /** Constructor.
     *
     * @param time Length of survival (can be in any units desired)
     * @param censored Whether this survival time was censored
     */
    public Survival(double time, boolean censored)
    {
        Time = time;
        Censored = censored;
    }

    @Override
    public String toString()
    {
        String out = String.valueOf(Time);
        out += Censored ? " (Censored)" : "";

        return out;
    }
}