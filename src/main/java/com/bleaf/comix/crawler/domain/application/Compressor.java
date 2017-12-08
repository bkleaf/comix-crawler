package com.bleaf.comix.crawler.domain.application;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.google.common.io.Files;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Data
@Component
public class Compressor {
    @Autowired
    ComixConfig comixConfig;

    private String temp_path = "/Users/bleaf/Documents/comix/20171207/친구 게임 50화";
    private String temp_zip = "/Users/bleaf/Documents/comix/20171207/친구 게임 50화.zip";
    private String parent_dir="친구 게임 50화/";

    public static void main(String[] args) {
        Compressor compressor = new Compressor();
        compressor.compress(null);
    }


    public void compress(List<Comix> comixList) {
        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(new File(temp_zip))) {

            ZipArchiveEntry zipArchiveEntry;

            Path tempPath = Paths.get(temp_path);

            String[] files = tempPath.toFile().list();

            File file;
            for(String f : files) {
                file = new File(temp_path + File.separator + f);

                zipArchiveEntry = new ZipArchiveEntry(file.getName());
                zipArchiveEntry.setSize(file.length());

                zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                zipArchiveOutputStream.write(Files.toByteArray(file));
                zipArchiveOutputStream.closeArchiveEntry();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
