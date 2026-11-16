package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.User;
import ru.skambet.domain.VerificationDocument;
import ru.skambet.domain.VerificationRequest;
import ru.skambet.domain.VerificationStatus;
import ru.skambet.repository.UserRepository;
import ru.skambet.repository.VerificationDocumentRepository;
import ru.skambet.repository.VerificationRequestRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Feature 19: verification of identity. The user sends a photo or a scan of a document,
 * the administrator approves or rejects it. Withdrawals are possible only after approval.
 */
@Service
public class VerificationService {

    public static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Allowed formats: the extension and the first bytes of the file must match,
     * so a renamed text file is not accepted as a picture.
     */
    private enum Format {
        JPEG("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, "jpg", "jpeg"),
        PNG("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}, "png"),
        PDF("application/pdf", new byte[]{'%', 'P', 'D', 'F'}, "pdf");

        private final String contentType;
        private final byte[] signature;
        private final List<String> extensions;

        Format(String contentType, byte[] signature, String... extensions) {
            this.contentType = contentType;
            this.signature = signature;
            this.extensions = List.of(extensions);
        }

        static Optional<Format> byExtension(String fileName) {
            int dot = fileName.lastIndexOf('.');
            String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
            return Arrays.stream(values()).filter(f -> f.extensions.contains(extension)).findFirst();
        }

        boolean matches(byte[] content) {
            return content.length >= signature.length
                    && Arrays.equals(Arrays.copyOf(content, signature.length), signature);
        }
    }

    /**
     * @param verified      identity is approved
     * @param latestRequest the last request of the user, if any
     */
    public record State(boolean verified, User user, VerificationRequest latestRequest) {

        public boolean isUnderReview() {
            return latestRequest != null && latestRequest.isPending();
        }

        public boolean canSubmit() {
            return !verified && !isUnderReview();
        }
    }

    public record Document(String fileName, String contentType, byte[] content) {
    }

    private final VerificationRequestRepository requestRepository;
    private final VerificationDocumentRepository documentRepository;
    private final UserRepository userRepository;

    public VerificationService(VerificationRequestRepository requestRepository,
                               VerificationDocumentRepository documentRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public State getState(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return new State(user.isVerified(), user,
                requestRepository.findFirstByUserUsernameOrderByCreatedAtDescIdDesc(username).orElse(null));
    }

    @Transactional
    public VerificationRequest submit(String username, String originalFileName, byte[] content) {
        if (content == null || content.length == 0 || originalFileName == null || originalFileName.isBlank()) {
            throw new VerificationException("Выберите файл документа");
        }
        if (content.length > MAX_FILE_SIZE) {
            throw new VerificationException("Размер файла — не больше 5 МБ");
        }
        String fileName = cleanFileName(originalFileName);
        Format format = Format.byExtension(fileName)
                .orElseThrow(() -> new VerificationException("Допустимые форматы: JPG, PNG, PDF"));
        if (!format.matches(content)) {
            throw new VerificationException("Файл повреждён или не соответствует формату " + format.name());
        }

        User user = userRepository.findForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (user.isVerified()) {
            throw new VerificationException("Личность уже подтверждена");
        }
        if (requestRepository.existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING)) {
            throw new VerificationException("Документ уже на проверке");
        }
        VerificationRequest request = requestRepository.save(
                new VerificationRequest(user, fileName, format.contentType, content.length));
        documentRepository.save(new VerificationDocument(request.getId(), content));
        return request;
    }

    @Transactional(readOnly = true)
    public List<VerificationRequest> getPendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtAscIdAsc(VerificationStatus.PENDING);
    }

    @Transactional
    public VerificationRequest approve(Long requestId) {
        VerificationRequest request = lockPending(requestId);
        request.approve();
        return request;
    }

    @Transactional
    public VerificationRequest reject(Long requestId, String reason) {
        VerificationRequest request = lockPending(requestId);
        request.reject(reason);
        return request;
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long requestId) {
        VerificationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new VerificationException("Заявка не найдена"));
        VerificationDocument document = documentRepository.findById(requestId)
                .orElseThrow(() -> new VerificationException("Файл не найден"));
        return new Document(request.getFileName(), request.getContentType(), document.getContent());
    }

    private VerificationRequest lockPending(Long requestId) {
        VerificationRequest request = requestRepository.findForUpdate(requestId)
                .orElseThrow(() -> new VerificationException("Заявка не найдена"));
        if (!request.isPending()) {
            throw new VerificationException("Заявка уже рассмотрена");
        }
        return request;
    }

    // only the name itself, without a path the browser might send
    private static String cleanFileName(String name) {
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).trim();
        return base.length() > 100 ? base.substring(base.length() - 100) : base;
    }
}
