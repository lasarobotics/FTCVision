//
// FAST object detection
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

    static std::vector<KeyPoint> keypoints_object;
    static Mat descriptors_object;

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

    static vector<DMatch> matches;

    static vector<Point2f> train_pts, query_pts;
    static vector<KeyPoint> train_kpts, query_kpts;
    static vector<unsigned char> match_mask;

    static Mat gray;

    static bool ref_live = true;

    static Mat train_desc, query_desc;
    static const int DESIRED_FTRS = 500;

    static GridAdaptedFeatureDetector detector(new FastFeatureDetector(10, true), DESIRED_FTRS, 4, 4);
    static BriefDescriptorExtractor brief(32);
    static BFMatcher desc_matcher(NORM_HAMMING);

    static Mat H_prev = Mat::eye(3, 3, CV_32FC1);

    /*** PUBLIC ***/

    JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_Homography_analyzeObject(JNIEnv* jobject, jlong addrGrayObject) {

        Mat img_object = *(Mat*)addrGrayObject;

        if( !img_object.data )
        { printf(" --(!) Error reading images "); return; }

        //detect
        detector.detect( img_object, keypoints_object );

        //extract
        //extractor = FREAK::create();
        brief.compute( img_object, keypoints_object, descriptors_object);

        __android_log_print(ANDROID_LOG_ERROR, "ftcvision", "Object ananlyzed!");
    }

    JNIEXPORT void JNICALL Java_com_lasarobotics_vision_detection_Homography_findObject(JNIEnv* jobject, jlong addrGrayObject, jlong addrGrayScene, jlong addrOutput) {

        Mat frame = *(Mat*)addrGrayScene;
        Mat object = *(Mat*)addrGrayObject; //TODO currently unused!
        Mat img_matches = *(Mat*)addrOutput;

        if (frame.empty())
            return;

        cvtColor(frame, gray, COLOR_RGB2GRAY);

        detector.detect(gray, query_kpts); //Find interest points

        brief.compute(gray, query_kpts, query_desc); //Compute brief descriptors at each keypoint location

        if (!train_kpts.empty())
        {
            vector<KeyPoint> test_kpts;
            warpKeypoints(H_prev.inv(), query_kpts, test_kpts);

            Mat mask = windowedMatchingMask(test_kpts, train_kpts, 25, 25);
            desc_matcher.match(query_desc, train_desc, matches, mask);
            drawKeypoints(frame, test_kpts, frame, Scalar(255, 0, 0), DrawMatchesFlags::DRAW_OVER_OUTIMG);

            matches2points(train_kpts, query_kpts, matches, train_pts, query_pts);

            if (matches.size() > 5)
            {
                Mat H = findHomography(train_pts, query_pts, RANSAC, 4, match_mask);
                if (countNonZero(Mat(match_mask)) > 15)
                {
                    H_prev = H;
                }
                else
                    resetH(H_prev);
                drawMatchesRelative(train_kpts, query_kpts, matches, frame, match_mask);
                drawObjectLocation(H, frame, img_matches);
            }
            else
                resetH(H_prev);

        }
        else
        {
            H_prev = Mat::eye(3, 3, CV_32FC1);
            Mat out;
            drawKeypoints(gray, query_kpts, out);
            frame = out;
        }

        //imshow("frame", frame);

        if (ref_live)
        {
            train_kpts = query_kpts;
            query_desc.copyTo(train_desc);
        }
        return;
    }

}