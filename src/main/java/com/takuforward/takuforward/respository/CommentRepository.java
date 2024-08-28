package com.takuforward.takuforward.respository;

import com.takuforward.takuforward.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}