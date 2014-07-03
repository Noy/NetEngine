package com.noyhillel.survivalgames.arena;

import lombok.Value;

import java.util.List;

@Value
public final class ArenaMeta {
    private String name;
    private List<String> authors;
    private String socialLink;
}
