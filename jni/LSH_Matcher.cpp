#include "LSH_Matcher.h"
#include "opencv2\core\core.hpp"
#include "opencv2\highgui\highgui.hpp"
#include "opencv2\features2d\features2d.hpp"
#include "opencv2\nonfree\features2d.hpp"
#include "opencv2\calib3d\calib3d.hpp"
#include "opencv2\flann\flann.hpp"
#include "opencv2\objdetect\objdetect.hpp"
#include <stdlib.h>

using namespace cv;
using namespace std;

private LSH_Matcher::FlannBasedMatcher match;
/*
 * Class:     ccard_thesis_Indoor_Localization_and_Guidance_JNI_Interface_LSH_Wrapper
 * Method:    init
 * Signature: (III)V
 */
JNIEXPORT void JNICALL LSH_Matcher::Java_ccard_thesis_Indoor_1Localization_1and_1Guidance_JNI_1Interface_LSH_1Wrapper_init
  (JNIEnv *env, jobject obj, jint table_num, jint key_size, jint multi_probe_level){
  		match = FlannBasedMatcher(new flann::LshIndexParams(table_num,
		key_size,multi_probe_level));
  }

/*
 * Class:     ccard_thesis_Indoor_Localization_and_Guidance_JNI_Interface_LSH_Wrapper
 * Method:    train
 * Signature: ()V
 */
JNIEXPORT void JNICALL LSH_Matcher::Java_ccard_thesis_Indoor_1Localization_1and_1Guidance_JNI_1Interface_LSH_1Wrapper_train
  (JNIEnv *env, jobject obj){
  	match.train();
  }

/*
 * Class:     ccard_thesis_Indoor_Localization_and_Guidance_JNI_Interface_LSH_Wrapper
 * Method:    add
 * Signature: (J)V
 */
JNIEXPORT void JNICALL LSH_Matcher::Java_ccard_thesis_Indoor_1Localization_1and_1Guidance_JNI_1Interface_LSH_1Wrapper_add
  (JNIEnv *env, jobject obj, jlong image){
  	Mat &des = *(Mat *)image;
  	match.add(des);
  }

/*
 * Class:     ccard_thesis_Indoor_Localization_and_Guidance_JNI_Interface_LSH_Wrapper
 * Method:    knnMatch
 * Signature: (JIZ)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL LSH_Matcher::Java_ccard_thesis_Indoor_1Localization_1and_1Guidance_JNI_1Interface_LSH_1Wrapper_knnMatch
  (JNIEnv *env, jobject obj, jlong query, jint k, jboolean consolidate){
  	vector<DMatch> v;
  	vector<Mat> masks;
  	Mat &q = *(Mat*)query;

  	match.knnMatch(q,v,k,masks,consolidate);

  	return NULL;
  }