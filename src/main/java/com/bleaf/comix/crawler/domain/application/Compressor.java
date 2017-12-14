package com.bleaf.comix.crawler.domain.application;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.domain.dto.Comix;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Data
@Component
public class Compressor {
    @Autowired
    ComixConfig comixConfig;

    public int zip(List<Comix> comixList) {
        Path servicePath;

        Path zipPath, comixPath;

        ZipArchiveEntry zipArchiveEntry;

        int count = 0;

        ComixPageFilter comixPageFilter = new ComixPageFilter();
        File pageFile;
        for (Comix comix : comixList) {

            comixPath = comix.getDownloadPath();

            if (!Files.exists(comixPath)) continue;

            servicePath = comix.getServicePath();
            zipPath = Paths.get(servicePath.toString() + ".zip");

            comixPageFilter.setExts(comix.getExts());

            try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(zipPath.toFile());
                 DirectoryStream<Path> directoryStream = Files.newDirectoryStream(comixPath, comixPageFilter)) {

                for (Path page : directoryStream) {
                    pageFile = page.toFile();
                    zipArchiveEntry = new ZipArchiveEntry(pageFile.getName());
                    zipArchiveEntry.setSize(pageFile.length());

                    zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
                    zipArchiveOutputStream.write(com.google.common.io.Files.toByteArray(pageFile));
                    zipArchiveOutputStream.closeArchiveEntry();
                }

                zipArchiveOutputStream.finish();
                zipArchiveOutputStream.flush();

                log.debug(" ### compress zip = {}", zipPath.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

            count += 1;
        }

        return count;
    }
}

@Slf4j
@Data
class ComixPageFilter implements DirectoryStream.Filter<Path> {
    List<String> exts;

    @Override
    public boolean accept(Path entry) throws IOException {
        String ext = com.google.common.io.Files.getFileExtension(entry.getFileName().toString());

        return (Files.isRegularFile(entry) && exts.contains(ext));
    }
}