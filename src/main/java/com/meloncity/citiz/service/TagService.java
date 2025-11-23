package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Tag;
import com.meloncity.citiz.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreate(String tagName) {
        Tag tag = tagRepository.findByTag(tagName).orElse(null);

        if (tag != null) {
            return tag;
        }

        Tag newTag = new Tag(tagName);
        tagRepository.save(newTag);
        return newTag;
    }
}