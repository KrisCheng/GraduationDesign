package shh.connect;

import com.jcraft.jsch.UserInfo;

/**
 * Created by Kris Chan on 11:33 AM 30/03/2017 .
 * All right reserved.
 */
public class MyUserInfo implements UserInfo {
    private String password;

    private String passphrase;

    public String getPassphrase()
    {
        System.out.println("MyUserInfo.getPassphrase()");
        return null;
    }

    public String getPassword() {
        System.out.println("MyUserInfo.getPassword()");
        return null;
    }

    public boolean promptPassphrase(final String arg0) {
        System.out.println("MyUserInfo.promptPassphrase()");
        System.out.println(arg0);
        return false;
    }

    public boolean promptPassword(final String arg0) {
        System.out.println("MyUserInfo.promptPassword()");
        System.out.println(arg0);
        return false;
    }

    public boolean promptYesNo(final String arg0) {
        System.out.println("MyUserInfo.promptYesNo()");
        System.out.println(arg0);
        if (arg0.contains("The authenticity of host")) {
            return true;
        }
        return false;
    }

    public void showMessage(final String arg0) {
        System.out.println("MyUserInfo.showMessage()");
    }
}
