package com.teamloci.loci.repository;

import com.teamloci.loci.domain.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("SELECT c FROM PostComment c " +
            "JOIN FETCH c.user " +
            "WHERE c.post.id = :postId " +
            "AND (:cursorId IS NULL OR c.id < :cursorId) " +
            "ORDER BY c.id DESC")
    List<PostComment> findByPostIdWithCursor(
            @Param("postId") Long postId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    long countByPostId(Long postId);
}