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

//ʶ���������ȷ��������
bool IsCorrectGestureName(const string &gestureName);

//��ʾ��ȷ��
void PrintStatisticalResults();

//�洢�ɼ�������
void SaveCollectData();

//�ɼ�����
void collectionData();

//������Ϣд��point.txt
void writeToPoint();

//����ʵʱ��������
void calCharacter();

// ʶ����жϱ�׼
void DistinguishStandard();

//����ģ��ʶ������
void PredictGesture();

//����ѹ����ֵ
long calPressAvg();

//�ж�ָ����Ʒ���������
void judgeControlDirection();

//�����������
void printCommonCharac();

//���ָ����Ʒ���������
void printControlCharac();

void printGestureCount();

//��ȡԤ����
void printResult();

//�������
void ClearData();

//ʶ������
void recognize();

void ThreadFunc();

void tt();

//�ϲ�ѵ�������ļ�
void MergeTrainFile(char *targetFile, const string &sourceFile1, const string &sourceFile2);

//���ѵ��������
void printTrainCount();

//����ѵ��ģ���õ�������
void CalTrainDataCharac(char *targetFile, const string &sourceFile);

//ѵ��ģ��
void trainModel();

//�ַ���ת������
//��ʽ��date:2020/12/31
void StrToDate(const string &s, string &year, string &month, string &day);

//�ַ���ת����ͳ�ƽ��
//��ʽ��T:XX F:XXX
void StrToTodayResult(const string &s);

//�ַ���תȫ��ͳ�ƽ��
void StrToTotalResult(const string &s);

//����ʹ�ü�¼
void LoadUsageRecord();

//����ͳ�ƽ��
void UpdateUsageRecord();

//�ϲ��ⲿ�����ļ�
void MergerExternalDataFile(const string &sourceDataFile12, const string &sourceDataFile3,
                            const string &sourceStatisticalFile);

//���벻ͬ�ʻ���������
void SeparateDataByStroke();


