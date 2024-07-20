package ru.seeker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seeker.dto.SheetDTO;
import ru.seeker.entity.FileStory;
import ru.seeker.mapper.SheetMapper;
import ru.seeker.repository.FilesStoryRepository;
import ru.seeker.repository.SheetRepository;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {
    private final FilesStoryRepository storyRepository;
    private final SheetRepository sheetRepository;
    private final SheetMapper sheetMapper;

    public ResponseEntity<HttpStatus> deleteAllDataBySheetUuid(UUID sheetUuid) {
        boolean isExists = sheetRepository.existsByUuid(sheetUuid);
        if (!isExists) {
            log.info("Не найдена на удаление страница '{}'", sheetUuid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UUID docUuid = sheetRepository.getDocUuidByUuid(sheetUuid);
        String sheetName = sheetRepository.getSheetNameByUuid(sheetUuid);
        log.warn("Удаление данных страницы '{}' {}...", sheetName, sheetUuid);
        sheetRepository.deleteAllByUuid(sheetUuid);
        log.warn("Страница '{}' {} была удалёна из базы.", sheetName, sheetUuid);


        if (sheetRepository.countByDocUuid(docUuid) == 0) {
            storyRepository.deleteByUuid(docUuid);
            log.warn("Документ '{}' был удалён из базы т.к. у него не осталось страниц.", docUuid);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> deleteAllDataByDocUuid(UUID docUuid) {
        boolean isExists = storyRepository.existsByUuid(docUuid);
        if (!isExists) {
            log.info("Не найден на удаление документ с uuid '{}'", docUuid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String docName = storyRepository.getDocNameUuid(docUuid);

        log.warn("Удаление страниц документа '{}' ({})...", docName, docUuid);
        sheetRepository.deleteAllByDocUuid(docUuid);

        log.warn("Удаление документа '{}' ({})...", docName, docUuid);
        storyRepository.deleteByUuid(docUuid);

        log.warn("Документ '{}' ({}) был удалён из базы.", docName, docUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public Page<FileStory> findAllDocuments(int count, int page) {
        return storyRepository.findAll(Pageable.ofSize(count).withPage(page));
    }

    @Transactional(readOnly = true)
    public Page<SheetDTO> findAllSheets(int count, int page) {
        Page<SheetDTO> found = sheetMapper.toDto(sheetRepository.findAll(Pageable.ofSize(count).withPage(page)));
        found.forEach(s -> s.setItems(null));
        return found;
    }

    // Метод для удаления блочных данных, таких как json или csv:
    public void deleteAllDataBySheetName(String sheetName) {
        log.warn("Удаление блока данных '{}'...", sheetName);
        sheetRepository.deleteAllBySheetNameLike(sheetName + "%");
        log.warn("Блок '{}' был удалён из базы.", sheetName);
    }
}
