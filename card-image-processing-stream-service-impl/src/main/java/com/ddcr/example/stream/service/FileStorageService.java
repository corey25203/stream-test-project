package com.ddcr.example.stream.service;

import com.ddcr.example.stream.FileUploadAndFrameStreamApplicationProperties;
import com.ddcr.example.stream.exceptions.FileNotFoundException;
import com.ddcr.example.stream.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileUploadAndFrameStreamApplicationProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("creating storage dir exception", ex);
        }
    }

    public Mono<String> storeFile(Mono<FilePart> filePartMono) {

        return filePartMono.doOnNext(fp -> log.info("receiving {}", fp.filename())).flatMap(filePart -> {
            String filename = filePart.filename();
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            return filePart.transferTo(targetLocation).then(Mono.just(filename));
        });
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("file not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("file not found " + fileName, ex);
        }
    }

    public Flux<DataBuffer> load(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096);
            } else {
                throw new RuntimeException("load file exception");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("err: " + e.getMessage());
        }
    }

    public Stream<Path> listFiles() {

        try {
            return Files.walk(this.fileStorageLocation, 1)
                    .filter(path -> !path.equals(this.fileStorageLocation))
                    .map(this.fileStorageLocation::relativize);
        } catch (IOException e) {
            throw new RuntimeException("list files exception");
        }
    }
}