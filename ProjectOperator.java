package up.mi.jgm.td3;

import java.util.List;

public class ProjectOperator implements IRecordIterator {

    private IRecordIterator it;    // L'iteration défini par l'interface
    private List<Integer> colonnes;    // Liste indices de colonnes à garder

    public ProjectOperator(IRecordIterator it, List<Integer> colonnes) {
        this.it = it;
        this.colonnes = colonnes;
    }

    @Override
    public Record getNextRecord() {
        // Récupère le prochain record
        Record recordOriginel = it.getNextRecord();
        if (recordOriginel == null) {
            return null; // Plus de record
        }

        // Construit nouveau record avec uniquement les colonnes voulues
        Record recordProjete = new Record();  //record projeté
        for (Integer colIndex : colonnes) {
            recordProjete.addValue(recordOriginel.getValue(colIndex));
        }

        return recordProjete;
    }

    // Ferme itérateur
    @Override
    public void close() {

        it.close();
    }

    // Réinitialise itérateur
    @Override
    public void reset() {

        it.reset();
    }
}
