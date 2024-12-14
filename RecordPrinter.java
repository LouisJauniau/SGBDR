package up.mi.jgm.bdda;

public class RecordPrinter {

    public static void printTousLesRecords(IRecordIterator it) {
        int count = 0;
        try {
            Record rec;
            while ((rec = it.getNextRecord()) != null) {
                System.out.println(formatRecord(rec));
                count++;
            }
        } finally {
            it.close();
        }
        System.out.println("Total records = " + count);
    }

    private static String formatRecord(Record record) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < record.getSize(); i++) {
            sb.append(record.getValue(i).getData());
            if (i < record.getSize() - 1) {
                sb.append(" ; ");
            }
        }
        sb.append(".");
        return sb.toString();
    }
}