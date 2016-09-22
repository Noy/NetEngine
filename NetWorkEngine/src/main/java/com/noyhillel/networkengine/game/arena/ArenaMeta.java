package com.noyhillel.networkengine.game.arena;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public final class ArenaMeta {
    private String name;
    private List<String> authors;
    private String socialLink;
}
