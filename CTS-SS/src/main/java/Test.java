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
        double [][]a = new double[10][10];
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                a[i][j] = i*10+j+1;
                System.out.print(a[i][j]+" ");
            }
            System.out.println();
        }
        Matrix test = new Matrix(a);
        System.out.println(test.get(2,2));
    }
}
