package com.takuforward.takuforward.respository;

import com.takuforward.takuforward.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
