#pragma once
#include<iostream>
#include<vector>
#include<algorithm>
#include<fstream>
using namespace std;
extern vector<string> gesture;

extern vector<vector<int>> stokeCountForGesture;
extern int numOfNotSplit;
extern int globalStrokeCount;
extern bool is_train;
extern bool scale;

class ComputeCharacter
{
public:
	string des; //�����켣�����ַ���
	vector<vector<int>> sourcePoints;//��Ź���ÿ�����ֵ�����ԭʼ����㼯��
	vector<vector<int>> points;//��Ź���ÿ�����ֵľ���Ԥ���������㼯��
	vector<vector<vector<int>>> pointsStroke; //�ֱʻ������
	vector<vector<int>> firstStrokePoints;
	vector<double> distance;//������ڵ�֮��ľ���
	int tag;//��ű�ǩ
	int strokeCount;//�ʻ���
	int pointsNumber;//����������
	int minX, minY, maxX, maxY;//x,y�����Сֵ
	int xLength, yLength;//�߿�ĳ���
	int square;//�߿򹹳ɵ����
	double HandWritingLength;//�ʼ�����
	double CentroidX, CentroidY;//��������
	double XYRatio;//�߽����
	double Compactness;//������
	double closeness;//�����
	double pointDensity;//���ܶ�
	vector<double> angle;//����������ļн�

	double curvature;//����
	double beginDirection;//��ʼ����
	double endDirection;//��ֹ����
	double XstartPointMinOffset;//��ʼ�㵽�߽����Сֵƫ����x
	double XstartPointMaxOffset;//��ʼ�㵽�߽�����ֵƫ����x
	double YstartPointMinOffset;//��ʼ�㵽�߽����Сֵƫ����y
	double YstartPointMaxOffset;//��ʼ�㵽�߽�����ֵƫ����y
	double XendPointMinOffset;//��ʼ�㵽�߽����Сֵƫ����x
	double YendPointMinOffset;//��ʼ�㵽�߽����Сֵƫ����y
	double XendPointMaxOffset;//��ʼ�㵽�߽�����ֵƫ����x
	double YendPointMaxOffset;//��ʼ�㵽�߽�����ֵƫ����y

	//2020/9/21
	//wsk
	double xStartMaxLengthRatio, yStartMaxLengthRatio;//��ʼ�㵽x��y���ֵ���������߿�ı�ֵ
	double xEndMaxLengthRatio, yEndMaxLengthRatio;//��ֹ�㵽x��y���ֵ���������߿�ı�ֵ
	double xStartMinLengthRatio, yStartMinLengthRatio;//��ʼ�㵽x��y���ֵ���������߿�ı�ֵ
	int InflectionPointsNumber = 0;//�յ�(б��ͻ��-���)

	//���캯��
	ComputeCharacter(const string str);
	//��ֵ�˲�
	void medianFilter(const vector<vector<int>> source);
	//stringתint
	void StrToCoor(const string &s);
	//��������������
	void CalCount();
	//�������������Ϊ�ֱʻ����������
	void SplitStroke();
	//����x,y����ֵ
	void GetXY();
	//����߽糤��
	void CalBoardLength();
	//����߽�Χ�ɵ����
	void CalBoardSquare();
	//�������ڵ�֮��ľ���
	void CalDistanceOfTwoPoint();
	//����ʼ�����
	void CalHandWritingLength();
	//��������
	void CalCentroid();
	//����߽����
	void CalXYRatio();
	//���������
	void CalCompactness();
	//��������
	void CalCloseness();
	//��������������ļн�
	void CalAngle();
	//��������
	void CalCurvature();
	//�����ʼ����
	void CalBeginDirection();
	//������ֹ����
	void CalEndDirection();
	//����ƫ����
	void CalOffset();

	//author��wsk
	//time��2020/9/21
	//��ʼ��&��ֹ�㵽x��y���ֵ���������߿�ı�ֵ
	void RatioOfESPoint();

	//���㵥�ʻ��յ����
	int CalStrokeInflectionPointsNumber(const vector<vector<int>> tmp);
	//����յ�ĸ���
	void CalInflectionPointsNumber();

	void InrcStroketCount();
	//���㺯��
	void Compute();
	//���Ժ���
	void debug();
};

#pragma once
