package org.lasarobotics.vision.ftc.resq;

import org.jetbrains.annotations.Nullable;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.util.MathUtil;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Internal class designed to score and analyze data relating to the beacon
 */
class BeaconScoring {
    static class ScoredEllipse extends Ellipse
    {
        double score;

        ScoredEllipse(RotatedRect rect, double score) {
            super(rect);
            this.score = score;
        }
        ScoredEllipse(Ellipse ellipse, double score) {
            super(ellipse.getRect());
            this.score = score;
        }
    }

    static final double ECCENTRICITY_BEST = 0.3; //best eccentricity for 100% score
    static final double ECCENTRICITY_BIAS = 5.0; //points given at best eccentricity
    static final double ECCENTRICITY_NORM = 0.1; //normal distribution variance for eccentricity


    static final double AREA_MIN = 0.0025;        //minimum area as percentage of screen (0 points)
    static final double AERA_MAX = 0.1;           //maximum area (0 points given)

    static final double SCORE_MIN = 0; //minimum score to keep the ellipse

    static List<ScoredEllipse> scoreEllipses(List<Ellipse> ellipses,
                                             @Nullable Point estimateLocation,
                                             @Nullable Double estimateDistance)
    {
        List<ScoredEllipse> scores = new ArrayList<>();

        for(Ellipse ellipse : ellipses)
        {
            double score = 0;

            //Find the eccentricity - the closer it is to 0, the better
            double eccentricity = ellipse.eccentricity();
            //Best value is 1
            double eccentricityValue = (Math.max(eccentricity - ECCENTRICITY_BEST, 0));
            score += MathUtil.normalDistribution(eccentricityValue, ECCENTRICITY_NORM, 0) * ECCENTRICITY_BIAS;
            //currently, an eccentricity of .7 gives 3 less points (3.5 pts) than an eccentricity of .3 (6.5 pts)

            //Find the area - the closer to a certain range, the better
            double area = ellipse.area();

            //TODO Find the on-screen location - the closer it is to the estimate, the better

            //Find the color - the more black, the better (significantly)

            //If score is above a value, keep the ellipse
            if (score >= SCORE_MIN)
                scores.add(new ScoredEllipse(ellipse, score));
        }
        return scores;
    }
}
