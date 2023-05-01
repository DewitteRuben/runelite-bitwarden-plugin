package com.bitwarden;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

import java.io.*;

@Slf4j
public class Storage {
    public static final String DATA_FOLDER = "bitwarden";
    public static final String CREDENTIALS_FILE = "credentials.json";

    public static File STORAGE_DIRECTORY;

    public static Gson GSON;

    static {
        STORAGE_DIRECTORY = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER);
        STORAGE_DIRECTORY.mkdirs();

        Gson gson = new Gson();
        GSON = gson.newBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public static void saveCredentials(Bitwarden.Config config) {
        try {
            STORAGE_DIRECTORY.mkdirs();
            File credentials = new File(STORAGE_DIRECTORY, CREDENTIALS_FILE);

            Writer writer = new FileWriter(credentials);
            GSON.toJson(config, writer);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.warn("Error ignored while updating saving credential data: " + e.getMessage());
        }
    }

    public static void clearCredentials() {
        STORAGE_DIRECTORY.mkdirs();
        File credentials = new File(STORAGE_DIRECTORY, CREDENTIALS_FILE);
        if (!credentials.exists())
        {
            return;
        }

        credentials.delete();
    }


    public static Bitwarden.Config loadCredentials() {
       try {
           File credentials = new File(STORAGE_DIRECTORY, CREDENTIALS_FILE);

           if (!credentials.exists())
           {
               return null;
           }

           return GSON.fromJson(new FileReader(credentials), Bitwarden.Config.class);
       } catch (Exception e) {
           log.warn("Error ignored while loading credential data: " + e.getMessage());
       }

        return null;
    }
}
