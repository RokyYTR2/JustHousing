package dev.meyba.justHousing.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class VersionChecker {
    private final Plugin plugin;
    private final String githubUser;
    private final String repoName;

    public VersionChecker(Plugin plugin, String githubUser, String repoName) {
        this.plugin = plugin;
        this.githubUser = githubUser;
        this.repoName = repoName;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String currentVersion = plugin.getDescription().getVersion();
                String latestVersion = getLatestVersion();

                if (latestVersion != null && !currentVersion.equals(latestVersion)) {
                    plugin.getLogger().warning("================================================");
                    plugin.getLogger().warning("A new version is available!");
                    plugin.getLogger().warning("Current version: " + currentVersion);
                    plugin.getLogger().warning("Latest version: " + latestVersion);
                    plugin.getLogger().warning("Download: https://github.com/" + githubUser + "/" + repoName);
                    plugin.getLogger().warning("================================================");
                } else if (latestVersion != null) {
                    plugin.getLogger().info("You are running the latest version!");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws Exception {
        String urlString = String.format(
                "https://raw.githubusercontent.com/%s/%s/refs/heads/%s/src/main/resources/plugin.yml",
                githubUser, repoName, "main"
        );

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("version:")) {
                        String version = line.substring(line.indexOf(":") + 1).trim();
                        version = version.replaceAll("['\"]", "");

                        reader.close();
                        connection.disconnect();

                        return version;
                    }
                }

                reader.close();
                connection.disconnect();
            }
        } catch (Exception e) {
            throw new Exception("Could not fetch version: " + e.getMessage());
        }

        throw new Exception("Could not fetch version from " + urlString);
    }
}