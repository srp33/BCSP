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

import java.util.*;

/** This class contains logic for summarizing how features are ranked across multiple repetitions (for example, across multiple cross-validation folds).
 * @author Stephen Piccolo
 */
public class FeatureRanks implements Iterable<FeatureRank>
{
    private static final String STORAGE_DELIMITER = ";";

    private ArrayList<FeatureRank> _cumulativeRanks = new ArrayList<FeatureRank>();
    private int _numIterations = 0;

    /** Adds a list of ranked features.
     *
     * @param rankedFeatures List of ranked features
     */
    public void Add(ArrayList<String> rankedFeatures)
    {
        for (int i = 0; i < rankedFeatures.size(); i++)
            SetRank(new FeatureRank(rankedFeatures.get(i), i + 1));

        _numIterations++;
    }

//    public FeatureRanks GetMeanRanks()
//    {
//        Collections.sort(_cumulativeRanks);
//        FeatureRanks results = new FeatureRanks();
//
//        for (FeatureRank fr : _cumulativeRanks)
//            results.SetRank(GetMeanRank(fr));
//
//        return results;
//    }

    private FeatureRank GetMeanRank(FeatureRank featureRank)
    {
        double mean = featureRank.Rank / (double)_numIterations;
        return new FeatureRank(featureRank.Feature, mean);
    }

    /** Gets the features in order of their mean rank.
     *
     * @return Features in order of their mean rank
     */
    public FeatureRanks GetFeaturesInOrderOfMeanRank()
    {
        return GetFeaturesInOrderOfMeanRank(this._cumulativeRanks.size());
    }

    /** Gets the features in order of their mean rank. May only return a subset of features, depending on the numTop value specified.
     *
     * @param numTop Number of top-ranked features to return.
     * @return Top-ranked features
     */
    public FeatureRanks GetFeaturesInOrderOfMeanRank(int numTop)
    {
        Collections.sort(_cumulativeRanks);

        FeatureRanks topFeatures = new FeatureRanks();
        topFeatures._numIterations = this._numIterations;

        for (int i = 0; i < numTop; i++)
            topFeatures.SetRank(GetMeanRank(_cumulativeRanks.get(i)));

        return topFeatures;
    }

    private void SetRank(FeatureRank featureRank)
    {
        if (_cumulativeRanks.contains(featureRank))
        {
            int index = _cumulativeRanks.indexOf(featureRank);
            FeatureRank existing = _cumulativeRanks.get(index);
            existing.Rank = existing.Rank + featureRank.Rank;
            _cumulativeRanks.set(index, existing);
        }
        else
            _cumulativeRanks.add(featureRank);
    }

    private ArrayList<String> GetFeatureNames(FeatureRanks featureRanks)
    {
        ArrayList<String> features = new ArrayList<String>();

        for (FeatureRank fr : featureRanks)
            features.add(fr.Feature);

        return features;
    }

    private ArrayList<Double> GetRanks(FeatureRanks featureRanks)
    {
        ArrayList<Double> ranks = new ArrayList<Double>();

        for (FeatureRank fr : featureRanks)
            ranks.add(fr.Rank);

        return ranks;
    }

    /** Saves features to a text file, in the mean order in which they were ranked.
     *
     * @param filePath Absolute file path
     * @throws Exception
     */
    public void SaveFeaturesInOrderOfMeanRankToFile(String filePath) throws Exception
    {
        FeatureRanks meanRanks = GetFeaturesInOrderOfMeanRank();
        //Files.WriteLineToFile(filePath, Lists.Join(Lists.CreateStringList(Lists.CreateIntegerSequenceList(1, meanRanks.Size())), "\t"));
        //Files.AppendLineToFile(filePath, Lists.Join(Utilities.UnformatNames(GetFeatureNames(meanRanks)), "\t"));
        //Files.AppendLineToFile(filePath, Lists.Join(Lists.CreateStringList(GetRanks(meanRanks)), "\t"));

        ArrayList<String> featureNames = Utilities.UnformatNames(GetFeatureNames(meanRanks));
        ArrayList<Double> ranks = GetRanks(meanRanks);

        Files.WriteLineToFile(filePath, "Feature\tMean Rank");
        for (int i=0; i<featureNames.size(); i++)
            Files.AppendLineToFile(filePath, featureNames.get(i) + "\t" + ranks.get(i));
    }

    /** Indicates number of features that have been ranked.
     *
     * @return Number of features that have been ranked
     */
    public int Size()
    {
        return _cumulativeRanks.size();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (FeatureRank fr : _cumulativeRanks)
            builder.append(STORAGE_DELIMITER + fr.Feature + ":" + fr.Rank);

        return builder.toString();
    }

    public Iterator<FeatureRank> iterator()
    {
        return _cumulativeRanks.iterator();
    }
}
