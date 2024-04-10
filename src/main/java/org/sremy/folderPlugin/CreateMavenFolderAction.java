package org.sremy.folderPlugin;

import java.util.List;

public class CreateMavenFolderAction extends CreateFolderAction {

    @Override
    protected List<String> getListOfSubFolders() {
        return List.of("src", "src/main/java", "src/test/java", "src/main/resources", "src/test/resources");
    }

}
