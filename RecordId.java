package up.mi.jgm.bdda;

public class RecordId {
    private PageId pageId;
    private int slotIdx;

    // Constructeur
    public RecordId(PageId pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }

    // Getters
    public PageId getPageId() {
        return pageId;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    // Méthode equals pour comparer deux RecordId
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecordId recordId = (RecordId) obj;
        return slotIdx == recordId.slotIdx && pageId.equals(recordId.pageId);
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return 31 * pageId.hashCode() + slotIdx;
    }

    // Méthode toString pour affichage
    @Override
    public String toString() {
        return "RecordId{" + "pageId=" + pageId + ", slotIdx=" + slotIdx + '}';
    }
}
