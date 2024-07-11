package ru.seeker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.seeker.entity.FileStory;
import ru.seeker.repository.FilesStoryRepository;

import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {
    private final ParsedRowService parsedRowService;
    private final FilesStoryRepository storyRepository;

    public ResponseEntity<HttpStatus> deleteAllDataByDocName(String docName) {
        return parsedRowService.deleteAllDataByDocName(docName);
    }

    public ResponseEntity<HttpStatus> deleteAllDataBySheetName(String sheetName) {
        return parsedRowService.deleteAllDataBySheetName(sheetName);
    }

    public Page<FileStory> findAllDocuments(int count, int page) {
        return storyRepository.findAll(Pageable.ofSize(count).withPage(page));
//        PageImpl(List<T> content, Pageable pageable, long total);
    }

//    public void storeFile(MultipartFile file) {
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", file.getResource());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        MultiFile response = restTemplate.exchange(
//                props.getStorageUrl(),
//                HttpMethod.POST,
//                requestEntity,
//                MultiFile.class).getBody();
//    }

//    public void deleteFile(String file) {
//        Optional<UserRegisterDTO> user = userService.findFirstByPartnerId(partnerDto.getPartnerId());
//        if (user.isPresent()) {
//            boolean f = false;
//            Set<MultiFile> resFiles = new HashSet<>();
//            for (MultiFileDTO multiFileDTO : user.get().getFiles()) {
//                if (multiFileDTO.getUri().equals(file)) {
//                    f = true;
//                } else {
//                    resFiles.add(fileMapper.toEntity(multiFileDTO));
//                }
//            }
//            if (!f) {
//                return ResponseEntity.ok().build();
//            }
//            UserRegister userRegister = userMapper.toEntity(user.get());
//            userRegister.setFiles(resFiles);
//            userService.saveUser(userRegister);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(getThoseAccessToken());
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String url = props.getStorageUrl() + "?file=" + file;
//
//            log.info("Попытка удалить из хранилища файл '{}'. Источник: {}", file, source);
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
//            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
//                if (source != null && source.equals("front")) {
//                    log.info("Удаление файла '{}' из CMS...", file);
//                    cmsStorageService.deleteCmsFile(file);
//                }
//                log.info("Удаление файла '{}' успешно.", file);
//                return ResponseEntity.ok().build();
//            } else {
//                throw new GlobalServiceException(ErrorMessages.FILE_NOT_DELETE);
//            }
//        } else {
//            throw new GlobalServiceException(ErrorMessages.LOGIN_CHECK_DB_USER_ABSENT);
//        }
//    }

//    public ResponseEntity<HttpStatus> deleteFileWOPC(String file) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(getThoseAccessToken());
//
//        log.info("Попытка удалить из хранилища файл '{}'. Источник: {}", file, source);
//        ResponseEntity<String> response = restTemplate.exchange(
//                props.getStorageUrl() + "?file=" + file,
//                HttpMethod.DELETE,
//                new HttpEntity<>(headers),
//                String.class);
//        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
//            if (source != null && source.equals("front")) {
//                log.info("Удаление файла '{}' из CMS...", file);
//                cmsStorageService.deleteCmsFile(file);
//            }
//            log.info("Удаление файла '{}' успешно.", file);
//            return ResponseEntity.ok().build();
//        } else {
//            throw new GlobalServiceException(ErrorMessages.FILE_NOT_DELETE);
//        }
//    }

//    public ResponseEntity<byte[]> getFile(String file) {
//        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("someStorageUrl").queryParam("file", file);
//        log.info("Запрос из хранилища файла '{}'", file);
//        return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, byte[].class);
//    }

//    private String getThoseAccessToken() throws JsonProcessingException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", props.getStorageAuthorization());
//
//        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(props.getStorageTokenUrl())
//                .queryParam("client_id", props.getStorageClientId())
//                .queryParam("secret", props.getStorageSecret())
//                .queryParam("username", props.getStorageUsername())
//                .queryParam("password", props.getStoragePassword())
//                .queryParam("grant_type", props.getStorageGrantType());
//
//        log.info("Попытка получить AToken для удаления...");
//        ResponseEntity<String> response = restTemplate.exchange(
//                builder.toUriString(),
//                HttpMethod.POST,
//                new HttpEntity<>(null, headers),
//                String.class);
//        log.debug(response.getBody());
//        return mapper.readTree(response.getBody()).get("access_token").asText();
//    }
}
