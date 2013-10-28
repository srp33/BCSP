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
import java.util.HashSet;
import java.util.Iterator;

/** This class stores information about multiple predictions that have been made. It contains methods to make it easier to deal with multiple predictons.
 * @author Stephen Piccolo
 */
public class Predictions implements Iterable<Prediction>
{
    private ArrayList<Prediction> _predictions = new ArrayList<Prediction>();

    /** Default constructor
     */
    public Predictions()
    {
    }

    /** Alternate constructor
     *
     * @param predictionList List of predictions
     */
    public Predictions(ArrayList<Predictions> predictionList)
    {
        for (Predictions predictions : predictionList)
            Add(predictions);
    }

    /** Adds a prediction to this set of predictions
     *
     * @param prediction A single prediction to add
     * @return This instance
     */
    public Predictions Add(Prediction prediction)
    {
        _predictions.add(prediction);
        return this;
    }

    /** Adds a list of predictions to this set of predictions
     *
     * @param predictions Predictions to add
     * @return This instance
     */
    public Predictions Add(ArrayList<Prediction> predictions)
    {
        _predictions.addAll(predictions);
        return this;
    }

    /** Adds a set of predictions to this set of predictions
     *
     * @param predictions Predictions to add
     * @return This instance
     */
    public Predictions Add(Predictions predictions)
    {
        _predictions.addAll(predictions._predictions);
        return this;
    }

    /** Retrieves the prediction for a given index value
     *
     * @param i Index of the prediction to get
     * @return A prediction
     */
    public Prediction Get(int i)
    {
        return _predictions.get(i);
    }

    /** Retrieves a list of instance IDs for which predictions have been made
     *
     * @return List of instance IDs for which predictions have been made
     */
    public ArrayList<String> GetInstanceIDs()
    {
        HashSet<String> instanceIDs = new HashSet<String>();

        for (Prediction prediction : _predictions)
            instanceIDs.add(prediction.InstanceID);

        return new ArrayList<String>(instanceIDs);
    }

    /** Retrieves a list predictions for a given set of instance IDs
     *
     * @param instanceIDs Instance IDs for which predictions have been made
     * @return Predictions
     */
    public Predictions GetInstancePredictions(ArrayList<String> instanceIDs)
    {
        Predictions predictions = new Predictions();

        for (Prediction prediction : _predictions)
            if (instanceIDs.contains(prediction.InstanceID))
                predictions.Add(prediction);

        return predictions;
    }


    private Predictions GetInstancePredictions(String instanceID)
    {
        Predictions predictions = new Predictions();

        for (Prediction prediction : _predictions)
            if (prediction.InstanceID.equals(instanceID))
                predictions.Add(prediction);

        return predictions;
    }

    /** Convenience method for obtaining a prediction for a given instance ID
     *
     * @param instanceID Data instance ID
     * @return Prediction
     * @throws Exception
     */
    public Prediction GetSinglePrediction(String instanceID) throws Exception
    {
        Predictions predictions = GetInstancePredictions(instanceID);

        if (predictions.Size() == 0)
            throw new Exception("No prediction exists for " + instanceID);
        if (predictions.Size() > 1)
            throw new Exception("More than one prediction exists for " + instanceID + ". Predictions: " + predictions.toString());

        return predictions._predictions.get(0);
    }

    /** Convenience method to indicate how many predictions matched the specified class value
     *
     * @param predictedClass Predicted class value
     * @return Number of predictions matching the specified class value
     */
    public int GetNumMatchingPredictedClasses(String predictedClass)
    {
        int count = 0;

        for (Prediction prediction : _predictions)
            if (prediction.Prediction.equals(predictedClass))
                count++;

        return count;
    }

    /** Indicates the number of unique predicted classes
     *
     * @return Number of unique predicted classes
     */
    public ArrayList<String> GetUniquePredictedClasses()
    {
        HashSet<String> predictedClasses = new HashSet<String>();

        for (Prediction prediction : new ArrayList<Prediction>(_predictions))
                predictedClasses.add(prediction.Prediction);

        return new ArrayList<String>(predictedClasses);
    }

    /** Indicates the number of unique actual classes
     *
     * @return Number of unique actual classes
     */
    public ArrayList<String> GetUniqueActualClasses()
    {
        HashSet<String> actualClasses = new HashSet<String>();

        for (Prediction prediction : new ArrayList<Prediction>(_predictions))
                actualClasses.add(prediction.DependentVariableValue);

        return new ArrayList<String>(actualClasses);
    }

    /** Indicates whether a prediction has been made for a given instance ID
     *
     * @param instanceID Data instance ID
     * @return Whether a prediction has been made
     * @throws Exception
     */
    public boolean HasPrediction(String instanceID) throws Exception
    {
        return GetInstancePredictions(instanceID).Size() > 0;
    }

    /** Reads predictions from a text file when those predictions have already been made and stored.
     *
     * @param filePath Absolute path to the file containing predictions
     * @return Predictions that were in the file
     * @throws Exception
     */
    public static Predictions ReadFromFile(String filePath) throws Exception
    {
        if (!Files.FileExists(filePath))
            return new Predictions();

        ArrayList<ArrayList<String>> fileLines = Files.ParseDelimitedFile(filePath);

        if (fileLines.size() == 0)
            return new Predictions();

        fileLines.remove(0);

        Predictions predictions = new Predictions();

        for (ArrayList<String> row : fileLines)
        {
            String id = row.get(0);
            String actualClass = row.get(1);
            String predictedClass = row.get(2);

            ArrayList<Double> classProbabilities = new ArrayList<Double>();
            for (int i=3; i<row.size(); i++)
                classProbabilities.add(Double.parseDouble(row.get(i)));

            predictions.Add(new Prediction(id, actualClass, predictedClass, classProbabilities));
        }

        return predictions;
    }

    /** Saves predictions that have already been made, to a file.
     *
     * @param filePath Absolute file path where the predictions will be stored
     * @throws Exception
     */
    public void SaveToFile(String filePath) throws Exception
    {
        ArrayList<String> header = new ArrayList<String>();
        header.addAll(Lists.CreateStringList("InstanceID", "DependentVariableValue", "Prediction"));

        for (String x : Utilities.ProcessorVault.DependentVariableDataProcessor.GetUniqueDependentVariableValues())
            header.add(x + "_Probability");

        Files.WriteLineToFile(filePath, Lists.Join(header, "\t"));

        for (Prediction prediction : _predictions)
        {
            ArrayList<String> outputVals = new ArrayList<String>();

            outputVals.add(prediction.InstanceID);
            outputVals.add(prediction.DependentVariableValue);
            outputVals.add(prediction.Prediction);

            for (double classProbability : prediction.ClassProbabilities)
                outputVals.add(String.valueOf(classProbability));

            Files.AppendLineToFile(filePath, Lists.Join(outputVals, "\t"));
        }
    }

    /** Indicates the number of predictions that have been made
     *
     * @return The number of predictions that have been made
     */
    public int Size()
    {
        return _predictions.size();
    }

    public Iterator<Prediction> iterator()
    {
        return _predictions.iterator();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (Prediction prediction : _predictions)
            builder.append("\n" + prediction.toString());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof Predictions))
            return false;

        return ((Predictions)obj)._predictions.equals(_predictions);
    }

    @Override
    public int hashCode()
    {
        return _predictions.hashCode();
    }
}