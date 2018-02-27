package fileSystem;

import java.io.File;

public class fsOperation {

    public void createDirectories(File dir){
        if (!dir.exists()){
            if (dir.mkdirs()){
                System.out.println("Directory created");
            } else {
                System.out.println("Directory not created");
            }
        }
    }

    public void deleteDirectory(File dir){
        String[] entries = dir.list();
        if(entries.length != 0){ //the directory dir isn't empty
            for(String s: entries){
                File currentFile = new File(dir.getPath(), s);
                currentFile.delete();
            }
        }
        dir.delete();
    }

    public void mv(File file, String newPath){
        file.renameTo(new File(newPath));
    }

    public void removeFile(File file){
        if(file.exists()){
            file.delete();
        }
    }
}
