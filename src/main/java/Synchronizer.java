
import be.malbrecq.DockerSync;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class Synchronizer {

    public static void main(String[] args) {
        String containerName;
        List<String> directories = new ArrayList<>();

        if (args.length == 0) {
            // start interactive console
            Console console = System.console();
            containerName = console.readLine("Container name: ");

            String dirEntry = console.readLine("Directory to monitor (leave blank to finish): ");
            if (dirEntry.isEmpty()) {
                System.err.println("Error: You must add at least one directory to monitor");
                System.exit(1);
            }

            directories.add(dirEntry);

            while (!dirEntry.isEmpty()) {
                dirEntry = console.readLine("Directory to monitor (leave blank to finish): ");
                if (!dirEntry.isEmpty()) {
                    directories.add(dirEntry);
                }
            }

        } else {
            if (args.length < 2) {
                System.err.println("Error: Missing argument");
                System.exit(1);
            }

            containerName = args[0];
            for (int i = 1; i <= args.length - 1; i++) {
                directories.add(args[i]);
            }
        }

        DockerSync synchronizer = new DockerSync(containerName);

        for (String dir: directories) {
            if (!synchronizer.addWatchPath(dir)) {
                System.out.format("Path %s doesn't exists or is not a directory, skipping%n", dir);
            }
        }

        synchronizer.start();
    }
}
