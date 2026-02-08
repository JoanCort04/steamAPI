package com.paucasesnoves.steamAPI.modules.csv.service;

import com.opencsv.CSVReader;
import com.paucasesnoves.steamAPI.modules.games.domain.*;
import com.paucasesnoves.steamAPI.modules.games.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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

    private static final int BATCH_SIZE = 50;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String[] header = reader.readNext();
            if (header == null) {
                System.out.println("CSV file is empty");
                return;
            }

            String[] line;
            List<Game> batch = new ArrayList<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int lineCount = 0;
            int savedCount = 0;

            while ((line = reader.readNext()) != null) {
                lineCount++;

                if (line.length < 18) {
                    System.out.println("Line " + lineCount + " has invalid format, skipping...");
                    continue;
                }

                try {
                    Long appId = parseLongSafe(line[0]);
                    if (appId == null) {
                        System.out.println("Line " + lineCount + ": Invalid appId, skipping...");
                        continue;
                    }

                    if (gameRepo.existsById(appId)) {
                        System.out.println("Game " + appId + " already exists, skipping...");
                        continue;
                    }

                    Game game = new Game();
                    game.setAppId(appId);
                    game.setTitle(line[1] != null ? line[1].trim() : "");

                    // release_date
                    if (line[2] != null && !line[2].trim().isEmpty()) {
                        try {
                            game.setReleaseDate(LocalDate.parse(line[2].trim(), dtf));
                        } catch (DateTimeParseException e) {
                            System.out.println("Line " + lineCount + ": Invalid date format: " + line[2]);
                        }
                    }

                    // english
                    game.setEnglish("1".equals(line[3] != null ? line[3].trim() : ""));

                    // developers (SOLO agregar al juego)
                    if (line[4] != null && !line[4].trim().isEmpty()) {
                        for (String devName : line[4].split(";")) {
                            devName = devName.trim();
                            if (devName.isEmpty()) continue;

                            String finalDevName = devName;
                            Developer dev = developerRepo.findByName(devName)
                                    .orElseGet(() -> {
                                        Developer newDev = new Developer(finalDevName);
                                        return developerRepo.save(newDev);
                                    });

                            game.getDevelopers().add(dev);
                            // ELIMINADO: dev.getGames().add(game);
                        }
                    }

                    // publishers (SOLO agregar al juego)
                    if (line[5] != null && !line[5].trim().isEmpty()) {
                        for (String pubName : line[5].split(";")) {
                            pubName = pubName.trim();
                            if (pubName.isEmpty()) continue;

                            String finalPubName = pubName;
                            Publisher pub = publisherRepo.findByName(pubName)
                                    .orElseGet(() -> {
                                        Publisher newPub = new Publisher(finalPubName);
                                        return publisherRepo.save(newPub);
                                    });

                            game.getPublishers().add(pub);
                            // ELIMINADO: pub.getGames().add(game);
                        }
                    }

                    // platforms (SOLO agregar al juego)
                    if (line[6] != null && !line[6].trim().isEmpty()) {
                        for (String platName : line[6].split(";")) {
                            platName = platName.trim();
                            if (platName.isEmpty()) continue;

                            String finalPlatName = platName;
                            Platform platform = platformRepo.findByName(platName)
                                    .orElseGet(() -> {
                                        Platform newPlatform = new Platform(finalPlatName);
                                        return platformRepo.save(newPlatform);
                                    });

                            game.getPlatforms().add(platform);
                            // ELIMINADO: platform.getGames().add(game);
                        }
                    }

                    // required_age → minAge
                    if (line[7] != null && !line[7].trim().isEmpty()) {
                        try {
                            game.setMinAge(Integer.parseInt(line[7].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid minAge: " + line[7]);
                        }
                    }

                    // categories (solo la primera)
                    if (line[8] != null && !line[8].trim().isEmpty()) {
                        String[] categories = line[8].split(";");
                        if (categories.length > 0) {
                            String catName = categories[0].trim();
                            if (!catName.isEmpty()) {
                                Category category = categoryRepo.findByName(catName)
                                        .orElseGet(() -> {
                                            Category newCategory = new Category(catName);
                                            return categoryRepo.save(newCategory);
                                        });
                                game.setCategory(category);
                                // ELIMINADO: category.getGames().add(game);
                            }
                        }
                    }

                    // genres (SOLO agregar al juego)
                    if (line[9] != null && !line[9].trim().isEmpty()) {
                        for (String genreName : line[9].split(";")) {
                            genreName = genreName.trim();
                            if (genreName.isEmpty()) continue;

                            String finalGenreName = genreName;
                            Genre genre = genreRepo.findByName(genreName)
                                    .orElseGet(() -> {
                                        Genre newGenre = new Genre(finalGenreName);
                                        return genreRepo.save(newGenre);
                                    });

                            game.getGenres().add(genre);
                            // ELIMINADO: genre.getGames().add(game);
                        }
                    }

                    // steamspy_tags (SOLO agregar al juego)
                    if (line[10] != null && !line[10].trim().isEmpty()) {
                        for (String tagName : line[10].split(";")) {
                            tagName = tagName.trim();
                            if (tagName.isEmpty()) continue;

                            String finalTagName = tagName;
                            Tag tag = tagRepo.findByName(tagName)
                                    .orElseGet(() -> {
                                        Tag newTag = new Tag(finalTagName);
                                        return tagRepo.save(newTag);
                                    });

                            game.getTags().add(tag);
                            // ELIMINADO: tag.getGames().add(game);
                        }
                    }

                    // achievements
                    if (line[11] != null && !line[11].trim().isEmpty()) {
                        try {
                            game.setAchievements(Integer.parseInt(line[11].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid achievements: " + line[11]);
                        }
                    }

                    // positive_ratings
                    if (line[12] != null && !line[12].trim().isEmpty()) {
                        try {
                            game.setPositiveRatings(Integer.parseInt(line[12].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid positive ratings: " + line[12]);
                        }
                    }

                    // negative_ratings
                    if (line[13] != null && !line[13].trim().isEmpty()) {
                        try {
                            game.setNegativeRatings(Integer.parseInt(line[13].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid negative ratings: " + line[13]);
                        }
                    }

                    // average_playtime → avgPlaytime
                    if (line[14] != null && !line[14].trim().isEmpty()) {
                        try {
                            game.setAvgPlaytime(Double.parseDouble(line[14].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid avg playtime: " + line[14]);
                        }
                    }

                    // median_playtime
                    if (line[15] != null && !line[15].trim().isEmpty()) {
                        try {
                            game.setMedianPlaytime(Double.parseDouble(line[15].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid median playtime: " + line[15]);
                        }
                    }

                    // owners → ownersLower, ownersUpper, ownersMid
                    if (line[16] != null && !line[16].trim().isEmpty()) {
                        String ownersStr = line[16].trim();
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
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid owners format: " + ownersStr);
                        }
                    }

                    // price
                    if (line[17] != null && !line[17].trim().isEmpty()) {
                        try {
                            game.setPrice(new BigDecimal(line[17].trim()));
                        } catch (NumberFormatException e) {
                            System.out.println("Line " + lineCount + ": Invalid price: " + line[17]);
                        }
                    }

                    batch.add(game);
                    savedCount++;

                    if (batch.size() >= BATCH_SIZE) {
                        gameRepo.saveAll(batch);
                        System.out.println("Saved batch of " + batch.size() + " games. Total: " + savedCount);
                        batch.clear();
                    }

                } catch (Exception e) {
                    System.err.println("Error processing line " + lineCount + ": " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }

            if (!batch.isEmpty()) {
                gameRepo.saveAll(batch);
                System.out.println("Saved final batch of " + batch.size() + " games. Total saved: " + savedCount);
            }

            System.out.println("Import completed. Processed " + lineCount + " lines, saved " + savedCount + " games.");

        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            throw e;
        }
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}