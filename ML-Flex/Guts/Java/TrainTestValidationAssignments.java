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
import java.util.HashMap;

/** This class stores assignments of data instances to either training or testing groups. It is used when cross-validation is NOT performed (or you can think of it as one-fold cross-validation).
 * @author Stephen Piccolo
 */
public class TrainTestValidationAssignments extends CrossValidationAssignments
{
    /** Constructor
     *
     * @param assignments Map indicate which fold each data instance should belong to
     * @param dependentVariableInstances Dependent variable instances
     * @throws Exception
     */
    public TrainTestValidationAssignments(HashMap<Integer, ArrayList<String>> assignments, DataInstanceCollection dependentVariableInstances, boolean IsInner) throws Exception
    {
        super(2, dependentVariableInstances, IsInner);
        Assignments = assignments;
    }

    @Override
    public int GetFoldNumber(String instanceID) throws Exception
    {
        if (!Assignments.get(1).contains(instanceID) && !Assignments.get(2).contains(instanceID))
            throw new Exception("The instance ID (" + instanceID + ") is not assigned to a fold");

        return 1;
    }

    @Override
    public ArrayList<Integer> GetAllFoldNumbers()
    {
        return Lists.CreateIntegerList(1);
    }

    @Override
    public ArrayList<Integer> GetFoldsWithTestData(AbstractDataProcessor processor) throws Exception
    {
        if (HasAnyTestData(processor))
            return GetAllFoldNumbers();

        throw new Exception("No test data for " + processor.GetDescription() + ".");
    }

    @Override
    public ArrayList<String> GetTestIDs(int fold) throws Exception
    {
        if (fold > 1)
            throw new Exception("When performing training and testing, only fold 0 is allowed. Fold " + fold + " was specified.");

        return Assignments.get(2);
    }

    @Override
    public ArrayList<String> GetTrainIDs(int fold) throws Exception
    {
        if (fold > 1)
            throw new Exception("When performing training and testing, only fold 0 is allowed. Fold " + fold + " was specified.");

        return FilterTrainIDs(Assignments.get(1));
    }

    @Override
    public boolean HasAnyTestData(AbstractDataProcessor processor) throws Exception
    {
        return GetTestIDs(1).size() > 0;
    }

    private CrossValidationAssignments _innerAssignments = null;
    @Override
    public CrossValidationAssignments GetInnerAssignments(int outerFold) throws Exception
    {
        if (_innerAssignments == null)
            _innerAssignments = new CrossValidationAssignments(Utilities.Config.GetNumInnerCrossValidationFolds(), DependentVariableInstances.Get(GetTrainIDs(1)), true).AssignFolds();

        return _innerAssignments;
    }
}