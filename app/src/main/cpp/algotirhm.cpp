#include "algorithm.h"
#include "inc/common.h"
#include <fstream>
#include<thread>
#include"ComputeCharacter.h"
#include"svm_predict.h"
#include"svm_train.h"
#include<map>
#include <jni.h>

#include <android/log.h>
#include <sstream>
#include <array>

#define LOG_TAG "gestureRecognition"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

vector<PEN_INFO> res;
int sampleCount = 0;
vector<double> rollBackProb;
vector<double> commonCharacterProb;
bool isRollBack = true;
bool isCommonCharacter = true;

int bihua = 0;

char tag = 'a'; //初始化

int inflectionPointsNumber = 0; //拐点数量

//bool status = false;
void onDataPacket(const PEN_INFO &penInfo, int count) {
    //penInfo.status: 0x00离开, 0x10悬浮, 0x11书写
//	LOGE("penInfo_status===%#x\n", penInfo.nStatus);
//	LOGE("penInfo_nX===%hu\n", penInfo.nX);
//	LOGE("penInfo_nY===%hu\n", penInfo.nY);
	//penInfo.nPress: 0-1023
//	LOGE("penInfo_nPress===%hu\n", penInfo.nPress);
	if (!(penInfo.nX == 20000 && penInfo.nY == 10000)) {
		res.push_back(penInfo);
	} else {
		bihua = count;
		LOGE("bihua===%d\n", bihua);
		LOGE("recognize() begin");
		recognize();
		LOGE("recognize() end");
	}
}

JavaVM *g_VM;
jobject g_obj;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_xmatenotes_logic_model_instruction_SymbolicCommand_encapsulation(JNIEnv *env,
																				  jobject thiz,
																				  jbyte status,
																				  jshort n_x,
																				  jshort n_y,
																				  jshort n_press,
																				  jint count) {
	//JavaVM是虚拟机在JNI中的表示，后面在其他线程回调java层需要用到
	env->GetJavaVM( &g_VM);
	//生成一个全局引用保留下来，以便回调
	g_obj = env->NewGlobalRef(thiz);

	encapsulation(status, n_x, n_y, n_press, count);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_xmatenotes_logic_model_instruction_Instruction_encapsulation(JNIEnv *env,
																			  jobject thiz,
																			  jbyte status,
																			  jshort n_x,
																			  jshort n_y,
																			  jshort n_press,
																			  jint count) {
	//JavaVM是虚拟机在JNI中的表示，后面在其他线程回调java层需要用到
	env->GetJavaVM( &g_VM);
	//生成一个全局引用保留下来，以便回调
	g_obj = env->NewGlobalRef(thiz);

	encapsulation(status, n_x, n_y, n_press, count);
}

void encapsulation(unsigned char status, unsigned short nX, unsigned short nY, unsigned short nPress, int count){
	PEN_INFO penInfo = {status, nX, nY, nPress};
	onDataPacket(penInfo, count);
}

//将点信息写入point.txt（存储单次点信息）
void writeToPoint()
{
	ofstream outfile;
	if (res.size() > 0)
	{
		outfile.open("/data/data/com.example.xmatenotes/files/point.txt");
		outfile << tag << " ";
		outfile << bihua << " ";
		for (int i = 0; i < res.size(); ++i)
		{
			outfile << res[i].nX << " " << res[i].nY << " ";
		}
		outfile << endl;
		outfile.close();
	}
}

//计算实时数据特征
void calCharacter()
{
	ifstream infile;
	ofstream outfile;
	string s;

	infile.open("/data/data/com.example.xmatenotes/files/point.txt");
	outfile.open("/data/data/com.example.xmatenotes/files/processsinglepoint.txt");
	getline(infile, s);
	istringstream iss(s);
	string data;
	iss >> data;
	outfile << data << " ";
	iss >> data;
	outfile << data;
	// 初始化上一个点的坐标
	int last_x, last_y;
	iss >> last_x >> last_y;
	// 从第三个数据开始遍历每个点的坐标
	while (iss >> data) {
		int x = stoi(data);
		iss >> data;
		int y = stoi(data);
		// 计算当前点和上一个点的距离
		double distance = sqrt(pow(x - last_x, 2) + pow(y - last_y, 2));
		// 如果距离大于等于26.08，则记录当前点的坐标
		if (distance >= 26.08) {
			outfile << " " << x << " " << y;
			// 更新上一个点的坐标
			last_x = x;
			last_y = y;
		}
	}
	// 写入换行符
	outfile << endl;
	infile.close();
	outfile.close();

	infile.open("/data/data/com.example.xmatenotes/files/processsinglepoint.txt");

//	infile.open("/data/data/com.example.xmatenotes/files/point.txt");
	outfile.open("/data/data/com.example.xmatenotes/files/pointcharacter.txt");

	getline(infile, s);

	ComputeCharacter cc(s);
	LOGE("cc.Compute() begin");
	cc.Compute();
	LOGE("cc.Compute() end");

	outfile << cc.tag << " ";
	outfile << "0:" << cc.XYRatio << " ";
	outfile << "1:" << cc.strokeCount << " ";
	outfile << "2:" << cc.closeness << " ";
	outfile << "3:" << cc.Compactness << " ";
	outfile << "4:" << cc.curvature << " ";
	outfile << "5:" << cc.xStartMaxLengthRatio << " ";
	outfile << "6:" << cc.yStartMaxLengthRatio << " ";
	outfile << "7:" << cc.xEndMaxLengthRatio << " ";
	outfile << "8:" << cc.yEndMaxLengthRatio << " ";
	outfile << "9:" << cc.xStartMinLengthRatio << " ";
	outfile << "10:" << cc.yStartMinLengthRatio << " ";
	outfile << "11:" << cc.InflectionPointsNumber << " ";
	outfile << "12:" << cc.beginDirection << " "; //开始方向
	outfile << "13:" << cc.endDirection << " "; //中止方向
    outfile << "14:" << cc.pointDensity; //点密度
	inflectionPointsNumber = cc.InflectionPointsNumber;

	outfile << endl;

	infile.close();
	outfile.close();

}

//调用模型识别手势
void PredictGesture()
{
	int paramNumberPredict = 4;
	char *tmp[4] = { "" };
	//tmp[1] = { "data\\point\\pointCharacter.txt" };
	tmp[1] = { "/data/data/com.example.xmatenotes/files/pointcharacter.txt" };
	//tmp[2] = { "data\\model\\modelStroke.txt" };
	tmp[2] = { "/data/data/com.example.xmatenotes/files/modelstroke.txt" };
	//tmp[3] = { "data\\point\\outPoint.txt" };
	tmp[3] = { "/data/data/com.example.xmatenotes/files/outpoint.txt" };
	LOGE("svmPredict() begin");
	svmPredict(paramNumberPredict, tmp);
	LOGE("svmPredict() end");
}

//获取预测结果
void printResult()
{
	ifstream infile;
	string s;

	//infile.open("data\\point\\outPoint.txt");
	infile.open("/data/data/com.example.xmatenotes/files/outpoint.txt");
	getline(infile, s);
    getline(infile, s);
	LOGE("outpoint.txt:%s", s.c_str());
	infile.close();

	if (s.size() <= 0)
	{
		LOGE("1");
        returnResult(6);
	}
	else
	{
		LOGE("2");
		if (isCommonCharacter) {
			LOGE("3");
			LOGE("commonCharacter");
			returnResult(19);
			return;
		} else {
			LOGE("4");

			LOGE("inflectionPointsNumber:%d", inflectionPointsNumber);
//			int result = s[0] - '0';
//			if (s.size() >= 2 && s[1] != ' ') {
//				result = result * 10 + (s[1] - '0');
//			}
			int result = predict_label;
			LOGE("result:%d", result);
			if (result == 0 && bihua == 1 && !isRollBack) {
				LOGE("指令控制符");
				returnResult(4);
			} else if (result == 1 && bihua == 1 && !isRollBack) {
				LOGE("对");
				returnResult(5);
			} else if (result == 2 && bihua == 2 && !isRollBack) {
				LOGE("半对");
				returnResult(6);
			} else if (result == 3 && bihua == 3 && !isRollBack) {
				LOGE("半半对");
				returnResult(7);
			} else if (result == 4 && bihua == 4 && !isRollBack) {
				LOGE("半半半对");
				returnResult(8);
			} else if (result == 5 && bihua == 2 && !isRollBack && inflectionPointsNumber == 0) {
				LOGE("叉");
				returnResult(9);
			} else if (result == 6 && bihua == 2 && !isRollBack) {
				LOGE("问号");
				returnResult(10);
			} else if (result == 7 && bihua == 3 && !isRollBack) {
				LOGE("半问号");
				returnResult(11);
			} else if (result == 8 && bihua == 4 && !isRollBack) {
				LOGE("半半问号");
				returnResult(12);
			} else if (result == 9 && bihua == 5 && !isRollBack) {
				LOGE("半半半问号");
				returnResult(13);
			} else if (result == 10 && bihua == 2 && !isRollBack) {
				LOGE("叹号");
				returnResult(14);
			} else if (result == 11 && bihua == 3 && !isRollBack) {
				LOGE("半叹号");
				returnResult(15);
			} else if (result == 12 && bihua == 4 && !isRollBack) {
				LOGE("半半叹号");
				returnResult(16);
			} else if (result == 13 && bihua == 5 && !isRollBack) {
				LOGE("半半半叹号");
				returnResult(17);
			} else {
				LOGE("请重新输入");
				returnResult(18);
			}
		}
	}
}

void returnResult(int result) {

	JNIEnv *env;
	bool mNeedDetach = JNI_FALSE;

	//获取当前native线程是否有没有被附加到jvm环境中
	int getEnvStat = g_VM->GetEnv( (void **) &env,JNI_VERSION_1_6);
	if (getEnvStat == JNI_EDETACHED) {
		//如果没有， 主动附加到jvm环境中，获取到env
		if (g_VM->AttachCurrentThread( &env, NULL) != 0) {
			return;
		}
		mNeedDetach = JNI_TRUE;
	}

    LOGE("before");
	//通过全局变量g_obj 获取到要回调的类
	jclass callObjectCls = env->GetObjectClass(g_obj);
	if (callObjectCls == 0) {
		LOGE("returnResult(): Unable to find class");
		g_VM->DetachCurrentThread();
		return;
	}
    LOGE("after");

	jmethodID observeMid = env->GetMethodID(callObjectCls, "observe", "(I)V");
	env->CallVoidMethod(g_obj, observeMid, result);

    ClearData();//清空数据结构

	//释放当前线程
	if(mNeedDetach) {
		g_VM->DetachCurrentThread();
	}
	env = NULL;

}

//清空数据
void ClearData()
{
	res.clear();
	LOGE("ClearData()");
}

void loadProb(){
    ifstream infile;
    infile.open("/data/data/com.example.xmatenotes/files/staticprob.txt");

	LOGE("infile.open() success");
    string line;
    while (getline(infile, line)) {
		LOGE("acquire first line");
        stringstream ss(line);
        array<double, 14> arr;
        for (int i = 0; i < 14; i++) {
            if (!(ss >> arr[i])) {
                cerr << "Failed to parse line\n";
                return ;
            }
        }
		LOGE("acquire rollBackProb");
        rollBackProb.insert(rollBackProb.end(), arr.begin(), arr.end());
		LOGE("acquire second line");
        getline(infile, line);
        ss.clear();
        ss.str(line);
        for (int i = 0; i < 14; i++) {
            if (!(ss >> arr[i])) {
                cerr << "Failed to parse line\n";
                return;
            }
        }
		LOGE("acquire commonCharacterProb");
        commonCharacterProb.insert(commonCharacterProb.end(), arr.begin(), arr.end());
    }
    infile.close();
	for (int i = 0; i < 14; i++) {
		LOGE("label:%d  rollBackProb:%f commonCharacterProb:%f", i, rollBackProb[i], commonCharacterProb[i]);
	}
}

//识别手势
void recognize()
{
	if (bihua > 5)
	{
//		cout << "【输入错误，未识别！】" << endl;
//		cout << "*************************end********************" << endl << endl;
	}
	else
	{
		LOGE("loadProb()");
        loadProb();
		LOGE("writeToPoint()");
		writeToPoint();
		globalStrokeCount = 0;
		LOGE("calCharacter()");
		calCharacter();
		LOGE("PredictGesture()");
		PredictGesture();
		LOGE("printResult()");
		printResult();
		LOGE("printResult() end");
		globalStrokeCount = 0;
	}
}
