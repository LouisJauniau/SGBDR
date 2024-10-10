package up.mi.jgm.td3;

import java.io.IOException;
import java.util.Scanner;
import up.mi.jgm.td3.DBConfig;
import up.mi.jgm.td3.DiskManager;
import up.mi.jgm.td3.BufferManager;
import up.mi.jgm.td3.PageId;

public class MainApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DBConfig config = null;
        DiskManager diskManager = null;
        BufferManager bufferManager = null;

        // Chargement de la configuration à partir du fichier
        config = DBConfig.LoadDBConfig("config.txt");
        if (config == null) {
            System.out.println("Failed to load config.");
            System.exit(1);
        }
        System.out.println("Database path: " + config.getDbpath());
        System.out.println("Page size: " + config.getPagesize());
        System.out.println("DM Max File Size: " + config.getDm_maxfilesize());
        System.out.println("Buffer count: " + config.getBm_buffercount());
        System.out.println("Buffer Manager Policy: " + config.getBm_policy());

        // Initialisation de DiskManager et BufferManager
        diskManager = new DiskManager(config);
        bufferManager = new BufferManager(config, diskManager);

        // Boucle de commandes
        while (true) {
            System.out.print("Tapez votre commande (svp): ");
            String command = scanner.nextLine().trim().toUpperCase();

            switch (command) {
                case "EXIT":
                    System.out.println("Exiting application...");
                    try {
                        bufferManager.FlushBuffers();
                    } catch (IOException e) {
                        System.out.println("Error while flushing buffers: " + e.getMessage());
                    }
                    diskManager.SaveState();
                    break;
                case "ALLOCATE_PAGE":
                    PageId newPage = diskManager.AllocPage();
                    System.out.println("Allocated new page: FileIdx=" + newPage.getFileIdx() + ", PageIdx=" + newPage.getPageIdx());
                    break;
                case "DEALLOCATE_PAGE":
                    System.out.print("Enter FileIdx: ");
                    int fileIdx = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter PageIdx: ");
                    int pageIdx = Integer.parseInt(scanner.nextLine());
                    diskManager.DeallocPage(new PageId(fileIdx, pageIdx));
                    System.out.println("Deallocated page: FileIdx=" + fileIdx + ", PageIdx=" + pageIdx);
                    break;
                case "GET_PAGE":
                    System.out.print("Enter FileIdx: ");
                    fileIdx = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter PageIdx: ");
                    pageIdx = Integer.parseInt(scanner.nextLine());
                    try {
                        byte[] pageData = bufferManager.GetPage(new PageId(fileIdx, pageIdx));
                        System.out.println("Retrieved page data: " + new String(pageData));
                    } catch (IOException e) {
                        System.out.println("Error retrieving page: " + e.getMessage());
                    }
                    break;
                case "FREE_PAGE":
                    System.out.print("Enter FileIdx: ");
                    fileIdx = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter PageIdx: ");
                    pageIdx = Integer.parseInt(scanner.nextLine());
                    System.out.print("Is the page dirty (true/false): ");
                    boolean isDirty = Boolean.parseBoolean(scanner.nextLine());
                    bufferManager.FreePage(new PageId(fileIdx, pageIdx), isDirty);
                    System.out.println("Freed page: FileIdx=" + fileIdx + ", PageIdx=" + pageIdx);
                    break;
                case "SET_REPLACEMENT_POLICY":
                    System.out.print("Enter new replacement policy: ");
                    String policy = scanner.nextLine();
                    bufferManager.SetCurrentReplacementPolicy(policy);
                    System.out.println("Replacement policy set to: " + policy);
                    break;
                default:
                    System.out.println("Commande non reconnue: " + command);
            }

            if (command.equals("EXIT")) {
                break;
            }
        }

        scanner.close();
    }
}
