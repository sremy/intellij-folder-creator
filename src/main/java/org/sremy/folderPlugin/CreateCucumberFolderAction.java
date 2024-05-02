package org.sremy.folderPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CreateCucumberFolderAction extends CreateFolderAction {

    @Override
    protected List<String> getListOfSubFolders() {
        return List.of("inputs", "mocks", "output");
    }

    @Override
    protected void doActionInFolder(List<Path> selectedFolder) throws IOException {
        // Create a feature file in root folder
        for (Path rootFolder : selectedFolder) {
            Files.createFile(rootFolder.resolve("cucumber.feature"));
        }
    }
}
