package up.mi.jgm.td3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RelationDataTest {
    public static void main(String[] args) {
        try {
            // Charger la configuration
            DBConfig config = DBConfig.loadDBConfig("config.json");

            // Créer une instance de DiskManager
            DiskManager dm = new DiskManager(config);
            dm.LoadState();

            // Créer une instance de BufferManager
            BufferManager bm = new BufferManager(config, dm);

            // Définir le schéma de la relation
            List<ColInfo> columns = new ArrayList<>();
            columns.add(new ColInfo("id", ColInfo.Type.INT, 0));
            columns.add(new ColInfo("name", ColInfo.Type.CHAR, 10));
            columns.add(new ColInfo("age", ColInfo.Type.INT, 0));
            columns.add(new ColInfo("description", ColInfo.Type.VARCHAR, 50));

            // Créer la relation
            Relation relation = new Relation("Person", columns, dm, bm);

            // Insérer des records
            Record record1 = new Record();
            record1.addValue(new Value(Value.Type.INT, 1));
            record1.addValue(new Value(Value.Type.CHAR, "Alice"));
            record1.addValue(new Value(Value.Type.INT, 30));
            record1.addValue(new Value(Value.Type.VARCHAR, "Software Engineer"));

            Record record2 = new Record();
            record2.addValue(new Value(Value.Type.INT, 2));
            record2.addValue(new Value(Value.Type.CHAR, "Bob"));
            record2.addValue(new Value(Value.Type.INT, 28));
            record2.addValue(new Value(Value.Type.VARCHAR, "Data Scientist"));

            RecordId rid1 = relation.InsertRecord(record1);
            System.out.println("Record 1 inséré avec RecordId : " + rid1);

            RecordId rid2 = relation.InsertRecord(record2);
            System.out.println("Record 2 inséré avec RecordId : " + rid2);

            // Récupérer tous les records
            List<Record> allRecords = relation.GetAllRecords();
            System.out.println("Tous les records de la relation :");
            for (Record rec : allRecords) {
                for (Value val : rec.getValues()) {
                    System.out.print(val.getData() + " ");
                }
                System.out.println();
            }

            // Sauvegarder l'état du DiskManager
            dm.SaveState();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}