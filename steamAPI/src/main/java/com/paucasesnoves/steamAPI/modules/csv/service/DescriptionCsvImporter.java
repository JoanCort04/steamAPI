package com.paucasesnoves.steamAPI.modules.csv.service;
import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.Game;
import com.paucasesnoves.steamAPI.modules.games.domain.GameDescription;
import com.paucasesnoves.steamAPI.modules.games.repository.GameDescriptionRepository;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DescriptionCsvImporter {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameDescriptionRepository descriptionRepo;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, "UTF-8"))) {  // <-- usa inputStream directo

            String[] line;
            reader.readNext(); // saltar cabecera
            List<GameDescription> batch = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);
                Game game = gameRepo.findById(appId).orElse(null);

                if (game == null) continue; // saltar descripciones de juegos que no existen
                if (descriptionRepo.existsByGame(game)) continue; // evitar duplicados

                String detailed = line[1];
                String about = line[2];
                String shortDesc = line[3];

                GameDescription gd = new GameDescription();
                gd.setGame(game);
                gd.setDetailedDescription(detailed);
                gd.setAboutTheGame(about);
                gd.setShortDescription(shortDesc);

                batch.add(gd);

                if (batch.size() >= BATCH_SIZE) {
                    descriptionRepo.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                descriptionRepo.saveAll(batch);
            }
        }
    }
}
