package com.teamloci.loci.global.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FileDto {

    @Getter
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String fileUrl;
    }
}