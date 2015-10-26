package org.lasarobotics.vision.ftc.resq;

import org.jetbrains.annotations.Nullable;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Internal class designed to score and analyze data relating to the beacon
 */
class BeaconScoring {
    class ScoredEllipse extends Ellipse
    {
        double score;

        ScoredEllipse(RotatedRect rect, double score) {
            super(rect);
        }
    }

    static List<ScoredEllipse> scoreEllipses(List<Ellipse> ellipses, @Nullable Point estimate)
    {
        List<ScoredEllipse> scores = new ArrayList<>();

        //Find the eccentricity - the closer it is to 0, the better

        //Find the size - the closer to a certain range, the better

        //TODO Find the on-screen location - the closer it is to the estimate, the better

        //Find the color - the more black, the better (significantly)

        return scores;
    }
}
