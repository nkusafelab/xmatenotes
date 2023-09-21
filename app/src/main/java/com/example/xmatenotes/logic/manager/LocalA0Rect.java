package com.example.xmatenotes.logic.manager;

public class LocalA0Rect {
    //下列是写入时，需要的数据

    public static String Localgroup = "1"; //笔迹点所在位置的小组

    public static String Plocalgroup = "1"; //得到该组的结伴组信息
    public static String time = "1";   //打点时间
    public static String group = "1";  //组别 填写时使用的组别

    public static String pName = "i";  //姓名

    public static String field = "1";   //写入需要的字段

    public static  String result = "1"; //写入表格中的结果

    public static String  wOrR = "do";    //用来表示是组别还是周还是姓名，根据此产生不同的响应
    // 0是读 1是写

    //下面是读取时需要的数据

    public static String  groupMessage[] = new String[1000]; //提取得到的组信息,设置成数组可以多响应

    public static String timeMessage[] = new String[1000]; //提取得到的周信息

    public static String nameMessage[] = new String[1000]; //提取得到的名字信息

    public static int gOrTA = 0;   //0去G表，1去TA表


    /**小组编号是类似1，2，3等
     * 根据助教来获得哪几个组
     * @param A
     */
    public static void getGroupMessage(String A ){  //助教负责组的分配
        if(A == "A组"){
            groupMessage[0] = "1";
            groupMessage[1] = "2";
            groupMessage[2] = "5";
            groupMessage[3] = "6";
        }
        if(A == "B组"){

            groupMessage[0] = "7";
            groupMessage[1] = "9";
            groupMessage[2] = "10";
            groupMessage[3] = "11";
            groupMessage[4] = "12";
            groupMessage[5] = "13";
            groupMessage[6] = "14";
            groupMessage[7] = "15";
            groupMessage[8] = "16";
        }
    }

    /**
     * 根据点击的哪个组来获得那几个组
     * @param A
     */
    public static void getGroupMessage(int A ){  //长压小组区域时调用

         String B = Integer.toString(A);

         groupMessage[0] = B;
    }

    /**
     * 根据在N表中的查询情况来写入名字数组
     * @param A
     */
    public static void getNameMessage(String A){  //长压姓名区域时调用

        nameMessage[0] = A;

    }

    /**
     * 根据对G0的特殊处理（压成一级来获得周信息）
     * @param A
     */
    public static void getTimeMessage(String A ){  //包含多个时间区域

        String substring = A.substring(0, A.length() - 1);

        String[] result = substring.split("，");

        int i = 0;

        while(i<100){    //先做一次清除
            timeMessage[i] = "0";
            i++;
        }

        int j = result.length;

        for(int k = 0;k<j;k++){   //得到周数
            timeMessage[k] = result[k];
        }

    }

}
