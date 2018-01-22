package be.malbrecq.config.model;

import java.util.Map;

public class SyncConfig {
    private String containerName;
    private Boolean delete;
    private String[] directories;
    private String[] extraCommands;
    private String[] excludes;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Boolean getDelete() {
        return delete == null ? false : delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public String[] getDirectories() {
        return directories;
    }

    public void setDirectories(String[] directories) {
        this.directories = directories;
    }

    public String[] getExtraCommands() {
        return extraCommands;
    }

    public void setExtraCommands(String[] extraCommands) {
        this.extraCommands = extraCommands;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}
