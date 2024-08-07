package com.graphql.analytics.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.graphql.analytics.entity.DataRecord;
import com.graphql.analytics.repository.DataRecordRepository;

@Controller
public class DataRecordController {

    @Autowired
    private DataRecordRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @QueryMapping
    public List<DataRecord> allRecords(
            @Argument Optional<String> nameFilter,
            @Argument Optional<String> sortBy,
            @Argument Optional<String> sortDirection) {
        List<DataRecord> records = repository.findAll();

        // Apply name filter if present
        if (nameFilter.isPresent()) {
            records = records.stream()
                    .filter(record -> record.getName().contains(nameFilter.get()))
                    .collect(Collectors.toList());
        }

        // Apply sorting if sortBy is present
        if (sortBy.isPresent()) {
            if (sortBy.get().equals("name")) {
                if (sortDirection.isPresent() && sortDirection.get().equalsIgnoreCase("desc")) {
                    records.sort((a, b) -> b.getName().compareTo(a.getName()));
                } else {
                    records.sort((a, b) -> a.getName().compareTo(b.getName()));
                }
            } else if (sortBy.get().equals("salary")) {
                if (sortDirection.isPresent() && sortDirection.get().equalsIgnoreCase("desc")) {
                    records.sort((a, b) -> Integer.compare(b.getSalary(), a.getSalary()));
                } else {
                    records.sort((a, b) -> Integer.compare(a.getSalary(), b.getSalary()));
                }
            }
        }

        return records; 
    }

    @QueryMapping
    public Optional<DataRecord> recordById(@Argument Integer id) {
        return repository.findById(id);
    }

    @MutationMapping
    public DataRecord createRecord(@Argument String name, @Argument Integer salary) {
        DataRecord record = new DataRecord(name, salary);
        DataRecord savedRecord = repository.save(record);
        messagingTemplate.convertAndSend("/topic/records", savedRecord);
        return savedRecord;
    }

    @MutationMapping
    public DataRecord updateRecord(@Argument Integer id, @Argument String name, @Argument Integer salary) {
        Optional<DataRecord> optionalRecord = repository.findById(id);
        if (optionalRecord.isPresent()) {
            DataRecord record = optionalRecord.get();
            record.setName(name != null ? name : record.getName());
            record.setSalary(salary != null ? salary : record.getSalary());
            DataRecord updatedRecord = repository.save(record);
            messagingTemplate.convertAndSend("/topic/records", updatedRecord);
            return updatedRecord;
        } else {
            return null;
        }
    }

    @MutationMapping
    public Boolean deleteRecord(@Argument Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            messagingTemplate.convertAndSend("/topic/records", id);
            return true;
        }
        return false;
    }
}
