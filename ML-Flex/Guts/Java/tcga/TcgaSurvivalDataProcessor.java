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
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** This data processor is designed to parse information from the TCGA clinical data; specifically, it handles survival data. This data can be downloaded from http://tcga-data.nci.nih.gov/tcga/dataAccessMatrix.htm. This code has so far been tested only on the glioblastoma multiforme data.
 * @author Stephen Piccolo
 */
public class TcgaSurvivalDataProcessor extends AbstractDependentVariableDataProcessor
{
    private String _survivalDataPointName = "";
    private String _lowRiskDescriptor = "";
    private String _highRiskDescriptor = "";

    private TcgaClinicalTabDataParser _parser;
    private AbstractDependentVariableTransformer _dependentVariableTransformer;

    /** Constructor */
    public TcgaSurvivalDataProcessor() throws Exception
    {
        _parser = new TcgaClinicalTabDataParser(this);
        _dependentVariableTransformer = GetDependentVariableTransformerFromConfig();
        _survivalDataPointName = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_DATA_POINT_NAME", "ContinuousDependentVariable");
        _lowRiskDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_LOW_DESCRIPTOR", "LOW");
        _highRiskDescriptor = Utilities.Config.GetStringValue("CONTINUOUS_DEPENDENT_VARIABLE_HIGH_DESCRIPTOR", "HIGH");
    }

    private AbstractDependentVariableTransformer GetDependentVariableTransformerFromConfig() throws Exception
    {
        String className = Utilities.Config.GetStringValue("DEPENDENT_VARIABLE_TRANSFORMER", new MedianContinuousDependentVariableTransformer().getClass().getSimpleName());
        return (AbstractDependentVariableTransformer) ((Constructor) Class.forName(className).getConstructor()).newInstance();
    }

    @Override
    protected boolean HasContinuousRawValues()
    {
        return true;
    }

    @Override
    protected String GetRawDataDirName()
    {
        return "TCGAData";
    }

    @Override
    protected AbstractDependentVariableTransformer GetDependentVariableTransformer()
    {
        return _dependentVariableTransformer;
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
        transformed.AddDataPoint(_survivalDataPointName, instance.GetDataPointValue(TcgaClinicalTabDataParser.SURVIVAL_DATA_POINT_NAME));
        return transformed;
    }

    @Override
    protected boolean KeepTransformedInstance(DataValues instance) throws Exception
    {
        return new TcgaTreatmentsDataProcessor().KeepInstance(instance.GetID());
    }

    @Override
    protected Boolean PostProcessRawData() throws Exception
    {
        _dependentVariableTransformer.Preprocess();
        return Boolean.TRUE;
    }

    @Override
    public String GetDependentVariableDataPointName()
    {
        return _survivalDataPointName;
    }

    @Override
    protected ArrayList<String> GetUniqueDependentVariableValues() throws Exception
    {
        return Lists.CreateStringList(_lowRiskDescriptor, _highRiskDescriptor);
    }

    @Override
    public boolean CalculateResultsSeparatelyForEachFold() throws Exception
    {
        return _dependentVariableTransformer instanceof ContinuousDependentVariableDiscretizationTransformer;
    }
}