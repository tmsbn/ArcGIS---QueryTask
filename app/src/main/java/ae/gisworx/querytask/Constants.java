package ae.gisworx.querytask;

import com.esri.core.io.UserCredentials;

/**
 * Created by tmsbn on 4/20/15.
 */
public class Constants {



    public static final String QUERY_URL = "YOUR QUERY_TASK URL";

    public static UserCredentials getUserCredentials() {


        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserAccount("username", "password");

        return userCredentials;
    }


}
