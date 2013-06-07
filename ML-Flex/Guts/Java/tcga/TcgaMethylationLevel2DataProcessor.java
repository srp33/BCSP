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
import java.util.ArrayList;

/** This data processor is designed to parse DNA methylation data from TCGA formatted files; specifically, it parses files in the "Level 2" format, which contains probe-level values. The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaMethylationLevel2DataProcessor extends AbstractTcgaDataProcessor
{
    @Override
    protected void ParseRawData() throws Exception
    {
        for (String filePath : GetRawDataFilePaths())
            ProcessInputFile(filePath);
    }

    private ArrayList<String> GetRawDataFilePaths() throws Exception
    {
        ArrayList<String> filePaths = new ArrayList<String>();

        filePaths.add(GetRawDataDir() + "JHU_USC__IlluminaDNAMethylation_OMA002_CPI/Level_2/jhu-usc.edu__IlluminaDNAMethylation_OMA002_CPI__beta-value.txt");
        filePaths.add(GetRawDataDir() + "JHU_USC__IlluminaDNAMethylation_OMA003_CPI/Level_2/jhu-usc.edu__IlluminaDNAMethylation_OMA003_CPI__beta-value.txt");

        return filePaths;
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    private void ProcessInputFile(String betaFilePath) throws Exception
    {
        Utilities.Log.Info("Processing input file " + betaFilePath);

        BigFileReader betaFileReader = new BigFileReader(betaFilePath);

        ArrayList<String> header = Lists.CreateStringList(betaFileReader.ReadLine().split("\t"));
        header.remove(0);

        betaFileReader.ReadLine();

        for (String betaLine : betaFileReader)
        {
            ArrayList<String> betaLineItems = Lists.CreateStringList(betaLine.trim().split("\t"));

            String probe = betaLineItems.remove(0);

            ArrayList<String> sampleIDs = new ArrayList<String>(header);

            while (betaLineItems.size() > 0)
            {
                String patientID = FormatColumnName(sampleIDs.remove(0));
                String beta = betaLineItems.remove(0);

                if (!beta.equals("null") && !beta.equals("N/A"))
                    SaveRawDataPoint(probe, patientID, beta);
            }
        }
    }

    @Override
    protected DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer());
    }

    @Override
    protected ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return Lists.CreateStringList("MGMT");
        // Weller2009,Hegi2005a,Krex2007
    }
}