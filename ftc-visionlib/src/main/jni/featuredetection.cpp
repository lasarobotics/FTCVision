//
// Algorithms for object detection
//

#include <jni.h>
#include <vector>
#include <stdio.h>
#include <android/log.h>

#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include "opencv2/nonfree/nonfree.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/flann/flann.hpp"

using namespace std;
using namespace cv;

extern "C"
{

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_drawKeypoints
                       (JNIEnv* jobject, jlong outMat, jlong keypointMat)
{
    Mat output = *(Mat*)outMat;
    Mat keypoints = *(Mat*)keypointMat; //TODO this should really by KeyPoint[]


}

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_analyzeObject
                       (JNIEnv* jobject, jlong detectorAddr, jlong extractorAddr, jlong objMat, jlong descriptorMat)
{
    Mat object = *(Mat*)objMat;
    Mat descriptors = *(Mat*)descriptorMat;

    FeatureDetector detector = *(FeatureDetector*)detectorAddr;
    DescriptorExtractor extractor = *(FeatureDetector*)extractorAddr;


}

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_locateObject
                       (JNIEnv* jobject, jlong detectorAddr, jlong extractorAddr, jlong matcherAddr, jlong descriptorMat, jlong outMat)
{
    Mat object = *(Mat*)outMat;
    Mat descriptors = *(Mat*)descriptorMat;
}

}