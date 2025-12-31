package com.can25.Batch;

import com.can25.Entity.SeatLocation;
import com.can25.Entity.Spectator;
import com.can25.Entity.SpectatorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class BatchConfig {

    // =====================================================
    // JSON READER
    // =====================================================
    private ItemStreamReader<SpectatorDTO> jsonItemReader(String inputFile) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JacksonJsonObjectReader<SpectatorDTO> reader =
                new JacksonJsonObjectReader<>(SpectatorDTO.class);
        reader.setMapper(mapper);

        return new JsonItemReaderBuilder<SpectatorDTO>()
                .name("jsonSpectatorReader")
                .resource(new FileSystemResource(inputFile))
                .jsonObjectReader(reader)
                .strict(true)
                .build();
    }

    // =====================================================
    // XML READER
    // =====================================================
    private StaxEventItemReader<SpectatorDTO> xmlItemReader(String inputFile) {

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(Map.of(
                "Spectator", SpectatorDTO.class,
                "seatLocation", SeatLocation.class
        ));

        // REQUIRED since XStream 1.4+
        marshaller.setTypePermissions(AnyTypePermission.ANY);

        return new StaxEventItemReaderBuilder<SpectatorDTO>()
                .name("xmlSpectatorReader")
                .resource(new FileSystemResource(inputFile))
                .addFragmentRootElements("Spectator")
                .unmarshaller(marshaller)
                .build();
    }

    // =====================================================
    // DYNAMIC READER
    // =====================================================
    @Bean
    @StepScope
    public ItemStreamReader<SpectatorDTO> dynamicItemReader(
            @Value("#{jobParameters['inputType']}") String inputType,
            @Value("#{jobParameters['inputFile']}") String inputFile) {

        if ("xml".equalsIgnoreCase(inputType)) {
            return xmlItemReader(inputFile);
        }
        return jsonItemReader(inputFile);
    }

    // =====================================================
    // STEP
    // =====================================================
    @Bean
    public Step spectatorStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemStreamReader<SpectatorDTO> dynamicItemReader,
            ItemProcessor<SpectatorDTO, Spectator> processor,
            DatabaseWriter writer) {

        return new StepBuilder("spectatorStep", jobRepository)
                .<SpectatorDTO, Spectator>chunk(10, transactionManager)
                .reader(dynamicItemReader)
                .processor(processor)
                .writer(writer)
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
