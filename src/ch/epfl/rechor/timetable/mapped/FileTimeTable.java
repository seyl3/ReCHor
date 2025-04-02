package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Implémentation de l'interface TimeTable qui charge les données d'horaire à partir de fichiers.
 * <p>
 * Cette classe utilise le mappage mémoire pour accéder efficacement aux données de l'horaire
 * stockées dans différents fichiers binaires. Les fichiers ne sont chargés en mémoire que lorsque
 * nécessaire, ce qui améliore les performances au démarrage du programme.
 * </p>
 * <p>
 * Les données indépendantes de la date (gares, voies/quais, etc.) sont chargées lors de la
 * création de l'instance,
 * tandis que les données dépendantes de la date (courses, liaisons) sont chargées à la demande
 * via les méthodes
 * tripsFor et connectionsFor.
 * </p>
 *
 * @param directory      Chemin d'accès au dossier contenant les fichiers de données d'horaire
 * @param stringTable    Table des chaînes de caractères
 * @param stations       Gares indexées
 * @param stationAliases Noms alternatifs des gares
 * @param platforms      Voies/quais indexés
 * @param routes         Lignes de transport indexées
 * @param transfers      Changements indexés
 */
public record FileTimeTable(Path directory, List<String> stringTable, Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms, Routes routes,
                            Transfers transfers) implements TimeTable {

    private static final Charset STRING_CHARSET = StandardCharsets.ISO_8859_1;

    /**
     * Crée une nouvelle instance de FileTimeTable à partir du dossier spécifié.
     * <p>
     * Cette méthode charge toutes les données indépendantes de la date à partir des fichiers
     * se trouvant dans le dossier spécifié. Les fichiers suivants sont attendus dans ce dossier :
     * <ul>
     *   <li>strings.txt : la table des chaînes</li>
     *   <li>stations.bin : les gares</li>
     *   <li>station-aliases.bin : les noms alternatifs des gares</li>
     *   <li>platforms.bin : les voies/quais</li>
     *   <li>routes.bin : les lignes</li>
     *   <li>transfers.bin : les changements</li>
     * </ul>
     * </p>
     *
     * @param directory Le chemin d'accès au dossier contenant les données
     * @return Une nouvelle instance de TimeTable
     * @throws IOException En cas d'erreur lors de la lecture des fichiers
     */
    public static TimeTable in(Path directory) throws IOException {
        Path platformsPath = directory.resolve("platforms.bin");
        Path stationsPath = directory.resolve("stations.bin");
        Path routesPath = directory.resolve("routes.bin");
        Path transfersPath = directory.resolve("transfers.bin");
        Path stationAliasesPath = directory.resolve("station-aliases.bin");
        Path stringsPath = directory.resolve("strings.txt");
        System.out.println(stringsPath);
        List<String> stringTable = List.copyOf(Files.readAllLines(stringsPath, STRING_CHARSET));
        ByteBuffer platformsBuffer;
        ByteBuffer stationsBuffer;
        ByteBuffer routesBuffer;
        ByteBuffer transfersBuffer;
        ByteBuffer stationAliasesBuffer;

        try (FileChannel fileChannel = FileChannel.open(platformsPath)) {
            platformsBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(stationsPath)) {
            stationsBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(routesPath)) {
            routesBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(transfersPath)) {
            transfersBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
        try (FileChannel fileChannel = FileChannel.open(stationAliasesPath)) {
            stationAliasesBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                    fileChannel.size());
        }

        Stations stations = new BufferedStations(stringTable, stationsBuffer);
        StationAliases stationAliases = new BufferedStationAliases(stringTable,
                stationAliasesBuffer);
        Platforms platforms = new BufferedPlatforms(stringTable, platformsBuffer);
        Routes routes = new BufferedRoutes(stringTable, routesBuffer);
        Transfers transfers = new BufferedTransfers(transfersBuffer);

        return new FileTimeTable(directory, stringTable, stations, stationAliases, platforms,
                routes, transfers);

    }


    /**
     * Retourne les courses actives à la date spécifiée.
     * <p>
     * Cette méthode charge les données des courses depuis le fichier trips.bin
     * se trouvant dans le sous-dossier correspondant à la date spécifiée.
     * </p>
     *
     * @param date La date pour laquelle récupérer les courses
     * @return Les courses actives à la date spécifiée
     * @throws UncheckedIOException En cas d'erreur lors de la lecture du fichier
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        try {
            Path datePath = directory.resolve(date.toString());
            Path tripsPath = datePath.resolve("trips.bin");
            FileChannel fileChannel = FileChannel.open(tripsPath);
            ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                    fileChannel.size());
            return new BufferedTrips(stringTable, buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retourne les liaisons  à la date spécifiée.
     * <p>
     * Cette méthode charge les données des liaisons depuis les fichiers connections.bin
     * et connections-succ.bin se trouvant dans le sous-dossier correspondant à la date spécifiée.
     * </p>
     *
     * @param date La date pour laquelle récupérer les liaisons
     * @return Les liaisons actives à la date spécifiée
     * @throws UncheckedIOException En cas d'erreur lors de la lecture des fichiers
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        try {
            Path datePath = directory.resolve(date.toString());
            Path connectionsPath = datePath.resolve("connections.bin");
            Path succConnectionsPath = datePath.resolve("connections-succ.bin");
            FileChannel connectionsFileChannel = FileChannel.open(connectionsPath);
            FileChannel succConnectionsFileChannel = FileChannel.open(succConnectionsPath);
            ByteBuffer connectionsBuffer =
                    connectionsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                            connectionsFileChannel.size());
            ByteBuffer succConnectionsBuffer =
                    succConnectionsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                            succConnectionsFileChannel.size());
            return new BufferedConnections(connectionsBuffer, succConnectionsBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
