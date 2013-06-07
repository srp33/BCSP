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

/** This class is used as a generic means to measure the effectiveness of a set of predictions. It is used rarely.
 * @author Stephen Piccolo
 */
public abstract class AbstractResultMeasurer
{
    /** Predictions that have been made previously
     */
    protected Predictions Predictions;

    /** The constructor requires specification of the predictions that have been made previously.
     * @param predictions Predictioins
     */
    public AbstractResultMeasurer(Predictions predictions)
    {
        Predictions = predictions;
    }

    /** This abstract method supports retrieving result values.
     * @return Result value
     * @throws Exception
     */
    protected abstract double AbstractGetResult() throws Exception;

    /** This abstract method provides a template for indicatign whether one result beats another one.
     * @param compareMeasurer Measurer to be compared with this one
     * @return Value indicating whether the value from this measure beats the value from the other one
     * @throws Exception
     */
    public abstract boolean Beats(AbstractResultMeasurer compareMeasurer) throws Exception;

    /** This abstract method provides a template for indicatign whether one result ties another one.
     * @param compareMeasurer Measurer to be compared with this one
     * @return Value indicating whether the value from this measure ties the value from the other one
     * @throws Exception
     */
    public boolean Ties(AbstractResultMeasurer compareMeasurer) throws Exception
    {
        return GetResult() == compareMeasurer.GetResult();
    }

    private Double _result = Double.NaN;

    /** This method returns the result (and caches results when they are returned.
     * @return Result value
     * @throws Exception
     */
    public double GetResult() throws Exception
    {
        if (_result.equals(Double.NaN) && Predictions.Size() > 0)
            _result = AbstractGetResult();

        return _result;
    }
}