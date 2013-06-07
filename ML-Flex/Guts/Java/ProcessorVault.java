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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** This class provides convenience methods to access instances of data processors.
 * @author Stephen Piccolo
 */
public class ProcessorVault
{
    public ArrayList<AbstractMetadataProcessor> MetaDataProcessors = new ArrayList<AbstractMetadataProcessor>();
    public ArrayList<AbstractDataProcessor> AllDataProcessors = new ArrayList<AbstractDataProcessor>();
    public AbstractDependentVariableDataProcessor DependentVariableDataProcessor = null;
    public AbstractDataProcessor ThresholdSelectionDataProcessor = null;
    public ArrayList<AbstractDataProcessor> IndependentVariableDataProcessors = new ArrayList<AbstractDataProcessor>();

    /** This method obtains configuration information for the various types of processors that will be used in a given experiment, creates instances of those processors, and caches those instances in public objects for each access (and so they only have to be instantiated one time).
     *
     * @throws Exception
     */
    public void Load() throws Exception
    {
        for (String className : Utilities.Config.GetStringListConfigValue("META_DATA_PROCESSORS", new String[0]))
            MetaDataProcessors.add((AbstractMetadataProcessor) ((Constructor) Class.forName(className).getConstructor()).newInstance());

        for (String classInstantiationText : Utilities.Config.GetMandatoryStringListConfigValue("INDEPENDENT_VARIABLE_DATA_PROCESSORS"))
        {
            AbstractDataProcessor processor = InstantiateDataProcessor(classInstantiationText);
            IndependentVariableDataProcessors.add(processor);

            AddProcessorToAll(processor);
        }

        DependentVariableDataProcessor = (AbstractDependentVariableDataProcessor)InstantiateDataProcessor(Utilities.Config.GetMandatoryStringValue("DEPENDENT_VARIABLE_DATA_PROCESSOR"));

        if (DependentVariableDataProcessor.getClass().getName().equals(AggregateDataProcessor.class.getName()))
            throw new Exception("The dependent variable data processor cannot be " + AggregateDataProcessor.class.getName());

        AddProcessorToAll(DependentVariableDataProcessor);

        if (Utilities.Config.HasConfigValue("THRESHOLD_SELECTION_DATA_PROCESSOR"))
        {
            ThresholdSelectionDataProcessor = InstantiateDataProcessor(Utilities.Config.GetMandatoryStringValue("THRESHOLD_SELECTION_DATA_PROCESSOR"));
            AddProcessorToAll(ThresholdSelectionDataProcessor);
        }
    }

    private void AddProcessorToAll(AbstractDataProcessor processor)
    {
        if (AllDataProcessors.contains(processor))
            Utilities.Log.ExceptionFatal("Multiple data processors with description of " + processor.GetDescription() + " cannot be used in same experiment.");

        AllDataProcessors.add(processor);
    }

    private AbstractDataProcessor InstantiateDataProcessor(String classInstantiationText) throws Exception
    {
        Utilities.Log.Debug("Attempting to instantiate " + classInstantiationText);

        if (classInstantiationText.contains("("))
        {
            String className = classInstantiationText.substring(0, classInstantiationText.indexOf("("));

            classInstantiationText = classInstantiationText.replace("\"", "").substring(classInstantiationText.indexOf("(") + 1).replace(")", "");

            ArrayList<String> parameters = Lists.CreateStringList(classInstantiationText.split(","));

            for (int i=0; i<parameters.size(); i++)
                parameters.set(i, parameters.get(i).trim());

            Class[] constructorParamClasses = new Class[parameters.size()];

            for (int i=0; i<constructorParamClasses.length; i++)
            {
                constructorParamClasses[i] = String.class;

                if (DataTypes.IsInteger((parameters.get(i).toString())))
                    constructorParamClasses[i] = Integer.class;
                else
                {
                    if (DataTypes.IsDouble((parameters.get(i).toString())))
                        constructorParamClasses[i] = Double.class;
                }

                if (DataTypes.IsBoolean((parameters.get(i).toString())))
                    constructorParamClasses[i] = Boolean.class;
            }

            Object[] objectParameters = new Object[parameters.size()];
            for (int i=0; i<parameters.size(); i++)
            {
                Object objectParameter = parameters.get(i);

                if (DataTypes.IsInteger(parameters.get(i).toString()))
                    objectParameter = Integer.parseInt(parameters.get(i));
                else
                {
                    if (DataTypes.IsDouble(parameters.get(i).toString()))
                        objectParameter = Double.parseDouble(parameters.get(i));
                }

                if (DataTypes.IsBoolean(parameters.get(i).toString()))
                    objectParameter = Boolean.parseBoolean(parameters.get(i));

                objectParameters[i] = objectParameter;
            }

            return (AbstractDataProcessor) ((Constructor) Class.forName(className).getConstructor(constructorParamClasses)).newInstance(objectParameters);
        }
        else
        {
            return (AbstractDataProcessor) ((Constructor) Class.forName(classInstantiationText).getConstructor()).newInstance();
        }
    }
}
