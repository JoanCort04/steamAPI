package com.paucasesnoves.steamAPI.modules.csv.controller;

import com.paucasesnoves.steamAPI.modules.csv.service.CsvImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsvImportController {

    @Autowired
    private CsvImportService csvImportService;

    @GetMapping("/import-csv")
    public String importCsv() {
        try {
            csvImportService.importAllCsv();
            return "Importación completada correctamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error durante la importación: " + e.getMessage();
        }
    }
}
