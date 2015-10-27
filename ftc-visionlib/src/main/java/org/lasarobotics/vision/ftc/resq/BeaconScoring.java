package org.lasarobotics.vision.ftc.resq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.color.ColorSpace;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Internal class designed to score and analyze data relating to the beacon
 */
class BeaconScoring {

    Size imgSize;

    BeaconScoring(Size imgSize)
    {
        this.imgSize = imgSize;
    }

    static class Scorable implements Comparable<Scorable>
    {
        double score;

        protected Scorable(double score)
        {
            this.score = score;
        }

        public int compareTo(@NotNull Scorable another) {
            //This is an inverted sort - largest first
            return this.score > another.score ? -1 : this.score < another.score ? 1 : 0;
        }

        public static List<? extends Scorable> sort(List<? extends Scorable> ellipses)
        {
            Scorable[] scoredEllipses = ellipses.toArray(new Scorable[ellipses.size()]);
            Arrays.sort(scoredEllipses);
            List<Scorable> finalEllipses = new ArrayList<>();
            Collections.addAll(finalEllipses, scoredEllipses);
            return finalEllipses;
        }
    }

    static class ScoredEllipse extends Scorable
    {
        Ellipse ellipse;

        ScoredEllipse(Ellipse ellipse, double score) {
            super(score);
            this.ellipse = ellipse;
        }

        public static List<Ellipse> getList(List<ScoredEllipse> scored)
        {
            List<Ellipse> ellipses = new ArrayList<>();
            for (ScoredEllipse e : scored)
                ellipses.add(e.ellipse);
            return ellipses;
        }
    }

    static class ScoredContour extends Scorable
    {
        Contour contour;

        ScoredContour(Contour contour, double score) {
            super(score);
            this.contour = contour;
        }
    }

    static final double ECCENTRICITY_BEST = 0.4; //best eccentricity for 100% score
    static final double ECCENTRICITY_BIAS = 3.0; //points given at best eccentricity
    static final double ECCENTRICITY_NORM = 0.1; //normal distribution variance for eccentricity

    static final double AREA_MIN = 0.0025;        //minimum area as percentage of screen (0 points)
    static final double AREA_MAX = 0.1;           //maximum area (0 points given)

    static final double CONTRAST_THRESHOLD = 60.0;
    static final double CONTRAST_BIAS = 7.0;
    static final double CONTRAST_NORM = 0.1;

    static final double SCORE_MIN = 1; //minimum score to keep the ellipse

    /**
     * Create a normalized subscore around a current and expected value, using the normalized Normal CDF.
     *
     * This function is derived from the statistical probability of achieving the optimal value. Values that
     * are less than the best value will return the best subscore, or the bias.
     * @param value The actual value - if this is < bestValue, the subscore will be maximized to the bias
     * @param bestValue The optimal value
     * @param variance The variance of the normal CDF function, greater than 0
     * @param bias The bias that the normalized normal CDF is to be multiplied by - the result will not be larger than the bias
     * @return The subscore based on the probability of achieving the optimal value
     */
    private static double createSubscore(double value, double bestValue, double variance, double bias)
    {
        return Math.min(MathUtil.normalPDFNormalized(Math.max((value - bestValue), 0) / bestValue,
                variance, 0) * bias, bias);
    }

    @SuppressWarnings("unchecked")
    List<ScoredEllipse> scoreEllipses(List<Ellipse> ellipses,
                                      @Nullable Point estimateLocation,
                                      @Nullable Double estimateDistance,
                                      Mat gray)
    {
        List<ScoredEllipse> scores = new ArrayList<>();

        for(Ellipse ellipse : ellipses)
        {
            double score = 1;

            //Find the eccentricity - the closer it is to 0, the better
            double eccentricity = ellipse.eccentricity();
            double eccentricitySubscore = createSubscore(eccentricity, ECCENTRICITY_BEST, ECCENTRICITY_NORM, ECCENTRICITY_BIAS);
            score *= eccentricitySubscore;
            //f(0.3) = 5, f(0.75) = 0

            //TODO Find the area - the closer to a certain range, the better
            double area = ellipse.area();

            //TODO Find the on-screen location - the closer it is to the estimate, the better


            //Find the color - the more black, the better (significantly)
            Ellipse e = ellipse.scale(0.5); //get the center 50% of data
            double averageColor = e.averageColor(gray, ColorSpace.GRAY).getScalar().val[0];
            double colorSubscore = createSubscore(averageColor, CONTRAST_THRESHOLD, CONTRAST_NORM, CONTRAST_BIAS);
            score *= colorSubscore;

            //If score is above a value, keep the ellipse
            if (score >= SCORE_MIN)
                scores.add(new ScoredEllipse(ellipse, score));
        }

        return (List<ScoredEllipse>)Scorable.sort(scores);
    }
}
