package algorithm;

import Jama.Matrix;
import org.apache.poi.ss.usermodel.*;
import tool.FileHelper;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * Created by Kris Chan on 9:39 AM 24/03/2017 .
 * All right reserved.
 * The main class for CTS-SS algorithm.
 */
public class CTS_SS {
    //初始参数配置
    private final int xAxis = 360;
    private final int yAxis = 200;
    private final int Dim = 20; //特征空间维度(主成分数目)

    private final int tabuLength = 10; //禁忌长度
    private final int initNumber = 1; //初始候选解数目
    private int m1; //阶段一候选解个数
    private int m2; //阶段一候选解个数
    private int R1; //阶段1邻域半径
    private int R2; //阶段1邻域半径
    private int MAX_CYCLE1; //阶段一迭代次数
    private int MAX_CYCLE2; //阶段二迭代次数
    private double tabuParameter; //禁忌判定参数

    private double[][] tabuList = new double[Dim][tabuLength]; //禁忌表
    private double[][] initList; //候选初始解列表
    private double[][] initValue = new double[Dim][1]; //初始解
    private double initEvaluation; //初始解扰动值
    private double[][] bestValue; //当前最优解
    private double bestEvaluation; //最优解扰动值
    private double[][] tempValue; //临时解
    private double tempEvaluation; //临时解扰动值
    private double[][] localValue; //一次迭代中的局部解
    private double localEvaluation; //局部解扰动值
    Random random = new Random(); //生成随机数
    private int curCycle; //当前迭代次数
    Matrix tempTransMatrix; //降维后矩阵

    public CTS_SS(int s1, int r1, int cycle1, int s2, int r2, int cycle2, double para) {
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
     *
     * @param filename 数据文件名,存储所有的主成分维度坐标
     * @throws IOException
     */
    public void init(String filename) throws IOException {
        try {
            double[][] tempTransList = new double[xAxis * yAxis][Dim];
            File excelFile = new File(filename); //创建文件对象
            FileInputStream is = new FileInputStream(excelFile); //文件流
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数
            for (int r = 0; r < rowCount; r++) {
                Row row = sheet.getRow(r);
                int cellCount = row.getPhysicalNumberOfCells(); //获取总列数
                //遍历每一列
                for (int c = 0; c < cellCount; c++) {
                    Cell cell = row.getCell(c);
                    tempTransList[r][c] = cell.getNumericCellValue();
                }
            }
            tempTransMatrix = new Matrix(tempTransList);//降维后矩阵
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Sine生成初值是否满足初值约束
    public boolean isLegal(double[][] num){
        Matrix p = new Matrix(num);
        p = tempTransMatrix.times(p);
        double[] lat = FileHelper.getLat();
        double[][] sigma = FileHelper.getSigma();
        double sum = 0.0;
        for(int j = 0; j < yAxis; j++){
            for(int k = 0; k < xAxis; k++){
                sum += Math.pow(Math.cos(lat[j]) * p.get(k * 200 + j, 0) / sigma[j][k], 2);
            }
        }
        if (Math.sqrt(sum) > 150){
            return false;
        }
        return true;

    }

    //生成Sine初值并记录
    public Matrix initSineMap() {
        initList = new double[Dim][initNumber];
        Matrix initMatrix = new Matrix(xAxis*yAxis,Dim);
        //通过sin函数获取初始解并保存在单独文件中
        for (int i = 0; i < initNumber; i++) {
            try {
                File file = new File(FILE_PATH.RESOURCE_PATH+ "PCA" + i + ".txt");
                PrintStream ps = new PrintStream(new FileOutputStream(file));
                for (int j = 0; j < Dim; j++) {
                    initList[j][i] = Math.sin(Math.PI * Math.random());//通过sin函数获取初始解
                    ps.println(initList[j][i]);
                    ps.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
            Matrix initSineMatrix = new Matrix(initList);
            //获得initNumber个初始解,全部保存在initMatrix中
            initMatrix = tempTransMatrix.times(initSineMatrix);
            //记录初始解
        //todo:调用shell模式求解,在满足约束的条件(即禁忌判断参数)下,与适应度函数做对比,得到最优初始解(GFDL模式运行时间过长,暂时取第0个)
        //获取一个初始值作为输入扰动并将结果输出
            double[][] temp = new double[xAxis][yAxis];
            for(int m = 0; m < yAxis; m++) {
                for (int n = 0; n < xAxis; n++) {
                    temp[n][m] = initMatrix.get((m * n + n), 0);
                }
            }
        //todo:判断该解是否满足约束
        //存储初始解
        for(int t = 0; t < Dim; t ++){
            initValue[t][0] = initList[t][0];
            bestValue[t][0] = initList[t][0];
        }
        System.out.println("PCA and Initialization finished. go to run the GFDL.");
        return new Matrix(temp);
    }


    //执行GFDL模式并获取CNOP值,运行第i个文件,temp表示该文件对应的初始扰动矩阵
    public void evaluate(int i, Matrix temp) {
        //数据准备,将扰动加入INPUT文件中
        FileHelper.deleteFile(FILE_PATH.RESOURCE_PATH +"PCA"+ i + ".txt");
        FileHelper.prepareFile(i, temp);
        FileHelper.copyFile(FILE_PATH.RESOURCE_PATH + "/ocean_temp_salt_" + i + ".nc", FILE_PATH.INPUT_PATH + "/ocean_temp_salt.res.nc", true);
        //调脚本
        FileHelper.exec("bsub ./fr21.csh");
        while(true){
            String tem = FileHelper.exec("bjobs");
            if(tem.contains("No")){
                break;
            }
        }
        tempEvaluation = adaptValue();

        //下次运行前需要删除的文件
        FileHelper.deleteDirectory(FILE_PATH.OUTPUT_PATH + "ascii");
        FileHelper.deleteDirectory(FILE_PATH.OUTPUT_PATH + "history");
        FileHelper.deleteDirectory(FILE_PATH.OUTPUT_PATH + "RESTART");
        FileHelper.deleteFile(FILE_PATH.OUTPUT_PATH + "data_table");
        FileHelper.deleteFile(FILE_PATH.OUTPUT_PATH + "diag_table");
        FileHelper.deleteFile(FILE_PATH.OUTPUT_PATH + "field_table");
        FileHelper.deleteFile(FILE_PATH.OUTPUT_PATH + "input.nml");
        System.out.println("模式运行完成,接下来进行寻优步骤");
        if(tempEvaluation > bestEvaluation){
            //todo 禁忌和禁忌表
        }
    }


    //适应度函数
    public double adaptValue(){

        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(FILE_PATH.HISTORY_PATH + "00510301.ocean_month.nc");
            Matrix outputMatrix = FileHelper.readRestartFile();
            //处理restart文件获得adaptValue
            //计算（sst-sst'）平方求和 该值即为适应度值
            try{
                Variable sst = ncfile.findVariable("sst");
                Array part = sst.read("5:5:1, 0:199:1, 0:359:1");
                Index index = part.reduce().getIndex();
                double[][] tem = new double[200][360];
                double adapt = 0;
                for(int j = 0; j < 200; j++){
                    for(int k = 0; k < 360; k++){
                        tem[j][k] = part.reduce().getDouble(index.set(j, k)) - outputMatrix.get(j, k);
                        adapt += Math.pow(tem[j][k], 2);
                    }
                }
                return adapt;

            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (IOException ioe) {
            return -1;
        }

        return -1;

    }

    //领域交换
    public double[][] swap(int radius, double[][] tempChr) {
        boolean flag = true;
        double[][] temp = new double[Dim][1];
        for(int k = 0; k < Dim; k ++){
            temp[k][0] = tempChr[k][0];
        }
        while (flag) {
            for (int i = 0; i < radius; i++) {
                int rand = random.nextInt() % Dim;
                tempChr[rand][0] = Math.sin(Math.PI * Math.random());
            }
            //todo:禁忌参数判断,若满足条件,则领域交换成功,退出
            double sum = 0;
            for(int k = 0; k < Dim; k ++){
                sum += Math.abs(temp[k][0]-tempChr[k][0]);
            }
            if (sum < tabuParameter) {
                flag = false;
            }
        }
        return tempChr;
    }

    //TODO 判断某个解是否在禁忌表中
    public boolean isTabuList(double[] tempChr) {
        for (int i = 0; i < tabuLength; i++) {
            //todo:tempChr与tabuList中元素进行对比
            if (true) {
                return true;
            }
        }
        return false;
    }

    //TODO 解除禁忌与加入禁忌表
    public void addtoTabuList(double[] tempChr) {
        for (int i = 0; i < tabuLength - 1; i++) {
            for (int j = 0; j < Dim; j++) {
                tabuList[i][j] = tabuList[i + 1][j];
            }
        }
        //将新的最优解加入禁忌表
        for (int k = 0; k < Dim; k++) {
            tabuList[tabuLength - 1][k] = tempChr[k];
        }
    }

    //主成分与目标矩阵的转换方法
    public Matrix convert(double[][] temp){
        Matrix tempVector =  new Matrix(temp);
        Matrix finalVector = tempTransMatrix.times(tempVector);
        double[][] tempList = new double[xAxis][yAxis];
        for(int m = 0; m < yAxis; m++) {
            for (int n = 0; n < xAxis; n++) {
                temp[n][m] = finalVector.get((m * n + n), 0);
            }
        }
        return new Matrix(tempList);
    }

    //初始并分阶段搜索
    public void solution() {
        //1.初始化
        bestValue = new double[Dim][1];
        tempValue = new double[Dim][1];
        localValue = new double[Dim][1];
        Matrix tempSolution = initSineMap();
        Matrix bestSolution = initSineMap();

        //2.分阶段搜索
        for (curCycle = 1; curCycle <= MAX_CYCLE1; curCycle++) {
            for (int i = 0; i < m1; i++) {
                tempValue = swap(R1, tempValue);
                tempSolution = convert(tempValue);
                evaluate(i,tempSolution);
            }
        }
        for (curCycle = 1; curCycle <= MAX_CYCLE2; curCycle++) {
            for (int i = 0; i < m2; i++) {
                tempValue = swap(R2, tempValue);
                tempSolution = convert(tempValue);
                evaluate(i,tempSolution);
            }
        }
        //3.打印结果

    }

}
