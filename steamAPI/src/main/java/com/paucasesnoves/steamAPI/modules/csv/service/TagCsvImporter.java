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
            if (header == null || header.length < 2) {
                System.out.println("CSV de tags vacío o con formato incorrecto");
                return;
            }

            String[] line;
            List<Game> batchGames = new ArrayList<>();
            int lineCount = 0;
            int processedCount = 0;

            while ((line = reader.readNext()) != null) {
                lineCount++;

                if (line.length < 2) {
                    System.out.println("Línea " + lineCount + " tiene formato inválido, saltando...");
                    continue;
                }

                try {
                    Long appId = Long.parseLong(line[0].trim());
                    Game game = gameRepo.findById(appId).orElse(null);

                    if (game == null) {
                        System.out.println("Juego con appId " + appId + " no encontrado, saltando...");
                        continue;
                    }

                    boolean hasTags = false;
                    for (int i = 1; i < Math.min(line.length, header.length); i++) {
                        String tagName = header[i];
                        if (tagName == null || tagName.trim().isEmpty()) continue;

                        if ("1".equals(line[i].trim())) {
                            Tag tag = tagRepo.findByName(tagName.trim())
                                    .orElseGet(() -> {
                                        Tag newTag = new Tag(tagName.trim());
                                        return tagRepo.save(newTag);
                                    });

                            // SOLO agregar al juego (relación unidireccional)
                            game.getTags().add(tag);
                            hasTags = true;
                        }
                    }

                    if (hasTags) {
                        batchGames.add(game);
                        processedCount++;
                    }

                    if (batchGames.size() >= BATCH_SIZE) {
                        gameRepo.saveAll(batchGames);
                        System.out.println("Guardado lote de " + batchGames.size() + " juegos con tags");
                        batchGames.clear();
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Línea " + lineCount + ": appId inválido: " + line[0]);
                    continue;
                } catch (Exception e) {
                    System.err.println("Error procesando línea " + lineCount + ": " + e.getMessage());
                    continue;
                }
            }

            if (!batchGames.isEmpty()) {
                gameRepo.saveAll(batchGames);
                System.out.println("Guardado último lote de " + batchGames.size() + " juegos con tags");
            }

            System.out.println("Importación de tags completada. Procesadas " + lineCount + " líneas, actualizados " + processedCount + " juegos.");

        } catch (Exception e) {
            System.err.println("Error leyendo archivo CSV de tags: " + e.getMessage());
            throw e;
        }
    }
}