package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.*;
import com.paucasesnoves.steamAPI.modules.games.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameCsvImporter {

    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private DeveloperRepository developerRepo;
    @Autowired
    private PublisherRepository publisherRepo;
    @Autowired
    private PlatformRepository platformRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private GenreRepository genreRepo;
    @Autowired
    private TagRepository tagRepo;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 1000;

    // Caches optimizadas
    private final Map<String, Developer> developerCache = new ConcurrentHashMap<>();
    private final Map<String, Publisher> publisherCache = new ConcurrentHashMap<>();
    private final Map<String, Platform> platformCache = new ConcurrentHashMap<>();
    private final Map<String, Category> categoryCache = new ConcurrentHashMap<>();
    private final Map<String, Genre> genreCache = new ConcurrentHashMap<>();
    private final Map<String, Tag> tagCache = new ConcurrentHashMap<>();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importCsv(InputStream inputStream) throws Exception {
        long startTime = System.currentTimeMillis();

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String[] header = reader.readNext();
            if (header == null) {
                System.out.println("CSV file is empty");
                return;
            }

            String[] line;
            List<Game> batch = new ArrayList<>(BATCH_SIZE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int lineCount = 0;
            int savedCount = 0;
            int batchCounter = 0;

            // Cargar datos existentes en cache
            loadExistingDataToCache();

            while ((line = reader.readNext()) != null) {
                lineCount++;

                if (lineCount % 5000 == 0) {
                    System.out.printf("Procesadas: %,d líneas | Guardados: %,d juegos%n", lineCount, savedCount);
                }

                if (line.length < 18) continue;

                try {
                    Long appId = parseLongSafe(line[0]);
                    if (appId == null || gameRepo.existsById(appId)) continue;

                    Game game = createBasicGame(line, appId, dtf);

                    // Procesar relaciones MANUALMENTE (sin cascade para evitar problemas)
                    processRelationsManually(game, line);

                    batch.add(game);
                    savedCount++;

                    if (batch.size() >= BATCH_SIZE) {
                        saveBatch(batch, ++batchCounter, savedCount);
                        batch.clear();

                        if (batchCounter % 5 == 0) {
                            entityManager.clear();
                        }
                    }

                } catch (Exception e) {
                    if (lineCount % 10000 == 0) {
                        System.err.println("Error en línea " + lineCount + ": " + e.getMessage());
                    }
                    continue;
                }
            }

            if (!batch.isEmpty()) {
                saveBatch(batch, ++batchCounter, savedCount);
            }

            long endTime = System.currentTimeMillis();
            double totalSeconds = (endTime - startTime) / 1000.0;

            System.out.println("\n" + "=".repeat(50));
            System.out.println("IMPORTACIÓN COMPLETADA");
            System.out.println("=".repeat(50));
            System.out.printf("Total procesado:  %,d líneas%n", lineCount);
            System.out.printf("Juegos guardados: %,d%n", savedCount);
            System.out.printf("Tiempo total:     %.1f segundos%n", totalSeconds);
            System.out.printf("Velocidad:        %.0f juegos/segundo%n", savedCount / totalSeconds);
            System.out.println("=".repeat(50));

        } finally {
            clearCaches();
        }
    }

    private void loadExistingDataToCache() {
        System.out.println("Cargando datos existentes en cache...");

        // Cargar solo si hay datos
        if (developerRepo.count() > 0) {
            developerRepo.findAll().forEach(d -> developerCache.put(d.getName(), d));
        }
        if (publisherRepo.count() > 0) {
            publisherRepo.findAll().forEach(p -> publisherCache.put(p.getName(), p));
        }
        if (platformRepo.count() > 0) {
            platformRepo.findAll().forEach(p -> platformCache.put(p.getName(), p));
        }
        if (categoryRepo.count() > 0) {
            categoryRepo.findAll().forEach(c -> categoryCache.put(c.getName(), c));
        }
        if (genreRepo.count() > 0) {
            genreRepo.findAll().forEach(g -> genreCache.put(g.getName(), g));
        }
        if (tagRepo.count() > 0) {
            tagRepo.findAll().forEach(t -> tagCache.put(t.getName(), t));
        }

        System.out.println("Cache cargada");
    }

    private Game createBasicGame(String[] line, Long appId, DateTimeFormatter dtf) {
        Game game = new Game();
        game.setAppId(appId);
        game.setTitle(line[1] != null ? line[1].trim() : "");

        // Campos básicos
        if (line[2] != null && !line[2].trim().isEmpty()) {
            try {
                game.setReleaseDate(LocalDate.parse(line[2].trim(), dtf));
            } catch (DateTimeParseException ignored) {}
        }

        game.setEnglish("1".equals(line[3] != null ? line[3].trim() : ""));

        // Procesar otros campos...
        processNumericFields(game, line);

        return game;
    }

    private void processNumericFields(Game game, String[] line) {
        // required_age
        if (line[7] != null && !line[7].trim().isEmpty()) {
            try {
                game.setMinAge(Integer.parseInt(line[7].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // achievements
        if (line[11] != null && !line[11].trim().isEmpty()) {
            try {
                game.setAchievements(Integer.parseInt(line[11].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // positive_ratings
        if (line[12] != null && !line[12].trim().isEmpty()) {
            try {
                game.setPositiveRatings(Integer.parseInt(line[12].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // negative_ratings
        if (line[13] != null && !line[13].trim().isEmpty()) {
            try {
                game.setNegativeRatings(Integer.parseInt(line[13].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // avg_playtime
        if (line[14] != null && !line[14].trim().isEmpty()) {
            try {
                game.setAvgPlaytime(Double.parseDouble(line[14].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // median_playtime
        if (line[15] != null && !line[15].trim().isEmpty()) {
            try {
                game.setMedianPlaytime(Double.parseDouble(line[15].trim()));
            } catch (NumberFormatException ignored) {}
        }

        // owners
        if (line[16] != null && !line[16].trim().isEmpty()) {
            processOwners(game, line[16].trim());
        }

        // price
        if (line[17] != null && !line[17].trim().isEmpty()) {
            try {
                game.setPrice(new BigDecimal(line[17].trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void processOwners(Game game, String ownersStr) {
        try {
            if (ownersStr.contains("-")) {
                String[] parts = ownersStr.split("-");
                if (parts.length == 2) {
                    int low = Integer.parseInt(parts[0].trim());
                    int high = Integer.parseInt(parts[1].trim());
                    game.setOwnersLower(low);
                    game.setOwnersUpper(high);
                    game.setOwnersMid((low + high) / 2);
                }
            } else {
                int val = Integer.parseInt(ownersStr);
                game.setOwnersLower(val);
                game.setOwnersUpper(val);
                game.setOwnersMid(val);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void processRelationsManually(Game game, String[] line) {
        // developers
        if (line[4] != null && !line[4].trim().isEmpty()) {
            for (String devName : line[4].split(";")) {
                devName = devName.trim();
                if (!devName.isEmpty()) {
                    Developer dev = getOrCreateDeveloper(devName);
                    game.getDevelopers().add(dev);
                }
            }
        }

        // publishers
        if (line[5] != null && !line[5].trim().isEmpty()) {
            for (String pubName : line[5].split(";")) {
                pubName = pubName.trim();
                if (!pubName.isEmpty()) {
                    Publisher pub = getOrCreatePublisher(pubName);
                    game.getPublishers().add(pub);
                }
            }
        }

        // platforms
        if (line[6] != null && !line[6].trim().isEmpty()) {
            for (String platName : line[6].split(";")) {
                platName = platName.trim();
                if (!platName.isEmpty()) {
                    Platform platform = getOrCreatePlatform(platName);
                    game.getPlatforms().add(platform);
                }
            }
        }

        // category (solo primera)
        if (line[8] != null && !line[8].trim().isEmpty()) {
            String[] categories = line[8].split(";");
            if (categories.length > 0) {
                String catName = categories[0].trim();
                if (!catName.isEmpty()) {
                    Category category = getOrCreateCategory(catName);
                    game.setCategory(category);
                }
            }
        }

        // genres
        if (line[9] != null && !line[9].trim().isEmpty()) {
            for (String genreName : line[9].split(";")) {
                genreName = genreName.trim();
                if (!genreName.isEmpty()) {
                    Genre genre = getOrCreateGenre(genreName);
                    game.getGenres().add(genre);
                }
            }
        }

        // tags
        if (line[10] != null && !line[10].trim().isEmpty()) {
            for (String tagName : line[10].split(";")) {
                tagName = tagName.trim();
                if (!tagName.isEmpty()) {
                    Tag tag = getOrCreateTag(tagName);
                    game.getTags().add(tag);
                }
            }
        }
    }

    // === MÉTODOS GET OR CREATE MEJORADOS ===

    private Developer getOrCreateDeveloper(String name) {
        return developerCache.computeIfAbsent(name, n -> {
            Optional<Developer> existing = developerRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Developer newDev = new Developer(n);
                return developerRepo.save(newDev); // Guardar inmediatamente
            }
        });
    }

    private Publisher getOrCreatePublisher(String name) {
        return publisherCache.computeIfAbsent(name, n -> {
            Optional<Publisher> existing = publisherRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Publisher newPub = new Publisher(n);
                return publisherRepo.save(newPub); // Guardar inmediatamente
            }
        });
    }

    private Platform getOrCreatePlatform(String name) {
        return platformCache.computeIfAbsent(name, n -> {
            Optional<Platform> existing = platformRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Platform newPlatform = new Platform(n);
                return platformRepo.save(newPlatform); // Guardar inmediatamente
            }
        });
    }

    private Category getOrCreateCategory(String name) {
        return categoryCache.computeIfAbsent(name, n -> {
            Optional<Category> existing = categoryRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Category newCategory = new Category(n);
                return categoryRepo.save(newCategory); // Guardar inmediatamente
            }
        });
    }

    private Genre getOrCreateGenre(String name) {
        return genreCache.computeIfAbsent(name, n -> {
            Optional<Genre> existing = genreRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Genre newGenre = new Genre(n);
                return genreRepo.save(newGenre); // Guardar inmediatamente
            }
        });
    }

    private Tag getOrCreateTag(String name) {
        return tagCache.computeIfAbsent(name, n -> {
            Optional<Tag> existing = tagRepo.findByName(n);
            if (existing.isPresent()) {
                return existing.get();
            } else {
                Tag newTag = new Tag(n);
                return tagRepo.save(newTag); // Guardar inmediatamente
            }
        });
    }

    private void saveBatch(List<Game> batch, int batchNumber, int totalSaved) {
        long start = System.currentTimeMillis();

        try {
            gameRepo.saveAll(batch);
        } catch (Exception e) {
            System.err.println("Error en lote #" + batchNumber + ": " + e.getMessage());
            // Intentar guardar uno por uno
            for (Game game : batch) {
                try {
                    gameRepo.save(game);
                } catch (Exception ex) {
                    System.err.println("Error guardando juego " + game.getAppId() + ": " + ex.getMessage());
                }
            }
        }

        long end = System.currentTimeMillis();
        double seconds = (end - start) / 1000.0;

        System.out.printf("Lote #%d: %,d juegos | Total: %,d | Tiempo: %.2fs%n",
                batchNumber, batch.size(), totalSaved, seconds);
    }

    private void clearCaches() {
        developerCache.clear();
        publisherCache.clear();
        platformCache.clear();
        categoryCache.clear();
        genreCache.clear();
        tagCache.clear();
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}