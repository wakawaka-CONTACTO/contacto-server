package org.kiru.user.external.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.user.portfolio.common.PortfolioIdGenerator;
import org.kiru.user.portfolio.service.out.SaveUserPortfolioPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final SaveUserPortfolioPort saveUserPortfolioPort;
    private final S3Service s3Service;
    private final PortfolioIdGenerator portfolioIdGenerator;

    @Value("${s3.bucket.path}")
    private String path;
    @Value("${cloudfront.domain}")
    private String cachePath;

    @Transactional
    public List<UserPortfolioItem> saveImages(final List<MultipartFile> images, final Long userId, String userName) {
        List<UserPortfolioItem> savedImages = Collections.synchronizedList(new ArrayList<>());
        Long portfolioId = portfolioIdGenerator.generatePortfolioId();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, images.size())
                    .mapToObj(index -> CompletableFuture.runAsync(() -> {
                        try {
                            String imagePath = s3Service.uploadImage(path, images.get(index));
                            UserPortfolioItem newImage = UserPortfolioImg.of(userId, portfolioId,cachePath + imagePath, index + 1,userName);
                            savedImages.add(newImage);
                        } catch (IOException e) {
                            throw new CompletionException(new BadRequestException(FailureCode.BAD_REQUEST));
                        }
                    }, executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            saveUserPortfolioPort.saveAll(savedImages);
            return savedImages;
        }
    }

    @Transactional
    public UserPortfolioItem upLoadImage(final MultipartFile image, final Long userId, final UserPortfolioImg userPortfolioImg) {
        try {
            String imagePath = s3Service.uploadImage(path, image);
            String imageUrl = cachePath + imagePath;
            UserPortfolioItem newImage = UserPortfolioImg.of(userId, userPortfolioImg.getPortfolioId(),cachePath + imagePath, userPortfolioImg.getSequence(), userPortfolioImg.getUserName());
            userPortfolioImg.portfolioImageUrl(imageUrl);
            saveUserPortfolioPort.save(newImage);
            return newImage;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(FailureCode.BAD_REQUEST);
        }
    }

    @Transactional
    public List<UserPortfolioItem> saveImagesS3WithSequence(final Map<Integer, Object> changedPortfolioImages , UserPortfolio userPortfolio, String username) {
        Map<Integer,MultipartFile> images = UserPortfolio.findUpdateItem(changedPortfolioImages);
        List<UserPortfolioItem> savedImages = Collections.synchronizedList(new ArrayList<>());
        Long portfolioId = userPortfolio.getPortfolioId() == null ? portfolioIdGenerator.generatePortfolioId() : userPortfolio.getPortfolioId();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = images.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        try {
                            String imagePath = s3Service.uploadImage(path, entry.getValue());
                            UserPortfolioItem newImage = UserPortfolioImg.of(userPortfolio.getUserId(),
                                    portfolioId, cachePath + imagePath,
                                    entry.getKey(), username);
                            savedImages.add(newImage);
                        } catch (IOException e) {
                            throw new CompletionException(new BadRequestException(FailureCode.BAD_REQUEST));
                        }
                    }, executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return savedImages;
        }
    }

    @Transactional
    public List<UserPortfolioItem> verifyExistingImages(final Map<Integer, Object> changedPortfolioImages, UserPortfolio userPortfolio, String username) {
        Map<Integer, String> existingImages = userPortfolio.findExistingItem(changedPortfolioImages);
        Long portfolioId = userPortfolio.getPortfolioId() == null ? portfolioIdGenerator.generatePortfolioId() : userPortfolio.getPortfolioId();
        List<UserPortfolioItem> savedImages = new ArrayList<>();
        existingImages.forEach((sequence, imageUrl) -> {
            UserPortfolioItem item = UserPortfolioImg.of(userPortfolio.getUserId(),
                portfolioId, imageUrl, sequence, username);
            savedImages.add(item);
        });
        return savedImages;
    }

    public String getImageUrl(final MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        try {
            return cachePath + s3Service.uploadImage("/user", image);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(FailureCode.WRONG_IMAGE_URL);
        }
    }
}
