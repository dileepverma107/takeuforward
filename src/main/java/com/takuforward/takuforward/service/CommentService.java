package com.takuforward.takuforward.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takuforward.takuforward.model.Comment;
import com.takuforward.takuforward.model.CommentLike;
import com.takuforward.takuforward.model.Post;
import com.takuforward.takuforward.model.AppUser;
import com.takuforward.takuforward.respository.CommentRepository;
import com.takuforward.takuforward.respository.LikeRepository;
import com.takuforward.takuforward.respository.PostRepository;
import com.takuforward.takuforward.respository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ObjectMapper objectMapper;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, LikeRepository likeRepository, ObjectMapper objectMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Comment addComment(Long postId, String username, String content) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(content);
        content = jsonNode.get("content").asText();
        AppUser user = userRepository.findByEmail(username);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment replyToComment(Long commentId, String username, String content) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(content);
        content = jsonNode.get("content").asText();
        AppUser user = userRepository.findByEmail(username);
        Comment parentComment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setParentComment(parentComment);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getComments();
    }

    public void likeComment(Long commentId, String username) {
        AppUser user = userRepository.findByEmail(username);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));

        CommentLike like = new CommentLike();
        like.setComment(comment);
        like.setUser(user);

        // Ensure user hasn't already liked the comment
        if (likeRepository.countByCommentAndUser(comment, user) == 0) {
            likeRepository.save(like);
        }
    }
}

