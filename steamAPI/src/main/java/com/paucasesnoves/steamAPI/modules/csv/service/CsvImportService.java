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
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        System.out.println("=== INICIANDO IMPORTACIÓN POR ORDEN ===");

        // 1. PRIMERO: Importar juegos (steam.csv)
        Resource gameResource = resolver.getResource("classpath:/data/steam.csv");
        if (gameResource.exists()) {
            System.out.println("Importando juegos desde: steam.csv");
            gameImporter.importCsv(gameResource.getInputStream());
            System.out.println("Juegos importados correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam.csv");
        }

        // Pausa para asegurar que los juegos están guardados
        Thread.sleep(1000);

        // 2. SEGUNDO: Importar tags
        Resource tagResource = resolver.getResource("classpath:/data/steamspy_tag_data.csv");
        if (tagResource.exists()) {
            System.out.println("Importando tags desde: steamspy_tag_data.csv");
            tagImporter.importCsv(tagResource.getInputStream());
            System.out.println("Tags importados correctamente");
        }

        // 3. TERCERO: Importar descripciones
        Resource descResource = resolver.getResource("classpath:/data/steam_description_data.csv");
        if (descResource.exists()) {
            System.out.println("Importando descripciones desde: steam_description_data.csv");
            descriptionImporter.importCsv(descResource.getInputStream());
            System.out.println("Descripciones importadas correctamente");
        }

        // 4. CUARTO: Importar media
        Resource mediaResource = resolver.getResource("classpath:/data/steam_media_data.csv");
        if (mediaResource.exists()) {
            System.out.println("Importando media desde: steam_media_data.csv");
            mediaImporter.importCsv(mediaResource.getInputStream());
            System.out.println("Media importada correctamente");
        }

        // 5. QUINTO: Importar requisitos
        Resource reqResource = resolver.getResource("classpath:/data/steam_requirements_data.csv");
        if (reqResource.exists()) {
            System.out.println("Importando requisitos desde: steam_requirements_data.csv");
            requirementsImporter.importCsv(reqResource.getInputStream());
            System.out.println("Requisitos importados correctamente");
        }

        // 6. SEXTO: Importar soporte
        Resource supportResource = resolver.getResource("classpath:/data/steam_support_info.csv");
        if (supportResource.exists()) {
            System.out.println("Importando soporte desde: steam_support_info.csv");
            supportImporter.importCsv(supportResource.getInputStream());
            System.out.println("Soporte importado correctamente");
        }

        System.out.println("=== IMPORTACIÓN COMPLETADA ===");
    }

    // === MÉTODOS INDIVIDUALES PARA IMPORTAR POR PARTES ===

    public void importGamesOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource gameResource = resolver.getResource("classpath:/data/steam.csv");
        if (gameResource.exists()) {
            System.out.println("Importando SOLO juegos desde: steam.csv");
            gameImporter.importCsv(gameResource.getInputStream());
            System.out.println("Juegos importados correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam.csv");
        }
    }

    public void importTagsOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource tagResource = resolver.getResource("classpath:/data/steamspy_tag_data.csv");
        if (tagResource.exists()) {
            System.out.println("Importando SOLO tags desde: steamspy_tag_data.csv");
            tagImporter.importCsv(tagResource.getInputStream());
            System.out.println("Tags importados correctamente");
        } else {
            System.out.println("ERROR: No se encontró steamspy_tag_data.csv");
        }
    }

    public void importDescriptionsOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource descResource = resolver.getResource("classpath:/data/steam_description_data.csv");
        if (descResource.exists()) {
            System.out.println("Importando SOLO descripciones desde: steam_description_data.csv");
            descriptionImporter.importCsv(descResource.getInputStream());
            System.out.println("Descripciones importadas correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam_description_data.csv");
        }
    }

    public void importMediaOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource mediaResource = resolver.getResource("classpath:/data/steam_media_data.csv");
        if (mediaResource.exists()) {
            System.out.println("Importando SOLO media desde: steam_media_data.csv");
            mediaImporter.importCsv(mediaResource.getInputStream());
            System.out.println("Media importada correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam_media_data.csv");
        }
    }

    public void importRequirementsOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource reqResource = resolver.getResource("classpath:/data/steam_requirements_data.csv");
        if (reqResource.exists()) {
            System.out.println("Importando SOLO requisitos desde: steam_requirements_data.csv");
            requirementsImporter.importCsv(reqResource.getInputStream());
            System.out.println("Requisitos importados correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam_requirements_data.csv");
        }
    }

    public void importSupportOnly() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource supportResource = resolver.getResource("classpath:/data/steam_support_info.csv");
        if (supportResource.exists()) {
            System.out.println("Importando SOLO soporte desde: steam_support_info.csv");
            supportImporter.importCsv(supportResource.getInputStream());
            System.out.println("Soporte importado correctamente");
        } else {
            System.out.println("ERROR: No se encontró steam_support_info.csv");
        }
    }

    // === MÉTODOS PARA VERIFICAR ARCHIVOS ===

    public String checkFiles() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        StringBuilder result = new StringBuilder();
        result.append("=== ARCHIVOS ENCONTRADOS EN /data ===\n");

        String[] files = {"steam.csv", "steamspy_tag_data.csv", "steam_description_data.csv",
                "steam_media_data.csv", "steam_requirements_data.csv", "steam_support_info.csv"};

        for (String filename : files) {
            Resource resource = resolver.getResource("classpath:/data/" + filename);
            if (resource.exists()) {
                result.append("✓ ").append(filename).append("\n");
            } else {
                result.append("✗ ").append(filename).append(" (NO ENCONTRADO)\n");
            }
        }

        return result.toString();
    }
}