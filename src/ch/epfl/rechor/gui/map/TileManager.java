package ch.epfl.rechor.gui.map;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TileManager {

    private static final int MEM_CACHE_MAX = 100;
    private final Path cacheDir;
    private final String tileServer;
    private final Map<TileId, Image> memory = java.util.Collections.synchronizedMap(
            new LinkedHashMap<TileId, Image>(MEM_CACHE_MAX + 1, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<TileId, Image> eldest) {
                    return size() > MEM_CACHE_MAX;
                }
            });

    /**
     * @param cacheDir   chemin vers le dossier racine du cache disque
     * @param tileServer nom de domaine du serveur OSM (p. ex. {@code tile.openstreetmap.org})
     */
    public TileManager(Path cacheDir, String tileServer) throws IllegalArgumentException {
        this.cacheDir = cacheDir;
        this.tileServer = tileServer;
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de créer le dossier cache: " + cacheDir,
                    e);
        }
    }

    /**
     * Retourne l’image (256×256) correspondant à la tuile donnée,
     * en passant par les caches si possible.
     *
     * @param id identité de la tuile recherchée
     * @return l’image JavaFX de la tuile
     * @throws IOException si le téléchargement ou la lecture échoue
     */
    public Image imageForTileAt(TileId id) throws IOException {
        Image img = memory.get(id);
        if (img != null) return img;

        Path imgPath = cacheDir.resolve(
                Path.of(Integer.toString(id.zoom()),
                        Integer.toString(id.x()),
                        id.y() + ".png"));
        if (Files.exists(imgPath)) {
            try (InputStream in = Files.newInputStream(imgPath)) {
                img = new Image(in);
            }
            memory.put(id, img);
            return img;
        }

        String urlStr = "https://" + tileServer + '/'
                + id.zoom() + '/' + id.x() + '/' + id.y() + ".png";
        URI uri = URI.create(urlStr);
        URL url = uri.toURL();
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "ReCHor");

        byte[] data;
        try (InputStream in = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            in.transferTo(bos);
            data = bos.toByteArray();
        }
        Files.createDirectories(imgPath.getParent());
        Files.write(imgPath, data);

        img = new Image(new ByteArrayInputStream(data));
        memory.put(id, img);
        return img;
    }

    public record TileId(int zoom, int x, int y) {
    }
}