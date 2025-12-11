package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PostRespDto {
    private Long profileId;
    private String profileName;
    private String profileImg;

    // post Entity
    private Long postId;
    private String title;
    private String content;
    private String createdAt;
    private String views = "0";

    private int likeCount;        // 좋아요 수
    private boolean liked;        // 사용자가 좋아요 눌렀는지 여부(엔티티보다는 DTO에 적합)
    private boolean bookmarked;   // 북마크
    private String rating = "0.0";        // 평점

    // 주소정보
    private String locationName;  // 장소 이름
    private String address;       // 주소
    private String openingHours;  // 운영 시간
    private Double latitude;      // 위도(선택)
    private Double longitude;     // 경도(선택)


    //post_photo Entity
    private List<String> images; // 파일 경로

    //post_tag Entity
    private List<String> tags;

    //comment
    private int commentCount;     // 댓글 수
    private List<CommentResDto> comments = new ArrayList<>();
}
