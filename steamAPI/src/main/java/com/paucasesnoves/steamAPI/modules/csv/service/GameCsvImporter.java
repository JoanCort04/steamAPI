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
import java.util.*;

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

    private static final int BATCH_SIZE = 100;

    @Transactional
    public void importCsv(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {

            String[] header = reader.readNext(); // cabecera
            String[] line;
            List<Game> batch = new ArrayList<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readNext()) != null) {
                Long appId = Long.parseLong(line[0]);

                // Saltar si ya existe
                if (gameRepo.existsById(appId)) continue;

                Game game = new Game();
                game.setId(appId);
                game.setTitle(line[1]);

                // release_date
                if (!line[2].isEmpty()) {
                    game.setReleaseDate(LocalDate.parse(line[2], dtf));
                }

                // english
                game.setEnglish("1".equals(line[3].trim()));

                // developers
                for (String devName : line[4].split(";")) {
                    devName = devName.trim();
                    if (devName.isEmpty()) continue;

                    String finalDevName = devName;
                    Developer dev = developerRepo.findByName(devName)
                            .orElseGet(() -> developerRepo.save(new Developer(finalDevName)));

                    game.setDeveloper(dev); // Asignar el developer al juego
                    dev.getGames().add(game); // Si en Developer tienes @OneToMany Set<Game> games

                }

                // publishers
                for (String pubName : line[5].split(";")) {
                    pubName = pubName.trim();
                    if (pubName.isEmpty()) continue;

                    String finalPubName = pubName;
                    Publisher pub = publisherRepo.findByName(pubName)
                            .orElseGet(() -> publisherRepo.save(new Publisher(finalPubName)));

                    game.getPublishers().add(pub);
                    pub.getGames().add(game);
                }

                // platforms
                for (String platName : line[6].split(";")) {
                    platName = platName.trim();
                    if (platName.isEmpty()) continue;

                    String finalPlatName = platName;
                    Platform platform = platformRepo.findByName(platName)
                            .orElseGet(() -> platformRepo.save(new Platform(finalPlatName)));

                    game.getPlatforms().add(platform);
                    platform.getGames().add(game);
                }

                // required_age → minAge
                if (!line[7].isEmpty()) {
                    game.setMinAge(Integer.parseInt(line[7]));
                }

                // categories
                for (String catName : line[8].split(";")) {
                    catName = catName.trim();
                    if (catName.isEmpty()) continue;

                    String finalCatName = catName;
                    Category category = categoryRepo.findByName(catName)
                            .orElseGet(() -> categoryRepo.save(new Category(finalCatName)));

                    game.setCategory(category);
                    category.getGames().add(game);
                }

                // genres
                for (String genreName : line[9].split(";")) {
                    genreName = genreName.trim();
                    if (genreName.isEmpty()) continue;

                    String finalGenreName = genreName;
                    Genre genre = genreRepo.findByName(genreName)
                            .orElseGet(() -> genreRepo.save(new Genre(finalGenreName)));

                    game.getGenres().add(genre);
                    genre.getGames().add(game);
                }

                // steamspy_tags
                for (String tagName : line[10].split(";")) {
                    tagName = tagName.trim();
                    if (tagName.isEmpty()) continue;

                    String finalTagName = tagName;
                    Tag tag = tagRepo.findByName(tagName)
                            .orElseGet(() -> tagRepo.save(new Tag(finalTagName)));

                    game.getTags().add(tag);
                    tag.getGames().add(game);
                }

                // achievements
                if (!line[11].isEmpty()) game.setAchievements(Integer.parseInt(line[11]));

                // positive_ratings
                if (!line[12].isEmpty()) game.setPositiveRatings(Integer.parseInt(line[12]));

                // negative_ratings
                if (!line[13].isEmpty()) game.setNegativeRatings(Integer.parseInt(line[13]));

                // average_playtime → avgPlaytime
                if (!line[14].isEmpty()) game.setAvgPlaytime(Double.parseDouble(line[14]));

                // median_playtime
                if (!line[15].isEmpty()) game.setMedianPlaytime(Double.parseDouble(line[15]));

                // owners → ownersLower, ownersUpper, ownersMid
                if (!line[16].isEmpty()) {
                    String ownersStr = line[16];
                    if (ownersStr.contains("-")) {
                        String[] parts = ownersStr.split("-");
                        int low = Integer.parseInt(parts[0]);
                        int high = Integer.parseInt(parts[1]);
                        game.setOwnersLower(low);
                        game.setOwnersUpper(high);
                        game.setOwnersMid((low + high) / 2);
                    } else {
                        int val = Integer.parseInt(ownersStr);
                        game.setOwnersLower(val);
                        game.setOwnersUpper(val);
                        game.setOwnersMid(val);
                    }
                }

                // price
                if (!line[17].isEmpty()) game.setPrice(new BigDecimal(line[17]));

                batch.add(game);

                if (batch.size() >= BATCH_SIZE) {
                    gameRepo.saveAll(batch);
                    batch.clear();
                }
            }

            // guardar último batch
            if (!batch.isEmpty()) {
                gameRepo.saveAll(batch);
            }
        }
    }
}
