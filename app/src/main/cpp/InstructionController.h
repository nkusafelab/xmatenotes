#pragma once
#include<vector>
#include <iostream>
#include<algorithm>
using namespace std;
//计算包体拐点
void CalInclusionInflectionPoint(const std::vector<std::vector<int>>& source, vector<int>& MainInclusionMinPoint, vector<int>& MainInclusionMaxPoint, vector<int>& AuxiliaryInclusionMinPoint, vector<int>& AuxiliaryInclusionMaxPoint);
//根据起始点和终点距离判断
bool StartPointAndEndPoint(const std::vector<std::vector<int>>& source, const std::vector<std::vector<int>>& des);
//根据点之间的距离和判断
bool SumOfPointDistance(const std::vector<std::vector<int>>& source, const std::vector<std::vector<int>>& des);
//根据包体长宽判断
bool LengthAndWidth(const std::vector<std::vector<int>>& source, const std::vector<std::vector<int>>& des);
//根据中心点判断
bool CenterPoint(const std::vector<std::vector<int>>& source, const std::vector<std::vector<int>>& des);
//strToInt
void StrToCoor(const string &s, std::vector<std::vector<int>>& source,int &id);