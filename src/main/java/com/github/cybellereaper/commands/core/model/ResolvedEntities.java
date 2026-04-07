package com.github.cybellereaper.commands.core.model;

import com.github.cybellereaper.client.ResolvedAttachment;
import com.github.cybellereaper.client.ResolvedChannel;
import com.github.cybellereaper.client.ResolvedMember;
import com.github.cybellereaper.client.ResolvedMessage;
import com.github.cybellereaper.client.ResolvedRole;
import com.github.cybellereaper.client.ResolvedUser;

import java.util.Map;

public record ResolvedEntities(
        Map<String, ResolvedUser> users,
        Map<String, ResolvedMember> members,
        Map<String, ResolvedRole> roles,
        Map<String, ResolvedChannel> channels,
        Map<String, ResolvedMessage> messages,
        Map<String, ResolvedAttachment> attachments
) {
    public static ResolvedEntities empty() {
        return new ResolvedEntities(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    public ResolvedEntities {
        users = users == null ? Map.of() : Map.copyOf(users);
        members = members == null ? Map.of() : Map.copyOf(members);
        roles = roles == null ? Map.of() : Map.copyOf(roles);
        channels = channels == null ? Map.of() : Map.copyOf(channels);
        messages = messages == null ? Map.of() : Map.copyOf(messages);
        attachments = attachments == null ? Map.of() : Map.copyOf(attachments);
    }
}
