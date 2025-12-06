package com.can25.Batch;

import com.can25.Entity.SeatLocation;
import com.can25.Entity.Spectator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfig {

    @Bean
    public JsonItemReader<Spectator> jsonItemReader() {
        ObjectMapper objectMapper = new ObjectMapper();

        JacksonJsonObjectReader<Spectator> jsonObjectReader =
                new JacksonJsonObjectReader<>(Spectator.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<Spectator>()
                .name("jsonSpectatorReader")
                .jsonObjectReader(jsonObjectReader)
                .resource(new ClassPathResource("spectators_json.json"))
                .strict(false)
                .build();
    }
    @Bean
    public StaxEventItemReader<Spectator> xmlItemReader() {

        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("Spectator", Spectator.class);
        aliases.put("seatLocation", SeatLocation.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);

        return new StaxEventItemReaderBuilder<Spectator>()
                .name("xmlSpectatorReader")
                .resource(new FileSystemResource("spectators_xml.xml"))
                .addFragmentRootElements("Spectator")
                .unmarshaller(marshaller)
                .build();
    }


    @Bean
    public Step spectatorStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("jsonItemReader") ItemReader<Spectator> reader,
            ItemProcessor<Spectator, Spectator> processor,
            DatabaseWriter writer) {  // ✅ Injection directe de DatabaseWriter

        return new StepBuilder("spectatorStep", jobRepository)
                .<Spectator, Spectator>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)  // ✅ writer est DatabaseWriter
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }

    // =====================================================
    // JOB
    // =====================================================

    @Bean
    public Job spectatorJob(JobRepository jobRepository, Step spectatorStep) {
        return new JobBuilder("spectatorJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(spectatorStep)
                .build();
    }

    

}