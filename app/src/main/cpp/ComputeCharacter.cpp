#include"ComputeCharacter.h"

//构造函数
ComputeCharacter::ComputeCharacter(const string str)
{
	des = str;
}
//字符串转标签和数字坐标
void ComputeCharacter::StrToCoor(const string &s)
{
	int n = s.size();
	string tmp;
	vector<int>coor;
	int index = 0;
	int k = 1;

	for (;index < n;++index)
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	tag = atoi(tmp.c_str());
	tmp.clear();

	index++;
	for (;index < n;++index)
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	strokeCount = atoi(tmp.c_str());
	tmp.clear();

	for (int i = index + 1;i < n;++i)
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
				points.push_back(coor);
				coor.clear();
			}
			k++;
			tmp.clear();
		}
	}
}

//计算坐标点的数量
void ComputeCharacter::CalCount()
{
	pointsNumber = points.size();
}

//计算x,y的最大值和最小值
void ComputeCharacter::GetXY()
{
	if (pointsNumber <= 0)return;
	minX = maxX = points[0][0];
	minY = maxY = points[0][1];

	for (int i = 1; i < pointsNumber;++i)
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
	for (int i = 1;i < pointsNumber;++i)
	{
		tmp = sqrt((double)(points[i][0] - points[i - 1][0])*(points[i][0] - points[i - 1][0]) + (double)(points[i][1] - points[i - 1][1])*(points[i][1] - points[i - 1][1]));
		distance.push_back(tmp);
	}
}

//计算笔迹长度
void ComputeCharacter::CalHandWritingLength()
{
	for (int i = 0;i < distance.size();++i)
	{
		HandWritingLength += distance[i];
	}
}

//计算形心
void ComputeCharacter::CalCentroid()
{
	long long sumX = 0;
	long long sumY = 0;
	for (int i = 0;i < pointsNumber;++i)
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

//计算紧密度
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
	int step = pointsNumber / 10;
	for (int i = 0;i < pointsNumber - 2*step;i += step)
	{
		double a = sqrt((double)(points[i][0] - points[i + step][0])*(points[i][0] - points[i + step][0]) + (double)(points[i][1] - points[i + step][1])*(points[i][1] - points[i + step][1]));
		double b = sqrt((double)(points[i + 2*step][0] - points[i + step][0])*(points[i + 2*step][0] - points[i + step][0]) + (double)(points[i + 2*step][1] - points[i + step][1])*(points[i + 2*step][1] - points[i + step][1]));;
		double c = sqrt((double)(points[i][0] - points[i + 2*step][0])*(points[i][0] - points[i + 2*step][0]) + (double)(points[i][1] - points[i + 2*step][1])*(points[i][1] - points[i + 2*step][1]));
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
	for (int i = 0;i < angle.size();++i)
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
	StrToCoor(des);
	//计算坐标点的数量
	CalCount();
	//计算x,y的最值
	GetXY();
	//计算边界长度
	CalBoardLength();
	//计算边界围成的面积
	CalBoardSquare();
	//计算相邻点之间的距离
	CalDistanceOfTwoPoint();
	//计算笔迹长度
	CalHandWritingLength();
	//计算质心
	CalCentroid();
	//计算边界比例
	CalXYRatio();
	//计算紧密性
	CalCompactness();
	//计算封闭性
	CalCloseness();
	//计算三个连续点的夹角
	CalAngle();
	//计算曲率
	CalCurvature();
	//计算初始方向
	CalBeginDirection();
	//计算终止方向
	CalEndDirection();
	//计算偏移量
	CalOffset();
	//初始点&终止点到x、y最大值距离与最大边框的比值
	RatioOfESPoint();
	//计算拐点的个数
	CalInflectionPointsNumber();
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

//计算拐点的个数
void ComputeCharacter::CalInflectionPointsNumber()
{
	double slope = 0;
	int step = pointsNumber / 10;
	for (int i = step;i < pointsNumber - 1;i += step)
	{
		if (points[i][0] - points[i + 1][0] != 0)
		{
			if (slope == 0)
			{
				slope = (double)(points[i][1] - points[i + 1][1]) / (double)(points[i][0] - points[i + 1][0]);
			}
			else
			{
				double tmp = (double)(points[i][1] - points[i + 1][1]) / (double)(points[i][0] - points[i + 1][0]);
				if (slope*tmp < 0)
				{
					InflectionPointsNumber++;
					slope = tmp;
				}
			}
		}
	}
}

void ComputeCharacter::InrcStroketCount()
{
	ofstream outfile;
	strokeCount++;
	if (strokeCount == 3)
	{
		outfile.open("/data/data/com.example.xmatenotes/files/trainStroke3.txt", ios::app);
	}
	else
	{
		outfile.open("/data/data/com.example.xmatenotes/files/trainStroke12.txt", ios::app);
	}
	outfile << tag << " ";
	outfile << strokeCount << " ";
	for (int i = 0;i < pointsNumber;++i)
	{
		outfile << points[i][0] << " " << points[i][1] << " ";
	}
	outfile << endl;
}