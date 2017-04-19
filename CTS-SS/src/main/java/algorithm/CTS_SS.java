package algorithm;

import Jama.Matrix;
import org.apache.poi.ss.usermodel.*;
import tool.FileHelper;
import tool.ShellHelper;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.*;
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

    private double[] tabuList = new double[tabuLength]; //禁忌表
    private double[][] initList; //候选初始解列表

    private double[][] bestValue = new double[Dim][1]; //当前最优解
    private double bestEvaluation = 0; //最优解扰动值
    Matrix bestSolution;

    private double[][] tempValue= new double[Dim][1]; //临时解
    private double tempEvaluation = 0; //临时解扰动值
    Matrix tempSolution;

    private double[][] localValue = new double[Dim][1]; //局部解
    private double localEvaluation = 0; //局部解扰动值
    Matrix localSolution;

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
            tempTransMatrix = new Matrix(tempTransList);//降维后大矩阵
            System.out.println("PCA finished. go to initialization.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Sine生成初值是否满足初值约束
    public boolean isLegal(double[][] num) {
        Matrix p = new Matrix(num);
        p = tempTransMatrix.times(p);
        double[] lat = FileHelper.getLat();
        double[][] sigma = FileHelper.getSigma();
        double sum = 0.0;
        for (int j = 0; j < yAxis; j++) {
            for (int k = 0; k < xAxis; k++) {
                sum += Math.pow(Math.cos(lat[j]) * p.get(k * 200 + j, 0) / sigma[j][k], 2);
            }
        }
        if (Math.sqrt(sum) > 150) {
            return false;
        }
        return true;

    }

    //生成Sine初值并记录
    public Matrix initSineMap() {
        initList = new double[Dim][initNumber];
        //通过sin函数获取初始解并保存在单独文件中
        for (int i = 0; i < initNumber; i++) {
//            try {
//                File file = new File(FILE_PATH.PCA_PATH + "PCA" + i + ".txt");
//                PrintStream ps = new PrintStream(new FileOutputStream(file));
                for (int j = 0; j < Dim; j++) {
                    initList[j][i] = Math.sin(Math.PI * Math.random());//通过sin函数获取初始解
//                    ps.println(initList[j][i]);
//                    ps.close();
                }
//            }
//            catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }
        Matrix initSineMatrix = new Matrix(initList);
        //获得initNumber个初始解,全部保存在initMatrix中
        Matrix initMatrix = tempTransMatrix.times(initSineMatrix);
        //记录初始解
        //todo:调用shell模式求解,在满足约束的条件(即禁忌判断参数)下,与适应度函数做对比,得到最优初始解(GFDL模式运行时间过长,暂时取第0个)
        //获取一个初始值作为输入扰动并将结果输出
        double[][] temp = new double[xAxis][yAxis];
        for (int m = 0; m < yAxis; m++) {
            for (int n = 0; n < xAxis; n++) {
                temp[n][m] = initMatrix.get((m * n + n), 0);
            }
        }
        //todo:判断该解是否满足约束
        //存储初始解,即为最优解
        for (int t = 0; t < Dim; t++) {
            bestValue[t][0] = initList[t][0];
            tempValue[t][0] = bestValue[t][0];
        }
//        bestEvaluation = adaptValue();//暂时未运行模式,注释
        bestSolution = convert(bestValue);
        tempSolution = bestSolution;

        System.out.println("---------------------------");
//        System.out.println("the initialize CNOP is " + bestEvaluation); //暂时未运行模式,注释
        System.out.println("the initialize result is:");
        for (int i = 0; i < Dim; i++) {
            System.out.println(i + " : " + bestSolution.get(i, 0));
        }
        System.out.println("Initialization finished. go to run the GFDL.");
        System.out.println("---------------------------");

        return new Matrix(temp);
    }


    //执行GFDL模式并获取CNOP值,运行第i个文件,temp表示该文件对应的初始扰动矩阵
    public void evaluate(Matrix temp) {
        //数据准备,将扰动加入INPUT文件中
        FileHelper.prepareFile(temp);
        FileHelper.copyFile(FILE_PATH.RESOURCE_PATH + "/ocean_temp_salt.nc", FILE_PATH.INPUT_PATH + "/ocean_temp_salt.res.nc", true);

        //调shell
        FileHelper.exec("bsub ./fr21.csh");
        //轮询(5分钟一次),判断模式是否完成
        while (true) {
            try {
                Thread.sleep(1000 * 60 * 5);
                String tem = ShellHelper.exec("bjobs");
                if (tem.equals("")) {
                    System.out.println("GFDL run finished!");
                    break;
                } else {
                    System.out.println("This cycle not finished!");
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        FileHelper.deleteFile(FILE_PATH.EXP_PATH + "CM2.1p1.output.tar.gz");
        FileHelper.deleteFile(FILE_PATH.EXP_PATH + "fms.out");
        System.out.println("The model finished, next step is find the best solution.");

        if (!inTabuList(tempEvaluation)) {
            if (tempEvaluation > localEvaluation) {
                localEvaluation = tempEvaluation;
                localSolution = temp;
                addtoTabuList(localEvaluation);
                System.out.println("---------------------------");
                System.out.println("Get a better solution: " + localEvaluation);
                System.out.println("---------------------------");
            }
        }
    }

    //适应度函数
    public double adaptValue() {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(FILE_PATH.HISTORY_PATH + "00510301.ocean_month.nc");
            Matrix outputMatrix = FileHelper.readRestartFile();
            //处理restart文件获得adaptValue
            //计算（sst-sst'）平方求和 该值即为适应度值
            try {
                Variable sst = ncfile.findVariable("sst");
                Array part = sst.read("5:5:1, 0:199:1, 0:359:1");
                Index index = part.reduce().getIndex();
                double[][] tem = new double[200][360];
                double adapt = 0;
                for (int j = 0; j < 200; j++) {
                    for (int k = 0; k < 360; k++) {
                        tem[j][k] = part.reduce().getDouble(index.set(j, k)) - outputMatrix.get(j, k);
                        adapt += Math.pow(tem[j][k], 2);
                    }
                }
                return adapt;

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException ioe) {
            return -1;
        }
        return -1;
    }

    //领域交换,产生一个新解
    public double[][] swap(int radius, double[][] tempChr) {
        double[][] temp = new double[Dim][1];
        for (int k = 0; k < Dim; k++) {
            temp[k][0] = tempChr[k][0];
        }
        for (int i = 0; i < radius; i++) {
            int rand = random.nextInt(65535) % Dim;
            tempChr[rand][0] = Math.sin(Math.PI * Math.random());
        }
        return temp;
    }


    // 判断某个解是否在禁忌表中
    public boolean inTabuList(double adaptValue) {
        for (int i = 0; i < tabuLength; i++) {
            if (Math.abs(tabuList[i]-adaptValue)<tabuParameter) {
                return true;
            }
        }
        return false;
    }

    //TODO 解除禁忌与加入禁忌表
    public void addtoTabuList(double tempChr) {
        for (int i = 0; i < tabuLength - 1; i++) {
                tabuList[i] = tabuList[i + 1];
        }
        //将新的最优解加入禁忌表
        tabuList[tabuLength - 1] = tempChr;
    }

    //主成分与目标矩阵的转换方法
    public Matrix convert(double[][] temp){
        Matrix tempVector =  new Matrix(temp);
        Matrix finalVector = tempTransMatrix.times(tempVector);
        double[][] tempList = new double[xAxis][yAxis];
        for(int m = 0; m < yAxis; m++) {
            for (int n = 0; n < xAxis; n++) {
                tempList[n][m] = finalVector.get((m * n + n), 0);
            }
        }
        return new Matrix(tempList);
    }

    //判断本次迭代有没有获得更优解,如有,替换
    public void isBest(double localEvaluation,double bestEvaluation){
        if (localEvaluation-bestEvaluation>tabuParameter) {
            //获得更优解,更新并输出
            bestEvaluation = localEvaluation;
            bestSolution = localSolution;
            for(int i = 0; i < Dim; i++){
                bestValue[i][0] = localValue[i][0];
            }
            System.out.println("---------------------------");
            System.out.println("Cycle finished. Get a better solution: " + bestEvaluation);
            System.out.println("---------------------------");
        }
        else{
            System.out.println("---------------------------");
            System.out.println("Cycle finished. no better solution found.");
            System.out.println("---------------------------");
        }
    }

    //初始并分阶段搜索
    public void solution() {
        //1.初始化
        bestSolution = initSineMap();

        //2.分阶段搜索
        for (curCycle = 1; curCycle <= MAX_CYCLE1; curCycle++) {
            for (int i = 0; i < m1; i++) {
                tempValue = swap(R1, tempValue);
                tempSolution = convert(tempValue);
                evaluate(tempSolution);
            }
            isBest(localEvaluation, bestEvaluation);
        }
        for (curCycle = 1; curCycle <= MAX_CYCLE2; curCycle++) {
            for (int i = 0; i < m2; i++) {
                tempValue = swap(R2, tempValue);
                tempSolution = convert(tempValue);
                evaluate(tempSolution);
            }
            isBest(localEvaluation, bestEvaluation);
        }

        //3.打印结果
        System.out.println("---------------------------");
        System.out.println("the program finished.");
        System.out.println("the best CNOP is " + bestEvaluation);
        System.out.println("the best result is:");
        for (int i = 0; i < Dim; i++) {
            System.out.println(i+" : "+bestValue[i][0]);
        }
    }

}
