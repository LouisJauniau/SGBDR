package up.mi.jgm.td3;

import java.util.Scanner;

public class SGBD {

    private DBConfig dbconfig;
    private DiskManager diskmanager;

    public SGBD(DBConfig dbconfig) {
        this.dbconfig = dbconfig;
        this.diskmanager = new DiskManager(dbconfig);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("Veuillez svp taper une commande: ");
            command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("QUIT")) {
                processQuitCommand();
                break;
            }

            parseAndProcessCommand(command);
        }

        scanner.close();
    }

    private void parseAndProcessCommand(String command) {
        // Analyse la commande pour déterminer de quel type elle est
        String[] parts = command.split("\\s+");
        String commandType = parts[0].toUpperCase();

        switch (commandType) {
            case "CREATE":
                if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLE")) {
                    processCreateTableCommand(command);
                } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASE")) {
                    processCreateDatabaseCommand(command);
                }
                break;
            case "DROP":
                if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLE")) {
                    processDropTableCommand(command);
                } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASES")) {
                    processDropDatabasesCommand(command);
                }
                break;
            case "SET":
                processSetDatabaseCommand(command);
                break;
            case "LIST":
                if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLES")) {
                    processListTablesCommand(command);
                } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASES")) {
                    processListDatabasesCommand(command);
                }
                break;
            default:
                System.out.println("Commande inconnue: " + command);
                break;
        }
    }

    private void processQuitCommand() {
        // Assurez-vous de sauvegarder et de vider les buffers
        saveState();
        flushBuffers();
        System.out.println("Exécution terminée.");
    }

    private void processCreateTableCommand(String command) {
        // Implémenter la logique pour créer une table
        System.out.println("Création de la table avec la commande: " + command);
    }

    private void processCreateDatabaseCommand(String command) {
        // Implémenter la logique pour créer une base de données
        System.out.println("Création de la base de données avec la commande: " + command);
    }

    private void processDropTableCommand(String command) {
        // Implémenter la logique pour supprimer une table
        System.out.println("Suppression de la table avec la commande: " + command);
    }

    private void processDropDatabasesCommand(String command) {
        // Implémenter la logique pour supprimer des bases de données
        System.out.println("Suppression des bases de données avec la commande: " + command);
    }

    private void processSetDatabaseCommand(String command) {
        // Implémenter la logique pour définir la base de données en cours
        System.out.println("Définir la base de données avec la commande: " + command);
    }

    private void processListTablesCommand(String command) {
        // Implémenter la logique pour lister les tables
        System.out.println("Liste des tables demandée avec la commande: " + command);
    }

    private void processListDatabasesCommand(String command) {
        // Implémenter la logique pour lister les bases de données
        System.out.println("Liste des bases de données demandée avec la commande: " + command);
    }

    private void saveState() {
        // Implémentez la logique de sauvegarde de l'état
        System.out.println("Sauvegarde de l'état.");
    }

    private void flushBuffers() {
        // Implémentez la logique de vidage des buffers
        System.out.println("Vidage des buffers.");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SGBD <cheminFichierDeConfiguration>");
            return;
        }

        String configFilePath = args[0];
        DBConfig dbconfig = new DBConfig(configFilePath);
        SGBD sgbd = new SGBD(dbconfig);
        sgbd.run();
    }
}