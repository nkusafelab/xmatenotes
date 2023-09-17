#pragma once
#include <string>
#include "inc/common.h"
#include <vector>
#include"ComputeCharacter.h"
using namespace std;

extern vector<PEN_INFO> res;
//extern int nRet;
//extern int NumberToday, correctToday, wrongToday;
//extern int TotalNumber, TotalCorrect, TotalWrong;
//extern double AccuracyToday, TotalAccuracy;

//extern double time_start, time_end;
//extern double write_start_time, write_end_time;
extern double CenterX, CenterY;
extern int lengthX, lengthY;

extern int count1;
extern int bihua;
extern char tag;
extern int gestureCount;
extern int sampleCount;
extern int inflectionPointsNumber; //�յ�����

extern "C" {
void onDataPacket(const PEN_INFO &penInfo, bool isLast, int count);

void encapsulation(unsigned char status, unsigned short nX, unsigned short nY, unsigned short nPress, int count);

void returnResult(int i);
}

//������Ϣд��point.txt
void writeToPoint();

//����ʵʱ��������
void calCharacter();

//����ģ��ʶ������
void PredictGesture();

//��ȡԤ����
void printResult();

//�������
void ClearData();

//ʶ������
void recognize();
