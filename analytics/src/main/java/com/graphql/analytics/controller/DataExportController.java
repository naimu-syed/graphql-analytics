package com.graphql.analytics.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.graphql.analytics.entity.DataRecord;
import com.graphql.analytics.repository.DataRecordRepository;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/export")
public class DataExportController {

    @Autowired
    private DataRecordRepository repository;

    @GetMapping("/csv")
    public void exportDataToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=data_records.csv");

        List<DataRecord> records = repository.findAll();
        PrintWriter writer = response.getWriter();
        writer.println("ID,Name,Salary");

        for (DataRecord record : records) {
            writer.println(record.getId() + "," + record.getName() + "," + record.getSalary());
        }
    }
}
