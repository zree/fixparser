package utils;

public enum FIXTags {
    FIX_VERSION(8),
    BODYLEN(9),
    CHECKSUM(10),
    MSGTYPE(35);


    private final int id;
    private final String name;
    private FIXTags(int id) {
        this.id = id;
        this.name = super.name();
    }

    public int id(){
        return id;
    }
}
