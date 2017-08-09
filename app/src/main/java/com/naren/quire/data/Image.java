
package com.naren.quire.data;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("img_content_type")
    @Expose
    private String imgContentType;
    private boolean deleted = false;
    private boolean image_changed = false;

    private final static long serialVersionUID = -2797149912139427315L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Image() {
    }

    /**
     * 
     * @param id
     * @param imgContentType
     * @param url
     */
    public Image(Integer id, String url, String imgContentType) {
        super();
        this.id = id;
        this.url = url;
        this.imgContentType = imgContentType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgContentType() {
        return imgContentType;
    }

    public void setImgContentType(String imgContentType) {
        this.imgContentType = imgContentType;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", imgContentType='" + imgContentType + '\'' +
                ", deleted=" + deleted +
                ", image_changed=" + image_changed +
                '}';
    }

    public boolean isImage_changed() {
        return image_changed;
    }

    public void setImage_changed(boolean image_changed) {
        this.image_changed = image_changed;
    }

}
