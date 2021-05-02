package com.codesample.mymemo.data;

import android.net.Uri;

public class Memo {
    public int memoid;
    public String userid;
    public String title;
    public String content;
    public String fileUrl; // 서버 이미지용 데이터.
    public Uri fileUri; // 업로드용 데이터.
    public String originalFileName;
    public long savedTime;

    public Memo(){ }
    // 파일 업로드 하지 않을 때.
    public Memo(String userid, String title, String content, long savedTime){
        this.userid=userid; this.title=title; this.content=content; this.savedTime=savedTime;
    }
    // 파일 업로드 할 때.
    public Memo(String userid, String title, String content, Uri fileUri, String originalFileName, long savedTime){
        this.userid=userid; this.title=title; this.content=content; this.savedTime=savedTime;
        this.fileUri=fileUri; this.originalFileName=originalFileName;
    }
}
