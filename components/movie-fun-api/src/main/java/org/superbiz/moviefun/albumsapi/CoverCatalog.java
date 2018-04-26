package org.superbiz.moviefun.albumsapi;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.lang.String.format;

@Component
public class CoverCatalog {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlobStore blobStore;

    public CoverCatalog(BlobStore blobStore) {
        this.blobStore = blobStore;
    }

    public void uploadCover(Long albumId, InputStream uploadedFile, String contentType) throws IOException {
        Blob coverBlob = new Blob(
                getCoverBlobName(albumId),
                uploadedFile,
                contentType
        );
        blobStore.put(coverBlob);
    }

    @HystrixCommand(fallbackMethod = "buildDefaultCoverBlob")
    public Blob getCover(long albumId) {
        try {
            Optional<Blob> maybeCoverBlob = blobStore.get(getCoverBlobName(albumId));
            return maybeCoverBlob.orElseGet(this::buildDefaultCoverBlob);

        } catch (IOException e) {
            logger.warn("Unable to get cover", e);
            return buildDefaultCoverBlob();
        }
    }

    public Blob buildDefaultCoverBlob(long albumId) {
        return buildDefaultCoverBlob();
    }

    private Blob buildDefaultCoverBlob() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");
        return new Blob("default-cover", input, MediaType.IMAGE_JPEG_VALUE);
    }

    private String getCoverBlobName(long albumId) {
        return format("covers/%d", albumId);
    }
}
