package com.ddcr.example.stream.dao.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SourceFrame {

    String id;
    String streamId;
    String sessionId;
    String data;
    String description;
    String status;
    String dataContentInfo;

}
