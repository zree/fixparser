import exception.InvalidFIXMessageException;
import fix.FIXMessage;
import fix.FIXGroupStructure;
import utils.Constants;
import utils.FIXTags;

import java.util.Map;

public class FIXParser {
    private final InvalidFIXMessageException invalidFIXMessageException = new InvalidFIXMessageException();
    private final FIXMessage fixMsg;
    private final Map<Integer, FIXGroupStructure> repeatingGroupStructures;

    public FIXParser() {
        this(null);
    }

    public FIXParser(Map<Integer, FIXGroupStructure> repeatingGroupStructures) {
        this.fixMsg = new FIXMessage();
        this.repeatingGroupStructures = repeatingGroupStructures;
    }

    public FIXMessage parse(byte[] rawMsg) {
        fixMsg.init(rawMsg);
        int len = rawMsg.length;
        int i=0;
        int start = 0;
        int checkSum = 0;
        while(i < len) {
            int tagId = 0;
            int length = -1;
            int tmpSum = 0;
            byte c = rawMsg[i++];
            tmpSum += c;
            while(c!= Constants.EQUALS) {
                if(c <= Constants.NINE && c >= Constants.ZERO) {
                    tagId = tagId * 10 + (c - Constants.ZERO);
                } else {
                    throw invalidFIXMessageException.reset().append("FIX message with not numeric TagId: ").append(tagId).append(c).done();
                }
                c = rawMsg[i++];
                tmpSum += c;
            }
            int offset = i;
            while(c!=Constants.DELIMITER) {
                c = rawMsg[i++];
                tmpSum += c;
                length++;
            }
            if(tagId == FIXTags.BODYLEN.id()) {
                start = i;
            }
            if(tagId == FIXTags.CHECKSUM.id()) {
                fixMsg.addTag(tagId, offset, length);
                fixMsg.setCheckSum(checkSum % 256);
                fixMsg.setBodyLen(offset - 3 - start); // "10=" has 3 chars.
                fixMsg.validate();
                fixMsg.parseRepeatingGroup(repeatingGroupStructures);
                return fixMsg;
            }
            checkSum += tmpSum;
            fixMsg.addTag(tagId, offset, length);
        }
        return null;
    }
}
