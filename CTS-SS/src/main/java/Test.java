import Jama.Matrix;
import algorithm.FILE_PATH;
import tool.FileHelper;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kris Chan on 9:06 AM 24/04/2017 .
 * All right reserved.
 */
public class Test {

    public static void main(String args[]) {
        FileOutputStream out = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        try {

            out = new FileOutputStream(new File("src/main/java/test.txt"));

            long begin = System.currentTimeMillis();

            for (int i = 0; i < 10; i++) {

                out.write(("hello world"+" "+df.format(new Date())+'\n').getBytes());

            }
            out.close();
        }catch (Exception e) {

            e.printStackTrace();

        }
    }
}
