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
import java.util.concurrent.Callable;

/** This class coordinates the process of selecting/ranking features for each combination of data processor, feature-selection algorithm, and cross-validation fold. It contains logic for splitting the work across multiple computing nodes and threads.
 * @author Stephen Piccolo
 */
public class FeatureEvaluator
{
    public final AbstractDataProcessor Processor;
    public final FeatureSelectionAlgorithm SelectionAlgorithm;
    public final int OuterFold;

    private ArrayList<String> _allFeatures;

    /** Constructor
     *
     * @param processor Data processor containing data to be evaluated
     * @param fsAlgorithm Feature selection/ranking algorithm
     * @param outerFold Number of outer cross-validation fold
     */
    public FeatureEvaluator(AbstractDataProcessor processor, FeatureSelectionAlgorithm fsAlgorithm, int outerFold) throws Exception
    {
        Processor = processor;
        SelectionAlgorithm = fsAlgorithm;
        OuterFold = outerFold;

        _allFeatures = Utilities.InstanceVault.GetAnalysisDataPoints(Processor, null);
    }

    private String GetSaveDirectory()
    {
        return Settings.FEATURE_SELECTION_DIR + Processor.GetDescription() + "Data/" + SelectionAlgorithm + "/Iteration" + Utilities.Iteration + "/OuterFold" + OuterFold + "/";
    }

    private String GetOuterSaveFilePath()
    {
        return GetSaveDirectory() + GetOuterFoldFileName();
    }

    private String GetInnerSaveFilePath(int innerFold)
    {
        return GetSaveDirectory() + GetInnerFoldFileName(innerFold);
    }

    private String GetLockFilePrefix()
    {
        return Processor.GetDescription() + "_" + SelectionAlgorithm + "_" + Utilities.Iteration + "_" + OuterFold + "_";
    }

    private String GetOuterLockFilePath()
    {
        return GetLockFilePrefix() + GetOuterFoldFileName();
    }

    private String GetInnerLockFilePath(int innerFold)
    {
        return GetLockFilePrefix() + GetInnerFoldFileName(innerFold);
    }

    private String GetStatusFilePrefix()
    {
        return GetSaveDirectory().replace(Settings.FEATURE_SELECTION_DIR, "SelectedFeatures/");
    }

    private String GetOuterStatusFilePath()
    {
        return GetStatusFilePrefix() + GetOuterFoldFileName();
    }

    private String GetInnerStatusFilePath(int innerFold)
    {
        return GetStatusFilePrefix() + GetInnerFoldFileName(innerFold);
    }

    /** Name of the file where the outer cross-validation results will be stored.
     *
     * @return File name
     */
    private String GetOuterFoldFileName()
    {
        return "OuterFold_SelectedFeatures.txt";
    }

    /** Name of the file where the inner cross-validation results will be stored for a given cross-validation fold.
     *
     * @param innerFold Number of inner cross-validation fold
     * @return File name
     */
    private String GetInnerFoldFileName(int innerFold)
    {
        return "InnerFold" + innerFold + "_SelectedFeatures.txt";
    }

    private DataInstanceCollection GetOuterTrainingInstances() throws Exception
    {
        return Utilities.InstanceVault.GetCrossValidationAssignments().GetTrainInstances(Processor, OuterFold);
    }

    private DataInstanceCollection GetInnerTrainingInstances(int innerFold) throws Exception
    {
        return Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetTrainInstances(Processor, innerFold);
    }

    /** This method performs the work of selecting/ranking features for the inner cross-validation folds and sharing the load across multiple threads and computing nodes (where applicable).
     *
     * @throws Exception
     */
    public void SelectFeatures() throws Exception
    {
        Files.CreateDirectoryNoFatalError(GetSaveDirectory());

        MultiThreadedTaskHandler taskHandler = new MultiThreadedTaskHandler();

        for (final int innerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(OuterFold).GetFoldsWithTestData(Processor))
        {
            taskHandler.Add(new LockedCallable<Object>(GetInnerStatusFilePath(innerFold), GetInnerLockFilePath(innerFold), "Select features for " + GetDescription() + ", outerFold=" + OuterFold + ", innerFold=" + innerFold, new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    return SelectAndSaveFeatures(GetInnerTrainingInstances(innerFold), GetInnerSaveFilePath(innerFold));
                }
            }));
        }

        taskHandler.Add(new LockedCallable<Object>(GetOuterStatusFilePath(), GetOuterLockFilePath(), "Select features for " + GetDescription() + ", outer fold=" + OuterFold, new Callable<Object>()
        {
            public Object call() throws Exception
            {
                return SelectAndSaveFeatures(GetOuterTrainingInstances(), GetOuterSaveFilePath());
            }
        }));

        taskHandler.ExecuteWithRetries("select features for " + GetDescription());
    }

    private Boolean SelectAndSaveFeatures(DataInstanceCollection trainInstances, String filePath) throws Exception
    {
        ArrayList<String> selectedFeatures = SelectionAlgorithm.SelectFeatures(trainInstances, Utilities.InstanceVault.GetTransformedDependentVariableInstances(OuterFold));
        Files.WriteLineToFile(filePath, Lists.Join(selectedFeatures, ","));

        return selectedFeatures.equals(GetSelectedFeatures(filePath));
    }

    /** This method retrieves the features that have already been selected and saved. It retrieves all features.
     *
     * @param filePath Path where the file may be saved
     * @return Features in ranked order
     * @throws Exception
     */
    private ArrayList<String> GetSelectedFeatures(String filePath) throws Exception
    {
        return GetSelectedFeatures(filePath, -1);
    }

    /** This method retrieves the features that have already been selected and saved. It retrieves only the specified number of top-ranked features.
     *
     * @param filePath Path where the file may be saved
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    private ArrayList<String> GetSelectedFeatures(String filePath, int numTop) throws Exception
    {
        if (SelectionAlgorithm.IsNone())
            return new ArrayList<String>(_allFeatures);

        if (SelectionAlgorithm.IsPriorKnowledge())
            return Processor.GetPriorKnowledgeSelectedFeatures();

        if (!Files.FileExists(filePath))
            return new ArrayList<String>();

        ArrayList<String> selectedFeatures = Lists.CreateStringList(Files.ReadTextFile(filePath).trim().split(","));

        if (numTop > selectedFeatures.size() || numTop < 1)
            return selectedFeatures;

        return Lists.Subset(selectedFeatures, 0, numTop);
    }

    /** This method retrieves the features that have already been selected and saved for the outer cross-validation fold. It retrieves only the specified number of top-ranked features.
     *
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    public ArrayList<String> GetOuterSelectedFeatures(int numTop) throws Exception
    {
        return GetSelectedFeatures(GetOuterSaveFilePath(), numTop);
    }

    /** This method retrieves the features that have already been selected and saved for the specified inner cross-validation fold. It retrieves only the specified number of top-ranked features.
     *
     * @param innerFold Number of inner cross-validation fold
     * @param numTop Number of top-ranked features to return
     * @return Features in ranked order
     * @throws Exception
     */
    public ArrayList<String> GetInnerSelectedFeatures(int innerFold, int numTop) throws Exception
    {
        return GetSelectedFeatures(GetInnerSaveFilePath(innerFold), numTop);
    }

    /** Returns a description of this object.
     *
     * @return Description
     */
    public String GetDescription()
    {
        return Processor.GetDescription() + "_" + SelectionAlgorithm;
    }
}