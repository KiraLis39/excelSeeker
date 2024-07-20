package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.FileStory;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilesStoryRepository extends JpaRepository<FileStory, UUID>, JpaSpecificationExecutor<FileStory> {

    @Modifying
    void deleteByUuid(UUID docUuid);

    Optional<FileStory> findByDocNameAndDocSize(String docName, long docSize);

    boolean existsByUuid(UUID docUuid);

    @Query(value = """
            select fs.docName from FileStory fs where fs.uuid = :docUuid""")
    String getDocNameUuid(UUID docUuid);
}
