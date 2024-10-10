package up.mi.jgm.td3;

public class PageId {
    private int FileIdx;
    private int PageIdx;

    // Constructeur qui prend en argument l'identifiant du fichier et l'indice de la page
    public PageId(int fileIdx, int pageIdx) {
        this.FileIdx = fileIdx;
        this.PageIdx = pageIdx;
    }

    // Getters et setters pour FileIdx et PageIdx
    public int getFileIdx() {
        return FileIdx;
    }

    public void setFileIdx(int fileIdx) {
        this.FileIdx = fileIdx;
    }

    public int getPageIdx() {
        return PageIdx;
    }

    public void setPageIdx(int pageIdx) {
        this.PageIdx = pageIdx;
    }

    // Méthode pour afficher le PageId sous forme de chaîne de caractères
    @Override
    public String toString() {
        return "PageId{" +
                "FileIdx=" + FileIdx +
                ", PageIdx=" + PageIdx +
                '}';
    }
}