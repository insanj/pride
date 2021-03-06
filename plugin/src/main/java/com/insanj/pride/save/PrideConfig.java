package com.insanj.pride.save;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.Gson;

public class PrideConfig {
    public static PrideConfig configFromFile(File configFile) {
      Gson gson = new Gson();
      PrideConfig config = null;
      try {
          config = gson.fromJson( new FileReader(configFile), PrideConfig.class);
      } catch (Exception e) {
          e.printStackTrace();
      }
      finally {
          return (config == null ? new PrideConfig() : config);
      }
    }

    public static PrideConfig writeConfigToFile(File configFile) {
      Gson gson = new Gson();
      PrideConfig config = new PrideConfig();
      String result = gson.toJson(config);
      try {
          FileOutputStream out = new FileOutputStream(configFile, false);

          out.write(result.getBytes());
          out.flush();
          out.close();

      } catch (IOException ex) {
          ex.printStackTrace();
      }

      return config;
    }

    // how many blocks away in all x/y/z directions required to activate pride area
    public double activationDistance = 50.0;
    public int bottleneckLimit = 100;
    public List<UUID> suppressedUUIDs = new ArrayList<UUID>();

    public void saveConfig(String configPath) {
        File configFile = new File(configPath);
        String result = new Gson().toJson(this);
        try {
            FileOutputStream out = new FileOutputStream(configFile, false);

            out.write(result.getBytes());
            out.flush();
            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
