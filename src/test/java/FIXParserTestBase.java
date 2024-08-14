import fix.FIXGroupStructure;
import utils.Constants;

import java.util.HashMap;

public class FIXParserTestBase {

    protected final FIXParser parser = new FIXParser(new HashMap<Integer, FIXGroupStructure>(){{
        put(268, new FIXGroupStructure(269, 270, 279,280,281));
        put(279, new FIXGroupStructure(280, 281));
    }});

    protected final StringBuilder sb = new StringBuilder(100);

    protected byte[] normalizeFIXMsg(String fixMessage) {
        fixMessage = fixMessage.replace('|', (char) Constants.DELIMITER);
        return fixMessage.getBytes();
    }
}
