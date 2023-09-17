#include"ComputeCharacter.h"
#include <numeric>

#include <android/log.h>
#define LOG_TAG "gestureRecognition2"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

vector<string> gesture = {
		"指令控制符",
		"对",
		"半对",
		"半半对",
		"半半半对",
		"错",
		"问号",
		"半问号",
		"半半问号",
		"半半半问号",
		"叹号",
		"半叹号",
		"半半叹号",
		"半半半叹号"
};

vector<vector<int>> stokeCountForGesture = {
		{ 0, 1 },
		{ 2, 5, 6, 10 },
		{ 3, 7, 11 },
		{ 4, 8, 12 },
		{ 9, 13 }
};
int numOfNotSplit = 0;
int globalStrokeCount = 0;
bool is_train = false;
bool scale = false;

//构造函数
ComputeCharacter::ComputeCharacter(const string str)
{
	des = str;
}

//中值滤波
void ComputeCharacter::medianFilter(const vector<vector<int>> source)
{
	int size = source.size();
	vector<int> x, y;
	int num = 2;

	for (int i = 0; i < size; i++) {
		for (int j = i - num; j < i + num; j++) {
			if (j >= 0 && j < size) {
				x.push_back(source[j][0]);
				y.push_back(source[j][1]);
			}
		}
		sort(x.begin(), x.end());
		sort(y.begin(), y.end());
		int medium = x.size() / 2;
		firstStrokePoints.push_back({ x[medium], y[medium] });
		//int xMedium = 0;
		//int yMedium = 0;
		//for (int j = 0; j < x.size(); j++) {
		//	xMedium += x[j];
		//	yMedium += y[j];
		//}
		//xMedium /= x.size();
		//yMedium /= x.size();
		//points.push_back({ xMedium, yMedium});
	}
}
// 定义移动窗口的大小
const int WINDOW_SIZE = 5;

// 定义坐标去重的窗口阈值
const int THRESHOLD = 25;

// 判断两个点是否相似
bool isSimilar(vector<int> p1, vector<int> p2) {
	int dx = p1[0] - p2[0];
	int dy = p1[1] - p2[1];
	return dx * dx + dy * dy < THRESHOLD * THRESHOLD;
}

// 对所有点进行坐标去重
void removeDuplicates(vector<vector<int>>& points) {
	vector<vector<int>> uniquePoints;
	for (int i = 0; i < points.size(); i++) {
		bool isDuplicate = false;
		for (int j = 0; j < uniquePoints.size(); j++) {
			if (isSimilar(points[i], uniquePoints[j])) {
				isDuplicate = true;
				break;
			}
		}
		if (!isDuplicate) {
			uniquePoints.push_back(points[i]);
		}
	}
	points = uniquePoints;
}

// 对所有点进行移动窗口平均化
void smoothPoints(vector<vector<int>>& points) {
	vector<vector<int>> smoothedPoints;
	for (int i = 0; i < points.size(); i++) {
		int sumX = 0;
		int sumY = 0;
		int count = 0;
		for (int j = max(i - WINDOW_SIZE, 0); j <= min(i + WINDOW_SIZE, (int)points.size() - 1); j++) {
			sumX += points[j][0];
			sumY += points[j][1];
			count++;
		}
		vector<int> smoothedPoint = { sumX / count, sumY / count };
		smoothedPoints.push_back(smoothedPoint);
	}
	points = smoothedPoints;
}


//字符串转标签和数字坐标
void ComputeCharacter::StrToCoor(const string &s)
{
	int n = s.size();
	string tmp;
	vector<int>coor;
	int index = 0;
	int k = 1;

	for (; index < n; ++index) //获取数字标签
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	tag = tmp[0] - 'a';
	tmp.clear();

	index++;
	for (; index < n; ++index) //获取笔画数
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	strokeCount = atoi(tmp.c_str());
	tmp.clear();

	for (int i = index + 1; i < n; ++i) //获取笔画信息，遇空格则表明是新的一个坐标点横纵坐标，若模2为0，则表明横纵坐标均已记录，便可以加入坐标点集
	{
		if (s[i] != ' ')
		{
			tmp += s[i];
		}
		else
		{
			coor.push_back(atoi(tmp.c_str()));
			if (k % 2 == 0)
			{
				//sourcePoints.push_back(coor);
				points.push_back(coor);
				coor.clear();
			}
			k++;
			tmp.clear();
		}
	}
	//medianFilter(sourcePoints);
}

//计算坐标点的数量
void ComputeCharacter::CalCount()
{
	pointsNumber = points.size();
}

//将坐标点数组拆分为分笔画坐标点数组
void ComputeCharacter::SplitStroke()
{
	int distance = 0;
	vector<vector<int>> tmp;
	tmp.push_back(points[0]);
	for (int i = 1; i < pointsNumber; i++) {
		distance = sqrt(pow(points[i][0] - points[i - 1][0], 2) + pow(points[i][1] - points[i - 1][1], 2));
		if (distance > 121) { //判定连续两个坐标点距离是否大于121，大于则认为这两个坐标点是两个笔画的分界点，否则则认为是一笔画上连续两点
			pointsStroke.push_back(tmp);
			tmp.clear();
			tmp.push_back(points[i]);
		}
		else {
			tmp.push_back(points[i]);
		}
	}
	if (tmp.size() > 0) pointsStroke.push_back(tmp);
}

//计算x,y的最大值和最小值
void ComputeCharacter::GetXY()
{
	if (pointsNumber <= 0)return;
	minX = maxX = points[0][0];
	minY = maxY = points[0][1];

	for (int i = 1; i < pointsNumber; ++i)
	{
		minX = min(minX, points[i][0]);
		maxX = max(maxX, points[i][0]);
		minY = min(minY, points[i][1]);
		maxY = max(maxY, points[i][1]);
	}
}

//计算边界长度
void ComputeCharacter::CalBoardLength()
{
	xLength = maxX - minX;
	yLength = maxY - minY;
}

//计算边界框的面积
void ComputeCharacter::CalBoardSquare()
{
	square = xLength * yLength;
}

//计算相邻点之间的距离
void ComputeCharacter::CalDistanceOfTwoPoint()
{
	double tmp;
	for (int i = 1; i < pointsNumber; ++i)
	{
		tmp = sqrt((double)(points[i][0] - points[i - 1][0])*(points[i][0] - points[i - 1][0]) + (double)(points[i][1] - points[i - 1][1])*(points[i][1] - points[i - 1][1]));
		distance.push_back(tmp);
	}
}

//计算笔迹长度
void ComputeCharacter::CalHandWritingLength()
{
	for (int i = 0; i < distance.size(); ++i)
	{
		HandWritingLength += distance[i];
	}
}

//计算形心
void ComputeCharacter::CalCentroid()
{
	long long sumX = 0;
	long long sumY = 0;
	for (int i = 0; i < pointsNumber; ++i)
	{
		sumX += points[i][0];
		sumY += points[i][1];
	}

	CentroidX = sumX / pointsNumber;
	CentroidY = sumY / pointsNumber;
}

//计算边框比例
void ComputeCharacter::CalXYRatio()
{
	XYRatio = (double)xLength / (double)yLength;
}

//计算紧密度  ？
void ComputeCharacter::CalCompactness()
{
	Compactness = HandWritingLength * HandWritingLength / square;
}

//计算封闭性
void ComputeCharacter::CalCloseness()
{
	closeness = HandWritingLength / sqrt((points[0][0] - points[pointsNumber - 1][0])*(points[0][0] - points[pointsNumber - 1][0]) + (points[0][1] - points[pointsNumber - 1][1])*(points[0][1] - points[pointsNumber - 1][1]));
}

//计算三个连续点的夹角
void ComputeCharacter::CalAngle()
{
	int step = (pointsNumber / 10 == 0 ? 1 : pointsNumber / 10);
	for (int i = 0; i < pointsNumber - 2 * step; i += step)
	{
		double a = sqrt((double)(points[i][0] - points[i + step][0])*(points[i][0] - points[i + step][0]) + (double)(points[i][1] - points[i + step][1])*(points[i][1] - points[i + step][1]));
		double b = sqrt((double)(points[i + 2 * step][0] - points[i + step][0])*(points[i + 2 * step][0] - points[i + step][0]) + (double)(points[i + 2 * step][1] - points[i + step][1])*(points[i + 2 * step][1] - points[i + step][1]));;
		double c = sqrt((double)(points[i][0] - points[i + 2 * step][0])*(points[i][0] - points[i + 2 * step][0]) + (double)(points[i][1] - points[i + 2 * step][1])*(points[i][1] - points[i + 2 * step][1]));
		if (a + b > c && a + c > b && b + c > a)
		{
			angle.push_back(acos((a*a + b*b - c*c) / (2 * a*b)) * 180 / 3.1415);
		}
	}
}

//void ComputeCharacter::CalAngle()
//{
//	int step = pointsNumber / 10;
//	for (int i = 0;i < pointsNumber - 2;++i)
//	{
//		double a = distance[i];
//		double b = distance[i + 1];
//		double c = sqrt((double)(points[i][0] - points[i + 2][0])*(points[i][0] - points[i + 2][0]) + (double)(points[i][1] - points[i + 2][1])*(points[i][1] - points[i + 2][1]));
//		if (a + b > c && a + c > b && b + c > a)
//		{
//			angle.push_back(acos((a*a + b*b - c*c) / (2 * a*b)) * 180 / 3.1415);
//		}
//	}
//}

//计算曲率
void ComputeCharacter::CalCurvature()
{
	for (int i = 0; i < angle.size(); ++i)
	{
		curvature += angle[i];
	}

	curvature /= angle.size();
	//cout << "【角度/边的数量】：" << curvature / angle.size() << endl << endl;
}

//计算初始方向
void ComputeCharacter::CalBeginDirection()
{
	if (points[1][0] - points[0][0] == 0)
	{
		if (points[2][0] - points[0][0] == 0)
		{
			beginDirection = 0;
		}
		else
		{
			beginDirection = (double)(points[2][1] - points[0][1]) / (double)(points[2][0] - points[0][0]);
		}
	}
	else
	{
		beginDirection = (double)(points[1][1] - points[0][1]) / (double)(points[1][0] - points[0][0]);
	}
}

//计算终止方向
void ComputeCharacter::CalEndDirection()
{
	if (points[pointsNumber - 1][0] - points[pointsNumber - 2][0] == 0)
	{
		if (points[pointsNumber - 1][0] - points[pointsNumber - 3][0] == 0)
		{
			endDirection = 0;
		}
		else
		{
			endDirection = (double)(points[pointsNumber - 1][1] - points[pointsNumber - 3][1]) / (double)(points[pointsNumber - 1][0] - points[pointsNumber - 3][0]);
		}
	}
	else
	{
		endDirection = (double)(points[pointsNumber - 1][1] - points[pointsNumber - 2][1]) / (double)(points[pointsNumber - 1][0] - points[pointsNumber - 2][0]);
	}
}

//计算偏移量
void ComputeCharacter::CalOffset()
{
	//XstartPointMinOffset = (points[0][0] - minX) / xLength;
	//YstartPointMinOffset = (points[0][1] - minY) / xLength;//初始点到边界框最小值偏移量y
	//XstartPointMaxOffset = (points[0][0] - maxX) / xLength;//初始点到边界框最大值偏移量x
	//YstartPointMaxOffset = (points[0][1] - maxY) / xLength;//初始点到边界框最大值偏移量y
	//XendPointMinOffset = (points[pointsNumber - 1][0] - minX) / xLength;//终止点到边界框最小值偏移量x
	//YendPointMinOffset = (points[pointsNumber - 1][1] - minY) / xLength;//终止点到边界框最小值偏移量y
	//XendPointMaxOffset = (points[pointsNumber - 1][0] - maxX) / xLength;//终止点到边界框最大值偏移量x
	//YendPointMaxOffset = (points[pointsNumber - 1][0] - maxY) / xLength;//终止点到边界框最大值偏移量y

	XstartPointMinOffset = (points[0][0] - minX);
	YstartPointMinOffset = (points[0][1] - minY);//初始点到边界框最小值偏移量y
	XstartPointMaxOffset = (points[0][0] - maxX);//初始点到边界框最大值偏移量x
	YstartPointMaxOffset = (points[0][1] - maxY);//初始点到边界框最大值偏移量y
	XendPointMinOffset = (points[pointsNumber - 1][0] - minX);//终止点到边界框最小值偏移量x
	YendPointMinOffset = (points[pointsNumber - 1][1] - minY);//终止点到边界框最小值偏移量y
	XendPointMaxOffset = (points[pointsNumber - 1][0] - maxX);//终止点到边界框最大值偏移量x
	YendPointMaxOffset = (points[pointsNumber - 1][0] - maxY);//终止点到边界框最大值偏移量y
}

//计算函数
void ComputeCharacter::Compute()
{
	//string转int
	LOGE("StrToCoor()");
	StrToCoor(des);
	//计算坐标点的数量
	LOGE("CalCount()");
	CalCount();
	//计算x,y的最值
	LOGE("GetXY()");
	GetXY();
	//计算边界长度
	LOGE("CalBoardLength()");
	CalBoardLength();
	//计算边界围成的面积
	LOGE("CalBoardSquare()");
	CalBoardSquare();
	//计算相邻点之间的距离
	LOGE("CalDistanceOfTwoPoint()");
	CalDistanceOfTwoPoint();
	//计算笔迹长度
	LOGE("CalHandWritingLength()");
	CalHandWritingLength();
	//计算质心
	LOGE("CalCentroid()");
	CalCentroid();
	//计算边界比例
	LOGE("CalXYRatio()");
	CalXYRatio();
	//计算紧密性
	LOGE("CalComPactness()");
	CalCompactness();
	//计算封闭性
	LOGE("CalCloseness()");
	CalCloseness();
	//计算三个连续点的夹角
	LOGE("CalAngle()");
	CalAngle();
	//计算曲率
	LOGE("CalCurvature()");
	CalCurvature();
	//计算初始方向
	LOGE("CalBeginDirection()");
	CalBeginDirection();
	//计算终止方向
	LOGE("CalEndDirection()");
	CalEndDirection();
	//计算偏移量
	LOGE("CalOffset()");
	CalOffset();
	//初始点&终止点到x、y最大值距离与最大边框的比值
	LOGE("RationOfESPoint()");
	RatioOfESPoint();
	//计算拐点的个数
	LOGE("CalInflectionPointsNumber()");
	CalInflectionPointsNumber();
	LOGE("this->InflectionPointsNumber = %d", this->InflectionPointsNumber);
	//计算点密度
	pointDensity = double(1.0 * pointsNumber) / double(square);
	//cout << tag << " " << pointsNumber << " " << square << " " << pointDensity << endl;
}

//调试函数
void ComputeCharacter::debug()
{
	cout << "标签：" << tag << endl;
	cout << "坐标对的数量：" << pointsNumber << endl;
	cout << "X的最大值:" << maxX << endl;
	cout << "X的最小值:" << minX << endl;
	cout << "Y的最大值:" << maxY << endl;
	cout << "Y的最小值:" << minY << endl;
	cout << "边框长度X：" << xLength << endl;
	cout << "边框长度Y：" << yLength << endl;
	cout << "边框构成的面积：" << square << endl;
	cout << "笔迹长度：" << HandWritingLength << endl;
	cout << "形心坐标X：" << CentroidX << endl;
	cout << "形心坐标Y：" << CentroidY << endl;
	cout << "边界比例：" << XYRatio << endl;
	cout << "紧密性：" << Compactness << endl;
	cout << "封闭性：" << closeness << endl;
	cout << "曲率：" << curvature << endl;
	cout << "初始方向：" << beginDirection << endl;
	cout << "终止方向：" << endDirection << endl;
	cout << "**************end***************" << endl;
}

//初始点&终止点到x、y最大值距离与最大边框的比值
void ComputeCharacter::RatioOfESPoint()
{
	xStartMaxLengthRatio = (double)(points[0][0] - maxX) / ((double)max(xLength, yLength) / (double)1000);
	yStartMaxLengthRatio = (double)(points[0][1] - maxY) / ((double)max(xLength, yLength) / (double)1000);
	xEndMaxLengthRatio = (double)(points[pointsNumber - 1][0] - maxX) / ((double)max(xLength, yLength) / (double)1000);
	yEndMaxLengthRatio = (double)(points[pointsNumber - 1][1] - maxY) / ((double)max(xLength, yLength) / (double)1000);
	xStartMinLengthRatio = (double)(points[0][0] - minX) / ((double)max(xLength, yLength) / (double)1000);;
	yStartMinLengthRatio = (double)(points[0][1] - minY) / ((double)max(xLength, yLength) / (double)1000);
}

//计算单笔画拐点个数
int ComputeCharacter::CalStrokeInflectionPointsNumber(const vector<vector<int>> tmp)
{
	LOGE("8");
	if (tmp.size() < 8) return 0;
	int num = 0;
	double slope = 0;
	int step = 1;
	for (int i = 1; i < tmp.size() - 1; i += step)
	{
		if (tmp[i][0] - tmp[i + 1][0] != 0)
		{
			if (slope == 0)
			{
				slope = (double)(tmp[i][1] - tmp[i + 1][1]) / (double)(tmp[i][0] - tmp[i + 1][0]);
			}
			else
			{
				double tmpSlope = (double)(tmp[i][1] - tmp[i + 1][1]) / (double)(tmp[i][0] - tmp[i + 1][0]);
				if (slope * tmpSlope < 0)
				{
					num++;
//					cout << tmp[i][0] << " " << tmp[i][1] << endl;
					slope = tmpSlope;
				}
			}
		}

	}
	LOGE("9");
    if (num == 0) {
		LOGE("10");
        // 使用最小二乘法来拟合直线
        vector<float> x, y;
        for (int i = 3; i < tmp.size() - 4; i += step) {
            x.push_back(float(tmp[i][0]));
            y.push_back(float(tmp[i][1]));
        }
        int n = x.size();
        float sum_x = accumulate(x.begin(), x.end(), 0.0);
        float sum_y = accumulate(y.begin(), y.end(), 0.0);
        float sum_xy = inner_product(x.begin(), x.end(), y.begin(), 0.0);
        float sum_x2 = inner_product(x.begin(), x.end(), x.begin(), 0.0);
        float k = (n * sum_xy - sum_x * sum_y) / (n * sum_x2 - sum_x * sum_x);
        float b = (sum_y - k * sum_x) / n;
        // 设置浮动范围为直线斜率10%
        float floating_range = abs(k) * 0.71;
        // 如果浮动超过10%，则认为至少有一个拐点
        for (int i = 3; i < tmp.size() - 3; i += step) {
            if (tmp[i][0] - tmp[i + 1][0] != 0) {
                if (abs((double)(tmp[i][1] - tmp[i + 1][1]) / (double)(tmp[i][0] - tmp[i + 1][0]) - k) > floating_range) {
                    num = 1;
                    break;
                }
            }
        }
    }
	LOGE("11");
	return num;
}


//计算拐点的个数
void ComputeCharacter::CalInflectionPointsNumber()
{
	if (strokeCount == 1) {
		LOGE("1");
		/*	medianFilter(points);
		InflectionPointsNumber = CalStrokeInflectionPointsNumber(firstStrokePoints);*/
//		smoothPoints(points);
		this->InflectionPointsNumber = CalStrokeInflectionPointsNumber(points);
		LOGE("2");
	}
	else {
		LOGE("3");
		SplitStroke();//把坐标点数组拆分为单笔画坐标点数组
		if (pointsStroke.size() >= 2) {
			LOGE("4");
//			medianFilter(pointsStroke[0]);
			this->InflectionPointsNumber = CalStrokeInflectionPointsNumber(pointsStroke[0]);
			LOGE("5");
		}
		else {
//			medianFilter(points);
			LOGE("6");
			this->InflectionPointsNumber = CalStrokeInflectionPointsNumber(pointsStroke[0]);
			LOGE("7");
		}
	}
}

void ComputeCharacter::InrcStroketCount()
{
	ofstream outfile;
	strokeCount++;
	if (strokeCount == 3)
	{
		outfile.open("data\\trainStroke3.txt", ios::app);
	}
	else
	{
		outfile.open("data\\trainStroke12.txt", ios::app);
	}
	outfile << tag << " ";
	outfile << strokeCount << " ";
	for (int i = 0; i < pointsNumber; ++i)
	{
		outfile << points[i][0] << " " << points[i][1] << " ";
	}
	outfile << endl;
}