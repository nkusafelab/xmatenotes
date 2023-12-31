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
	string des; //坐标点轨迹坐标字符串
	vector<vector<int>> sourcePoints;//存放构成每个数字的所有原始坐标点集合
	vector<vector<int>> points;//存放构成每个数字的经过预处理的坐标点集合
	vector<vector<vector<int>>> pointsStroke; //分笔画坐标点
	vector<vector<int>> firstStrokePoints;
	vector<double> distance;//存放相邻点之间的距离
	int tag;//存放标签
	int strokeCount;//笔划数
	int pointsNumber;//坐标点的数量
	int minX, minY, maxX, maxY;//x,y最大最小值
	int xLength, yLength;//边框的长度
	int square;//边框构成的面积
	double HandWritingLength;//笔迹长度
	double CentroidX, CentroidY;//形心坐标
	double XYRatio;//边界比例
	double Compactness;//紧密性
	double closeness;//封闭性
	double pointDensity;//点密度
	vector<double> angle;//三个连续点的夹角

	double curvature;//曲率
	double beginDirection;//初始方向
	double endDirection;//终止方向
	double XstartPointMinOffset;//初始点到边界框最小值偏移量x
	double XstartPointMaxOffset;//初始点到边界框最大值偏移量x
	double YstartPointMinOffset;//初始点到边界框最小值偏移量y
	double YstartPointMaxOffset;//初始点到边界框最大值偏移量y
	double XendPointMinOffset;//初始点到边界框最小值偏移量x
	double YendPointMinOffset;//初始点到边界框最小值偏移量y
	double XendPointMaxOffset;//初始点到边界框最大值偏移量x
	double YendPointMaxOffset;//初始点到边界框最大值偏移量y

	//2020/9/21
	//wsk
	double xStartMaxLengthRatio, yStartMaxLengthRatio;//初始点到x、y最大值距离与最大边框的比值
	double xEndMaxLengthRatio, yEndMaxLengthRatio;//终止点到x、y最大值距离与最大边框的比值
	double xStartMinLengthRatio, yStartMinLengthRatio;//初始点到x、y最大值距离与最大边框的比值
	int InflectionPointsNumber = 0;//拐点(斜率突变-变号)

	//构造函数
	ComputeCharacter(const string str);
	//中值滤波
	void medianFilter(const vector<vector<int>> source);
	//string转int
	void StrToCoor(const string &s);
	//计算坐标点的数量
	void CalCount();
	//将坐标点数组拆分为分笔画坐标点数组
	void SplitStroke();
	//计算x,y的最值
	void GetXY();
	//计算边界长度
	void CalBoardLength();
	//计算边界围成的面积
	void CalBoardSquare();
	//计算相邻点之间的距离
	void CalDistanceOfTwoPoint();
	//计算笔迹长度
	void CalHandWritingLength();
	//计算质心
	void CalCentroid();
	//计算边界比例
	void CalXYRatio();
	//计算紧密性
	void CalCompactness();
	//计算封闭性
	void CalCloseness();
	//计算三个连续点的夹角
	void CalAngle();
	//计算曲率
	void CalCurvature();
	//计算初始方向
	void CalBeginDirection();
	//计算终止方向
	void CalEndDirection();
	//计算偏移量
	void CalOffset();

	//author：wsk
	//time：2020/9/21
	//初始点&终止点到x、y最大值距离与最大边框的比值
	void RatioOfESPoint();

	//计算单笔画拐点个数
	int CalStrokeInflectionPointsNumber(const vector<vector<int>> tmp);
	//计算拐点的个数
	void CalInflectionPointsNumber();

	void InrcStroketCount();
	//计算函数
	void Compute();
	//调试函数
	void debug();
};

#pragma once
