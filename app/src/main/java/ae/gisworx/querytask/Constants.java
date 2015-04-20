package ae.gisworx.querytask;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.io.UserCredentials;

import java.util.List;

/**
 * Created by tmsbn on 4/20/15.
 */
public class Constants {

    public static final String QUERY_URL = "YOUR QUERYTASK";

    public static UserCredentials getUserCredentials() {


        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserAccount("username", "password");

        return userCredentials;
    }


    public static Envelope calculateEnvelope(List<Point> pointList) {

        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;

        double xMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE;

        for (Point point : pointList) {

            if (point.getX() < xMin)
                xMin = point.getX();

            if (point.getY() < yMin)
                yMin = point.getY();

            if (point.getX() > xMax)
                xMax = point.getX();

            if (point.getY() > yMax)
                yMax = point.getY();
        }

        return new Envelope(xMin, yMin, xMax, yMax);

    }
}
