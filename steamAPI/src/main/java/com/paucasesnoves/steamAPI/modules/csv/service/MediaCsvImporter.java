package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.Game;
import com.paucasesnoves.steamAPI.modules.games.domain.GameMedia;
import com.paucasesnoves.steamAPI.modules.games.repository.GameMediaRepository;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class MediaCsvImporter {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameMediaRepository mediaRepo;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, "UTF-8"))) {

            String[] line;
            reader.readNext(); // saltar cabecera
            List<GameMedia> batch = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);

                // Usar findById (porque appId es @Id)
                Game game = gameRepo.findById(appId).orElse(null);

                if (game == null) {
                    System.out.println("Juego con appId " + appId + " no encontrado, saltando media...");
                    continue;
                }

                if (mediaRepo.existsByGame(game)) {
                    System.out.println("Media ya existe para juego appId " + appId + ", saltando...");
                    continue;
                }

                GameMedia media = new GameMedia();
                media.setGame(game);
                media.setHeaderImage(line[1]);
                media.setScreenshots(parseMultiple(line[2]));
                media.setBackground(line[3]);
                media.setMovies(parseMultiple(line[4]));

                batch.add(media);

                if (batch.size() >= BATCH_SIZE) {
                    mediaRepo.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                mediaRepo.saveAll(batch);
            }

            System.out.println("Media importada: " + batch.size() + " registros");
        }
    }

    private List<String> parseMultiple(String field) {
        List<String> list = new ArrayList<>();
        if (field != null && !field.isEmpty()) {
            for (String s : field.split(";")) {
                list.add(s.trim());
            }
        }
        return list;
    }
}