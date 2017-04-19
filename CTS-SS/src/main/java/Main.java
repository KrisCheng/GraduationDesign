import algorithm.CTS_SS;
import algorithm.FILE_PATH;

import java.io.IOException;

/**
 * Created by Kris Chan on 1:49 PM 15/04/2017 .
 * All right reserved.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CTS_SS cts_ss = new CTS_SS(1, 10, 2, 2, 5, 1, 0.1);
        String filename = FILE_PATH.DATA_PATH+"sst_20.xlsx";
        cts_ss.init(filename);
        cts_ss.solution();
    }
}
