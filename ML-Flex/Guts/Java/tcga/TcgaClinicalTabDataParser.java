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

/** This class is designed to parse the tab-delimited files with clinical data in The Cancer Genome Atlas. These files come in a standard format, but it's necessary to perform some interpretation on these data as a preprocessing step to performing machine-learning analyses.
 * @author Stephen Piccolo
 */
public class TcgaClinicalTabDataParser
{
    public static final String SURVIVAL_DATA_POINT_NAME = "DAYSTODEATH";
    private static String SOURCE_FILE_PATTERN = "clinical_*.txt";

    private AbstractDataProcessor _processor;
    private ArrayList<String> _drugList;

    /** Constructor
     *
     * @param processor Data processor for which parsed values will be saved
     */
    public TcgaClinicalTabDataParser(AbstractDataProcessor processor)
    {
        this(processor, new ArrayList<String>());
    }

    /** Constructor
     *
     * @param processor Data processor for which parsed values will be saved
     * @param drugList List of drugs that should be considered as individual entities
     */
    public TcgaClinicalTabDataParser(AbstractDataProcessor processor, ArrayList<String> drugList)
    {
        _processor = processor;
        _drugList = drugList;
    }

    /** This method parses values from the files and saves the values using the specified data processor.
     *
     * @throws Exception
     */
    public void SaveRawData() throws Exception
    {
        for (File file : Files.GetFilesInDirectoryRecursively(_processor.GetRawDataDir(), SOURCE_FILE_PATTERN))
        {
            ArrayList<ArrayList<String>> rows = Files.ParseDelimitedFile(file.getAbsolutePath(), "\t");
            ArrayList<String> header = Lists.TrimStrings(rows.remove(0));

            for (ArrayList<String> row : rows)
            {
                String patientID = FormatPatientID(row.get(0));

                for (int i = 1; i < header.size(); i++)
                {
                    DataValueMeta meta = FindDataValueMeta(header.get(i));

                    if (meta != null)
                    {
                        String value = row.get(i);

                        if (!meta.IsNullValue(value))
                            _processor.SaveRawDataPoint(meta.Name, patientID, value);
                    }
                }
            }
        }
    }

    /** This method indicates whether a patient's data should be kept or ignored.
     *
     * @param patient Data values for patient
     * @return Whether the patient's data should be kept or ignored
     * @throws Exception
     */
    public boolean KeepPatient(DataValues patient) throws Exception
    {
        String vitalStatus = patient.GetDataPointValue("VITALSTATUS");
        String daysToDeath = patient.GetDataPointValue(SURVIVAL_DATA_POINT_NAME);
        String preTreatment = patient.GetDataPointValue("PRETREATMENTHISTORY");

        if (Utilities.IsMissing(vitalStatus) || Utilities.IsMissing(daysToDeath) || Utilities.IsMissing(preTreatment))
            return false;

        return vitalStatus.equals("DECEASED") && preTreatment.equals("NO");
    }

    private ArrayList<DataValueMeta> GetClinicalMeta()
    {
        ArrayList<DataValueMeta> meta = new ArrayList<DataValueMeta>();

        meta.add(new DataValueMeta("DRUGNAME", new CommaSummarizer(), new AbstractTransformer()
            {
                protected String Transform(String value) throws Exception
                {
                    value = value.toUpperCase();

                    if (!_drugList.contains(value))
                        return "OTHER";

                    return value;
                }
            }
        , "null"));
        meta.add(new DataValueMeta("KARNOFSKYPERFORMANCESCORE", new MeanSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta("GENDER", new GetFirstSummarizer(), new GenderTransformer(), "null"));
        meta.add(new DataValueMeta("PRETREATMENTHISTORY", new GetFirstSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta("VITALSTATUS", new GetFirstSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta(SURVIVAL_DATA_POINT_NAME, new GetFirstSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta("AGEATINITIALPATHOLOGICDIAGNOSIS", new GetFirstSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta("RADIATIONTHERAPY", new GetFirstSummarizer(), new YesNoTransformer(), "null"));
        meta.add(new DataValueMeta("NUMBERPROLIFERATINGCELLS", new MeanSummarizer(), new NullTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTTUMORCELLS", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTTUMORNUCLEI", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTNECROSIS", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTSTROMALCELLS", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTINFLAMINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTLYMPHOCYTEINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTMONOCYTEINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTGRANULOCYTEINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTNEUTROPHILINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("PERCENTEOSINOPHILINFILTRATION", new MeanSummarizer(), new HistologicalQuantityTransformer(), "null"));
        meta.add(new DataValueMeta("ENDOTHELIALPROLIFERATION", new MeanSummarizer(), new YesNoTransformer(), "null"));
        meta.add(new DataValueMeta("NUCLEARPLEOMORPHISM", new MeanSummarizer(), new YesNoTransformer(), "null"));
        meta.add(new DataValueMeta("PALISADINGNECROSIS", new MeanSummarizer(), new YesNoTransformer(), "null"));
        meta.add(new DataValueMeta("CELLULARITY", new MeanSummarizer(), new YesNoTransformer(), "null"));

        return meta;
    }

    /** This method does a brute-force search for a meta object describing the given data point name.
     *
     * @param dataPointName Data point name
     * @return Meta information
     */
    public DataValueMeta FindDataValueMeta(String dataPointName)
    {
        DataValueMeta meta = null;

        for (DataValueMeta x : GetClinicalMeta())
            if (x.Name.equals(dataPointName))
            {
                meta = x;
                break;
            }

        return meta;
    }

    /** This method formats a TCGA patient ID into a standard representation.
     *
     * @param patientID Raw patient ID
     * @return Formatted patient ID
     */
    public static String FormatPatientID(String patientID)
    {
        patientID = patientID.substring(0, 12);

        if (!patientID.startsWith("TCGA"))
            patientID = "TCGA_" + patientID;

        return patientID.replace("-", "_");
    }

    private class HistologicalQuantityTransformer extends AbstractTransformer
    {
        @Override
        protected String Transform(String value) throws Exception
        {
            if (value.equals("<5") || value.equals("<1"))
                return "0";

            if (value.equals(">95"))
                return "100";

            return value;
        }
    }

    private class YesNoTransformer extends AbstractTransformer
    {
        @Override
        protected String Transform(String value) throws Exception
        {
            if (value.equals("YES"))
                return "1";

            return "0";
        }
    }

    private class GenderTransformer extends AbstractTransformer
    {
        @Override
        protected String Transform(String value) throws Exception
        {
            if (value.equals("FEMALE"))
                return "1";

            return "0";
        }
    }

    private class CommaSummarizer extends AbstractSummarizer
    {
        @Override
        public String Summarize(ArrayList<String> values) throws Exception
        {
            return Lists.Join(values, ",");
        }
    }
}