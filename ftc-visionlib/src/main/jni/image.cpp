//
// Image manipulation and correction
//

#include <string.h>
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
using namespace std;
using namespace cv;

extern "C"
{

    JNIEXPORT void JNICALL Java_com_lasarobotics_vision_Image_rotate(JNIEnv* jobject, jlong addrImage, jdouble angle) {
        Mat img = *(Mat*)addrImage;

        int len = std::max(img.cols, img.rows);
        Point2f pt(len/2., len/2.);
        Mat r = cv::getRotationMatrix2D(pt, angle, 1.0);

        cv:warpAffine(img, img, r, cv::Size(len, len));
    }

}

