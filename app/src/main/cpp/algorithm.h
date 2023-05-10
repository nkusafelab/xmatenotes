#pragma once
#include <string>
#include "inc/common.h"
#include <vector>
#include"ComputeCharacter.h"
using namespace std;

extern vector<PEN_INFO> res;
extern int nRet;
extern int NumberToday, correctToday, wrongToday;
extern int TotalNumber, TotalCorrect, TotalWrong;
extern double AccuracyToday, TotalAccuracy;

extern double time_start, time_end;
extern double write_start_time, write_end_time;
extern double CenterX, CenterY;
extern int lengthX, lengthY;

extern int count1;
extern int bihua;
extern int tag;
extern int label;
extern int gestureCount;
extern int sampleCount;
extern bool judgeRecognizeResult;

extern "C" {
void onDataPacket(const PEN_INFO &penInfo);

void encapsulation(unsigned char status, unsigned short nX, unsigned short nY, unsigned short nPress);

void returnResult(int i);
}

//识别输入的正确手势名称
bool IsCorrectGestureName(const string &gestureName);

//显示正确率
void PrintStatisticalResults();

//存储采集的数据
void SaveCollectData();

//采集数据
void collectionData();

//将点信息写入point.txt
void writeToPoint();

//计算实时数据特征
void calCharacter();

// 识别的判断标准
void DistinguishStandard();

//调用模型识别手势
void PredictGesture();

//计算压力均值
long calPressAvg();

//判断指令控制符正序逆序
void judgeControlDirection();

//输出共有特征
void printCommonCharac();

//输出指令控制符独有特征
void printControlCharac();

void printGestureCount();

//获取预测结果
void printResult();

//清空数据
void ClearData();

//识别手势
void recognize();

void ThreadFunc();

void tt();

//合并训练数据文件
void MergeTrainFile(char *targetFile, const string &sourceFile1, const string &sourceFile2);

//输出训练样本数
void printTrainCount();

//计算训练模型用到的特征
void CalTrainDataCharac(char *targetFile, const string &sourceFile);

//训练模型
void trainModel();

//字符串转年月日
//格式：date:2020/12/31
void StrToDate(const string &s, string &year, string &month, string &day);

//字符串转今日统计结果
//格式：T:XX F:XXX
void StrToTodayResult(const string &s);

//字符串转全部统计结果
void StrToTotalResult(const string &s);

//加载使用记录
void LoadUsageRecord();

//更新统计结果
void UpdateUsageRecord();

//合并外部数据文件
void MergerExternalDataFile(const string &sourceDataFile12, const string &sourceDataFile3,
                            const string &sourceStatisticalFile);

//分离不同笔划数的数据
void SeparateDataByStroke();


