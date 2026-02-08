package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.Game;
import com.paucasesnoves.steamAPI.modules.games.domain.GameSupportInfo;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRepository;
import com.paucasesnoves.steamAPI.modules.games.repository.GameSupportInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupportCsvImporter {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameSupportInfoRepository supportRepo;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String[] line;
            reader.readNext(); // saltar cabecera
            List<GameSupportInfo> batch = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);
                Game game = gameRepo.findById(appId).orElse(null);
                if (game == null) continue;
                if (supportRepo.existsByGame(game)) continue;

                GameSupportInfo info = new GameSupportInfo();
                info.setGame(game);
                info.setWebsite(line[1]);
                info.setSupportUrl(line[2]);
                info.setSupportEmail(line[3]);

                batch.add(info);

                if (batch.size() >= BATCH_SIZE) {
                    supportRepo.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                supportRepo.saveAll(batch);
            }
        }
    }
}