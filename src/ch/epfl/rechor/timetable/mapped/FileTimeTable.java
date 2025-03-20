package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public record FileTimeTable(Path directory,
                           List<String> stringTable, 
                           Stations stations,
                            StationAliases stationAliases, 
                            Platforms platforms,
                            Routes routes, 
                            Transfers transfers) implements TimeTable {

    private static final Charset STRING_CHARSET = StandardCharsets.ISO_8859_1;


    public static TimeTable in(Path directory) throws IOException {

        Path stringsPath = directory.resolve("strings.txt");
        List<String> strings = List.copyOf(Files.readAllLines(stringsPath, STRING_CHARSET));

        ByteBuffer stationsBuffer;
        try (FileChannel channel = FileChannel.open(directory.resolve("stations.bin"))) {
            stationsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        ByteBuffer stationAliasesBuffer;
        try (FileChannel channel = FileChannel.open(directory.resolve("station-aliases.bin"))) {
            stationAliasesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        ByteBuffer platformsBuffer;
        try (FileChannel channel = FileChannel.open(directory.resolve("platforms.bin"))) {
            platformsBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        ByteBuffer routesBuffer;
        try (FileChannel channel = FileChannel.open(directory.resolve("routes.bin"))) {
            routesBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        ByteBuffer transfersBuffer;
        try (FileChannel channel = FileChannel.open(directory.resolve("transfers.bin"))) {
            transfersBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        Stations stations = new BufferedStations(strings, stationsBuffer);
        StationAliases stationAliases = new BufferedStationAliases(strings, stationAliasesBuffer);
        Platforms platforms = new BufferedPlatforms(strings, platformsBuffer);
        Routes routes = new BufferedRoutes(strings, routesBuffer);
        Transfers transfers = new BufferedTransfers(transfersBuffer);


        return new FileTimeTable(
                directory,
                strings,
                stations,
                stationAliases,
                platforms,
                routes,
                transfers);
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        return null;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        return null;
    }
}
