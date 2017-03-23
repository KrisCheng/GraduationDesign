package fileread;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import java.io.IOException;
/**
 * Created by Kris Chan on 10:14 AM 23/03/2017 .
 * All right reserved.
 */
public class FileRead {
    public static void main(String[] args) {
        //获取nc文件中x轴和y轴数据
        final String filename = "src/main/java/fileread/ocean_temp_salt.res.nc";
        final int xAxis = 360;
        final int yAxis = 200;
        double[][] data = new double[xAxis][yAxis];
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            //共有 temp 和 salt 两种参数
            String variable = "temp";
            Variable varBean = ncfile.findVariable(variable);
            //读取数据
            if(null != varBean) {
                int[] origin = new int[] {0,0,0,0};
                int[] size = new int[] {1,1,yAxis,xAxis};
                Array data2D = varBean.read(origin, size).reduce();
                Index index = data2D.getIndex();
                //将数据存储至data数组中
                for(int i = 0; i < yAxis; i++){
                    for(int j = 0; j < xAxis; j++){
                        data[j][i] = data2D.getDouble(index.set(i,j));
                    }
                }
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        } finally {
            if (null != ncfile)
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                }
        }
    }
}
