import Jama.Matrix;
import algorithm.FILE_PATH;
import tool.FileHelper;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

/**
 * Created by Kris Chan on 9:06 AM 24/04/2017 .
 * All right reserved.
 */
public class AdapTest {
    public static double adaptValue1() {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(FILE_PATH.ROOT_PATH+"output/00510301.ocean_month.nc");
            Matrix outputMatrix = FileHelper.readRestartFile();
            //处理restart文件获得adaptValue
            //计算（sst-sst'）平方求和 该值即为适应度值
            try {
                Variable sst = ncfile.findVariable("sst");
                Array part = sst.read("5:5:1, 0:199:1, 0:359:1");
                Index index = part.reduce().getIndex();
                double[][] tem = new double[200][360];
                double adapt = 0;
                for (int j = 62; j < 130; j++) {
                    for (int k = 0; k < 220; k++) {
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
    public static void main(String args[]){
        double a = adaptValue1();
        System.out.println(a);
    }
}
