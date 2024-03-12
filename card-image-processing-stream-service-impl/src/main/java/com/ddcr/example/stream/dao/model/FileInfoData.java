package com.ddcr.example.stream.dao.model;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FileInfoData {

    String name;
    String type;
    String size;
    String encodedData;
    String storageURL;

}
