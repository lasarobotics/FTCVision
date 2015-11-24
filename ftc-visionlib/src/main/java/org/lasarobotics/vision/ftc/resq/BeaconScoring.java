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

        public static List<Contour> getList(List<ScoredContour> scored)
        {
            List<Contour> contours= new ArrayList<>();
            for (ScoredContour c : scored)
                contours.add(c.contour);
            return contours;
        }
    }

    static class AssociatedContour extends Scorable
    {
        ScoredContour contour;
        List<ScoredEllipse> ellipses;

        AssociatedContour(ScoredContour contour, List<ScoredEllipse> ellipses)
        {
            super(score());
            this.contour = contour;
            this.ellipses = ellipses;
        }

        private static double score()
        {
            //TODO Finally, a fraction of the ellipse value is added to the value of the contour
            //The best ellipse is found first, then only this ellipse adds to the value

            return 0.0f;
        }
    }

    static final double CONTOUR_RATIO_BEST = Constants.BEACON_WH_RATIO; //best ratio for 100% score
    static final double CONTOUR_RATIO_BIAS = 1.5; //points given at best ratio
    static final double CONTOUR_RATIO_NORM = 0.2; //normal distribution variance for ratio

    static final double CONTOUR_AREA_MIN = Math.log10(0.01);
    static final double CONTOUR_AREA_MAX = Math.log10(0.75);
    static final double CONTOUR_AREA_NORM = 0.2;
    static final double CONTOUR_AREA_BIAS = 5.0;

    static final double CONTOUR_SCORE_MIN = 1;


    static final double ELLIPSE_ECCENTRICITY_BEST = 0.4; //best eccentricity for 100% score
    static final double ELLIPSE_ECCENTRICITY_BIAS = 3.0; //points given at best eccentricity
    static final double ELLIPSE_ECCENTRICITY_NORM = 0.1; //normal distribution variance for eccentricity

    static final double ELLIPSE_AREA_MIN = 0.0001;        //minimum area as percentage of screen (0 points)
    static final double ELLIPSE_AREA_MAX = 0.01;         //maximum area (0 points given)
    static final double ELLIPSE_AREA_NORM = 1;
    static final double ELLIPSE_AREA_BIAS = 2.0;

    static final double ELLIPSE_CONTRAST_THRESHOLD = 60.0;
    static final double ELLIPSE_CONTRAST_BIAS = 7.0;
    static final double ELLIPSE_CONTRAST_NORM = 0.1;

    static final double ELLIPSE_SCORE_MIN = 1; //minimum score to keep the ellipse - theoretically, should be 1


    static final double ASSOCIATION_MAX_DISTANCE = 0.05; //as fraction of screen


    /**
     * Create a normalized subscore around a current and expected value, using the normalized Normal CDF.
     *
     * This function is derived from the statistical probability of achieving the optimal value. Values that
     * are less than the best value will return the best subscore, or the bias (unless ignoreSign = true).
     * @param value The actual value - if this is < bestValue, the subscore will be maximized to the bias
     * @param bestValue The optimal value
     * @param variance The variance of the normal CDF function, greater than 0
     * @param bias The bias that the normalized normal CDF is to be multiplied by - the result will not be larger than the bias
     * @param ignoreSign If true, ignore the sign of value - bestValue. If false (default), the calculation of value in the
     *                   normal PDF will be performed as Math.max(value - bestValue, 0), ensuring that if value is less than
     *                   bestValue, that the PDF will return the bias.
     * @return The subscore based on the probability of achieving the optimal value
     */
    private static double createSubscore(double value, double bestValue, double variance, double bias, boolean ignoreSign)
    {
        return Math.min(MathUtil.normalPDFNormalized((ignoreSign ? value - bestValue : Math.max((value - bestValue), 0)) / bestValue,
                variance, 0) * bias, bias);
    }

    List<ScoredContour> scoreContours(List<Contour> contours,
                                      @Nullable Point estimateLocation,
                                      @Nullable Double estimateDistance,
                                      Mat rgba,
                                      Mat gray)
    {
        List<ScoredContour> scores = new ArrayList<>();

        for(Contour contour : contours)
        {
            double score = 1;

            //Find ratio - the closer it is to the actual ratio of the beacon, the better
            Size size = contour.size();
            double ratio = size.width / size.height;
            double ratioSubscore = createSubscore(ratio, CONTOUR_RATIO_BEST, CONTOUR_RATIO_NORM, CONTOUR_RATIO_BIAS, true);
            score *= ratioSubscore;

            //Find the area - the closer to a certain range, the better
            //We also take the log for better area comparisons
            double area = Math.log10(size.area() / imgSize.area());
            //Best value is the root mean squared of the min and max areas
            final double areaBestValue = Math.signum(CONTOUR_AREA_MIN) * Math.sqrt(CONTOUR_AREA_MIN * CONTOUR_AREA_MIN + CONTOUR_AREA_MAX * CONTOUR_AREA_MAX) / 2;
            double areaSubscore = createSubscore(area, areaBestValue, CONTOUR_AREA_NORM, CONTOUR_AREA_BIAS, true);
            score *= areaSubscore;

            //TODO take color estimations into account

            //If score is above a value, keep the contour
            if (score >= CONTOUR_SCORE_MIN)
                scores.add(new ScoredContour(contour, score));
        }
        return scores;
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
            double eccentricitySubscore = createSubscore(eccentricity, ELLIPSE_ECCENTRICITY_BEST, ELLIPSE_ECCENTRICITY_NORM, ELLIPSE_ECCENTRICITY_BIAS, false);
            score *= eccentricitySubscore;
            //f(0.3) = 5, f(0.75) = 0

            //Find the area - the closer to a certain range, the better
            double area = ellipse.area() / gray.size().area(); //area as a percentage of the area of the screen
            //Best value is the root mean squared of the min and max areas
            final double areaBestValue = Math.sqrt(ELLIPSE_AREA_MIN * ELLIPSE_AREA_MIN + ELLIPSE_AREA_MAX * ELLIPSE_AREA_MAX) / 2;
            double areaSubscore = createSubscore(area, areaBestValue, ELLIPSE_AREA_NORM, ELLIPSE_AREA_BIAS, true);
            score *= areaSubscore;

            //TODO Find the on-screen location - the closer it is to the estimate, the better

            //Find the color - the more black, the better (significantly)
            Ellipse e = ellipse.scale(0.5); //get the center 50% of data
            double averageColor = e.averageColor(gray, ColorSpace.GRAY).getScalar().val[0];
            double colorSubscore = createSubscore(averageColor, ELLIPSE_CONTRAST_THRESHOLD, ELLIPSE_CONTRAST_NORM, ELLIPSE_CONTRAST_BIAS, false);
            score *= colorSubscore;

            //If score is above a value, keep the ellipse
            if (score >= ELLIPSE_SCORE_MIN)
                scores.add(new ScoredEllipse(ellipse, score));
        }

        return (List<ScoredEllipse>)Scorable.sort(scores);
    }

    List<AssociatedContour> associate(List<ScoredContour> contours, List<ScoredEllipse> ellipses)
    {
        //TODO Ellipses with nearby/contained contours associate themselves with the contour
        //TODO Ellipses without nearby/contained contours are removed
        //TODO Contours without nearby/contained ellipses lose value

        List<AssociatedContour> associations = new ArrayList<>();

        for (ScoredContour contour : contours)
        {
            AssociatedContour associatedContour = new AssociatedContour(contour, new ArrayList<ScoredEllipse>());
            for (ScoredEllipse ellipse : ellipses)
            {
                if (ellipse.ellipse.isInside(contour.contour) ||
                        (MathUtil.distance(ellipse.ellipse.center(), contour.contour.center()) <=
                                ASSOCIATION_MAX_DISTANCE * imgSize.width))
                    associatedContour.ellipses.add(ellipse);
            }
            if (associatedContour.ellipses.size() > 0)
                associations.add(associatedContour);
        }

        return associations;
    }

    static class MultiAssociatedContours
    {
        List<AssociatedContour> redContours;
        List<AssociatedContour> blueContours;

        MultiAssociatedContours(List<AssociatedContour> redContours, List<AssociatedContour> blueContours)
        {
            this.redContours = redContours;
            this.blueContours = blueContours;
        }
    }

    @SuppressWarnings("unchecked")
    MultiAssociatedContours scoreAssociations(List<ScoredContour> contoursRed,
                                              List<ScoredContour> contoursBlue,
                                              List<ScoredEllipse> ellipses)
    {
        List<AssociatedContour> associationsRed = associate(contoursRed, ellipses);
        List<AssociatedContour> associationsBlue = associate(contoursBlue, ellipses);

        //TODO Pairs of ellipses (those with similar size and x-position) greatly increase the associated contours' value
        //calculateEllipsePairs()
        //TODO Contours near another contour of the opposite color increase in value
        //calculateContourPairs()
        //TODO Contours near the expected zone (if any expected zone) increase in value
        //calculateContourZones()

        //Finally, the list is sorted by score
        return new MultiAssociatedContours((List<AssociatedContour>)AssociatedContour.sort(associationsRed),
                (List<AssociatedContour>)AssociatedContour.sort(associationsBlue));
    }
}
