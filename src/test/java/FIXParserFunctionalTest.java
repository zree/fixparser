import exception.InvalidFIXMessageException;
import fix.FIXGroup;
import fix.FIXGroupStructure;
import fix.FIXMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class FIXParserFunctionalTest extends FIXParserTestBase{

    @Test
    public void testBasicFIXMessage() {
        String fixMsg = "8=FIX.4.2|9=109|35=D|34=1|49=Broker1|52=20240813-10:15:30.123|56=Client1|11=Order123|21=1|55=AAPL|54=-1|38=100|40=2|44=50.25|10=175|";
        byte[] testFix = normalizeFIXMsg(fixMsg);
        FIXMessage msg = parser.parse(testFix);
        assert msg.getInt(9) == 109;
        assert msg.getSignedInt(9) == 109;
        assert msg.getSignedInt(54) == -1;
        assert msg.getByte(35) == 'D';
        assert msg.getString(8, sb).toString().equals("FIX.4.2");
        assert msg.getDouble(44) == 50.25;
        // DataType validation
        Assertions.assertThrows(InvalidFIXMessageException.class, () -> {
           msg.getInt(35);
        });
        Assertions.assertThrows(InvalidFIXMessageException.class, () -> {
            msg.getByte(8);
        });
        // CheckSum/BodyLen Validation
        Assertions.assertThrows(InvalidFIXMessageException.class, () -> {
            parser.parse(normalizeFIXMsg(fixMsg.replace("10=175", "10=130")));
        });
        Assertions.assertThrows(InvalidFIXMessageException.class, () -> {
            parser.parse(normalizeFIXMsg(fixMsg.replace("9=109", "9=130")));
        });

        // When there's no repeatingGroup structure
        FIXParser parser2 = new FIXParser();
        FIXMessage msg2 = parser2.parse(testFix);
        assert msg2.getString(52, sb).toString().equals("20240813-10:15:30.123");
        assert msg2.getString(55, sb).toString().equals("AAPL");
    }

    @Test
    public void testSingleLayerRepeatingGroup() {
        String fixMsg= "8=FIX.4.2|9=99|35=X|34=2|49=Broker1|52=20240813-10:30:45.678|56=Client1|55=AAPL|268=2|269=0|270=100|269=1|270=150|10=251|";
        byte[] testFix = normalizeFIXMsg(fixMsg);
        FIXMessage msg = parser.parse(testFix);
        FIXGroup fixGroup = msg.getRepeatingGroup(268);
        assert fixGroup.getSize() == 2;
        assert msg.getInt(fixGroup.getTag(0, 269)) == 0;
        assert msg.getSignedInt(fixGroup.getTag(0, 270)) == 100;
        assert msg.getInt(fixGroup.getTag(1, 269)) == 1;
        assert msg.getSignedInt(fixGroup.getTag(1, 270)) == 150;
        assert fixGroup.getTag(2, 269) == null;
        assert fixGroup.getTag(1, 290) == null;

        // When there's no repeatingGroup structure
        FIXParser parser2 = new FIXParser();
        FIXMessage msg2 = parser2.parse(testFix);
        assert msg2.getRepeatingGroup(268) == null;
    }

    @Test
    public void testMultiLayerRepeatingGroup() {
        String fixMsg = "8=FIX.4.2|9=162|35=X|34=4|49=Broker1|52=20240813-11:00:30.333|56=Client1|55=AAPL|268=2|269=0|270=100|279=2|280=50.25|281=10|280=51.22|281=13|269=1|270=150|279=1|280=55.75|281=21|10=100|";
        byte[] testFix = normalizeFIXMsg(fixMsg);
        FIXMessage msg = parser.parse(testFix);
        FIXGroup fixGroup = msg.getRepeatingGroup(268);
        assert fixGroup.getSize() == 2;
        FIXGroup innerGroup = fixGroup.getRepeatingGroup(0, 279);
        assert innerGroup.getSize() == 2;
        assert msg.getDouble(innerGroup.getTag(0, 280)) == 50.25;
        assert msg.getInt(innerGroup.getTag(0, 281)) == 10;
        assert msg.getDouble(innerGroup.getTag(1, 280)) == 51.22;
        assert msg.getInt(innerGroup.getTag(1, 281)) == 13;
        innerGroup = fixGroup.getRepeatingGroup(1, 279);
        assert innerGroup.getSize() == 1;
        assert msg.getDouble(innerGroup.getTag(0, 280)) == 55.75;
        assert msg.getInt(innerGroup.getTag(0, 281)) == 21;

        assert fixGroup.getRepeatingGroup(2, 279) == null;
        assert fixGroup.getRepeatingGroup(1, 280) == null;

        // When there's no repeatingGroup structure
        FIXParser parser2 = new FIXParser();
        FIXMessage msg2 = parser2.parse(testFix);
        assert msg2.getRepeatingGroup(268) == null;

        // When there's partial layer repeatingGroup structure
        FIXParser parser3 = new FIXParser(new HashMap<Integer, FIXGroupStructure>(){{
            put(268, new FIXGroupStructure(269, 270, 279, 280, 281));
        }});
        FIXMessage msg3 = parser3.parse(testFix);
        FIXGroup fixGroup3 = msg3.getRepeatingGroup(268);
        assert fixGroup3.getRepeatingGroup(0, 279) == null;
    }

}
