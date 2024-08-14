import org.junit.jupiter.api.Test;

public class FIXParserPerformanceTest extends FIXParserTestBase {

    @Test
    public void singleLayerSpeedTest() {
        String fixMsg = "8=FIX.4.2|9=109|35=D|34=1|49=Broker1|52=20240813-10:15:30.123|56=Client1|11=Order123|21=1|55=AAPL|54=-1|38=100|40=2|44=50.25|10=175|";
        byte[] testFix = normalizeFIXMsg(fixMsg);
        int times = 1_000_000;
        long start = System.currentTimeMillis();
        for(int i=0; i<times; i++) {
            parser.parse(testFix);
        }
        System.out.printf("Latency for parse a single layer 100-byte FIX message for %d times: %d ms\n", times, (System.currentTimeMillis() - start));
    }

    @Test
    public void multiLayerSpeedTest() {
        String fixMsg = "8=FIX.4.2|9=162|35=X|34=4|49=Broker1|52=20240813-11:00:30.333|56=Client1|55=AAPL|268=2|269=0|270=100|279=2|280=50.25|281=10|280=51.22|281=13|269=1|270=150|279=1|280=55.75|281=21|10=100|";
        byte[] testFix = normalizeFIXMsg(fixMsg);
        int times = 1_000_000;
        long start = System.currentTimeMillis();
        for(int i=0; i<times; i++) {
            parser.parse(testFix);
        }
        System.out.printf("Latency for parse a 3-layer 160-byte FIX message for %d times: %d ms\n", times, (System.currentTimeMillis() - start));
    }

}
