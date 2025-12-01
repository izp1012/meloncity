package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.*;
import com.meloncity.citiz.dto.PostReqDto;
import com.meloncity.citiz.dto.PostRespDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.PostRepository;
import com.meloncity.citiz.repository.PostTagRepository;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    private final FileStorageService fileStorageService;

    public void savePost(PostReqDto postReqDto) throws IOException {

        Optional<Profile> optional = profileRepository.findById(postReqDto.getProfileId());
        Profile profile = optional.orElseThrow(() -> new ResourceNotFoundException("Profile", "Id", postReqDto.getProfileId()));

        Post post = new Post(postReqDto.getTitle(), postReqDto.getContent(), profile);

        // 게시물 파일 셋팅
        if(postReqDto.getImages() != null){
            for(MultipartFile file : postReqDto.getImages()){
                String fileDir = fileStorageService.upload(file);
                post.addPhoto(new PostPhoto(fileDir));
            }
        }

        // 태그 저장
        for (String tagName : postReqDto.getTagIds()) {
            Tag tag = tagService.findOrCreate(tagName);
            PostTag postTag = new PostTag(tag);
            post.addTag(postTag);
        }

        postRepository.save(post);
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
    public PostRespDto getPost(Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        PostRespDto postRespDto = new PostRespDto();
        postRespDto.setProfileId(post.getCreatedBy().getId());
        postRespDto.setPostId(post.getId());
        postRespDto.setTitle(post.getTitle());
        postRespDto.setContent(post.getContent());

        // 사진
        List<String> images = new ArrayList<>();
        for(PostPhoto postPhoto : post.getPhotos()){
            images.add("/img/" + postPhoto.getImgUrl());
        }
        postRespDto.setImages(images);

        // 태그
        List<String> tags = new ArrayList<>();
        for(PostTag postTag : post.getPostTags()){
            tags.add(postTag.getTag().getTag());
        }
        postRespDto.setTags(tags);

        return postRespDto;
    }

    public List<Post> getPostAll(){

        return postRepository.findAll();
    }
}
