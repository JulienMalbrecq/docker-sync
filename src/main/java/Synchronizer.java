
import be.malbrecq.DockerSync;
import be.malbrecq.config.model.SyncConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Synchronizer {

    public static void main(String[] args) {
        SyncConfig config = new SyncConfig();

        if (args.length == 0) {
            String containerName;
            List<String> directories = new ArrayList<>();

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

            config.setContainerName(containerName);
            config.setDirectories(directories.toArray(new String[0]));

        } else {
//            if (args.length < 2) {
//                System.err.println("Error: Missing argument");
//                System.exit(1);
//            }
//
//            containerName = args[0];
//            for (int i = 1; i <= args.length - 1; i++) {
//                directories.add(args[i]);
//            }

            if (args.length < 1) {
                System.err.println("Error: Missing argument");
                System.exit(1);
            }

            try {
                config = getConfig(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error while loading configuration file");
                System.exit(1);

            }
        }

        DockerSync synchronizer = new DockerSync(config);
        synchronizer.start();
    }

    private static SyncConfig getConfig(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(path), SyncConfig.class);
    }
}
