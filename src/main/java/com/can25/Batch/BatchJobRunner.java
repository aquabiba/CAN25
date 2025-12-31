package com.can25.Batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class BatchJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job spectatorJob;

    public BatchJobRunner(JobLauncher jobLauncher, Job spectatorJob) {
        this.jobLauncher = jobLauncher;
        this.spectatorJob = spectatorJob;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("===== LANCEMENT DU JOB BATCH =====");

        Path inputDir = Paths.get("input");

        try (Stream<Path> files = Files.list(inputDir)) {

            files
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".json") || f.toString().endsWith(".xml"))
                    .forEach(file -> {

                        String inputType = file.toString().endsWith(".xml") ? "xml" : "json";

                        System.out.println(
                                "Traitement du fichier : " + file.getFileName() +
                                        " | type = " + inputType
                        );

                        JobParameters params = new JobParametersBuilder()
                                .addLong("startAt", System.currentTimeMillis())
                                .addString("inputType", inputType)
                                .addString("inputFile", file.toAbsolutePath().toString())
                                .toJobParameters();

                        try {
                            jobLauncher.run(spectatorJob, params);
                        } catch (Exception e) {
                            throw new RuntimeException("Erreur lors du traitement de " + file, e);
                        }
                    });
        }

        System.out.println("===== TOUS LES FICHIERS ONT ÉTÉ TRAITÉS =====");
    }
}
