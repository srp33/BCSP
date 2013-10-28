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

/** A scalar result represents a single value that can be recorded to evaluate how ML-Flex has performed or to describe the data used for the predictions.
 * @author Stephen Piccolo
 */
public class ScalarValue
{
    public String Description;
    public String Metric;
    public Object Value;

    /** Constructor
     *
     * @param description General description
     * @param metric Description of metric
     * @param value Scalar value
     */
    public ScalarValue(String description, String metric, Object value)
    {
        Description = description;
        Metric = metric;
        Value = value;
    }

    @Override
    public String toString()
    {
        return Description + "\t" + Metric + "\t" + Value;
    }
}
