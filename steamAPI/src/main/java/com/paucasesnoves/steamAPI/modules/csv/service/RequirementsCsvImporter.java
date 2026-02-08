package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.Game;
import com.paucasesnoves.steamAPI.modules.games.domain.GameRequirements;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRepository;
import com.paucasesnoves.steamAPI.modules.games.repository.GameRequirementsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class RequirementsCsvImporter {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameRequirementsRepository requirementsRepo;

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, "UTF-8"))) {

            String[] line;
            reader.readNext(); // saltar cabecera
            List<GameRequirements> batch = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);

                // Usar findById (porque appId es @Id)
                Game game = gameRepo.findById(appId).orElse(null);

                if (game == null) {
                    System.out.println("Juego con appId " + appId + " no encontrado, saltando requisitos...");
                    continue;
                }

                if (requirementsRepo.existsByGame(game)) {
                    System.out.println("Requisitos ya existen para juego appId " + appId + ", saltando...");
                    continue;
                }

                GameRequirements gr = new GameRequirements();
                gr.setGame(game);
                gr.setPcRequirements(line[1]);
                gr.setMacRequirements(line[2]);
                gr.setLinuxRequirements(line[3]);
                gr.setMinimum(line[4]);
                gr.setRecommended(line[5]);

                batch.add(gr);

                if (batch.size() >= BATCH_SIZE) {
                    requirementsRepo.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                requirementsRepo.saveAll(batch);
            }

            System.out.println("Requisitos importados: " + batch.size() + " registros");
        }
    }
}