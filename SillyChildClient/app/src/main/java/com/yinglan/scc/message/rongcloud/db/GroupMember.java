package com.yinglan.scc.message.rongcloud.db;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

import android.net.Uri;

import io.rong.imlib.model.UserInfo;

/**
 * Entity mapped to table GROUP_MEMBER.
 */
public class GroupMember extends UserInfo {

    /** Not-null value. */
    private String groupId;
    private String displayName;
    private String nameSpelling;
    private String displayNameSpelling;
    private String groupName;
    private String groupNameSpelling;
    private String groupPortraitUri;

    public GroupMember(String userId, String name, Uri portraitUri) {
        super(userId, name, portraitUri);
    }

    public GroupMember(String groupId, String userId, String name, Uri portraitUri, String displayName, String nameSpelling, String displayNameSpelling, String groupName, String groupNameSpelling, String groupPortraitUri) {
        super(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
        this.groupName = groupName;
        this.groupNameSpelling = groupNameSpelling;
        this.groupPortraitUri = groupPortraitUri;
    }

    public GroupMember(String groupId, String userId, String name, Uri portraitUri, String displayName, String nameSpelling, String displayNameSpelling, String groupName, String groupNameSpelling) {
        super(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
        this.groupName = groupName;
        this.groupNameSpelling = groupNameSpelling;
    }

    public GroupMember(String groupId, String userId, String name, Uri portraitUri, String displayName) {
        super(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
    }

    /** Not-null value. */
    public String getGroupId() {
        return groupId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public String getDisplayNameSpelling() {
        return displayNameSpelling;
    }

    public void setDisplayNameSpelling(String displayNameSpelling) {
        this.displayNameSpelling = displayNameSpelling;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupNameSpelling() {
        return groupNameSpelling;
    }

    public void setGroupNameSpelling(String groupNameSpelling) {
        this.groupNameSpelling = groupNameSpelling;
    }

    public String getGroupPortraitUri() {
        return groupPortraitUri;
    }

    public void setGroupPortraitUri(String groupPortraitUri) {
        this.groupPortraitUri = groupPortraitUri;
    }
}
