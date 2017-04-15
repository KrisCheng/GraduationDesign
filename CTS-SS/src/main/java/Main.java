import algorithm.CTS_SS;

import java.io.IOException;

/**
 * Created by Kris Chan on 1:49 PM 15/04/2017 .
 * All right reserved.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        CTS_SS cts_ss = new CTS_SS(80, 10, 10, 40, 20, 10, 0.01);
        String filename = "src/main/java/data/sst_20.xlsx";
        cts_ss.init(filename);
        cts_ss.solution();
    }
}
