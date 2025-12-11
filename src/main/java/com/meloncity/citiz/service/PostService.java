package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.*;
import com.meloncity.citiz.dto.CommentResDto;
import com.meloncity.citiz.dto.CustomUserDetails;
import com.meloncity.citiz.dto.PostReqDto;
import com.meloncity.citiz.dto.PostRespDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.PostRepository;
import com.meloncity.citiz.repository.PostTagRepository;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.TimeAgoUtil;
import com.meloncity.citiz.util.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        // 게시물 파일 저장
        if(postReqDto.getImages() != null){
            for(MultipartFile file : postReqDto.getImages()){
                String fileDir = fileStorageService.upload(file);
                post.addPhoto(new PostPhoto(fileDir));
            }
        }

        // 태그 저장
        for (String tagName : postReqDto.getTagIds()) {
            Tag tag = tagService.findOrCreate(tagName);
            PostTag postTag = new PostTag(post, tag);
            post.addTag(postTag);
        }

        postRepository.save(post);
    }

    public String updatePost(Long id, PostReqDto postReqDto, CustomUserDetails user){
        String result = "SUCCESS";

        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if(!post.getCreatedBy().getId().equals(user.getId())){
            result = "PERMISSION DENIED";
        }else{
            // 게시물 내용 변경, 파일/태그 제외
            post.updatePost(postReqDto);

            // 2. 새 태그 목록 (중복 방지용 Set)
            Set<String> newTagNames = new HashSet<>(postReqDto.getTagIds());

            // 3. 현재 글에 달려있는 태그 이름들
            List<PostTag> currentPostTags = post.getPostTags();
            Set<String> currentTagNames = currentPostTags.stream()
                    .map(pt -> pt.getTag().getTag())
                    .collect(Collectors.toSet());

            // 4. 삭제 대상: 기존에는 있었지만, 새 목록에는 없는 태그들
            //    → PostTag 엔티티 삭제 (orphanRemoval에 맡김)
            currentPostTags.removeIf(pt -> !newTagNames.contains(pt.getTag().getTag()));

            // 5. 추가 대상: 새 목록에는 있는데, 기존에는 없던 태그들
            Set<String> toAdd = newTagNames.stream()
                    .filter(tagName -> !currentTagNames.contains(tagName))
                    .collect(Collectors.toSet());

            // 태그 변경
            for (String tagName : toAdd) {
                Tag tag = tagService.findOrCreate(tagName);
                PostTag postTag = new PostTag(post, tag);
                post.addTag(postTag);
            }

            postRepository.save(post);
        }

        return result;
    }

    public String deletePost(Long id, CustomUserDetails user) throws IOException{
        String result = "SUCCESS";
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if(!post.getCreatedBy().getId().equals(user.getId())){
            result = "PERMISSION DENIED";
        }else{
            //게시물 파일 삭제
            List<PostPhoto> photos = post.getPhotos();
            for(PostPhoto postPhoto: photos){
                fileStorageService.delete(postPhoto.getImgUrl());
            }

            postRepository.delete(post);
        }

        return result;
    }
    public PostRespDto getPost(Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        PostRespDto postRespDto = new PostRespDto();
        postRespDto.setProfileId(post.getCreatedBy().getId());
        postRespDto.setProfileName(post.getCreatedBy().getName());
        postRespDto.setProfileImg(post.getCreatedBy().getImageUrl());
        postRespDto.setPostId(post.getId());
        postRespDto.setTitle(post.getTitle());
        postRespDto.setContent(post.getContent());

        // 생성된 시간
        postRespDto.setCreatedAt(TimeAgoUtil.formatTime(post.getCreateDate()));

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

        // 댓글
        List<CommentResDto> comments = new ArrayList<>();
        for(Comment comment : post.getComments()){
            CommentResDto commentDto = new CommentResDto(comment.getCreatedBy(), comment);
            comments.add(commentDto);
        }
        postRespDto.setComments(comments);

        return postRespDto;
    }

    public List<Post> getPostAll(){

        return postRepository.findAll();
    }
}
