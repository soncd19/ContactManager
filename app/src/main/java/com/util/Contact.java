package com.util;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public class Contact {
    private long mId;

    private String mNameContact;

    private String mNumberContact;

    private boolean mIsStarred;

    private boolean mIsBlock;

    public Contact(String name, String number){
        this.mNameContact = name;
        this.mNumberContact = number;
    }

    public Contact(long id, String name, String number, boolean isStar, boolean isBlock){
        this.mId = id;
        this.mNameContact = name;
        this.mNumberContact = number;
        this.mIsStarred = isStar;
        this.mIsBlock = isBlock;
    }

    public void setNameContact(String name){
        this.mNameContact = name;
    }
    public void setNumberContact(String number){
        this.mNumberContact = number;
    }
    public void setIdContact(long id){
        this.mId = id;
    }
    public Long getIdContact(){
        return mId;
    }
    public String getNameContact(){
        return mNameContact;
    }
    public String getNumberContact(){
        return mNumberContact;
    }

    public void setIsStar(boolean isStar){
        this.mIsStarred = isStar;
    }

    public boolean getIsStart(){
        return mIsStarred;
    }

    public void setIsBlock(boolean isBlock){
        this.mIsBlock = isBlock;
    }
    public boolean getIsBlock(){
        return mIsBlock;
    }
}
