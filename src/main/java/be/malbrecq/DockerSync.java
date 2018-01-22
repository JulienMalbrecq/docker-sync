package be.malbrecq;

import be.malbrecq.config.model.SyncConfig;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class DockerSync {
    private WatchService watcher;

    private SyncConfig config;

    private List<List<String>> commands;

    public DockerSync (SyncConfig config)
    {
        this.config = config;
        this.commands = new LinkedList<>();

        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        for (String dir: config.getDirectories()) {
            if (!addWatchPath(dir)) {
                System.out.format("Path %s doesn't exists or is not a directory, skipping%n", dir);
            }
        }

        buildCommands();
    }

    @SuppressWarnings("unchecked")
    public void start() {
        try {
            WatchKey watchKey;
            while (true) {
                System.out.println("Waiting for change ...");
                watchKey = watcher.take();

                System.out.println("Received change, buffering for 5 seconds ...");
                Thread.sleep(5000);

                if (watchKey != null) {
                    watchKey.pollEvents(); // remove all queued events
                    watchKey.reset();
                    System.out.format("Synchronizing %s%n", config.getContainerName());
                    sync();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void buildCommands() {
        // build sync command
        System.out.println("Building sync command");
        String[] baseCommand = {"docker", "exec", config.getContainerName(), "rsync", "-ravt", "/mnt/sources/", "/var/www/html/","--chown=www-data:www-data"};
        List<String> command = new LinkedList<>(Arrays.asList(baseCommand));

        System.out.format(
            "Delete is %s%n",
            config.getDelete() ? "TRUE" : "FALSE"
        );

        if (config.getDelete()) {
            command.add("--delete");
        }

        String[] excludes = config.getExcludes();
        if (excludes != null) {
            for (String exclude: excludes) {
                command.add("--exclude");
                command.add(exclude);
                System.out.format("Excluding directory %s%n", exclude);
            }
        } else {
            System.out.println("Not excluding file in the synchronization");
        }

        this.commands.add(command);

        // @todo build extra commands
    }

    private boolean addWatchPath(String path)
    {
        Path dir = Paths.get(path);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return false;
        } else {
            System.out.format("Monitoring directory %s%n", path);
            watchDirectory(dir);
            return true;
        }
    }

    private void sync() {
        for (List<String> command : commands) {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.inheritIO();
                Process p = pb.start();

                p.waitFor();

                Reader result = p.exitValue() == 0 ? new InputStreamReader(p.getInputStream()) : new InputStreamReader(p.getErrorStream());
                BufferedReader reader = new BufferedReader(result);

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("Synchronization is complete");
    }

    private void watchDirectory (Path directory)
    {
        try {
            // register events
            directory.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            // Monitor sub directories
            Files.list(directory).filter(path -> Files.isDirectory(path)).forEach(this::watchDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
