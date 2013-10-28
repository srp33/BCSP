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

/** This result measure uses the "kappa" value. This value is the improvement in accuracy over the baseline (accuracy attained if the majority class is always selected).
 * @author Stephen Piccolo
 */
public class KappaResultMeasurer extends AbstractResultMeasurer
{
    /** Pass-through constructor
     *
     * @param predictions Predictions
     */
    public KappaResultMeasurer(Predictions predictions)
    {
        super(predictions);
    }

    @Override
    protected double AbstractGetResult() throws Exception
    {
        return new PredictionResults(Predictions).GetBaselineImprovement();
    }

    @Override
    public boolean Beats(AbstractResultMeasurer compareMeasurer) throws Exception
    {
        return GetResult() > compareMeasurer.GetResult();
    }
}