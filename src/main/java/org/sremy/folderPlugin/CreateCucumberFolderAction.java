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
    protected void doActionInFolder(List<Path> selectedFolder) {
        // Create a feature file in root folder
        selectedFolder.forEach(rootFolder -> {
            try {
                Files.createFile(rootFolder.resolve("cucumber.feature"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }


}
