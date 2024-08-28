package com.takuforward.takuforward.respository;

import com.takuforward.takuforward.model.Comment;
import com.takuforward.takuforward.model.CommentLike;
import com.takuforward.takuforward.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<CommentLike, Long> {
    long countByCommentAndUser(Comment comment, AppUser user);
}
