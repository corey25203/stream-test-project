package com.ddcr.example.stream.controller;

import com.ddcr.example.stream.dao.model.UploadFileResponse;
import com.ddcr.example.stream.service.FileParseService;
import com.ddcr.example.stream.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


@RestController
public class SourceFrameOperationsController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileParseService fileParseService;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @CrossOrigin
    @PostMapping("/uploadFile")
    public Mono<ResponseEntity<List<UploadFileResponse>>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {

        return fileStorageService.storeFile(filePartMono).map(
                (filename) -> ResponseEntity
                        .ok().body(Arrays.asList(
                                new UploadFileResponse(filename,
                                        UriComponentsBuilder.newInstance().path("/download/{filename}")
                                                .buildAndExpand(filename).toUriString(),
                                        UriComponentsBuilder.newInstance().path("/parseFile/{filename}")
                                                .buildAndExpand(filename).toUriString(),
                                        "", 0))));
    }

    @CrossOrigin
    @GetMapping("/download/{path:.+}")
    public ResponseEntity<Flux<DataBuffer>> getFile(@PathVariable String path) {
        Flux<DataBuffer> file = fileStorageService.load(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
    }

    @CrossOrigin
    @GetMapping("/files")
    public ResponseEntity<Flux<UploadFileResponse>> getFilesList() {

        Stream<UploadFileResponse> fileInfoStream = fileStorageService.listFiles().map(path -> {
            String filename = path.getFileName().toString();
            String downloadUrl = UriComponentsBuilder.newInstance().path("/download/{filename}")
                    .buildAndExpand(filename).toUriString();
            String parseUrl = UriComponentsBuilder.newInstance().path("/parseFile/{filename}")
                    .buildAndExpand(filename).toUriString();
            return new UploadFileResponse(filename, downloadUrl, parseUrl, "", 0);
        });

        Flux<UploadFileResponse> fileInfosFlux = Flux.fromStream(fileInfoStream);

        return ResponseEntity.status(HttpStatus.OK).body(fileInfosFlux);
    }


    @CrossOrigin
    @GetMapping("/parseFile/{fileName:.+}")
    public ResponseEntity<Flux<Resource>> parseFile(@PathVariable String fileName) {

        Resource resource = fileStorageService.loadFileAsResource(fileName);
        fileParseService.parseAndSendToKafka(resource);

        Stream<Resource> fileInfoStream = Stream.<Resource>builder().build();
        Flux<Resource> fileInfosFlux = Flux.fromStream(fileInfoStream);

        return ResponseEntity.status(HttpStatus.OK).body(fileInfosFlux);
    }

}
