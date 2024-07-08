package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.FileStory;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilesStoryRepository extends JpaRepository<FileStory, Long>, JpaSpecificationExecutor<FileStory> {

    @Modifying
    void deleteByDocName(String docName);

    @Modifying
    void deleteByUuid(UUID uuid);

    Optional<FileStory> findByDocNameAndDocSize(String docName, long docSize);
}
