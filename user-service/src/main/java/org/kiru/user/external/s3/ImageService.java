package org.kiru.user.external.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final UserPortfolioRepository userPortfolioRepository;
    private final S3Service s3Service;

    @Value("${s3.bucket.path}")
    private String path;
    @Value("${cloudfront.domain}")
    private String cachePath;

    @Transactional
    public UserPortfolio saveImages(final List<MultipartFile> images, final Long userId, String userName) {
        Queue<UserPortfolioImg> savedImages = new ConcurrentLinkedQueue<>();
        Long portfolioId = generatePortfolioId();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, images.size())
                    .mapToObj(index -> CompletableFuture.runAsync(() -> {
                        try {
                            String imagePath = s3Service.uploadImage(path, images.get(index));
                            UserPortfolioImg newImage = UserPortfolioImg.of(userId, portfolioId,cachePath + imagePath, index + 1,userName);
                            savedImages.add(newImage);
                        } catch (IOException e) {
                            throw new CompletionException(new BadRequestException(FailureCode.BAD_REQUEST));
                        }
                    }, executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            List<UserPortfolioImg> sortedImages = saveSortedImages(savedImages, portfolioId, userId);
            List<String> sortedImageUrls = sortedImages.stream().map(UserPortfolioImg::getPortfolioImageUrl)
                    .toList();
            userPortfolioRepository.saveAll(sortedImages);
            return UserPortfolio.builder().portfolioId(portfolioId).portfolioImages(sortedImageUrls).userId(userId).build();
        }
    }

    @Transactional
    public UserPortfolioImg upLoadImage(final MultipartFile image, final Long userId, final UserPortfolioImg userPortfolioImg) {
        try {
            String imagePath = s3Service.uploadImage(path, image);
            String imageUrl = cachePath + imagePath;
            UserPortfolioImg newImage = UserPortfolioImg.of(userId, userPortfolioImg.getPortfolioId(),cachePath + imagePath, userPortfolioImg.getSequence(), userPortfolioImg.getUserName());
            userPortfolioImg.portfolioImageUrl(imageUrl);
            userPortfolioRepository.save(newImage);
            return newImage;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(FailureCode.BAD_REQUEST);
        }
    }

    @Transactional
    public List<UserPortfolioImg> saveImagesWithSequence(final Map<Integer, MultipartFile> images, final Long userId, final Long portfolioId, String userName) {
        Queue<UserPortfolioImg> savedImages = new ConcurrentLinkedQueue<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = images.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        try {
                            String imagePath = s3Service.uploadImage(path, entry.getValue());
                            UserPortfolioImg newImage = UserPortfolioImg.of(userId, portfolioId, cachePath + imagePath, entry.getKey(),userName);
                            savedImages.add(newImage);
                        } catch (IOException e) {
                            throw new CompletionException(new BadRequestException(FailureCode.BAD_REQUEST));
                        }
                    }, executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return saveSortedImages(savedImages, portfolioId, userId);
        }
    }

    private Long generatePortfolioId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
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

    private List<UserPortfolioImg> saveSortedImages(Queue<UserPortfolioImg> savedImages, Long portfolioId, Long userId) {
        List<UserPortfolioImg> sortedImages = new ArrayList<>(savedImages);
        sortedImages.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        return sortedImages;
    }
}
