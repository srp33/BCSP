package mlflex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** This class is designed to store information about which cross-validation fold each data instance is assigned to. It also provides methods to make it easier to access this information for machine-learning tasks.
 * @author Stephen Piccolo
 */
public class CrossValidationAssignments
{
    protected HashMap<Integer, ArrayList<String>> Assignments = new HashMap<Integer, ArrayList<String>>();
    protected int NumFolds;
    protected DataInstanceCollection DependentVariableInstances;
    protected boolean IsInner;

    /** Constructor
     *
     * @param numFolds Number of cross validation folds to use
     * @param dependentVariableInstances Dependent variable data instances
     * @param isInner Indicates whether this is dealing with inner folds or outer folds
     * @throws Exception
     */
    public CrossValidationAssignments(int numFolds, DataInstanceCollection dependentVariableInstances, boolean isInner) throws Exception
    {
        NumFolds = numFolds;
        DependentVariableInstances = dependentVariableInstances;
        IsInner = isInner;
    }

    /** Assigns data instances to cross-validation folds that have been created
     *
     * @return This object
     * @throws Exception
     */
    public CrossValidationAssignments AssignFolds() throws Exception
    {
        ArrayList<String> configTrainIDs = Utilities.Config.GetTrainingInstanceIDs();
        ArrayList<String> configTestIDs = Utilities.Config.GetTestInstanceIDs();

        if (!IsInner && configTrainIDs.size() > 0 && configTestIDs.size() > 0)
        {
            Assignments.put(1, configTrainIDs);
            Assignments.put(2, configTestIDs);

            Utilities.Log.Info("Training and testing (not cross validation) will be performed using assignments from the TRAINING_INSTANCE_IDS and TEST_INSTANCE_IDS experiment configuration settings.");
            return new TrainTestValidationAssignments(Assignments, DependentVariableInstances, IsInner);
        }

        if (NumFolds == 1)
        {
            AssignToFoldsViaStratification(2);
            return new TrainTestValidationAssignments(Assignments, DependentVariableInstances, IsInner);
        }

        if (NumFolds == DependentVariableInstances.Size())
        {
            for (int i = 1; i <= DependentVariableInstances.Size(); i++)
                Assignments.put(i, Lists.CreateStringList(DependentVariableInstances.Get(i).GetID()));

            return this;
        }

        AssignToFoldsViaStratification(NumFolds);

        return this;
    }

    private void AssignToFoldsViaStratification(int numFolds) throws Exception
    {
        ArrayList<String> uniqueClasses = Lists.Sort(DependentVariableInstances.GetUniqueValues(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName()));

        int currentFold = 1;

        for (String x : uniqueClasses)
        {
            ArrayList<String> instanceIDs = DependentVariableInstances.FilterByDataPointValue(Utilities.ProcessorVault.DependentVariableDataProcessor.GetDependentVariableDataPointName(), x).GetIDs();

            Collections.shuffle(instanceIDs, Utilities.RandomNumberGenerator);

            for (String instanceID : instanceIDs)
            {
                AssignToFold(currentFold, instanceID);

                if (currentFold == numFolds)
                    currentFold = 1;
                else
                    currentFold++;
            }
        }
    }

    private void AssignToFold(int foldNumber, String id)
    {
        if (Assignments.containsKey(foldNumber))
        {
            ArrayList<String> existing = Assignments.get(foldNumber);
            existing.add(id);
            Assignments.put(foldNumber, existing);
        }
        else
            Assignments.put(foldNumber, Lists.CreateStringList(id));
    }

    /** Indicates whether fold a given instance is assigned to
     *
     * @param instanceID Data instance ID
     * @return Which fold the instance is assigned to
     * @throws Exception
     */
    public int GetFoldNumber(String instanceID) throws Exception
    {
        for (Map.Entry<Integer, ArrayList<String>> entry : Assignments.entrySet())
            if (entry.getValue().contains(instanceID))
                return entry.getKey();
        
        throw new Exception("The instance ID (" + instanceID + ") is not assigned to a fold");
    }

    /** Indicates which folds have at least one data instance assigned to them, after filtering has occurred
     *
     * @param processor Data processor
     * @return List of folds
     * @throws Exception
     */
    public ArrayList<Integer> GetFoldsWithTestData(AbstractDataProcessor processor) throws Exception
    {
        ArrayList<Integer> folds = new ArrayList<Integer>();

        for (int fold : GetAllFoldNumbers())
            if (HasTestData(processor, fold))
                folds.add(fold);

        return folds;
    }

     /** Indicates all cross-validation folds, whether or not any data instance have been assigned to them
     *
     * @return List of folds
     * @throws Exception
     */
    public ArrayList<Integer> GetAllFoldNumbers() throws Exception
    {
        ArrayList<Integer> folds = new ArrayList<Integer>(Assignments.keySet());
        Collections.sort(folds);
        return folds;
    }

    /** Indicates IDs for data instances that have been assigned to cross-validation folds
     *
     * @return Data instance IDs
     */
    private ArrayList<String> GetAllIDs()
    {
        ArrayList<String> ids = new ArrayList<String>();

        for (ArrayList<String> x : Assignments.values())
            ids.addAll(x);

        return ids;
    }

    /** Indicates test instance IDs for a given cross-validation fold
     *
     * @param fold Number of cross validation fold
     * @return Data instance IDs assigned to that fold
     * @throws Exception
     */
    public ArrayList<String> GetTestIDs(int fold) throws Exception
    {
        return new ArrayList<String>(Assignments.get(fold));
    }

    /** Indicates training instance IDs for a given cross-validation fold
     *
     * @param fold Number of cross validation fold
     * @return Data instance IDs assigned to that fold
     * @throws Exception
     */
    public ArrayList<String> GetTrainIDs(int fold) throws Exception
    {
        return FilterTrainIDs(Lists.RemoveAll(GetAllIDs(), GetTestIDs(fold)));
    }

    /** Returns a list of training instance IDs that have been excluded across all cross-validation folds.
     *
     * @return Training instance IDs that have been excluded
     * @throws Exception
     */
    public ArrayList<String> GetAllExcludedTrainIDs() throws Exception
    {
        ArrayList<String> excluded = new ArrayList<String>();

        for (int fold : GetAllFoldNumbers())
        {
            ArrayList<String> trainIDs = Lists.RemoveAll(GetAllIDs(), GetTestIDs(fold));
            excluded.addAll(GetTrainIDsToExclude(trainIDs));
        }

        return excluded;
    }

    /** Indicates which instances can be used for testing, across all cross-validation folds.
     *
     * @return Test instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAllTestIDs() throws Exception
    {
        ArrayList<String> testIDs = new ArrayList<String>();

        for (int fold : GetAllFoldNumbers())
            testIDs.addAll(GetTestIDs(fold));

        return testIDs;
    }

    /** Indicates which instances should be used for testing, across all cross-validation folds.
     *
     * @return Test instance IDs
     * @throws Exception
     */
    public ArrayList<String> GetAllTestIDsWithData() throws Exception
    {
        HashSet<String> testIDs = new HashSet<String>();

        for (int outerFold : GetAllFoldNumbers())
            for (AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
                testIDs.addAll(GetTestInstances(processor, outerFold, new ArrayList<String>()).GetIDs());

        return new ArrayList<String>(testIDs);
    }

    /** If the relevant configuration value is specified, this method randomly excludes a subset of training IDs.
     *
     * @param trainIDs List of training IDs to be filtered
     * @throws Exception
     */
    protected ArrayList<String> FilterTrainIDs(ArrayList<String> trainIDs) throws Exception
    {
        return Lists.RemoveAll(trainIDs, GetTrainIDsToExclude(trainIDs));
    }

    /** This method indicates which training instances, if any, should be excluded randomly from the analysis.
     * @param instanceIDs List of all instance IDs that may be excluded
     * @return List of instance IDs to exclude
     * @throws Exception
     */
    public ArrayList<String> GetTrainIDsToExclude(ArrayList<String> instanceIDs) throws Exception
    {
        ArrayList<String> filterIDs = new ArrayList<String>();

        if (!IsInner)
        {
            int numInstancesToExclude = Utilities.Config.GetNumTrainingInstancesToExcludeRandomly();
            if (numInstancesToExclude > 0)
                filterIDs = Lists.GetRandomSubset(instanceIDs, numInstancesToExclude);
        }

        return filterIDs;
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTrainInstances(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetTrainInstances(processor, fold, null);
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @param dataPoints List of data points that should be included for the given data instances
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTrainInstances(AbstractDataProcessor processor, int fold, ArrayList<String> dataPoints) throws Exception
    {
        return Utilities.InstanceVault.GetAnalysisInstances(processor, GetTrainIDs(fold), dataPoints);
    }

    /** Indicates which training instances for a given data processor are assigned to a given cross-validation fold.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @param dataPoints List of data points that should be included for the given data instances
     * @return Collection of instances
     * @throws Exception
     */
    public DataInstanceCollection GetTestInstances(AbstractDataProcessor processor, int fold, ArrayList<String> dataPoints) throws Exception
    {
        return Utilities.InstanceVault.GetAnalysisInstances(processor, GetTestIDs(fold), dataPoints);
    }

    /** Indicates how many training instances are assigned to a given fold for a given data processor. This method is provided to improve performance.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Number of instances
     * @throws Exception
     */
    public int GetNumTrainInstances(AbstractDataProcessor processor, int fold) throws Exception
    {
        return Utilities.InstanceVault.GetAnalysisInstancesSize(processor, GetTrainIDs(fold));
    }

    /** Indicates how many test instances are assigned to a given fold for a given data processor. This method is provided to improve performance.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Number of instances
     * @throws Exception
     */
    public int GetNumTestInstances(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetTestInstances(processor, fold, new ArrayList<String>()).Size();
    }

    /** Indicates whether a given combination of data processor and cross-validation fold have any test instances.
     *
     * @param processor Data processor
     * @param fold Cross-validation fold
     * @return Whether there are any test instances
     * @throws Exception
     */
    public boolean HasTestData(AbstractDataProcessor processor, int fold) throws Exception
    {
        return GetNumTestInstances(processor, fold) > 0;
    }

    /** Indicates whether a given data processor has any test instances for any cross-validation fold.
     *
     * @param processor Data processor
     * @return Whether there are any test instances
     * @throws Exception
     */
    public boolean HasAnyTestData(AbstractDataProcessor processor) throws Exception
    {
        for (int outerFold : GetAllFoldNumbers())
            if (HasTestData(processor, outerFold))
                return true;

        return false;
    }

    private ConcurrentHashMap<Integer, CrossValidationAssignments> _innerAssignments = null;
    /** Returns the inner cross-validation assignments for a given outer cross-validation fold
     *
     * @param outerFold Outer cross-validation fold
     * @return Cross-validation assignments
     * @throws Exception
     */
    public CrossValidationAssignments GetInnerAssignments(int outerFold) throws Exception
    {
        if (_innerAssignments == null)
        {
            _innerAssignments = new ConcurrentHashMap<Integer, CrossValidationAssignments>();

            for (int f : GetAllFoldNumbers())
            {
                CrossValidationAssignments assignments = new CrossValidationAssignments(Utilities.Config.GetNumInnerCrossValidationFolds(), DependentVariableInstances.Get(GetTrainIDs(f)), true).AssignFolds();
                _innerAssignments.put(f, assignments);
            }
        }

        return _innerAssignments.get(outerFold);
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();

        for (Map.Entry<Integer, ArrayList<String>> entry : Assignments.entrySet())
        {
            output.append("Fold " + Integer.toString(entry.getKey()) + " (" + entry.getValue().size() + " instances): ");
            output.append(Lists.Join(Lists.Sort(entry.getValue()), ",") + "\n");
        }

        return output.toString();
    }
}
