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
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/** This class provides convenience methods for accessing information about data instances that are used for machine-learning analyses.
 * @author Stephen Piccolo
 */
public class InstanceVault
{
    /** This method returns a list of the data instances that should be used in machine-learning analyses for a given data processor.
     *
     * @param processor The data processor
     * @return List of data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisInstanceIDs(AbstractDataProcessor processor) throws Exception
    {
        return GetAnalysisInstanceIDs(processor, null);
    }

    /** This method returns a list of the data instances that should be used in machine-learning analyses for a given data processor.
     *
     * @param processor The data processor
     * @param instanceIDs List of candidate instance IDs (any analysis instance ID that is not in this list will be ignored)
     * @return List of data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisInstanceIDs(AbstractDataProcessor processor, ArrayList<String> instanceIDs) throws Exception
    {
        return GetAnalysisInstanceIDs(processor, instanceIDs, Utilities.Config.OnlyInstancesWithAllData());
    }

    /** This method returns a list of the data instances that should be used in machine-learning analyses for a given data processor.
     *
     * @param processor The data processor
     * @param instanceIDs List of candidate instance IDs (any analysis instance ID that is not in this list will be ignored)
     * @param onlyInstancesWithAllData Whether to only include data instances that have data for all data processors (when there are multiple processors per data instance)
     * @return List of data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisInstanceIDs(AbstractDataProcessor processor, ArrayList<String> instanceIDs, boolean onlyInstancesWithAllData) throws Exception
    {
        if (instanceIDs == null)
            instanceIDs = GetAnalysisInstances(processor).GetIDs();

        if (!onlyInstancesWithAllData || processor.equals(Utilities.ProcessorVault.ThresholdSelectionDataProcessor) || processor instanceof AbstractDependentVariableDataProcessor)
        {
            return instanceIDs;
        }
        else
        {
            ArrayList<String> subset = Lists.Intersect(instanceIDs, GetInstanceIDsWithAllData());
            return subset;
        }
    }

    /** This method returns a list of the data instances that should be used in machine-learning analyses for all data processors.
     *
     * @return List of data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisInstanceIDs() throws Exception
    {
        return GetAnalysisInstanceIDs(Utilities.ProcessorVault.IndependentVariableDataProcessors);
    }

    /** This method returns a list of the data instances that should be used in machine-learning analyses for various data processors.
     *
     * @param processors The data processors
     * @return List of data instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisInstanceIDs(ArrayList<AbstractDataProcessor> processors) throws Exception
    {
        HashSet<String> patientIDs = new HashSet<String>();

        for (AbstractDataProcessor processor : processors)
            patientIDs.addAll(GetAnalysisInstanceIDs(processor));

        return new ArrayList<String>(patientIDs);
    }

    /** This method returns a list of data points that should be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @param dataPoints List of data points (analysis data points that are not in this list will be ignored)
     * @return List of data points to be used in analyses
     * @throws Exception
     */
    public ArrayList<String> GetAnalysisDataPoints(AbstractDataProcessor processor, ArrayList<String> dataPoints) throws Exception
    {
        if (dataPoints == null)
            dataPoints = GetAnalysisInstances(processor).GetDataPointNames();

        return dataPoints;
    }

    private Vector<ProcessorInstances> _analysisInstancesMap = new Vector<ProcessorInstances>();
    private DataInstanceCollection GetAnalysisInstances(AbstractDataProcessor processor) throws Exception
    {
        ProcessorInstances pi = new ProcessorInstances(processor);

        if (!_analysisInstancesMap.contains(pi))
        {
            Utilities.Log.Info("Getting analysis instances for " + processor.GetDescription());
            DataInstanceCollection instances = new DataInstanceCollection();
            for (DataValues instance : processor.GetTransformedInstances())
                if (processor.KeepTransformedInstance(instance))
                    instances.Add(instance);
            Utilities.Log.Debug("Keeping " + instances.Size() + " transformed instances");

            instances.RemoveInstances(Utilities.Config.GetInstanceIDsToExclude());
            Utilities.Log.Debug(instances.Size() + " instances remaining after excluding any instances based on config");

            Utilities.Log.Info("Updating instances for analysis for " + processor.GetDescription());
            processor.UpdateInstancesForAnalysis(instances);
            Utilities.Log.Debug(instances.Size() + " instances remaining after updating instances for analysis");

            Utilities.Log.Info("Removing sparse data points for " + processor.GetDescription());
            processor.RemoveSparseDataPoints(instances);
            Utilities.Log.Debug(instances.GetNumDataPoints() + " data points remaining after removing any sparse data points");

            Utilities.Log.Info("Removing sparse instances for " + processor.GetDescription());
            processor.RemoveSparseInstances(instances);
            Utilities.Log.Debug(instances.Size() + " instances remaining after removing any sparse instances");

            if (!(processor instanceof AbstractDependentVariableDataProcessor))
            {
                instances.RemoveDataPointNamesMatching(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName());

                Utilities.Log.Info("Keeping only instances that are needed for " + processor.GetDescription());
                instances.KeepInstances(GetAnalysisInstanceIDs(Utilities.ProcessorVault.DependentVariableDataProcessor));
            }

            pi.Instances = instances;

            if (pi.Processor.equals(Utilities.ProcessorVault.DependentVariableDataProcessor) && Utilities.Config.GetBooleanValue("PERMUTE_DEPENDENT_VARIABLE_VALUES", false))
            {
                Utilities.Log.Info("Permuting class labels for " + pi.Processor);
                pi.Instances = DataInstanceCollection.PermuteIDs(pi.Instances);
            }

            _analysisInstancesMap.add(pi);

            Utilities.Log.Info(instances.Size() + " analysis instances, " + instances.GetNumDataPoints() + " data points for " + processor.GetDescription());
        }

        return _analysisInstancesMap.get(_analysisInstancesMap.indexOf(pi)).Instances;
    }

    /** Retrieves a list of data instances that can be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @param instanceIDs List of data instance IDs (any instance ID not in this list will be ignored)
     * @param dataPoints List of data points (any data point not in this list will be ignored)
     * @return Collection of data instances that can be used in machine-learning analyses
     * @throws Exception
     */
    public DataInstanceCollection GetAnalysisInstances(AbstractDataProcessor processor, ArrayList<String> instanceIDs, ArrayList<String> dataPoints) throws Exception
    {
        ArrayList<String> analysisInstanceIDs = GetAnalysisInstanceIDs(processor, instanceIDs);
        ArrayList<String> analysisDataPoints = GetAnalysisDataPoints(processor, dataPoints);

        return GetAnalysisInstances(processor).Clone(analysisInstanceIDs, analysisDataPoints);
    }

    /** This method indicates how many data points should be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @return Number of data points that will be used in machine-learning analysese
     * @throws Exception
     */
    public int GetAnalysisInstancesNumDataPoints(AbstractDataProcessor processor) throws Exception
    {
        return GetAnalysisInstances(processor).GetNumDataPoints();
    }

    /** This method indicates how many data instances should be used in machine-learning analyses for a given data processor.
     *
     * @param processor Data processor
     * @param instanceIDs List of instance IDs to consider (any not in this list will be ignored)
     * @return Number of data instances that will be used in machine-learning analyses
     * @throws Exception
     */
    public int GetAnalysisInstancesSize(AbstractDataProcessor processor, ArrayList<String> instanceIDs) throws Exception
    {
        ArrayList<String> allIDs = GetAnalysisInstances(processor).GetIDs();

        return Lists.Intersect(allIDs, instanceIDs).size();
    }

    private ArrayList<String> _instanceIDsWithAllData = null;
    private ArrayList<String> GetInstanceIDsWithAllData() throws Exception
    {
        if (_instanceIDsWithAllData == null)
        {
            for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
            {
                if (_instanceIDsWithAllData == null)
                    _instanceIDsWithAllData = GetAnalysisInstances(processor).GetIDs();
                else
                    _instanceIDsWithAllData = Lists.Intersect(_instanceIDsWithAllData, GetAnalysisInstances(processor).GetIDs());
            }
        }

        return _instanceIDsWithAllData;
    }

    private CrossValidationAssignments _cvAssignments = null;

    /** This is a convenience method that returns the cross-validation assignments that can be used in machine-learning analyses.
     *
     * @return Cross-validation assignments
     * @throws Exception
     */
    public CrossValidationAssignments GetCrossValidationAssignments() throws Exception
    {
        if (_cvAssignments == null)
        {
            DataInstanceCollection dependentVariableInstances = Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableTransformer().GetDependentVariableInstancesForCrossValidation();
            _cvAssignments = new CrossValidationAssignments(Utilities.Config.GetNumOuterCrossValidationFolds(dependentVariableInstances.Size()), dependentVariableInstances, false).AssignFolds();
        }

        return _cvAssignments;
    }

    /** This method returns raw dependent-variable instances (for cases where transformations of the dependent variable must occur)
     *
     * @return Raw dependent-variable instances
     * @throws Exception
     */
    public DataInstanceCollection GetRawDependentVariableInstances() throws Exception
    {
        return Utilities.InstanceVault.GetAnalysisInstances(Utilities.ProcessorVault.DependentVariableDataProcessor, null, null);
    }

    private ConcurrentHashMap<Integer, DataInstanceCollection> _foldTransformedDependentVariableInstances = null;

    /** This method returns the dependent-variable instances that can be used for machine-learning analyses for a given cross-validation fold.
     *
     * @param outerFold Outer cross-validation fold
     * @return Transformed dependent-variable instances
     * @throws Exception
     */
    public DataInstanceCollection GetTransformedDependentVariableInstances(int outerFold) throws Exception
    {
        if (_foldTransformedDependentVariableInstances == null)
        {
            _foldTransformedDependentVariableInstances = new ConcurrentHashMap<Integer, DataInstanceCollection>();

            DataInstanceCollection dependentVariableInstances = Utilities.InstanceVault.GetAnalysisInstances(Utilities.ProcessorVault.DependentVariableDataProcessor, null, null);

            for (int outerFold2 : Lists.CreateIntegerSequenceList(0, Utilities.Config.GetNumOuterCrossValidationFolds(dependentVariableInstances.Size() - 1)))
                _foldTransformedDependentVariableInstances.put(outerFold2, Utilities.ProcessorVault.DependentVariableDataProcessor.TransformDependentVariableInstances(dependentVariableInstances, outerFold2));
        }

        return _foldTransformedDependentVariableInstances.get(outerFold);
    }

    /** This method returns the dependent-variable instances that can be used for machine-learning analyses.
     *
     * @return Transformed dependent-variable instances
     * @throws Exception
     */
    public DataInstanceCollection GetTransformedDependentVariableInstances() throws Exception
    {
        DataInstanceCollection dependentVariableInstances = Utilities.InstanceVault.GetAnalysisInstances(Utilities.ProcessorVault.DependentVariableDataProcessor, null, null);
        DataInstanceCollection instances = new DataInstanceCollection();
        
        for (int outerFold : Lists.CreateIntegerSequenceList(0, Utilities.Config.GetNumOuterCrossValidationFolds(dependentVariableInstances.Size() - 1)))
            instances.Add(GetTransformedDependentVariableInstances(outerFold));

        return instances;
    }

    /** This method return the raw dependent-variable value for a given data instance ID
     *
     * @param instanceID Data instance ID
     * @return Raw dependent-variable instances
     * @throws Exception
     */
    public String GetRawDependentVariableValue(String instanceID) throws Exception
    {
        return GetRawDependentVariableInstances().Get(instanceID).GetDataPointValue(0);
    }

    /** This method return the transformed dependent-variable value for a given data instance ID
     *
     * @param instanceID Data instance ID
     * @return Transformed dependent-variable instances
     * @throws Exception
     */
    public String GetTransformedDependentVariableValue(String instanceID) throws Exception
    {
        return GetTransformedDependentVariableInstances().Get(instanceID).GetDataPointValue(0);
    }

    /** The purpose of this method is to remove analysis instances that have been cached in memory.
     *
     * @param processor Data processor
     * @throws Exception
     */
    public void RemoveAnalysisInstancesFromCache(AbstractDataProcessor processor) throws Exception
    {
        if (_analysisInstancesMap.contains(new ProcessorInstances(processor)))
            _analysisInstancesMap.remove(new ProcessorInstances(processor));
    }

    private class ProcessorInstances
    {
        AbstractDataProcessor Processor;
        DataInstanceCollection Instances;

        ProcessorInstances(AbstractDataProcessor processor)
        {
            Processor = processor;
        }

        @Override
        public boolean equals(Object obj)
        {
            ProcessorInstances compareObj = (ProcessorInstances)obj;
            return this.Processor.equals(compareObj.Processor);
        }

        @Override
        public int hashCode()
        {
            return this.Processor.hashCode();
        }
    }
}
