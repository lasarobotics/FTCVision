#include <string.h>
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
using namespace std;
using namespace cv;

extern "C"
{
    JNIEXPORT jstring JNICALL Java_com_lasarobotics_vision_detection_Features_stringFromJNI(
            JNIEnv *env, jobject type);

    JNIEXPORT jstring JNICALL Java_com_lasarobotics_vision_detection_Features_stringFromJNI
            (JNIEnv *env, jobject type) {
        return env->NewStringUTF("Hello from JNI");
    }

    JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_Features_highlightFeatures(JNIEnv* jobject, jlong addrGray, jlong addrRgba)
    {
        Mat& mGr  = *(Mat*)addrGray;
        Mat& mRgb = *(Mat*)addrRgba;
        vector<KeyPoint> v;

        Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
        detector->detect(mGr, v);
        for( unsigned int i = 0; i < v.size(); i++ )
        {
            const KeyPoint& kp = v[i];
            circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
        }
    }
}