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

import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** This class contains logic for various methods of evaluating classification performance.
 * @author Stephen Piccolo
 */
public class EvaluationMetrics implements ITest
{
    public void Test() throws Exception
    {
        //http://www.ncbi.nlm.nih.gov/pmc/articles/PMC403858/table/tbl1/
        //Survivals survivalsGroup1 = new Survivals(new Survival(6), new Survival(13), new Survival(21), new Survival(30), new Survival(31, true), new Survival(37), new Survival(38), new Survival(47, true), new Survival(49), new Survival(50), new Survival(63), new Survival(79), new Survival(80, true), new Survival(82, true), new Survival(82, true), new Survival(86), new Survival(98), new Survival(149, true), new Survival(202), new Survival(219));
        //Survivals survivalsGroup2 = new Survivals(new Survival(10), new Survival(10), new Survival(12), new Survival(13), new Survival(14), new Survival(15), new Survival(16), new Survival(17), new Survival(18), new Survival(20), new Survival(24), new Survival(24), new Survival(25), new Survival(28), new Survival(30), new Survival(33), new Survival(34, true), new Survival(35), new Survival(37), new Survival(40), new Survival(40), new Survival(40, true), new Survival(46), new Survival(48), new Survival(70, true), new Survival(76), new Survival(81), new Survival(82), new Survival(91), new Survival(112), new Survival(181));
        //Survivals survivalsGroup1 = new Survivals(new Survival(1, true), new Survival(2), new Survival(3), new Survival(4));
        //Survivals survivalsGroup2 = new Survivals(new Survival(10), new Survival(20), new Survival(30), new Survival(40, true));
        //Survivals survivalsGroup1 = new Survivals(new Survival(9), new Survival(13), new Survival(13, true), new Survival(18), new Survival(23), new Survival(28, true), new Survival(31), new Survival(34), new Survival(45, true), new Survival(48), new Survival(161, true));
        //Survivals survivalsGroup2 = new Survivals(new Survival(5), new Survival(5), new Survival(8), new Survival(8), new Survival(12), new Survival(16, true), new Survival(23), new Survival(27), new Survival(30), new Survival(33), new Survival(43), new Survival(45));
        //SurvivalsList sList = new SurvivalsList(survivalsGroup1, survivalsGroup2);
        //CalculateLogRankStatisticMartinBland(sList);
        //CalculateLogRankStatisticTwoGroups(sList);
    }

    /** Calculates the log-rank statistic for two or more survival groups.
     *
     * @param survivalsList List of survival groups
     * @return Log-rank statistic
     * @throws Exception
     */
    public double CalculateLogRankStatistic(SurvivalGroups survivalsList) throws Exception
    {
        if (survivalsList.Size() > 2)
            return CalculateLogRankStatisticMultipleGroups(survivalsList);
        else
            return CalculateLogRankStatisticTwoGroups(survivalsList);
    }

    // This should give the exact same result as when computed in R
    private double CalculateLogRankStatisticTwoGroups(SurvivalGroups survivalsList) throws Exception
    {
        ArrayList<Double> allDiff1 = new ArrayList<Double>();
        ArrayList<Double> allVar = new ArrayList<Double>();

        for (double time : new ArrayList<Double>(Lists.Sort(new ArrayList(new HashSet<Double>(survivalsList.GetObservedTimes())))))
        {
            double n = Lists.GreaterThan(survivalsList.GetAllTimes(), time, true).size();
            double n1 = Lists.GreaterThan(survivalsList.GetGroup(0).GetAllTimes(), time, true).size();
            double n2 = Lists.GreaterThan(survivalsList.GetGroup(1).GetAllTimes(), time, true).size();
            double e = Lists.GetNumEqualTo(survivalsList.GetObservedTimes(), time);
            double e1 = Lists.GetNumEqualTo(survivalsList.GetGroup(0).GetObservedTimes(), time);
            double exp1 = e * (n1 / n);
            double var = n <= 1.0 ? 0.0 : (n1 * n2 * e * (n-e)) / (n * n * (n-1));

            allDiff1.add(e1 - exp1);
            allVar.add(var);
        }

        double logRankStatistic = Math.pow(MathUtility.Sum(allDiff1), 2.0) / MathUtility.Sum(allVar);

        org.apache.commons.math.distribution.ChiSquaredDistributionImpl chi = new ChiSquaredDistributionImpl(survivalsList.Size() - 1);
        double chiSquareP = 1 - chi.cumulativeProbability(logRankStatistic);

        return chiSquareP;
    }

    // This approach is based on the following article and should be suitable when there are more than two survival groups
    // http://www.ncbi.nlm.nih.gov/pmc/articles/PMC403858/?tool=pmcentrez
    private double CalculateLogRankStatisticMultipleGroups(SurvivalGroups survivalsList) throws Exception
    {
        double[] allExpected = new double[survivalsList.Size()];
        long[] allObserved = new long[survivalsList.Size()];

        HashMap<Integer, ArrayList<Double>> allTimesExpected = new HashMap<Integer, ArrayList<Double>>();

        for (int i=0; i<survivalsList.Size(); i++)
            allTimesExpected.put(i, new ArrayList<Double>());

        for (double time : new ArrayList<Double>(Lists.Sort(new ArrayList(new HashSet<Double>(survivalsList.GetObservedTimes())))))
        {
            double numAtRisk = Lists.GreaterThan(survivalsList.GetAllTimes(), time, true).size();
            double numDied = Lists.GetNumEqualTo(survivalsList.GetObservedTimes(), time);
            double riskOfDeath = numDied / numAtRisk;

            for (int i=0; i<survivalsList.Size(); i++)
            {
                Survivals survivals = survivalsList.GetGroup(i);

                double numAllEvents = Lists.GreaterThan(survivals.GetAllTimes(), time, true).size();
                double numExpectedEvents = numAllEvents * riskOfDeath;

                allTimesExpected.get(i).add(numExpectedEvents);
            }
        }

        for (int i=0; i<survivalsList.Size(); i++)
        {
            double expected = MathUtility.Sum(allTimesExpected.get(i));
            long observed = survivalsList.GetGroup(i).GetObservedTimes().size();

            allExpected[i] = expected;
            allObserved[i] = observed;
        }

        //http://commons.apache.org/math/api-2.1/index.html
        org.apache.commons.math.stat.inference.ChiSquareTestImpl chi = new org.apache.commons.math.stat.inference.ChiSquareTestImpl();
        double chiSquareP = chi.chiSquareTest(allExpected, allObserved);

        return chiSquareP;
    }


    //Methods for Multi-Category Cancer Diagnosis from Gene
//From: Expression Data: A Comprehensive Evaluation to Inform Decision Support System Development.?
//RCI package:StabPerf
// 663
// 664 Calculates Relative Classifier Information.
// 665
// 666 DESCRIPTION
// 667 Calculates Relative Class Information of a confusion matrix.
// 668
// 669 USAGE
// 670 RCI(cmat)
// 671
// 672 EXAMPLES
// 673 model <- load.model(\"data/model.RData\")
// 674 data <- loadData(\"data/dataset.RData\")
// 675 prediction <- predict(model, data[,1:2])
// 676 cmat <- confusion.matrix(data$y[1:2], prediction)   # create confusion matrix
// 677 RCI(cmat)
// 678
// 679 ARGUMENTS
// 680  cmat: confusion matrix with true membership in rows classifications in cols.
// 681
// 682 VALUE
// 683  score: Numeric. Value of Relative Class Information.
// 684
// 685 DETAILS
// 686 RCI takes prior class probability into account. See reference for further information.
// 687
// 688 SEE.ALSO
// 689 accuracy
// 690
// 691 REFERENCES
// 692 Statnikov et al., 2004
//
//    RCI <- function(cmat) {
//     695     if (is.null(cmat) || is.na(cmat)) return(MediaSize.NA)
//     696     if (dim(cmat)[1] != dim(cmat)[2]) return(MediaSize.NA)
//     697
//     698     # Rows (i) are the real class memberships
//     699     # Columns (j) are the classification labels returned by some classifier
//     700     total.sum <- sum(cmat)
//     701     row.sums <- as.numeric(apply(cmat, 1, sum))
//     702     col.sums <- as.numeric(apply(cmat, 2, sum))
//     703
//     704     # Probability that a previously unseen sample is truly in class i
//     705     # I.e. prior class probabilities
//     706     # I.e. probability that the classifier *input* is in class i
//     707     probs.in <- row.sums / total.sum
//     708     # Probability of any sample being labeled as the given class
//     709     # I.e. probability that the classifier *output* is in class j
//     710     probs.out <- col.sums / total.sum
//     711
//     712     # Entropy/uncertainty of class priors
//     713     H.in <- sum(-probs.in*log(probs.in))
//     714
//     715     # Given the class j, what's the probability that a sample with real class i
//     716     # will be classfied as j ?
//     717     likelihood <- apply(cmat, 2, function(x) x / sum(x) )
//     718     # Entropy/uncertainty of a classfication, given the classification j
//     719     H.likelihood <-
//     720       apply(likelihood, 2, function(x) sum(-x*log(x), na.rm=TRUE) )
//
//# Entropy/uncertainty of input class given classifier output
// 724     H.out <- sum(probs.out * H.likelihood)
// 725
// 726     # How much the entropy/uncertainty was reduced is our
// 727     # Relative Classifier Information (RCI)
// 728     return(H.in - H.out)
// 729
// 730 } # RCI
}
