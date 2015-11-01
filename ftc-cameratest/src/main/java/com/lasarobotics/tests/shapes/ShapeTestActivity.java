package com.lasarobotics.tests.shapes;

import com.lasarobotics.tests.exceptions.*;

import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.detection.objects.Line;
import org.lasarobotics.vision.detection.objects.Quadrilateral;
import org.opencv.core.Point;

/**
 * Created by arnavmohan on 11/1/15.
 */

public class ShapeTestActivity {
    private boolean rectResult;
    private boolean ellipseResult;
    private boolean lineResult;
    private boolean quadResult;

    public ShapeTestActivity() throws SlopeUndefinedError, IncorrectAnswerError, FakePointError {
        /* new ShapeTestActivity() -> void
        engages each test
         */
        //rectResult = ;
        //ellipseResult = ;
        lineResult = vlineTest() && hlineTest(); //&& nlineTest() && ctrlLine();
        //quadResult = ;
    }

    public static boolean vlineTest() throws SlopeUndefinedError, FakePointError, IncorrectAnswerError {
        Line vline = new Line(new Point(2,4), new Point(2,1));
        try{
            if (vline.getSlope() != Double.MIN_VALUE){
                throw new SlopeUndefinedError("ERROR: Line vertical, slope UNDEFINED");
                return false;
            }
            else if (vline.getYIntercept() != Double.MIN_VALUE){
                throw new FakePointError("ERROR: Line vertical, y int DNE");
                return false;
            }
            else if (vline.getLength() != 3){
                throw new IncorrectAnswerError("ERROR: Line length incorrect, correct = 3");
                return false;
            }
            else if (vline.evaluateX(2) != Double.MAX_VALUE){
                throw new IncorrectAnswerError("ERROR: Line x evaluation incorrect, correct = ALL");
                return false;
            }
            else if (vline.intersection(new Line(new Point(2,4),new Point(4,4))) != new Point(2,4)){
                throw new IncorrectAnswerError("ERROR: Point inters incorrect, correct = (2,4)");
                return false;
            }
            else{
                return true;
            }
        }
        catch (SlopeUndefinedError|FakePointError|IncorrectAnswerError e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean hlineTest() throws IncorrectAnswerError{
        Line hLine = new Line(new Point(2,4), new Point(4,4));
        try{
            if (hLine.getSlope() != 0){
                throw new IncorrectAnswerError("ERROR: Line slope incorrect, correct = 0");
                return false;
            }
            else if (hLine.getYIntercept() != 4){
                throw new IncorrectAnswerError("ERROR: Line y int incorrect, correct = 4");
                return false;
            }
            else if (hLine.getLength() != 2){
                throw new IncorrectAnswerError("ERROR: Line length incorrect, correct = 2");
                return false;
            }
            else if (hLine.evaluateX(3) != 4){
                throw new IncorrectAnswerError("ERROR: Line x evaluation incorrect, correct = 4");
                return false;
            }
            else if (hLine.intersection(new Line(new Point(2,4),new Point(2,1))) != new Point(2,4)){
                throw new IncorrectAnswerError("ERROR: Point inters incorrect, correct = (2,4)");
                return false;
            }
            else{
                return true;
            }
        }
        catch (IncorrectAnswerError e){
            e.printStackTrace();
        }

        return true;
    }


}
