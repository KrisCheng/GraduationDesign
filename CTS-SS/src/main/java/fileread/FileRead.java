package fileread;

import ucar.ma2.Array;
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
        String filename = "src/main/java/fileread/ocean_temp_salt.res.nc";
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
            //find variable
            String variable = "temp";
            Variable varBean = ncfile.findVariable(variable);
            //Reading data from a Variable
            if(null != varBean) {
                Array all = varBean.read();
                System.out.println("读取所有：\n"+NCdumpW.printArray(all, variable, null));
            }
            if(null != varBean) {
                int[] origin = new int[] {0,0,0,0};
                int[] size = new int[] {1,1,2,2};
                Array data2D = varBean.read(origin, size);
                System.out.println(NCdumpW.printArray(data2D, variable, null));
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
