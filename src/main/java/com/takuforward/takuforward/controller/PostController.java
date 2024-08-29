package com.takuforward.takuforward.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.takuforward.takuforward.model.AppUser;
import com.takuforward.takuforward.model.Comment;
import com.takuforward.takuforward.model.Post;
import com.takuforward.takuforward.respository.PostRepository;
import com.takuforward.takuforward.respository.UserRepository;
import com.takuforward.takuforward.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    public PostController(PostRepository postRepository, UserRepository userRepository, CommentService commentService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/hello")
    public String getHello() {
        return "Hello World!";
    }


    @PostMapping
    public ResponseEntity<Post> createPost(@RequestParam String username, @RequestBody Post postDetails) {
        AppUser user = userRepository.findByEmail(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }

        postDetails.setUser(user);
        Post savedPost = postRepository.save(postDetails);
        return ResponseEntity.ok(savedPost);
    }

    @PostMapping("/comments/{postId}")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody String content, @RequestParam String username) throws JsonProcessingException {
        System.out.println(username);
        Comment comment = commentService.addComment(postId, username, content);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<Comment> replyToComment(@PathVariable Long commentId, @RequestBody String content, @RequestParam String username) throws JsonProcessingException {
        Comment comment = commentService.replyToComment(commentId, username, content);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsForPost(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, @RequestParam String username) {
        commentService.likeComment(commentId, username);
        return ResponseEntity.ok().build();
    }
}

