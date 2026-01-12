package com.booknara.booknaraPrj.events.dto;


import lombok.ToString;

@ToString
public class ImageDTO {
    private Long imageId;
    private String imgType;
    private String imgUrl;

    // 기본 생성자
    public ImageDTO() {}

    // 생성자
    public ImageDTO(Long imageId, String imgType, String imgUrl) {
        this.imageId = imageId;
        this.imgType = imgType;
        this.imgUrl = imgUrl;
    }

    // Getter & Setter
    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }

    public String getImgType() { return imgType; }
    public void setImgType(String imgType) { this.imgType = imgType; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
}
