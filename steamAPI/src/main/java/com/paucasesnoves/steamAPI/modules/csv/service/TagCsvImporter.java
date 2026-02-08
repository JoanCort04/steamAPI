package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.Game;
import com.paucasesnoves.steamAPI.modules.games.domain.Tag;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRepository;
import com.paucasesnoves.steamAPI.modules.games.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class TagCsvImporter {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private TagRepository tagRepo;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String[] header = reader.readNext(); // primera fila: nombres de columnas
            String[] line;
            List<Game> batchGames = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);
                Game game = gameRepo.findById(appId).orElse(null);
                if (game == null) continue;

                for (int i = 1; i < line.length; i++) {
                    String tagName = header[i];
                    if ("1".equals(line[i].trim())) {
                        Tag tag = tagRepo.findByName(tagName)
                                .orElseGet(() -> tagRepo.save(new Tag(tagName)));
                        game.getTags().add(tag);       // ManyToMany
                        tag.getGames().add(game);
                    }
                }

                batchGames.add(game);

                if (batchGames.size() >= BATCH_SIZE) {
                    gameRepo.saveAll(batchGames);
                    batchGames.clear();
                }
            }

            if (!batchGames.isEmpty()) {
                gameRepo.saveAll(batchGames);
            }
        }
    }
}