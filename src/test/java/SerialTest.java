import cc.carm.app.sensormaster.data.SerialData;
import org.junit.Test;

public class SerialTest {

    @Test
    public void test() {
//        01 06 00 3E 00 54 E9 F9
        SerialData data = SerialData.of(0x01, 0x06, 0x00, 0x3E, 0x00, 0x54);
        System.out.println(data);


    }

}
