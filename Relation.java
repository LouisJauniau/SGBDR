package up.mi.jgm.bdda;

import java.io.IOException;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Relation implements Serializable {
    private static final long serialVersionUID = 1L;
    // Variables existantes
    private String name;
    private int columnCount;
    private List<ColInfo> columns;

    // Nouvelles variables membres
    private PageId headerPageId;
    private DiskManager diskManager;
    private BufferManager bufferManager;

    // Constructeur modifié
    public Relation(String name, List<ColInfo> columns, DiskManager diskManager, BufferManager bufferManager) throws IOException {
        this.name = name;
        this.columns = columns;
        this.columnCount = columns.size();
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;

        // Initialisation de la Header Page
        initializeHeaderPage();
    }

    // Méthode pour initialiser la Header Page
    private void initializeHeaderPage() throws IOException {
        // Allouer une page pour la Header Page
        this.headerPageId = diskManager.AllocPage();

        // Obtenir le buffer de la page via le BufferManager
        ByteBuffer buffer = bufferManager.GetPage(headerPageId);

        // Initialiser la Header Page avec N = 0 (aucune page de données)
        buffer.putInt(0); // N = 0

        // Indiquer que la page a été modifiée lors de la libération
        bufferManager.FreePage(headerPageId, true);
    }

    // Getters pour les nouvelles variables
    public PageId getHeaderPageId() {
        return headerPageId;
    }

    public String getName() {
        return name;
    }

    public List<ColInfo> getColumns() {
        return columns;
    }
    
    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    // Méthode pour ajouter une page de données
    public void addDataPage() throws IOException {
        // Allouer une nouvelle page via le DiskManager
        PageId dataPageId = diskManager.AllocPage();

        // Obtenir le buffer de la nouvelle page via le BufferManager
        ByteBuffer dataBuffer = bufferManager.GetPage(dataPageId);

        // Initialiser la page de données
        // Écrire le PageId de la page de données au début
        dataBuffer.putInt(dataPageId.getFileIdx());
        dataBuffer.putInt(dataPageId.getPageIdx());

        // Position du curseur après le PageId
        int freeSpacePosition = dataBuffer.position();

        // Initialiser le Slot Directory à la fin de la page
        int pageSize = diskManager.getConfig().getPageSize();
        dataBuffer.position(pageSize - 8); // 8 octets pour freeSpaceOffset et M
        dataBuffer.putInt(freeSpacePosition); // freeSpaceOffset
        dataBuffer.putInt(0); // M = 0

        // Indiquer que la page a été modifiée lors de la libération
        bufferManager.FreePage(dataPageId, true);

        // Mettre à jour la Header Page
        updateHeaderPage(dataPageId, pageSize - freeSpacePosition);
    }

    private void updateHeaderPage(PageId dataPageId, int freeSpace) throws IOException {
        // Obtenir le buffer de la Header Page via le BufferManager
        ByteBuffer headerBuffer = bufferManager.GetPage(headerPageId);

        // Lire N
        int N = headerBuffer.getInt(0);

        // Se positionner pour ajouter la nouvelle entrée
        headerBuffer.position(4 + N * (8 + 4)); // Chaque entrée : PageId (8 octets) + freeSpace (4 octets)

        // Ajouter le nouveau PageId
        headerBuffer.putInt(dataPageId.getFileIdx());
        headerBuffer.putInt(dataPageId.getPageIdx());

        // Ajouter le freeSpace
        headerBuffer.putInt(freeSpace);

        // Incrémenter N
        N++;
        headerBuffer.putInt(0, N);

        // Indiquer que la page a été modifiée lors de la libération
        bufferManager.FreePage(headerPageId, true);
    }

    // Méthode pour trouver une page de données avec assez d'espace
    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        // Obtenir le buffer de la Header Page via le BufferManager
        ByteBuffer headerBuffer = bufferManager.GetPage(headerPageId);

        // Lire N
        int N = headerBuffer.getInt(0);

        // Parcourir les N entrées
        int position = 4;
        for (int i = 0; i < N; i++) {
            headerBuffer.position(position);
            int fileIdx = headerBuffer.getInt();
            int pageIdx = headerBuffer.getInt();
            int freeSpace = headerBuffer.getInt();

            if (freeSpace >= sizeRecord + 8) { // 8 octets pour l'entrée dans le Slot Directory
                bufferManager.FreePage(headerPageId, false);
                return new PageId(fileIdx, pageIdx);
            }
            position += 12; // Passer à l'entrée suivante
        }

        bufferManager.FreePage(headerPageId, false);
        return null; // Aucune page avec assez d'espace trouvée
    }

    // Méthode pour écrire un record dans une page de données
    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        // Obtenir le buffer de la page de données via le BufferManager
        ByteBuffer pageBuffer = bufferManager.GetPage(pageId);

        // Lire le PageId au début (déjà connu, peut être ignoré ici)
        pageBuffer.position(8); // Sauter les 8 premiers octets

        // Récupérer la position de l'espace libre et M depuis le Slot Directory
        int pageSize = diskManager.getConfig().getPageSize();
        pageBuffer.position(pageSize - 8);
        int freeSpaceOffset = pageBuffer.getInt();
        int M = pageBuffer.getInt();

        // Écrire le record à la position freeSpaceOffset
        pageBuffer.position(freeSpaceOffset);
        int bytesWritten = writeRecordToBuffer(record, pageBuffer, freeSpaceOffset);

        // Mettre à jour le Slot Directory
        // Ajouter une nouvelle entrée
        int slotPosition = pageSize - 8 - M * 8;
        pageBuffer.position(slotPosition);
        pageBuffer.putInt(freeSpaceOffset); // Position du record
        pageBuffer.putInt(bytesWritten);    // Taille du record

        // Incrémenter M
        M++;
        pageBuffer.position(pageSize - 4);
        pageBuffer.putInt(M);

        // Mettre à jour freeSpaceOffset
        freeSpaceOffset += bytesWritten;
        pageBuffer.position(pageSize - 8);
        pageBuffer.putInt(freeSpaceOffset);

        // Indiquer que la page a été modifiée lors de la libération
        bufferManager.FreePage(pageId, true);

        // Mettre à jour le freeSpace dans la Header Page
        updateFreeSpaceInHeaderPage(pageId, pageSize - freeSpaceOffset - 8 - M * 8);

        // Retourner le RecordId
        return new RecordId(pageId, M - 1); // M-1 car les slots commencent à l'indice 0
    }

    private void updateFreeSpaceInHeaderPage(PageId pageId, int freeSpace) throws IOException {
        // Obtenir le buffer de la Header Page via le BufferManager
        ByteBuffer headerBuffer = bufferManager.GetPage(headerPageId);

        // Lire N
        int N = headerBuffer.getInt(0);

        // Parcourir les N entrées pour trouver la page
        int position = 4;
        for (int i = 0; i < N; i++) {
            headerBuffer.position(position);
            int fileIdx = headerBuffer.getInt();
            int pageIdx = headerBuffer.getInt();
            int idxFreeSpace = headerBuffer.position();

            if (fileIdx == pageId.getFileIdx() && pageIdx == pageId.getPageIdx()) {
                // Mettre à jour le freeSpace
                headerBuffer.putInt(freeSpace);
                break;
            }
            position += 12; // Passer à l'entrée suivante
        }

        // Indiquer que la page a été modifiée lors de la libération
        bufferManager.FreePage(headerPageId, true);
    }

    // Méthode pour obtenir les records dans une page de données
    public List<Record> getRecordsInDataPage(PageId pageId) throws IOException {
        List<Record> records = new ArrayList<>();

        // Obtenir le buffer de la page de données via le BufferManager
        ByteBuffer pageBuffer = bufferManager.GetPage(pageId);

        // Sauter le PageId
        pageBuffer.position(8);

        // Récupérer M depuis le Slot Directory
        int pageSize = diskManager.getConfig().getPageSize();
        pageBuffer.position(pageSize - 4);
        int M = pageBuffer.getInt();

        // Parcourir le Slot Directory
        for (int i = 0; i < M; i++) {
            int slotPosition = pageSize - 8 - (i + 1) * 8;
            pageBuffer.position(slotPosition);
            int recordOffset = pageBuffer.getInt();
            int recordSize = pageBuffer.getInt();

            if (recordSize > 0) { // Si le record n'est pas supprimé
                // Lire le record
                pageBuffer.position(recordOffset);
                Record record = new Record();
                readFromBuffer(record, pageBuffer, recordOffset);
                records.add(record);
            }
        }

        // Libérer la page
        bufferManager.FreePage(pageId, false);

        return records;
    }

    // Méthode pour obtenir toutes les pages de données
    public List<PageId> getDataPages() throws IOException {
        List<PageId> pageIds = new ArrayList<>();

        // Obtenir le buffer de la Header Page via le BufferManager
        ByteBuffer headerBuffer = bufferManager.GetPage(headerPageId);

        // Lire N
        int N = headerBuffer.getInt(0);

        // Parcourir les N entrées
        int position = 4;
        for (int i = 0; i < N; i++) {
            headerBuffer.position(position);
            int fileIdx = headerBuffer.getInt();
            int pageIdx = headerBuffer.getInt();
            headerBuffer.getInt(); // Sauter freeSpace
            pageIds.add(new PageId(fileIdx, pageIdx));
            position += 12; // Passer à l'entrée suivante
        }

        // Libérer la page
        bufferManager.FreePage(headerPageId, false);

        return pageIds;
    }

    // Méthode pour insérer un record
    public RecordId InsertRecord(Record record) throws IOException {
        // Calculer la taille du record
        int recordSize = calculateRecordSize(record);

        // Trouver une page avec assez d'espace
        PageId dataPageId = getFreeDataPageId(recordSize);
        if (dataPageId == null) {
            // Aucune page disponible, en ajouter une nouvelle
            addDataPage();
            dataPageId = getFreeDataPageId(recordSize);
            if (dataPageId == null) {
                throw new IOException("Impossible d'allouer une nouvelle page de données.");
            }
        }

        // Écrire le record dans la page de données
        RecordId rid = writeRecordToDataPage(record, dataPageId);
        return rid;
    }

    private int calculateRecordSize(Record record) {
        // Calculer la taille du record en utilisant writeRecordToBuffer avec un ByteBuffer temporaire
        ByteBuffer tempBuffer = ByteBuffer.allocate(1024); // Taille arbitraire
        int size = writeRecordToBuffer(record, tempBuffer, 0);
        return size;
    }

    // Méthode pour obtenir tous les records de la relation
    public List<Record> GetAllRecords() throws IOException {
        List<Record> allRecords = new ArrayList<>();
        List<PageId> dataPages = getDataPages();

        for (PageId pageId : dataPages) {
            List<Record> recordsInPage = getRecordsInDataPage(pageId);
            allRecords.addAll(recordsInPage);
        }

        return allRecords;
    }

    // Méthode pour écrire un record dans un buffer
    public int writeRecordToBuffer(Record record, ByteBuffer buff, int pos) {
        int initialPos = pos;
        buff.position(pos);

        if (!hasVarchar()) {
            // Format à taille fixe
            for (int i = 0; i < columnCount; i++) {
                ColInfo col = columns.get(i);
                Value value = record.getValue(i);
                writeValueToBuffer(value, col, buff);
            }
        } else {
            // Format à taille variable avec offset directory
            int[] offsets = new int[columnCount + 1];
            int offsetPos = buff.position();
            // Réserver l'espace pour l'offset directory
            buff.position(buff.position() + (columnCount + 1) * Integer.BYTES);

            int dataStartPos = buff.position();

            for (int i = 0; i < columnCount; i++) {
                offsets[i] = buff.position() - dataStartPos;
                ColInfo col = columns.get(i);
                Value value = record.getValue(i);
                writeValueToBuffer(value, col, buff);
            }

            offsets[columnCount] = buff.position() - dataStartPos;

            // Écrire l'offset directory
            int currentPos = buff.position();
            buff.position(offsetPos);
            for (int offset : offsets) {
                buff.putInt(offset);
            }
            buff.position(currentPos);
        }

        return buff.position() - initialPos;
    }

    // Méthode pour lire un record depuis un buffer
    public int readFromBuffer(Record record, ByteBuffer buff, int pos) {
        int initialPos = pos;
        buff.position(pos);

        if (!hasVarchar()) {
            // Format à taille fixe
            for (int i = 0; i < columnCount; i++) {
                ColInfo col = columns.get(i);
                Value value = readValueFromBuffer(col, buff);
                record.addValue(value);
            }
        } else {
            // Format à taille variable avec offset directory
            int[] offsets = new int[columnCount + 1];
            for (int i = 0; i <= columnCount; i++) {
                offsets[i] = buff.getInt();
            }

            int dataStartPos = buff.position();

            for (int i = 0; i < columnCount; i++) {
                int valueStart = dataStartPos + offsets[i];
                int valueEnd = dataStartPos + offsets[i + 1];
                int length = valueEnd - valueStart;

                buff.position(valueStart);
                ColInfo col = columns.get(i);
                Value value = readValueFromBuffer(col, buff, length);
                record.addValue(value);
            }
        }

        return buff.position() - initialPos;
    }

    // Méthode pour écrire une valeur dans le buffer
    private void writeValueToBuffer(Value value, ColInfo col, ByteBuffer buff) {
        switch (col.getType()) {
            case INT:
                buff.putInt((Integer) value.getData());
                break;
            case REAL:
                buff.putFloat((Float) value.getData());
                break;
            case CHAR:
                String charData = (String) value.getData();
                charData = padRight(charData, col.getSize());
                buff.put(charData.getBytes());
                break;
            case VARCHAR:
                String varcharData = (String) value.getData();
                buff.put(varcharData.getBytes());
                break;
        }
    }

    // Méthode pour lire une valeur depuis le buffer
    private Value readValueFromBuffer(ColInfo col, ByteBuffer buff) {
        switch (col.getType()) {
            case INT:
                int intValue = buff.getInt();
                return new Value(Value.Type.INT, intValue);
            case REAL:
                float floatValue = buff.getFloat();
                return new Value(Value.Type.REAL, floatValue);
            case CHAR:
                byte[] charBytes = new byte[col.getSize()];
                buff.get(charBytes);
                String charData = new String(charBytes).trim();
                return new Value(Value.Type.CHAR, charData);
            case VARCHAR:
                // Devrait être géré dans la méthode avec length
            default:
                return null;
        }
    }

    // Surcharge de readValueFromBuffer pour VARCHAR avec longueur
    private Value readValueFromBuffer(ColInfo col, ByteBuffer buff, int length) {
        switch (col.getType()) {
            case VARCHAR:
                byte[] varcharBytes = new byte[length];
                buff.get(varcharBytes);
                String varcharData = new String(varcharBytes);
                return new Value(Value.Type.VARCHAR, varcharData);
            default:
                // Appeler la méthode sans longueur pour les autres types
                return readValueFromBuffer(col, buff);
        }
    }

    // Méthode pour ajouter des espaces à droite d'une chaîne
    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    // Méthode pour vérifier si la relation contient des VARCHAR
    private boolean hasVarchar() {
        for (ColInfo col : columns) {
            if (col.getType() == ColInfo.Type.VARCHAR) {
                return true;
            }
        }
        return false;
    }
}
