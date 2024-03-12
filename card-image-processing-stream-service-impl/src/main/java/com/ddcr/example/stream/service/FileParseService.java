package com.ddcr.example.stream.service;

import com.ddcr.example.stream.FileUploadAndFrameStreamApplicationProperties;
import com.ddcr.example.stream.dao.model.SourceFrame;
import com.ddcr.example.stream.exceptions.FileStorageException;
import com.ddcr.example.stream.kafka.SourceFrameKafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;


@Service
@Slf4j
public class FileParseService {
    @Autowired
    private SourceFrameKafkaProducer sourceFrameKafkaProducer;


    private static final Logger logger = LoggerFactory.getLogger(FileParseService.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final Path fileStorageLocation;


    public FileParseService(FileUploadAndFrameStreamApplicationProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("err creating storage directory", ex);
        }
    }

    public String parseAndSendToKafka(Resource resource) {

        String fileName = null;
        try {
            fileName = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (fileName.contains("..")) {
            throw new FileStorageException("filename contains invalid path " + fileName);
        }

        Mat frame = new Mat();
        VideoCapture camera = new VideoCapture(fileName);

        int frameReadsCount = 0;
        while (camera.read(frame)) {

            log.info("frameReadsCount {}", frameReadsCount);

            String base64EncodedFrame = bufferedImage(frame);
            ++frameReadsCount;

            SourceFrame sourceFrame = new SourceFrame();
            sourceFrame.setData(base64EncodedFrame);
            //  sourceFrame.setId(String.valueOf(randomNum));
            sourceFrame.setId(String.valueOf(frameReadsCount));
            sourceFrameKafkaProducer.sendMessages(sourceFrame);
        }

        return fileName;
    }

    public static String bufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        Mat src = mat.clone();

        /*
        //resize
        Mat resizeimage = new Mat();
        Size scaleSize = new Size(80, 150);// *7
        Imgproc.resize(src, resizeimage, scaleSize, 0, 0, INTER_AREA);
        */

        MatOfByte matOfByte = new MatOfByte();
        //Imgcodecs.imencode(".jpg", resizeimage, matOfByte2);
        Imgcodecs.imencode(".jpg", src, matOfByte);

        String result = Base64.getEncoder().encodeToString(matOfByte.toArray());
        log.info("image to string array size {}", matOfByte.size());
        return result;
    }

}