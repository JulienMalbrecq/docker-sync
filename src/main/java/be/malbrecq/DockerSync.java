package be.malbrecq;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class DockerSync {
    private final String containerName;
    private WatchService watcher;

    public DockerSync (String containerName)
    {
        this.containerName = containerName;

        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean addWatchPath(String path)
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
                    System.out.format("Synchronizing %s%n", containerName);
                    sync();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sync() {
        try {
            String[] command = {"docker", "exec", containerName, "rsync", "-ravt", "/mnt/sources/","/var/www/html/","--exclude",".idea","--exclude",".git","--exclude","node/modules","--chown=www-data:www-data"};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            Process p = pb.start();

            p.waitFor();

            Reader result = p.exitValue() == 0 ? new InputStreamReader(p.getInputStream()) : new InputStreamReader(p.getErrorStream());
            BufferedReader reader = new BufferedReader(result);

            String line;
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
