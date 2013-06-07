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


/** This data processor is designed to parse information from the TCGA clinical data; specifically, it indicates whether each patient received radiation treament. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaRadiationDependentVariableDataProcessor extends AbstractDependentVariableDataProcessor
{
    private TcgaClinicalTabDataParser _parser;

    /** Constructor */
    public TcgaRadiationDependentVariableDataProcessor() throws Exception
    {
        _parser = new TcgaClinicalTabDataParser(this);
    }

    @Override
    public String GetDependentVariableDataPointName()
    {
        return Utilities.FormatName("radiation treatment");
    }

    @Override
    protected ArrayList<String> GetUniqueDependentVariableValues() throws Exception
    {
        return Lists.CreateStringList("1", "0");
    }

    @Override
    protected String GetRawDataDirName()
    {
        return "TCGAData";
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
        transformed.AddDataPoint("radiation treatment", instance.GetDataPointValue("RADIATIONTHERAPY"));

        return transformed;
    }
}
