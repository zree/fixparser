package fix;

import java.util.Arrays;

public class FIXGroupStructure {
    public int header;
    public int[] tagList;

    public FIXGroupStructure(int... tagList) {
        this.header = tagList[0];
        this.tagList = tagList;
        Arrays.sort(tagList);
    }
}
