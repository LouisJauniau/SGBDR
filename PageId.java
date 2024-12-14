package up.mi.jgm.bdda;

import java.io.Serializable;

public class PageId implements Serializable {
    private static final long serialVersionUID = 1L; // Version de sérialisation

    private int fileIdx;
    private int pageIdx;

    // Constructeur
    public PageId(int fileIdx, int pageIdx) {
        this.fileIdx = fileIdx;
        this.pageIdx = pageIdx;
    }

    // Getters
    public int getFileIdx() {
        return fileIdx;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    // Méthode toString pour faciliter l'affichage
    @Override
    public String toString() {
        return "(" + fileIdx + ", " + pageIdx + ")";
    }

    // Méthode equals pour comparer deux PageId
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PageId pageId = (PageId) obj;
        return fileIdx == pageId.fileIdx && pageIdx == pageId.pageIdx;
    }

    // Méthode hashCode pour utiliser PageId comme clé dans une collection
    @Override
    public int hashCode() {
        return 31 * fileIdx + pageIdx;
    }
}
