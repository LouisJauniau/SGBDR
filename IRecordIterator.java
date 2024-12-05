package up.mi.jgm.td3;

public interface IRecordIterator {


    /*
    retourne le record courant et « avance d’un cran le curseur de l’itérateur ».
    Ou retourne null si plus de record dans l'ensemble de tuples.
     */
    Record getNextRecord();


    //Iterateur plus utilisé
    void close();


    //Remet le curseur au debut des record à parcourir.
    void reset();

}
