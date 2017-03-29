package algorithm;

import Jama.Matrix;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Kris Chan on 9:39 AM 24/03/2017 .
 * All right reserved.
 * The main class for CTS-SS algorithm.
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
    private double tabuParameter; //禁忌判定参数
    private double[][] tabuList = new double[Dim][tabuLength]; //禁忌表
    private double[][] initList; //候选初始解列表
    private double[] initValue = new double[Dim]; //初始解
    private double initEvaluation; //初始解扰动值
    private double[] bestValue; //当前最优解
    private double bestEvaluation; //最优解扰动值
    private double[] tempValue; //临时解
    private double tempEvaluation; //临时解扰动值
    private double[] localValue; //一次迭代中的局部解
    private double localEvaluation; //局部解扰动值
    Random random = new Random(); //生成随机数
    private int curCycle; //当前迭代次数

    public CTS_SS(int s1, int r1, int cycle1, int s2, int r2, int cycle2, float para){
        m1 = s1;
        R1 = r1;
        MAX_CYCLE1 = cycle1;
        m2 = s2;
        R2 = r2;
        MAX_CYCLE2 = cycle2;
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
     public void initSineMap(){
        initList = new double[Dim][initNumber];
        for(int i = 0; i < initNumber; i++){
            for(int j = 0; j < Dim; j++){
                initList[i][j] = Math.sin(Math.PI * Math.random());
            }
            Matrix init = new Matrix(initList);
            //todo:调用shell模式求解,在满足约束的条件(即禁忌判断参数)下,与适应度函数做对比,得到最优初始解
        }
    }


    //获取CNOP值
    public double evaluate(double[] chr){
        double CNOP = 0;
        for(int i = 0; i < Dim; i++){
            //todo:此处做矩阵减法,通过适应度函数来获取CNOP
        }
        return CNOP;
    }

    //领域交换
    public void swap(int radius, double[] tempChr){
        boolean flag = true;
        while(flag) {
            for(int i = 0; i < radius; i++) {
                int rand =random.nextInt()%Dim;
                tempChr[rand] = Math.sin(Math.PI * Math.random());
            }
            //todo:禁忌参数判断,若满足条件,则领域交换成功,退出
            if(true){
                flag = false;
            }
        }
    }

    //判断某个解是否在禁忌表中
    public boolean isTabuList(double[] tempChr){
        for(int i = 0; i < tabuLength; i++){
            //todo:tempChr与tabuList中元素进行对比
            if(true){
                return true;
            }
        }
        return false;
    }

    //解除禁忌与加入禁忌表
    public void addtoTabuList(double[] tempChr){
        for(int i = 0; i < tabuLength-1; i++){
            for(int j = 0; j < Dim; j++){
                tabuList[i][j] = tabuList[i+1][j];
            }
        }
        //将新的最优解加入禁忌表
        for(int k = 0; k < Dim; k++){
            tabuList[tabuLength-1][k] = tempChr[k];
        }
    }

    //初始并分阶段搜索
    public void StagedSearch() {
        //1.初始化
        bestValue = new double[Dim];
        tempValue = new double[Dim];
        localValue = new double[Dim];
        initSineMap();
        //2.分阶段搜索
        for(curCycle = 1; curCycle <= MAX_CYCLE1; curCycle++) {
            for (int i = 0; i < m1; i++) {
                swap(R1, tempValue);
                if (isTabuList(tempValue)) {
                    addtoTabuList(tempValue);
                }
            }
        }
        for(curCycle = 1; curCycle <= MAX_CYCLE2; curCycle++) {
            for (int i = 0; i < m2; i++) {
                swap(R2, tempValue);
                if (isTabuList(tempValue)) {
                    addtoTabuList(tempValue);
                }
            }
        }
        //3.打印结果

    }

    public static void main(String[] args) {

    }
}
