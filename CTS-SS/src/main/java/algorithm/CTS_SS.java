package algorithm;

import java.io.IOException;

/**
 * Created by Kris Chan on 9:39 AM 24/03/2017 .
 * All right reserved.
 * The main class for cts-ss algorithm.
 */
public class CTS_SS {
    //初始参数配置
    private final int Dim = 80; //特征空间维度(主成分数目)
    private final int tabuLength= 10; //禁忌长度
    private final int initNumber = 10; //初始候选解数目
    private int m1; //阶段一候选解个数
    private int m2; //阶段一候选解个数
    private int R1; //阶段1邻域半径
    private int R2; //阶段1邻域半径
    private int MAX_CYCLE1; //阶段一迭代次数
    private int MAX_CYCLE2; //阶段二迭代次数
    private float tabuParameter; //禁忌判定参数
    private float[][] tabuList = new float[Dim][tabuLength]; //禁忌表
    private float[][] initList = new float[Dim][initNumber]; //候选初始解列表
    private float[] initValue = new float[Dim]; //初始解
    private float initEvaluation; //初始解扰动值
    private float[] bestValue = new float[Dim]; //当前最优解
    private float bestEvaluation; //最优解扰动值
    private float[] tempValue = new float[Dim]; //临时解
    private float tempEvaluation; //临时解扰动值
    private float[] localValue = new float[Dim]; //一次迭代中的局部解
    private float localEvaluation; //局部解扰动值

    public CTS_SS(int s1, int r1, int s2, int r2, float para){
        m1 = s1;
        R1 = r1;
        m2 = s2;
        R2 = r2;
        tabuParameter = para;
    }

    /**
     * 初始化CTS-SS算法类
     * @param filename 数据文件名,存储所有的主成分维度坐标
     * @throws IOException
     */
    private void init(String filename) throws IOException{

    }

    //生成Sine初值


    //Stage1

    //Stage2

}
