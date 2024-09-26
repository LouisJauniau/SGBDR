package up.mi.jgm.td1;

import java.io.*;
import java.util.*;

public class DiskManager {
    private DBConfig config;
    private List<PageId> freePages;
    private Map<Integer, RandomAccessFile> openFiles;
    private int nextFileIdx;

    public DiskManager(DBConfig config) {
        this.config = config;
        this.freePages = new ArrayList<>();
        this.openFiles = new HashMap<>();
        this.nextFileIdx = 0;
    }

    public PageId AllocPage() throws IOException {
        if (!freePages.isEmpty()) {
            return freePages.remove(freePages.size() - 1);
        }

        int fileIdx = nextFileIdx;
        RandomAccessFile file = openFiles.get(fileIdx);
        if (file == null || file.length() >= config.getDm_maxfilesize()) {
            fileIdx = nextFileIdx++;
            file = new RandomAccessFile(config.getDbpath() + "/F" + fileIdx, "rw");
            openFiles.put(fileIdx, file);
        }

        long pageIdx = file.length() / config.getPagesize();
        file.setLength(file.length() + config.getPagesize());

        return new PageId(fileIdx, (int) pageIdx);
    }

    public void ReadPage(PageId pageId, byte[] buff) throws IOException {
        RandomAccessFile file = openFiles.get(pageId.getFileIdx());
        if (file == null) {
            file = new RandomAccessFile(config.getDbpath() + "/F" + pageId.getFileIdx(), "r");
            openFiles.put(pageId.getFileIdx(), file);
        }

        file.seek((long) pageId.getPageIdx() * config.getPagesize());
        file.read(buff);
    }

    public void WritePage(PageId pageId, byte[] buff) throws IOException {
        RandomAccessFile file = openFiles.get(pageId.getFileIdx());
        if (file == null) {
            file = new RandomAccessFile(config.getDbpath() + "/F" + pageId.getFileIdx(), "rw");
            openFiles.put(pageId.getFileIdx(), file);
        }

        file.seek((long) pageId.getPageIdx() * config.getPagesize());
        file.write(buff);
    }

    public void DeallocPage(PageId pageId) {
        freePages.add(pageId);
    }

    public void SaveState() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(config.getDbpath() + "/dm.save"))) {
            out.writeObject(freePages);
        }
    }

    public void LoadState() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(config.getDbpath() + "/dm.save"))) {
            freePages = (List<PageId>) in.readObject();
        }
    }
}
