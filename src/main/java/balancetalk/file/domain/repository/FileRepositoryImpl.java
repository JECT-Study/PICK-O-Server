package balancetalk.file.domain.repository;

import static balancetalk.file.domain.QFile.file;

import balancetalk.file.domain.File;
import balancetalk.file.domain.FileType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findImgUrlsByResourceIdAndFileType(Long resourceId, FileType fileType) {
        List<File> images = queryFactory.selectFrom(file)
                .where(file.fileType.eq(fileType), file.resourceId.eq(resourceId))
                .fetch();

        return images.stream()
                .map(File::getS3Url)
                .toList();
    }

    @Override
    public List<Long> findIdsByResourceIdAndFileType(Long resourceId, FileType fileType) {
        return queryFactory.select(file.id)
                .from(file)
                .where(file.fileType.eq(fileType), file.resourceId.eq(resourceId))
                .fetch();
    }

    @Override
    public List<File> findAllByResourceIdAndFileType(Long resourceId, FileType fileType) {
        return queryFactory.select(file)
                .from(file)
                .where(file.fileType.eq(fileType), file.resourceId.eq(resourceId))
                .fetch();
    }

    @Override
    public List<File> findAllByResourceIdsAndFileType(List<Long> resourceIds, FileType fileType) {
        return queryFactory.select(file)
                .from(file)
                .where(file.fileType.eq(fileType), file.resourceId.in(resourceIds))
                .fetch();
    }

    @Override
    public Optional<Long> findByResourceId(Long resourceId) {
        return Optional.ofNullable(
                queryFactory.select(file.id)
                        .from(file)
                        .where(file.resourceId.eq(resourceId))
                        .fetchOne()
        );
    }
}
