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

package mlflex.tcga;

import mlflex.*;

/** This class is design for machine-learning analyses that are designed to predict survival status. It specifies patients who survived longer than 2 years as "longer-term survivors" (LTS) and those who survived less than two-years as "shorter-term survivors" (STS).
 * @author Stephen Piccolo
 */
public class TwoYearSurvivalTransformer extends SpecificValueDependentValueTransformer
{
    /** Pass-through constructor */
    public TwoYearSurvivalTransformer() throws Exception
    {
        super(730, Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_LOW_DESCRIPTOR", "LTS"), Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_HIGH_DESCRIPTOR", "STS"));
    }
}
