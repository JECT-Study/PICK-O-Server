package balancetalk.file.domain.repository;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileType;
import java.util.List;
import java.util.Optional;

public interface FileRepositoryCustom {

    List<String> findImgUrlsByResourceIdAndFileType(Long resourceId, FileType fileType);

    List<Long> findIdsByResourceIdAndFileType(Long resourceId, FileType fileType);

    List<File> findAllByResourceIdAndFileType(Long resourceId, FileType fileType);

    List<File> findAllByResourceIdsAndFileType(List<Long> resourceIds, FileType fileType);

    Optional<String> findImgUrlByResourceIdAndFileType(Long resourceId, FileType fileType);
}
