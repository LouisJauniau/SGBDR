package up.mi.jgm.td3;

public class RecordId {
    private int pageId;
    private int slotIdx;

    public RecordId(int pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }
}