package dt.tsg.utils;

import dt.tsg.InputParams.InputParameters.Distribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;

public class RandomNumberGeneration {

    /**
     * returns a random integer within the lowerbound (inclusive) and upperbound (inclusive) according to the set distribution.
     *
     * @param lowerBound   The lower bound for the random value generation
     * @param upperBound   The upper bound for the random value generation
     * @param distribution The distribution for the random value generation
     * @param poissonMean  The Poisson mean (only used if distribution is set to POISSON)
     * @return A random value.
     */
    public static int getNumWithDistribution(int lowerBound, int upperBound, Distribution distribution, Utils utils, double poissonMean, double binomialP, int binomialN, ArrayList<String> warnings) {
        int maxDiscards = 1000000;
        int returnvalue;
        switch (distribution) {
            case UNIFORM -> returnvalue = utils.random.nextInt(lowerBound, upperBound + 1);
            case GEOMETRIC -> {
                returnvalue = lowerBound;
                while (returnvalue != upperBound && utils.random.nextInt(2) == 0) {
                    returnvalue++;
                }
            }
            case POISSON -> {
                if (poissonMean == 0.0) {
                    System.out.println("WARNING: PoissonMean was 0.0 during Random Number Generation. Do not use the POISSON Distribution for values with an expected mean of 0!");
                    if (!warnings.contains("WARNING: PoissonMean was 0.0 during Random Number Generation. Do not use the POISSON Distribution for values with an expected mean of 0!")) {
                        warnings.add("WARNING: PoissonMean was 0.0 during Random Number Generation. Do not use the POISSON Distribution for values with an expected mean of 0!");
                    }
                    return 0;       // the mean value is 0, meaning the only outcome is 0
                }
                PoissonDistribution poissonDistribution = new PoissonDistribution(poissonMean);
                // get a poisson distributed value, discard all values that are not inside our bounds
                int numIterations = 0;
                do {
                    returnvalue = poissonDistribution.sample();
                    numIterations++;
                } while ((returnvalue < lowerBound || returnvalue > upperBound) && numIterations < maxDiscards);
                if (numIterations == maxDiscards) {
                    throw new RuntimeException("Maximum number of discards reached during random number generation.\nMaxDiscards = " + maxDiscards);
                }
            }
            case BINOMIAL -> {
                BinomialDistribution binomialDistribution = new BinomialDistribution(binomialN, binomialP);
                // get a poisson distributed value, discard all values that are not inside our bounds
                int numIterations = 0;
                do {
                    returnvalue = binomialDistribution.sample();
                    numIterations++;
                } while ((returnvalue < lowerBound || returnvalue > upperBound) && numIterations < maxDiscards);
                if (numIterations == maxDiscards) {
                    throw new RuntimeException("Maximum number of discards reached during random number generation.\nMaxDiscards = " + maxDiscards);
                }
            } // add more Distributions here
            default -> {
                System.out.println("WARNING: Distribution not recognized. Using uniform distribution as default.");
                if (!warnings.contains("WARNING: Distribution not recognized. Using uniform distribution as default.")) {
                    warnings.add("WARNING: Distribution not recognized. Using uniform distribution as default.");
                }
                returnvalue = utils.random.nextInt(lowerBound, upperBound + 1);
            }
        }
        return returnvalue;
    }
}
