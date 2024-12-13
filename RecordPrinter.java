package up.mi.jgm.td3;

public class RecordPrinter {

    public static void printTousLesRecords(IRecordIterator it) {
        int nombreRecords = 0;
        try {
            Record rec;
            while ((rec = it.getNextRecord()) != null) {
                System.out.println(formatRecord(rec));
                nombreRecords++;
            }
        } finally {
            // On ferme l'itérateur, ce qui signale qu'on a terminé
            it.close();
        }

        System.out.println("Total records = " + nombreRecords);
    }

    private static String formatRecord(Record record) {
        // Construit la ligne à afficher pour le record
        // Valeurs séparées par " ; " et terminer par un "."
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
