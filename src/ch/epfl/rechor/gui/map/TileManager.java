package ch.epfl.rechor.gui.map;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Gestionnaire des tuiles OpenStreetMap.
 * <p>
 * Se charge de :
 * <ul>
 *   <li>vérifier/obtenir une tuile depuis le cache mémoire (LRU, 100 entrées max) ;</li>
 *   <li>sinon, la prendre dans le cache disque ;</li>
 *   <li>sinon, la télécharger du serveur OSM, la stocker sur disque puis en mémoire.</li>
 * </ul>
 * </p>
 *
 * @author Pessi
 */
public final class TileManager {

    /** Identité d’une tuile OSM. */
    public record TileId(int zoom, int x, int y) {}

    private static final int TILE_SIZE = 256;
    private static final int MEM_CACHE_MAX = 100;

    private final Path cacheDir;
    private final String tileServer;

    // LRU cache mémoire  (synchronized via wrapper)
    private final Map<TileId, Image> memory = java.util.Collections.synchronizedMap(
            new LinkedHashMap<TileId, Image>(MEM_CACHE_MAX + 1, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<TileId, Image> eldest) {
                    return size() > MEM_CACHE_MAX;
                }
            });

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------
    /**
     * @param cacheDir chemin vers le dossier racine du cache disque
     * @param tileServer nom de domaine du serveur OSM (p. ex. {@code tile.openstreetmap.org})
     */
    public TileManager(Path cacheDir, String tileServer) throws IllegalArgumentException {
        this.cacheDir   = cacheDir;
        this.tileServer = tileServer;
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            // convert to unchecked to simplify
            throw new IllegalArgumentException("Impossible de créer le dossier cache: " + cacheDir, e);
        }
    }

    // -------------------------------------------------------------------------
    // Accès public
    // -------------------------------------------------------------------------
    /**
     * Retourne l’image (256×256) correspondant à la tuile donnée,
     * en passant par les caches si possible.
     *
     * @param id identité de la tuile recherchée
     * @return   l’image JavaFX de la tuile
     * @throws IOException si le téléchargement ou la lecture échoue
     */
    public Image imageForTileAt(TileId id) throws IOException {
        // 1) mémoire
        Image img = memory.get(id);
        if (img != null) return img;

        // 2) disque
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

        // 3) téléchargement
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
        // sauvegarde disque
        Files.createDirectories(imgPath.getParent());
        Files.write(imgPath, data);

        // création Image
        img = new Image(new ByteArrayInputStream(data));
        memory.put(id, img);
        return img;
    }
}