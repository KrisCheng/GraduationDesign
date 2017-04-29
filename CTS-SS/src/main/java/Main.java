import algorithm.CTS_SS;
import algorithm.FILE_PATH;

import java.io.IOException;

/**
 * Created by Kris Chan on 1:49 PM 15/04/2017 .
 * All right reserved.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CTS_SS cts_ss = new CTS_SS(1, 10, 2, 3, 5, 2, 1);
        String filename = FILE_PATH.DATA_PATH+"sst_20.xlsx";
        cts_ss.init(filename);//获取降为后数据矩阵
        cts_ss.solution();//求解过程
    }
}
