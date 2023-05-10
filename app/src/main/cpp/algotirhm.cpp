#include "algorithm.h"
#include "inc/common.h"
#include <fstream>
#include<thread>
#include"ComputeCharacter.h"
#include"svm_predict.h"
#include"svm_train.h"

#include <android/log.h>
#include <jni.h>

#define LOG_TAG "gestureRecognition"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

vector<PEN_INFO> res;
double CenterX, CenterY = 0;
int lengthX, lengthY = 0;
int count1 = 1;
int tag = 0;
int label = 0;
int gestureCount = 1;
int sampleCount = 0;

double write_start_time = 0, write_end_time = 0;
double time_start = 0, time_end = 0;
int bihua = 0;
int nRet = 0;
int NumberToday = 0, correctToday = 0, wrongToday = 0;
int TotalNumber = 0, TotalCorrect = 0, TotalWrong = 0;
double AccuracyToday = 0.0, TotalAccuracy = 0.0;
char* path = nullptr;//????????????·??

bool status = false;
void onDataPacket(const PEN_INFO &penInfo) {
    //penInfo.status: 0x00离开, 0x10悬浮, 0x11书写
	LOGE("penInfo_status===%#x\n", penInfo.nStatus);
	LOGE("penInfo_nX===%hu\n", penInfo.nX);
	LOGE("penInfo_nY===%hu\n", penInfo.nY);
	//penInfo.nPress: 0-1023
	LOGE("penInfo_nPress===%hu\n", penInfo.nPress);
    if ((int)penInfo.nPress > 0)
    {
        status = true;
        if (write_start_time == 0)
        {
            //write_start_time = GetTickCount();
        }
        res.push_back(penInfo);
        //time_start = GetTickCount();
    }
    else
    {
        if (status == true)
        {
            //write_end_time = GetTickCount();
            bihua++;
            tt();
            status = false;
        }

    }
}

JavaVM *g_VM;
jobject g_obj;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_xmatenotes_instruction_Instruction_encapsulation(JNIEnv *env, jobject thiz,
																  jbyte status, jshort n_x,
																  jshort n_y, jshort n_press) {
	//JavaVM是虚拟机在JNI中的表示，后面在其他线程回调java层需要用到
	env->GetJavaVM( &g_VM);
	//生成一个全局引用保留下来，以便回调
	g_obj = env->NewGlobalRef(thiz);

	encapsulation(status, n_x, n_y, n_press);
}

void encapsulation(unsigned char status, unsigned short nX, unsigned short nY, unsigned short nPress){
	PEN_INFO penInfo = {status, nX, nY, nPress};
	onDataPacket(penInfo);
}

void setAddr(char* addr){
	path = addr;
}

//???????????????????
bool IsCorrectGestureName(const string &gestureName)
{
	int n = gestureName.size();
	if (n <= 0 || n > 3)
	{
		return false;
	}
	else
	{
		if (gestureName[0] >= '0' && gestureName[0] <= '5' && n == 1)
		{
			label = gestureName[0] - '0';
			return true;
		}
	}
	return false;
}

//????????
//一二三
void PrintStatisticalResults()
{
	TotalNumber++;
	NumberToday++;
	TotalAccuracy = (double)TotalCorrect / (double)TotalNumber;
	AccuracyToday = (double)correctToday / (double)NumberToday;
	cout << "********************???????????????********************" << endl;

	cout << "*??????????????????????" << TotalNumber << "                               " << endl;
	cout << "*??????????????????????????" << TotalCorrect << "                           " << endl;
	cout << "*?????????????????????????" << TotalWrong << "                           " << endl;
	cout << "*???????????????????????" << TotalAccuracy * 100 << "%" << "                          " << endl;
	cout << "*                                                          " << endl;
	cout << "*??????????????????????" << NumberToday << "                               " << endl;
	cout << "*??????????????????????????" << correctToday << "                           " << endl;
	cout << "*?????????????????????????" << wrongToday << "                           " << endl;
	cout << "*???????????????????????" << AccuracyToday * 100 << "%" << "                          " << endl;

	cout << "************************************************************" << endl << endl;
}

//?洢?????????
void SaveCollectData()
{
	ofstream outfile;



	if (res.size() > 0)
	{
		//outfile.open("data\\collectData\\collectDataStroke.txt", ios::app);
		outfile.open("/data/data/com.example.xmatenotes/files/collectdatastroke.txt", ios::app);

		outfile << label << " ";

		outfile << bihua << " ";
		for (int i = 0; i < res.size(); ++i)
		{
			outfile << res[i].nX << " " << res[i].nY << " ";
		}
		outfile << endl;
		outfile.close();
	}
}

//???????
void collectionData()
{
	ofstream outfile;
	if (res.size() > 0)
	{
		outfile.open("test20200909.txt", ios::app);

		outfile << tag << " ";
		for (int i = 0; i < res.size(); ++i)
		{
			outfile << res[i].nX << " " << res[i].nY << " ";
		}
		outfile << endl;
		res.clear();
		outfile.close();

		cout << "tag:" << tag << "  " << "count:" << count1 << endl;
		if (count1 % 10 == 0)
		{
			tag++;
			count1 = 0;
			cout << "*************************end********************" << endl;
		}
		count1++;
	}
}

//???????д??point.txt
void writeToPoint()
{
	ofstream outfile;
	if (res.size() > 0)
	{
		//outfile.open("data\\point\\point.txt");
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

//??????????????
void calCharacter()
{
	ifstream infile;
	ofstream outfile;
	string s;

	//infile.open("data\\point\\point.txt");
	//outfile.open("data\\point\\pointCharacter.txt");
	infile.open("/data/data/com.example.xmatenotes/files/point.txt");
	outfile.open("/data/data/com.example.xmatenotes/files/pointcharacter.txt");

	getline(infile, s);

	ComputeCharacter cc(s);
	cc.Compute();

	CenterX = cc.CentroidX;
	CenterY = cc.CentroidY;
	lengthX = cc.xLength;
	lengthY = cc.yLength;

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
	outfile << "11:" << cc.InflectionPointsNumber;
	outfile << endl;

	infile.close();
	outfile.close();

}

// ?????ж???
void DistinguishStandard()
{
	ifstream infile;
	string s;

	//infile.open("data\\point\\point.txt");
	infile.open("/data/data/com.example.xmatenotes/files/point.txt");
	getline(infile, s);
	ComputeCharacter cc(s);
	cc.Compute();
	cout << "*                                               *" << endl;
	cout << "*???????ж????????" << "                         *" << endl;
	cout << "*???????????????" << cc.XYRatio << "                      *" << endl;
	cout << "*???????????" << cc.strokeCount << "                                  *" << endl;
	cout << "*??????????" << cc.closeness << "                            *" << endl;
	cout << "*???????????" << cc.Compactness << "                           *" << endl;
	cout << "*?????????" << cc.curvature << "                              *" << endl;
	cout << "*??????????" << cc.beginDirection << "                         " << endl;
	cout << "*??????????" << cc.endDirection << "                            " << endl;
	cout << "*???????X??????????????????????" << cc.xStartMaxLengthRatio << "  " << endl;
	cout << "*???????Y??????????????????????" << cc.yStartMaxLengthRatio << "  " << endl;
	cout << "*???????X??????????????????????" << cc.xEndMaxLengthRatio << "     " << endl;
	cout << "*???????Y??????????????????????" << cc.yEndMaxLengthRatio << "     " << endl;
	cout << "*???????X????????????С??????????" << cc.xStartMinLengthRatio << "   " << endl;
	cout << "*???????Y????????????С??????????" << cc.yStartMinLengthRatio << "    " << endl;
	cout << "*??????????????" << cc.InflectionPointsNumber << "                              *" << endl;

	infile.close();

}

//??????????????
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
	svmPredict(paramNumberPredict, tmp);
}

//??????????
long calPressAvg()
{
	long avgPress = 0;
	for (int j = 0; j < res.size(); ++j)
	{
		avgPress += res[j].nPress;
	}

	int n = res.size();
	if (n > 0)
	{
		avgPress = avgPress / n;
	}
	return avgPress;
}

//?ж????????????????
void judgeControlDirection()
{
	cout << "????";
}

//???????????
void printCommonCharac()
{

	cout << "* ????д???????????                          *" << endl;

	cout << "* ????д???????" << (write_end_time - write_start_time) << "ms                           *" << endl;
	cout << "* ????д??????????" << calPressAvg() << "                         *" << endl;
	cout << "* ???????0                                   *" << endl;
	cout << "* ???????????????                            *" << endl;
	cout << "* ??????????????????????                    *" << endl;
	cout << "* ????д????(???????????)????A4(27000,20600) *" << endl;
	cout << "* ???????                                    *" << endl;
	//cout << "*********************************************" << endl;
}

//??????????????????
void printControlCharac()
{
	cout << "* ???????????????????                        *" << endl;
	cout << "* ???????????????????                        *" << endl;
	cout << "* ???????峤????" << lengthX << "                            *" << endl;
	cout << "* ???????????" << lengthY << "                            *" << endl;

	cout << "* ???б???????????????                        *" << endl;

	cout << "* ??????????????                            *" << endl;
	cout << "* ??????????γ?????                            *" << endl;
	cout << "* ?????????????????????                        *" << endl;
}

void printGestureCount()
{
	cout << "* ??????????????????????";
	if (gestureCount < 10)
	{
		cout << gestureCount++ << "                   *" << endl;
	}
	else
	{
		cout << gestureCount++ << "                  *" << endl;
	}
}

//????????
void printResult()
{
	ifstream infile;
	string s;

	//infile.open("data\\point\\outPoint.txt");
	infile.open("/data/data/com.example.xmatenotes/files/outpoint.txt");
	getline(infile, s);
	LOGE("outpoint.txt文件内容为%s", s.c_str());
	infile.close();
//	cout << "*************手势指令相关信息如下****************" << endl;
	LOGE("*************手势指令相关信息如下****************");
//	cout << "* ??????????????????";
	LOGE("* 【手势指令识别结果】：");

	if (s.size() <= 0)
	{
//		cout << "δ???" << endl;
		LOGE("未识别");
        returnResult(6);
	}
	else
	{
		int result = 0;
		for (int i = 0; i < s.size(); ++i)
		{
			result = result * 10 + (s[i] - '0');
		}
		label = result;
		if (result == 0 && bihua == 1)
		{
//			cout << "???????              *" << endl;
			LOGE("指令控制符");
            returnResult(4);
			printGestureCount();
			printControlCharac();
		}
		else if (result == 1 && bihua == 1)
		{
//			cout << "??" << "                      *" << endl;
			LOGE("对");
            returnResult(5);
			printGestureCount();
		}
		else if (result == 2 && bihua == 2)
		{
//			cout << "???1" << "                      *" << endl;
			LOGE("半对1");
            returnResult(6);
			printControlCharac();
		}
		else if (result == 3 && bihua == 3)
		{
//			cout << "???2" << "                      *" << endl;
			LOGE("半对2");
            returnResult(7);
			printControlCharac();
		}
		else if (result == 4 && bihua == 4)
		{
//			cout << "???3" << "                      *" << endl;
			LOGE("半对3");
            returnResult(8);
			printControlCharac();
		}
		else if (result == 5 && bihua == 2)
		{
//			cout << "??" << "                      *" << endl;
			LOGE("错");
            returnResult(9);
			printControlCharac();
		}
		else
		{
//			cout << "δ???" << endl;
			LOGE("未识别");
            returnResult(10);
		}
	}
	printCommonCharac();
	DistinguishStandard();
	cout << "***********************end***********************" << endl << endl;
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

//???????
void ClearData()
{
	res.clear();
	write_start_time = 0;
	write_end_time = 0;
	bihua = 0;
	LOGE("ClearData()");
}

int printaaa(){
    return 1;
}

//???????
void recognize()
{
	if (bihua > 5)
	{
		cout << "?????????δ????" << endl;
		cout << "*************************end********************" << endl << endl;
	}
	else
	{
		writeToPoint();
		calCharacter();
		PredictGesture();
		printResult();
	}
	cout << "??????????????????????????????????????2??????\n";
}

void ThreadFunc()
{
//	time_end = GetTickCount();
//	double temp_time = time_start;
//	while (time_end - time_start < 1000)
//	{
//		if (time_start - temp_time > 0.0)return;
//		time_end = GetTickCount();
//
//	}
	LOGE("ThreadFunc()");
	recognize();
}

void tt()
{
	thread t1(ThreadFunc);
	t1.detach();
}

//?????????????
void MergeTrainFile(char* targetFile, const string& sourceFile1, const string& sourceFile2)
{
	ifstream infile;
	ofstream outfile;
	remove(targetFile);
	infile.open(sourceFile1);
	outfile.open(targetFile, ios::app);
	string s;
	while (getline(infile, s))
	{
		sampleCount++;
		outfile << s << endl;
	}
	infile.close();

	infile.open(sourceFile2);
	while (getline(infile, s))
	{
		sampleCount++;
		outfile << s << endl;
	}
	infile.close();
	outfile.close();
}

//????????????
void printTrainCount()
{
	if (sampleCount < 10)
	{
		cout << "* ????????????" << sampleCount << "                                *" << endl;
		cout << "* ???????????????" << sampleCount - TotalWrong << "                         *" << endl;
		cout << "* ????????????????" << TotalWrong << "                           *" << endl;
		cout << "* ????????????       ?????????          *" << endl;
	}
	else if (sampleCount < 100)
	{
		cout << "* ????????????" << sampleCount << "                               *" << endl;
		cout << "* ???????????????" << sampleCount - TotalWrong << "                         *" << endl;
		cout << "* ????????????????" << TotalWrong << "                           *" << endl;
		cout << "* ????????????       ?????????          *" << endl;
	}
	else if (sampleCount < 1000)
	{
		cout << "* ????????????" << sampleCount << "                              *" << endl;
		cout << "* ???????????????" << sampleCount - TotalWrong << "                          *" << endl;
		cout << "* ????????????????" << TotalWrong << "                           *" << endl;
		cout << "* ????????????       ?????????          *" << endl;
	}
	else
	{
		cout << "* ????????????" << sampleCount << "                             *" << endl;
		cout << "* ???????????????" << sampleCount - TotalWrong << "                          *" << endl;
		cout << "* ????????????????" << TotalWrong << "                           *" << endl;
		cout << "* ????????????       ?????????          *" << endl;
	}
}
//???????????????????
void CalTrainDataCharac(char* targetFile, const string& sourceFile)
{
	ifstream infile;
	ofstream outfile;
	remove(targetFile);
	infile.open(sourceFile);
	outfile.open(targetFile, ios::app);
	string s;
	while (getline(infile, s))
	{
		ComputeCharacter cc(s);
		cc.Compute();
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
		outfile << "11:" << cc.InflectionPointsNumber;
		outfile << endl;
	}
	infile.close();
	outfile.close();
}

//??????
void trainModel()
{
	cout << "********************??????********************" << endl;
	sampleCount = 0;
	//MergeTrainFile("data\\temp\\tempStroke.txt", "data\\collectData\\collectDataStroke.txt", "data\\train\\trainStroke.txt");
	MergeTrainFile("/data/data/com.example.xmatenotes/files/tempstroke.txt", "/data/data/com.example.xmatenotes/files/collectdatastroke.txt", "/data/data/com.example.xmatenotes/files/trainstroke.txt");
	cout << "* ???????????????                             *" << endl;
	//CalTrainDataCharac("data\\character\\characterStroke.txt", "data\\temp\\tempStroke.txt");
	CalTrainDataCharac("/data/data/com.example.xmatenotes/files/characterstroke.txt", "/data/data/com.example.xmatenotes/files/tempstroke.txt");
	cout << "* ??????????????                             *" << endl;
	int paramNumberPredict = 7;
	char *tmp[7] = { "" };
	tmp[1] = { "-s" };
	tmp[2] = { "0" };
	tmp[3] = { "-t" };
	tmp[4] = { "0" };
	//tmp[5] = { "data\\character\\characterStroke.txt" };
	tmp[5] = { "/data/data/com.example.xmatenotes/files/characterstroke.txt" };
	//tmp[6] = { "data\\model\\modelStroke.txt" };
	tmp[6] = { "/data/data/com.example.xmatenotes/files/modelstroke.txt" };
	cout << "* ???????????????????                   *" << endl;
	svmTrain(paramNumberPredict, tmp);
	cout << "* ?????????????                             *" << endl;
	cout << "* ????????????????6 (?????????????????1?????2?????3)  *" << endl;
	printTrainCount();
	cout << "*********************end************************" << endl << endl;;
}

//????????????
//?????date:2020/12/31
void StrToDate(const string &s, string &year, string &month, string &day)
{
	int index = 0;
	while (s[index] != ':')
	{
		index++;
	}
	index++;
	while (s[index] != '/')
	{
		year += s[index++];
	}
	index++;
	while (s[index] != '/')
	{
		month += s[index++];
	}
	index++;
	while (index < s.size())
	{
		day += s[index++];
	}
}

//???????????????
//?????T:XX F:XXX
void StrToTodayResult(const string &s)
{
	int index = 0;
	string tmp;
	while (s[index] != ':')
	{
		++index;
	}
	index++;

	while (s[index] != ' ')
	{
		tmp += s[index++];
	}
	correctToday = atoi(tmp.c_str());
	tmp.clear();

	while (s[index] != ':')
	{
		index++;
	}
	index++;

	while (index < s.size())
	{
		tmp += s[index++];
	}
	wrongToday = atoi(tmp.c_str());

	NumberToday = correctToday + wrongToday;
}

//??????????????
void StrToTotalResult(const string &s)
{
	int index = 0;
	string tmp;
	while (s[index] != ':')
	{
		++index;
	}
	index++;

	while (s[index] != ' ')
	{
		tmp += s[index++];
	}
	TotalCorrect = atoi(tmp.c_str());
	tmp.clear();

	while (s[index] != ':')
	{
		index++;
	}
	index++;

	while (index < s.size())
	{
		tmp += s[index++];
	}
	TotalWrong = atoi(tmp.c_str());

	TotalNumber = TotalCorrect + TotalWrong;
}
//??????ü??
void LoadUsageRecord()
{
	ifstream infile;

	//infile.open("data\\StatisticalResults\\StatisticalResults.txt");
	infile.open("/data/data/com.example.xmatenotes/files/statisticalresults.txt");
	string s, year, month, day;
	getline(infile, s);
	StrToDate(s, year, month, day);

	getline(infile, s);
	StrToTotalResult(s);

	getline(infile, s);
	StrToTodayResult(s);

	infile.close();

	//infile.open("data\\train\\trainStroke12.txt");
	infile.open("/data/data/com.example.xmatenotes/files/trainstroke12.txt");
	while (getline(infile, s))
	{
		sampleCount++;
	}
	infile.close();

	//infile.open("data\\train\\trainStroke3.txt");
	infile.open("/data/data/com.example.xmatenotes/files/trainstroke3.txt");
	while (getline(infile, s))
	{
		sampleCount++;
	}
	infile.close();
}

//?????????
void UpdateUsageRecord()
{
	//remove("data\\StatisticalResults\\StatisticalResults.txt");
	remove("/data/data/com.example.xmatenotes/files/statisticalresults.txt");
	ofstream outfile;

	//outfile.open("data\\StatisticalResults\\StatisticalResults.txt", ios::app);
	outfile.open("/data/data/com.example.xmatenotes/files/statisticalresults.txt", ios::app);
	outfile << "Total T:" << TotalCorrect << " " << "F:" << TotalWrong << endl;
	outfile << "Today T:" << correctToday << " " << "F:" << wrongToday << endl;
	outfile.close();
}

//????????????
void MergerExternalDataFile(const string &sourceDataFile12, const string &sourceDataFile3, const string &sourceStatisticalFile)
{
	cout << "**********************************" << endl;
	cout << "*?????????????                *" << endl;

	ifstream infile;
	string s;
	ofstream outfile;
	infile.open("/data/data/com.example.xmatenotes/files/" + sourceDataFile12);
	outfile.open("/data/data/com.example.xmatenotes/files/collectDataStroke12.txt", ios::app);

	while (getline(infile, s))
	{
		outfile << s << endl;
		//cout << s << endl;
	}
	infile.close();
	outfile.close();

	infile.open("/data/data/com.example.xmatenotes/files/" + sourceDataFile3);
	outfile.open("/data/data/com.example.xmatenotes/files/collectDataStroke3.txt", ios::app);

	while (getline(infile, s))
	{

		outfile << s << endl;
	}
	infile.close();
	outfile.close();

	infile.open("/data/data/com.example.xmatenotes/files/" + sourceStatisticalFile);

	getline(infile, s);

	getline(infile, s);

	int externalCorrect = 0;
	int externalWrong = 0;
	int index = 0;
	string tmp;
	while (s[index] != ':')
	{
		++index;
	}
	index++;

	while (s[index] != ' ')
	{
		tmp += s[index++];
	}
	externalCorrect = atoi(tmp.c_str());
	tmp.clear();

	while (s[index] != ':')
	{
		index++;
	}
	index++;

	while (index < s.size())
	{
		tmp += s[index++];
	}
	externalWrong = atoi(tmp.c_str());

	TotalCorrect += externalCorrect;
	TotalWrong += externalWrong;
	TotalNumber = TotalCorrect + TotalWrong;

	cout << "*?????????????                *" << endl;
	//MergeTrainFile();
	cout << "*???????????????????" << sampleCount << "        *" << endl;
	cout << "*???????????????????" << sampleCount - (externalCorrect + externalWrong) << "        *" << endl;
	cout << "*????????????????????" << (externalCorrect + externalWrong) << "       *" << endl;
	cout << "**********************************" << endl << endl;
	infile.close();
}


//????????????????
void SeparateDataByStroke()
{
	ifstream infile;
	//infile.open("data\\collectData.txt");
	infile.open("/data/data/com.example.xmatenotes/files/collectdata.txt");
	string s;

	while (getline(infile, s))
	{
		ComputeCharacter cc(s);
		cc.Compute();
		cc.InrcStroketCount();
	}

}