#pragma once
//检验模型代码
#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <string>
#include "svm.h"
#include "ComputeCharacter.h"

#include <android/log.h>
#define LOG_TAG "gestureRecognition1"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern vector<double> rollBackProb;
extern vector<double> commonCharacterProb;
extern bool isRollBack;
extern bool isCommonCharacter;
int print_null(const char *s, ...) { return 0; }

static int(*info)(const char *fmt, ...) = &printf;

struct svm_node *x;
int max_nr_attr = 64;
int predict_label;
double tmpMaxPredict = 0.0;

struct svm_model* model;
int predict_probability = 1; // default:0

static char *line = NULL;
static int max_line_len;

static char* readline(FILE *input)
{
	int len;

	if (fgets(line, max_line_len, input) == NULL)
		return NULL;

	while (strrchr(line, '\n') == NULL)
	{
		max_line_len *= 2;
		line = (char *)realloc(line, max_line_len);
		len = (int)strlen(line);
		if (fgets(line + len, max_line_len - len, input) == NULL)
			break;
	}
	return line;
}

void exit_input_error(int line_num)
{
	fprintf(stderr, "Wrong input format at line %d\n", line_num);
	exit(1);
}

void predict(FILE *input, FILE *output)
{
	int correct = 0;
	int total = 0;
	double error = 0;
	double sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;

	int svm_type = svm_get_svm_type(model);
	int nr_class = svm_get_nr_class(model);
	double *prob_estimates = NULL;
	int j;
	tmpMaxPredict = 0;

	if (predict_probability)
	{
		if (svm_type == NU_SVR || svm_type == EPSILON_SVR)
		{

		}
		//info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=%g\n", svm_get_svr_probability(model));
		else
		{
			int *labels = (int *)malloc(nr_class * sizeof(int));
			svm_get_labels(model, labels);
			prob_estimates = (double *)malloc(nr_class * sizeof(double));
			fprintf(output, "labels");
//			cout << "labels "; // cout
			for (j = 0; j < nr_class; j++) {
				fprintf(output, " %d", labels[j]);

//				cout << gesture[j] << "(" << char('a' + labels[j]) << ")" << " "; // cout
			}
//			cout << endl; // cout
			fprintf(output, "\n");
			free(labels);
		}
	}

	max_line_len = 1024;
	line = (char *)malloc(max_line_len * sizeof(char));
	while (readline(input) != NULL)
	{
		int i = 0;
		double target_label;
		char *idx, *val, *label, *endptr;
		int inst_max_index = -1; // strtol gives 0 if wrong format, and precomputed kernel has <index> start from 0

		label = strtok(line, " \t\n");
		LOGE("label == NULL:%d", label == NULL);
		if (label == NULL) {// empty line
			LOGE("exit1");
			exit_input_error(total + 1);
		}
		target_label = strtod(label, &endptr);
		LOGE("enpter == label: %d  *enptr != '\\0' %d", endptr == label, *endptr != '\0');
		if (endptr == label || *endptr != '\0') {
			LOGE("exit2");
			exit_input_error(total + 1);
		}
		while (1)
		{
			if (i >= max_nr_attr - 1)	// need one more for index = -1
			{
				max_nr_attr *= 2;
				x = (struct svm_node *) realloc(x, max_nr_attr * sizeof(struct svm_node));
			}

			idx = strtok(NULL, ":");
			val = strtok(NULL, " \t");

			if (val == NULL)
				break;
			errno = 0;
			x[i].index = (int)strtol(idx, &endptr, 10);
			LOGE("endptr == idx:%d  errno != 0:%d  *endptr != '\\0' :%d  x[i].index <= inst_max_index: %d", endptr == idx, errno != 0, *endptr != '\0', x[i].index <= inst_max_index);
			if (endptr == idx || errno != 0 || *endptr != '\0' || x[i].index <= inst_max_index){
				LOGE("exit3");
				exit_input_error(total + 1);
			}
			else {
				inst_max_index = x[i].index;
			}

			errno = 0;
			x[i].value = strtod(val, &endptr);
			LOGE("endptr == val:%d  errno != 0:%d  *endptr != '\\0':%d  !isspace(*endptr):%d", endptr == val, errno != 0, *endptr != '\0', !isspace(*endptr));
			if (endptr == val || errno != 0 || (*endptr != '\0' && !isspace(*endptr))) {
				LOGE("exit4");
				exit_input_error(total + 1);
			}

			++i;
		}
		x[i].index = -1;
		if (predict_probability && (svm_type == C_SVC || svm_type == NU_SVC))
		{
			predict_label = svm_predict_probability(model, x, prob_estimates);

			if (globalStrokeCount > 0 && globalStrokeCount <= 5) {
				int tmp = 0;
				double maxProbability = 0;
				for (int i = 0; i < stokeCountForGesture[globalStrokeCount - 1].size(); i++) {
					if (maxProbability < prob_estimates[stokeCountForGesture[globalStrokeCount - 1][i]]) {
						maxProbability = prob_estimates[stokeCountForGesture[globalStrokeCount - 1][i]];
						tmp = stokeCountForGesture[globalStrokeCount - 1][i];
					}
				}
				predict_label = tmp;
			}
			LOGE("acquire prob success");
			fprintf(output, "%g", predict_label);
//			cout << "对应label预测概率："; // cout
			for (j = 0; j < nr_class; j++) {
				fprintf(output, " %g", prob_estimates[j]);
				tmpMaxPredict = tmpMaxPredict > prob_estimates[j] ? tmpMaxPredict : prob_estimates[j];
				LOGE("%d:%f maxProb:%f", j, prob_estimates[j], tmpMaxPredict);
//				cout << prob_estimates[j] * 100 << "%"; // cout prob_estimates
//				if (j != nr_class - 1) {
//					cout << " ";
//				}
			}
//			cout << endl << "预测结果：" << gesture[int(predict_label)] << "(" << char('a' + int(predict_label)) << "):" << tmpMaxPredict * 100 << "%" << endl;
//			scale = false;
//			if (tmpMaxPredict >= 0) scale = true;
//			fprintf(output, "\n");
//			LOGE("tmpMaxPredict:%f", tmpMaxPredict);

			LOGE("rollback or commonCharacter");
			LOGE("predict_label:%d maxProb:%f", predict_label, tmpMaxPredict);
//			isCommonCharacter = true;
//			isRollBack = true;
			LOGE("rollProb:%f  commonCharacterPron:%f maxProb:%f", rollBackProb[predict_label], commonCharacterProb[predict_label],tmpMaxPredict);
			if (tmpMaxPredict >= rollBackProb[int(predict_label)]) {
				LOGE("output");
				isRollBack = false;
				isCommonCharacter = false;
				LOGE("isRollBack:%d", isRollBack);
				LOGE("isCommonCharacter:%d", isCommonCharacter);
			}
			else if (tmpMaxPredict >= commonCharacterProb[int(predict_label)]){
				LOGE("rollback");
				isCommonCharacter = false;
				isRollBack = true;
				LOGE("isRollBack:%d", isRollBack);
				LOGE("isCommonCharacter:%d", isCommonCharacter);
			}
			else {
				LOGE("commonCharacter");
				isRollBack = false;
				isCommonCharacter = true;
				LOGE("isRollBack:%d", isRollBack);
				LOGE("isCommonCharacter:%d", isCommonCharacter);
			}
			LOGE("acquire isRollBack and isCommonCharacter");
			//存储预测概率
//			ofstream outfile;  // 创建或打开文件
//			outfile.open("data\\predictProb\\ⅡGesturePredictProb.csv", ios::out | ios::app);

//			if (outfile.is_open()) {
				// 将数据写入文件
//				outfile << predict_label << "," << tmpMaxPredict << endl;
//				outfile.close();  // 关闭文件
//			}
		}
		else
		{
			predict_label = svm_predict(model, x);
			fprintf(output, "%.17g\n", predict_label);
		}

		if (predict_label == target_label)
			++correct;
		error += (predict_label - target_label)*(predict_label - target_label);
		sump += predict_label;
		sumt += target_label;
		sumpp += predict_label*predict_label;
		sumtt += target_label*target_label;
		sumpt += predict_label*target_label;
		++total;

	}
	if (svm_type == NU_SVR || svm_type == EPSILON_SVR)
	{
		/*info("Mean squared error = %g (regression)\n", error / total);
		info("Squared correlation coefficient = %g (regression)\n",
		((total*sumpt - sump*sumt)*(total*sumpt - sump*sumt)) /
		((total*sumpp - sump*sump)*(total*sumtt - sumt*sumt))
		);*/
	}
	/*else
	info("Accuracy = %g%% (%d/%d) (classification)\n",
	(double)correct / total * 100, correct, total);*/
	if (predict_probability)
		free(prob_estimates);

}

void exit_with_help()
{
	printf(
		"Usage: svm-predict [options] test_file model_file output_file\n"
		"options:\n"
		"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); for one-class SVM only 0 is supported\n"
		"-q : quiet mode (no outputs)\n"
		);
	exit(1);
}

int svmPredict(int argc, char **argv)
{
	argc = 4;
	FILE *input, *output;
	int i;
	// parse options
	for (i = 1; i<argc; i++)
	{
		if (argv[i][0] != '-') break;
		++i;
		switch (argv[i - 1][1])
		{
		case 'b':
			predict_probability = atoi(argv[i]);
			break;
		case 'q':
			//info = &print_null;
			i--;
			break;
		default:
			fprintf(stderr, "Unknown option: -%c\n", argv[i - 1][1]);
			exit_with_help();
		}
	}

	if (i >= argc - 2)
		exit_with_help();

	input = fopen(argv[i], "r");
	if (input == NULL)
	{
		fprintf(stderr, "can't open input file %s\n", argv[i]);
		exit(1);
	}

	output = fopen(argv[i + 2], "w");
	if (output == NULL)
	{
		fprintf(stderr, "can't open output file %s\n", argv[i + 2]);
		exit(1);
	}

	if ((model = svm_load_model(argv[i + 1])) == 0)
	{
		fprintf(stderr, "can't open model file %s\n", argv[i + 1]);
		exit(1);
	}

	x = (struct svm_node *) malloc(max_nr_attr * sizeof(struct svm_node));
	if (predict_probability)
	{
		if (svm_check_probability_model(model) == 0)
		{
			fprintf(stderr, "Model does not support probabiliy estimates\n");
			exit(1);
		}
	}
	else
	{
		if (svm_check_probability_model(model) != 0)
		{

		}
		//info("Model supports probability estimates, but disabled in prediction.\n");
	}

	LOGE("predict() begin");
	predict(input, output);
	LOGE("predict() end");
	svm_free_and_destroy_model(&model);
	free(x);
	free(line);
	fclose(input);
	fclose(output);
	return 0;
}

