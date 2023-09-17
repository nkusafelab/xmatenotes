#include"ComputeCharacter.h"
#include <numeric>

#include <android/log.h>
#define LOG_TAG "gestureRecognition2"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

vector<string> gesture = {
		"ָ����Ʒ�",
		"��",
		"���",
		"����",
		"�����",
		"��",
		"�ʺ�",
		"���ʺ�",
		"����ʺ�",
		"�����ʺ�",
		"̾��",
		"��̾��",
		"���̾��",
		"����̾��"
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

//���캯��
ComputeCharacter::ComputeCharacter(const string str)
{
	des = str;
}

//��ֵ�˲�
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
// �����ƶ����ڵĴ�С
const int WINDOW_SIZE = 5;

// ��������ȥ�صĴ�����ֵ
const int THRESHOLD = 25;

// �ж��������Ƿ�����
bool isSimilar(vector<int> p1, vector<int> p2) {
	int dx = p1[0] - p2[0];
	int dy = p1[1] - p2[1];
	return dx * dx + dy * dy < THRESHOLD * THRESHOLD;
}

// �����е��������ȥ��
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

// �����е�����ƶ�����ƽ����
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


//�ַ���ת��ǩ����������
void ComputeCharacter::StrToCoor(const string &s)
{
	int n = s.size();
	string tmp;
	vector<int>coor;
	int index = 0;
	int k = 1;

	for (; index < n; ++index) //��ȡ���ֱ�ǩ
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	tag = tmp[0] - 'a';
	tmp.clear();

	index++;
	for (; index < n; ++index) //��ȡ�ʻ���
	{
		if (s[index] == ' ')break;
		tmp += s[index];
	}
	strokeCount = atoi(tmp.c_str());
	tmp.clear();

	for (int i = index + 1; i < n; ++i) //��ȡ�ʻ���Ϣ�����ո���������µ�һ�������������꣬��ģ2Ϊ0�����������������Ѽ�¼������Լ�������㼯
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

//��������������
void ComputeCharacter::CalCount()
{
	pointsNumber = points.size();
}

//�������������Ϊ�ֱʻ����������
void ComputeCharacter::SplitStroke()
{
	int distance = 0;
	vector<vector<int>> tmp;
	tmp.push_back(points[0]);
	for (int i = 1; i < pointsNumber; i++) {
		distance = sqrt(pow(points[i][0] - points[i - 1][0], 2) + pow(points[i][1] - points[i - 1][1], 2));
		if (distance > 121) { //�ж������������������Ƿ����121����������Ϊ������������������ʻ��ķֽ�㣬��������Ϊ��һ�ʻ�����������
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

//����x,y�����ֵ����Сֵ
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

//����߽糤��
void ComputeCharacter::CalBoardLength()
{
	xLength = maxX - minX;
	yLength = maxY - minY;
}

//����߽������
void ComputeCharacter::CalBoardSquare()
{
	square = xLength * yLength;
}

//�������ڵ�֮��ľ���
void ComputeCharacter::CalDistanceOfTwoPoint()
{
	double tmp;
	for (int i = 1; i < pointsNumber; ++i)
	{
		tmp = sqrt((double)(points[i][0] - points[i - 1][0])*(points[i][0] - points[i - 1][0]) + (double)(points[i][1] - points[i - 1][1])*(points[i][1] - points[i - 1][1]));
		distance.push_back(tmp);
	}
}

//����ʼ�����
void ComputeCharacter::CalHandWritingLength()
{
	for (int i = 0; i < distance.size(); ++i)
	{
		HandWritingLength += distance[i];
	}
}

//��������
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

//����߿����
void ComputeCharacter::CalXYRatio()
{
	XYRatio = (double)xLength / (double)yLength;
}

//������ܶ�  ��
void ComputeCharacter::CalCompactness()
{
	Compactness = HandWritingLength * HandWritingLength / square;
}

//��������
void ComputeCharacter::CalCloseness()
{
	closeness = HandWritingLength / sqrt((points[0][0] - points[pointsNumber - 1][0])*(points[0][0] - points[pointsNumber - 1][0]) + (points[0][1] - points[pointsNumber - 1][1])*(points[0][1] - points[pointsNumber - 1][1]));
}

//��������������ļн�
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

//��������
void ComputeCharacter::CalCurvature()
{
	for (int i = 0; i < angle.size(); ++i)
	{
		curvature += angle[i];
	}

	curvature /= angle.size();
	//cout << "���Ƕ�/�ߵ���������" << curvature / angle.size() << endl << endl;
}

//�����ʼ����
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

//������ֹ����
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

//����ƫ����
void ComputeCharacter::CalOffset()
{
	//XstartPointMinOffset = (points[0][0] - minX) / xLength;
	//YstartPointMinOffset = (points[0][1] - minY) / xLength;//��ʼ�㵽�߽����Сֵƫ����y
	//XstartPointMaxOffset = (points[0][0] - maxX) / xLength;//��ʼ�㵽�߽�����ֵƫ����x
	//YstartPointMaxOffset = (points[0][1] - maxY) / xLength;//��ʼ�㵽�߽�����ֵƫ����y
	//XendPointMinOffset = (points[pointsNumber - 1][0] - minX) / xLength;//��ֹ�㵽�߽����Сֵƫ����x
	//YendPointMinOffset = (points[pointsNumber - 1][1] - minY) / xLength;//��ֹ�㵽�߽����Сֵƫ����y
	//XendPointMaxOffset = (points[pointsNumber - 1][0] - maxX) / xLength;//��ֹ�㵽�߽�����ֵƫ����x
	//YendPointMaxOffset = (points[pointsNumber - 1][0] - maxY) / xLength;//��ֹ�㵽�߽�����ֵƫ����y

	XstartPointMinOffset = (points[0][0] - minX);
	YstartPointMinOffset = (points[0][1] - minY);//��ʼ�㵽�߽����Сֵƫ����y
	XstartPointMaxOffset = (points[0][0] - maxX);//��ʼ�㵽�߽�����ֵƫ����x
	YstartPointMaxOffset = (points[0][1] - maxY);//��ʼ�㵽�߽�����ֵƫ����y
	XendPointMinOffset = (points[pointsNumber - 1][0] - minX);//��ֹ�㵽�߽����Сֵƫ����x
	YendPointMinOffset = (points[pointsNumber - 1][1] - minY);//��ֹ�㵽�߽����Сֵƫ����y
	XendPointMaxOffset = (points[pointsNumber - 1][0] - maxX);//��ֹ�㵽�߽�����ֵƫ����x
	YendPointMaxOffset = (points[pointsNumber - 1][0] - maxY);//��ֹ�㵽�߽�����ֵƫ����y
}

//���㺯��
void ComputeCharacter::Compute()
{
	//stringתint
	LOGE("StrToCoor()");
	StrToCoor(des);
	//��������������
	LOGE("CalCount()");
	CalCount();
	//����x,y����ֵ
	LOGE("GetXY()");
	GetXY();
	//����߽糤��
	LOGE("CalBoardLength()");
	CalBoardLength();
	//����߽�Χ�ɵ����
	LOGE("CalBoardSquare()");
	CalBoardSquare();
	//�������ڵ�֮��ľ���
	LOGE("CalDistanceOfTwoPoint()");
	CalDistanceOfTwoPoint();
	//����ʼ�����
	LOGE("CalHandWritingLength()");
	CalHandWritingLength();
	//��������
	LOGE("CalCentroid()");
	CalCentroid();
	//����߽����
	LOGE("CalXYRatio()");
	CalXYRatio();
	//���������
	LOGE("CalComPactness()");
	CalCompactness();
	//��������
	LOGE("CalCloseness()");
	CalCloseness();
	//��������������ļн�
	LOGE("CalAngle()");
	CalAngle();
	//��������
	LOGE("CalCurvature()");
	CalCurvature();
	//�����ʼ����
	LOGE("CalBeginDirection()");
	CalBeginDirection();
	//������ֹ����
	LOGE("CalEndDirection()");
	CalEndDirection();
	//����ƫ����
	LOGE("CalOffset()");
	CalOffset();
	//��ʼ��&��ֹ�㵽x��y���ֵ���������߿�ı�ֵ
	LOGE("RationOfESPoint()");
	RatioOfESPoint();
	//����յ�ĸ���
	LOGE("CalInflectionPointsNumber()");
	CalInflectionPointsNumber();
	LOGE("this->InflectionPointsNumber = %d", this->InflectionPointsNumber);
	//������ܶ�
	pointDensity = double(1.0 * pointsNumber) / double(square);
	//cout << tag << " " << pointsNumber << " " << square << " " << pointDensity << endl;
}

//���Ժ���
void ComputeCharacter::debug()
{
	cout << "��ǩ��" << tag << endl;
	cout << "����Ե�������" << pointsNumber << endl;
	cout << "X�����ֵ:" << maxX << endl;
	cout << "X����Сֵ:" << minX << endl;
	cout << "Y�����ֵ:" << maxY << endl;
	cout << "Y����Сֵ:" << minY << endl;
	cout << "�߿򳤶�X��" << xLength << endl;
	cout << "�߿򳤶�Y��" << yLength << endl;
	cout << "�߿򹹳ɵ������" << square << endl;
	cout << "�ʼ����ȣ�" << HandWritingLength << endl;
	cout << "��������X��" << CentroidX << endl;
	cout << "��������Y��" << CentroidY << endl;
	cout << "�߽������" << XYRatio << endl;
	cout << "�����ԣ�" << Compactness << endl;
	cout << "����ԣ�" << closeness << endl;
	cout << "���ʣ�" << curvature << endl;
	cout << "��ʼ����" << beginDirection << endl;
	cout << "��ֹ����" << endDirection << endl;
	cout << "**************end***************" << endl;
}

//��ʼ��&��ֹ�㵽x��y���ֵ���������߿�ı�ֵ
void ComputeCharacter::RatioOfESPoint()
{
	xStartMaxLengthRatio = (double)(points[0][0] - maxX) / ((double)max(xLength, yLength) / (double)1000);
	yStartMaxLengthRatio = (double)(points[0][1] - maxY) / ((double)max(xLength, yLength) / (double)1000);
	xEndMaxLengthRatio = (double)(points[pointsNumber - 1][0] - maxX) / ((double)max(xLength, yLength) / (double)1000);
	yEndMaxLengthRatio = (double)(points[pointsNumber - 1][1] - maxY) / ((double)max(xLength, yLength) / (double)1000);
	xStartMinLengthRatio = (double)(points[0][0] - minX) / ((double)max(xLength, yLength) / (double)1000);;
	yStartMinLengthRatio = (double)(points[0][1] - minY) / ((double)max(xLength, yLength) / (double)1000);
}

//���㵥�ʻ��յ����
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
        // ʹ����С���˷������ֱ��
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
        // ���ø�����ΧΪֱ��б��10%
        float floating_range = abs(k) * 0.71;
        // �����������10%������Ϊ������һ���յ�
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


//����յ�ĸ���
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
		SplitStroke();//�������������Ϊ���ʻ����������
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