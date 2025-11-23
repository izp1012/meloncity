package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Post;
import com.meloncity.citiz.domain.PostTag;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.Tag;
import com.meloncity.citiz.dto.PostReqDto;
import com.meloncity.citiz.repository.PostRepository;
import com.meloncity.citiz.repository.PostTagRepository;
import com.meloncity.citiz.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final PostTagRepository postTagRepository;
    private final TagService tagService;

    public void savePost(PostReqDto postReqDto){

        Optional<Profile> optional = profileRepository.findById(postReqDto.getProfileId());
        Profile profile = optional.get();

        Post post = new Post(postReqDto.getTitle(), postReqDto.getContent(), profile);

        postRepository.save(post);

        for (String tagName : postReqDto.getTagIds()) {
            Tag tag = tagService.findOrCreate(tagName);
            PostTag postTag = new PostTag(post, tag);
            postTagRepository.save(postTag);
        }
    }

    public void updatePost(PostReqDto postReqDto){

        Optional<Post> optional = postRepository.findById(postReqDto.getPostId());
        Post post = optional.get();

        postTagRepository.deleteAllByPost(post);
        for (String tagName : postReqDto.getTagIds()) {
            Tag tag = tagService.findOrCreate(tagName);
            PostTag postTag = new PostTag(post, tag);
            postTagRepository.save(postTag);
        }

        post.updatePost(postReqDto.getTitle(), postReqDto.getContent());

        postRepository.save(post);
    }

    public void deletePost(Long id){
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        postTagRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }
    public Post getPost(Long id){
        Optional<Post> optional = postRepository.findById(id);

        return optional.get();
    }

    public List<Post> getPostAll(){

        return postRepository.findAll();
    }
}
