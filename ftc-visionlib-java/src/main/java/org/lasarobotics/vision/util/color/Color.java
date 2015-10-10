package org.lasarobotics.vision.util.color;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Implements a color in any color space
 */
public abstract class Color {

    protected Scalar scalar;

    Color(Scalar s)
    {
        this.scalar = s;
    }

    public Scalar getScalar()
    {
        return scalar;
    }
    public Scalar getScalarRGBA()
    {
        return convertColorScalar(ColorSpace.RGBA);
    }

    public abstract ColorSpace getColorSpace();

    public Color convertColor(ColorSpace to)
    {
        Scalar output = convertColorScalar(to);

        Class<? extends Color> colorClass = to.getColorClass();

        try {
            return colorClass.getConstructor(Scalar.class).newInstance(output);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Cannot convert color to the desired color space.");
        }
    }

    public Scalar convertColorScalar(ColorSpace to)
    {
        if (getColorSpace() == to)
            return getScalar();
        if (!getColorSpace().canConvertTo(to))
            throw new IllegalArgumentException("Cannot convert color to the desired color space.");

        Scalar output = this.getScalar();

        try {

            for (int i = 0; i < getColorSpace().getConversionsTo(to).length; i += 3) {
                int conversion = getColorSpace().getConversionsTo(to)[i];
                int inputDim = getColorSpace().getConversionsTo(to)[i + 1];
                int outputDim = getColorSpace().getConversionsTo(to)[i + 2];

                Mat pointMatTo = new Mat();
                Mat pointMatFrom = new Mat(1, 1, CvType.CV_8UC(inputDim), output);
                Imgproc.cvtColor(pointMatFrom, pointMatTo, conversion, outputDim);
                output = new Scalar(pointMatFrom.get(0, 0));
            }
        } catch (Exception ignored)
        {
            throw new IllegalArgumentException("Cannot convert color to the desired color space.");
        }

        return output;
    }
}
