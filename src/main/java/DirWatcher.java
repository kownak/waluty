import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class DirWatcher {
    public static void main(String[] args) {

        String myDirString = null;
        Path myDir = null;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("target/classes/config.properties"));
            myDirString = properties.getProperty("watchedDir");
            myDir = Paths.get(myDirString);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


        try {
            WatchService watcher = myDir.getFileSystem().newWatchService();
            myDir.register(watcher, ENTRY_CREATE);

            FileHandler fileHandler = new FileHandler();
            fileHandler.init();

            while (true) {

                WatchKey watckKey = watcher.take();
                List<WatchEvent<?>> events = watckKey.pollEvents();

                for (WatchEvent event : events) {
                    if (event.kind() == ENTRY_CREATE) {
                        System.out.println("Created: " + event.context().toString());
                        fileHandler.processNewFile(event.context().toString(),myDirString);
                    }
                }
                watckKey.reset();
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
            e.printStackTrace();
        }

    }
}
