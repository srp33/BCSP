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

/** This data processor parses chromosome-level data for Agilent CGH arrays for data from The Cancer Genome Atlas. This data can be downloaded from The TCGA data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm.
 * @author Stephen Piccolo
 */
public class TcgaChromosomeCopyNumberDataProcessor extends AbstractTcgaDataProcessor
{
    private final static String INPUT_FILE = "mskcc.org__HG-CGH-244A__copy_number_analysis.txt";

    @Override
    protected DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return new DataValueMeta(dataPointName, new MeanSummarizer(), new NullTransformer(), "NA");
    }

    @Override
    protected void ParseRawData() throws Exception
    {
        ChromosomeBandMetadataProcessor metadataProcessor = new ChromosomeBandMetadataProcessor();

        ArrayList<String> fileLines = Files.ReadLinesFromFile(GetRawDataDir() + INPUT_FILE);
        fileLines.remove(0);

        for (String line : fileLines)
        {
            String[] lineItems = line.split("\t");
            String patientID = FormatColumnName(lineItems[0]);
            //String chromosome = lineItems[1];
            int startPosition = Integer.parseInt(lineItems[2]);
            int stopPosition = Integer.parseInt(lineItems[3]);
            String value = lineItems[5];

            if (DataTypes.IsDouble(value))
                for (String chromosomeBand :  metadataProcessor.GetBandsMatchingPosition(startPosition, stopPosition))
                    SaveRawDataPoint(chromosomeBand, patientID, value);
        }
    }

    @Override
    protected String FormatColumnName(String columnName)
    {
        return TcgaClinicalTabDataParser.FormatPatientID(columnName);
    }

    @Override
    protected ArrayList<String> GetPriorKnowledgeSelectedFeatures() throws Exception
    {
        ArrayList<String> selectedFeatures = new ArrayList<String>();

        for (String feature : Utilities.InstanceVault.GetAnalysisDataPoints(this, null))
            for (String prefix : Lists.CreateStringList("12q", "7p", "10q23", "19p", "9p"))
                if (feature.startsWith("chr" + prefix))
                    selectedFeatures.add(feature);

        return Utilities.FormatNames(selectedFeatures);

        //return Lists.CreateStringList("MDM2", "EGFR", "PTEN", "CDKN2A");

        //CDK4 (12q): Ruano2009
        //MDM2 (12q): Houillier2006, Schiebe2000
        //7: Korshunov2005
        //EGFR (7p): Houillier2006,Korshunov2005 (only for patients < 50 years), Shinojima2003, Smith2001 (only for older patients)
        //6q, 10p, 10q, 19p, 19q, 20q (Burton2002)
        //p16/CDKN2A deletion (9p): Rasheed2002 (also included AA), Korshunov2005
        //9: Korshunov2005
        //PTEN (10q23): Sano1999, Korshunov2005
        //SKP2 (5p): Saigusa
        //1p: Homma2006
        //10q: Homma2006
        //19: Korshunov2005
    }
}