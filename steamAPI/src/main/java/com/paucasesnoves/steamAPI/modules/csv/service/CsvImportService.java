package com.paucasesnoves.steamAPI.modules.csv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class CsvImportService {

    @Autowired
    private GameCsvImporter gameImporter;

    @Autowired
    private TagCsvImporter tagImporter;

    @Autowired
    private DescriptionCsvImporter descriptionImporter;

    @Autowired
    private MediaCsvImporter mediaImporter;

    @Autowired
    private RequirementsCsvImporter requirementsImporter;

    @Autowired
    private SupportCsvImporter supportImporter;

    public void importAllCsv() throws Exception {
        // Resolver para todos los CSV en /resources/data
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/data/*.csv");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            System.out.println("Importando archivo: " + filename);

            switch (filename) {
                case "steam.csv":
                    gameImporter.importCsv(resource.getInputStream());
                    break;
                case "steamspy_tag_data.csv":
                    tagImporter.importCsv(resource.getInputStream());
                    break;
                case "steam_description_data.csv":
                    descriptionImporter.importCsv(resource.getInputStream());
                    break;
                case "steam_media_data.csv":
                    mediaImporter.importCsv(resource.getInputStream());
                    break;
                case "steam_requirements_data.csv":
                    requirementsImporter.importCsv(resource.getInputStream());
                    break;
                case "steam_support_info.csv":
                    supportImporter.importCsv(resource.getInputStream());
                    break;
                default:
                    System.out.println("Archivo sin importador definido: " + filename);
            }
        }
    }
}
