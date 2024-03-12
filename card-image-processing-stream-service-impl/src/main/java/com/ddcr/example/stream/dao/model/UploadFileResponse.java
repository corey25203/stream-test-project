package com.ddcr.example.stream.dao.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UploadFileResponse {
    private String fileName;
    private String fileDownloadUrl;
    private String fileParseUrl;
    private String fileType;
    private long size;


}
