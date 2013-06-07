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

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class DescriptionFileSaver
{
    public LockedCallable SaveMiscellaneousDescriptionFiles() throws Exception
    {
        return new LockedCallable<Object>("SaveMiscellaneousDescriptionFiles", new Callable<Object>()
        {
            public Object call() throws Exception
            {
                // These tasks are quick to execute, so they are grouped together
                Files.CopyFile(Settings.EXPERIMENTS_DIR + Utilities.Experiment.Description + ".txt", Settings.GetExperimentOutputDir(false) + Utilities.Experiment.toString() + ".txt");

                for (File configFile : Files.GetFilesInDirectory(Settings.CONFIG_DIR, "*.txt"))
                    Files.CopyFile(configFile.getAbsolutePath(), Settings.GetExperimentOutputDir(false) + configFile.getName());

                SaveExcludedTrainingIDInfo();
                SaveCrossValidationAssignments();

                return Boolean.TRUE;
            }
        });
    }

    private void SaveExcludedTrainingIDInfo() throws Exception
    {
        ArrayList<String> excludedTrainingIDs = Utilities.InstanceVault.GetCrossValidationAssignments().GetAllExcludedTrainIDs();
        if (excludedTrainingIDs.size() > 0)
        {
            String filePath = Settings.GetExperimentOutputDir(true) + "_ExcludedTrainingIDs.txt";
            String output = Lists.Join(excludedTrainingIDs, "\t");
            Files.WriteLineToFile(filePath, output);
        }
    }

    private void SaveCrossValidationAssignments() throws Exception
    {
        Files.WriteTextToFile(Settings.GetExperimentOutputDir(true) + "Validation_Assignments_OuterFolds.txt", Utilities.InstanceVault.GetCrossValidationAssignments().toString());

        if (Utilities.InstanceVault.GetCrossValidationAssignments().NumFolds != Utilities.InstanceVault.GetTransformedDependentVariableInstances().Size())
            for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetAllFoldNumbers())
            {
                Files.WriteTextToFile(Settings.GetExperimentOutputDir(true) + "Validation_Assignments_InnerFolds_For_OuterFold" + outerFold + ".txt", Utilities.InstanceVault.GetCrossValidationAssignments().GetInnerAssignments(outerFold).toString());
            }
    }

    public ArrayList<LockedCallable<Object>> SaveMeanFeatureRanksFiles() throws Exception
    {
        ArrayList<LockedCallable<Object>> lockedCallables = new ArrayList<LockedCallable<Object>>();

        for (final AbstractDataProcessor processor : Utilities.ProcessorVault.IndependentVariableDataProcessors)
            for (final FeatureSelectionAlgorithm fsAlgorithm : Utilities.Config.GetFeatureSelectionAlgorithms())
                lockedCallables.add(new LockedCallable("SaveMeanFeatureRanks" + processor.GetDescription() + fsAlgorithm.Description, new Callable<Object>()
                {
                    public Object call() throws Exception
                    {
                        SaveMeanFeatureRanksFile(processor, fsAlgorithm);
                        return Boolean.TRUE;
                    }
                }));

        return lockedCallables;
    }

    private void SaveMeanFeatureRanksFile(AbstractDataProcessor processor, FeatureSelectionAlgorithm fsAlgorithm) throws Exception
    {
        if (fsAlgorithm.IsNone())
            return;

        FeatureRanks ranks = new FeatureRanks();

        for (int outerFold : Utilities.InstanceVault.GetCrossValidationAssignments().GetFoldsWithTestData(processor))
        {
            ArrayList<String> rankedFeatures = new FeatureEvaluator(processor, fsAlgorithm, outerFold).GetOuterSelectedFeatures(-1);

            if (rankedFeatures.size() == 0)
                throw new Exception("An error occurred when trying to save mean feature ranks file for outer fold " + outerFold + ", feature selection algorithm " + fsAlgorithm.Description + " and " + processor.GetDescription() + ". No features were selected.");

            ranks.Add(rankedFeatures);
        }

        String filePath = Settings.GetExperimentOutputDir(true) + processor.GetDescription() + "_" + fsAlgorithm + "_MeanFeatureRanks.txt";
        ranks.SaveFeaturesInOrderOfMeanRankToFile(filePath);
    }
}
