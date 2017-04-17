package tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Kris Chan on 3:11 PM 17/04/2017 .
 * All right reserved.
 */
public class ShellHelper {
    public static String exec(String cmd){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String results = "";
            while ((line = input.readLine()) != null) {
                results += line;
            }
            input.close();
            return results;
        } catch (Exception e) {
            System.out.println("blank");
            e.printStackTrace();
            return "error";
        }

    }
}
