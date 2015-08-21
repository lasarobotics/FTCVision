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

static void Mat_to_vector_KeyPoint(Mat& mat, vector<KeyPoint>& v_kp)
{
    v_kp.clear();
    assert(mat.type()==CV_32FC(7) && mat.cols==1);
    for (int i=0; i<mat.rows; i++)
    {
        Vec<float, 7> v = mat.at< Vec<float, 7> >(i, 0);
        KeyPoint kp(v[0], v[1], v[2], v[3], v[4], (int)v[5], (int)v[6]);
        v_kp.push_back(kp);
    }
}

static void vector_KeyPoint_to_Mat(vector<KeyPoint> v_kp, Mat& mat)
{
    int count = (int)v_kp.size();
    mat.create(count, 1, CV_32FC(7));
    for(int i=0; i<count; i++)
    {
        KeyPoint kp = v_kp[i];
        mat.at< Vec<float, 7> >(i, 0) = Vec<float, 7>(kp.pt.x, kp.pt.y, kp.size, kp.angle, kp.response, (float)kp.octave, (float)kp.class_id);
    }
}

static void drawObjectLocation(Mat H, Mat img, Mat& imgout)
{
    std::vector<Point2f> obj_corners(4);
    obj_corners[0] = cvPoint(0,0);
    obj_corners[1] = cvPoint(img.cols, 0);
    obj_corners[2] = cvPoint(img.cols, img.rows);
    obj_corners[3] = cvPoint(0, img.rows);
    std::vector<Point2f> scene_corners(4);

    //if (H.cols != 3 || H.rows != 3) { return; }

    perspectiveTransform( obj_corners, scene_corners, H); //TODO this function throws libc fatal signal 11 (SIGSEGV)

    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
    line( imgout, scene_corners[0],
          scene_corners[1], Scalar(0, 255, 0), 4 );
    line( imgout, scene_corners[1],
          scene_corners[2], Scalar( 0, 255, 0), 4 );
    line( imgout, scene_corners[2],
          scene_corners[3], Scalar( 0, 255, 0), 4 );
    line( imgout, scene_corners[3],
          scene_corners[0], Scalar( 0, 255, 0), 4 );
}

static void drawMatchesRelative(const vector<KeyPoint>& train, const vector<KeyPoint>& query,
                                std::vector<cv::DMatch>& matches, Mat& img, const vector<unsigned char>& mask = vector<unsigned char>())
{
    for (int i = 0; i < (int)matches.size(); i++)
    {
        if (mask.empty() || mask[i])
        {
            Point2f pt_new = query[matches[i].queryIdx].pt;
            Point2f pt_old = train[matches[i].trainIdx].pt;

            cv::line(img, pt_new, pt_old, Scalar(0, 0, 255), 1); //blue line from onld to new pointS
            cv::circle(img, pt_new, 2, Scalar(0, 255, 0), 1);    //green circle at current matches
        }
    }
}

//Takes a descriptor and turns it into an xy point
static void keypoints2points(const vector <KeyPoint> &in, vector <Point2f> &out) {
    out.clear();
    out.reserve(in.size());
    for (size_t i = 0; i < in.size(); ++i) {
        out.push_back(in[i].pt);
    }
}

//Takes an xy point and appends that to a keypoint structure
static void points2keypoints(const vector <Point2f> &in, vector <KeyPoint> &out) {
    out.clear();
    out.reserve(in.size());
    for (size_t i = 0; i < in.size(); ++i) {
        out.push_back(KeyPoint(in[i], 1));
    }
}

//Uses computed homography H to warp original input points to new planar position
static void warpKeypoints(const Mat &H, const vector <KeyPoint> &in, vector <KeyPoint> &out) {
    vector <Point2f> pts;
    keypoints2points(in, pts);
    vector <Point2f> pts_w(pts.size());
    Mat m_pts_w(pts_w);
    perspectiveTransform(Mat(pts), m_pts_w, H);
    points2keypoints(pts_w, out);
}

//Converts matching indices to xy points
static void matches2points(const vector <KeyPoint> &train, const vector <KeyPoint> &query,
                           const std::vector <cv::DMatch> &matches, std::vector <cv::Point2f> &pts_train,
                           std::vector <Point2f> &pts_query) {

    pts_train.clear();
    pts_query.clear();
    pts_train.reserve(matches.size());
    pts_query.reserve(matches.size());

    size_t i = 0;

    for (; i < matches.size(); i++) {

        const DMatch &dmatch = matches[i];

        pts_query.push_back(query[dmatch.queryIdx].pt);
        pts_train.push_back(train[dmatch.trainIdx].pt);

    }

}

static void resetH(Mat & H) {
    H = Mat::eye(3, 3, CV_32FC1);
}

/*** PUBLIC ***/

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_drawKeypoints
                       (JNIEnv* jobject, jlong outMat, jlong keypointMat)
{
    Mat output = *(Mat*)outMat;
    Mat keypoints = *(Mat*)keypointMat; //TODO this should really by KeyPoint[]

    drawKeypoints(output, keypoints, output, Scalar(255, 0, 0), DrawMatchesFlags::DRAW_OVER_OUTIMG);
}

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_analyzeObject
                       (JNIEnv* jobject, jlong detectorAddr, jlong extractorAddr, jlong objMat, jlong descriptorMat, jlong keypointsVector)
{
    __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "ANALYSIS - Getting mats");

    Mat object = *(Mat*)objMat;
    Mat descriptors = *(Mat*)descriptorMat;

    __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "ANALYSIS - Getting detector/extractor");

    FeatureDetector* detector = (FeatureDetector*)detectorAddr;
    DescriptorExtractor* extractor = (DescriptorExtractor*)extractorAddr;

    __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "ANALYSIS - Getting keypoints");

    vector<KeyPoint> keypoints;
    Mat keypointsMat = *(Mat*)keypointsVector;
    Mat_to_vector_KeyPoint(keypointsMat, keypoints);

    __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "Analyzing object...");

    if( !object.data )
    {
        __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "ERROR reading object image");
        return;
    }

    //detect
    detector->detect(object, keypoints);

    //extract
    extractor->compute( object, keypoints, descriptors); //we want the descriptors

    __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "Object ananlyzed!");
}

JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_FeatureDetection_locateObject
                       (JNIEnv* jobject, jlong detectorAddr, jlong extractorAddr, jlong matcherAddr, jlong descriptorMat, jlong keypointsObject, jlong sceneMat, jlong outMat)
{
    Mat output = *(Mat*)outMat;
    Mat img_scene = *(Mat*)sceneMat;
    Mat descriptors_object = *(Mat*)descriptorMat;
    std::vector<KeyPoint> keypoints_object = *(std::vector<KeyPoint>*)keypointsObject;

    FeatureDetector* detector = (FeatureDetector*)detectorAddr;
    DescriptorExtractor* extractor = (DescriptorExtractor*)extractorAddr;
    DescriptorMatcher* matcher = (DescriptorMatcher*)matcherAddr;

    std::vector<KeyPoint> keypoints_scene;
    detector->detect( img_scene, keypoints_scene );

    // extract
    Mat descriptors_scene;
    extractor->compute( img_scene, keypoints_scene, descriptors_scene );

    if ((descriptors_object.cols != descriptors_scene.cols) ||
        (descriptors_object.type() != descriptors_scene.type()))
    { return; }

    // match
    std::vector<DMatch> matches;
    matcher->match(descriptors_object, descriptors_scene, matches);

    double max_dist = 0; double min_dist = 100;

    //-- Quick calculation of max and min distances between keypoints
    for( int i = 0; i < descriptors_object.rows; i++ )
    { double dist = matches[i].distance;
        if( dist < min_dist ) min_dist = dist;
        if( dist > max_dist ) max_dist = dist;
    }

    printf("-- Max dist : %f \n", max_dist );
    printf("-- Min dist : %f \n", min_dist );

    //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
    std::vector< DMatch > good_matches;

    for( int i = 0; i < descriptors_object.rows; i++ )
    { if( matches[i].distance < 3*min_dist )
        { good_matches.push_back( matches[i]); }
    }

    //drawMatches( img_object, keypoints_object, img_scene, keypoints_scene,
    //             good_matches, img_matches, Scalar::all(-1), Scalar::all(-1),
    //             vector<char>(), DrawMatchesFlags::DRAW_OVER_OUTIMG);

    //-- Localize the object
    std::vector<Point2f> obj;
    std::vector<Point2f> scene;

    for( int i = 0; i < good_matches.size(); i++ )
    {
        //-- Get the keypoints from the good matches
        obj.push_back( keypoints_object[ good_matches[i].queryIdx ].pt );
        scene.push_back( keypoints_scene[ good_matches[i].trainIdx ].pt );
    }

    if ((obj.size() < 4) || (scene.size() < 4))
    {
        return;
    }

    Mat H = findHomography( obj, scene, RANSAC );

    //-- Get the corners from the image_1 ( the object to be "detected" )
    std::vector<Point2f> obj_corners(4);
    obj_corners[0] = cvPoint(0,0);
    obj_corners[1] = cvPoint( output.cols, 0 );
    obj_corners[2] = cvPoint( output.cols, output.rows );
    obj_corners[3] = cvPoint( 0, output.rows );
    std::vector<Point2f> scene_corners(4);

    if(obj_corners.size() != scene_corners.size())
    {
        return;
    }
    if (H.cols != 3 || H.rows != 3) { return; }

    perspectiveTransform( obj_corners, scene_corners, H); //TODO this function throws libc fatal signal 11 (SIGSEGV)

    //-- Draw lines between the corners (the mapped object in the scene - image_2 )
    line( output, scene_corners[0] + Point2f( output.cols, 0),
          scene_corners[1] + Point2f( output.cols, 0), Scalar(0, 255, 0), 4 );
    line( output, scene_corners[1] + Point2f( output.cols, 0),
          scene_corners[2] + Point2f( output.cols, 0), Scalar( 0, 255, 0), 4 );
    line( output, scene_corners[2] + Point2f( output.cols, 0),
          scene_corners[3] + Point2f( output.cols, 0), Scalar( 0, 255, 0), 4 );
    line( output, scene_corners[3] + Point2f( output.cols, 0),
          scene_corners[0] + Point2f( output.cols, 0), Scalar( 0, 255, 0), 4 );
}

}