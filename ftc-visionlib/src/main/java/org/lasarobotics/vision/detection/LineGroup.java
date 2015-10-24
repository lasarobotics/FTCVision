package org.lasarobotics.vision.detection;

/**
 * Store line pairs for Edge Detection
 */
public class LineGroup {
    private Line lineOne;
    private Line lineTwo;
    private Line lineThree;
    private Line lineFour;
    public double distBetween;

    public LineGroup(Line lineOne, Line lineTwo, Line lineThree, Line lineFour)
    {
        this.lineOne = lineOne;
        this.lineTwo = lineTwo;
        this.lineThree = lineThree;
        this.lineFour = lineFour;
        Init();
    }

    private void Init()
    {
        double dist1to2 = 0;
        double dist3to4 = 0;
        double phi =
        distBetween = (dist1to2 + dist3to4)/2.0;
    }
}
