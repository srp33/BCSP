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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** This data processor is designed to parse information from the TCGA somatic-mutation data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaSomaticMutationDataProcessor extends AbstractTcgaDataProcessor
{
    @Override
    protected void ParseRawData() throws Exception
    {
        HashMap<String, ArrayList<String>> genePatientMutations = new HashMap<String, ArrayList<String>>();

        for (File file : Files.GetFilesInDirectory(GetRawDataDir(), "*level3.maf"))
        {
            BigFileReader reader = new BigFileReader(file);
            ArrayList<String> headerItems = Lists.CreateStringList(reader.ReadLine().trim().split("\t"));

            for (String line : reader)
            {
                ArrayList<String> lineItems = Lists.CreateStringList(line.trim().split("\t"));
                String gene = lineItems.get(headerItems.indexOf("Hugo_Symbol"));
                String patientID = FormatColumnName(lineItems.get(headerItems.indexOf("Tumor_Sample_Barcode")));
                String variantClassification = lineItems.get(headerItems.indexOf("Variant_Classification"));
                String validationStatus = lineItems.get(headerItems.indexOf("Validation_Status"));
                String mutationStatus = lineItems.get(headerItems.indexOf("Mutation_Status"));

                if (!variantClassification.contains("Silent") && validationStatus.equals("Valid") && mutationStatus.equals("Somatic"))
                {
                    ArrayList<String> patientsWithAMutation = Lists.CreateStringList(patientID);
                    if (genePatientMutations.containsKey(gene))
                        patientsWithAMutation.addAll(genePatientMutations.get(gene));
                    genePatientMutations.put(gene, new ArrayList<String>(new HashSet<String>(patientsWithAMutation)));
                }
            }
        }

        ArrayList<String> patientIDs = GetUniquePatientIDs(genePatientMutations);

        for (String gene : genePatientMutations.keySet())
        {
            for (String patientID : patientIDs)
            {
                String status = "0";
                if (genePatientMutations.get(gene).contains(patientID))
                    status = "1";

                SaveRawDataPoint(gene, patientID, status);
            }
        }
    }

    @Override
    protected void UpdateInstancesForAnalysis(DataInstanceCollection instances) throws Exception
    {
        ArrayList<String> dataPointsToRemove = new ArrayList<String>();

        for (String dataPoint : instances.GetDataPointNames())
        {
            int mutationCount = Lists.GetNumMatches(instances.GetDataPointValues(dataPoint).GetAllValues(), "1");
            if (mutationCount < 2)
                dataPointsToRemove.add(dataPoint);
        }

        instances.RemoveDataPointNames(dataPointsToRemove);
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    private ArrayList<String> GetUniquePatientIDs(HashMap<String, ArrayList<String>> map)
    {
        HashSet<String> patientIDs = new HashSet<String>();

        for (Map.Entry<String, ArrayList<String>> entry : map.entrySet())
            patientIDs.addAll(entry.getValue());

        return new ArrayList<String>(patientIDs);
    }

    @Override
    protected ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return Utilities.FormatNames(Lists.CreateStringList("IDH1", "TP53"));
        //IDH1: Parsons, Bujko2010
        //TP53: Ohgaki2004 (but not independent of age), Schmidt2002
    }
}
