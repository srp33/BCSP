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

/** This data processor is designed to parse information from the TCGA clinical data; specifically, it handles treatment data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaTreatmentsDataProcessor extends AbstractTcgaDataProcessor
{
    private TcgaClinicalTabDataParser _parser;

    /** Constructor */
    public TcgaTreatmentsDataProcessor() throws Exception
    {
        ArrayList<String> drugList = Lists.CreateStringList("TEMOZOLOMIDE", "DEXAMETHASONE", "LOMUSTINE", "BEVACIZUMAB");
        _parser = new TcgaClinicalTabDataParser(this, drugList);
    }

    @Override
    protected void ParseRawData() throws Exception
    {
        _parser.SaveRawData();
    }

    @Override
    protected DataValueMeta GetDataPointMeta(String dataPointName)
    {
        return _parser.FindDataValueMeta(dataPointName);
    }

    @Override
    protected boolean KeepRawInstance(DataValues instance) throws Exception
    {
        return _parser.KeepPatient(instance);
    }

    @Override
    protected DataValues TransformRawInstance(DataValues instance) throws Exception
    {
        DataValues transformed = instance.CopyStructure();

        String drugName = instance.GetDataPointValue("DRUGNAME");
        if (drugName != null)
        {
            ArrayList<String> drugs = Lists.CreateStringList(drugName.split(","));
            transformed.AddBinaryDataPoint("temozolomide treatment", drugs, "TEMOZOLOMIDE");
            transformed.AddBinaryDataPoint("dexamethasone treatment", drugs, "DEXAMETHASONE");
            transformed.AddBinaryDataPoint("lomustine treatment", drugs, "LOMUSTINE");
            transformed.AddBinaryDataPoint("bevacizumab treatment", drugs, "BEVACIZUMAB");
            transformed.AddBinaryDataPoint("other drug treatment", drugs, "OTHER");
        }

        transformed.AddDataPoint("radiation treatment", instance.GetDataPointValue("RADIATIONTHERAPY"));

        return transformed;
    }

    @Override
    protected DataInstanceCollection GetTransformedInstances() throws Exception
    {
        DataInstanceCollection instances = GetInstances();

        for (String x : GetPatientFilterCriteria())
            instances.RemoveDataPointName(Utilities.FormatName(x));

        return instances;
    }

    @Override
    protected ArrayList<String> GetPriorKnowledgeSelectedFeatures()
    {
        return Utilities.FormatNames(Lists.CreateStringList("temozolomide treatment", "radiation treatment"));
        // Temozolomide: Stupp,Weller
        // Radiation: DAVIS1949,Rich2005,Ruano2009
        // Tumor resection: Lacroix2001,Houillier2006,Ruano2009,Gundersen1996,Lamborn2004,Batchelor
    }

    private DataInstanceCollection _instances = null;
    private DataInstanceCollection GetInstances() throws Exception
    {
        if (_instances == null)
            _instances = GetInstancesFromFile();
        return _instances;
    }

    /** Indicates whether to keep a given data instance or to ignore it.
     *
     * @param instanceID Data instance ID
     * @return Whether to keep the data instance or ignore it
     * @throws Exception
     */
    public boolean KeepInstance(String instanceID) throws Exception
    {
        if (GetPatientFilterCriteria().size() == 0)
            return true;

        DataValues patient = GetInstances().Get(instanceID);

        for (String x : GetPatientFilterCriteria())
        {
            x = Utilities.FormatName(x);

            if (patient.GetDataPointValue(x).equals("0") || patient.GetDataPointValue(x).equals(Settings.MISSING_VALUE_STRING))
                return false;
        }

        return true;
    }

    private static ArrayList<String> GetPatientFilterCriteria() throws Exception
    {
        return Utilities.Config.GetStringListConfigValue("PATIENT_FILTER_CRITERIA", new String[0]);
    }
}