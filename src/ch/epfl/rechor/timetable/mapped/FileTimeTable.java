package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;


import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;
import java.util.List;


public record FileTimeTable(Path directory, List<String> stringTable, Stations stations, StationAliases stationAliases, Platforms platforms, Routes routes, Transfers transfers) implements TimeTable {


    public static TimeTable in(Path directory) throws IOException {
        Path platformsPath = directory.resolve("platforms.bin");
        Path stationsPath = directory.resolve("stations.bin");
        Path routesPath = directory.resolve("routes.bin");
        Path transfersPath = directory.resolve("transfers.bin");
        Path stationAliasesPath = directory.resolve("station-aliases.bin");
        Path stringsPath = directory.resolve("strings.txt");
        List<String> stringTable = List.copyOf(Files.readAllLines(stringsPath));
        ByteBuffer platformsBuffer;
        ByteBuffer stationsBuffer;
        ByteBuffer routesBuffer;
        ByteBuffer transfersBuffer;
        ByteBuffer stationAliasesBuffer;

        try (FileChannel fileChannel = FileChannel.open(platformsPath)) { platformsBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());}
        try (FileChannel fileChannel = FileChannel.open(stationsPath)) { stationsBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());}
        try (FileChannel fileChannel = FileChannel.open(routesPath)) { routesBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());}
        try (FileChannel fileChannel = FileChannel.open(transfersPath)) { transfersBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());}
        try (FileChannel fileChannel = FileChannel.open(stationAliasesPath)) { stationAliasesBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());}

        return new FileTimeTable(directory,stringTable
                , new BufferedStations(stringTable,stationsBuffer)
                , new BufferedStationAliases(stringTable,stationAliasesBuffer)
                ,new BufferedPlatforms(stringTable,platformsBuffer)
                , new BufferedRoutes(stringTable,routesBuffer)
                , new BufferedTransfers(transfersBuffer));

    }



    @Override
    public Stations stations() {
        return stations;
    }

    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    @Override
    public Platforms platforms() {
        return platforms;
    }

    @Override
    public Routes routes() {
        return routes;
    }

    @Override
    public Transfers transfers() {
        return transfers;
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        try{
            Path datePath = directory.resolve(date.toString());
            Path tripsPath = datePath.resolve("trips.bin");
            FileChannel fileChannel = FileChannel.open(tripsPath);
            ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            return new BufferedTrips(stringTable,buffer);
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        try{
            Path datePath = directory.resolve(date.toString());
            Path connectionsPath = datePath.resolve("connections.bin");
            Path succConnectionsPath = datePath.resolve("connections-succ.bin");
            FileChannel connectionsFileChannel = FileChannel.open(connectionsPath);
            FileChannel succConnectionsFileChannel = FileChannel.open(succConnectionsPath);
            ByteBuffer connectionsBuffer = connectionsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, connectionsFileChannel.size());
            ByteBuffer succConnectionsBuffer = succConnectionsFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, succConnectionsFileChannel.size());
            return new BufferedConnections(connectionsBuffer,succConnectionsBuffer);
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }
}
